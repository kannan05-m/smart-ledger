import { useEffect, useState, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ScrollText, Tags, ShieldAlert, FileCheck2, AlertTriangle } from 'lucide-react';
import { pollAudit } from '../services/api';

const STAGES = [
  { key: 'PARSING', label: 'Reading transactions', icon: ScrollText },
  { key: 'CATEGORIZING', label: 'Categorizing with Claude', icon: Tags },
  { key: 'ANOMALY_CHECK', label: 'Scanning for anomalies', icon: ShieldAlert },
  { key: 'SUMMARIZING', label: 'Writing audit narrative', icon: FileCheck2 },
];

const POLL_INTERVAL_MS = 2000;

export default function LoadingPage() {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const [stageIndex, setStageIndex] = useState(0);
  const [error, setError] = useState(null);
  const elapsedRef = useRef(0);
  const [elapsed, setElapsed] = useState(0);

  useEffect(() => {
    if (!sessionId) {
      navigate('/');
      return;
    }

    let cancelled = false;

    const interval = setInterval(async () => {
      elapsedRef.current += POLL_INTERVAL_MS / 1000;
      setElapsed(elapsedRef.current);

      try {
        const report = await pollAudit(sessionId);
        if (cancelled) return;

        if (report.status === 'READY') {
          clearInterval(interval);
          navigate('/dashboard', { state: { report, sessionId } });
          return;
        }

        if (report.status === 'FAILED') {
          clearInterval(interval);
          setError(report.error || 'The audit failed while processing your file.');
          return;
        }

        if (report.stage) {
          const idx = STAGES.findIndex((s) => s.key === report.stage);
          if (idx !== -1) setStageIndex(idx);
        } else {
          // Fallback: advance based on elapsed time if backend doesn't report a stage
          setStageIndex((prev) => Math.min(prev + (Math.random() > 0.6 ? 1 : 0), STAGES.length - 1));
        }
      } catch (err) {
        if (cancelled) return;
        clearInterval(interval);
        setError('Lost connection to the audit service. Please try again.');
      }
    }, POLL_INTERVAL_MS);

    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, [sessionId, navigate]);

  return (
    <div className="loading-page">
      <style>{`
        .loading-page {
          max-width: 640px;
          margin: 60px auto;
          text-align: center;
          color: var(--bg-paper);
        }
        .loading-stamp-wrap {
          display: flex;
          justify-content: center;
          margin-bottom: 32px;
        }
        .loading-stamp {
          width: 120px;
          height: 120px;
          color: var(--gold);
          font-size: 13px;
          animation: spin-slow 18s linear infinite, pulse-ring 2.4s ease-out infinite;
        }
        .loading-page h1 {
          font-family: var(--font-display);
          font-size: 30px;
          margin: 0 0 8px;
        }
        .loading-page .session-id {
          font-family: var(--font-mono);
          font-size: 12px;
          color: var(--sage);
          margin-bottom: 36px;
        }
        .stage-list {
          display: grid;
          gap: 12px;
          text-align: left;
        }
        .stage-item {
          display: flex;
          align-items: center;
          gap: 14px;
          padding: 14px 18px;
          border-radius: var(--radius);
          background: rgba(247, 244, 236, 0.04);
          border: 1px solid rgba(247, 244, 236, 0.08);
          font-size: 14px;
          color: var(--sage);
          transition: all 0.25s ease;
        }
        .stage-item.active {
          background: rgba(212, 168, 87, 0.1);
          border-color: var(--gold);
          color: var(--bg-paper);
        }
        .stage-item.done {
          color: var(--green-light);
        }
        .stage-icon-wrap {
          width: 30px;
          height: 30px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(247, 244, 236, 0.06);
          flex-shrink: 0;
        }
        .stage-item.active .stage-icon-wrap {
          background: var(--gold);
          color: var(--bg-deep);
        }
        .stage-item.done .stage-icon-wrap {
          background: var(--green-forest);
          color: var(--bg-paper);
        }
        .elapsed {
          margin-top: 24px;
          font-family: var(--font-mono);
          font-size: 12px;
          color: var(--sage);
        }
        .loading-error {
          margin-top: 28px;
          background: var(--red-soft);
          color: var(--red-flag);
          border-radius: var(--radius);
          padding: 16px 20px;
          display: flex;
          align-items: center;
          gap: 10px;
          justify-content: center;
          font-size: 14px;
        }
      `}</style>

      <div className="loading-stamp-wrap">
        <div className="audit-stamp loading-stamp">
          <span>Auditing</span>
          <span style={{ fontSize: 22 }}>&hellip;</span>
        </div>
      </div>

      <h1>Running your audit</h1>
      <div className="session-id">session {sessionId}</div>

      <div className="stage-list">
        {STAGES.map((stage, idx) => {
          const Icon = stage.icon;
          const status = idx < stageIndex ? 'done' : idx === stageIndex ? 'active' : '';
          return (
            <div key={stage.key} className={`stage-item ${status}`}>
              <div className="stage-icon-wrap">
                <Icon size={16} />
              </div>
              {stage.label}
            </div>
          );
        })}
      </div>

      <div className="elapsed">{Math.round(elapsed)}s elapsed</div>

      {error && (
        <div className="loading-error">
          <AlertTriangle size={18} />
          {error}
        </div>
      )}
    </div>
  );
}
