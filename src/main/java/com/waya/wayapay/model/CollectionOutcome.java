package com.waya.wayapay.model;

/** How a {@link CollectionStatus} should be acted on. */
public enum CollectionOutcome {
    /** In flight — keep polling; don't refund or retry. */
    IN_FLIGHT,
    /** Funds confirmed — fulfil the order. */
    SUCCEEDED,
    /** Previously-successful transaction was refunded. */
    REFUNDED,
    /** Customer not debited — do not fulfil. */
    NOT_DEBITED,
    /** Outcome unknown — reconcile; don't refund unilaterally. */
    INDETERMINATE,
}
