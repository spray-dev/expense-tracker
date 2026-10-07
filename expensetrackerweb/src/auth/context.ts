import { createContext, useContext } from "react";
import type { User } from "@/lib/api";
export type AuthState = { user: User | null; loading: boolean; error: string; retry: () => void; login: (email: string, password: string) => Promise<void>; register: (username: string, email: string, password: string) => Promise<void>; updateUser: (user: User) => void; logout: () => void };
export const AuthContext = createContext<AuthState | null>(null);
export function useAuth() { const value = useContext(AuthContext); if (!value) throw new Error("AuthProvider required"); return value; }
