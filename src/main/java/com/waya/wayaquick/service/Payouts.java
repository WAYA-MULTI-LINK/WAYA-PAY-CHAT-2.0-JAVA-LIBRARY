package com.waya.wayaquick.service;

import com.waya.wayaquick.WayaQuickClient;
import com.waya.wayaquick.WayaQuickException;
import com.waya.wayaquick.model.Bank;
import com.waya.wayaquick.model.PayoutRequest;
import com.waya.wayaquick.model.PayoutResponse;
import com.waya.wayaquick.model.PayoutStatusResponse;
import com.waya.wayaquick.model.VerifyAccountRequest;
import com.waya.wayaquick.model.VerifyAccountResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Payout (disbursement), bank-list, and account-verification endpoints. */
public final class Payouts {

    private final WayaQuickClient client;

    public Payouts(WayaQuickClient client) {
        this.client = client;
    }

    /** {@code GET /get-bank-list}. Returns all supported banks and their CBN codes. */
    public List<Bank> listBanks() {
        List<Bank> data = client.request(
                "GET", "/get-bank-list", null, null, client.listType(Bank.class));
        return data != null ? data : List.of();
    }

    /**
     * {@code POST /verify-account}. Resolves an account number to its registered name.
     * {@code bankCode} is required when {@code enquiryType} is "OTHERS". Always call this before
     * initiating a payout.
     *
     * @throws IllegalArgumentException if {@code bankCode} is missing for an "OTHERS" enquiry
     * @throws WayaQuickException         on an API error
     */
    public VerifyAccountResponse verifyAccount(VerifyAccountRequest input) {
        if (input == null)
            throw new IllegalArgumentException("input is required.");
        if ("OTHERS".equals(input.enquiryType()) && (input.bankCode() == null || input.bankCode().isEmpty()))
            throw new IllegalArgumentException("bankCode is required when enquiryType is \"OTHERS\".");

        VerifyAccountResponse data = client.request(
                "POST", "/verify-account", input, null, client.type(VerifyAccountResponse.class));
        if (data == null)
            throw new WayaQuickException("Empty response data from /verify-account");
        return data;
    }

    /**
     * {@code POST /payment-payout/initiate}. PROCESSING means accepted, not settled.
     * Confirm via webhook or status check with the reference before treating as delivered.
     *
     * @throws WayaQuickException on an API error
     */
    public PayoutResponse initiate(PayoutRequest input) {
        if (input == null)
            throw new IllegalArgumentException("input is required.");

        PayoutResponse data = client.request(
                "POST", "/payment-payout/initiate", input, null, client.type(PayoutResponse.class));
        if (data == null)
            throw new WayaQuickException("Empty response data from /payment-payout/initiate");
        return data;
    }

    /**
     * {@code GET /payment-payout/status/{reference}}. Returns the latest status of a payout by the
     * reference you sent at initiation. Scoped to the authenticated merchant — a reference belonging
     * to another merchant (or a different environment) returns 404.
     *
     * @throws IllegalArgumentException if {@code reference} is blank
     * @throws WayaQuickException         on an API error
     */
    public PayoutStatusResponse getStatus(String reference) {
        if (reference == null || reference.isBlank())
            throw new IllegalArgumentException("reference is required.");

        String path = "/payment-payout/status/" + URLEncoder.encode(reference, StandardCharsets.UTF_8);
        PayoutStatusResponse data = client.request(
                "GET", path, null, null, client.type(PayoutStatusResponse.class));
        if (data == null)
            throw new WayaQuickException("Empty response data from " + path);
        return data;
    }
}
