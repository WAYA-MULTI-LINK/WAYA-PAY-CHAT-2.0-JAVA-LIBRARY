# Contributing

## Requirements

- JDK 17 or newer (`java -version`)
- Maven 3.8+ (`mvn -version`), or use the bundled `./mvnw` wrapper
- An IDE with Java support (IntelliJ IDEA, Eclipse, VS Code + Java Extension Pack)

## Project layout

```
src/main/java/com/waya/wayapay/
  WayaPayClient.java         # Entry point — HTTP client, retry loop, auth headers, generateReference
  WayaPayOptions.java        # Configuration (builder) passed to the constructor
  WayaPayException.java      # Thrown on API/transport failures (carries statusCode)
  WayaPayWebhook.java        # Static HMAC-SHA256 verification + parsing
  WayaPayWebhookException.java
  service/                   # Payouts, Collection, Identity, Webhooks
  model/                     # Request/response records and status enums per service
  examples/
    Demo.java                # Runnable end-to-end demo — kept in sync with the API

src/test/java/com/waya/wayapay/
  ClientTest.java            # End-to-end client tests against a local HttpServer
  WebhookTest.java           # Signature, replay, and tamper tests
  StatusParsingTest.java     # Enum parsing / outcome / terminality tests
```

## Build

```bash
mvn clean install        # or: ./mvnw clean install
# Output: target/wayapay-java-sdk-<version>.jar (+ a -sources.jar)
```

## Run unit tests

```bash
# All tests
mvn test

# A single test class
mvn test -Dtest=WebhookTest

# A single test method
mvn test -Dtest=ClientTest#listBanksParsesArrayPayloadAndSendsAuthHeaders
```

Tests run entirely against a local `com.sun.net.httpserver.HttpServer` and offline HMAC signing. No credentials, no external network.

## Run the demo

```bash
WAYA_MERCHANT_ID=MER_... WAYA_SECRET_KEY=WAYASECK_TEST_... \
  mvn -q exec:java -Dexec.mainClass=com.waya.wayapay.examples.Demo
```

## Adding a new feature

1. Add request/response `record` types (and any status enum) under `src/main/java/com/waya/wayapay/model/`.
2. Add the method to the relevant service in `src/main/java/com/waya/wayapay/service/`, routing through `client.request(...)`.
3. Add tests covering the happy path, error path, correct HTTP method/path, and request body shape.
4. Update `examples/Demo.java` if the feature is user-facing.
5. Update `README.md` and `CHANGELOG.md` under the relevant version.

## Versioning

This project follows [Semantic Versioning](https://semver.org). Releases are cut by pushing a `v*.*.*` tag.

## Code style

- `record` for all model types
- Builders for configuration and request types with optional fields
- `IllegalArgumentException` for boundary validation *before* any network call; `WayaPayException` for API/transport failures
- No comments explaining *what* the code does — only add one when the *why* is non-obvious
