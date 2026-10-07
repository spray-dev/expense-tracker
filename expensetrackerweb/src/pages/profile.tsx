import { PageIntro, ThemeToggle } from "@/components/shared";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/auth/context";
import { useTheme } from "@/components/theme-context";
export default function Profile() {
  const { user, logout } = useAuth();
  const { theme } = useTheme();
  return (
    <>
      <PageIntro
        eyebrow="SEU ESPAÇO PESSOAL"
        title="Sinta-se em casa."
        description="Seus dados de conta e preferências pessoais."
      />
      <div className="profile-layout">
        <section className="panel">
          <div className="profile-heading">
            <span className="avatar avatar-large">{user?.username.slice(0, 2).toLocaleUpperCase("pt-BR")}</span>
            <div>
              <h2>{user?.username}</h2>
              <p className="muted small">Conta autenticada</p>
            </div>
          </div>
          <form className="form-stack" onSubmit={(e) => e.preventDefault()}>
            <label>
              Nome de usuário
              <Input defaultValue={user?.username} readOnly />
            </label>
            <label>
              E-mail
              <Input type="email" defaultValue={user?.email} readOnly />
            </label>
            <Button disabled>Salvar alterações · Em breve</Button>
            <p className="small muted">
              Dados verificados pelo servidor. A edição da conta ainda não está disponível.
            </p>
          </form>
        </section>
        <div>
          <section className="panel">
            <h2>Aparência</h2>
            <p className="muted small">Uma visualização confortável de dia ou à noite.</p>
            <div className="appearance-row">
              <span>Tema {theme === "dark" ? "escuro" : "claro"}</span>
              <ThemeToggle />
            </div>
          </section>
          <section className="panel account-note">
            <h2>Configurações da conta</h2>
            <p className="muted small">
              A alteração de senha e outras ações da conta estarão disponíveis em uma próxima etapa.
            </p>
            <Button variant="outline" onClick={logout}>
              Sair
            </Button>
          </section>
        </div>
      </div>
    </>
  );
}
