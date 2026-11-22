package io.fauzan.api.tests;

import io.fauzan.api.client.BookingClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;

/** Base for tests that hit the real API. A failed health check stops the run with a clear reason. */
@Tag("live")
abstract class LiveApiTest {
    protected static BookingClient api;
    protected static String token;

    @BeforeAll
    static void connect() {
        api = new BookingClient();
        assertThat(api.ping().statusCode())
                .as("API health check (/ping) must respond before running functional tests")
                .isEqualTo(201);
        token = api.token();
        assertThat(token).as("auth token").isNotBlank();
    }
}
