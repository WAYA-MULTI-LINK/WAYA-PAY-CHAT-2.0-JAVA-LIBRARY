package com.faymosInc.wayapaylib.config;

import lombok.Getter;

@Getter
public class WayaPayConfig {

    private final String merchantId;
    private final String secretKey;
    private final String baseUrl;
    private final String paymentLink;

    public WayaPayConfig(String merchantId, String secretKey, String environment) {

        if (merchantId == null || merchantId.isBlank()) {
            throw new IllegalArgumentException("merchantId is required");
        }

        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("secretKey is required");
        }

        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("environment is required");
        }

        boolean isProd = environment.trim().equalsIgnoreCase("production")
                || environment.trim().equalsIgnoreCase("prod");

        this.merchantId = merchantId;
        this.secretKey = secretKey;

        this.baseUrl = isProd
                ? "https://services.wayapay.ng"
                : "https://services.staging.wayapay.ng";

        this.paymentLink = isProd
                ? "https://pay.wayapay.ng/?_tranId="
                : "https://pay.staging.wayapay.ng/?_tranId=";
    }
}