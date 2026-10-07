import { useEffect, useState } from "react";
import { NavLink, Outlet, useLocation, Link } from "react-router-dom";
import {
  LayoutDashboard,
  WalletCards,
  ChartNoAxesCombined,
  CircleUserRound,
  PanelLeftClose,
  PanelLeftOpen,
  Menu,
  ChevronDown,
  Target,
  ArrowUpRight,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetTitle,
  SheetDescription,
} from "@/components/ui/sheet";
import { Brand, ThemeToggle } from "./shared";
import { useAuth } from "@/auth/context";
const nav = [
  { path: "/dashboard", title: "Painel", icon: LayoutDashboard },
  { path: "/expenses", title: "Despesas", icon: WalletCards },
  { path: "/budget", title: "Orçamento", icon: Target },
  { path: "/analytics", title: "Análises", icon: ChartNoAxesCombined },
  { path: "/profile", title: "Perfil", icon: CircleUserRound },
];
export type MonthContext = { month: string };
function Navigation({
  collapsed = false,
  onNavigate,
}: {
  collapsed?: boolean;
  onNavigate?: () => void;
}) {
  return (
    <nav aria-label="Navegação principal">
      {nav.map((item) => (
        <NavLink
          key={item.path}
          to={item.path}
          onClick={onNavigate}
          title={collapsed ? item.title : undefined}
          className={({ isActive }) => `nav-item ${isActive ? "active" : ""}`}
        >
          <item.icon size={20} />
          {!collapsed && <span>{item.title}</span>}
        </NavLink>
      ))}
    </nav>
  );
}
export function AppLayout() {
  const { user, logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [month, setMonth] = useState(() => { const now = new Date(); return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`; });
  const [calendarBase] = useState(() => new Date());
  const location = useLocation();
  const title =
    nav.find((n) => n.path === location.pathname)?.title ?? "Expense Tracker";
  useEffect(() => {
    document.title = `${title} · Expense Tracker`;
  }, [title]);
  return (
    <div className={`app-layout ${collapsed ? "sidebar-collapsed" : ""}`}>
      <a href="#main-content" className="skip-link">
        Pular para o conteúdo
      </a>
      <aside className="sidebar">
        <Brand compact={collapsed} />
        <div className="sidebar-nav">
          {!collapsed && <p className="nav-caption">SEU ESPAÇO</p>}
          <Navigation collapsed={collapsed} />
        </div>
        {!collapsed && (
          <div className="sidebar-note">
            <span className="sparkle">✦</span>
            <h3>Reserve espaço para o que importa.</h3>
            <p>Pequenos hábitos. Mais clareza.</p>
            <Link to="/budget">
              Explore seu orçamento <ArrowUpRight size={15} />
            </Link>
          </div>
        )}
        <div className="sidebar-footer">
          <Button
            variant="ghost"
            size={collapsed ? "icon" : "default"}
            onClick={() => setCollapsed(!collapsed)}
            aria-label={collapsed ? "Expandir menu lateral" : "Recolher menu lateral"}
          >
            {collapsed ? (
              <PanelLeftOpen />
            ) : (
              <>
                <PanelLeftClose /> Recolher menu lateral
              </>
            )}
          </Button>
        </div>
      </aside>
      <Sheet open={mobileOpen} onOpenChange={setMobileOpen}>
        <SheetContent side="left" className="mobile-sheet">
          <SheetTitle className="sr-only">Navegação</SheetTitle>
          <SheetDescription className="sr-only">
            Escolha uma página.
          </SheetDescription>
          <Brand />
          <Navigation onNavigate={() => setMobileOpen(false)} />
        </SheetContent>
      </Sheet>
      <div className="workspace">
        <header className="topbar">
          <div className="topbar-title">
            <Button
              variant="ghost"
              size="icon"
              className="mobile-menu"
              aria-label="Abrir navegação"
              onClick={() => setMobileOpen(true)}
            >
              <Menu />
            </Button>
            <span>{title}</span>
            <span className="topbar-divider" />
            <span className="small muted desktop-context">
              Seu dinheiro com perspectiva
            </span>
          </div>
          <div className="topbar-actions">
            <label className="month-select">
              <span className="sr-only">Mês selecionado</span>
              <select aria-label="Mês selecionado" value={month} onChange={(e) => setMonth(e.target.value)}>
                {Array.from({ length: 24 }, (_, offset) => {
                  const now = calendarBase;
                  const date = new Date(now.getFullYear(), now.getMonth() - offset, 1);
                  const value = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
                  return <option key={value} value={value}>{new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric" }).format(date)}</option>;
                })}
              </select>
              <ChevronDown size={14} />
            </label>
            <Button variant="ghost" onClick={logout}>Sair</Button>
            <ThemeToggle />
            <Link to="/profile" className="avatar" aria-label="Abrir perfil">
              {user?.username.slice(0, 2).toLocaleUpperCase("pt-BR")}
            </Link>
          </div>
        </header>
        <main id="main-content" tabIndex={-1} className="main-content">
          <Outlet key={month} context={{ month } satisfies MonthContext} />
          <footer className="page-footer">
            <span>Expense Tracker</span>
            <span>
              <i /> Dados da sua conta
            </span>
          </footer>
        </main>
      </div>
    </div>
  );
}
