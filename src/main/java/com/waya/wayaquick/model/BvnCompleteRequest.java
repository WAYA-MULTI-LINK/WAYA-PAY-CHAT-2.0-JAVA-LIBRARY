package com.waya.wayaquick.model;

/**
 * Request body for {@code POST /identity-verification/bvn/complete}.
 *
 * @param reference Session reference returned by initiate.
 * @param otp       OTP received by the BVN holder.
 */
public record BvnCompleteRequest(
        String reference,
        String otp
) {
}

