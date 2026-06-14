import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import UploadPage from './pages/UploadPage';
import LoadingPage from './pages/LoadingPage';
import DashboardPage from './pages/DashboardPage';
import './index.css';

function AppHeader() {
  return (
    <header className="app-header">
      <Link to="/" className="brand">
        <div className="brand-mark">$L</div>
        <div>
          <div className="brand-name">SmartLedger</div>
          <div className="brand-tag">AI expense auditor</div>
        </div>
      </Link>
    </header>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <div className="app-shell">
        <AppHeader />
        <main className="app-main">
          <Routes>
            <Route path="/" element={<UploadPage />} />
            <Route path="/loading/:sessionId" element={<LoadingPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}
