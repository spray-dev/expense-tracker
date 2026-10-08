# Expense Tracker frontend

React/TypeScript/Vite frontend for Expense Tracker, with a responsive pt-BR interface, BRL formatting, and light/dark themes. The [project README](../README.md) is the main reference for v1 capabilities, API routes, setup, security, and deployment status.

## Development

From this directory, with Node/npm installed and the API running:

```powershell
Copy-Item .env.example .env.local
npm ci
npm run dev -- --host localhost --port 5173 --strictPort
```

Open `http://localhost:5173`, the exact origin allowed by current backend CORS. The API URL defaults to `http://localhost:8080`; `.env.example` documents `VITE_API_BASE_URL`. Restart Vite after changes. Set this public variable before building for another environment; never put secrets in it.

```powershell
npm run test:auth
npm run build
npm run lint
```

The 16 focused Node client/contract tests passed on 2026-10-08, along with build and lint. These are not browser end-to-end tests. `npm run preview` serves the generated `dist/` build, but its default origin is outside current backend CORS.

## Integration notes

- `src/lib/api.ts` centralizes Axios, the base URL, bearer token, timeout, and protected 401 handling.
- `src/auth/` restores the session through `/api/users/me`, protects pages, and handles login/register/logout. Registration returns a user, followed by login to obtain a token.
- JWT storage uses memory and `sessionStorage` (`expense-auth-token`), with a memory-only fallback. Logout is local; the server does not revoke tokens. Stale-session responses are guarded against.
- `src/lib/dashboard.ts` loads the selected-month dashboard, including up to five recent and largest expenses within that month.
- `src/lib/finance.ts` maps filters, CRUD/CSV, budgets, analytics, and profile requests to backend contracts. CSV contains all account expenses regardless of active list filters. Analytics requests six months ending in the selected month.
- `src/lib/use-resource.ts` handles cancellable loading and retry states. Pages distinguish missing budgets and empty data from request failures.
- `src/pages/` contains auth, dashboard, expenses, budget, analytics, and profile screens. `src/components/` contains layout, charts, themes, and shared UI; `src/data/categories.ts` supplies category presentation.

All financial screens use authenticated API data. Keep the Expense Tracker brand and Brazilian Portuguese interface consistent when adding the planned real screenshots.
