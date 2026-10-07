import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import ts from 'typescript';
const values = new Map([['expense-auth-token', 'restored-token']]);
globalThis.sessionStorage = { getItem: key => values.get(key) ?? null, setItem: (key, value) => values.set(key, value), removeItem: key => values.delete(key) };
globalThis.window = new EventTarget();
let ended = 0;
window.addEventListener('expense-session-ended', () => ended++);
const axiosURL = new URL('../node_modules/axios/index.js', import.meta.url).href;
const source = (await readFile(new URL('../src/lib/api.ts', import.meta.url), 'utf8')).replace('"axios"', JSON.stringify(axiosURL)).replace('import.meta.env.VITE_API_BASE_URL', 'undefined');
const js = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 } }).outputText;
const { api, getToken, setToken } = await import('data:text/javascript;base64,' + Buffer.from(js).toString('base64'));
await test('restores token and adds Bearer only to protected requests', async () => {
  assert.equal(getToken(), 'restored-token');
  let sent;
  api.defaults.adapter = async config => { sent = config.headers.Authorization; return { data: {}, status: 200, statusText: 'OK', headers: {}, config }; };
  await api.get('/api/users/me'); assert.equal(sent, 'Bearer restored-token');
  await api.post('/api/auth/login', {}); assert.equal(sent, undefined);
  setToken('new-token'); assert.equal(values.get('expense-auth-token'), 'new-token');
});
await test('login 401 preserves session; protected 401 clears storage and signals logout', async () => {
  api.defaults.adapter = async config => { throw { config, response: { status: 401 } }; };
  await assert.rejects(api.post('/api/auth/login', {})); assert.equal(getToken(), 'new-token'); assert.equal(ended, 0);
  await assert.rejects(api.get('/api/users/me')); assert.equal(getToken(), null); assert.equal(values.has('expense-auth-token'), false); assert.equal(ended, 1);
});
await test('late 401 from previous session does not clear a newer login', async () => {
  setToken('old-token');
  let reject;
  api.defaults.adapter = config => new Promise((_resolve, fail) => { reject = () => fail({ config, response: { status: 401 } }); });
  const request = api.get('/api/users/me');
  await new Promise(resolve => setImmediate(resolve));
  setToken('current-token'); reject(); await assert.rejects(request);
  assert.equal(getToken(), 'current-token'); assert.equal(ended, 1);
});

const apiModuleURL = 'data:text/javascript;base64,' + Buffer.from(js).toString('base64');
const dashboardSource = (await readFile(new URL('../src/lib/dashboard.ts', import.meta.url), 'utf8')).replace('"./api"', JSON.stringify(apiModuleURL));
const dashboardJS = ts.transpileModule(dashboardSource, { compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 } }).outputText;
const { getDashboard, presentExpense, categoryLabel } = await import('data:text/javascript;base64,' + Buffer.from(dashboardJS).toString('base64'));
await test('dashboard sends selected year/month with the current JWT and preserves server data', async () => {
  const payload = { monthlyTotal: 825, averageSpending: 275, expenseCount: 3, budget: null, recentExpenses: [], largestExpenses: [] };
  api.defaults.adapter = async config => {
    assert.equal(config.url, '/api/expenses/dashboard');
    assert.deepEqual(config.params, { year: 2025, month: 12 });
    assert.equal(config.headers.Authorization, 'Bearer current-token');
    return { data: payload, status: 200, statusText: 'OK', headers: {}, config };
  };
  assert.deepEqual(await getDashboard('2025-12'), payload);
});
await test('dashboard aborts canceled month requests and propagates failures for retry', async () => {
  const controller = new AbortController(); controller.abort();
  await assert.rejects(getDashboard('2026-10', controller.signal), error => error.code === 'ERR_CANCELED');
  api.defaults.adapter = async () => { throw new Error('offline'); };
  await assert.rejects(getDashboard('2026-10'), /offline/);
});
await test('backend category labels preserve unrecognized categories and timestamps', () => {
  assert.equal(categoryLabel('FOOD'), 'Alimentação');
  assert.equal(categoryLabel('FUTURE'), 'FUTURE');
  const expense = presentExpense({ id: 1, description: 'Compra', category: 'HOME', amount: 25, date: '2026-10-07T15:30:00' });
  assert.equal(expense.category, 'Moradia'); assert.equal(expense.date, '2026-10-07T15:30:00');
});
const formatSource = await readFile(new URL('../src/lib/format.ts', import.meta.url), 'utf8');
const formatJS = ts.transpileModule(formatSource, { compilerOptions: { module: ts.ModuleKind.ESNext } }).outputText;
const { dateLabel } = await import('data:text/javascript;base64,' + Buffer.from(formatJS).toString('base64'));
await test('Brazilian dates accept backend timestamps and demo date-only values', () => {
  assert.equal(dateLabel('2026-10-07T23:30:00'), dateLabel('2026-10-07'));
  assert.match(dateLabel('2026-10-07T23:30:00'), /7/);
});

