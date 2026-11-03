# WayaQuick Java SDK Documentation (v2.0.4)

Official integration guide for the **WayaQuick Merchant API v2** using the official Java SDK.

---

## 1. Package Overview

* **Group ID**: `io.github.waya-multi-link`
* **Artifact ID**: `wayaquick-integration`
* **Version**: `2.0.4`
* **Java Support**: Java 17 or higher
* **HTTP Client**: Built-in JDK `java.net.http.HttpClient` (zero third-party networking overhead)
* **JSON Serialization**: Jackson 2.17+
* **Thread Safety**: `WayaQuickClient` is immutable and thread-safe. Instantiate once and share across threads.
* **Runnable Example**: [`src/main/java/com/waya/wayaquick/examples/Demo.java`](src/main/java/com/waya/wayaquick/examples/Demo.java)

---

## 2. Installation

### Maven (`pom.xml`)

```xml
<dependencies>
    <dependency>
        <groupId>io.github.waya-multi-link</groupId>
        <artifactId>wayaquick-integration</artifactId>
        <version>2.0.4</version>
    </dependency>
</dependencies>
```

### Gradle (`build.gradle`)

```groovy
dependencies {
    implementation 'io.github.waya-multi-link:wayaquick-integration:2.0.4'
}
```

---

## 3. Initialization & Authentication

Authenticate with your **Merchant ID** (`MER_...`) and **Secret Key** (`WAYASECK_TEST_...` for test sandbox, `WAYASECK_...` for live production):

```java
import com.waya.wayaquick.WayaQuickClient;
import com.waya.wayaquick.WayaQuickOptions;

// Simple constructor:
WayaQuickClient client = new WayaQuickClient(
    System.getenv("WAYA_MERCHANT_ID"),
    System.getenv("WAYA_SECRET_KEY")
);

// Or with builder configuration:
WayaQuickClient client = new WayaQuickClient(WayaQuickOptions.builder()
    .merchantId(System.getenv("WAYA_MERCHANT_ID"))
    .secretKey(System.getenv("WAYA_SECRET_KEY"))
    .webhookSecret(System.getenv("WAYA_WEBHOOK_SECRET")) // optional
    .timeoutMs(30_000)                                   // default 30s
    .maxRetries(2)                                       // default 2 (GET only)
    .build());
```

---

## 4. Core Features

### 4.1. Reference Generation (Idempotency)

```java
String payoutRef = WayaQuickClient.generateReference("PAYOUT");
// Produces timestamped collision-resistant key: PAYOUT-1748160000000-A1B2C3D4
```

---

### 4.2. List Banks (`GET /get-bank-list`)

Fetch supported banks and financial institutions with their CBN codes:

```java
import com.waya.wayaquick.model.Bank;
import java.util.List;

List<Bank> banks = client.payouts().listBanks();
for (Bank b : banks) {
    System.out.println(b.name() + " -> " + b.code());
}
```

---

### 4.3. Account Verification / Name Enquiry (`POST /verify-account`)

Validate the bank account number and retrieve the registered account name:

```java
import com.waya.wayaquick.model.VerifyAccountRequest;
import com.waya.wayaquick.model.VerifyAccountResponse;

// Inter-bank enquiry (requires bankCode):
VerifyAccountResponse account = client.payouts()
    .verifyAccount(VerifyAccountRequest.others("0123456789", "044"));
System.out.println("Resolved Name: " + account.accountName());

// Intra-bank enquiry (Waya Bank to Waya Bank):
VerifyAccountResponse wayaAccount = client.payouts()
    .verifyAccount(VerifyAccountRequest.wayaBank("0123456789"));
```

---

### 4.4. BVN Identity Verification (Two-Step OTP Flow)

In compliance with Nigerian data privacy regulations, BVN verification uses a 2-step OTP flow:

```java
import com.waya.wayaquick.model.BvnInitiateResponse;
import com.waya.wayaquick.model.BvnResponse;

// Step 1: Initiate verification (sends SMS OTP to customer's BVN phone number)
BvnInitiateResponse session = client.identity().initiateBvn("22222222222"); // 11 digits
System.out.println("OTP sent to: " + session.phoneNumber() + ", Session: " + session.reference());

// Step 2: Complete verification with the received OTP
BvnResponse profile = client.identity().completeBvn(session.reference(), "123456");
System.out.println("Customer: " + profile.firstName() + " " + profile.lastName());
System.out.println("Watchlisted: " + profile.watchListed());
```

