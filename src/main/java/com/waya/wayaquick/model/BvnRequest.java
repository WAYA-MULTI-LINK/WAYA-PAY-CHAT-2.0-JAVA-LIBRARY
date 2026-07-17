package com.waya.wayaquick.model;

/**
 * Request body for {@code POST /identity-verification/bvn}.
 * Verifies a BVN and returns the matching holder record.
 *
 * @param bvn 11-digit Bank Verification Number, e.g. "22500809037".
 */
public record BvnRequest(String bvn) {
}
