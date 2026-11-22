package io.fauzan.api.tests;

import io.fauzan.api.data.BookingFactory;
import io.fauzan.api.model.Booking;
import io.fauzan.api.model.CreatedBooking;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Booking API")
@Feature("Booking lifecycle (CRUD)")
class BookingLifecycleTest extends LiveApiTest {

    @Test
    void booking_can_be_created_read_updated_patched_and_deleted() {
        Booking original = BookingFactory.valid();

        int id = Allure.step("Create booking", () -> {
            Response response = api.create(original);
            assertThat(response.statusCode()).isEqualTo(200);
            CreatedBooking created = response.as(CreatedBooking.class);
            assertThat(created.booking()).usingRecursiveComparison().isEqualTo(original);
            return created.bookingid();
        });

        Allure.step("Read it back", () -> {
            Booking stored = api.get(id).then().statusCode(200).extract().as(Booking.class);
            assertThat(stored).usingRecursiveComparison().isEqualTo(original);
        });

        Booking updated = original.withFirstname("Updated").withTotalprice(999);
        Allure.step("Replace it with PUT", () -> {
            Booking body = api.update(id, updated, token).then().statusCode(200).extract().as(Booking.class);
            assertThat(body).usingRecursiveComparison().isEqualTo(updated);
        });

        Allure.step("Change one field with PATCH", () -> {
            Booking body = api.patch(id, Map.of("additionalneeds", "Late checkout"), token)
                    .then().statusCode(200).extract().as(Booking.class);
            assertThat(body.additionalneeds()).isEqualTo("Late checkout");
            assertThat(body.firstname()).as("PATCH must not touch other fields").isEqualTo("Updated");
        });

        Allure.step("Delete it", () -> assertThat(api.delete(id, token).statusCode()).isEqualTo(201));

        Allure.step("It is gone", () -> assertThat(api.get(id).statusCode()).isEqualTo(404));
    }

    @Test
    void new_booking_is_findable_by_guest_name() {
        Booking booking = BookingFactory.valid();
        int id = api.create(booking).then().statusCode(200).extract().path("bookingid");

        Response search = api.search(Map.of("firstname", booking.firstname(), "lastname", booking.lastname()));

        assertThat(search.statusCode()).isEqualTo(200);
        assertThat(search.jsonPath().getList("bookingid", Integer.class)).contains(id);
        api.delete(id, token);
    }
}
