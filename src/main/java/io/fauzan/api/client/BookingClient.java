package io.fauzan.api.client;

import io.fauzan.api.config.ApiConfig;
import io.fauzan.api.model.Booking;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

/** Thin API client: one method per endpoint, no assertions. Tests decide what "correct" means. */
public class BookingClient {
    private final RequestSpecification spec;
    private final RetryPolicy retry;

    public BookingClient() {
        this(ApiConfig.baseUri(), new RetryPolicy(ApiConfig.maxAttempts(), 500));
    }

    public BookingClient(String baseUri, RetryPolicy retry) {
        int timeoutMs = (int) ApiConfig.timeout().toMillis();
        this.spec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setConfig(RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutMs)
                        .setParam("http.socket.timeout", timeoutMs)))
                .addFilter(new AllureRestAssured())
                .build();
        this.retry = retry;
    }

    public Response ping() {
        return retry.execute(() -> given(spec).get("/ping"));
    }

    public Response createToken(String username, String password) {
        return given(spec).body(Map.of("username", username, "password", password)).post("/auth");
    }

    public String token() {
        return retry.execute(() -> createToken(ApiConfig.username(), ApiConfig.password()))
                .then().extract().path("token");
    }

    public Response create(Booking booking) {
        return given(spec).body(booking).post("/booking");
    }

    public Response get(int id) {
        return retry.execute(() -> given(spec).get("/booking/{id}", id));
    }

    public Response search(Map<String, ?> filters) {
        return retry.execute(() -> given(spec).queryParams(filters).get("/booking"));
    }

    public Response update(int id, Booking booking, String token) {
        return withToken(token).body(booking).put("/booking/{id}", id);
    }

    public Response patch(int id, Map<String, ?> fields, String token) {
        return withToken(token).body(fields).patch("/booking/{id}", id);
    }

    public Response delete(int id, String token) {
        return withToken(token).delete("/booking/{id}", id);
    }

    private RequestSpecification withToken(String token) {
        RequestSpecification request = given(spec);
        return token == null ? request : request.cookie("token", token);
    }
}
