# Pharos X402 Agent Buyer

An intelligent agent developed in **Java** for the Pharos Agent Arena designed to call x402-paywalled APIs, enforce strict budgets and price caps, log payments with running totals, ensure safe retries without double-counting, and demonstrate both successful completion and budget-limit stops.

## 📋 Requirements Met

1. **Multi-Endpoint Support (Req. 01):** Calls at least 2 different x402 endpoints (`/api/market-data` and `/api/whale-metrics`).
2. **Budget & Price Cap Enforcement (Req. 02):** Enforces a total spend budget and a per-call price cap, refusing calls that exceed either constraint.
3. **Detailed Payment Auditing (Req. 03):** Logs every payment with price, item bought, transaction hash, and running total.
4. **Safe Retries & Idempotency (Req. 04):** Handles payment failures and retries safely, ensuring the exact same payment never counts twice.
5. **Demonstration Scenarios (Req. 05):** Includes runnable demos showing both an agent completing its job successfully and an agent stopping when it hits its budget.

## 🚀 How to Run

1. Make sure you have **Java JDK** installed.
2. Compile the file:
   ```bash
   javac X402AgentBuyer.java
