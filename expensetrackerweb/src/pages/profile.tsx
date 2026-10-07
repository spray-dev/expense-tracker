import { useCallback, useRef, useState, type FormEvent } from "react";
import { PageIntro, ThemeToggle } from "@/components/shared";
import { ResourceState } from "@/components/resource-state";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/auth/context";
import { useTheme } from "@/components/theme-context";
import { getProfile, updateProfile, deleteCurrentAccount, requestError } from "@/lib/finance";
import { useResource } from "@/lib/use-resource";

export default function Profile() {
  const { logout, updateUser } = useAuth();
  const { theme } = useTheme();
  const load = useCallback((signal: AbortSignal) => getProfile(signal), []);
  const resource = useResource("profile", load);
  const [busy, setBusy] = useState(false);
  const [feedback, setFeedback] = useState<{ error: boolean; message: string }>();
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleteText, setDeleteText] = useState("");
  const lock = useRef(false);
  async function save(event: FormEvent<HTMLFormElement>, field: "username" | "email" | "password") {
    event.preventDefault(); if (lock.current) return;
    const form = event.currentTarget; const fields = new FormData(form);
    const value = field === "password" ? String(fields.get(field)) : String(fields.get(field)).trim();
    if (!value.trim()) { setFeedback({ error: true, message: "Preencha o campo informado." }); return; }
    if (field === "password" && value !== fields.get("confirmation")) { setFeedback({ error: true, message: "As senhas não coincidem." }); return; }
    lock.current = true; setBusy(true); setFeedback(undefined);
    try { const user = await updateProfile(field, value); updateUser(user); resource.refresh(); if (field === "password") form.reset(); setFeedback({ error: false, message: field === "password" ? "Senha alterada." : "Dados atualizados." }); }
    catch (error) { setFeedback({ error: true, message: requestError(error) }); }
    finally { lock.current = false; setBusy(false); }
  }
  async function remove() {
    if (lock.current || deleteText !== "EXCLUIR") return;
    lock.current = true; setBusy(true); setFeedback(undefined);
    try { await deleteCurrentAccount(logout); }
    catch (error) { setFeedback({ error: true, message: requestError(error) }); }
    finally { lock.current = false; setBusy(false); }
  }
  const user = resource.data;
  return <>
    <PageIntro eyebrow="SEU ESPAÇO PESSOAL" title="Sinta-se em casa." description="Seus dados de conta e preferências pessoais." />
    {feedback && <p className={`feedback ${feedback.error ? "" : "success"}`} role={feedback.error ? "alert" : "status"}>{feedback.message}</p>}
    {resource.loading || resource.error ? <ResourceState error={resource.error} retry={resource.refresh} /> : user && <div className="profile-layout">
      <section className="panel"><div className="profile-heading"><span className="avatar avatar-large">{user.username.slice(0, 2).toLocaleUpperCase("pt-BR")}</span><div><h2>{user.username}</h2><p className="muted small">Conta autenticada</p></div></div>
        <form key={`username-${user.username}`} className="form-stack profile-form" onSubmit={event => save(event, "username")}><label>Nome de usuário<Input name="username" required defaultValue={user.username} disabled={busy} /></label><Button disabled={busy}>Salvar nome de usuário</Button></form>
        <form key={`email-${user.email}`} className="form-stack profile-form" onSubmit={event => save(event, "email")}><label>E-mail<Input name="email" type="email" required defaultValue={user.email} disabled={busy} /></label><Button disabled={busy}>Salvar e-mail</Button></form>
        <form className="form-stack profile-form" onSubmit={event => save(event, "password")}><label>Nova senha<Input name="password" type="password" required minLength={8} maxLength={100} autoComplete="new-password" disabled={busy} /></label><label>Confirmar nova senha<Input name="confirmation" type="password" required minLength={8} maxLength={100} autoComplete="new-password" disabled={busy} /></label><Button disabled={busy}>Alterar senha</Button></form>
      </section>
      <div><section className="panel"><h2>Aparência</h2><p className="muted small">Uma visualização confortável de dia ou à noite.</p><div className="appearance-row"><span>Tema {theme === "dark" ? "escuro" : "claro"}</span><ThemeToggle /></div></section>
        <section className="panel account-note"><h2>Configurações da conta</h2><Button variant="outline" onClick={logout} disabled={busy}>Sair</Button><Button variant="outline" disabled={busy} onClick={() => setConfirmDelete(true)}>Excluir conta</Button>
          {confirmDelete && <div className="confirmation" role="group" aria-label="Confirmar exclusão da conta"><p>Excluir sua conta remove permanentemente todas as suas despesas e orçamentos. Esta ação não pode ser desfeita.</p><label>Digite EXCLUIR para confirmar<Input value={deleteText} onChange={event => setDeleteText(event.target.value)} disabled={busy} /></label><Button variant="destructive" disabled={busy || deleteText !== "EXCLUIR"} onClick={remove}>{busy ? "Excluindo…" : "Excluir minha conta permanentemente"}</Button><Button variant="ghost" disabled={busy} onClick={() => { setConfirmDelete(false); setDeleteText(""); }}>Cancelar</Button></div>}
        </section>
      </div>
    </div>}
  </>;
}
