package com.waya.wayapay.model;

/** Known values of {@link WebhookEvent#status()}. */
public enum WebhookStatus {
    /** Status string not recognised by this SDK version. Don't fulfil; reconcile. */
    UNKNOWN,
    /** Customer paid the full amount (or more). Funds queued for settlement — fulfil the order. */
    SUCCESSFUL,
    /** Paid into a virtual account but less than expected. Hold fulfilment; a top-up sends a later SUCCESSFUL. */
    PARTIAL,
    /** Declined, abandoned, or upstream-rejected. Funds never moved — no fulfilment. */
    FAILED;

    /** Parses the raw status string. Returns {@link #UNKNOWN} for unrecognised values. */
    public static WebhookStatus from(String status) {
        if (status == null) return UNKNOWN;
        try {
            return valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
