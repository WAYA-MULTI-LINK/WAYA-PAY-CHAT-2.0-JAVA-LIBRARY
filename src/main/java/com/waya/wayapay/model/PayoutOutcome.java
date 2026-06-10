package com.waya.wayapay.model;

/** How a {@link PayoutStatus} should be acted on. */
public enum PayoutOutcome {
    /** Submitted; terminal outcome not yet recorded — keep reconciling. */
    RECONCILING,
    /** Completed successfully — funds delivered. */
    SUCCEEDED,
    /** Failed/reversed — the merchant wallet was re-credited. */
    REVERSED,
}
