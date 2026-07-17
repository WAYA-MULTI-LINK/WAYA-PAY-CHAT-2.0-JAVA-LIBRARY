package com.waya.wayapay.service;

import com.waya.wayapay.WayaPayClient;
import com.waya.wayapay.WayaPayException;
import com.waya.wayapay.model.CollectionRequest;
import com.waya.wayapay.model.CollectionResponse;
import com.waya.wayapay.model.CollectionStatusResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Payment collection (deposit) endpoints. */
public final class Collection {

    private final WayaPayClient client;

    public Collection(WayaPayClient client) {
        this.client = client;
    }

    /**
     * {@code POST /payment-collect/initiate}. Returns a checkout URL to redirect the customer to.
     *
     * @throws WayaPayException on an API error
     */
    public CollectionResponse initiate(CollectionRequest input) {
        if (input == null)
            throw new IllegalArgumentException("input is required.");

        CollectionResponse data = client.request(
                "POST", "/payment-collect/initiate", input, null, client.type(CollectionResponse.class));
        if (data == null)
            throw new WayaPayException("Empty response data from /payment-collect/initiate");
        return data;
    }

    /**
     * {@code GET /payment-collect/status/{refNo}}. Returns the current state of a deposit by its refNo
     * (the gateway transactionId / webhook OrderId). Use for reconciliation alongside the deposit
     * webhook — the webhook is the primary signal; this is the pull/safety-net path.
     *
     * @throws IllegalArgumentException if {@code refNo} is blank
     * @throws WayaPayException         on an API error
     */
    public CollectionStatusResponse getStatus(String refNo) {
        if (refNo == null || refNo.isBlank())
            throw new IllegalArgumentException("refNo is required.");

        String path = "/payment-collect/status/" + URLEncoder.encode(refNo, StandardCharsets.UTF_8);
        CollectionStatusResponse data = client.request(
                "GET", path, null, null, client.type(CollectionStatusResponse.class));
        if (data == null)
            throw new WayaPayException("Empty response data from " + path);
        return data;
    }
}
