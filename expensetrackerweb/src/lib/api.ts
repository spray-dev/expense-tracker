import axios from "axios";
export type User = { id: number; username: string; email: string };
const key = "expense-auth-token";
let token: string | null = null;
try { token = sessionStorage.getItem(key); } catch { /* Memory-only fallback. */ }
export function getToken() { return token; }
export function setToken(value: string | null) {
  token = value;
  try { if (value) sessionStorage.setItem(key, value); else sessionStorage.removeItem(key); } catch { /* Memory-only fallback. */ }
}
export const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080", timeout: 15000 });
api.interceptors.request.use((config) => {
  if (token && !config.url?.startsWith("/api/auth/")) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
api.interceptors.response.use(response => response, error => {
  if (error.response?.status === 401 && !error.config?.url?.startsWith("/api/auth/")) {
    const sent = error.config?.headers?.Authorization;
    if (sent === `Bearer ${token}`) {
      setToken(null);
      window.dispatchEvent(new Event("expense-session-ended"));
    }
  }
  return Promise.reject(error);
});
export function authError(error: unknown): string {
  if (!axios.isAxiosError(error)) return "Não foi possível concluir. Tente novamente.";
  if (!error.response) return "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.";
  if (error.response.status === 401) return "E-mail ou senha incorretos, ou sessão expirada. Entre novamente.";
  if (error.response.status === 409) return "Este e-mail já está cadastrado.";
  if (error.response.status === 400) return "Confira os dados informados. A senha deve ter entre 8 e 100 caracteres.";
  if (error.response.status === 429) return "Muitas tentativas. Aguarde e tente novamente.";
  return "Não foi possível concluir a solicitação. Tente novamente.";
}

export const passwordRules = { minLength: 8, maxLength: 100 };
export function validateNewPassword(password: string) {
  return password.trim() && password.length >= passwordRules.minLength && password.length <= passwordRules.maxLength
    ? "" : "A senha deve ter entre 8 e 100 caracteres e não pode conter apenas espaços.";
}
export async function forgotPassword(email: string) {
  const { data } = await api.post<{ message: string }>("/api/auth/forgot-password", { email: email.trim() });
  return data.message === "If an account exists for this email, a reset link has been sent."
    ? "Se existir uma conta com este e-mail, um link para redefinir sua senha foi enviado."
    : data.message;
}
export async function resetPassword(token: string, newPassword: string) {
  if (!token.trim()) throw new Error("Abra o link enviado por e-mail ou solicite um novo link.");
  const validation = validateNewPassword(newPassword);
  if (validation) throw new Error(validation);
  await api.post("/api/auth/reset-password", { token, newPassword });
}
export function passwordResetError(error: unknown, resetting: boolean): string {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const message = error.response?.data?.message;
    if (resetting && status === 400 && ["Invalid token", "Token has already been used", "Token has expired"].includes(message)) {
      return "Este link é inválido, expirou ou já foi utilizado. Solicite um novo link para redefinir sua senha.";
    }
    if (!resetting && (status === 404 || status === 409)) return "Não foi possível enviar o link. Tente novamente mais tarde.";
    if (status === 400) return resetting ? "Confira a nova senha e tente novamente." : "Confira o e-mail informado e tente novamente.";
  }
  return authError(error);
}
