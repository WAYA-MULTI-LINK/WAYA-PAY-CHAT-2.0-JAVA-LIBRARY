# WayaQuick Java SDK

Java client for the **WayaQuick Merchant API v2**. Collect payments, send payouts, verify bank accounts, and run BVN identity checks in Nigeria.

Targets **Java 17+**. One runtime dependency (Jackson for JSON); the HTTP layer is the JDK's built-in `java.net.http`. **Server-side only** — your secret key must never leave your server.

## Install

The library isn't on Maven Central yet. A pre-built JAR ships with the repo under
[`artifact/version2.0.1/`](artifact/README.md), bundled as a single **zip** for easy download. You
install it from GitHub in three steps: **download the zip**, **unzip it**, then **register it with Maven**.

### Step 1 — Download the zip from GitHub

Open the zip on GitHub and click **Download raw file**, or `curl` the raw URL:

```bash
curl -L -O https://github.com/WAYA-MULTI-LINK/WAYA-PAY-CHAT-2.0-JAVA-LIBRARY/raw/main/artifact/version2.0.1/wayaquick-integration-2.0.1.zip
```

The zip contains the library JAR, the sources JAR, and the POM.

> Tip: cloning the repo (`git clone …`) gives you the same files under
> `artifact/version2.0.1/` — skip the download and `cd` into that folder instead.

### Step 2 — Unzip it

```bash
unzip wayaquick-integration-2.0.1.zip
# -> wayaquick-integration-2.0.1.jar, wayaquick-integration-2.0.1-sources.jar, wayaquick-integration-2.0.1.pom
```

### Step 3 — Install it into your local Maven repo

From the folder where you unzipped the files, run:

```bash
mvn install:install-file \
  -Dfile=wayaquick-integration-2.0.1.jar \
  -DpomFile=wayaquick-integration-2.0.1.pom \
  -Dsources=wayaquick-integration-2.0.1-sources.jar
```

This places the SDK in `~/.m2/repository/io/github/waya-multi-link/wayaquick-integration/2.0.1/`. The `-DpomFile` flag is
what makes Jackson resolve transitively — without it you'd have to add Jackson by hand.

### Step 4 — Declare the dependency

In your project's `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.waya-multi-link</groupId>
        <artifactId>wayaquick-integration</artifactId>
        <version>2.0.1</version>
    </dependency>
</dependencies>
```

That's it — `mvn compile` now resolves the SDK (and Jackson) from your local repo. Requires **Java 17+**;
set `<maven.compiler.release>17</maven.compiler.release>` (or higher) in your project's properties.

### Gradle setup

After Steps 1–3 above (the SDK now lives in your local Maven repo, `~/.m2`), add `mavenLocal()` as a
repository and declare the dependency. `mavenLocal()` reads the POM, so Jackson resolves transitively.

```groovy
// build.gradle (Groovy DSL)
repositories {
    mavenLocal()    // resolves the SDK you installed in Step 2
    mavenCentral()  // resolves Jackson (pulled in transitively)
}

dependencies {
    implementation 'io.github.waya-multi-link:wayaquick-integration:2.0.1'
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}
```

```kotlin
// build.gradle.kts (Kotlin DSL)
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.waya-multi-link:wayaquick-integration:2.0.1")
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(17) }
}
```

Prefer not to install into `~/.m2`? Drop the downloaded `wayaquick-integration-2.0.1.jar` into a `libs/`
folder and use a flat-dir repository instead (you must then add Jackson yourself):

```groovy
repositories {
    flatDir { dirs 'libs' }
    mavenCentral()
}

dependencies {
    implementation name: 'wayaquick-integration-2.0.1'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.1'
}
```

### Building from source instead

```bash
git clone https://github.com/WAYA-MULTI-LINK/WAYA-PAY-CHAT-2.0-JAVA-LIBRARY.git
cd WAYA-PAY-CHAT-2.0-JAVA-LIBRARY
mvn clean install   # builds, tests, and installs to ~/.m2 — artifact: target/wayaquick-integration-2.0.1.jar
```

Not using Maven? See [`artifact/README.md`](artifact/README.md) for Maven `system`-scope, Gradle
flat-dir, and plain-classpath setups.

## Quickstart

```java
import com.waya.wayaquick.WayaQuickClient;

