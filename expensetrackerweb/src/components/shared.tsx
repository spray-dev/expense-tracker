import {
  ArrowUpRight,
  Coffee,
  House,
  ShoppingBag,
  Clapperboard,
  Car,
  Moon,
  Sun,
  WalletCards,
} from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { useTheme } from "./theme-context";
import { categoryColors } from "@/data/categories";
import { dateLabel, money } from "@/lib/format";
const categoryIcons = {
  Alimentação: Coffee,
  Compras: ShoppingBag,
  Transporte: Car,
  Moradia: House,
  Lazer: Clapperboard,
};
export function Brand({ compact = false }: { compact?: boolean }) {
  return (
    <Link to="/dashboard" className="brand" aria-label="Início do Controle de Despesas">
      <span className="brand-icon">
        <WalletCards size={22} />
      </span>
      {!compact && (
        <span>
          controle<span className="brand-light"> de despesas</span>
          <small>Um pouco de clareza todos os dias.</small>
        </span>
      )}
    </Link>
  );
}
export function ThemeToggle() {
  const { theme, toggle } = useTheme();
  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={toggle}
      aria-label={`Ativar tema ${theme === "dark" ? "claro" : "escuro"}`}
    >
      {theme === "dark" ? <Sun /> : <Moon />}
    </Button>
  );
}
export function PageIntro({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow: string;
  title: string;
  description: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="page-intro">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p className="muted">{description}</p>
      </div>
      {action}
    </div>
  );
}
export function ExpenseCard({
  expense,
  onEdit,
}: {
  expense: { id: number; description: string; amount: number; date: string; category: string; note?: string };
  onEdit?: () => void;
}) {
  const Icon = categoryIcons[expense.category as keyof typeof categoryIcons] ?? WalletCards;
  return (
    <article className="expense-card">
      <div className="expense-card-top">
        <span
          className="category-icon"
          style={{
            color: (categoryColors[expense.category as keyof typeof categoryColors] ?? "#8a7550"),
            background: `${(categoryColors[expense.category as keyof typeof categoryColors] ?? "#8a7550")}18`,
          }}
        >
          <Icon size={22} />
        </span>
        <span className="small muted">{dateLabel(expense.date)}</span>
      </div>
      <h3>{expense.description}</h3>
      <p className="small muted">{expense.note}</p>
      <div className="expense-card-bottom">
        <strong>{money(expense.amount)}</strong>
        <span className="tag" style={{ background: `${(categoryColors[expense.category as keyof typeof categoryColors] ?? "#8a7550")}20`, borderLeft: `3px solid ${(categoryColors[expense.category as keyof typeof categoryColors] ?? "#8a7550")}` }}>{expense.category}</span>
      </div>
      {onEdit && (
        <Button
          variant="ghost"
          className="edit-expense"
          onClick={onEdit}
          aria-label={`Ver detalhes de ${expense.description}`}
        >
          Ver detalhes <ArrowUpRight size={14} />
        </Button>
      )}
    </article>
  );
}
export function EmptyState() {
  return (
    <div className="empty-state">
      <WalletCards size={32} />
      <h3>Nenhuma despesa encontrada</h3>
      <p className="muted">Tente outro mês ou limpe os filtros.</p>
    </div>
  );
}
