import { useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { UploadCloud, FileSpreadsheet, FileText, AlertCircle } from 'lucide-react';
import { uploadFile } from '../services/api';

const ACCEPTED_TYPES = ['.csv', '.xlsx', '.xls'];

export default function UploadPage() {
  const [isDragging, setIsDragging] = useState(false);
  const [file, setFile] = useState(null);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [progress, setProgress] = useState(0);
  const inputRef = useRef(null);
  const navigate = useNavigate();

  const validateAndSetFile = (candidate) => {
    if (!candidate) return;
    const ext = '.' + candidate.name.split('.').pop().toLowerCase();
    if (!ACCEPTED_TYPES.includes(ext)) {
      setError(`Unsupported file type "${ext}". Upload a .csv, .xlsx, or .xls bank export.`);
      setFile(null);
      return;
    }
    setError(null);
    setFile(candidate);
  };

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    setIsDragging(false);
    const dropped = e.dataTransfer.files?.[0];
    validateAndSetFile(dropped);
  }, []);

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = () => setIsDragging(false);

  const handleBrowse = (e) => {
    validateAndSetFile(e.target.files?.[0]);
  };

  const handleSubmit = async () => {
    if (!file) return;
    setSubmitting(true);
    setError(null);
    setProgress(0);
    try {
      const response = await uploadFile(file, setProgress);
      navigate(`/loading/${response.sessionId}`);
    } catch (err) {
      setSubmitting(false);
      setProgress(0);
      const message =
        err.response?.data?.message ||
        'Upload failed. Check that the backend is running and the file format is supported.';
      setError(message);
    }
  };

  return (
    <div className="upload-page">
      <style>{`
        .upload-page {
          max-width: 880px;
          margin: 0 auto;
          display: grid;
          gap: 28px;
        }
        .upload-hero {
          color: var(--bg-paper);
        }
        .upload-hero h1 {
          font-family: var(--font-display);
          font-size: clamp(32px, 5vw, 48px);
          font-weight: 600;
          margin: 0 0 10px;
          letter-spacing: -0.01em;
        }
        .upload-hero p {
          color: var(--sage);
          font-size: 16px;
          margin: 0;
          max-width: 520px;
          line-height: 1.6;
        }
        .dropzone {
          padding: 56px 32px;
          text-align: center;
          border: 2px dashed var(--bg-paper-dim);
          transition: border-color 0.15s ease, background 0.15s ease;
        }
        .dropzone.dragging {
          border-color: var(--gold);
          background: var(--bg-paper-dim);
        }
        .dropzone-icon {
          width: 56px;
          height: 56px;
          border-radius: 50%;
          background: var(--green-forest);
          color: var(--bg-paper);
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 0 auto 18px;
        }
        .dropzone h3 {
          font-family: var(--font-display);
          font-size: 22px;
          margin: 0 0 6px;
          color: var(--ink);
        }
        .dropzone p {
          color: var(--sage);
          font-size: 14px;
          margin: 0 0 18px;
        }
        .file-pill {
          display: inline-flex;
          align-items: center;
          gap: 10px;
          background: var(--bg-paper-dim);
          border-radius: var(--radius);
          padding: 10px 16px;
          font-family: var(--font-mono);
          font-size: 13px;
          color: var(--ink);
          margin-bottom: 18px;
        }
        .error-banner {
          display: flex;
          align-items: flex-start;
          gap: 10px;
          background: var(--red-soft);
          color: var(--red-flag);
          border-radius: var(--radius);
          padding: 12px 16px;
          font-size: 13.5px;
          margin-bottom: 16px;
          text-align: left;
        }
        .progress-track {
          height: 6px;
          background: var(--bg-paper-dim);
          border-radius: 4px;
          overflow: hidden;
          margin-bottom: 16px;
        }
        .progress-fill {
          height: 100%;
          background: var(--gold);
          transition: width 0.2s ease;
        }
        .format-guide {
          padding: 28px 32px;
        }
        .format-guide h4 {
          font-family: var(--font-display);
          font-size: 18px;
          margin: 0 0 16px;
          color: var(--ink);
        }
        .format-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
          gap: 16px;
        }
        .format-item {
          display: flex;
          gap: 12px;
          align-items: flex-start;
        }
        .format-item-icon {
          flex-shrink: 0;
          color: var(--green-forest);
        }
        .format-item h5 {
          margin: 0 0 4px;
          font-size: 14px;
          font-weight: 600;
          color: var(--ink);
        }
        .format-item p {
          margin: 0;
          font-size: 13px;
          color: var(--sage);
          line-height: 1.5;
        }
        .format-cols {
          font-family: var(--font-mono);
          font-size: 12px;
          background: var(--bg-paper-dim);
          padding: 2px 6px;
          border-radius: 4px;
          color: var(--ink);
        }
      `}</style>

      <div className="upload-hero">
        <h1>Drop in your statement.<br />Walk away with an audit.</h1>
        <p>
          SmartLedger reads your bank export, categorizes every transaction,
          flags anomalies, and writes a plain-English report &mdash; in minutes.
        </p>
      </div>

      <div
        className={`paper-card dropzone ${isDragging ? 'dragging' : ''}`}
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
      >
        <div className="dropzone-icon">
          <UploadCloud size={26} />
        </div>

        {!file && (
          <>
            <h3>Drag your CSV or XLSX here</h3>
            <p>or click below to browse your files</p>
          </>
        )}

        {file && (
          <div className="file-pill">
            <FileSpreadsheet size={16} />
            {file.name} &middot; {(file.size / 1024).toFixed(1)} KB
          </div>
        )}

        {error && (
          <div className="error-banner">
            <AlertCircle size={18} style={{ flexShrink: 0, marginTop: 1 }} />
            <span>{error}</span>
          </div>
        )}

        {submitting && (
          <div className="progress-track">
            <div className="progress-fill" style={{ width: `${progress}%` }} />
          </div>
        )}

        <input
          ref={inputRef}
          type="file"
          accept={ACCEPTED_TYPES.join(',')}
          onChange={handleBrowse}
          style={{ display: 'none' }}
        />

        <div style={{ display: 'flex', gap: 12, justifyContent: 'center', flexWrap: 'wrap' }}>
          <button className="btn btn-dark" onClick={() => inputRef.current?.click()} disabled={submitting}>
            {file ? 'Choose a different file' : 'Browse files'}
          </button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={!file || submitting}>
            {submitting ? `Uploading\u2026 ${progress}%` : 'Run audit'}
          </button>
        </div>
      </div>

      <div className="paper-card format-guide">
        <h4>Supported formats</h4>
        <div className="format-grid">
          <div className="format-item">
            <FileSpreadsheet size={20} className="format-item-icon" />
            <div>
              <h5>.xlsx / .xls</h5>
              <p>Excel exports from most banking portals, parsed via Apache POI.</p>
            </div>
          </div>
          <div className="format-item">
            <FileText size={20} className="format-item-icon" />
            <div>
              <h5>.csv</h5>
              <p>Comma-separated statements, parsed via OpenCSV.</p>
            </div>
          </div>
        </div>
        <div style={{ marginTop: 18, fontSize: 13, color: 'var(--sage)', lineHeight: 1.6 }}>
          Expected columns: <span className="format-cols">date</span>,{' '}
          <span className="format-cols">description</span>,{' '}
          <span className="format-cols">amount</span>,{' '}
          <span className="format-cols">type (CR/DR)</span>. Extra columns are ignored.
        </div>
      </div>
    </div>
  );
}
