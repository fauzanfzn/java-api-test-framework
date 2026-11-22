package io.fauzan.api.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.fauzan.api.client.BookingClient;
import io.fauzan.api.client.RetryPolicy;
import io.fauzan.api.data.BookingFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs offline against a WireMock stub. Verifies how the client behaves when the
 * server misbehaves: transient 5xx, permanent outage, and the exact request it sends.
 */
@Tag("mock")
@Epic("API client")
@Feature("Resilience and request contract (WireMock)")
class ClientResilienceTest {
    private static WireMockServer server;
    private BookingClient client;

    @BeforeAll
    static void start() {
        server = new WireMockServer(options().dynamicPort());
        server.start();
    }

    @AfterAll
    static void stop() {
        server.stop();
    }

    @BeforeEach
    void reset() {
        server.resetAll();
        client = new BookingClient(server.baseUrl(), new RetryPolicy(3, 10));
    }

    @Test
    void transient_503_is_retried_until_success() {
        server.stubFor(get("/booking/7").inScenario("flaky").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(serviceUnavailable()).willSetStateTo("recovering"));
        server.stubFor(get("/booking/7").inScenario("flaky").whenScenarioStateIs("recovering")
                .willReturn(serviceUnavailable()).willSetStateTo("up"));
        server.stubFor(get("/booking/7").inScenario("flaky").whenScenarioStateIs("up")
                .willReturn(okJson("{\"firstname\":\"Ana\",\"lastname\":\"Lee\",\"totalprice\":10,"
                        + "\"depositpaid\":true,\"bookingdates\":{\"checkin\":\"2026-01-01\",\"checkout\":\"2026-01-02\"}}")));

        assertThat(client.get(7).statusCode()).isEqualTo(200);
        server.verify(3, getRequestedFor(urlEqualTo("/booking/7")));
    }

    @Test
    void permanent_outage_gives_up_after_max_attempts() {
        server.stubFor(get(urlPathEqualTo("/booking")).willReturn(serverError()));

        assertThat(client.search(java.util.Map.of("firstname", "x")).statusCode()).isEqualTo(500);
        server.verify(3, getRequestedFor(urlPathEqualTo("/booking")));
    }

    @Test
    void client_errors_are_not_retried() {
        server.stubFor(get("/booking/404").willReturn(notFound()));

        assertThat(client.get(404).statusCode()).isEqualTo(404);
        server.verify(1, getRequestedFor(urlEqualTo("/booking/404")));
    }

    @Test
    void create_is_never_retried_to_avoid_duplicate_bookings() {
        server.stubFor(post("/booking").willReturn(serviceUnavailable()));

        assertThat(client.create(BookingFactory.valid()).statusCode()).isEqualTo(503);
        server.verify(1, postRequestedFor(urlEqualTo("/booking")));
    }

    @Test
    void writes_send_json_and_the_auth_cookie() {
        server.stubFor(put("/booking/1").willReturn(ok()));

        client.update(1, BookingFactory.valid(), "tok123");

        server.verify(putRequestedFor(urlEqualTo("/booking/1"))
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Accept", containing("application/json"))
                .withCookie("token", equalTo("tok123"))
                .withRequestBody(matchingJsonPath("$.bookingdates.checkin"))
                .withRequestBody(matchingJsonPath("$.totalprice")));
    }
}
