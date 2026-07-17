package com.waya.wayaquick.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single bank entry from {@code GET /get-bank-list}.
 * Use {@link #code()} for payouts and account verification.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Bank(
        /** CBN bank code, e.g. "044". */
        String code,
        /** Bank name, e.g. "Access Bank". */
        String name,
        /** Bank identifier, e.g. "044". */
        String id,
        /** Whether the bank is currently enabled for transfers. */
        boolean status
) {
}
