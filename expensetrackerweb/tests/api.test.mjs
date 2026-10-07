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
