package com.waya.wayapay;

/**
 * Thrown when a WayaPay API request fails — a non-2xx HTTP status, an envelope with
 * {@code success == false}, a network error, a timeout, or a non-JSON response.
 *
 * <p>The message carries the API's error message when one is available
 * (e.g. {@code "IP 1.2.3.4 is not whitelisted"}); otherwise it describes the transport failure.
 * Input validation errors ({@code IllegalArgumentException}) are thrown <em>before</em> any
 * network call and are not wrapped in this type.
 */
public class WayaPayException extends RuntimeException {

    /** HTTP status code associated with the failure, or 0 when the request never got a response. */
    private final int statusCode;

    public WayaPayException(String message) {
        this(message, 0, null);
    }

    public WayaPayException(String message, int statusCode) {
        this(message, statusCode, null);
    }

    public WayaPayException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /** HTTP status code associated with the failure, or 0 when the request never got a response. */
    public int statusCode() {
        return statusCode;
    }
}