// Shorthand — default base URL and settings:
WayaQuickClient client = new WayaQuickClient(
        "MER_...",            // merchant ID, from the dashboard
        "WAYASECK_TEST_...");  // swap for WAYASECK_... on live

// Or with full options:
WayaQuickClient client = new WayaQuickClient(WayaQuickOptions.builder()
        .merchantId("MER_...")
        .secretKey("WAYASECK_TEST_...")
        .webhookSecret("...")   // optional: enables client.webhooks() without passing a secret
        .timeoutMs(30_000)      // default: 30s
        .maxRetries(2)          // default: 2 — GET only, exponential backoff
        .build());
```

The client is thread-safe — build one and share it.

## API at a glance

| Call | Returns |
|------|---------|
| `client.payouts().listBanks()` | `List<Bank>` |
| `client.payouts().verifyAccount(…)` | `VerifyAccountResponse` |
| `client.payouts().initiate(…)` | `PayoutResponse` |
| `client.payouts().getStatus(reference)` | `PayoutStatusResponse` |
| `client.collection().initiate(…)` | `CollectionResponse` |
| `client.collection().getStatus(refNo)` | `CollectionStatusResponse` |
| `client.identity().verifyBvn(…)` | `BvnResponse` |
| `client.webhooks().constructEvent(…)` | `WebhookEvent` |
| `client.webhooks().verifySignature(…)` | `boolean` |
| `WayaQuickClient.generateReference(prefix)` | `String` |

Every API call returns the unwrapped payload directly and throws `WayaQuickException` on failure.

## List banks

```java
List<Bank> banks = client.payouts().listBanks();
// each entry has .code(), .name(), .id(), .status()
```

## Verify an account

Always verify before sending a payout — confirms the account exists and returns the registered name.

```java
VerifyAccountResponse account = client.payouts()
        .verifyAccount(VerifyAccountRequest.others("0123456789", "044"));
//      ... or .wayaBank("0123456789") for an intra-bank ("WAYA-BANK") enquiry
System.out.println(account.accountName()); // "JOHN DOE"
```

**Returns** `VerifyAccountResponse` — `.successful()`, `.accountNumber()`, `.accountName()`, `.bankCode()`, `.bankName()`, `.responseCode()`, `.responseMessage()`, `.enquiryType()`.

## Initiate a payout

```java
PayoutResponse payout = client.payouts().initiate(PayoutRequest.builder()
        .amount("5000.00")
        .currency("NGN")
        .accountNumber("0123456789")
        .bankCode("044")
        .accountName(account.accountName())
        .reference(WayaQuickClient.generateReference("PAYOUT"))
        .narration("April salary")
        .build());
// payout.status() == "PROCESSING" means accepted, not yet settled
```

**Returns** `PayoutResponse` — `.payoutReference()`, `.merchantReference()`, `.status()`, `.message()`.

`generateReference` produces a timestamped, collision-resistant key (`PAYOUT-1748160000000-A1B2C3D4`). Generate a fresh one per operation and reuse the same one on retries.

## Check payout status

Reconcile a payout by the reference you sent at initiation.

```java
PayoutStatusResponse payout = client.payouts().getStatus("PAYOUT-20260604-001");

switch (payout.parsedStatus().outcome()) {
    case SUCCEEDED   -> { /* funds delivered */ }
    case REVERSED    -> { /* failed — wallet re-credited */ }
    case RECONCILING -> { /* PENDING — check again later */ }
}
```

**Returns** `PayoutStatusResponse` — `.transactionReference()`, `.status()`, `.amount()`, `.destinationAccountNumber()`, `.destinationAccountName()`, `.destinationBankName()`, `.narration()`, `.createdAt()`. Parse `.status()` with `.parsedStatus()` → `PayoutStatus`.

| `status` | Terminal | Meaning |
|------------|----------|---------|
| `PENDING`  | no  | Submitted; terminal outcome not yet recorded (reconciling). |
| `SUCCESS`  | yes | Completed successfully. |
| `REVERSED` | yes | Failed/reversed — the merchant wallet was re-credited. |

## Collect a payment

```java
CollectionResponse collection = client.collection().initiate(CollectionRequest.builder()
        .amount("1500.00")
        .currency("NGN")
        .email("customer@example.com")
        .transactionId(WayaQuickClient.generateReference("TXN"))
        .firstName("John")
        .lastName("Doe")
        .phone("08012345678")
        .description("Order #1234")
        .build());
