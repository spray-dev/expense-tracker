import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { createElement } from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter } from 'react-router-dom';
import ts from 'typescript';

const moduleURL = source => 'data:text/javascript;base64,' + Buffer.from(source).toString('base64');
const dependency = name => import.meta.resolve(name);
// Keep the real auth page and router; replace only session state and visual components.
const authContext = moduleURL('export function useAuth() { return globalThis.recoveryTestAuth; }');
const visuals = moduleURL('import { createElement } from ' + JSON.stringify(dependency('react')) + '; export const Brand = () => null; export const ThemeToggle = () => null; export const Input = props => createElement("input", props); export const Button = ({variant, size, ...props}) => createElement("button", props);');
const api = moduleURL('export const passwordRules = { minLength: 8, maxLength: 100 }; export const authError = () => ""; export const passwordResetError = () => ""; export const validateNewPassword = () => ""; export const forgotPassword = async () => ""; export const resetPassword = async () => {};');
let source = await readFile(new URL('../src/pages/auth.tsx', import.meta.url), 'utf8');
for (const [specifier, url] of Object.entries({
  '@/auth/context': authContext, '@/lib/api': api,
  '@/components/ui/button': visuals, '@/components/ui/input': visuals, '@/components/shared': visuals,
  'react': dependency('react'), 'react-router-dom': dependency('react-router-dom'), 'lucide-react': dependency('lucide-react'),
})) source = source.replace(JSON.stringify(specifier), JSON.stringify(url));
const compiled = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022, jsx: ts.JsxEmit.ReactJSX } }).outputText.replace('"react/jsx-runtime"', JSON.stringify(dependency('react/jsx-runtime')));
const { default: Auth } = await import(moduleURL(compiled));
function render(path, props, session = { user: null, loading: false }) {
  globalThis.recoveryTestAuth = session;
  return renderToStaticMarkup(createElement(MemoryRouter, { initialEntries: [path] }, createElement(Auth, props)));
}
await test('login offers recovery; registration retains its existing password bounds', () => {
  assert.match(render('/login', {}), /href="\/forgot-password"/);
  const registration = render('/register', { register: true });
  assert.match(registration, /minLength="8"/);
  assert.match(registration, /maxLength="100"/);
});
await test('forgot password displays only email and remains public during session loading or login', () => {
  for (const session of [{ user: null, loading: true }, { user: { id: 1 }, loading: false }, { user: null, loading: false }]) {
    const html = render('/forgot-password', { recovery: 'forgot' }, session);
    assert.match(html, /name="email"/);
    assert.doesNotMatch(html, /name="password"|name="username"/);
    assert.match(html, /Enviar link/);
    assert.match(html, /href="\/login"/);
  }
});
await test('reset link displays only new password with registration rules and new-password autocomplete', () => {
  const html = render('/reset-password?token=url-safe_token', { recovery: 'reset' }, { user: { id: 1 }, loading: true });
  assert.match(html, /name="password"/);
  assert.match(html, /autoComplete="new-password"/);
  assert.match(html, /minLength="8"/);
  assert.match(html, /maxLength="100"/);
  assert.doesNotMatch(html, /name="email"|name="username"|url-safe_token/);
  assert.match(html, /href="\/forgot-password"/);
});
await test('missing or blank token shows an accessible recovery message instead of a submit form', () => {
  for (const path of ['/reset-password', '/reset-password?token=', '/reset-password?token=%20']) {
    const html = render(path, { recovery: 'reset' });
    assert.match(html, /role="alert"/);
    assert.match(html, /não contém um token/);
    assert.doesNotMatch(html, /<form/);
    assert.match(html, /href="\/forgot-password"/);
  }
});
