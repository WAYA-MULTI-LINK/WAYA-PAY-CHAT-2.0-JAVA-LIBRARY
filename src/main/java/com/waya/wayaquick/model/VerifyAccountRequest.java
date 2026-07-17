package com.waya.wayapay.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request body for {@code POST /verify-account}. Resolves an account number to its registered name.
 * Always call this before initiating a payout.
 *
 * @param accountNumber 10-digit NUBAN account number.
 * @param enquiryType   "WAYA-BANK" for intra-bank, "OTHERS" for inter-bank.
 * @param bankCode      CBN bank code. Required when {@code enquiryType} is "OTHERS".
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerifyAccountRequest(
        String accountNumber,
        String enquiryType,
        String bankCode
) {
    /** Convenience for inter-bank ("OTHERS") enquiries, which require a bank code. */
    public static VerifyAccountRequest others(String accountNumber, String bankCode) {
        return new VerifyAccountRequest(accountNumber, "OTHERS", bankCode);
    }

    /** Convenience for intra-bank ("WAYA-BANK") enquiries, which need no bank code. */
    public static VerifyAccountRequest wayaBank(String accountNumber) {
        return new VerifyAccountRequest(accountNumber, "WAYA-BANK", null);
    }
}
