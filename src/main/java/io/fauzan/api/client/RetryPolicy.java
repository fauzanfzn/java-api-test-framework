package io.fauzan.api.client;

import io.restassured.response.Response;

import java.util.function.Supplier;

/**
 * Retries idempotent calls on transient failures (5xx or network errors).
 * Shared staging environments are flaky; retrying with back-off keeps the suite
 * honest about real defects instead of reporting infrastructure noise.
 */
public final class RetryPolicy {
    private final int maxAttempts;
    private final long baseDelayMs;

    public RetryPolicy(int maxAttempts, long baseDelayMs) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.baseDelayMs = baseDelayMs;
    }

    public Response execute(Supplier<Response> call) {
        RuntimeException lastError = null;
        Response last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                last = call.get();
                if (last.statusCode() < 500) return last;
            } catch (RuntimeException e) {
                lastError = e;
            }
            sleep(baseDelayMs * attempt);
        }
        if (last != null) return last;
        throw lastError;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
