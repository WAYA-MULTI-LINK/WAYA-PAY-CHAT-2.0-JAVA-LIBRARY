package com.waya.wayaquick.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from {@code POST /payment-collect/initiate}.
 * Redirect the customer to {@link #checkOutUrl()} to complete payment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CollectionResponse(
        /** Echoes back your transactionId. */
        String uniqueId,
        /** WayaQuick's internal transaction ID. */
        String transactionId,
        String email,
        /** Quoted string, e.g. "1500.00". */
        String amount,
        /** Redirect the customer here to complete payment. */
        String checkOutUrl,
        /** Merchant's unique identifier. */
        String merchantId
) {
}
