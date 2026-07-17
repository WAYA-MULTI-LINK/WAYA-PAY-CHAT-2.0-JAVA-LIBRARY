package com.waya.wayapay.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /payment-payout/initiate}. Transfers funds to a bank account.
 *
 * <p>Build with the {@link #builder()} — {@code accountName} and {@code narration} are optional.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PayoutRequest(
        /** Amount to transfer. Must be greater than 0. */
        BigDecimal amount,
        /** ISO-4217 currency code, e.g. "NGN". */
        String currency,
        /** 10-digit NUBAN destination account number. */
        String accountNumber,
        /** CBN bank code from the bank list endpoint. */
        String bankCode,
        /** Your unique reference / idempotency key. Generate a fresh one per operation. */
        String reference,
        /** Destination account name. Should match the name returned by account verification. Optional. */
        String accountName,
        /** Statement narration shown on the recipient's bank statement. Optional. */
        String narration
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private BigDecimal amount;
        private String currency;
        private String accountNumber;
        private String bankCode;
        private String reference;
        private String accountName;
        private String narration;

        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder amount(String amount) { this.amount = new BigDecimal(amount); return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public Builder bankCode(String bankCode) { this.bankCode = bankCode; return this; }
        public Builder reference(String reference) { this.reference = reference; return this; }
        public Builder accountName(String accountName) { this.accountName = accountName; return this; }
        public Builder narration(String narration) { this.narration = narration; return this; }

        public PayoutRequest build() {
            return new PayoutRequest(amount, currency, accountNumber, bankCode, reference, accountName, narration);
        }
    }
}
