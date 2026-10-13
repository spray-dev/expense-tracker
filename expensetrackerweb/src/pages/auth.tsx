import { useAuth } from "@/auth/context";
import { authError, forgotPassword, resetPassword, passwordResetError, passwordRules, validateNewPassword } from "@/lib/api";
import { useEffect, useState } from "react";
import { Link, Navigate, useLocation, useSearchParams } from "react-router-dom";
import { Eye, EyeOff, Check, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Brand, ThemeToggle } from "@/components/shared";
export default function Auth({ register = false, recovery }: { register?: boolean; recovery?: "forgot" | "reset" }) {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const resetting = recovery === "reset";
  const [success, setSuccess] = useState("");
  const title = recovery ? resetting ? "Redefinir senha" : "Recuperar acesso" : register ? "Criar conta" : "Entrar";
  const [visible, setVisible] = useState(false);
  const { user, loading, login, register: createAccount } = useAuth();
  const location = useLocation();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  useEffect(() => { document.title = `${title} · Expense Tracker`; }, [title]);
  const from = location.state?.from;
  const destination = typeof from === "string" && from.startsWith("/") && !from.startsWith("//") && !from.startsWith("/login") && !from.startsWith("/register") ? from : "/dashboard";
  if (!recovery && loading) return <div className="empty-state" role="status">Verificando sua sessão…</div>;
  if (!recovery && user) return <Navigate to={destination} replace />;
  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (pending) return;
    const fields = new FormData(event.currentTarget);
    setPending(true); setError("");
    try {
      const email = String(fields.get("email")).trim();
      const password = String(fields.get("password"));
      if (recovery === "forgot") setSuccess(await forgotPassword(email));
      else if (resetting) {
        const validation = validateNewPassword(password);
        if (validation) { setError(validation); return; }
        await resetPassword(token, password);
        setSuccess("Senha redefinida com sucesso. Entre com sua nova senha.");
      }
      else if (register) await createAccount(String(fields.get("username")).trim(), email, password);
      else await login(email, password);
    } catch (err) { setError(err instanceof Error && err.message.startsWith("Cadastro concluído") ? err.message : recovery ? passwordResetError(err, resetting) : authError(err)); }
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
        <p className="small">Expense Tracker</p>
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
            {recovery ? "ACESSO COM TRANQUILIDADE" : register ? "SEU PRÓXIMO CAPÍTULO" : "BOM TER VOCÊ AQUI"}
          </p>
          <h1>{recovery ? resetting ? "Uma nova senha." : "Esqueceu sua senha?" : register ? "Um novo começo." : "Bem-vindo de volta."}</h1>
          <p className="muted">
            {recovery ? resetting ? "Escolha uma senha de 8 a 100 caracteres." : "Informe seu e-mail para receber um link de redefinição." : register
              ? "Crie um espaço para seus hábitos financeiros."
              : "Vamos acompanhar seu mês."}
          </p>
          {success ? <p role="status">{success}</p> : resetting && !token.trim() ? <p role="alert">Este link não contém um token. Solicite um novo link para redefinir sua senha.</p> : <form className="form-stack" onSubmit={submit}>
            {register && (
              <label>
                Nome de usuário
                <Input name="username" required autoComplete="username" placeholder="Seu nome de usuário" />
              </label>
            )}
            {!resetting && <label>
              E-mail
              <Input
                name="email" required
                autoComplete={register || recovery ? "email" : "username"}
                type="email"
                placeholder="voce@exemplo.com"
              />
            </label>}
            {recovery !== "forgot" && <label>
              {resetting ? "Nova senha" : "Senha"}
              <div className="password-input">
                <Input
                  name="password" required minLength={register || resetting ? passwordRules.minLength : undefined} maxLength={register || resetting ? passwordRules.maxLength : undefined}
                  autoComplete={register || resetting ? "new-password" : "current-password"}
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
            </label>}
            {!register && !recovery && <p className="auth-switch"><Link to="/forgot-password">Esqueceu sua senha?</Link></p>}
            {error && <p role="alert">{error}</p>}
            <Button type="submit" disabled={pending}>
              {pending ? "Aguarde…" : recovery ? resetting ? "Redefinir senha" : "Enviar link" : register ? "Criar conta" : "Entrar"}
            </Button>
          </form>}
          {recovery && <p className="auth-switch"><Link to="/login">Voltar para entrar</Link>{resetting && <> · <Link to="/forgot-password">Solicitar novo link</Link></>}</p>}
          {!recovery && <p className="auth-switch">
            {register ? "Já tem uma conta?" : "Ainda não tem uma conta?"}{" "}
            <Link to={register ? "/login" : "/register"}>
              {register ? "Entrar" : "Criar uma conta"}
            </Link>
          </p>}

        </div>
        <p className="auth-bottom small muted">Uma forma tranquila de acompanhar seus gastos.</p>
      </main>
    </div>
  );
}