---

### 4.5. Disburse Payouts (`POST /payment-payout/initiate`)

Send payouts from your merchant balance to any bank account:

```java
import com.waya.wayaquick.model.PayoutRequest;
import com.waya.wayaquick.model.PayoutResponse;
import com.waya.wayaquick.model.PayoutStatusResponse;
import java.math.BigDecimal;

// 1. Submit payout request
PayoutResponse payout = client.payouts().initiate(PayoutRequest.builder()
    .amount(new BigDecimal("25000.00"))
    .currency("NGN")
    .accountNumber("0123456789")
    .bankCode("044")
    .accountName("JOHN DOE")
    .reference(WayaQuickClient.generateReference("PAYOUT"))
    .narration("Salary payment")
    .build());

System.out.println("Payout status: " + payout.status()); // "PROCESSING"

// 2. Reconcile payout status:
PayoutStatusResponse status = client.payouts().getStatus(payout.merchantReference());
switch (status.parsedStatus().outcome()) {
    case SUCCEEDED   -> System.out.println("Payout delivered.");
    case REVERSED    -> System.out.println("Payout reversed — wallet re-credited.");
    case RECONCILING -> System.out.println("Payout still reconciling — check again later.");
}
```

---

### 4.6. Payment Collections / Hosted Checkout (`POST /payment-collect/initiate`)

Collect customer payments using the WayaQuick Hosted Checkout page:

```java
import com.waya.wayaquick.model.CollectionRequest;
import com.waya.wayaquick.model.CollectionResponse;
import com.waya.wayaquick.model.CollectionStatusResponse;
import com.waya.wayaquick.model.CollectionStatus;

// 1. Create Checkout Session
CollectionResponse checkout = client.collection().initiate(CollectionRequest.builder()
    .transactionId(WayaQuickClient.generateReference("TXN"))
    .amount("1500.00")
    .currency("NGN")
    .firstName("John")
    .lastName("Doe")
    .email("customer@example.com")
    .phone("08012345678")
    .description("Order #1001")
    .build());

System.out.println("Send customer to checkout URL: " + checkout.checkOutUrl());

// 2. Poll / Reconcile Status by transactionId (or gateway refNo)
CollectionStatusResponse deposit = client.collection().getStatus(checkout.transactionId());
if (deposit.parsedStatus() == CollectionStatus.SUCCESSFUL) {
    System.out.println("Funds confirmed! Amount received: " + deposit.amountPaid());
}
```

---

### 4.7. Webhook Verification

Verify HMAC-SHA256 signatures over raw inbound HTTP requests to prevent replay and forgery attacks:

```java
import com.waya.wayaquick.WayaQuickWebhook;
import com.waya.wayaquick.WayaQuickWebhookException;
import com.waya.wayaquick.model.WebhookEvent;
import com.waya.wayaquick.model.WebhookStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WayaWebhookController {

    @PostMapping("/webhook/waya")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody, // Must be unparsed raw string!
            @RequestHeader("x-waya-timestamp") String timestamp,
            @RequestHeader("x-waya-signature") String signature) {

        try {
            WebhookEvent event = WayaQuickWebhook.constructEvent(
                rawBody,
                timestamp,
                signature,
                System.getenv("WAYA_SECRET_KEY")
            );

            if (event.parsedStatus() == WebhookStatus.SUCCESSFUL) {
                // Fulfil order keyed by event.orderId()
                System.out.println("Fulfil order for: " + event.orderId());
            }

            return ResponseEntity.ok().build();
        } catch (WayaQuickWebhookException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
```

---

## 5. Error Handling

Failed API operations throw `WayaQuickException` containing the HTTP status and API response message:

```java
import com.waya.wayaquick.WayaQuickException;

try {
    client.payouts().initiate(request);
} catch (WayaQuickException e) {
    System.err.println("API Error: " + e.getMessage());
    System.err.println("HTTP Status: " + e.statusCode());
} catch (IllegalArgumentException e) {
    System.err.println("Client validation failed before network call: " + e.getMessage());
}
```

---

## 6. Runnable Example Document

The SDK includes a complete runnable demonstration:

