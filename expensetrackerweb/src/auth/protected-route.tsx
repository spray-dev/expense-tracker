import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./context";
import { Button } from "@/components/ui/button";
export function ProtectedRoute() {
  const { user, loading, error, retry, logout } = useAuth();
  const location = useLocation();
  if (loading) return <div className="empty-state" role="status">Verificando sua sessão…</div>;
  if (error) return <div className="empty-state"><p role="alert">{error}</p><Button onClick={retry}>Tentar novamente</Button><Button variant="outline" onClick={logout}>Voltar ao login</Button></div>;
  return user ? <Outlet /> : <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
}
