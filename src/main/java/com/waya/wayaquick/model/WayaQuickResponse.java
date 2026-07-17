package com.waya.wayaquick.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Generic envelope returned by every WayaQuick endpoint.
 * {@code code "00"} + {@code success true} means OK; anything else is an error.
 *
 * @param <T> the typed payload carried in {@link #data()}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WayaQuickResponse<T>(
        boolean success,
        /** Bank-style response code. "00" means success. */
        String code,
        /** Human-readable message describing the response. */
        String message,
        /** Typed payload. Null on errors. */
        T data,
        /** Timestamp of the response. */
        String timestamp
) {
}