const financeSource = (await readFile(new URL('../src/lib/finance.ts', import.meta.url), 'utf8'))
  .replace('"./api"', JSON.stringify(apiModuleURL)).replace('"axios"', JSON.stringify(axiosURL));
const financeJS = ts.transpileModule(financeSource, { compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 } }).outputText;
const finance = await import('data:text/javascript;base64,' + Buffer.from(financeJS).toString('base64'));
const filters = { month: '2024-02', description: ' café ', category: 'FOOD', period: 'SELECTED_MONTH', start: '', end: '', sort: 'amount:asc', page: 2 };
await test('expense filters use inclusive leap-month boundaries and server pagination', async () => {
  api.defaults.adapter = async config => {
    assert.equal(config.url, '/api/expenses'); assert.equal(config.headers.Authorization, 'Bearer current-token');
    assert.deepEqual(config.params, { page: 2, size: 6, sortBy: 'amount', direction: 'asc', description: 'café', category: 'FOOD', startDate: '2024-02-01T00:00:00', endDate: '2024-02-29T23:59:59.999999' });
    return { data: { content: [], page: 2, totalElements: 12, totalPages: 2 }, status: 200, headers: {}, config };
  };
  assert.equal((await finance.listExpenses(filters)).totalElements, 12);
  assert.equal(finance.expenseParams({ ...filters, month: '2024-12' }).endDate, '2024-12-31T23:59:59.999999');
});
await test('custom ranges validate paired dates and periods never send conflicting ranges', () => {
  assert.throws(() => finance.expenseParams({ ...filters, period: 'CUSTOM' }), /intervalo/);
  assert.throws(() => finance.expenseParams({ ...filters, period: 'CUSTOM', start: '2026-10-08', end: '2026-10-07' }), /intervalo/);
  const custom = finance.expenseParams({ ...filters, period: 'CUSTOM', start: '2026-10-07', end: '2026-10-07' });
  assert.equal(custom.endDate, '2026-10-07T23:59:59.999999'); assert.equal(custom.period, undefined);
  const relative = finance.expenseParams({ ...filters, period: 'LAST_7_DAYS' });
  assert.equal(relative.period, 'LAST_7_DAYS'); assert.equal(relative.startDate, undefined);
  assert.equal(finance.expenseParams({ ...filters, period: 'ALL' }).startDate, undefined);
});
await test('expense create edit delete and export use authenticated backend contracts', async () => {
  const requests = [];
  api.defaults.adapter = async config => { requests.push(config); assert.equal(config.headers.Authorization, 'Bearer current-token'); return { data: { id: 9 }, status: 200, headers: {}, config }; };
  const input = { description: 'Café', amount: 10.50, date: '2026-10-07T10:30:15', category: 'FOOD' };
  await finance.saveExpense(input); await finance.saveExpense(input, 9); await finance.deleteExpense(9); await finance.exportExpenses();
  assert.deepEqual(requests.map(({ method, url }) => [method, url]), [['post', '/api/expenses'], ['patch', '/api/expenses/9'], ['delete', '/api/expenses/9'], ['get', '/api/expenses/export']]);
  assert.deepEqual(JSON.parse(requests[0].data), input); assert.deepEqual(JSON.parse(requests[1].data), input);
  assert.equal(requests[3].responseType, 'blob');
});
await test('missing budget is distinct from server failure and save uses selected month', async () => {
  api.defaults.adapter = async config => { throw { isAxiosError: true, config, response: { status: 404 } }; };
  assert.equal(await finance.getBudget('2026-10'), null);
  api.defaults.adapter = async config => { throw { isAxiosError: true, config, response: { status: 500 } }; };
  await assert.rejects(finance.getBudget('2026-10'));
  api.defaults.adapter = async config => {
    assert.equal(config.method, 'put'); assert.deepEqual(config.params, { year: 2025, month: 12 }); assert.deepEqual(JSON.parse(config.data), { amount: 1200 });
    return { data: { budget: 1200, spent: 1300, remaining: -100, isOverBudget: true }, status: 200, headers: {}, config };
  };
  assert.equal((await finance.saveBudget('2025-12', 1200)).isOverBudget, true);
});
await test('analytics uses existing endpoints and six-month range across year rollover', async () => {
  const requests = [];
  api.defaults.adapter = async config => { requests.push(config); return { data: config.url.endsWith('monthly-category-summary') ? { categoryTotals: {} } : config.url.endsWith('yearly-summary') ? { year: 2026, total: 0, monthlyTotals: [] } : [], status: 200, headers: {}, config }; };
  const result = await finance.getAnalytics('2026-02');
  assert.deepEqual(requests[0].params, { startYear: 2025, startMonth: 9, endYear: 2026, endMonth: 2 });
  assert.deepEqual(requests[1].params, { year: 2026, month: 2 }); assert.deepEqual(requests[3].params, { year: 2026 });
  assert.deepEqual(result.categories, {}); assert.equal(result.yearly.total, 0);
  assert.equal(requests.length, 4);
});
await test('profile loads and updates individual fields; account delete uses current user', async () => {
  const requests = [];
  api.defaults.adapter = async config => { requests.push(config); return { data: { id: 1, username: 'Ana', email: 'ana@example.com' }, status: 200, headers: {}, config }; };
  await finance.getProfile(); await finance.updateProfile('username', 'Ana'); await finance.updateProfile('email', 'ana@example.com'); await finance.updateProfile('password', 'test-password'); await finance.deleteAccount();
  assert.deepEqual(requests.map(({ method, url }) => [method, url]), [['get', '/api/users/me'], ['patch', '/api/users/me/username'], ['patch', '/api/users/me/email'], ['patch', '/api/users/me/password'], ['delete', '/api/users/me']]);
  assert.deepEqual(JSON.parse(requests[3].data), { password: 'test-password' });
  setToken(null); assert.equal(values.has('expense-auth-token'), false);
});
await test('integrated requests cancel on navigation and propagate errors for retry', async () => {
  const controller = new AbortController(); controller.abort();
  await assert.rejects(finance.listExpenses(filters, controller.signal), error => error.code === 'ERR_CANCELED');
  await assert.rejects(finance.getBudget('2026-10', controller.signal), error => error.code === 'ERR_CANCELED');
  api.defaults.adapter = async () => { throw new Error('offline'); };
  await assert.rejects(finance.getAnalytics('2026-10'), /offline/);
  assert.match(finance.requestError({ isAxiosError: true, response: { status: 409 } }), /nome de usuário ou e-mail/);
});


