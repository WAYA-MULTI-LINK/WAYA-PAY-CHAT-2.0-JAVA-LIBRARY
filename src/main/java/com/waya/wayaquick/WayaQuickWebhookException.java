package com.waya.wayaquick;

/** Thrown when a webhook fails signature verification, replay checks, or cannot be parsed. */
public class WayaQuickWebhookException extends RuntimeException {

    public WayaQuickWebhookException(String message) {
        super(message);
    }

    public WayaQuickWebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
