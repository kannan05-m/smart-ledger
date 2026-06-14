import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const client = axios.create({
  baseURL: API_BASE_URL,
});

/**
 * Uploads a CSV/XLSX bank export.
 * Backend: POST /api/upload (multipart/form-data)
 * Returns: { sessionId, fileName, transactionCount, status }
 */
export async function uploadFile(file, onUploadProgress) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await client.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (event) => {
      if (onUploadProgress && event.total) {
        const percent = Math.round((event.loaded * 100) / event.total);
        onUploadProgress(percent);
      }
    },
  });

  return response.data;
}

/**
 * Polls the audit status/result for a session.
 * Backend: GET /api/audit/{sessionId}
 * Returns: { status: 'PENDING' | 'PROCESSING' | 'READY' | 'FAILED', report?: AuditReportDto, stage?, error? }
 */
export async function pollAudit(sessionId) {
  const response = await client.get(`/audit/${sessionId}`);
  return response.data;
}

/**
 * Downloads the generated PDF audit report.
 * Backend: GET /api/report/{sessionId}/pdf
 * Triggers a browser file download.
 */
export async function downloadPdf(sessionId, fileName = 'smartledger-audit-report.pdf') {
  const response = await client.get(`/report/${sessionId}/pdf`, {
    responseType: 'blob',
  });

  const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export default client;
