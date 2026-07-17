package com.waya.wayaquick.service;

import com.waya.wayaquick.WayaQuickWebhook;
import com.waya.wayaquick.model.WebhookEvent;

import java.time.Duration;

/**
 * Verifies and parses incoming transaction webhooks. A thin, discoverable wrapper over the static
 * {@link WayaQuickWebhook} — the methods without a {@code secret} parameter use the
 * {@code webhookSecret} configured on {@link com.waya.wayaquick.WayaQuickOptions}; the ones with it let
 * you route per environment.
 */
public final class Webhooks {

    private final String secret;

    public Webhooks(String webhookSecret) {
        this.secret = webhookSecret;
    }

    /**
     * Verifies the signature and replay window using the configured webhook secret, then parses the
     * body. Throws {@link com.waya.wayaquick.WayaQuickWebhookException} if verification fails.
     */
    public WebhookEvent constructEvent(String payload, String timestamp, String signature) {
        return WayaQuickWebhook.constructEvent(payload, timestamp, signature, requireSecret());
    }

    /** Same as {@link #constructEvent(String, String, String)} but with a custom tolerance window. */
    public WebhookEvent constructEvent(String payload, String timestamp, String signature, Duration tolerance) {
        return WayaQuickWebhook.constructEvent(payload, timestamp, signature, requireSecret(), tolerance);
    }

    /** Same as {@link #constructEvent(String, String, String)} but with an explicit secret (to route TEST vs PRODUCTION). */
    public WebhookEvent constructEvent(String payload, String timestamp, String signature, String secret, Duration tolerance) {
        return WayaQuickWebhook.constructEvent(payload, timestamp, signature, secret, tolerance);
    }

    /** Signature-only check (no replay window) using the configured webhook secret. */
    public boolean verifySignature(String payload, String timestamp, String signature) {
        return WayaQuickWebhook.verifySignature(payload, timestamp, signature, requireSecret());
    }

    /** Signature-only check (no replay window) with an explicit secret. */
    public boolean verifySignature(String payload, String timestamp, String signature, String secret) {
        return WayaQuickWebhook.verifySignature(payload, timestamp, signature, secret);
    }

    private String requireSecret() {
        if (secret == null || secret.isEmpty())
            throw new IllegalStateException(
                    "No webhook secret configured. Set WayaQuickOptions.webhookSecret, or call the method that takes an explicit secret.");
        return secret;
    }
}
