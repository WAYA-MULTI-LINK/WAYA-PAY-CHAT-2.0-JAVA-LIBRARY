package com.waya.wayaquick.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Status of a single payout (disbursement) transaction.
 * Use {@link #transactionReference()} as the idempotency key.
 * Inspect {@link #status()} via {@link #parsedStatus()} to decide whether to keep reconciling,
 * treat as delivered, or treat as failed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PayoutStatusResponse(
        /** Your unique reference, e.g. "PAYOUT-20260604-001". Stable idempotency key. */
        String transactionReference,
        /** Raw payout status, e.g. "SUCCESS". Parse with {@link #parsedStatus()}. */
        String status,
        /** Amount disbursed, quoted string, e.g. "500.00". */
        String amount,
        /** Destination NUBAN account number, e.g. "0123456789". */
        String destinationAccountNumber,
        /** Destination account name, e.g. "JOHN DOE". */
        String destinationAccountName,
        /** Destination bank name, e.g. "GTBank". */
        String destinationBankName,
        /** Bank narration / transfer description. */
        String narration,
        /** Creation timestamp, e.g. "2026-06-04T10:00:32". */
        String createdAt
) {
    /** Parses {@link #status()} into a {@link PayoutStatus}. */
    public PayoutStatus parsedStatus() {
        return PayoutStatus.from(status);
    }
}
