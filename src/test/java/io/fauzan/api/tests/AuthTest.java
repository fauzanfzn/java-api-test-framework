package io.fauzan.api.tests;

import io.fauzan.api.config.ApiConfig;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Booking API")
@Feature("Authentication")
class AuthTest extends LiveApiTest {

    @Test
    void valid_credentials_return_a_token() {
        Response response = api.createToken(ApiConfig.username(), ApiConfig.password());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("token")).matches("[a-z0-9]{10,}");
    }

    @ParameterizedTest(name = "[{index}] user=''{0}'' password=''{1}'' is rejected")
    @CsvSource(value = {
            "admin, wrong-password",
            "unknown-user, password123",
            "'', ''"
    }, emptyValue = "")
    void invalid_credentials_never_issue_a_token(String user, String password) {
        Response response = api.createToken(user, password);

        assertThat(response.jsonPath().getString("token")).isNull();
        assertThat(response.jsonPath().getString("reason")).isEqualTo("Bad credentials");
    }
}
