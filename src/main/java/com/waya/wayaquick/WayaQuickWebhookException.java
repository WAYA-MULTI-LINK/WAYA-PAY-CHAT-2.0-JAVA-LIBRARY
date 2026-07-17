package com.waya.wayapay;

/** Thrown when a webhook fails signature verification, replay checks, or cannot be parsed. */
public class WayaPayWebhookException extends RuntimeException {

    public WayaPayWebhookException(String message) {
        super(message);
    }

    public WayaPayWebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
