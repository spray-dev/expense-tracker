import { useCallback, useRef, useState, type FormEvent } from "react";
import { useOutletContext, Link } from "react-router-dom";
import { Target, ArrowUpRight, Sprout } from "lucide-react";
import { PageIntro } from "@/components/shared";
import { ResourceState } from "@/components/resource-state";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { getBudget, saveBudget, requestError } from "@/lib/finance";
import { useResource } from "@/lib/use-resource";
import { money } from "@/lib/format";
import type { MonthContext } from "@/components/layout";

export default function Budget() {
  const { month } = useOutletContext<MonthContext>();
  const load = useCallback((signal: AbortSignal) => getBudget(month, signal), [month]);
  const resource = useResource(month, load);
  const [busy, setBusy] = useState(false);
  const [feedback, setFeedback] = useState<{ month: string; message: string; error: boolean }>();
  const lock = useRef(false);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); if (lock.current) return;
    const amount = Number(new FormData(event.currentTarget).get("amount"));
    if (!Number.isFinite(amount) || amount <= 0) { setFeedback({ month, message: "Informe um valor maior que zero.", error: true }); return; }
    lock.current = true; setBusy(true); setFeedback(undefined);
    try {
      await saveBudget(month, amount);
      setFeedback({ month, message: "Orçamento salvo.", error: false }); resource.refresh();
    } catch (error) { setFeedback({ month, message: requestError(error), error: true }); }
    finally { lock.current = false; setBusy(false); }
  }
  const budget = resource.data;
  return <>
    <PageIntro eyebrow="UM PLANO COM ESPAÇO PARA VOCÊ" title="Gaste com mais consciência." description="Um orçamento mensal para acompanhar quanto você ainda pode gastar." />
    {resource.loading || resource.error ? <ResourceState error={resource.error} retry={resource.refresh} /> : <div className="budget-layout">
      <section className="panel budget-panel"><div className="panel-heading"><span className="category-icon lavender"><Target /></span><span className="tag">{month.split("-").reverse().join("/")}</span></div>
        {budget ? <><p className="muted">Seu orçamento mensal</p><div className="budget-amount">{money(budget.budget)}</div><div className="budget-progress-label"><span>{money(budget.spent)} gastos</span><strong>{new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 1 }).format(budget.percentageSpent)}% utilizado</strong></div><progress className={budget.isOverBudget ? "over-budget" : undefined} value={Math.max(0, Math.min(budget.percentageSpent, 100))} max={100} aria-label="Progresso do orçamento" /><div className="budget-summary"><div><p className="small muted">Gastos até agora</p><strong>{money(budget.spent)}</strong></div><div><p className="small muted">{budget.isOverBudget ? "Acima do orçamento" : "Ainda disponível"}</p><strong className={budget.isOverBudget ? "text-coral" : "text-teal"}>{money(Math.abs(budget.remaining))}</strong></div></div>{budget.isOverBudget && <p role="status" className="text-coral">Seus gastos ultrapassaram o orçamento deste mês.</p>}</> : <div className="empty-state"><h2>Nenhum orçamento neste mês</h2><p className="muted">Defina um limite mensal para acompanhar seus gastos.</p></div>}
        <form key={`${month}-${budget?.budget ?? "new"}`} onSubmit={submit} className="form-stack"><label>{budget ? "Novo limite mensal (R$)" : "Limite mensal (R$)"}<Input name="amount" type="number" min="0.01" step="0.01" required defaultValue={budget?.budget} disabled={busy} /></label><Button disabled={busy}>{busy ? "Salvando…" : budget ? "Atualizar orçamento" : "Criar orçamento"}</Button></form>
        {feedback?.month === month && <p className={`feedback ${feedback.error ? "" : "success"}`} role={feedback.error ? "alert" : "status"}>{feedback.message}</p>}
      </section>
      <aside className="budget-aside"><Sprout size={36} /><p className="eyebrow">UM PEQUENO LEMBRETE</p><h2>Clareza é um hábito,<br />uma prática contínua.</h2><p className="muted">Acompanhe os padrões e reserve espaço para o que importa para você.</p><Button variant="outline" asChild><Link to="/expenses">Explore suas despesas <ArrowUpRight size={16} /></Link></Button></aside>
    </div>}
  </>;
}
