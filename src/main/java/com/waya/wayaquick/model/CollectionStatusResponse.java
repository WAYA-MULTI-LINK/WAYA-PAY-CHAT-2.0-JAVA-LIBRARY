package com.waya.wayaquick.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Status of a single collection (deposit) transaction.
 * Use {@link #refNo()} as the idempotency key when fulfilling a SUCCESSFUL payment.
 * Inspect {@link #status()} via {@link #parsedStatus()} to decide whether to keep polling,
 * fulfil, or reconcile.
 *
 * <p>{@link #amount()} is the expected amount; {@link #amountPaid()} is what was actually
 * received — it can be smaller (PARTIAL underpayment) or larger (overpayment). Use
 * {@code status} + {@code amountPaid} as authoritative.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CollectionStatusResponse(
        /** Provider reference number. Stable idempotency key, e.g. "1779662251460508970". */
        String refNo,
        /** WayaQuick's internal transaction ID (GUID). */
        String tranId,
        /** Merchant's unique identifier. */
        String merchantId,
        /** Amount requested, quoted string, e.g. "1500.00". */
        String amount,
        /** Customer email address. */
        String customerEmail,
        /** Amount actually paid, quoted string. May be less than {@link #amount()} when PARTIAL. */
        String amountPaid,
        /** Processing fee, quoted string, e.g. "15.00". */
        String fee,
        /** ISO currency code, e.g. "NGN". */
        String currencyCode,
        /** Raw transaction status, e.g. "SUCCESSFUL". Parse with {@link #parsedStatus()}. */
        String status,
        /** Settlement status, e.g. "PENDING". */
        String settlementStatus,
        /** Payment channel, e.g. "CARD". */
        String channel,
        /** Processor that handled the transaction, e.g. "ISW". */
        String processedBy,
        /** Merchant-supplied description, e.g. "Order #4523". */
        String description,
        /** Environment the transaction ran in, e.g. "LIVE" or "TEST". */
        String environment,
        /** Transaction timestamp, e.g. "2026-06-04T10:00:32". */
        String tranDate
) {
    /** Parses {@link #status()} into a {@link CollectionStatus}. */
    public CollectionStatus parsedStatus() {
        return CollectionStatus.from(status);
    }
}
