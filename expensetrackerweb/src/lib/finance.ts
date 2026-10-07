import axios from "axios";
import { api, getToken, type User } from "./api";
import type { DashboardExpense } from "./dashboard";

export type Expense = DashboardExpense;
export type ExpenseInput = Omit<Expense, "id">;
export type ExpensePage = { content: Expense[]; page: number; size: number; totalElements: number; totalPages: number };
export type BudgetResponse = { id: number; year: number; month: number; budget: number; spent: number; remaining: number; percentageSpent: number; isOverBudget: boolean };
export type MonthlyPoint = { year: number; month: number; total: number };
export type YearlySummary = { year: number; total: number; monthlyTotals: MonthlyPoint[] };
export type Filters = { month: string; description: string; category: string; period: string; start: string; end: string; sort: string; page: number };
export function monthParams(month: string) {
  const [year, selectedMonth] = month.split("-").map(Number);
  return { year, month: selectedMonth };
}
function lastDay(month: string) {
  const { year, month: selectedMonth } = monthParams(month);
  return `${month}-${String(new Date(year, selectedMonth, 0).getDate()).padStart(2, "0")}`;
}
export function expenseParams(filters: Filters) {
  const [sortBy, direction] = filters.sort.split(":");
  const params: Record<string, string | number> = { page: filters.page, size: 6, sortBy, direction };
  if (filters.description.trim()) params.description = filters.description.trim();
  if (filters.category) params.category = filters.category;
  if (filters.period === "SELECTED_MONTH" || filters.period === "CUSTOM") {
    const start = filters.period === "CUSTOM" ? filters.start : `${filters.month}-01`;
    const end = filters.period === "CUSTOM" ? filters.end : lastDay(filters.month);
    if (!start || !end || start > end) throw new Error("Informe um intervalo de datas válido.");
    params.startDate = `${start}T00:00:00`;
    // The API's custom range is inclusive; PostgreSQL stores microseconds.
    params.endDate = `${end}T23:59:59.999999`;
  } else if (filters.period !== "ALL") params.period = filters.period;
  return params;
}
export async function listExpenses(filters: Filters, signal?: AbortSignal) {
  return (await api.get<ExpensePage>("/api/expenses", { params: expenseParams(filters), signal })).data;
}
export async function listExpensePage(filters: Filters, signal?: AbortSignal) {
  const result = await listExpenses(filters, signal);
  if (result.page > 0 && result.page >= result.totalPages) {
    return listExpenses({ ...filters, page: Math.max(0, result.totalPages - 1) }, signal);
  }
  return result;
}
export async function saveExpense(input: ExpenseInput, id?: number) {
  return (id === undefined ? await api.post<Expense>("/api/expenses", input) : await api.patch<Expense>(`/api/expenses/${id}`, input)).data;
}
export async function deleteExpense(id: number) { await api.delete(`/api/expenses/${id}`); }
export async function exportExpenses() { return (await api.get<Blob>("/api/expenses/export", { responseType: "blob" })).data; }
export async function getBudget(month: string, signal?: AbortSignal) {
  try { return (await api.get<BudgetResponse>("/api/budgets/monthly", { params: monthParams(month), signal })).data; }
  catch (error) { if (axios.isAxiosError(error) && error.response?.status === 404) return null; throw error; }
}
export async function saveBudget(month: string, amount: number) {
  return (await api.put<BudgetResponse>("/api/budgets/monthly", { amount }, { params: monthParams(month) })).data;
}
export async function getAnalytics(month: string, signal?: AbortSignal) {
  const params = monthParams(month);
  const start = new Date(params.year, params.month - 6, 1);
  const [trend, categories, top, yearly] = await Promise.all([
    api.get<MonthlyPoint[]>("/api/expenses/monthly-totals-over-time", { params: { startYear: start.getFullYear(), startMonth: start.getMonth() + 1, endYear: params.year, endMonth: params.month }, signal }),
    api.get<{ categoryTotals: Record<string, number> }>("/api/expenses/monthly-category-summary", { params, signal }),
    api.get<{ category: string; total: number }[]>("/api/expenses/top-spending-categories", { params, signal }),
    api.get<YearlySummary>("/api/expenses/yearly-summary", { params: { year: params.year }, signal }),
  ]);
  return { trend: trend.data, categories: categories.data.categoryTotals, top: top.data, yearly: yearly.data };
}
export async function getProfile(signal?: AbortSignal) { return (await api.get<User>("/api/users/me", { signal })).data; }
export async function updateProfile(field: "username" | "email" | "password", value: string) {
  return (await api.patch<User>(`/api/users/me/${field}`, { [field]: value })).data;
}
export async function deleteAccount() { await api.delete("/api/users/me"); }
export async function deleteCurrentAccount(logout: () => void) {
  const session = getToken();
  await deleteAccount();
  if (getToken() === session) logout();
}
export function requestError(error: unknown): string {
  if (!axios.isAxiosError(error)) return error instanceof Error ? error.message : "Não foi possível concluir. Tente novamente.";
  if (!error.response) return "Não foi possível conectar ao servidor. Tente novamente.";
  if (error.response.status === 400) return "Confira os dados e os limites dos campos informados.";
  if (error.response.status === 409) return "Este nome de usuário ou e-mail já está em uso.";
  if (error.response.status === 401) return "Sua sessão expirou. Entre novamente.";
  if (error.response.status === 404) return "O registro não foi encontrado. Atualize a página.";
  return "Não foi possível concluir a solicitação. Tente novamente.";
}
