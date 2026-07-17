package com.waya.wayaquick;

import com.waya.wayaquick.model.WebhookEvent;
import com.waya.wayaquick.model.WebhookStatus;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class WebhookTest {

    private static final String SECRET = "WAYASECK_TEST_demo_webhook_secret";
    private static final String BODY =
            "{\"OrderId\":\"1779662251460508970\",\"Amount\":1500.00,\"Fee\":15.00,\"Currency\":\"NGN\","
                    + "\"Status\":\"SUCCESSFUL\",\"productName\":\"CARD\",\"customer\":{\"email\":\"john@example.com\"},"
                    + "\"merchantId\":\"MER_xyz\",\"recurrentPayment\":false}";

    private static String sign(String secret, String timestamp, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] h = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(h);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void constructEventVerifiesAndParses() {
        String ts = String.valueOf(System.currentTimeMillis());
        String sig = sign(SECRET, ts, BODY);

        WebhookEvent evt = WayaQuickWebhook.constructEvent(BODY, ts, sig, SECRET);

        assertEquals("1779662251460508970", evt.orderId());
        assertEquals(WebhookStatus.SUCCESSFUL, evt.parsedStatus());
        assertTrue(evt.shouldFulfil());
        assertEquals("CARD", evt.productName());           // camelCase binds
        assertEquals("john@example.com", evt.customer().email());
        assertEquals(0, evt.amount().compareTo(new java.math.BigDecimal("1500.00")));
    }

    @Test
    void rejectsTamperedSignature() {
        String ts = String.valueOf(System.currentTimeMillis());
        String sig = sign("the-wrong-secret", ts, BODY);

        assertThrows(WayaQuickWebhookException.class,
                () -> WayaQuickWebhook.constructEvent(BODY, ts, sig, SECRET));
    }

    @Test
    void rejectsStaleTimestamp() {
        String oldTs = String.valueOf(System.currentTimeMillis() - Duration.ofMinutes(10).toMillis());
        String sig = sign(SECRET, oldTs, BODY);

        WayaQuickWebhookException ex = assertThrows(WayaQuickWebhookException.class,
                () -> WayaQuickWebhook.constructEvent(BODY, oldTs, sig, SECRET));
        assertTrue(ex.getMessage().toLowerCase().contains("replay"));
    }

    @Test
    void skipsTimestampCheckWithNegativeTolerance() {
        String oldTs = String.valueOf(System.currentTimeMillis() - Duration.ofHours(2).toMillis());
        String sig = sign(SECRET, oldTs, BODY);

        WebhookEvent evt = WayaQuickWebhook.constructEvent(BODY, oldTs, sig, SECRET, Duration.ofMillis(-1));
        assertEquals("1779662251460508970", evt.orderId());
    }

    @Test
    void verifySignatureReturnsFalseForGarbage() {
        String ts = String.valueOf(System.currentTimeMillis());
        assertFalse(WayaQuickWebhook.verifySignature(BODY, ts, "not-base64-!!!", SECRET));
        assertFalse(WayaQuickWebhook.verifySignature(BODY, null, "sig", SECRET));
    }
}
