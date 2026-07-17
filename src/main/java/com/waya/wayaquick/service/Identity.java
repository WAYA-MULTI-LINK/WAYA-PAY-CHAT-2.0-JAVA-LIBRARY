package com.waya.wayapay.service;

import com.waya.wayapay.WayaPayClient;
import com.waya.wayapay.WayaPayException;
import com.waya.wayapay.model.BvnRequest;
import com.waya.wayapay.model.BvnResponse;

import java.util.regex.Pattern;

/** Identity / KYC verification endpoints. */
public final class Identity {

    private static final Pattern ELEVEN_DIGITS = Pattern.compile("^\\d{11}$");

    private final WayaPayClient client;

    public Identity(WayaPayClient client) {
        this.client = client;
    }

    /**
     * {@code POST /identity-verification/bvn}. BVN is validated locally as exactly 11 digits before
     * the request is sent.
     *
     * @throws IllegalArgumentException if the BVN is not exactly 11 digits
     * @throws WayaPayException         on an API error
     */
    public BvnResponse verifyBvn(BvnRequest input) {
        if (input == null)
            throw new IllegalArgumentException("input is required.");
        if (input.bvn() == null || !ELEVEN_DIGITS.matcher(input.bvn()).matches())
            throw new IllegalArgumentException("bvn must be exactly 11 digits.");

        BvnResponse data = client.request(
                "POST", "/identity-verification/bvn", input, null, client.type(BvnResponse.class));
        if (data == null)
            throw new WayaPayException("Empty response data from /identity-verification/bvn");
        return data;
    }

    /** Convenience overload taking the BVN directly. */
    public BvnResponse verifyBvn(String bvn) {
        return verifyBvn(new BvnRequest(bvn));
    }
}
