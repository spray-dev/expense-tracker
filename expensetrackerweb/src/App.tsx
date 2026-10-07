import { AuthProvider } from "@/auth/provider";
import { ProtectedRoute } from "@/auth/protected-route";
import { BrowserRouter, Navigate, Route, Routes, Link } from "react-router-dom";
import { ThemeProvider } from "@/components/theme-provider";
import { AppLayout } from "@/components/layout";
import { lazy, Suspense } from "react";
const Dashboard = lazy(() => import("@/pages/dashboard"));
const Expenses = lazy(() => import("@/pages/expenses"));
const Budget = lazy(() => import("@/pages/budget"));
const Analytics = lazy(() => import("@/pages/analytics"));
const Profile = lazy(() => import("@/pages/profile"));
const Auth = lazy(() => import("@/pages/auth"));
export default function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <AuthProvider>
        <Suspense
          fallback={
            <div className="empty-state" role="status">
              Carregando seu espaço…
            </div>
          }
        >
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/login" element={<Auth key="login" />} />
            <Route path="/register" element={<Auth key="register" register />} />
            <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/expenses" element={<Expenses />} />
              <Route path="/budget" element={<Budget />} />
              <Route path="/analytics" element={<Analytics />} />
              <Route path="/profile" element={<Profile />} />
              <Route
                path="*"
                element={
                  <div className="empty-state">
                    <h1>Página não encontrada</h1>
                    <p className="muted">Vamos voltar à sua visão geral.</p>
                    <Link to="/dashboard">Ir para o painel</Link>
                  </div>
                }
              />
            </Route>
          </Route>
          </Routes>
        </Suspense>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}
