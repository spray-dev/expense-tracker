import { api } from "./api";
export type DashboardExpense = { id: number; description: string; amount: number; date: string; category: string };
export type DashboardResponse = {
 year: number; month: number; monthlyTotal: number; averageSpending: number; expenseCount: number;
 categoryTotals: Record<string, number>; topSpendingCategories: { category: string; total: number }[];
 recentExpenses: DashboardExpense[]; largestExpenses: DashboardExpense[];
 budget: null | { budget: number; spent: number; remaining: number; percentageSpent: number; isOverBudget: boolean };
};
export async function getDashboard(month: string, signal?: AbortSignal) {
 const [year, selectedMonth] = month.split("-").map(Number);
 const { data } = await api.get<DashboardResponse>("/api/expenses/dashboard", { params: { year, month: selectedMonth }, signal });
 return data;
}
const labels: Record<string, string> = { FOOD: "Alimentação", UTILITIES: "Serviços", ENTERTAINMENT: "Lazer", TRANSPORT: "Transporte", CLOTHING: "Vestuário", BILLS: "Contas", GIFT: "Presentes", GAS: "Combustível", HOME: "Moradia", CAR: "Carro", TRAVEL: "Viagens", MISC: "Diversos", OTHER: "Outros" };
export function categoryLabel(category: string) { return labels[category] ?? category; }
export function presentExpense(expense: DashboardExpense) { return { ...expense, category: categoryLabel(expense.category), note: "" }; }
