import { useAuth } from "@/auth/context";
import { authError } from "@/lib/api";
import { useEffect, useState } from "react";
import { Link, Navigate, useLocation } from "react-router-dom";
import { Eye, EyeOff, Check, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Brand, ThemeToggle } from "@/components/shared";
export default function Auth({ register = false }: { register?: boolean }) {
  const [visible, setVisible] = useState(false);
  const { user, loading, login, register: createAccount } = useAuth();
  const location = useLocation();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => { document.title = `${register ? "Criar conta" : "Entrar"} · Controle de Despesas`; }, [register]);
  const from = location.state?.from;
  const destination = typeof from === "string" && from.startsWith("/") && !from.startsWith("//") && !from.startsWith("/login") && !from.startsWith("/register") ? from : "/dashboard";
  if (loading) return <div className="empty-state" role="status">Verificando sua sessão…</div>;
  if (user) return <Navigate to={destination} replace />;
  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (pending) return;
    const fields = new FormData(event.currentTarget);
    setPending(true); setError("");
    try {
      const email = String(fields.get("email")).trim();
      const password = String(fields.get("password"));
      if (register) await createAccount(String(fields.get("username")).trim(), email, password);
      else await login(email, password);
    } catch (err) { setError(err instanceof Error && err.message.startsWith("Cadastro concluído") ? err.message : authError(err)); }
    finally { setPending(false); }
  }
  return (
    <div className="auth-layout">
      <aside className="auth-story">
        <Brand />
        <div className="auth-story-content">
          <p className="eyebrow">MAIS CLAREZA. MENOS DÚVIDAS.</p>
          <h2>
            Pequenos gastos.
            <br />
            Mais perspectiva.
          </h2>
          <p>
            Um espaço para seus gastos do dia a dia.
            <br />
            Reserve espaço para o que importa.
          </p>
          <div className="auth-art" aria-hidden="true">
            <div className="art-orbit" />
            <div className="art-card">
              <span>SEU MÊS EM UM OLHAR</span>
              <strong>
                Um pouco mais
                <br />
                de controle.
              </strong>
              <div className="art-bars">
                {[45, 75, 55, 92, 67, 82, 60].map((h, i) => (
                  <i key={i} style={{ height: `${h}%` }} />
                ))}
              </div>
              <div className="art-card-footer">
                <span className="art-dot" /> Pequenos hábitos, clareza duradoura
              </div>
            </div>
            <div className="art-sticker">
              <Sparkles size={20} />
              <span>Você está avançando.</span>
            </div>
          </div>
          <div className="auth-benefits">
            <span>
              <Check size={15} /> Uma visão mais clara
            </span>
            <span>
              <Check size={15} /> Seu ritmo, seu plano
            </span>
          </div>
        </div>
        <p className="small">Controle de Despesas · Um pouco de clareza todos os dias.</p>
      </aside>
      <main className="auth-main">
        <div className="auth-top">
          <span className="tag">Acesso à sua conta</span>
          <ThemeToggle />
        </div>
        <div className="auth-form-wrap">
          <div className="auth-mobile-brand">
            <Brand />
          </div>
          <p className="eyebrow">
            {register ? "SEU PRÓXIMO CAPÍTULO" : "BOM TER VOCÊ AQUI"}
          </p>
          <h1>{register ? "Um novo começo." : "Bem-vindo de volta."}</h1>
          <p className="muted">
            {register
              ? "Crie um espaço para seus hábitos financeiros."
              : "Vamos acompanhar seu mês."}
          </p>
          <form className="form-stack" onSubmit={submit}>
            {register && (
              <label>
                Nome de usuário
                <Input name="username" required autoComplete="username" placeholder="Seu nome de usuário" />
              </label>
            )}
            <label>
              E-mail
              <Input
                name="email" required
                autoComplete={register ? "email" : "username"}
                type="email"
                placeholder="voce@exemplo.com"
              />
            </label>
            <label>
              Senha
              <div className="password-input">
                <Input
                  name="password" required minLength={register ? 8 : undefined} maxLength={register ? 100 : undefined}
                  autoComplete={register ? "new-password" : "current-password"}
                  type={visible ? "text" : "password"}
                  placeholder="Digite sua senha"
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={() => setVisible(!visible)}
                  aria-label={visible ? "Ocultar senha" : "Mostrar senha"}
                >
                  {visible ? <EyeOff size={17} /> : <Eye size={17} />}
                </Button>
              </div>
            </label>
            {error && <p role="alert">{error}</p>}
            <Button type="submit" disabled={pending}>
              {pending ? "Aguarde…" : register ? "Criar conta" : "Entrar"}
            </Button>
          </form>
          <p className="auth-switch">
            {register ? "Já tem uma conta?" : "Ainda não tem uma conta?"}{" "}
            <Link to={register ? "/login" : "/register"}>
              {register ? "Entrar" : "Criar uma conta"}
            </Link>
          </p>

        </div>
        <p className="auth-bottom small muted">Uma forma tranquila de acompanhar seus gastos.</p>
      </main>
    </div>
  );
}
