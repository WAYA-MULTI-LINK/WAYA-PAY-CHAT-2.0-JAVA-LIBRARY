package com.waya.wayapay;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration for {@link WayaPayClient}. Build with {@link #builder()}.
 *
 * <p>
 * {@code merchantId} and {@code secretKey} are required; everything else has a
 * sensible default.
 */
public final class WayaPayOptions {

    /** Default base URL — WayaQuick Merchant API v2 (production). */
    public static final String DEFAULT_BASE_URL = "https://services.wayapay.ng/merchant-middleware/api/v2";

    private final String merchantId;
    private final String secretKey;
    private final String webhookSecret;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;
    private final HttpClient httpClient;

    private WayaPayOptions(Builder b) {
        this.merchantId = b.merchantId;
        this.secretKey = b.secretKey;
        this.webhookSecret = b.webhookSecret;
        this.baseUrl = b.baseUrl;
        this.timeout = b.timeout;
        this.maxRetries = b.maxRetries;
        this.httpClient = b.httpClient;
    }

    /** Your Merchant ID, format MER_... */
    public String merchantId() {
        return merchantId;
    }

    /** Your secret key. WAYASECK_TEST_... on staging, WAYASECK_... on live. */
    public String secretKey() {
        return secretKey;
    }

    /**
     * Optional. Merchant webhook secret used by {@link WayaPayClient#webhooks()} to
     * verify incoming
     * webhooks (your merchantSecretTestKey on TEST, merchantProductionSecretKey on
     * PRODUCTION).
     * When unset, call the {@code Webhooks} methods that take an explicit secret.
     */
    public String webhookSecret() {
        return webhookSecret;
    }

    /** API base URL. Defaults to {@link #DEFAULT_BASE_URL}. */
    public String baseUrl() {
        return baseUrl;
    }

    /** Per-request timeout. Defaults to 30 seconds. */
    public Duration timeout() {
        return timeout;
    }

    /** Max retries. Applies to GET only. Defaults to 2. */
    public int maxRetries() {
        return maxRetries;
    }

    /**
     * Optionally inject your own {@link HttpClient} (connection pooling, proxies,
     * tests).
     * When null, the client creates one internally.
     */
    public HttpClient httpClient() {
        return httpClient;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String merchantId;
        private String secretKey;
        private String webhookSecret;
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration timeout = Duration.ofSeconds(30);
        private int maxRetries = 2;
        private HttpClient httpClient;

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        public Builder webhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder timeoutMs(long timeoutMs) {
            this.timeout = Duration.ofMillis(timeoutMs);
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public WayaPayOptions build() {
            if (merchantId == null || merchantId.isEmpty())
                throw new IllegalArgumentException("merchantId is required.");
            if (secretKey == null || secretKey.isEmpty())
                throw new IllegalArgumentException("secretKey is required.");
            if (baseUrl == null || baseUrl.isEmpty())
                throw new IllegalArgumentException("baseUrl is required.");
            if (timeout == null || timeout.isNegative() || timeout.isZero())
                throw new IllegalArgumentException("timeout must be positive.");
            if (maxRetries < 0)
                throw new IllegalArgumentException("maxRetries must be >= 0.");
            return new WayaPayOptions(this);
        }
    }
}
