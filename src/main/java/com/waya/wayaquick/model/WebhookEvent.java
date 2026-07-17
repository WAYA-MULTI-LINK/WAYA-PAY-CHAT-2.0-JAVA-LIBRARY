package com.waya.wayapay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * A transaction webhook delivered by WayaPay when a payment becomes SUCCESSFUL, PARTIAL, or FAILED.
 * Construct one only via {@link com.waya.wayapay.WayaPayWebhook#constructEvent}, which verifies the
 * signature first. Use {@link #orderId()} as your idempotency key — the same OrderId may fire more
 * than once (e.g. a PARTIAL followed by a SUCCESSFUL).
 *
 * <p>The wire contract mixes casing: the first fields are PascalCase (OrderId, Amount, …) while newer
 * fields are camelCase (customer, merchantId, …). Deserialization is case-insensitive, so both bind.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookEvent(
        /** The transaction reference (refNo). Use this as your idempotency key. */
        String orderId,
        /** Amount the customer was charged. */
        BigDecimal amount,
        /** The description supplied at checkout. */
        String description,
        /** Processing fee deducted. Net to merchant = {@link #amount()} minus {@link #fee()}. */
        BigDecimal fee,
        /** ISO currency code. Always "NGN" today. */
        String currency,
        /** Raw status: "SUCCESSFUL", "PARTIAL", or "FAILED". Parse with {@link #parsedStatus()}. */
        String status,
        /** Transaction time on the gateway, ISO-8601 local, e.g. "2026-06-07T14:30:12". */
        String tranTime,
        /** Same instant, formatted "yyyy-MM-dd HH:mm:ss". */
        String transactionDate,
        /** Channel: "CARD", "WALLET", "USSD", "BANK", "PAYATTITUDE". */
        String productName,
        /** Your business name as registered on WayaPay. */
        String businessName,
        /** The paying customer's details. */
        WebhookCustomer customer,
        /** Your merchant ID. Same value for every webhook to your account. */
        String merchantId,
        /** The branch tag if you've configured one; otherwise null. */
        String branchCategory,
        /** True for charges driven by a subscription / saved card. */
        boolean recurrentPayment
) {
    /** Parses {@link #status()} into a {@link WebhookStatus}. */
    public WebhookStatus parsedStatus() {
        return WebhookStatus.from(status);
    }

    /**
     * True only when the customer paid in full — safe to fulfil the order
     * (after an idempotency check on {@link #orderId()}).
     */
    public boolean shouldFulfil() {
        return parsedStatus() == WebhookStatus.SUCCESSFUL;
    }
}
