package com.faymosInc.wayapaylib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePayoutRequest {

    /**
     * Amount to send, in the major unit.
     */
    private BigDecimal amount;

    /**
     * ISO currency code (e.g. NGN).
     */
    private String currency;

    /**
     * Destination account number.
     */
    private String accountNumber;

    /**
     * Destination bank code from Fetch Bank List.
     */
    private String bankCode;

    /**
     * Destination account name (match the verified name).
     */
    private String accountName;

    /**
     * Your unique reference per payout. Used for deduplication and tracking —
     * generate a fresh value for each new payout.
     */
    private String reference;

    /**
     * Description that appears on the transfer.
     */
    private String narration;
}