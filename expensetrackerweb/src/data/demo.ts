// UI-only fixtures. Replace this module with real data when integration is approved.
// Categories, profile fields and amounts here do not define backend capabilities.
export const demoProfile = {
  name: "Alex Morgan",
  email: "alex@example.com",
  initials: "AM",
};
export const categories = [
  "Alimentação",
  "Compras",
  "Transporte",
  "Moradia",
  "Lazer",
] as const;
export type Category = (typeof categories)[number];
export type DemoExpense = {
  id: number;
  description: string;
  category: Category;
  amount: number;
  date: string;
  note: string;
};
export const demoExpenses: DemoExpense[] = [
  {
    id: 1,
    description: "Compras da semana",
    category: "Alimentação",
    amount: 248.9,
    date: "2026-10-06",
    note: "Um novo começo para a semana",
  },
  {
    id: 2,
    description: "Café e conversa",
    category: "Alimentação",
    amount: 32.5,
    date: "2026-10-05",
    note: "As pequenas coisas",
  },
  {
    id: 3,
    description: "Tênis de corrida",
    category: "Compras",
    amount: 329,
    date: "2026-10-04",
    note: "Pronto para o próximo quilômetro",
  },
  {
    id: 4,
    description: "Corrida pela cidade",
    category: "Transporte",
    amount: 28.6,
    date: "2026-10-04",
    note: "Em movimento",
  },
  {
    id: 5,
    description: "Aluguel de outubro",
    category: "Moradia",
    amount: 1450,
    date: "2026-10-01",
    note: "Lar doce lar",
  },
  {
    id: 6,
    description: "Noite de cinema",
    category: "Lazer",
    amount: 64,
    date: "2026-10-03",
    note: "Com pipoca",
  },
  {
    id: 7,
    description: "Almoço com amigos",
    category: "Alimentação",
    amount: 86,
    date: "2026-10-02",
    note: "Uma boa tarde",
  },
  {
    id: 8,
    description: "Conta de internet",
    category: "Moradia",
    amount: 119.9,
    date: "2026-10-02",
    note: "Essenciais do mês",
  },
  {
    id: 9,
    description: "Livros para o fim de semana",
    category: "Lazer",
    amount: 78,
    date: "2026-10-01",
    note: "Um pouco de inspiração",
  },
  {
    id: 10,
    description: "Passe de ônibus",
    category: "Transporte",
    amount: 95,
    date: "2026-10-01",
    note: "Trajetos do dia a dia",
  },
  {
    id: 11,
    description: "Utensílios de cozinha",
    category: "Compras",
    amount: 139.9,
    date: "2026-10-02",
    note: "Uma melhoria para a casa",
  },
  {
    id: 12,
    description: "Jantar fora",
    category: "Alimentação",
    amount: 122,
    date: "2026-10-06",
    note: "Algo delicioso",
  },
  {
    id: 13,
    description: "Compras de setembro",
    category: "Alimentação",
    amount: 285,
    date: "2026-09-27",
    note: "Exemplo de um mês anterior",
  },
  {
    id: 14,
    description: "Aluguel de setembro",
    category: "Moradia",
    amount: 1450,
    date: "2026-09-01",
    note: "Essenciais do mês",
  },
  {
    id: 15,
    description: "Viagem de fim de semana",
    category: "Lazer",
    amount: 380,
    date: "2026-08-15",
    note: "Exemplo de agosto",
  },
];
export const demoBudget = 4000;
export const demoTrend = [
  { month: "Mai", spending: 2650 },
  { month: "Jun", spending: 3100 },
  { month: "Jul", spending: 2870 },
  { month: "Ago", spending: 3320 },
  { month: "Set", spending: 3050 },
  { month: "Out", spending: 2893.8 },
];
export const categoryColors: Record<string, string> = {
  Alimentação: "#b58736",
  Compras: "#ee9077",
  Transporte: "#4dafab",
  Moradia: "#cfad63",
  Lazer: "#7599dd",
  Serviços: "#6c9990", Vestuário: "#c38469", Contas: "#a58b55", Presentes: "#c78691", Combustível: "#88975b", Carro: "#66979a", Viagens: "#718db1", Diversos: "#998671", Outros: "#8a7550",
};
