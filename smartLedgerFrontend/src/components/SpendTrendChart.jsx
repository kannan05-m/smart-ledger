import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

const formatCurrency = (value, currency = 'INR') =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value ?? 0);

const formatShortDate = (dateStr) => {
  const d = new Date(dateStr);
  if (Number.isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
};

function CustomTooltip({ active, payload, label, currency }) {
  if (!active || !payload?.length) return null;
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
      <div style={{ fontWeight: 600, marginBottom: 2 }}>{formatShortDate(label)}</div>
      <div style={{ fontFamily: 'var(--font-mono)', color: 'var(--red-flag)' }}>
        {formatCurrency(payload[0].value, currency)}
      </div>
    </div>
  );
}

export default function SpendTrendChart({ dailySpend, currency = 'INR' }) {
  const data = dailySpend || [];

  if (!data.length) {
    return (
      <div className="paper-card chart-card empty-state">
        <h3 className="chart-title">Daily Spend</h3>
        <p className="empty-text">No spend data available for this period.</p>
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
      <h3 className="chart-title">Daily Spend</h3>
      <ResponsiveContainer width="100%" height={280}>
        <BarChart data={data} margin={{ top: 4, right: 8, left: 0, bottom: 0 }}>
          <CartesianGrid stroke="var(--bg-paper-dim)" vertical={false} />
          <XAxis
            dataKey="date"
            tickFormatter={formatShortDate}
            tick={{ fontSize: 11, fill: 'var(--sage)', fontFamily: 'var(--font-mono)' }}
            axisLine={{ stroke: 'var(--bg-paper-dim)' }}
            tickLine={false}
          />
          <YAxis
            tick={{ fontSize: 11, fill: 'var(--sage)', fontFamily: 'var(--font-mono)' }}
            tickFormatter={(v) => `${v >= 1000 ? `${v / 1000}k` : v}`}
            axisLine={false}
            tickLine={false}
            width={42}
          />
          <Tooltip content={<CustomTooltip currency={currency} />} cursor={{ fill: 'var(--bg-paper-dim)' }} />
          <Bar dataKey="total" fill="var(--green-forest)" radius={[3, 3, 0, 0]} maxBarSize={28} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
