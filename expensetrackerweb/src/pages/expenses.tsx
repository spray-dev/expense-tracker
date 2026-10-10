import { useCallback, useRef, useState, type FormEvent } from "react";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { Plus, Search, Download, ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { PageIntro, ExpenseCard, EmptyState } from "@/components/shared";
import { ResourceState } from "@/components/resource-state";
import { categoryLabels } from "@/data/categories";
import { presentExpense } from "@/lib/dashboard";
import { listExpensePage, saveExpense, deleteExpense, exportExpenses, requestError, type Expense, type Filters } from "@/lib/finance";
import { expenseDateFields, parseExpenseDate } from "@/lib/expense-date";
import { useResource } from "@/lib/use-resource";
import type { MonthContext } from "@/components/layout";

export default function Expenses() {
  const { month } = useOutletContext<MonthContext>();
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState<Omit<Filters, "month">>({ description: "", category: "", period: "SELECTED_MONTH", start: "", end: "", sort: "date:desc", page: 0 });
  const [open, setOpen] = useState(searchParams.get("new") === "1");
  const [selected, setSelected] = useState<Expense>();
  const [busy, setBusy] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [formError, setFormError] = useState("");
  const [notice, setNotice] = useState("");
  const [actionError, setActionError] = useState("");
  const [confirmDelete, setConfirmDelete] = useState(false);
  const lock = useRef(false);
  const queryKey = JSON.stringify({ ...filters, month });
  const load = useCallback((signal: AbortSignal) => listExpensePage(JSON.parse(queryKey) as Filters, signal), [queryKey]);
  const resource = useResource(queryKey, load);
  function change(patch: Partial<Filters>) { setFilters(current => ({ ...current, ...patch, page: 0 })); }
  function show(expense?: Expense) { setSelected(expense); setFormError(""); setConfirmDelete(false); setOpen(true); }
  function close() { setOpen(false); setSearchParams({}, { replace: true }); }
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); if (lock.current) return;
    const fields = new FormData(event.currentTarget);
    let date: string;
    try { date = parseExpenseDate(String(fields.get("date") ?? ""), String(fields.get("time") ?? "")); }
    catch (error) { setFormError((error as Error).message); return; }
    const description = String(fields.get("description")).trim();
    if (!description) { setFormError("Informe uma descrição."); return; }
    lock.current = true; setBusy(true); setFormError("");
    try {
      await saveExpense({ description, amount: Number(fields.get("amount")), date, category: String(fields.get("category")) }, selected?.id);
      close(); setNotice(selected ? "Despesa atualizada." : "Despesa adicionada. Se não aparecer, confira o mês e os filtros selecionados."); resource.refresh();
    } catch (error) { setFormError(requestError(error)); }
    finally { lock.current = false; setBusy(false); }
  }
  async function remove() {
    if (!selected || lock.current) return;
    lock.current = true; setBusy(true); setFormError("");
    try { await deleteExpense(selected.id); close(); setNotice("Despesa excluída."); resource.refresh(); }
    catch (error) { setFormError(requestError(error)); }
    finally { lock.current = false; setBusy(false); }
  }
  async function download() {
    setExporting(true); setActionError("");
    try {
      const blob = await exportExpenses(); const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a"); anchor.href = url; anchor.download = "despesas.csv"; anchor.click();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000); setNotice("CSV de todas as suas despesas exportado.");
    } catch (error) { setActionError(requestError(error)); }
    finally { setExporting(false); }
  }
  const dateFields = expenseDateFields(selected?.date ?? `${month}-01T12:00`);
  return <>
    <PageIntro eyebrow="SEUS GASTOS DO DIA A DIA" title="Pequenos gastos. Visão completa." description="Encontre e acompanhe as despesas do seu mês." action={<Button onClick={() => show()}><Plus size={17} /> Adicionar despesa</Button>} />
    {notice && <p className="feedback success" role="status">{notice}</p>}
    {actionError && <p className="feedback" role="alert">{actionError}</p>}
    <div className="filters-panel">
      <div className="search-row"><div className="search-input"><Search size={18} /><Input aria-label="Buscar despesas" placeholder="Buscar despesas…" value={filters.description} onChange={event => change({ description: event.target.value })} /></div><Button variant="outline" disabled={exporting} onClick={download}><Download size={16} /> {exporting ? "Exportando…" : "Exportar CSV (todas)"}</Button></div>
      <div className="filter-row"><span className="small filter-label">Categoria</span><div className="filter-chips">{[["", "Todas"], ...Object.entries(categoryLabels)].map(([value, label]) => <button key={value} className={`filter-chip ${filters.category === value ? "selected" : ""}`} aria-pressed={filters.category === value} onClick={() => change({ category: value })}>{label}</button>)}</div></div>
      <div className="filter-controls">
        <label>Período<select className="form-select" value={filters.period} onChange={event => change({ period: event.target.value })}><option value="SELECTED_MONTH">Mês selecionado</option><option value="ALL">Todas as datas</option><option value="CUSTOM">Intervalo personalizado</option><option value="THIS_MONTH">Mês atual</option><option value="LAST_MONTH">Mês anterior</option><option value="LAST_7_DAYS">Últimos 7 dias</option><option value="LAST_30_DAYS">Últimos 30 dias</option><option value="THIS_YEAR">Ano atual</option></select></label>
        {filters.period === "CUSTOM" && <><label>De<Input type="date" value={filters.start} onChange={event => change({ start: event.target.value })} /></label><label>Até<Input type="date" min={filters.start} value={filters.end} onChange={event => change({ end: event.target.value })} /></label></>}
        <label>Ordenar<select className="form-select" value={filters.sort} onChange={event => change({ sort: event.target.value })}><option value="date:desc">Mais recentes</option><option value="date:asc">Mais antigas</option><option value="amount:desc">Maior valor</option><option value="amount:asc">Menor valor</option><option value="description:asc">Descrição A–Z</option><option value="description:desc">Descrição Z–A</option></select></label>
        <Button variant="ghost" onClick={() => change({ description: "", category: "", period: "SELECTED_MONTH", start: "", end: "", sort: "date:desc" })}>Limpar filtros</Button>
      </div>
    </div>
    {resource.loading || resource.error ? <ResourceState error={resource.error} retry={resource.refresh} /> : <>
      <div className="results-heading"><p className="small muted">{resource.data?.totalElements} despesas encontradas</p></div>
      {resource.data?.content.length ? <div className="expense-grid">{resource.data.content.map(expense => <ExpenseCard key={expense.id} expense={presentExpense(expense)} onEdit={() => show(expense)} />)}</div> : <EmptyState />}
      {!!resource.data?.totalPages && <nav className="pagination" aria-label="Páginas de despesas"><Button variant="outline" size="icon" aria-label="Página anterior" disabled={resource.data.page === 0} onClick={() => setFilters(current => ({ ...current, page: resource.data!.page - 1 }))}><ChevronLeft /></Button><span>Página {resource.data.page + 1} de {resource.data.totalPages}</span><Button variant="outline" size="icon" aria-label="Próxima página" disabled={resource.data.page + 1 >= resource.data.totalPages} onClick={() => setFilters(current => ({ ...current, page: resource.data!.page + 1 }))}><ChevronRight /></Button></nav>}
    </>}
    <Dialog open={open || searchParams.get("new") === "1"} onOpenChange={value => { if (!busy) { if (!value) close(); else setOpen(value); } }}><DialogContent className="expense-dialog" showCloseButton={!busy} onEscapeKeyDown={event => { if (busy) event.preventDefault(); }} onPointerDownOutside={event => { if (busy) event.preventDefault(); }}><DialogHeader><DialogTitle>{selected ? "Editar despesa" : "Adicionar uma despesa"}</DialogTitle><DialogDescription>Preencha os dados para salvar na sua conta.</DialogDescription></DialogHeader>
      <form key={selected?.id ?? "new"} className="form-stack" onSubmit={submit}><fieldset disabled={busy} className="form-stack">
        <label>Descrição<Input name="description" required maxLength={50} defaultValue={selected?.description} placeholder="Qual foi o motivo?" /></label>
        <label>Valor (R$)<Input name="amount" type="number" required min="0" step="0.01" defaultValue={selected?.amount} placeholder="0,00" /></label>
        <div className="form-columns"><label>Data (DD/MM/AAAA)<Input name="date" type="text" inputMode="numeric" placeholder="DD/MM/AAAA" defaultValue={dateFields.date} /></label><label>Hora (24h)<Input name="time" type="time" step="60" placeholder="HH:mm — ex.: 19:30" defaultValue={dateFields.time} /></label></div>
        <label>Categoria<select name="category" className="form-select" defaultValue={selected?.category ?? "FOOD"}>{selected && !categoryLabels[selected.category] && <option value={selected.category}>{selected.category}</option>}{Object.entries(categoryLabels).map(([value, label]) => <option value={value} key={value}>{label}</option>)}</select></label>
        {formError && <p role="alert" className="feedback">{formError}</p>}
        <Button type="submit">{busy ? "Salvando…" : "Salvar despesa"}</Button>
        {selected && (confirmDelete ? <div className="confirmation"><p>Excluir “{selected.description}”? Esta ação não pode ser desfeita.</p><Button type="button" variant="destructive" onClick={remove}>Confirmar exclusão</Button><Button type="button" variant="ghost" onClick={() => setConfirmDelete(false)}>Cancelar</Button></div> : <Button type="button" variant="outline" onClick={() => setConfirmDelete(true)}>Excluir despesa</Button>)}
      </fieldset></form>
    </DialogContent></Dialog>
  </>;
}