// Redirect the customer to collection.checkOutUrl() to complete payment.
```

**Returns** `CollectionResponse` — `.uniqueId()`, `.transactionId()`, `.checkOutUrl()`, `.amount()`, `.email()`, `.merchantId()`.

## Check collection (deposit) status

The deposit webhook is the primary signal; this endpoint is the pull/safety-net path for reconciliation. Look it up by `refNo` (the gateway `transactionId` / webhook `OrderId`).

```java
CollectionStatusResponse deposit = client.collection().getStatus("1779662251460508970");

if (deposit.parsedStatus() == CollectionStatus.SUCCESSFUL) {
    // Funds confirmed — fulfil. Use deposit.refNo() as the idempotency key.
} else if (!deposit.parsedStatus().isTerminal()) {
    // Still in flight — keep polling; don't refund or retry.
}
```

**Returns** `CollectionStatusResponse` — `.refNo()`, `.tranId()`, `.merchantId()`, `.amount()`, `.amountPaid()`, `.fee()`, `.currencyCode()`, `.status()`, `.settlementStatus()`, `.channel()`, `.processedBy()`, `.customerEmail()`, `.description()`, `.environment()`, `.tranDate()`. Parse `.status()` with `.parsedStatus()` → `CollectionStatus`.

`amount()` is the expected amount; `amountPaid()` is what was actually received — it can be smaller (`PARTIAL` underpayment) or larger (overpayment). Use `status` + `amountPaid` as authoritative.

| `status` | Terminal | Outcome | Meaning |
|----------|----------|---------|---------|
| `INITIATED` / `PENDING` / `PROCESSING` / `APPROVED` | no | `IN_FLIGHT` | In flight — keep polling; don't refund or retry. |
| `PARTIAL` | no | `IN_FLIGHT` | Customer underpaid into a virtual account. |
| `SUCCESSFUL` | yes | `SUCCEEDED` | Funds confirmed — fulfil (use `refNo` for idempotency). |
| `REFUNDED` | yes | `REFUNDED` | Previously-successful transaction refunded. |
| `FAILED` / `DECLINED` / `REJECTED` / `ABANDONED` / `EXPIRED` / `CANCELLED` / `CUSTOMER_ERROR` / `FRAUD_ERROR` | yes | `NOT_DEBITED` | Customer not debited — no fulfilment. |
| `TIMEOUT` / `ERROR` / `SYSTEM_ERROR` / `BANK_ERROR` | yes | `INDETERMINATE` | Outcome unknown — reconcile, don't refund unilaterally. |

A reference that doesn't belong to the authenticated merchant returns `404` (surfaced as `WayaQuickException` with `statusCode() == 404`).

## Process webhooks

WayaQuick POSTs your server whenever a transaction becomes `SUCCESSFUL`, `PARTIAL`, or `FAILED`, so you can fulfil orders in real time instead of polling. **Verify every webhook before acting on it** — `constructEvent` checks the HMAC-SHA256 signature and the replay window, and throws `WayaQuickWebhookException` on anything it can't trust.

The signature is computed over the **exact raw request bytes**. Capture the body before any JSON middleware re-serialises it, or the recomputed HMAC won't match.

```java
import com.waya.wayaquick.WayaQuickWebhook;
import com.waya.wayaquick.WayaQuickWebhookException;
import com.waya.wayaquick.model.WebhookEvent;

