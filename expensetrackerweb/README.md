# Controle de Despesas

Frontend React, TypeScript e Vite com autenticação real. Painel, despesas, orçamento e análises continuam usando dados de demonstração. O perfil exibe apenas nome de usuário e e-mail retornados por `/api/users/me`; edição, CSV e operações financeiras ainda estão indisponíveis.

## Executar

```sh
npm ci
npm run dev -- --host localhost --port 5173 --strictPort
```

Use `http://localhost:5173`, a origem permitida pelo CORS atual do Spring Boot. `http://127.0.0.1:5173` é outra origem e não está autorizada. Nenhuma alteração no backend é necessária.

Copie `.env.example` para `.env.local` se precisar alterar a URL:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Reinicie o Vite após alterar variáveis. A mesma variável deve ser definida antes de gerar o build para outro ambiente. Ela é pública; não coloque segredos nela.

## Fluxo de autenticação

- `src/lib/api.ts` centraliza Axios, URL, timeout, token, cabeçalho Bearer e tratamento de 401.
- `src/auth/provider.tsx` mantém usuário, verificação inicial, entrada, cadastro e saída. O cadastro retorna usuário, portanto é seguido por login para obter o JWT e por `/me` para validar a sessão.
- `src/auth/context.ts` oferece o estado aos componentes. `protected-route.tsx` bloqueia páginas até confirmar `/me` e retorna ao login quando não há sessão. A página solicitada é retomada após entrar.
- O JWT fica na memória e no `sessionStorage` sob `expense-auth-token`. Sobrevive ao recarregamento da aba e é removido ao sair, expirar ou receber 401 de uma requisição protegida. Senhas e dados do usuário não são persistidos pelo aplicativo. Se o armazenamento não estiver disponível, a sessão funciona apenas em memória.
- `sessionStorage` é acessível ao JavaScript da mesma origem; esta solução não usa cookie HttpOnly e depende da prevenção de XSS. O servidor continua sendo responsável por validar o JWT. Logout encerra a sessão local, sem revogar o token no servidor.
- Falhas temporárias ao restaurar `/me` mostram erro e permitem tentar novamente. Um 401 atrasado de uma sessão antiga não encerra uma sessão mais nova.

## Interface

Toda a interface está em pt-BR, incluindo validação dos campos, estados, acessibilidade, diálogos e dados de demonstração. Valores usam BRL e datas/números usam convenções brasileiras. A identidade visual, temas e disposição foram preservados. As cores discretas das categorias agora também aparecem nos chips dos cartões; textos e ícones continuam identificando cada categoria.

## Verificação

```sh
npm run build
npm run lint
npm run test:auth
```

Os testes cobrem armazenamento/restauração do token, envio Bearer em requisições protegidas, 401 público versus protegido e proteção contra respostas atrasadas de uma sessão anterior. Usam Node e TypeScript já presentes no projeto, sem novas dependências.
