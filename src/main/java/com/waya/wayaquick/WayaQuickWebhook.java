package com.waya.wayapay;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waya.wayapay.model.WebhookEvent;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;

/**
 * Verifies and parses WayaPay transaction webhooks. Signature verification needs no network call,
 * so this is a standalone static helper — pass the raw request body, the signature headers, and the
 * merchant secret for the event's environment.
 *
 * <p><b>CRITICAL:</b> verify every webhook before acting on it. An unsigned or wrongly-signed call is
 * hostile — {@link #constructEvent} throws {@link WayaPayWebhookException} rather than returning a value.
 *
 * <p>The secret is your {@code merchantSecretTestKey} for a TEST transaction or your
 * {@code merchantProductionSecretKey} for a PRODUCTION one.
 *
 * <p>Capture the EXACT raw request bytes before any JSON parsing. If your framework deserialises and
 * re-serialises the body, the recomputed HMAC will not match.
 */
public final class WayaPayWebhook {

    /** Header carrying the epoch-millisecond timestamp that is signed alongside the body. */
    public static final String TIMESTAMP_HEADER = "X-Waya-Timestamp";

    /** Header carrying the Base64 HMAC-SHA256 signature. */
    public static final String SIGNATURE_HEADER = "X-Waya-Signature";

    /** Default replay-protection window. Webhooks older or newer than this are rejected. */
    public static final Duration DEFAULT_TOLERANCE = Duration.ofMinutes(5);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);

    private WayaPayWebhook() {
    }

    /**
     * Verifies the signature and replay window using {@link #DEFAULT_TOLERANCE}, then parses the body
     * into a {@link WebhookEvent}. Throws {@link WayaPayWebhookException} if verification fails — never
     * returns an unverified event.
     */
    public static WebhookEvent constructEvent(String payload, String timestamp, String signature, String secret) {
        return constructEvent(payload, timestamp, signature, secret, DEFAULT_TOLERANCE);
    }

    /**
     * Verifies the signature and replay window, then parses the body into a {@link WebhookEvent}.
     *
     * @param payload   the exact raw request body bytes, as text.
     * @param timestamp value of the {@link #TIMESTAMP_HEADER} header (epoch milliseconds).
     * @param signature value of the {@link #SIGNATURE_HEADER} header (Base64 HMAC-SHA256).
     * @param secret    the merchant secret for this event's environment (TEST or PRODUCTION).
     * @param tolerance replay window. Pass {@code null} to use {@link #DEFAULT_TOLERANCE}, or a negative
     *                  duration to skip the timestamp check (not recommended outside tests).
     */
    public static WebhookEvent constructEvent(
            String payload, String timestamp, String signature, String secret, Duration tolerance) {
        if (payload == null)
            throw new IllegalArgumentException("payload is required.");
        if (secret == null || secret.isEmpty())
            throw new IllegalArgumentException("Merchant secret is required.");

        if (!verifySignature(payload, timestamp, signature, secret))
            throw new WayaPayWebhookException("Webhook signature verification failed.");

        Duration window = tolerance != null ? tolerance : DEFAULT_TOLERANCE;
        if (!window.isNegative()) {
            long tsMs;
            try {
                tsMs = Long.parseLong(timestamp.trim());
            } catch (NumberFormatException e) {
                throw new WayaPayWebhookException("Webhook timestamp is not a valid epoch-millisecond value.");
            }
            long skewMs = Math.abs(System.currentTimeMillis() - tsMs);
            if (skewMs > window.toMillis())
                throw new WayaPayWebhookException(
                        "Webhook timestamp is outside the " + window.toSeconds() + "s tolerance window (possible replay).");
        }

        try {
            WebhookEvent evt = MAPPER.readValue(payload, WebhookEvent.class);
            if (evt == null)
                throw new WayaPayWebhookException("Webhook body deserialised to null.");
            return evt;
        } catch (WayaPayWebhookException e) {
            throw e;
        } catch (Exception e) {
            throw new WayaPayWebhookException("Webhook body is not valid JSON.", e);
        }
    }

    /**
     * Low-level signature check: returns true when {@code signature} equals
     * {@code Base64(HMAC-SHA256("{timestamp}.{payload}", secret))}. Does NOT check the replay window —
     * prefer {@link #constructEvent}. Comparison is constant-time.
     */
    public static boolean verifySignature(String payload, String timestamp, String signature, String secret) {
        if (payload == null)
            throw new IllegalArgumentException("payload is required.");
        if (isBlank(timestamp) || isBlank(signature) || isBlank(secret))
            return false;

        byte[] expected = hmacSha256(secret, timestamp + "." + payload);

        byte[] provided;
        try {
            provided = Base64.getDecoder().decode(signature);
        } catch (IllegalArgumentException e) {
            return false;
        }
        // MessageDigest.isEqual is constant-time and length-safe.
        return MessageDigest.isEqual(expected, provided);
    }

    private static byte[] hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // HmacSHA256 is guaranteed present on every JVM; an exception here is unrecoverable.
            throw new IllegalStateException("Failed to compute HMAC-SHA256.", e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
