package io.fauzan.api.data;

import io.fauzan.api.model.Booking;
import io.fauzan.api.model.BookingDates;
import net.datafaker.Faker;

import java.time.LocalDate;

/** Builds realistic, unique test data so parallel runs never collide on shared environments. */
public final class BookingFactory {
    private static final Faker FAKER = new Faker();

    private BookingFactory() {}

    public static Booking valid() {
        LocalDate checkin = LocalDate.now().plusDays(FAKER.number().numberBetween(1, 60));
        LocalDate checkout = checkin.plusDays(FAKER.number().numberBetween(1, 14));
        return new Booking(
                "QA" + FAKER.name().firstName().replaceAll("[^A-Za-z]", ""),
                FAKER.name().lastName().replaceAll("[^A-Za-z]", "") + System.nanoTime() % 100000,
                FAKER.number().numberBetween(50, 2000),
                FAKER.bool().bool(),
                new BookingDates(checkin.toString(), checkout.toString()),
                FAKER.options().option("Breakfast", "Late checkout", "Airport pickup", "None"));
    }
}
