import { useOutletContext, Link } from "react-router-dom";
import { ArrowUpRight, Wallet, CalendarDays, Target, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { PageIntro, ExpenseCard, EmptyState } from "@/components/shared";
import { CategoryChart, TrendChart } from "@/components/charts";
import { demoExpenses, demoBudget } from "@/data/demo";
import { money } from "@/lib/format";
import type { MonthContext } from "@/components/layout";
export default function Dashboard() {
  const { month } = useOutletContext<MonthContext>();
  const expenses = demoExpenses.filter((e) => e.date.startsWith(month));
  const total = expenses.reduce((sum, e) => sum + e.amount, 0);
  const percent = Math.round((total / demoBudget) * 100);
  return (
    <>
      <PageIntro
        eyebrow="VISÃO GERAL"
        title="Seu mês com mais clareza."
        description="Acompanhar faz a diferença. Veja como está seu mês."
        action={
          <Button asChild>
            <Link to="/expenses?new=1">
              <Plus size={17} /> Adicionar despesa
            </Link>
          </Button>
        }
      />
      <div className="stats-grid">
        <article className="stat-card featured">
          <div className="stat-label">
            Gastos do mês <Wallet size={20} />
          </div>
          <strong className="stat-number">{money(total)}</strong>
          <div className="stat-foot">
            <span className="stat-pill">{expenses.length} despesas</span>
            <span>no mês selecionado</span>
          </div>
          <div className="stat-decoration" />
        </article>
        <article className="stat-card">
          <div className="stat-label">
            Média por despesa <CalendarDays size={20} />
          </div>
          <strong className="stat-number">
            {money(expenses.length ? total / expenses.length : 0)}
          </strong>
          <div className="stat-foot muted">
            Mais perspectiva sobre cada compra
          </div>
        </article>
        <article className="stat-card">
          <div className="stat-label">
            Orçamento mensal <Target size={20} />
          </div>
          <strong className="stat-number">
            {percent}
            <span className="number-suffix">% utilizado</span>
          </strong>
          <progress
            value={Math.min(percent, 100)}
            max={100}
            aria-label="Orçamento mensal utilizado"
          />
          <div className="stat-foot muted">
            {money(Math.abs(demoBudget - total))}{" "}
            {total <= demoBudget ? "disponíveis de" : "acima de"} {money(demoBudget)}
          </div>
        </article>
      </div>
      <div className="dashboard-charts">
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Ritmo dos gastos</h2>
              <p className="small muted">Tendência ilustrativa de seis meses</p>
            </div>
            <span className="tag">Mai – Out</span>
          </div>
          <TrendChart />
        </section>
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Destino dos gastos</h2>
              <p className="small muted">Mês selecionado por categoria</p>
            </div>
          </div>
          <CategoryChart expenses={expenses} />
        </section>
      </div>
      <section>
        <div className="section-heading">
          <div>
            <h2>Despesas recentes</h2>
            <p className="small muted">
              Seu dia a dia em um só lugar.
            </p>
          </div>
          <Button variant="ghost" asChild>
            <Link to="/expenses">
              Ver todas <ArrowUpRight size={16} />
            </Link>
          </Button>
        </div>
        {expenses.length ? (
          <div className="expense-grid dashboard-expenses">
            {[...expenses]
              .sort((a, b) => b.date.localeCompare(a.date))
              .slice(0, 4)
              .map((e) => (
                <ExpenseCard key={e.id} expense={e} />
              ))}
          </div>
        ) : (
          <EmptyState />
        )}
      </section>
    </>
  );
}

