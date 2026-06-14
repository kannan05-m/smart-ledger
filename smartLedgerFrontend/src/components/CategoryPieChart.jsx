import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const PALETTE = [
  '#1F4D3D', // forest
  '#D4A857', // gold
  '#2E6E58', // light green
  '#C0392B', // red
  '#7A8C85', // sage
  '#8E6C3A', // bronze
  '#4A6E63', // teal-grey
  '#B8895A', // clay
];

const formatCurrency = (value, currency = 'INR') =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value ?? 0);

function CustomTooltip({ active, payload, currency }) {
  if (!active || !payload?.length) return null;
  const { name, value, percent } = payload[0].payload;
  return (
    <div
      style={{
        background: 'var(--bg-paper)',
        border: '1px solid var(--bg-paper-dim)',
        borderRadius: 6,
        padding: '8px 12px',
        fontSize: 13,
        fontFamily: 'var(--font-body)',
        boxShadow: 'var(--shadow-card)',
      }}
    >
      <div style={{ fontWeight: 600, marginBottom: 2 }}>{name}</div>
      <div style={{ fontFamily: 'var(--font-mono)', color: 'var(--sage)' }}>
        {formatCurrency(value, currency)} &middot; {(percent * 100).toFixed(1)}%
      </div>
    </div>
  );
}

export default function CategoryPieChart({ categories, currency = 'INR' }) {
  const data = (categories || []).map((c) => ({
    name: c.category,
    value: c.total,
  }));

  const total = data.reduce((sum, d) => sum + d.value, 0);
  const dataWithPercent = data.map((d) => ({ ...d, percent: total ? d.value / total : 0 }));

  if (!data.length) {
    return (
      <div className="paper-card chart-card empty-state">
        <h3 className="chart-title">Spending by Category</h3>
        <p className="empty-text">No categorized transactions yet.</p>
      </div>
    );
  }

  return (
    <div className="paper-card chart-card">
      <style>{`
        .chart-card {
          padding: 24px;
        }
        .chart-title {
          font-family: var(--font-display);
          font-size: 18px;
          margin: 0 0 16px;
          color: var(--ink);
        }
        .empty-text {
          color: var(--sage);
          font-size: 14px;
          margin: 0;
        }
      `}</style>
      <h3 className="chart-title">Spending by Category</h3>
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          <Pie
            data={dataWithPercent}
            dataKey="value"
            nameKey="name"
            cx="50%"
            cy="50%"
            innerRadius={60}
            outerRadius={100}
            paddingAngle={2}
          >
            {dataWithPercent.map((entry, idx) => (
              <Cell key={entry.name} fill={PALETTE[idx % PALETTE.length]} stroke="var(--bg-paper)" strokeWidth={2} />
            ))}
          </Pie>
          <Tooltip content={<CustomTooltip currency={currency} />} />
          <Legend
            verticalAlign="bottom"
            iconType="circle"
            wrapperStyle={{ fontSize: 12, fontFamily: 'var(--font-body)', color: 'var(--ink)' }}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
