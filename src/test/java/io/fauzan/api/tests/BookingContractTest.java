package io.fauzan.api.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fauzan.api.data.BookingFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;

@Epic("Booking API")
@Feature("Response contracts (JSON Schema)")
class BookingContractTest extends LiveApiTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void create_response_matches_contract() throws Exception {
        Response response = api.create(BookingFactory.valid());
        response.then().statusCode(200).body(matchesJsonSchemaInClasspath("schemas/created-booking.json"));

        // A nested path reaches Hamcrest as a Map, so serialise it back to JSON before validating.
        String nested = JSON.writeValueAsString(response.jsonPath().getMap("booking"));
        assertThat(nested, matchesJsonSchemaInClasspath("schemas/booking.json"));
    }

    @Test
    void get_response_matches_contract() {
        int id = api.create(BookingFactory.valid()).then().extract().path("bookingid");
        api.get(id).then().statusCode(200).body(matchesJsonSchemaInClasspath("schemas/booking.json"));
        api.delete(id, token);
    }
}
