import { useOutletContext, Link } from "react-router-dom";
import { Target, ArrowUpRight, Pencil, Sprout } from "lucide-react";
import { PageIntro } from "@/components/shared";
import { Button } from "@/components/ui/button";
import { demoBudget, demoExpenses } from "@/data/demo";
import { money } from "@/lib/format";
import type { MonthContext } from "@/components/layout";
export default function Budget() {
  const { month } = useOutletContext<MonthContext>();
  const total = demoExpenses
    .filter((e) => e.date.startsWith(month))
    .reduce((s, e) => s + e.amount, 0);
  const percent = Math.round((total / demoBudget) * 100);
  return (
    <>
      <PageIntro
        eyebrow="UM PLANO COM ESPAÇO PARA VOCÊ"
        title="Gaste com mais consciência."
        description="Um orçamento mensal para acompanhar quanto você ainda pode gastar."
      />
      <div className="budget-layout">
        <section className="panel budget-panel">
          <div className="panel-heading">
            <span className="category-icon lavender">
              <Target />
            </span>
            <Button variant="outline" disabled>
              <Pencil size={15} /> Editar orçamento
            </Button>
          </div>
          <p className="muted">Seu orçamento mensal</p>
          <div className="budget-amount">{money(demoBudget)}</div>
          <div className="budget-progress-label">
            <span>{money(total)} gastos</span>
            <strong>{percent}% utilizado</strong>
          </div>
          <progress
            value={Math.min(percent, 100)}
            max={100}
            aria-label="Progresso do orçamento"
          />
          <div className="budget-summary">
            <div>
              <p className="small muted">Gastos até agora</p>
              <strong>{money(total)}</strong>
            </div>
            <div>
              <p className="small muted">
                {total <= demoBudget ? "Ainda disponível" : "Acima do orçamento"}
              </p>
              <strong
                className={total <= demoBudget ? "text-teal" : "text-coral"}
              >
                {money(Math.abs(demoBudget - total))}
              </strong>
            </div>
          </div>
          <p className="preview-note">
            O valor do orçamento é ilustrativo. A edição estará disponível em uma próxima etapa.
          </p>
        </section>
        <aside className="budget-aside">
          <Sprout size={36} />
          <p className="eyebrow">UM PEQUENO LEMBRETE</p>
          <h2>
            Clareza é um hábito,
            <br />
            uma prática contínua.
          </h2>
          <p className="muted">
            Acompanhe os padrões e reserve espaço para o que importa para você.
          </p>
          <Button variant="outline" asChild>
            <Link to="/expenses">
              Explore suas despesas <ArrowUpRight size={16} />
            </Link>
          </Button>
        </aside>
      </div>
    </>
  );
}
