# SmartLedger

**AI-Powered Personal Finance Auditor**

SmartLedger is a full-stack web application that transforms raw bank statements into actionable financial insights. Users upload CSV or Excel transaction exports from their bank or UPI apps, and SmartLedger automatically categorizes transactions, identifies anomalies, detects duplicates, and generates an AI-powered audit report with plain-English explanations.

Built for individuals and small business owners, SmartLedger provides accountant-style spending analysis without requiring financial expertise or expensive consulting services.

---

## Overview

Managing personal or business finances often involves manually reviewing hundreds of transactions across multiple categories. SmartLedger automates this process by analyzing uploaded transaction data and producing a structured audit report within minutes.

The platform combines rule-based financial analysis with AI-powered reasoning to help users:

* Understand where their money is going
* Identify unusual spending patterns
* Detect duplicate or suspicious transactions
* Discover opportunities to reduce expenses
* Receive easy-to-understand financial insights

---

## Key Features

### Transaction Upload & Processing

* Upload bank statements in **CSV**, **XLSX**, or **XLS** formats
* Automatic parsing and validation of transaction records
* Fast processing of large transaction datasets

### AI-Powered Categorization

* Automatically classifies transactions into categories such as:

  * Food & Dining
  * Shopping
  * Bills & Utilities
  * Transportation
  * Income
  * Entertainment
  * ATM & Cash
  * Others

### Anomaly Detection

Detects unusual financial activity including:

* Abnormally large expenses
* Future-dated transactions
* Suspicious spending patterns
* Outlier transactions compared to historical behavior

### Duplicate Detection

Identifies potential duplicate transactions using:

* Amount matching
* Date comparison
* Description similarity analysis

### Spending Analytics

* Category-wise expenditure breakdown
* Month-over-month spending trends
* Daily spending analysis
* Cash flow summaries

### AI Audit Report

Generates a comprehensive audit report containing:

* Financial health assessment
* Spending insights
* Risk indicators
* Savings opportunities
* Plain-English explanations of findings

### PDF Export

* Download professional audit reports as PDF documents
* Share reports for record keeping or financial review

---

## Tech Stack

### Backend

* Java 21
* Spring Boot 3
* PostgreSQL
* Apache POI
* Claude API

### Frontend

* React
* Vite
* TypeScript

### DevOps

* Docker
* Docker Compose

---

## Architecture

```text
SmartLedger
│
├── smartLedgerFrontend
│   ├── React + Vite UI
│   ├── File Upload Interface
│   ├── Dashboard & Charts
│   └── Audit Report Viewer
│
└── smartLedgerBackend
    ├── Spring Boot REST APIs
    ├── File Processing Module
    ├── Transaction Categorization
    ├── Anomaly Detection Engine
    ├── Claude AI Integration
    ├── PDF Report Generator
    └── PostgreSQL Database
```

---

## Workflow

```text
Upload CSV/XLSX
        │
        ▼
Parse Transactions
        │
        ▼
Auto Categorization
        │
        ▼
Anomaly & Duplicate Detection
        │
        ▼
AI Financial Analysis
        │
        ▼
Audit Dashboard
        │
        ▼
PDF Audit Report
```

---

## Sample Insights Generated

* "Dining expenses increased by 32% compared to last month."
* "A transaction of ₹15,000 appears unusually large relative to your normal spending behavior."
* "Two transactions may be duplicates based on amount and timestamp similarity."
* "Reducing food delivery expenses by 20% could save approximately ₹3,000 per month."

---

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/<your-username>/smart-ledger.git
cd smart-ledger
```

### Backend Setup

```bash
cd smartLedgerBackend
./mvnw spring-boot:run
```

### Frontend Setup

```bash
cd smartLedgerFrontend
npm install
npm run dev
```

---

## Future Enhancements

* Multi-bank statement support
* Recurring subscription detection
* Budget planning recommendations
* Expense forecasting
* AI-powered financial assistant
* Multi-user authentication and dashboards

---

## Target Users

### Individuals

People who want a clear understanding of their spending habits and financial health.

### Freelancers

Professionals who need quick transaction categorization and expense analysis.

### Small Business Owners

Business owners seeking affordable financial insights without hiring a dedicated accountant.

---

## License

This project is developed for educational and portfolio purposes.