* **File Location**: [`src/main/java/com/waya/wayaquick/examples/Demo.java`](src/main/java/com/waya/wayaquick/examples/Demo.java)
* **Run Command**:
  ```bash
  WAYA_MERCHANT_ID=MER_... WAYA_SECRET_KEY=WAYASECK_TEST_... \
    mvn -q exec:java -Dexec.mainClass=com.waya.wayaquick.examples.Demo
  ```

---

## 7. Parseable AI Text (Copy & Paste for LLMs)

Copy the prompt block below to give any AI coding tool complete knowledge of how to use this library:

```xml
<wayaquick_java_sdk_context version="2.0.4">
  <system_role>
    You are an expert Java payment integration engineer using the official WayaQuick Java SDK (`io.github.waya-multi-link:wayaquick-integration:2.0.4`).
    Target Java 17+. Adhere strictly to the v2 API contract and method signatures below.
  </system_role>

  <rules>
    1. IMMUTABLE CLIENT: Instantiate `new WayaQuickClient(merchantId, secretKey)` once as a singleton or Spring `@Bean`.
    2. BVN 2-STEP OTP: BVN verification requires `client.identity().initiateBvn(bvn)` (sends OTP) followed by `client.identity().completeBvn(reference, otp)`. Single-step verification is deprecated.
    3. ACCOUNT ENQUIRY: Use `VerifyAccountRequest.others(accountNumber, bankCode)` for inter-bank or `VerifyAccountRequest.wayaBank(accountNumber)` for intra-bank.
    4. HOSTED CHECKOUT: Use `client.collection().initiate(CollectionRequest.builder()...build())`. Redirect customer to `checkout.checkOutUrl()`.
    5. PAYOUTS ARE ASYNC: `client.payouts().initiate(...)` returns status `PROCESSING`. Reconcile by polling `client.payouts().getStatus(ref)` or via webhooks.
    6. WEBHOOK RAW BODY: `WayaQuickWebhook.constructEvent(rawBody, timestamp, signature, secret)`. Compute over raw body string before Spring/Jackson deserialization.
  </rules>

  <package_info>
    <group_id>io.github.waya-multi-link</group_id>
    <artifact_id>wayaquick-integration</artifact_id>
    <version>2.0.4</version>
    <sample_file>src/main/java/com/waya/wayaquick/examples/Demo.java</sample_file>
  </package_info>

  <code_template>
    <![CDATA[
import com.waya.wayaquick.WayaQuickClient;
import com.waya.wayaquick.model.*;
import java.math.BigDecimal;

WayaQuickClient client = new WayaQuickClient(
    System.getenv("WAYA_MERCHANT_ID"),
    System.getenv("WAYA_SECRET_KEY")
);

// 1. List Banks
var banks = client.payouts().listBanks();

// 2. Account Name Enquiry
VerifyAccountResponse verified = client.payouts()
    .verifyAccount(VerifyAccountRequest.others("0123456789", "044"));

// 3. BVN 2-Step OTP
BvnInitiateResponse bvnSession = client.identity().initiateBvn("22222222222");
BvnResponse bvnProfile = client.identity().completeBvn(bvnSession.reference(), "123456");

// 4. Initiate Payout
PayoutResponse payout = client.payouts().initiate(PayoutRequest.builder()
    .amount(new BigDecimal("5000.00"))
    .currency("NGN")
    .accountNumber(verified.accountNumber())
    .bankCode("044")
    .accountName(verified.accountName())
    .reference(WayaQuickClient.generateReference("PAYOUT"))
    .narration("Transfer payment")
    .build());
PayoutStatusResponse payoutStatus = client.payouts().getStatus(payout.merchantReference());

// 5. Payment Collection
CollectionResponse checkout = client.collection().initiate(CollectionRequest.builder()
    .amount("1500.00")
    .currency("NGN")
    .transactionId(WayaQuickClient.generateReference("TXN"))
    .firstName("John")
    .lastName("Doe")
    .email("customer@example.com")
    .phone("08012345678")
    .description("Order #1001")
    .build());
// Redirect customer to: checkout.checkOutUrl()
CollectionStatusResponse collectStatus = client.collection().getStatus(checkout.transactionId());
    ]]>
  </code_template>
</wayaquick_java_sdk_context>
```

