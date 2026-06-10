package com.waya.wayapay.model;

/**
 * Known values of {@link CollectionStatusResponse#status()}.
 * Parse a raw status string with {@link #from(String)}; map to a merchant action with
 * {@link #outcome()}; check whether it can still change with {@link #isTerminal()}.
 */
public enum CollectionStatus {
    /** Status string not recognised by this SDK version. Treat as non-terminal and reconcile. */
    UNKNOWN,

    // ----- In flight (non-terminal): keep polling; don't refund or retry -----
    INITIATED,
    PENDING,
    PROCESSING,
    APPROVED,
    /** Customer underpaid into a virtual account. Non-terminal. */
    PARTIAL,

    // ----- Terminal: funds confirmed -----
    /** Funds confirmed — fulfil. Use refNo for idempotency. */
    SUCCESSFUL,
    /** Previously-successful transaction refunded. */
    REFUNDED,

    // ----- Terminal: customer not debited — no fulfilment -----
    FAILED,
    DECLINED,
    REJECTED,
    ABANDONED,
    EXPIRED,
    CANCELLED,
    CUSTOMER_ERROR,
    FRAUD_ERROR,

    // ----- Terminal: outcome unknown — reconcile, don't refund unilaterally -----
    TIMEOUT,
    ERROR,
    SYSTEM_ERROR,
    BANK_ERROR;

    /** Parses the raw status string. Returns {@link #UNKNOWN} for unrecognised values. */
    public static CollectionStatus from(String status) {
        if (status == null) return UNKNOWN;
        try {
            return valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /**
     * Maps this status to the action a merchant should take.
     * {@link #UNKNOWN} maps to {@link CollectionOutcome#INDETERMINATE} — reconcile rather than guess.
     */
    public CollectionOutcome outcome() {
        return switch (this) {
            case INITIATED, PENDING, PROCESSING, APPROVED, PARTIAL -> CollectionOutcome.IN_FLIGHT;
            case SUCCESSFUL -> CollectionOutcome.SUCCEEDED;
            case REFUNDED -> CollectionOutcome.REFUNDED;
            case FAILED, DECLINED, REJECTED, ABANDONED, EXPIRED, CANCELLED, CUSTOMER_ERROR, FRAUD_ERROR ->
                    CollectionOutcome.NOT_DEBITED;
            // TIMEOUT / ERROR / SYSTEM_ERROR / BANK_ERROR / UNKNOWN
            default -> CollectionOutcome.INDETERMINATE;
        };
    }

    /** True once the status will no longer change. Non-terminal statuses should be polled. */
    public boolean isTerminal() {
        return switch (this) {
            case INITIATED, PENDING, PROCESSING, APPROVED, PARTIAL, UNKNOWN -> false;
            default -> true;
        };
    }
}
