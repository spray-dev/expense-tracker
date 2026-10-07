import { useState } from "react";
import { useOutletContext, useSearchParams } from "react-router-dom";
import {
  Plus,
  Search,
  SlidersHorizontal,
  ChevronLeft,
  ChevronRight,
  Download,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { PageIntro, ExpenseCard, EmptyState } from "@/components/shared";
import { categories, demoExpenses, type DemoExpense } from "@/data/demo";
import { money } from "@/lib/format";
import type { MonthContext } from "@/components/layout";
export default function Expenses() {
  const { month } = useOutletContext<MonthContext>();
  const [searchParams, setSearchParams] = useSearchParams();
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("Todas");
  const [page, setPage] = useState(1);
  const [open, setOpen] = useState(searchParams.get("new") === "1");
  const [selected, setSelected] = useState<DemoExpense | null>(null);
  const filtered = demoExpenses.filter(
    (e) =>
      e.date.startsWith(month) &&
      (category === "Todas" || e.category === category) &&
      e.description.toLowerCase().includes(search.toLowerCase()),
  );
  const pages = Math.max(1, Math.ceil(filtered.length / 6));
  const currentPage = Math.min(page, pages);
  function show(expense: DemoExpense | null) {
    setSelected(expense);
    setOpen(true);
  }
  return (
    <>
      <PageIntro
        eyebrow="SEUS GASTOS DO DIA A DIA"
        title="Pequenos gastos. Visão completa."
        description="Encontre e acompanhe as despesas do seu mês."
        action={
          <Button onClick={() => show(null)}>
            <Plus size={17} /> Adicionar despesa
          </Button>
        }
      />
      <div className="filters-panel">
        <div className="search-row">
          <div className="search-input">
            <Search size={18} />
            <Input
              aria-label="Buscar despesas"
              placeholder="Buscar despesas…"
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(1);
              }}
            />
          </div>
          <Button
            variant="outline"
            disabled
            title="A exportação CSV estará disponível em uma próxima etapa"
          >
            <Download size={16} /> Exportar CSV
          </Button>
        </div>
        <div className="filter-row">
          <span className="small filter-label">
            <SlidersHorizontal size={16} /> Categoria
          </span>
          <div className="filter-chips">
            {["Todas", ...categories].map((c) => (
              <button
                key={c}
                className={`filter-chip ${c === category ? "selected" : ""}`}
                aria-pressed={c === category}
                onClick={() => {
                  setCategory(c);
                  setPage(1);
                }}
              >
                {c}
              </button>
            ))}
          </div>
        </div>
      </div>
      <div className="results-heading">
        <p className="small muted">
          {filtered.length} despesas de demonstração{" "}
          <span className="dot-separator">·</span>{" "}
          {money(filtered.reduce((s, e) => s + e.amount, 0))} total
        </p>
        {(search || category !== "Todas") && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setSearch("");
              setCategory("Todas");
              setPage(1);
            }}
          >
            Limpar filtros
          </Button>
        )}
      </div>
      {filtered.length ? (
        <div className="expense-grid">
          {filtered.slice((currentPage - 1) * 6, currentPage * 6).map((e) => (
            <ExpenseCard key={e.id} expense={e} onEdit={() => show(e)} />
          ))}
        </div>
      ) : (
        <EmptyState />
      )}
      <nav className="pagination" aria-label="Páginas de despesas">
        <Button
          variant="outline"
          size="icon"
          aria-label="Página anterior"
          disabled={currentPage === 1}
          onClick={() => setPage((p) => p - 1)}
        >
          <ChevronLeft />
        </Button>
        {Array.from({ length: pages }, (_, i) => (
          <Button
            key={i}
            variant={currentPage === i + 1 ? "default" : "ghost"}
            size="icon"
            aria-label={`Página ${i + 1}`}
            aria-current={currentPage === i + 1 ? "page" : undefined}
            onClick={() => setPage(i + 1)}
          >
            {i + 1}
          </Button>
        ))}
        <Button
          variant="outline"
          size="icon"
          aria-label="Próxima página"
          disabled={currentPage === pages}
          onClick={() => setPage((p) => p + 1)}
        >
          <ChevronRight />
        </Button>
      </nav>
      <Dialog
        open={open}
        onOpenChange={(value) => {
          setOpen(value);
          if (!value) setSearchParams({}, { replace: true });
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {selected ? "Detalhes da despesa" : "Adicionar uma despesa"}
            </DialogTitle>
            <DialogDescription>
              Prévia visual. Estes campos não salvam dados.
            </DialogDescription>
          </DialogHeader>
          <form
            key={selected?.id ?? "new"}
            onSubmit={(e) => e.preventDefault()}
            className="form-stack"
          >
            <label>
              Descrição
              <Input
                defaultValue={selected?.description}
                placeholder="Qual foi o motivo?"
              />
            </label>
            <div className="form-columns">
              <label>
                Valor (R$)
                <Input
                  type="number"
                  min="0"
                  step="0.01"
                  defaultValue={selected?.amount}
                  placeholder="0,00"
                />
              </label>
              <label>
                Data
                <Input
                  type="date"
                  defaultValue={selected?.date ?? `${month}-07`}
                />
              </label>
            </div>
            <label>
              Categoria
              <select
                className="form-select"
                defaultValue={selected?.category ?? "Alimentação"}
              >
                {categories.map((c) => (
                  <option key={c}>{c}</option>
                ))}
              </select>
            </label>
            <Button disabled>Salvar despesa · Em breve</Button>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
}
