import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Download, ArrowLeft, Loader2 } from 'lucide-react';
import SummaryCards from '../components/SummaryCards';
import CategoryPieChart from '../components/CategoryPieChart';
import SpendTrendChart from '../components/SpendTrendChart';
import TransactionTable from '../components/TransactionTable';
import AnomalyPanel from '../components/AnomalyPanel';
import AuditNarrative from '../components/AuditNarrative';
import { downloadPdf } from '../services/api';

export default function DashboardPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [downloading, setDownloading] = useState(false);

  const { report, sessionId } = location.state || {};

  if (!report) {
    return (
      <div className="dashboard-empty">
        <style>{`
          .dashboard-empty {
            max-width: 480px;
            margin: 80px auto;
            text-align: center;
            color: var(--bg-paper);
          }
          .dashboard-empty h2 {
            font-family: var(--font-display);
            font-size: 24px;
            margin-bottom: 12px;
          }
          .dashboard-empty p {
            color: var(--sage);
            margin-bottom: 24px;
          }
        `}</style>
        <h2>No report loaded</h2>
        <p>Upload a statement to generate a new audit report.</p>
        <button className="btn btn-primary" onClick={() => navigate('/')}>
          Go to upload
        </button>
      </div>
    );
  }

  const { summary, categories, dailySpend, transactions, anomalies, narrative, fileName } = report;
  const currency = summary?.currency || 'INR';

  const handleDownload = async () => {
    setDownloading(true);
    try {
      await downloadPdf(sessionId, `smartledger-${sessionId}.pdf`);
    } catch (err) {
      console.error('PDF download failed', err);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="dashboard-page">
      <style>{`
        .dashboard-page {
          max-width: 1200px;
          margin: 0 auto;
          display: grid;
          gap: 24px;
        }
        .dashboard-top {
          display: flex;
          align-items: center;
          justify-content: space-between;
          flex-wrap: wrap;
          gap: 16px;
        }
        .dashboard-heading {
          color: var(--bg-paper);
        }
        .dashboard-heading h1 {
          font-family: var(--font-display);
          font-size: 28px;
          margin: 0 0 4px;
        }
        .dashboard-heading .file-meta {
          font-size: 12.5px;
          color: var(--sage);
          font-family: var(--font-mono);
        }
        .dashboard-actions {
          display: flex;
          gap: 10px;
        }
        .charts-row {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 20px;
        }
        @media (max-width: 860px) {
          .charts-row {
            grid-template-columns: 1fr;
          }
        }
        .lower-row {
          display: grid;
          grid-template-columns: 1.6fr 1fr;
          gap: 20px;
          align-items: start;
        }
        @media (max-width: 980px) {
          .lower-row {
            grid-template-columns: 1fr;
          }
        }
        .spin {
          animation: spin-slow 1s linear infinite;
        }
      `}</style>

      <div className="dashboard-top">
        <div className="dashboard-heading">
          <h1>Audit Report</h1>
          {fileName && <div className="file-meta">source: {fileName} &middot; session {sessionId}</div>}
        </div>
        <div className="dashboard-actions">
          <button className="btn btn-ghost" onClick={() => navigate('/')}>
            <ArrowLeft size={16} />
            New audit
          </button>
          <button className="btn btn-primary" onClick={handleDownload} disabled={downloading}>
            {downloading ? <Loader2 size={16} className="spin" /> : <Download size={16} />}
            {downloading ? 'Preparing PDF\u2026' : 'Download PDF'}
          </button>
        </div>
      </div>

      <SummaryCards summary={summary} />

      <AuditNarrative narrative={narrative} />

      <div className="charts-row">
        <CategoryPieChart categories={categories} currency={currency} />
        <SpendTrendChart dailySpend={dailySpend} currency={currency} />
      </div>

      <div className="lower-row">
        <TransactionTable transactions={transactions} currency={currency} />
        <AnomalyPanel anomalies={anomalies} transactions={transactions} currency={currency} />
      </div>
    </div>
  );
}
