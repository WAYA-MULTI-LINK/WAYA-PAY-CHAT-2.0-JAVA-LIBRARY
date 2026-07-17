package com.waya.wayapay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from {@code POST /verify-account}. Contains the resolved account name and bank details.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VerifyAccountResponse(
        /** True when the account was resolved successfully. */
        boolean successful,
        /** Provider response code. "00" means approved. */
        String responseCode,
        /** Human-readable provider message, e.g. "Approved". */
        String responseMessage,
        /** 10-digit NUBAN account number. */
        String accountNumber,
        /** Registered account name as held by the bank. */
        String accountName,
        /** CBN bank code of the destination bank. */
        String bankCode,
        /** Full name of the destination bank. */
        String bankName,
        /** "WAYA-BANK" for intra-bank, "OTHERS" for inter-bank. */
        String enquiryType
) {
}
