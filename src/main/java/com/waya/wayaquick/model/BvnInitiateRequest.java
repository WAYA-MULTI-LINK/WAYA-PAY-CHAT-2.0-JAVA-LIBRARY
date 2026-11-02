package com.waya.wayaquick.model;

/**
 * Request body for {@code POST /identity-verification/bvn/initiate}.
 * Initiates two-step BVN verification by sending an OTP to the phone number registered to the BVN.
 *
 * @param bvn 11-digit Bank Verification Number.
 */
public record BvnInitiateRequest(String bvn) {
}

