import { useEffect, useRef, useState } from "react";
import { api, authError, getToken, setToken, type User } from "@/lib/api";
import { AuthContext } from "./context";
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [attempt, setAttempt] = useState(0);
  const generation = useRef(0);
  function logout() { generation.current++; setToken(null); setUser(null); setError(""); setLoading(false); }
  useEffect(() => {
    const ended = () => { generation.current++; setUser(null); setError(""); setLoading(false); };
    window.addEventListener("expense-session-ended", ended);
    return () => window.removeEventListener("expense-session-ended", ended);
  }, []);
  useEffect(() => {
    let active = true;
    const current = generation.current;
    if (!getToken()) { Promise.resolve().then(() => { if (active) setLoading(false); }); return; }
    api.get<User>("/api/users/me").then(({ data }) => {
      if (active && current === generation.current) setUser(data);
    }).catch(err => {
      if (active && current === generation.current && getToken()) setError(authError(err));
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [attempt]);
  useEffect(() => {
    if (!user) return;
    try {
      const payload = JSON.parse(atob(getToken()!.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
      if (typeof payload.exp !== "number") return;
      const remaining = payload.exp * 1000 - Date.now();
      const timer = window.setTimeout(() => {
        generation.current++; setToken(null); setUser(null); setError("");
      }, Math.max(0, Math.min(remaining, 2147483647)));
      return () => window.clearTimeout(timer);
    } catch { /* Server verification remains authoritative. */ }
  }, [user]);
  async function login(email: string, password: string) {
    const current = ++generation.current;
    const { data } = await api.post<{ token: string }>("/api/auth/login", { email, password });
    if (!data.token || typeof data.token !== "string") throw new Error("Invalid token response");
    if (current !== generation.current) return;
    setToken(data.token);
    try {
      const response = await api.get<User>("/api/users/me");
      if (current === generation.current) { setUser(response.data); setError(""); }
    } catch (err) { if (current === generation.current) logout(); throw err; }
  }
  async function register(username: string, email: string, password: string) {
    await api.post("/api/auth/register", { username, email, password });
    try { await login(email, password); } catch { throw new Error("Cadastro concluído. Entre com seu e-mail e senha para continuar."); }
  }
  return <AuthContext.Provider value={{ user, loading, error, retry: () => { setLoading(true); setError(""); setAttempt(v => v + 1); }, login, register, logout }}>{children}</AuthContext.Provider>;
}
