package com.waya.wayapay.model;

/**
 * Known values of {@link PayoutStatusResponse#status()}.
 * Parse a raw status string with {@link #from(String)}; map to a merchant action with
 * {@link #outcome()}; check whether it can still change with {@link #isTerminal()}.
 */
public enum PayoutStatus {
    /** Status string not recognised by this SDK version. Treat as non-terminal and reconcile. */
    UNKNOWN,
    /** Submitted; terminal outcome not yet recorded (reconciling). Non-terminal. */
    PENDING,
    /** Completed successfully. */
    SUCCESS,
    /** Failed/reversed — the merchant wallet was re-credited. */
    REVERSED;

    /** Parses the raw status string. Returns {@link #UNKNOWN} for unrecognised values. */
    public static PayoutStatus from(String status) {
        if (status == null) return UNKNOWN;
        try {
            return valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /**
     * Maps this status to the action a merchant should take.
     * {@link #UNKNOWN} maps to {@link PayoutOutcome#RECONCILING} — reconcile rather than guess.
     */
    public PayoutOutcome outcome() {
        return switch (this) {
            case SUCCESS -> PayoutOutcome.SUCCEEDED;
            case REVERSED -> PayoutOutcome.REVERSED;
            // PENDING / UNKNOWN
            default -> PayoutOutcome.RECONCILING;
        };
    }

    /** True once the status will no longer change. Non-terminal statuses should be reconciled. */
    public boolean isTerminal() {
        return switch (this) {
            case PENDING, UNKNOWN -> false;
            default -> true;
        };
    }
}
