import { Button } from "./ui/button";
export function ResourceState({ error, retry }: { error?: string; retry: () => void }) {
  return <div className="empty-state" role={error ? "alert" : "status"} aria-live="polite" aria-busy={!error}>
    <h2>{error ? "Não foi possível carregar" : "Carregando seus dados…"}</h2>
    {error && <><p className="muted">{error}</p><Button onClick={retry}>Tentar novamente</Button></>}
  </div>;
}
