import { useOutletContext } from "react-router-dom";
import { PageIntro } from "@/components/shared";
import { CategoryChart, TrendChart } from "@/components/charts";
import { demoExpenses } from "@/data/demo";
import { money } from "@/lib/format";
import type { MonthContext } from "@/components/layout";
export default function Analytics() {
  const { month } = useOutletContext<MonthContext>();
  const expenses = demoExpenses.filter((e) => e.date.startsWith(month));
  const largest = [...expenses].sort((a, b) => b.amount - a.amount)[0];
  return (
    <>
      <PageIntro
        eyebrow="PERCEBA OS PADRÕES"
        title="Seus gastos com perspectiva."
        description="Conheça melhor seus hábitos financeiros do dia a dia."
      />
      <section className="panel analytics-trend">
        <div className="panel-heading">
          <div>
            <h2>Visão de seis meses</h2>
            <p className="small muted">
              Dados ilustrativos do gráfico de tendências
            </p>
          </div>
          <span className="tag">Maio – Outubro de 2026</span>
        </div>
        <TrendChart />
      </section>
      <div className="analytics-bottom">
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Distribuição por categoria</h2>
              <p className="small muted">Com base no mês de demonstração selecionado</p>
            </div>
          </div>
          <CategoryChart expenses={expenses} />
        </section>
        <section className="panel insight-panel">
          <p className="eyebrow">RESUMO DO MÊS</p>
          <h2>
            Cada despesa
            <br />
            conta parte da história.
          </h2>
          <div className="insight-row">
            <span className="muted">Despesas registradas</span>
            <strong>{expenses.length}</strong>
          </div>
          <div className="insight-row">
            <span className="muted">Maior despesa</span>
            <strong>{largest ? money(largest.amount) : "—"}</strong>
          </div>
          <p className="small muted">
            {largest
              ? `${largest.description} é a maior compra neste mês de demonstração.`
              : "Selecione outubro ou setembro para explorar mais dados de demonstração."}
          </p>
        </section>
      </div>
    </>
  );
}
