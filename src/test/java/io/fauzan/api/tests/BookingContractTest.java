package io.fauzan.api.tests;

import io.fauzan.api.data.BookingFactory;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Epic("Booking API")
@Feature("Response contracts (JSON Schema)")
class BookingContractTest extends LiveApiTest {

    @Test
    void create_response_matches_contract() {
        api.create(BookingFactory.valid())
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/created-booking.json"))
                .body("booking", matchesJsonSchemaInClasspath("schemas/booking.json"));
    }

    @Test
    void get_response_matches_contract() {
        int id = api.create(BookingFactory.valid()).then().extract().path("bookingid");
        api.get(id).then().statusCode(200).body(matchesJsonSchemaInClasspath("schemas/booking.json"));
        api.delete(id, token);
    }
}
