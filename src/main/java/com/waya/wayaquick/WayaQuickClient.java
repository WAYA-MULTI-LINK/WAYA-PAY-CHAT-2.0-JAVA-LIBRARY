package com.waya.wayaquick;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waya.wayaquick.model.WayaQuickResponse;
import com.waya.wayaquick.service.Collection;
import com.waya.wayaquick.service.Identity;
import com.waya.wayaquick.service.Payouts;
import com.waya.wayaquick.service.Webhooks;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * WayaQuick Merchant API v2 client.
 * Server-side only — your secret key lives here and only here.
 * Never ship it to a browser, a mobile app, or a public repo.
 *
 * <p>One client is thread-safe and cheap to share. Reach the API through the service accessors:
 * {@link #payouts()}, {@link #collection()}, {@link #identity()}, {@link #webhooks()}.
 */
public final class WayaQuickClient {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final WayaQuickOptions options;
    private final HttpClient http;
    private final ObjectMapper mapper;

    private final Identity identity;
    private final Payouts payouts;
    private final Collection collection;
    private final Webhooks webhooks;

    public WayaQuickClient(WayaQuickOptions options) {
        if (options == null)
            throw new IllegalArgumentException("options is required.");
        this.options = options;

        this.http = options.httpClient() != null
                ? options.httpClient()
                : HttpClient.newBuilder().connectTimeout(options.timeout()).build();

        this.mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.identity = new Identity(this);
        this.payouts = new Payouts(this);
        this.collection = new Collection(this);
        this.webhooks = new Webhooks(options.webhookSecret());
    }

    /** Convenience constructor for the common case. Uses the default base URL and settings. */
    public WayaQuickClient(String merchantId, String secretKey) {
        this(WayaQuickOptions.builder().merchantId(merchantId).secretKey(secretKey).build());
    }

    public Identity identity() { return identity; }
    public Payouts payouts() { return payouts; }
    public Collection collection() { return collection; }
    public Webhooks webhooks() { return webhooks; }

    /** Max retries configured for GET requests. */
    public int maxRetries() { return options.maxRetries(); }

    // ----- low-level request plumbing used by all services -----

    /** Builds a Jackson {@link JavaType} for a simple class — pass as the {@code dataType} to {@link #request}. */
    public JavaType type(Class<?> clazz) {
        return mapper.getTypeFactory().constructType(clazz);
    }

    /** Builds a Jackson {@link JavaType} for a {@code List<element>} — for endpoints that return arrays. */
    public JavaType listType(Class<?> element) {
        return mapper.getTypeFactory().constructCollectionType(java.util.List.class, element);
    }

    /**
     * Low-level request helper used by all services. Returns the envelope's {@code data} field.
     * GET requests retry on timeouts, network errors, 429, and 5xx with exponential backoff;
     * writes never auto-retry.
     *
     * @param dataType the {@link JavaType} of the {@code data} payload (use {@link #type} / {@link #listType})
     * @param <T>      the payload type
     * @throws WayaQuickException on a non-2xx status, {@code success == false}, network error, or timeout
     */
    public <T> T request(String method, String path, Object body, Map<String, String> query, JavaType dataType) {
        URI url = buildUrl(path, query);
        boolean retryable = "GET".equalsIgnoreCase(method);
        int ceiling = retryable ? options.maxRetries() : 0;
        JavaType envelopeType = mapper.getTypeFactory()
                .constructParametricType(WayaQuickResponse.class, dataType);

        int attempt = 0;
        while (true) {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder(url)
                    .timeout(options.timeout())
                    .header("X-Merchant-Id", options.merchantId())
                    .header("Authorization", "Bearer " + options.secretKey())
                    .header("Accept", "application/json");

            HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.noBody();
            if (body != null) {
                String json = serialize(body, path);
                publisher = HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8);
                reqBuilder.header("Content-Type", "application/json");
            }
            reqBuilder.method(method.toUpperCase(), publisher);

            String raw;
            int status;
            try {
                HttpResponse<String> response = http.send(reqBuilder.build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                status = response.statusCode();
                raw = response.body();
            } catch (HttpTimeoutException e) {
                if (retryable && attempt < ceiling) { attempt++; backoff(attempt); continue; }
                throw new WayaQuickException(
                        "Request to " + path + " timed out after " + options.timeout().toMillis() + "ms.", 0, e);
            } catch (IOException e) {
                if (retryable && attempt < ceiling) { attempt++; backoff(attempt); continue; }
                throw new WayaQuickException("Network error on " + path + ": " + e.getMessage(), 0, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new WayaQuickException("Request to " + path + " was interrupted.", 0, e);
            }

            boolean ok = status >= 200 && status < 300;

            WayaQuickResponse<T> envelope = null;
            if (raw != null && !raw.isEmpty()) {
                try {
                    envelope = mapper.readValue(raw, envelopeType);
                } catch (IOException e) {
                    throw new WayaQuickException(
                            "Non-JSON response from " + path + " (HTTP " + status + "): " + raw, status, e);
                }
            }

            boolean failed = !ok || (envelope != null && !envelope.success());
            if (failed) {
                boolean transient_ = status >= 500 || status == 429;
                if (retryable && transient_ && attempt < ceiling) {
                    attempt++;
                    backoff(attempt);
                    continue;
                }
                String message = envelope != null && envelope.message() != null
                        ? envelope.message()
                        : "Request to " + path + " failed with HTTP " + status + ".";
                throw new WayaQuickException(message, status);
            }

            return envelope == null ? null : envelope.data();
        }
    }

    /**
     * Generate a unique reference — your idempotency and reconciliation key.
     * Use one fresh reference per logical operation; reuse the same one on retries.
     * Produces a value like {@code PAYOUT-1748160000000-A1B2C3D4}.
     */
    public static String generateReference(String prefix) {
        long ms = System.currentTimeMillis();
        byte[] bytes = new byte[4];
        RANDOM.nextBytes(bytes);
        return prefix + "-" + ms + "-" + HexFormat.of().withUpperCase().formatHex(bytes);
    }

    /** {@link #generateReference(String)} with the default {@code "WP"} prefix. */
    public static String generateReference() {
        return generateReference("WP");
    }

    private String serialize(Object body, String path) {
        try {
            return mapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new WayaQuickException("Failed to serialise request body for " + path + ": " + e.getMessage(), 0, e);
        }
    }

    private URI buildUrl(String path, Map<String, String> query) {
        String url = options.baseUrl() + path;
        if (query == null || query.isEmpty())
            return URI.create(url);

        StringJoiner pairs = new StringJoiner("&");
        for (Map.Entry<String, String> kv : query.entrySet()) {
            if (kv.getValue() == null || kv.getValue().isEmpty()) continue;
            pairs.add(encode(kv.getKey()) + "=" + encode(kv.getValue()));
        }
        String qs = pairs.toString();
        return URI.create(qs.isEmpty() ? url : url + "?" + qs);
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private void backoff(int attempt) {
        long baseMs = Math.min(1000L * (1L << (attempt - 1)), 4000L);
        long jitter = ThreadLocalRandom.current().nextInt(0, 200);
        try {
            Thread.sleep(baseMs + jitter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WayaQuickException("Retry backoff was interrupted.", 0, e);
        }
    }
}
