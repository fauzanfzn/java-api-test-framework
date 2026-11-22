package io.fauzan.api.config;

import java.time.Duration;

/** Central configuration. Every value can be overridden by an environment variable or -D system property. */
public final class ApiConfig {
    private ApiConfig() {}

    public static String baseUri() {
        return value("API_BASE_URI", "https://restful-booker.herokuapp.com");
    }

    public static String username() { return value("API_USERNAME", "admin"); }

    public static String password() { return value("API_PASSWORD", "password123"); }

    public static Duration timeout() {
        return Duration.ofSeconds(Long.parseLong(value("API_TIMEOUT_SECONDS", "30")));
    }

    public static int maxAttempts() { return Integer.parseInt(value("API_MAX_ATTEMPTS", "3")); }

    private static String value(String key, String fallback) {
        String prop = System.getProperty(key);
        if (prop != null && !prop.isBlank()) return prop;
        String env = System.getenv(key);
        return env != null && !env.isBlank() ? env : fallback;
    }
}
