package io.fauzan.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreatedBooking(int bookingid, Booking booking) {}
