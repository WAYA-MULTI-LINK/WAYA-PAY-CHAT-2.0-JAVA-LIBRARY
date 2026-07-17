package com.waya.wayapay.examples;

import com.waya.wayapay.WayaPayClient;
import com.waya.wayapay.WayaPayWebhook;
import com.waya.wayapay.WayaPayWebhookException;
import com.waya.wayapay.model.Bank;
import com.waya.wayapay.model.BvnResponse;
import com.waya.wayapay.model.CollectionRequest;
import com.waya.wayapay.model.CollectionResponse;
import com.waya.wayapay.model.CollectionStatus;
import com.waya.wayapay.model.CollectionStatusResponse;
import com.waya.wayapay.model.PayoutRequest;
import com.waya.wayapay.model.PayoutResponse;
import com.waya.wayapay.model.PayoutStatusResponse;
import com.waya.wayapay.model.VerifyAccountRequest;
import com.waya.wayapay.model.VerifyAccountResponse;
import com.waya.wayapay.model.WebhookEvent;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Runnable end-to-end demo. Run with:
 * <pre>
 *   WAYA_MERCHANT_ID=MER_... WAYA_SECRET_KEY=WAYASECK_TEST_... \
 *     mvn -q exec:java -Dexec.mainClass=com.waya.wayapay.examples.Demo
 * </pre>
 */
public final class Demo {

    public static void main(String[] args) {
        WayaPayClient client = new WayaPayClient(
                System.getenv("WAYA_MERCHANT_ID"),
                System.getenv("WAYA_SECRET_KEY"));

        try {
            // 1. List supported banks — grab codes for payouts and account verification
            var banks = client.payouts().listBanks();
            System.out.println("Banks loaded: " + banks.size());
            banks.stream()
                    .filter(b -> b.name().toLowerCase().contains("guaranty"))
                    .findFirst()
                    .ifPresent(b -> System.out.println("  GTB code: " + b.code()));

            // 2. Verify a destination account before moving money
            VerifyAccountResponse verified = client.payouts()
                    .verifyAccount(VerifyAccountRequest.others("0123456789", "044"));
            System.out.printf("Resolved: %s @ %s (%s)%n",
                    verified.accountName(), verified.bankName(), verified.responseCode());

            // 3. BVN identity check
            BvnResponse identity = client.identity().verifyBvn("22500809037");
            System.out.printf("BVN holder: %s %s | watch-listed: %s%n",
                    identity.firstName(), identity.lastName(), identity.watchListed());

            // 4. Initiate a payout — always verify the account first (step 2)
            PayoutResponse payout = client.payouts().initiate(PayoutRequest.builder()
                    .amount(new BigDecimal("250.00"))
                    .currency("NGN")
                    .accountNumber(verified.accountNumber())
                    .bankCode("044")
                    .accountName(verified.accountName())
                    .reference(WayaPayClient.generateReference("PAYOUT"))
                    .narration("Demo payout")
                    .build());
            System.out.println("Payout: " + payout.payoutReference() + " — " + payout.status());

            // 5. Check payout status — reconcile by the reference you sent at initiation
            String ref = payout.merchantReference() != null ? payout.merchantReference() : payout.payoutReference();
            PayoutStatusResponse payoutStatus = client.payouts().getStatus(ref);
            switch (payoutStatus.parsedStatus().outcome()) {
                case SUCCEEDED -> System.out.println("Payout delivered.");
                case REVERSED -> System.out.println("Payout reversed — wallet re-credited.");
                case RECONCILING -> System.out.println("Payout still reconciling — check again later.");
            }

            // 6. Initiate a collection — returns a checkout URL to redirect the customer to
            CollectionResponse collection = client.collection().initiate(CollectionRequest.builder()
                    .amount("1500.00")
                    .currency("NGN")
                    .email("customer@example.com")
                    .transactionId(WayaPayClient.generateReference("TXN"))
                    .firstName("John")
                    .lastName("Doe")
                    .phone("08012345678")
                    .description("Demo collection")
                    .build());
            System.out.println("Checkout URL: " + collection.checkOutUrl());

            // 7. Check collection status — the pull/safety-net path alongside the webhook
            CollectionStatusResponse deposit = client.collection().getStatus(collection.transactionId());
            System.out.printf("Collection status: %s (paid %s)%n", deposit.status(), deposit.amountPaid());
            if (deposit.parsedStatus() == CollectionStatus.SUCCESSFUL)
                System.out.println("Funds confirmed — fulfil using refNo " + deposit.refNo());

            // 8. Verify a webhook (offline demo). In production WayaPay POSTs this to your HTTPS endpoint;
            //    here we sign a sample body locally to show the verification flow end to end.
            final String webhookSecret = "WAYASECK_TEST_demo_webhook_secret";
            final String rawBody =
                    "{\"OrderId\":\"1779662251460508970\",\"Amount\":1500.00,\"Fee\":15.00,\"Currency\":\"NGN\","
                            + "\"Status\":\"SUCCESSFUL\",\"productName\":\"CARD\",\"customer\":{\"email\":\"john@example.com\"},"
                            + "\"merchantId\":\"MER_xyz\",\"recurrentPayment\":false}";
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = hmacBase64(webhookSecret, timestamp + "." + rawBody);

            try {
                WebhookEvent evt = WayaPayWebhook.constructEvent(rawBody, timestamp, signature, webhookSecret);
                System.out.printf("Webhook verified: %s — %s (%s %s)%n",
                        evt.orderId(), evt.status(), evt.amount(), evt.currency());
                if (evt.shouldFulfil())
                    System.out.println("  Fulfil order — idempotency key " + evt.orderId());
            } catch (WayaPayWebhookException e) {
                System.err.println("Rejected webhook: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("API error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String hmacBase64(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Demo() {
    }
}
