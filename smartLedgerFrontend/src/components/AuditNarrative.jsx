import { Sparkles } from 'lucide-react';

export default function AuditNarrative({ narrative }) {
  if (!narrative) {
    return null;
  }

  // Split into paragraphs for readable rendering
  const paragraphs = narrative
    .split(/\n\s*\n/)
    .map((p) => p.trim())
    .filter(Boolean);

  return (
    <div className="paper-card narrative-card">
      <style>{`
        .narrative-card {
          padding: 28px 32px;
          position: relative;
          overflow: hidden;
        }
        .narrative-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 16px;
        }
        .narrative-header .icon-wrap {
          width: 34px;
          height: 34px;
          border-radius: 50%;
          background: var(--green-forest);
          color: var(--bg-paper);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
        }
        .narrative-title {
          font-family: var(--font-display);
          font-size: 18px;
          margin: 0;
          color: var(--ink);
        }
        .narrative-subtitle {
          font-size: 12px;
          color: var(--sage);
          font-family: var(--font-mono);
        }
        .narrative-body {
          font-size: 15px;
          line-height: 1.75;
          color: var(--ink);
          font-family: var(--font-body);
        }
        .narrative-body p {
          margin: 0 0 14px;
        }
        .narrative-body p:last-child {
          margin-bottom: 0;
        }
        .narrative-card::after {
          content: '';
          position: absolute;
          top: -40px;
          right: -40px;
          width: 140px;
          height: 140px;
          border-radius: 50%;
          background: radial-gradient(circle, rgba(212, 168, 87, 0.12), transparent 70%);
        }
      `}</style>

      <div className="narrative-header">
        <div className="icon-wrap">
          <Sparkles size={16} />
        </div>
        <div>
          <h3 className="narrative-title">Audit Narrative</h3>
          <div className="narrative-subtitle">written by Claude</div>
        </div>
      </div>

      <div className="narrative-body">
        {paragraphs.map((p, idx) => (
          <p key={idx}>{p}</p>
        ))}
      </div>
    </div>
  );
}
