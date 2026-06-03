package com.faymosInc.wayapaylib.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.faymosInc.wayapaylib.config.WayaPayConfig;
import com.faymosInc.wayapaylib.dto.BvnVerificationRequest;
import com.faymosInc.wayapaylib.dto.CreateDynamicAccountRequest;
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

        if (isBlank(request.getAccountNumber())) {
            return error("accountNumber is required");
        }

        if (isBlank(request.getBankCode())) {
            return error("bankCode is required");
        }

        if (isBlank(request.getAccountName())) {
            return error("accountName is required");
        }

        if (isBlank(request.getReference())) {
            return error("reference is required");
        }

        if (isBlank(request.getNarration())) {
            return error("narration is required");
        }

        return sendRequest(
                HttpMethod.POST,
                "/payment-payout/initiate",
                request
        );
    }

    // Fixed: was using v1 path /payment/transaction?ref= and v1 query param name
    public Map<String, Object> verifyTransaction(String reference) {

        if (isBlank(reference)) {
            return error("reference is required");
        }

        String endpoint = UriComponentsBuilder
                .fromPath("/transaction/verify")
                .queryParam("reference", reference)
                .toUriString();

        return sendRequest(
                HttpMethod.GET,
                endpoint,
                null
        );
    }

    // Fixed: was using v1 path /banks-list
    public Map<String, Object> fetchBankList() {

        return sendRequest(
                HttpMethod.GET,
                "/account-enquiry/get-bank-list",
                null
        );
    }

    // Fixed: was GET /account-verification; v2 is POST /account-enquiry/verify-account
    // Added enquiryType as a required field per the v2 spec
    public Map<String, Object> verifyAccount(VerifyAccountRequest request) {

        if (isBlank(request.getAccountNumber())) {
            return error("accountNumber is required");
        }

        if (isBlank(request.getEnquiryType())) {
            return error("enquiryType is required");
        }

        // bankCode is only required when enquiryType is not WAYABANK
        if (!"WAYABANK".equalsIgnoreCase(request.getEnquiryType()) && isBlank(request.getBankCode())) {
            return error("bankCode is required for enquiryType OTHERS");
        }

        return sendRequest(
                HttpMethod.POST,
                "/account-enquiry/verify-account",
                request
        );
    }

    // New in v2
    public Map<String, Object> createDynamicAccount(CreateDynamicAccountRequest request) {

        if (isBlank(request.getAccountName())) {
            return error("accountName is required");
        }

        if (isBlank(request.getCustomerId())) {
            return error("customerId is required");
        }

        if (isBlank(request.getReferenceId())) {
            return error("referenceId is required");
        }

        if (isBlank(request.getPurpose())) {
            return error("purpose is required");
        }

        if (isBlank(request.getMode())) {
            return error("mode is required");
        }

        return sendRequest(
                HttpMethod.POST,
                "/account-enquiry/create-dynamic-account",
                request
        );
    }

    // New in v2
    public Map<String, Object> verifyBvn(BvnVerificationRequest request) {

        if (isBlank(request.getBvn())) {
            return error("bvn is required");
        }

        return sendRequest(
                HttpMethod.POST,
                "/identity-verification/bvn",
                request
        );
    }

    // New in v2
    public Map<String, Object> getTransactionHistory(
            Integer page,
            Integer size,
            String status,
            String from,
            String to
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/transaction/history");

        if (page != null) {
            builder.queryParam("page", page);
        }
        if (size != null) {
            builder.queryParam("size", size);
        }
        if (!isBlank(status)) {
            builder.queryParam("status", status);
        }
        if (!isBlank(from)) {
            builder.queryParam("from", from);
        }
        if (!isBlank(to)) {
            builder.queryParam("to", to);
        }

        return sendRequest(
                HttpMethod.GET,
                builder.toUriString(),
                null
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
            headers.set("accept", "application/json");
            headers.set("X-Merchant-Id", config.getMerchantId());
            // Fixed: was "Authorization " (trailing space) and "Bearer" (missing space before key)
            headers.set("Authorization", "Bearer " + config.getSecretKey());

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    config.getBaseUrl() + endpoint,
                    method,
                    entity,
                    JsonNode.class
            );

            JsonNode responseBody = response.getBody();

            Map<String, Object> result = new HashMap<>();
            // Fixed: was "status" — v2 envelope field is "success"
            result.put("success", responseBody != null && responseBody.path("success").asBoolean(false));
            result.put("data", responseBody);

            return result;

        } catch (Exception ex) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", ex.getMessage());

            return result;
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}