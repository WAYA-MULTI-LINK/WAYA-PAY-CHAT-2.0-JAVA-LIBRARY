package com.waya.wayaquick.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The paying customer embedded in a {@link WebhookEvent}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookCustomer(
        String name,
        String email,
        String phoneNumber,
        String customerId
) {
}
