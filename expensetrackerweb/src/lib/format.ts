// Presentation defaults only, not an API contract.
export const money = (value: number) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    value,
  );
export const dateLabel = (value: string) =>
  new Intl.DateTimeFormat("pt-BR", { month: "short", day: "numeric" }).format(
    new Date(`${value}T12:00:00`),
  );
