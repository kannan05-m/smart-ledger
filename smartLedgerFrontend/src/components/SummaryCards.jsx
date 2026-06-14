import { ArrowDownRight, ArrowUpRight, Scale } from 'lucide-react';

const formatCurrency = (value, currency = 'INR') =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value ?? 0);

function healthLabel(score) {
  if (score >= 80) return { label: 'Healthy', color: 'var(--green-forest)' };
  if (score >= 50) return { label: 'Watch', color: 'var(--gold)' };
  return { label: 'At Risk', color: 'var(--red-flag)' };
}

export default function SummaryCards({ summary }) {
  const {
    totalIncome = 0,
    totalExpense = 0,
    netCashflow = totalIncome - totalExpense,
    healthScore = 0,
    currency = 'INR',
    transactionCount = 0,
  } = summary || {};

  const health = healthLabel(healthScore);
  const isPositiveNet = netCashflow >= 0;

  return (
    <div className="summary-cards">
      <style>{`
        .summary-cards {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 16px;
        }
        .summary-card {
          padding: 22px 22px;
          display: flex;
          flex-direction: column;
          gap: 6px;
        }
        .summary-card .label {
          font-size: 12px;
          text-transform: uppercase;
          letter-spacing: 0.08em;
          color: var(--sage);
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 6px;
        }
        .summary-card .value {
          font-family: var(--font-display);
          font-size: 28px;
          font-weight: 600;
          color: var(--ink);
          line-height: 1.2;
        }
        .summary-card .sub {
          font-size: 12px;
          color: var(--sage);
          font-family: var(--font-mono);
        }
        .value.income { color: var(--green-forest); }
        .value.expense { color: var(--red-flag); }
        .value.net.positive { color: var(--green-forest); }
        .value.net.negative { color: var(--red-flag); }

        .health-card {
          display: flex;
          align-items: center;
          gap: 18px;
          padding: 18px 22px;
        }
        .health-stamp {
          width: 76px;
          height: 76px;
          flex-shrink: 0;
          font-size: 9px;
          gap: 2px;
        }
        .health-stamp .score-num {
          font-size: 22px;
          font-weight: 700;
          line-height: 1;
        }
        .health-meta .label {
          margin-bottom: 4px;
        }
        .health-meta .value {
          font-size: 20px;
        }
      `}</style>

      <div className="paper-card summary-card">
        <div className="label">
          <ArrowUpRight size={14} /> Total In
        </div>
        <div className="value income">{formatCurrency(totalIncome, currency)}</div>
        <div className="sub">{transactionCount} transactions analyzed</div>
      </div>

      <div className="paper-card summary-card">
        <div className="label">
          <ArrowDownRight size={14} /> Total Out
        </div>
        <div className="value expense">{formatCurrency(totalExpense, currency)}</div>
        <div className="sub">across all categories</div>
      </div>

      <div className="paper-card summary-card">
        <div className="label">
          <Scale size={14} /> Net Cashflow
        </div>
        <div className={`value net ${isPositiveNet ? 'positive' : 'negative'}`}>
          {isPositiveNet ? '+' : ''}
          {formatCurrency(netCashflow, currency)}
        </div>
        <div className="sub">{isPositiveNet ? 'surplus this period' : 'deficit this period'}</div>
      </div>

      <div className="paper-card health-card">
        <div className="audit-stamp health-stamp" style={{ color: health.color }}>
          <span className="score-num">{healthScore}</span>
          <span>/ 100</span>
        </div>
        <div className="health-meta">
          <div className="label">Health Score</div>
          <div className="value" style={{ color: health.color }}>
            {health.label}
          </div>
        </div>
      </div>
    </div>
  );
}
