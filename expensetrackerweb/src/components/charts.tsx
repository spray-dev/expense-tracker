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
import { categoryColors } from "@/data/categories";
import { money } from "@/lib/format";
export function TrendChart({ data }: { data: { month: string; spending: number }[] }) {
  return (
    <div
      className="chart"
      role="img"
      aria-label={data.map(item => `${item.month}: ${money(item.spending)}`).join(", ")}
    >
      <ResponsiveContainer width="100%" height="100%">
        <AreaChart
          data={data}
          margin={{ top: 15, right: 12, left: 0, bottom: 0 }}
        >
          <defs>
            <linearGradient id="spendingGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="var(--primary)" stopOpacity={0.25} />
              <stop offset="100%" stopColor="var(--primary)" stopOpacity={0} />
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
            stroke="var(--primary)"
            strokeWidth={3}
            fill="url(#spendingGradient)"
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
export function CategoryChart({ totals }: { totals: Record<string, number> }) {
  const data = Object.entries(totals).map(([category, value]) => ({
    category, value, color: categoryColors[category as keyof typeof categoryColors] ?? "#8a7550",
  })).filter(item => item.value > 0);  if (!data.length)
    return (
      <p className="empty-state muted">Nenhum gasto por categoria neste mês.</p>
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
