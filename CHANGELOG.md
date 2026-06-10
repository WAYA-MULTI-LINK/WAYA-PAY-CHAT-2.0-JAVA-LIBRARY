# Changelog

All notable changes to this project will be documented in this file.

This project follows [Semantic Versioning](https://semver.org).

## [2.0.0] - 2026-06-10

First functional release of the Java SDK, aligned with the WayaQuick Merchant API v2 and feature-matched to the WayaPay .NET SDK 2.0. Supersedes the empty `1.0.0` scaffold.

### Added

- `client.payouts().listBanks()` — returns all supported banks and CBN codes
- `client.payouts().verifyAccount(VerifyAccountRequest)` — resolves an account number to its registered name; validates that `bankCode` is present when `enquiryType` is `"OTHERS"`
- `client.payouts().initiate(PayoutRequest)` — initiates a bank transfer; `PROCESSING` means accepted, not settled
- `client.payouts().getStatus(reference)` — reconciles a payout by the reference sent at initiation
- `client.collection().initiate(CollectionRequest)` — starts a payment collection and returns a checkout URL
- `client.collection().getStatus(refNo)` — pull/safety-net status lookup for a deposit
- `client.identity().verifyBvn(BvnRequest)` — verifies a BVN with a local 11-digit format check before the network call
- `client.webhooks()` and the static `WayaPayWebhook` — HMAC-SHA256 signature verification, 5-minute replay window, and parsing into a `WebhookEvent` (constant-time signature comparison)
- `WayaPayClient.generateReference(prefix)` — timestamped, collision-resistant idempotency key (`PREFIX-millis-HEX`)
- Status enums with intent helpers: `PayoutStatus` / `CollectionStatus` / `WebhookStatus`, each with `from(String)`, `outcome()`, and `isTerminal()`
- Automatic retry with exponential backoff on GET requests (timeouts, network errors, 429, 5xx); writes never auto-retry
- `HttpClient` injection via `WayaPayOptions.builder().httpClient(...)` for connection pooling, proxies, and test fakes
- Builder-based configuration (`WayaPayOptions`) and request DTOs (`PayoutRequest`, `CollectionRequest`)

### Design notes

- **Synchronous** API — each call returns the unwrapped payload directly and throws on failure
- Transport is the JDK's built-in `java.net.http.HttpClient`; the only runtime dependency is Jackson (JSON)
- API failures throw `WayaPayException` (carrying `statusCode()`); input validation throws `IllegalArgumentException` before any network call
- Case-insensitive JSON binding handles the webhook's mixed PascalCase/camelCase wire format
- All model types are Java `record`s; targets Java 17+
