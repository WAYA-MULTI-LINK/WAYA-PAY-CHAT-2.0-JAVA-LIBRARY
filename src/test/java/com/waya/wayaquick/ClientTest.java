package com.waya.wayapay;

import com.sun.net.httpserver.HttpServer;
import com.waya.wayapay.model.Bank;
import com.waya.wayapay.model.BvnRequest;
import com.waya.wayapay.model.VerifyAccountRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> lastBody = new AtomicReference<>();
    private final AtomicReference<String> lastAuth = new AtomicReference<>();
    private final AtomicReference<String> lastMerchant = new AtomicReference<>();
    private final AtomicInteger hits = new AtomicInteger();

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private WayaPayClient clientWith(int maxRetries) {
        return new WayaPayClient(WayaPayOptions.builder()
                .merchantId("MER_test")
                .secretKey("WAYASECK_TEST_abc")
                .baseUrl(baseUrl)
                .maxRetries(maxRetries)
                .build());
    }

    private void respond(String path, int status, String json) {
        server.createContext(path, exchange -> {
            hits.incrementAndGet();
            lastAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            lastMerchant.set(exchange.getRequestHeaders().getFirst("X-Merchant-Id"));
            lastBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }

    @Test
    void listBanksParsesArrayPayloadAndSendsAuthHeaders() {
        respond("/get-bank-list", 200, """
                {"success":true,"code":"00","message":"ok","data":[
                  {"code":"044","name":"Access Bank","id":"044","status":true},
                  {"code":"058","name":"GTBank","id":"058","status":true}
                ]}""");

        List<Bank> banks = clientWith(2).payouts().listBanks();

        assertEquals(2, banks.size());
        assertEquals("Access Bank", banks.get(0).name());
        assertEquals("Bearer WAYASECK_TEST_abc", lastAuth.get());
        assertEquals("MER_test", lastMerchant.get());
    }

    @Test
    void apiErrorEnvelopeThrowsWithMessage() {
        respond("/verify-account", 200, """
                {"success":false,"code":"57","message":"IP 1.2.3.4 is not whitelisted"}""");

        WayaPayException ex = assertThrows(WayaPayException.class,
                () -> clientWith(0).payouts().verifyAccount(VerifyAccountRequest.others("0123456789", "044")));
        assertEquals("IP 1.2.3.4 is not whitelisted", ex.getMessage());
    }

    @Test
    void httpErrorStatusThrowsWithStatusCode() {
        respond("/payment-collect/status/abc", 404, """
                {"success":false,"message":"not found"}""");

        WayaPayException ex = assertThrows(WayaPayException.class,
                () -> clientWith(0).collection().getStatus("abc"));
        assertEquals(404, ex.statusCode());
    }

    @Test
    void getRetriesOnServerErrorThenSucceeds() {
        AtomicInteger calls = new AtomicInteger();
        server.createContext("/get-bank-list", exchange -> {
            String json = calls.getAndIncrement() == 0
                    ? "{\"success\":false,\"message\":\"boom\"}"   // first call: 500
                    : "{\"success\":true,\"data\":[]}";            // retry: 200
            int status = calls.get() == 1 ? 500 : 200;
            byte[] b = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, b.length);
            exchange.getResponseBody().write(b);
            exchange.close();
        });

        List<Bank> banks = clientWith(2).payouts().listBanks();
        assertTrue(banks.isEmpty());
        assertEquals(2, calls.get(), "should have retried once after the 500");
    }

    @Test
    void bvnValidatedLocallyBeforeNetwork() {
        // No context registered — if a request escaped, it would 404/timeout. It must not.
        WayaPayClient client = clientWith(0);
        assertThrows(IllegalArgumentException.class, () -> client.identity().verifyBvn(new BvnRequest("123")));
        assertEquals(0, hits.get());
    }

    @Test
    void verifyAccountRequiresBankCodeForOthers() {
        assertThrows(IllegalArgumentException.class,
                () -> clientWith(0).payouts().verifyAccount(new VerifyAccountRequest("0123456789", "OTHERS", null)));
        assertEquals(0, hits.get());
    }

    @Test
    void generateReferenceIsPrefixedAndUnique() {
        String a = WayaPayClient.generateReference("PAYOUT");
        String b = WayaPayClient.generateReference("PAYOUT");
        assertTrue(a.startsWith("PAYOUT-"));
        assertNotEquals(a, b);
    }
}
