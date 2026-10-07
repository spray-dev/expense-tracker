# Expense Tracker

Frontend React, TypeScript e Vite com autenticação JWT e integração v1 completa: painel, despesas, orçamento, análises e perfil conectados à API Spring Boot. Sem dados financeiros de demonstração.

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

Toda a interface está em pt-BR, incluindo validação dos campos, estados, acessibilidade, diálogos e feedback das operações. Valores usam BRL e datas/números usam convenções brasileiras. A identidade visual, temas e disposição foram preservados. As cores discretas das categorias agora também aparecem nos chips dos cartões; textos e ícones continuam identificando cada categoria.

## Verificação

```sh
npm run build
npm run lint
npm run test:auth
```

Os testes cobrem armazenamento/restauração do token, envio Bearer em requisições protegidas, 401 público versus protegido e proteção contra respostas atrasadas de uma sessão anterior. Usam Node e TypeScript já presentes no projeto, sem novas dependências.

## Painel conectado

O painel usa `GET /api/expenses/dashboard?year=YYYY&month=M` com o JWT do cliente Axios existente. A seleção de mês recarrega os dados e cancela solicitações anteriores. Totais, média, contagem, categorias e orçamento vêm da resposta; orçamento nulo tem estado próprio. Despesas recentes e maiores também seguem o mês selecionado, com até cinco despesas de cada lista e somente dados do usuário autenticado. O painel não apresenta tendência fictícia. Todas as páginas financeiras usam exclusivamente dados da conta autenticada.

A paleta usa ouro quente no cartão principal, âmbar escuro em ações no tema claro e âmbar suave no tema escuro. Os testes também verificam parâmetros do painel, autenticação, cancelamento, falhas, categorias e datas do backend.


## Integração v1

- Despesas: lista paginada autenticada, busca, categorias do backend, mês selecionado, períodos relativos ao relógio do servidor ou intervalo de datas inclusivo, ordenação, criação/edição com data e hora, exclusão confirmada. O CSV exporta **todas** as despesas da conta, conforme o endpoint existente; não apenas os filtros atuais.
- Orçamento: consulta e criação/atualização do limite no mês selecionado; ausência (404) e excesso tratados separadamente de erros de conexão.
- Análises: tendência de seis meses até o mês selecionado, categorias/maiores categorias mensais e resumo anual, pelos endpoints existentes.
- Perfil: `/me`, atualização separada de nome/e-mail/senha, confirmação de senha, logout e exclusão permanente confirmada com limpeza da sessão após sucesso.
- Carregamento, estados vazios, erros com tentativa novamente, validação e feedback em pt-BR; BRL, paleta âmbar e cores das categorias preservados. Nenhum dado financeiro fictício.

`npm run test:auth` executa testes focados do cliente de autenticação e dos contratos de integração (filtros/datas/paginação, CRUD/CSV, orçamento ausente, análises, perfil e cancelamento). `npm run build` e `npm run lint` verificam o frontend completo.
