# SmartLedger Backend

Spring Boot 3 and Java 21 backend for SmartLedger, an AI expense auditor that parses CSV/XLSX bank exports, runs rule-based anomaly checks, uses Claude for batch transaction categorization and plain-English audit summaries, and exports PDF audit reports.

## Run

```bash
mvn spring-boot:run
```

Optional Claude configuration:

```bash
export CLAUDE_API_KEY=your_api_key
export CLAUDE_MODEL=claude-sonnet-4-6
```

Without `CLAUDE_API_KEY`, the app uses local fallback categorization and summaries so development still works.

## API

Upload a bank export:

```bash
curl -F "file=@transactions.csv" http://localhost:8080/api/upload
```

Response:

```json
{
  "sessionId": "uuid",
  "status": "PROCESSING",
  "transactionCount": 87,
  "anomalyCount": 0,
  "message": "Upload parsed. Audit processing has started."
}
```

Fetch the audit JSON:

```bash
curl http://localhost:8080/api/audit/{sessionId}
```

Poll this endpoint until `status` is `READY`. If processing fails, the response returns `status: "FAILED"` with `errorMessage`.

Download the PDF:

```bash
curl -o audit.pdf http://localhost:8080/api/report/{sessionId}/pdf
```

Expected CSV/XLSX headers can include `date`, `description`, `amount`, `type`, `debit`, `credit`, `transaction date`, `posted date`, `memo`, `narration`, `time`, `transaction time`, or `timestamp`.

## Docker

```bash
docker compose up --build
```

## MVP Flow

1. `POST /api/upload` parses the file and creates an in-memory `sessionId`.
2. SmartLedger runs duplicate, late-night, weekend-spike, category-average, and subscription-creep anomaly checks.
3. Claude categorizes transactions in batches of 50.
4. Claude receives aggregate stats and returns a structured audit summary.
5. React polls `GET /api/audit/{sessionId}` until the report is ready.
