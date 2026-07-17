package com.waya.wayaquick.service;

import com.waya.wayaquick.WayaQuickClient;
import com.waya.wayaquick.WayaQuickException;
import com.waya.wayaquick.model.CollectionRequest;
import com.waya.wayaquick.model.CollectionResponse;
import com.waya.wayaquick.model.CollectionStatusResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Payment collection (deposit) endpoints. */
public final class Collection {

    private final WayaQuickClient client;

    public Collection(WayaQuickClient client) {
        this.client = client;
    }

    /**
     * {@code POST /payment-collect/initiate}. Returns a checkout URL to redirect the customer to.
     *
     * @throws WayaQuickException on an API error
     */
    public CollectionResponse initiate(CollectionRequest input) {
        if (input == null)
            throw new IllegalArgumentException("input is required.");

        CollectionResponse data = client.request(
                "POST", "/payment-collect/initiate", input, null, client.type(CollectionResponse.class));
        if (data == null)
            throw new WayaQuickException("Empty response data from /payment-collect/initiate");
        return data;
    }

    /**
     * {@code GET /payment-collect/status/{refNo}}. Returns the current state of a deposit by its refNo
     * (the gateway transactionId / webhook OrderId). Use for reconciliation alongside the deposit
     * webhook — the webhook is the primary signal; this is the pull/safety-net path.
     *
     * @throws IllegalArgumentException if {@code refNo} is blank
     * @throws WayaQuickException         on an API error
     */
    public CollectionStatusResponse getStatus(String refNo) {
        if (refNo == null || refNo.isBlank())
            throw new IllegalArgumentException("refNo is required.");

        String path = "/payment-collect/status/" + URLEncoder.encode(refNo, StandardCharsets.UTF_8);
        CollectionStatusResponse data = client.request(
                "GET", path, null, null, client.type(CollectionStatusResponse.class));
        if (data == null)
            throw new WayaQuickException("Empty response data from " + path);
        return data;
    }
}
