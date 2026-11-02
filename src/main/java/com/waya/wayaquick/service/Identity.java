package com.waya.wayaquick.service;

import com.waya.wayaquick.WayaQuickClient;
import com.waya.wayaquick.WayaQuickException;
import com.waya.wayaquick.model.BvnCompleteRequest;
import com.waya.wayaquick.model.BvnInitiateRequest;
import com.waya.wayaquick.model.BvnInitiateResponse;
import com.waya.wayaquick.model.BvnRequest;
import com.waya.wayaquick.model.BvnResponse;

import java.util.regex.Pattern;

/** Identity / KYC verification endpoints. */
public final class Identity {

    private static final Pattern ELEVEN_DIGITS = Pattern.compile("^\\d{11}$");

    private final WayaQuickClient client;

    public Identity(WayaQuickClient client) {
        this.client = client;
    }

    /**
     * {@code POST /identity-verification/bvn/initiate}.
     * Initiates a two-step BVN verification by submitting an 11-digit BVN and dispatching an OTP
     * to the phone number registered against the BVN.
     *
     * @throws IllegalArgumentException if the BVN is not exactly 11 digits
     * @throws WayaQuickException       on an API error
     */
    public BvnInitiateResponse initiateBvn(BvnInitiateRequest input) {
        if (input == null)
            throw new IllegalArgumentException("input is required.");
        if (input.bvn() == null || !ELEVEN_DIGITS.matcher(input.bvn()).matches())
            throw new IllegalArgumentException("bvn must be exactly 11 digits.");

        BvnInitiateResponse data = client.request(
                "POST", "/identity-verification/bvn/initiate", input, null, client.type(BvnInitiateResponse.class));
        if (data == null)
            throw new WayaQuickException("Empty response data from /identity-verification/bvn/initiate");
        return data;
    }

    /** Convenience overload taking the BVN directly. */
    public BvnInitiateResponse initiateBvn(String bvn) {
        return initiateBvn(new BvnInitiateRequest(bvn));
    }

    /**
     * {@code POST /identity-verification/bvn/complete}.
     * Completes the BVN verification using the session reference from {@code initiateBvn}
     * and the OTP received on the registered phone number.
     *
     * @throws IllegalArgumentException if reference or otp is null or blank
     * @throws WayaQuickException       on an API error
     */
    public BvnResponse completeBvn(BvnCompleteRequest input) {
        if (input == null)
            throw new IllegalArgumentException("input is required.");
        if (input.reference() == null || input.reference().isBlank())
            throw new IllegalArgumentException("reference is required.");
        if (input.otp() == null || input.otp().isBlank())
            throw new IllegalArgumentException("otp is required.");

        BvnResponse data = client.request(
                "POST", "/identity-verification/bvn/complete", input, null, client.type(BvnResponse.class));
        if (data == null)
            throw new WayaQuickException("Empty response data from /identity-verification/bvn/complete");
        return data;
    }

    /** Convenience overload taking reference and otp directly. */
    public BvnResponse completeBvn(String reference, String otp) {
        return completeBvn(new BvnCompleteRequest(reference, otp));
    }

    /**
     * {@code POST /identity-verification/bvn}. BVN is validated locally as exactly 11 digits before
     * the request is sent.
     *
     * @deprecated WayaQuick BVN verification requires a two-step OTP flow.
     * Use {@link #initiateBvn} followed by {@link #completeBvn} instead.
     */
    @Deprecated
    public BvnResponse verifyBvn(BvnRequest input) {
        if (input == null)
            throw new IllegalArgumentException("input is required.");
        if (input.bvn() == null || !ELEVEN_DIGITS.matcher(input.bvn()).matches())
            throw new IllegalArgumentException("bvn must be exactly 11 digits.");

        BvnResponse data = client.request(
                "POST", "/identity-verification/bvn", input, null, client.type(BvnResponse.class));
        if (data == null)
            throw new WayaQuickException("Empty response data from /identity-verification/bvn");
        return data;
    }

    /** Convenience overload taking the BVN directly. */
    @Deprecated
    public BvnResponse verifyBvn(String bvn) {
        return verifyBvn(new BvnRequest(bvn));
    }
}
