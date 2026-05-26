package com.faymosInc.wayapaylib.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.faymosInc.wayapaylib.config.WayaPayConfig;

import com.faymosInc.wayapaylib.dto.InitializePaymentRequest;
import com.faymosInc.wayapaylib.dto.InitiatePayoutRequest;
import com.faymosInc.wayapaylib.dto.VerifyAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class WayaPayClient {

    private final RestTemplate restTemplate;
    private final WayaPayConfig config;

    public Map<String, Object> initializePayment(InitializePaymentRequest request) {

        if (isBlank(request.getCurrency())) {
            return error("currency is required");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return error("amount is required");
        }

        if (isBlank(request.getCallBackUrl())) {
            return error("callBackUrl is required");
        }

        if (isBlank(request.getIdempotencyKey())) {
            return error("idempotencyKey is required");
        }

        if (isBlank(request.getPaymentRef())) {
            return error("paymentRef is required");
        }

        if (request.getMetadata() == null) {
            return error("metadata is required");
        }

        if (isBlank(request.getMetadata().getFirstName())) {
            return error("metadata.firstName is required");
        }

        if (isBlank(request.getMetadata().getLastName())) {
            return error("metadata.lastName is required");
        }

        if (isBlank(request.getMetadata().getPhoneNumber())) {
            return error("metadata.phoneNumber is required");
        }

        if (isBlank(request.getMetadata().getEmailAddress())) {
            return error("metadata.emailAddress is required");
        }

        return sendRequest(
                HttpMethod.POST,
                "/payment-collect/initiate",
                request
        );
    }

    public Map<String, Object> initiatePayout(InitiatePayoutRequest request) {

        if (isBlank(request.getCurrency())) {
            return error("currency is required");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return error("amount is required");
        }

        if (isBlank(request.getIdempotencyKey())) {
            return error("idempotencyKey is required");
        }

        if (isBlank(request.getBankCode())) {
            return error("bankCode is required");
        }

        if (isBlank(request.getAccountNumber())) {
            return error("accountNumber is required");
        }

        return sendRequest(
                HttpMethod.POST,
                "/payment-payout/initiate",
                request
        );
    }

    public Map<String, Object> verifyTransaction(String transactionRef) {

        if (isBlank(transactionRef)) {
            return error("transactionRef is required");
        }

        String endpoint = UriComponentsBuilder
                .fromPath("/payment/transaction")
                .queryParam("ref", transactionRef)
                .toUriString();

        return sendRequest(
                HttpMethod.GET,
                endpoint,
                null
        );
    }

    public Map<String, Object> fetchBankList() {

        return sendRequest(
                HttpMethod.GET,
                "/banks-list",
                null
        );
    }

    public Map<String, Object> verifyAccount(VerifyAccountRequest request) {

        if (isBlank(request.getAccountNumber())) {
            return error("accountNumber is required");
        }

        if (isBlank(request.getBankCode())) {
            return error("bankCode is required");
        }

        return sendRequest(
                HttpMethod.GET,
                "/account-verification",
                request
        );
    }

    private Map<String, Object> sendRequest(
            HttpMethod method,
            String endpoint,
            Object body
    ) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Merchant-ID", config.getMerchantId());
            headers.set("API-Secret-Key", config.getPublicKey());

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getBaseUrl() + endpoint,
                    method,
                    entity,
                    JsonNode.class
            );

            Map<String, Object> result = new HashMap<>();
            result.put("status", true);
            result.put("data", response.getBody());

            return result;

        } catch (Exception ex) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", false);
            result.put("message", ex.getMessage());

            return result;
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", false);
        error.put("message", message);
        return error;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}