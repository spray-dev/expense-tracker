import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import ts from 'typescript';

const source = await readFile(new URL('../src/lib/expense-date.ts', import.meta.url), 'utf8');
const js = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext } }).outputText;
const { parseExpenseDate, expenseDateFields } = await import('data:text/javascript;base64,' + Buffer.from(js).toString('base64'));

test('Brazilian create date 05/10 is October 5, with unchanged local time', () => {
  assert.equal(parseExpenseDate('05/10/2026', '19:30'), '2026-10-05T19:30');
  assert.equal(parseExpenseDate('10/05/2026', '00:00'), '2026-05-10T00:00');
});

test('edit displays Brazilian date and round trips seconds and fractions', () => {
  for (const iso of ['2026-10-05T19:30', '2026-10-05T23:59:42', '2026-01-01T00:00:00.123456789']) {
    const fields = expenseDateFields(iso);
    assert.equal(parseExpenseDate(fields.date, fields.time), iso);
  }
  assert.deepEqual(expenseDateFields('2026-10-05T19:30:00'), { date: '05/10/2026', time: '19:30:00' });
});

test('rejects impossible dates, malformed dates and non-leap years in Portuguese', () => {
  for (const date of ['31/02/2026', '31/04/2026', '29/02/2026', '29/02/1900', '00/10/2026', '05/00/2026', '05/13/2026', '05/10/0000', '5/10/2026', '2026-10-05', '']) {
    assert.throws(() => parseExpenseDate(date, '12:00'), /data válida.*DD\/MM\/AAAA/);
  }
  assert.equal(parseExpenseDate('29/02/2024', '12:00'), '2024-02-29T12:00');
  assert.equal(parseExpenseDate('29/02/2000', '12:00'), '2000-02-29T12:00');
});

test('validates 24-hour time and allows whitespace without timezone conversion', () => {
  for (const time of ['', '24:00', '12:60', '19:30:60', '7:30', '07:30 PM', '12:00Z']) {
    assert.throws(() => parseExpenseDate('05/10/2026', time), /hora válida.*24h/);
  }
  assert.equal(parseExpenseDate(' 05/10/2026 ', ' 07:30 '), '2026-10-05T07:30');
});
