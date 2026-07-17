package com.waya.wayaquick.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from {@code POST /payment-payout/initiate}. {@code PROCESSING} means accepted, not
 * settled — confirm via webhook or status check before treating as delivered.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PayoutResponse(
        /** WayaQuick's internal payout reference. Use this to track and confirm the payout. */
        String payoutReference,
        /** Echoes back your unique reference supplied in the request. */
        String merchantReference,
        /** Payout status, e.g. "PROCESSING". Does not mean settled. */
        String status,
        /** Human-readable status message. */
        String message
) {
}
