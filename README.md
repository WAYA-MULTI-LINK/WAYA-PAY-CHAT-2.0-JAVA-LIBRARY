# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.0/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.0/maven-plugin/reference/html/#build-image)

# WayaPay Java SDK

Official Java SDK for integrating with WayaPay payment APIs.

The SDK provides support for:

- Payment Collection
- Payouts
- Transaction Verification
- Bank Listing
- Account Verification

Built with Java and Spring Boot compatible components.

---

# Installation

## Maven

```xml
<dependency>
    <groupId>io.github.semzytheman</groupId>
    <artifactId>wayapay-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

# Requirements

- Java 17+
- Maven or Gradle
- Spring Boot 3+ (recommended)

---

# Recommended Project Structure

```text
src/main/java/com/wayapay/sdk/
├── WayaPayClient.java
├── WayaPayConfig.java
├── dto/
│   ├── InitializePaymentRequest.java
│   ├── InitiatePayoutRequest.java
│   ├── PaymentMetadata.java
│   └── VerifyAccountRequest.java
└── config/
    └── RestTemplateConfig.java
```

---

# Initialization

```java
import com.wayapay.sdk.WayaPayClient;
import com.wayapay.sdk.WayaPayConfig;

import org.springframework.web.client.RestTemplate;

RestTemplate restTemplate = new RestTemplate();

WayaPayConfig config = new WayaPayConfig(
        "your-merchant-id",
        "your-public-key",
        "development"
);

WayaPayClient client = new WayaPayClient(
        restTemplate,
        config
);
```

---

# Environment Values

| Environment | Description |
|---|---|
| development | Sandbox / staging |
| test | Sandbox / staging |
| production | Live production |
| prod | Live production |

---

# Initialize Payment

Initialize a payment collection request.

## Example

```java
import com.wayapay.sdk.dto.InitializePaymentRequest;
import com.wayapay.sdk.dto.PaymentMetadata;

import java.math.BigDecimal;
import java.util.UUID;

PaymentMetadata metadata = new PaymentMetadata();

metadata.setFirstName("John");
metadata.setLastName("Doe");
metadata.setPhoneNumber("08012345678");
metadata.setEmailAddress("john@example.com");
metadata.setCancelUrl("https://yourapp.com/payment/cancel");

InitializePaymentRequest request =
        new InitializePaymentRequest();

request.setCurrency("NGN");
request.setAmount(new BigDecimal("5000"));
request.setCallBackUrl("https://yourapp.com/payment/callback");
request.setIdempotencyKey(UUID.randomUUID().toString());
request.setPaymentRef("PAY-" + System.currentTimeMillis());
request.setMetadata(metadata);

Object response = client.initializePayment(request);

System.out.println(response);
```

---

# Request Parameters

| Field | Type | Required | Description |
|---|---|---|---|
| currency | String | Yes | ISO currency code |
| amount | BigDecimal | Yes | Amount to collect |
| callBackUrl | String | Yes | Redirect URL after payment |
| idempotencyKey | String | Yes | Prevent duplicate transactions |
| paymentRef | String | Yes | Unique merchant payment reference |
| metadata | Object | Yes | Customer information |

## Metadata Parameters

| Field | Type | Required |
|---|---|---|
| firstName | String | Yes |
| lastName | String | Yes |
| phoneNumber | String | Yes |
| emailAddress | String | Yes |
| cancelUrl | String | No |

---

# Initiate Payout

Send funds to a customer bank account.

## Example

```java
import com.wayapay.sdk.dto.InitiatePayoutRequest;

import java.math.BigDecimal;
import java.util.UUID;

InitiatePayoutRequest request =
        new InitiatePayoutRequest();

request.setCurrency("NGN");
request.setAmount(new BigDecimal("1000"));
request.setIdempotencyKey(UUID.randomUUID().toString());
request.setBankCode("058");
request.setAccountNumber("0123456789");

Object response = client.initiatePayout(request);

System.out.println(response);
```

---

# Verify Transaction

Verify the status of a payment or payout transaction.

## Example

```java
Object response =
        client.verifyTransaction("TRX-123456789");

System.out.println(response);
```

---

# Fetch Bank List

Retrieve supported banks and bank codes.

## Example

```java
Object response = client.fetchBankList();

System.out.println(response);
```

---

# Verify Account

Verify a customer bank account before payout.

## Example

```java
import com.wayapay.sdk.dto.VerifyAccountRequest;

VerifyAccountRequest request =
        new VerifyAccountRequest();

request.setAccountNumber("0123456789");
request.setBankCode("058");

Object response = client.verifyAccount(request);

System.out.println(response);
```

---

# Successful Response Format

```json
{
  "status": true,
  "data": {
    "reference": "PAY-123456",
    "authorizationUrl": "https://checkout.url"
  }
}
```

---

# Error Response Format

```json
{
  "status": false,
  "message": "currency is required"
}
```

---

# Spring Boot Configuration

## RestTemplate Bean

```java
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

# Maven Dependencies

```xml
<dependencies>

    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
        <version>6.1.8</version>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <dependency>
        <groupId>org.apache.httpcomponents.client5</groupId>
        <artifactId>httpclient5</artifactId>
        <version>5.3.1</version>
    </dependency>

</dependencies>
```

---

# Build the SDK

```bash
mvn clean install
```

Generated artifact:

```text
target/wayapay-java-sdk-1.0.0.jar
```

---

# Production Usage

Use environment variables for credentials.

```java
WayaPayConfig config = new WayaPayConfig(
        System.getenv("WAYAPAY_MERCHANT_ID"),
        System.getenv("WAYAPAY_SECRET_KEY"),
        "production"
);
```

---

# Example `.env`

```env
WAYAPAY_MERCHANT_ID=your-merchant-id
WAYAPAY_SECRET_KEY=your-secret-key
```

---

# Security Recommendations

- Never expose your secret key publicly
- Store credentials in environment variables
- Always verify transactions server-side
- Use unique idempotency keys for retries
- Verify customer bank accounts before payouts

---

# Publishing

## Maven Central (Recommended)

Publish the SDK to Maven Central for public consumption.

## GitHub Packages

Alternative private/public package distribution option.

---

# Support

For support and integration assistance:

- Website: https://wayapay.ng
- Email: support@wayapay.ng

---

# License

MIT License
