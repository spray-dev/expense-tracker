import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { Wallet, CalendarDays, Target } from "lucide-react";
import { Button } from "@/components/ui/button";
import { PageIntro, ExpenseCard } from "@/components/shared";
import { CategoryChart } from "@/components/charts";
import { money } from "@/lib/format";
import { categoryLabel, getDashboard, presentExpense, type DashboardResponse } from "@/lib/dashboard";
import type { MonthContext } from "@/components/layout";
export default function Dashboard() {
 const { month } = useOutletContext<MonthContext>();
 const [result, setResult] = useState<{ month: string; data?: DashboardResponse; error?: string }>();
 const [attempt, setAttempt] = useState(0);
 useEffect(() => {
  const controller = new AbortController();
  getDashboard(month, controller.signal).then(data => {
   if (!controller.signal.aborted) setResult({ month, data });
  }).catch(() => {
   if (!controller.signal.aborted) setResult({ month, error: "Não foi possível carregar seu painel. Verifique sua conexão e tente novamente." });
  });
  return () => controller.abort();
 }, [month, attempt]);
 const data = result?.month === month ? result.data : undefined;
 const error = result?.month === month ? result.error : undefined;
 const budget = data?.budget;
 return <>
  <PageIntro eyebrow="VISÃO GERAL" title="Seu mês com mais clareza." description="Acompanhar faz a diferença. Veja como está seu mês." />
  {error ? <div className="empty-state" role="alert"><h2>Seu painel está indisponível</h2><p className="muted">{error}</p><Button onClick={() => { setResult(undefined); setAttempt(v => v + 1); }}>Tentar novamente</Button></div>
  : !data ? <div className="empty-state" role="status" aria-live="polite" aria-busy="true"><Wallet size={32} /><h2>Carregando seu mês…</h2><p className="muted">Buscando suas despesas e seu orçamento.</p></div>
  : <>
   <div className="stats-grid">
    <article className="stat-card featured"><div className="stat-label">Gastos do mês <Wallet size={20} /></div><strong className="stat-number">{money(data.monthlyTotal)}</strong><div className="stat-foot"><span className="stat-pill">{data.expenseCount} despesas</span><span>no mês selecionado</span></div><div className="stat-decoration" /></article>
    <article className="stat-card"><div className="stat-label">Média por despesa <CalendarDays size={20} /></div><strong className="stat-number">{money(data.averageSpending)}</strong><div className="stat-foot muted">Mais perspectiva sobre cada compra</div></article>
    <article className="stat-card"><div className="stat-label">Orçamento mensal <Target size={20} /></div><strong className={`stat-number ${budget?.isOverBudget ? "text-coral" : ""}`}>{budget && budget.budget > 0 ? <>{new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 1 }).format(budget.percentageSpent)}<span className="number-suffix">% utilizado</span></> : budget ? "Sem limite positivo" : "Não definido"}</strong>
     {budget ? <><progress className={budget.isOverBudget ? "over-budget" : undefined} value={Math.max(0, Math.min(budget.percentageSpent, 100))} max={100} aria-label="Orçamento mensal utilizado" /><div className="stat-foot muted">{money(Math.abs(budget.remaining))} {budget.isOverBudget ? "acima de" : "disponíveis de"} {money(budget.budget)}</div></> : <p className="small muted">Nenhum orçamento cadastrado para este mês.</p>}
    </article>
   </div>
   {data.expenseCount === 0 && <div className="empty-state dashboard-empty" role="status"><Wallet size={28} /><h2>Nenhuma despesa neste mês</h2><p className="muted">Selecione outro mês para consultar seus gastos.</p></div>}
   <div className="dashboard-charts">
    <section className="panel"><div className="panel-heading"><div><h2>Maiores categorias</h2><p className="small muted">Onde você mais gastou no mês selecionado</p></div></div><div className="top-categories">{data.topSpendingCategories.length ? data.topSpendingCategories.map((item, index) => <div className="category-ranking" key={item.category}><span className="rank-number">{index + 1}</span><span>{categoryLabel(item.category)}</span><strong>{money(item.total)}</strong></div>) : <p className="muted">Nenhuma categoria com gastos neste mês.</p>}</div></section>
    <section className="panel"><div className="panel-heading"><div><h2>Destino dos gastos</h2><p className="small muted">Mês selecionado por categoria</p></div></div><CategoryChart totals={Object.fromEntries(Object.entries(data.categoryTotals).map(([key, value]) => [categoryLabel(key), value]))} /></section>
   </div>
   {([['Despesas recentes', data.recentExpenses], ['Maiores despesas', data.largestExpenses]] as const).map(([title, expenses]) => <section className="dashboard-list" key={title}><div className="section-heading"><div><h2>{title}</h2><p className="small muted">Somente o mês selecionado</p></div></div>{expenses.length ? <div className="expense-grid dashboard-expenses">{expenses.map(expense => <ExpenseCard key={expense.id} expense={presentExpense(expense)} />)}</div> : <div className="empty-state"><h3>Nenhuma despesa neste mês</h3><p className="muted">As despesas do mês selecionado aparecerão aqui.</p></div>}</section>)}
  </>}
 </>;
}
