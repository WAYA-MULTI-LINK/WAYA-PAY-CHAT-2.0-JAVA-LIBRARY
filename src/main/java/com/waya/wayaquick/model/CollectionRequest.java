package com.waya.wayapay.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Request body for {@code POST /payment-collect/initiate}.
 * Starts a payment collection and returns a checkout URL.
 *
 * <p>Build with the {@link #builder()} — only {@code meta} is optional.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CollectionRequest(
        /** Quoted string, e.g. "1500.00". */
        String amount,
        /** Quoted string, e.g. "NGN". */
        String currency,
        /** Quoted string, e.g. "example@email.com". */
        String email,
        /** Your unique reference / idempotency key. */
        String transactionId,
        /** Quoted string, e.g. "firstname". */
        String firstName,
        /** Quoted string, e.g. "lastname". */
        String lastName,
        /** Quoted string, e.g. "08012345678". */
        String phone,
        /** Quoted string, e.g. "Order #1234". */
        String description,
        /** Arbitrary metadata sent as a JSON object. Optional. */
        Map<String, Object> meta
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String amount;
        private String currency;
        private String email;
        private String transactionId;
        private String firstName;
        private String lastName;
        private String phone;
        private String description;
        private Map<String, Object> meta;

        public Builder amount(String amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder transactionId(String transactionId) { this.transactionId = transactionId; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder meta(Map<String, Object> meta) { this.meta = meta; return this; }

        public CollectionRequest build() {
            return new CollectionRequest(amount, currency, email, transactionId,
                    firstName, lastName, phone, description, meta);
        }
    }
}
