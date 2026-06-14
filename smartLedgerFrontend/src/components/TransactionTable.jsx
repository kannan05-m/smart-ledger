import { useMemo, useState } from 'react';
import { ArrowUpDown, Search, AlertTriangle } from 'lucide-react';

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

const CATEGORY_COLORS = {
  Food: '#D4A857',
  Groceries: '#D4A857',
  Transport: '#7A8C85',
  Travel: '#7A8C85',
  Shopping: '#B8895A',
  Bills: '#C0392B',
  Utilities: '#C0392B',
  Income: '#1F4D3D',
  Salary: '#1F4D3D',
  Transfer: '#4A6E63',
  Entertainment: '#8E6C3A',
  Subscriptions: '#8E6C3A',
  Other: '#9A9A9A',
};

function categoryColor(category) {
  return CATEGORY_COLORS[category] || '#9A9A9A';
}

const COLUMNS = [
  { key: 'date', label: 'Date' },
  { key: 'description', label: 'Description' },
  { key: 'category', label: 'Category' },
  { key: 'type', label: 'Type' },
  { key: 'amount', label: 'Amount' },
];

export default function TransactionTable({ transactions, currency = 'INR' }) {
  const [sortKey, setSortKey] = useState('date');
  const [sortDir, setSortDir] = useState('desc');
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('All');

  const categories = useMemo(() => {
    const set = new Set((transactions || []).map((t) => t.category).filter(Boolean));
    return ['All', ...Array.from(set).sort()];
  }, [transactions]);

  const filtered = useMemo(() => {
    let rows = transactions || [];

    if (categoryFilter !== 'All') {
      rows = rows.filter((t) => t.category === categoryFilter);
    }

    if (search.trim()) {
      const q = search.trim().toLowerCase();
      rows = rows.filter((t) => t.description?.toLowerCase().includes(q));
    }

    const sorted = [...rows].sort((a, b) => {
      let av = a[sortKey];
      let bv = b[sortKey];

      if (sortKey === 'date') {
        av = new Date(av).getTime();
        bv = new Date(bv).getTime();
      } else if (sortKey === 'amount') {
        av = Number(av);
        bv = Number(bv);
      } else {
        av = (av || '').toString().toLowerCase();
        bv = (bv || '').toString().toLowerCase();
      }

      if (av < bv) return sortDir === 'asc' ? -1 : 1;
      if (av > bv) return sortDir === 'asc' ? 1 : -1;
      return 0;
    });

    return sorted;
  }, [transactions, search, categoryFilter, sortKey, sortDir]);

  const toggleSort = (key) => {
    if (sortKey === key) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortDir('desc');
    }
  };

  return (
    <div className="paper-card txn-table-card">
      <style>{`
        .txn-table-card {
          padding: 24px;
        }
        .txn-header {
          display: flex;
          flex-wrap: wrap;
          gap: 12px;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 18px;
        }
        .txn-title {
          font-family: var(--font-display);
          font-size: 18px;
          margin: 0;
          color: var(--ink);
        }
        .txn-controls {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }
        .search-box {
          display: flex;
          align-items: center;
          gap: 8px;
          background: var(--bg-paper-dim);
          border-radius: var(--radius);
          padding: 8px 12px;
          font-size: 13px;
          color: var(--sage);
        }
        .search-box input {
          border: none;
          background: transparent;
          outline: none;
          font-size: 13px;
          font-family: var(--font-body);
          color: var(--ink);
          width: 160px;
        }
        .category-select {
          border: none;
          background: var(--bg-paper-dim);
          border-radius: var(--radius);
          padding: 8px 12px;
          font-size: 13px;
          font-family: var(--font-body);
          color: var(--ink);
        }
        .txn-table-wrap {
          overflow-x: auto;
        }
        table.txn-table {
          width: 100%;
          border-collapse: collapse;
          font-size: 13.5px;
        }
        table.txn-table th {
          text-align: left;
          padding: 10px 12px;
          font-size: 11px;
          text-transform: uppercase;
          letter-spacing: 0.06em;
          color: var(--sage);
          border-bottom: 2px solid var(--bg-paper-dim);
          cursor: pointer;
          white-space: nowrap;
          user-select: none;
        }
        table.txn-table th .th-inner {
          display: flex;
          align-items: center;
          gap: 4px;
        }
        table.txn-table td {
          padding: 11px 12px;
          border-bottom: 1px solid var(--bg-paper-dim);
          color: var(--ink);
          vertical-align: middle;
        }
        table.txn-table tr:hover td {
          background: rgba(31, 77, 61, 0.04);
        }
        table.txn-table tr.flagged td {
          background: rgba(192, 57, 43, 0.05);
        }
        .desc-cell {
          display: flex;
          align-items: center;
          gap: 8px;
          max-width: 320px;
        }
        .desc-text {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .category-badge {
          display: inline-flex;
          align-items: center;
          font-size: 11.5px;
          font-weight: 600;
          padding: 4px 10px;
          border-radius: 12px;
          color: var(--bg-paper);
          white-space: nowrap;
        }
        .type-badge {
          font-family: var(--font-mono);
          font-size: 11px;
          font-weight: 600;
          padding: 3px 8px;
          border-radius: 4px;
        }
        .type-badge.CR {
          background: rgba(31, 77, 61, 0.12);
          color: var(--green-forest);
        }
        .type-badge.DR {
          background: rgba(192, 57, 43, 0.1);
          color: var(--red-flag);
        }
        .amount-cell {
          font-family: var(--font-mono);
          text-align: right;
          font-weight: 600;
        }
        .amount-cell.CR {
          color: var(--green-forest);
        }
        .amount-cell.DR {
          color: var(--red-flag);
        }
        .no-results {
          text-align: center;
          color: var(--sage);
          padding: 32px;
          font-size: 14px;
        }
        .row-count {
          font-size: 12px;
          color: var(--sage);
          font-family: var(--font-mono);
          margin-top: 12px;
        }
      `}</style>

      <div className="txn-header">
        <h3 className="txn-title">Transactions</h3>
        <div className="txn-controls">
          <div className="search-box">
            <Search size={14} />
            <input
              placeholder="Search description..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <select
            className="category-select"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
          >
            {categories.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="txn-table-wrap">
        <table className="txn-table">
          <thead>
            <tr>
              {COLUMNS.map((col) => (
                <th
                  key={col.key}
                  onClick={() => toggleSort(col.key)}
                  style={col.key === 'amount' ? { textAlign: 'right' } : undefined}
                >
                  <div className="th-inner" style={col.key === 'amount' ? { justifyContent: 'flex-end' } : undefined}>
                    {col.label}
                    <ArrowUpDown size={11} style={{ opacity: sortKey === col.key ? 1 : 0.3 }} />
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filtered.map((txn) => (
              <tr key={txn.id} className={txn.flagged ? 'flagged' : ''}>
                <td style={{ whiteSpace: 'nowrap', fontFamily: 'var(--font-mono)', fontSize: 12.5 }}>
                  {formatDate(txn.date)}
                </td>
                <td>
                  <div className="desc-cell">
                    {txn.flagged && <AlertTriangle size={13} color="var(--red-flag)" />}
                    <span className="desc-text">{txn.description}</span>
                  </div>
                </td>
                <td>
                  <span className="category-badge" style={{ background: categoryColor(txn.category) }}>
                    {txn.category || 'Uncategorized'}
                  </span>
                </td>
                <td>
                  <span className={`type-badge ${txn.type}`}>{txn.type}</span>
                </td>
                <td className={`amount-cell ${txn.type}`}>
                  {txn.type === 'DR' ? '-' : '+'}
                  {formatCurrency(Math.abs(txn.amount), currency)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {filtered.length === 0 && <div className="no-results">No transactions match your filters.</div>}
      </div>

      <div className="row-count">
        showing {filtered.length} of {(transactions || []).length} transactions
      </div>
    </div>
  );
}
