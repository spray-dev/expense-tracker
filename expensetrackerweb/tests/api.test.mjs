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
