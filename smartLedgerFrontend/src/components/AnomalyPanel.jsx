import { useState } from 'react';
import { AlertOctagon, AlertTriangle, Info, ChevronDown, ShieldCheck } from 'lucide-react';

const formatCurrency = (value, currency = 'INR') =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency,
    maximumFractionDigits: 2,
  }).format(value ?? 0);

const formatDate = (dateStr) => {
  const d = new Date(dateStr);
  if (Number.isNaN(d.getTime())) return dateStr;
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
};

const SEVERITY_CONFIG = {
  HIGH: { icon: AlertOctagon, color: 'var(--red-flag)', bg: 'var(--red-soft)', label: 'High' },
  MEDIUM: { icon: AlertTriangle, color: '#A9762B', bg: '#F6EAD3', label: 'Medium' },
  LOW: { icon: Info, color: 'var(--sage)', bg: 'var(--bg-paper-dim)', label: 'Low' },
};

function severityConfig(severity) {
  return SEVERITY_CONFIG[severity?.toUpperCase()] || SEVERITY_CONFIG.LOW;
}

export default function AnomalyPanel({ anomalies, transactions, currency = 'INR' }) {
  const [expandedId, setExpandedId] = useState(null);
  const txnMap = new Map((transactions || []).map((t) => [t.id, t]));

  const sorted = [...(anomalies || [])].sort((a, b) => {
    const order = { HIGH: 0, MEDIUM: 1, LOW: 2 };
    return (order[a.severity?.toUpperCase()] ?? 3) - (order[b.severity?.toUpperCase()] ?? 3);
  });

  return (
    <div className="paper-card anomaly-panel">
      <style>{`
        .anomaly-panel {
          padding: 24px;
        }
        .anomaly-title {
          font-family: var(--font-display);
          font-size: 18px;
          margin: 0 0 4px;
          color: var(--ink);
          display: flex;
          align-items: center;
          gap: 8px;
        }
        .anomaly-sub {
          font-size: 12.5px;
          color: var(--sage);
          margin: 0 0 16px;
        }
        .anomaly-empty {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 10px;
          padding: 32px 16px;
          color: var(--green-forest);
          text-align: center;
        }
        .anomaly-list {
          display: flex;
          flex-direction: column;
          gap: 10px;
        }
        .anomaly-item {
          border: 1px solid var(--bg-paper-dim);
          border-radius: var(--radius);
          overflow: hidden;
        }
        .anomaly-row {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 13px 14px;
          cursor: pointer;
        }
        .severity-icon {
          width: 30px;
          height: 30px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }
        .anomaly-main {
          flex: 1;
          min-width: 0;
        }
        .anomaly-type {
          font-size: 13.5px;
          font-weight: 600;
          color: var(--ink);
          margin-bottom: 2px;
        }
        .anomaly-desc {
          font-size: 12.5px;
          color: var(--sage);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .severity-tag {
          font-size: 11px;
          font-weight: 700;
          text-transform: uppercase;
          letter-spacing: 0.05em;
          padding: 3px 9px;
          border-radius: 10px;
          white-space: nowrap;
        }
        .anomaly-details {
          padding: 0 14px 14px 56px;
          font-size: 13px;
          color: var(--ink);
          line-height: 1.6;
        }
        .anomaly-details .txn-ref {
          margin-top: 8px;
          font-family: var(--font-mono);
          font-size: 12px;
          background: var(--bg-paper-dim);
          border-radius: 4px;
          padding: 8px 10px;
          display: flex;
          justify-content: space-between;
          gap: 12px;
        }
        .chevron {
          transition: transform 0.15s ease;
          color: var(--sage);
          flex-shrink: 0;
        }
        .chevron.open {
          transform: rotate(180deg);
        }
      `}</style>

      <h3 className="anomaly-title">
        <AlertTriangle size={18} color="var(--gold)" />
        Flagged Transactions
      </h3>
      <p className="anomaly-sub">
        {sorted.length} {sorted.length === 1 ? 'item' : 'items'} need your attention
      </p>

      {sorted.length === 0 && (
        <div className="anomaly-empty">
          <ShieldCheck size={32} />
          <div>No anomalies detected. Everything checks out.</div>
        </div>
      )}

      <div className="anomaly-list">
        {sorted.map((anomaly, idx) => {
          const config = severityConfig(anomaly.severity);
          const Icon = config.icon;
          const id = anomaly.id ?? `${anomaly.txnId}-${idx}`;
          const isOpen = expandedId === id;
          const txn = txnMap.get(anomaly.txnId);

          return (
            <div key={id} className="anomaly-item">
              <div className="anomaly-row" onClick={() => setExpandedId(isOpen ? null : id)}>
                <div className="severity-icon" style={{ background: config.bg, color: config.color }}>
                  <Icon size={15} />
                </div>
                <div className="anomaly-main">
                  <div className="anomaly-type">{anomaly.type}</div>
                  <div className="anomaly-desc">{anomaly.description}</div>
                </div>
                <span className="severity-tag" style={{ background: config.bg, color: config.color }}>
                  {config.label}
                </span>
                <ChevronDown size={16} className={`chevron ${isOpen ? 'open' : ''}`} />
              </div>

              {isOpen && (
                <div className="anomaly-details">
                  {anomaly.description}
                  {txn && (
                    <div className="txn-ref">
                      <span>
                        {formatDate(txn.date)} &middot; {txn.description}
                      </span>
                      <span style={{ fontWeight: 600 }}>
                        {txn.type === 'DR' ? '-' : '+'}
                        {formatCurrency(Math.abs(txn.amount), currency)}
                      </span>
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
