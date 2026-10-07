import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import {
  categoryColors,
  type DemoExpense,
  type Category,
  demoTrend,
} from "@/data/demo";
import { money } from "@/lib/format";
export function TrendChart() {
  return (
    <div
      className="chart"
      role="img"
      aria-label="Gastos mensais ilustrativos: maio 2.650, junho 3.100, julho 2.870, agosto 3.320, setembro 3.050, outubro 2.893,80 reais"
    >
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart
          data={demoTrend}
          margin={{ top: 15, right: 12, left: 0, bottom: 0 }}
        >
          <defs>
            <linearGradient id="spendingGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="#8b6cef" stopOpacity={0.25} />
              <stop offset="100%" stopColor="#8b6cef" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid
            stroke="var(--border)"
            vertical={false}
            strokeDasharray="4 4"
          />
          <XAxis
            dataKey="month"
            tickLine={false}
            axisLine={false}
            tick={{ fill: "var(--muted-foreground)", fontSize: 12 }}
            dy={10}
          />
          <YAxis
            tickLine={false}
            axisLine={false}
            tick={{ fill: "var(--muted-foreground)", fontSize: 11 }}
            width={48}
            tickFormatter={(v) => `${new Intl.NumberFormat("pt-BR").format(Number(v) / 1000)} mil`}
          />
          <Tooltip
            contentStyle={{
              background: "var(--card)",
              border: "1px solid var(--border)",
              borderRadius: 4,
              color: "var(--foreground)",
            }}
            formatter={(v) => [money(Number(v)), "Gastos"]}
          />
          <Area
            type="monotone"
            dataKey="spending"
            stroke="#8b6cef"
            strokeWidth={3}
            fill="url(#spendingGradient)"
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
export function CategoryChart({ expenses }: { expenses: DemoExpense[] }) {
  const data = Object.entries(categoryColors)
    .map(([category, color]) => ({
      category: category as Category,
      color,
      value: expenses
        .filter((e) => e.category === category)
        .reduce((sum, e) => sum + e.amount, 0),
    }))
    .filter((c) => c.value > 0);
  if (!data.length)
    return (
      <p className="empty-state muted">Nenhum dado de categoria de demonstração neste mês.</p>
    );
  return (
    <div className="category-chart">
      <div
        className="donut"
        role="img"
        aria-label={data
          .map((d) => `${d.category}: ${money(d.value)}`)
          .join(", ")}
      >
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              dataKey="value"
              innerRadius="66%"
              outerRadius="92%"
              paddingAngle={4}
              stroke="none"
            >
              {data.map((d) => (
                <Cell key={d.category} fill={d.color} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                background: "var(--card)",
                border: "1px solid var(--border)",
                borderRadius: 4,
              }}
              formatter={(v, _n, item) => [
                money(Number(v)),
                item.payload.category,
              ]}
            />
          </PieChart>
        </ResponsiveContainer>
        <div className="donut-label">
          <strong>{data.length}</strong>
          <span>categorias</span>
        </div>
      </div>
      <div className="legend">
        {data.map((d) => (
          <div key={d.category}>
            <span>
              <i style={{ background: d.color }} />
              {d.category}
            </span>
            <strong>{money(d.value)}</strong>
          </div>
        ))}
      </div>
    </div>
  );
}
