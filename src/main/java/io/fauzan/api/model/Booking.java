package io.fauzan.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Booking(
        String firstname,
        String lastname,
        Integer totalprice,
        Boolean depositpaid,
        BookingDates bookingdates,
        String additionalneeds) {

    public Booking withFirstname(String value) {
        return new Booking(value, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }

    public Booking withTotalprice(Integer value) {
        return new Booking(firstname, lastname, value, depositpaid, bookingdates, additionalneeds);
    }
}