await test('account deletion clears auth only after success and preserves newer sessions', async () => {
  let logoutCount = 0;
  const logout = () => { logoutCount++; setToken(null); };
  setToken('delete-session');
  api.defaults.adapter = async () => { throw new Error('offline'); };
  await assert.rejects(finance.deleteCurrentAccount(logout), /offline/);
  assert.equal(logoutCount, 0); assert.equal(getToken(), 'delete-session');
  api.defaults.adapter = async config => ({ data: '', status: 204, headers: {}, config });
  await finance.deleteCurrentAccount(logout);
  assert.equal(logoutCount, 1); assert.equal(getToken(), null); assert.equal(values.has('expense-auth-token'), false);
  setToken('older-session');
  let finish;
  api.defaults.adapter = config => new Promise(resolve => { finish = () => resolve({ data: '', status: 204, headers: {}, config }); });
  const deleting = finance.deleteCurrentAccount(logout);
  await new Promise(resolve => setImmediate(resolve));
  setToken('newer-session'); finish(); await deleting;
  assert.equal(logoutCount, 1); assert.equal(getToken(), 'newer-session');
});

await test('deleting the last item of a page falls back to a valid server page', async () => {
  const pages = [];
  api.defaults.adapter = async config => {
    pages.push(config.params.page);
    return { data: { content: config.params.page === 0 ? [{ id: 1 }] : [], page: config.params.page, size: 6, totalElements: 1, totalPages: 1 }, status: 200, headers: {}, config };
  };
  const result = await finance.listExpensePage({ ...filters, page: 1 });
  assert.deepEqual(pages, [1, 0]); assert.equal(result.page, 0); assert.equal(result.content[0].id, 1);
});
