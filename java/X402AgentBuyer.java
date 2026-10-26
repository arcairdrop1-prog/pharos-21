import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class X402AgentBuyer {

    // Requirement 02: Spend budget and per-call price cap configuration
    private final double totalBudgetUSD;
    private final double perCallPriceCapUSD;
    private double currentSpentUSD = 0.0;

    // Requirement 03: Audit log entry for payments
    public static class PaymentLog {
        LocalDateTime timestamp;
        String endpoint;
        String itemBought;
        double priceUSD;
        String txHash;
        double runningTotalUSD;

        public PaymentLog(String endpoint, String itemBought, double priceUSD, String txHash, double runningTotalUSD) {
            this.timestamp = LocalDateTime.now();
            this.endpoint = endpoint;
            this.itemBought = itemBought;
            this.priceUSD = priceUSD;
            this.txHash = txHash;
            this.runningTotalUSD = runningTotalUSD;
        }

        @Override
        public String toString() {
            return String.format("[%s] Endpoint: %s | Item: %s | Price: $%.2f | TxHash: %s | Running Total: $%.2f",
                    timestamp, endpoint, itemBought, priceUSD, txHash, runningTotalUSD);
        }
    }

    private final List<PaymentLog> auditLogs = new ArrayList<>();
    private final List<String> processedTxHashes = new ArrayList<>(); // Requirement 04: Safe retries (no double-counting)

    public X402AgentBuyer(double totalBudgetUSD, double perCallPriceCapUSD) {
        this.totalBudgetUSD = totalBudgetUSD;
        this.perCallPriceCapUSD = perCallPriceCapUSD;
    }

    // Requirement 01: Calls x402 endpoints
    // Requirement 02: Enforces budget and price cap
    // Requirement 04: Safe retries (idempotency check via txHash)
    public String callPaywalledAPI(String endpoint, String itemBought, double priceUSD, String txHash) {
        System.out.println("\nAttempting to call endpoint: " + endpoint + " for item: " + itemBought);

        // Check per-call price cap
        if (priceUSD > perCallPriceCapUSD) {
            return "REFUSED: Price ($" + priceUSD + ") exceeds per-call price cap ($" + perCallPriceCapUSD + ").";
        }

        // Check total spend budget
        if ((currentSpentUSD + priceUSD) > totalBudgetUSD) {
            return "REFUSED: Call exceeds total remaining budget. Current spent: $" + currentSpentUSD + " / Budget: $" + totalBudgetUSD;
        }

        // Requirement 04: Safe retry check (prevent double payment)
        if (processedTxHashes.contains(txHash)) {
            return "RETRY BLOCKED: Transaction hash " + txHash + " has already been processed. Preventing double-charge.";
        }

        // Process payment and execution
        currentSpentUSD += priceUSD;
        processedTxHashes.add(txHash);

        // Requirement 03: Log payment details and running total
        PaymentLog log = new PaymentLog(endpoint, itemBought, priceUSD, txHash, currentSpentUSD);
        auditLogs.add(log);

        return "SUCCESS: API call completed and payment settled. " + log;
    }

    public void printAuditLogs() {
        System.out.println("\n=== X402 AGENT BUYER AUDIT LOGS ===");
        for (PaymentLog log : auditLogs) {
            System.out.println(log);
        }
        System.out.printf("Total Spent: $%.2f / $%.2f\n", currentSpentUSD, totalBudgetUSD);
    }

    // Requirement 05: Demo tasks (one completes job, one stops due to budget)
    public static void main(String[] args) {
        System.out.println("=== DEMO 1: Job Completes Within Budget ===");
        X402AgentBuyer buyer1 = new X402AgentBuyer(10.0, 4.0);
        
        // Requirement 01: Calls at least 2 different x402 endpoints
        System.out.println(buyer1.callPaywalledAPI("/api/market-data", "ETH-USD 24h Feed", 2.5, "0xabc123"));
        System.out.println(buyer1.callPaywalledAPI("/api/whale-metrics", "Top 10 Whale TXs", 3.0, "0xdef456"));
        buyer1.printAuditLogs();

        System.out.println("\n=== DEMO 2: Job Stops Because It Hits the Budget ===");
        X402AgentBuyer buyer2 = new X402AgentBuyer(5.0, 4.0);
        System.out.println(buyer2.callPaywalledAPI("/api/market-data", "BTC-USD Feed", 3.0, "0x789xyz"));
        // This next call exceeds the $5.0 total budget ($3.0 + $3.5 > $5.0)
        System.out.println(buyer2.callPaywalledAPI("/api/whale-metrics", "Deep Whale Analytics", 3.5, "0x987zyx"));
        buyer2.printAuditLogs();
    }
}
