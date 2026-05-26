package com.faymosInc.wayapaylib.dto;



import lombok.Data;

import java.math.BigDecimal;

@Data
public class InitiatePayoutRequest {

    private String currency;
    private BigDecimal amount;
    private String idempotencyKey;
    private String bankCode;
    private String accountNumber;
}