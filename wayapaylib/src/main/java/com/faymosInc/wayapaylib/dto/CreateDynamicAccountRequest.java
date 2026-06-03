package com.faymosInc.wayapaylib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDynamicAccountRequest {

    /**
     * Display name for the virtual account.
     */
    private String accountName;

    /**
     * Your identifier for the paying customer.
     */
    private String customerId;

    /**
     * Your unique reference for this account or order.
     */
    private String referenceId;

    /**
     * Free text describing what the account is for.
     */
    private String purpose;

    /**
     * ONE_TIME for a single use account, or your reusable mode.
     */
    private String mode;
}