package com.faymosInc.wayapaylib.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InitializePaymentRequest {

    private String currency;
    private BigDecimal amount;
    private String callBackUrl;
    private String idempotencyKey;
    private String paymentRef;
    private PaymentMetadata metadata;
}