// Spring MVC example. Take the body as a raw String so nothing re-serialises it.
@PostMapping("/waya/webhook")
public ResponseEntity<Void> handle(@RequestBody String rawBody,
                                   @RequestHeader(WayaQuickWebhook.TIMESTAMP_HEADER) String timestamp,
                                   @RequestHeader(WayaQuickWebhook.SIGNATURE_HEADER) String signature) {
    WebhookEvent evt;
    try {
        evt = WayaQuickWebhook.constructEvent(rawBody, timestamp, signature, webhookSecret);
    } catch (WayaQuickWebhookException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // unsigned / forged / stale
    }

    // Acknowledge fast (within ~10s), then queue the real work. orderId() is your idempotency key.
    switch (evt.parsedStatus()) {
        case SUCCESSFUL -> { /* upsert by evt.orderId(), then fulfil */ }
        case PARTIAL    -> { /* hold; query status by orderId for amount paid */ }
        case FAILED     -> { /* no fulfilment */ }
        default         -> { /* UNKNOWN — reconcile */ }
    }
    return ResponseEntity.ok().build();
}
```

**Returns** `WebhookEvent` — `.orderId()`, `.amount()`, `.fee()`, `.currency()`, `.status()`, `.description()`, `.tranTime()`, `.transactionDate()`, `.productName()`, `.businessName()`, `.customer()` (`.name()`, `.email()`, `.phoneNumber()`, `.customerId()`), `.merchantId()`, `.branchCategory()`, `.recurrentPayment()`. Parse `.status()` with `.parsedStatus()` → `WebhookStatus`. Throws `WayaQuickWebhookException` instead of returning when verification fails.

| `status` | `WebhookStatus` | What to do |
|----------|-----------------|------------|
| `SUCCESSFUL` | `SUCCESSFUL` | Fulfil the order. Check `orderId()` for idempotency. |
| `PARTIAL`    | `PARTIAL`    | Hold fulfilment — query the status endpoint by `orderId` for the latest amount paid. |
| `FAILED`     | `FAILED`     | No fulfilment. |

Notes:
- The merchant secret is your `merchantSecretTestKey` (TEST) or `merchantProductionSecretKey` (PRODUCTION). Keep one verifier per environment and route by which key validates.
- The same `orderId` may fire more than once (a `PARTIAL` then a `SUCCESSFUL`, or a re-emitted `SUCCESSFUL`). Always **upsert** keyed by `orderId`; never blindly insert.
- Replay protection rejects timestamps outside a 5-minute window by default. Override via the `tolerance` overload (pass a negative `Duration` to disable — not recommended).

For a signature-only check (no replay window), use `WayaQuickWebhook.verifySignature(...)`, which returns a `boolean`.

### Via the client

If you set `webhookSecret(...)` on the options, the same calls are available on the client without passing the secret each time:

```java
WebhookEvent evt = client.webhooks().constructEvent(rawBody, timestamp, signature);
```

`client.webhooks().constructEvent` / `verifySignature` also have overloads that take an explicit `secret`, so a single endpoint can route TEST vs PRODUCTION by trying each key.

## BVN identity check

```java
BvnResponse identity = client.identity().verifyBvn("22500809037"); // exactly 11 digits — validated locally
System.out.println(identity.firstName() + " " + identity.lastName());
```

**Returns** `BvnResponse` — `.bvn()`, `.firstName()`, `.middleName()`, `.lastName()`, `.dateOfBirth()`, `.gender()`, `.phoneNumber1()`, `.email()`, `.nationality()`, `.stateOfOrigin()`, `.lgaOfOrigin()`, `.lgaOfResidence()`, `.residentialAddress()`, `.maritalStatus()`, `.registrationDate()`, `.watchListed()`, `.base64Image()`.

BVN data is sensitive personal information. Store, transmit, and log it only as your data-protection obligations allow.

## Error handling

Failed requests throw `WayaQuickException` with the API message as the exception message and the HTTP status on `.statusCode()`.

```java
try {
    client.payouts().initiate(input);
} catch (WayaQuickException e) {
    System.err.println(e.getMessage());  // e.g. "IP 1.2.3.4 is not whitelisted"
    System.err.println(e.statusCode());  // e.g. 403
}
```

Input validation errors (missing required fields, malformed BVN, missing `bankCode`) throw `IllegalArgumentException` **before** any network call is made.

Retries apply to **GET requests only** (bank list, status checks) on timeouts, network errors, 429, and 5xx with exponential backoff. Writes never auto-retry.

## Full example

See [Demo.java](src/main/java/com/waya/wayaquick/examples/Demo.java) for a runnable end-to-end demo covering banks, account verification, BVN, payouts, collections, status checks, and webhook verification.

```bash
WAYA_MERCHANT_ID=MER_... WAYA_SECRET_KEY=WAYASECK_TEST_... \
  mvn -q exec:java -Dexec.mainClass=com.waya.wayaquick.examples.Demo
```

## Going live

On the merchant dashboard: finish KYC, grab your Merchant ID, generate your secret key under **Settings → API Keys and Webhooks**, and whitelist your server IPs. Swap `WAYASECK_TEST_...` for `WAYASECK_...` — the rest of your code stays the same.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Changes are tracked in [CHANGELOG.md](CHANGELOG.md).

## License

[MIT](LICENSE)
