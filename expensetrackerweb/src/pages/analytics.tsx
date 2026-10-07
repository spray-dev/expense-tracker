import { useCallback } from "react";
import { useOutletContext } from "react-router-dom";
import { PageIntro } from "@/components/shared";
import { ResourceState } from "@/components/resource-state";
import { CategoryChart, TrendChart } from "@/components/charts";
import { getAnalytics } from "@/lib/finance";
import { categoryLabel } from "@/lib/dashboard";
import { useResource } from "@/lib/use-resource";
import { money } from "@/lib/format";
import type { MonthContext } from "@/components/layout";

export default function Analytics() {
  const { month } = useOutletContext<MonthContext>();
  const load = useCallback((signal: AbortSignal) => getAnalytics(month, signal), [month]);
  const resource = useResource(month, load);
  const data = resource.data;
  const trend = data?.trend.map(point => ({ month: new Intl.DateTimeFormat("pt-BR", { month: "short", year: "2-digit" }).format(new Date(point.year, point.month - 1, 1)), spending: point.total })) ?? [];
  return <>
    <PageIntro eyebrow="PERCEBA OS PADRÕES" title="Seus gastos com perspectiva." description="Conheça melhor seus hábitos financeiros do dia a dia." />
    {resource.loading || resource.error ? <ResourceState error={resource.error} retry={resource.refresh} /> : data && <>
      <section className="panel analytics-trend"><div className="panel-heading"><div><h2>Visão de seis meses</h2><p className="small muted">Gastos mensais até o mês selecionado</p></div></div>{data.trend.some(point => point.total > 0) ? <TrendChart data={trend} /> : <p className="empty-state muted">Nenhum gasto nos últimos seis meses selecionados.</p>}</section>
      <div className="analytics-bottom"><section className="panel"><div className="panel-heading"><div><h2>Distribuição por categoria</h2><p className="small muted">Gastos do mês selecionado</p></div></div><CategoryChart totals={Object.fromEntries(Object.entries(data.categories).map(([key, value]) => [categoryLabel(key), value]))} /></section>
        <section className="panel insight-panel"><p className="eyebrow">MAIORES CATEGORIAS DO MÊS</p><h2>Onde você mais gastou.</h2>{data.top.length ? data.top.map((item, index) => <div className="category-ranking" key={item.category}><span className="rank-number">{index + 1}</span><span>{categoryLabel(item.category)}</span><strong>{money(item.total)}</strong></div>) : <p className="muted">Nenhuma despesa neste mês.</p>}</section>
      </div>
      <section className="panel yearly-summary"><div className="panel-heading"><div><h2>Resumo de {data.yearly.year}</h2><p className="small muted">Total anual: {money(data.yearly.total)}</p></div></div>{data.yearly.total === 0 && <p className="muted">Nenhum gasto registrado neste ano.</p>}<div className="yearly-grid">{data.yearly.monthlyTotals.map(point => <div className="insight-row" key={point.month}><span className="muted">{new Intl.DateTimeFormat("pt-BR", { month: "long" }).format(new Date(point.year, point.month - 1, 1))}</span><strong>{money(point.total)}</strong></div>)}</div></section>
    </>}
  </>;
}
