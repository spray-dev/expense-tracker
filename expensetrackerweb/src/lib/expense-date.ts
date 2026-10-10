/** LocalDateTime values are calendar fields, never UTC instants. */
export function expenseDateFields(iso: string) {
  const [date, time] = iso.split("T");
  const [year, month, day] = date.split("-");
  return { date: `${day}/${month}/${year}`, time };
}

export function parseExpenseDate(date: string, time: string): string {
  const match = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(date.trim());
  const dateError = "Informe uma data válida no formato DD/MM/AAAA.";
  if (!match) throw new Error(dateError);
  const [, day, month, year] = match;
  const d = Number(day), m = Number(month), y = Number(year);
  const leap = y % 4 === 0 && (y % 100 !== 0 || y % 400 === 0);
  const days = [31, leap ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
  if (y < 1 || m < 1 || m > 12 || d < 1 || d > days[m - 1]) throw new Error(dateError);
  const localTime = time.trim();
  if (!/^([01]\d|2[0-3]):[0-5]\d(?::[0-5]\d(?:\.\d{1,9})?)?$/.test(localTime)) {
    throw new Error("Informe uma hora válida no formato HH:mm (24h), por exemplo 19:30.");
  }
  return `${year}-${month}-${day}T${localTime}`;
}
