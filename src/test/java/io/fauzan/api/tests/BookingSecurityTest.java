package io.fauzan.api.tests;

import io.fauzan.api.data.BookingFactory;
import io.fauzan.api.model.Booking;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Booking API")
@Feature("Authorization and negative paths")
class BookingSecurityTest extends LiveApiTest {
    private int id;
    private Booking booking;

    @BeforeEach
    void createBooking() {
        booking = BookingFactory.valid();
        id = api.create(booking).then().statusCode(200).extract().path("bookingid");
    }

    @ParameterizedTest(name = "[{index}] token={0}")
    @NullSource
    @ValueSource(strings = {"", "not-a-real-token", "abc123abc123abc"})
    void write_operations_require_a_valid_token(String badToken) {
        assertThat(api.update(id, booking.withFirstname("Hacker"), badToken).statusCode()).isEqualTo(403);
        assertThat(api.patch(id, Map.of("totalprice", 1), badToken).statusCode()).isEqualTo(403);
        assertThat(api.delete(id, badToken).statusCode()).isEqualTo(403);

        Booking unchanged = api.get(id).as(Booking.class);
        assertThat(unchanged).as("rejected writes must not change data").usingRecursiveComparison().isEqualTo(booking);
    }

    @Test
    void unknown_booking_returns_404() {
        assertThat(api.get(Integer.MAX_VALUE).statusCode()).isEqualTo(404);
    }
}
