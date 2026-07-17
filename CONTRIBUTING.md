# Contributing

## Requirements

- JDK 17 or newer (`java -version`)
- Maven 3.8+ (`mvn -version`), or use the bundled `./mvnw` wrapper
- An IDE with Java support (IntelliJ IDEA, Eclipse, VS Code + Java Extension Pack)

## Project layout

```
src/main/java/com/waya/wayaquick/
  WayaQuickClient.java         # Entry point — HTTP client, retry loop, auth headers, generateReference
  WayaQuickOptions.java        # Configuration (builder) passed to the constructor
  WayaQuickException.java      # Thrown on API/transport failures (carries statusCode)
  WayaQuickWebhook.java        # Static HMAC-SHA256 verification + parsing
  WayaQuickWebhookException.java
  service/                   # Payouts, Collection, Identity, Webhooks
  model/                     # Request/response records and status enums per service
  examples/
    Demo.java                # Runnable end-to-end demo — kept in sync with the API

src/test/java/com/waya/wayaquick/
  ClientTest.java            # End-to-end client tests against a local HttpServer
  WebhookTest.java           # Signature, replay, and tamper tests
  StatusParsingTest.java     # Enum parsing / outcome / terminality tests
```

## Build

```bash
mvn clean install        # or: ./mvnw clean install
# Output: target/wayaquick-integration-<version>.jar (+ a -sources.jar)
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
  mvn -q exec:java -Dexec.mainClass=com.waya.wayaquick.examples.Demo
```

## Adding a new feature

1. Add request/response `record` types (and any status enum) under `src/main/java/com/waya/wayaquick/model/`.
2. Add the method to the relevant service in `src/main/java/com/waya/wayaquick/service/`, routing through `client.request(...)`.
3. Add tests covering the happy path, error path, correct HTTP method/path, and request body shape.
4. Update `examples/Demo.java` if the feature is user-facing.
5. Update `README.md` and `CHANGELOG.md` under the relevant version.

## Versioning

This project follows [Semantic Versioning](https://semver.org). Releases are cut by pushing a `v*.*.*` tag.

## Releasing & publishing to Maven Central

The library is published as [`io.github.waya-multi-link:wayaquick-integration`](https://central.sonatype.com/artifact/io.github.waya-multi-link/wayaquick-integration) on Maven Central. Publishing is automated: pushing a version tag runs `.github/workflows/release.yml`, which builds, tests, GPG-signs, uploads to the Sonatype Central Portal (auto-publishes after validation), and creates a GitHub Release with the jars attached.

### Cutting a release

1. Bump `<version>` in `pom.xml` and add an entry to `CHANGELOG.md`.
2. Restage the committed artifact bundle (see `artifact/README.md` for the recipe).
3. Commit everything, then tag and push:

   ```bash
   git tag v<x.y.z>
   git push origin main v<x.y.z>
   ```

4. Watch the **Release** workflow under the repo's Actions tab, and the deployment status under **Publish → Deployments** on [central.sonatype.com](https://central.sonatype.com). The artifact appears on Maven Central within ~30 minutes; [mvnrepository.com](https://mvnrepository.com) indexes it within a day.

> **Caution — versions on Central are immutable.** A version can never be re-published, changed, or deleted. If a version was already published (for example from a local `./mvnw clean deploy -Prelease`), do **not** tag that same version — the CI deploy would fail validation. Always start from the next version bump, and make sure the build is final before releasing.

### CI credentials

The workflow needs four repository secrets (Settings → Secrets and variables → Actions):

| Secret | Value |
|--------|-------|
| `CENTRAL_USERNAME` | Central Portal token username (Generate User Token on central.sonatype.com) |
| `CENTRAL_PASSWORD` | Central Portal token password |
| `GPG_PRIVATE_KEY` | Armored signing key: `gpg --armor --export-secret-keys <KEY_ID>` |
| `GPG_PASSPHRASE` | Passphrase of that key |

### Publishing manually (fallback)

With a Central Portal token in `~/.m2/settings.xml` (`<server><id>central</id>...`), a GPG key whose public half is on `keyserver.ubuntu.com`, and `export GPG_TTY=$(tty)` set:

```bash
./mvnw clean deploy -Prelease
```

Prefer the tag-driven CI release — manual publishes skip review and make it easy to burn a version number.

## Code style

- `record` for all model types
- Builders for configuration and request types with optional fields
- `IllegalArgumentException` for boundary validation *before* any network call; `WayaQuickException` for API/transport failures
- No comments explaining *what* the code does — only add one when the *why* is non-obvious
