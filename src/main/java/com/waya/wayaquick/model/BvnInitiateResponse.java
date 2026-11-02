package com.waya.wayaquick.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from {@code POST /identity-verification/bvn/initiate}.
 *
 * @param reference   Session reference to provide in {@code completeBvn}.
 * @param phoneNumber Masked phone number receiving the OTP (e.g. "234****0000").
 * @param expiresAt   Session expiration timestamp.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BvnInitiateResponse(
        String reference,
        String phoneNumber,
        String expiresAt
) {
}

