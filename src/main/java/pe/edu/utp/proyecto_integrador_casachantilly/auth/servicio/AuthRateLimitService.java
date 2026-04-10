package pe.edu.utp.proyecto_integrador_casachantilly.auth.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.utp.proyecto_integrador_casachantilly.comun.excepcion.TooManyRequestsException;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AuthRateLimitService {

    private static final class Bucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStartEpochMs;
        private volatile long blockedUntilEpochMs;
    }

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> resetBuckets = new ConcurrentHashMap<>();

    @Value("${app.auth.login.max-attempts:5}")
    private int loginMaxAttempts;
    @Value("${app.auth.login.window-seconds:300}")
    private int loginWindowSeconds;
    @Value("${app.auth.login.block-seconds:900}")
    private int loginBlockSeconds;

    @Value("${app.auth.reset.max-attempts:5}")
    private int resetMaxAttempts;
    @Value("${app.auth.reset.window-seconds:900}")
    private int resetWindowSeconds;
    @Value("${app.auth.reset.block-seconds:1800}")
    private int resetBlockSeconds;

    public void assertLoginAllowed(String key) {
        assertAllowed(loginBuckets, "LOGIN", key, loginWindowSeconds, loginBlockSeconds);
    }

    public void recordLoginFailure(String key) {
        registerFailure(loginBuckets, key, loginMaxAttempts, loginWindowSeconds, loginBlockSeconds);
    }

    public void clearLoginFailures(String key) {
        loginBuckets.remove(key);
    }

    public void consumeResetRequest(String key) {
        registerFailure(resetBuckets, key, resetMaxAttempts, resetWindowSeconds, resetBlockSeconds);
    }

    private void assertAllowed(Map<String, Bucket> buckets, String action, String key, int windowSeconds, int blockSeconds) {
        long now = Instant.now().toEpochMilli();
        Bucket bucket = buckets.computeIfAbsent(key, k -> {
            Bucket b = new Bucket();
            b.windowStartEpochMs = now;
            return b;
        });
        if (bucket.blockedUntilEpochMs > now) {
            long waitSeconds = Math.max(1, (bucket.blockedUntilEpochMs - now) / 1000);
            throw new TooManyRequestsException("Demasiados intentos de " + action + ". Reintenta en " + waitSeconds + " segundos.");
        }
        resetWindowIfNeeded(bucket, now, windowSeconds);
    }

    private void registerFailure(Map<String, Bucket> buckets, String key, int maxAttempts, int windowSeconds, int blockSeconds) {
        long now = Instant.now().toEpochMilli();
        Bucket bucket = buckets.computeIfAbsent(key, k -> {
            Bucket b = new Bucket();
            b.windowStartEpochMs = now;
            return b;
        });
        resetWindowIfNeeded(bucket, now, windowSeconds);
        int count = bucket.count.incrementAndGet();
        if (count >= maxAttempts) {
            bucket.blockedUntilEpochMs = now + (blockSeconds * 1000L);
        }
    }

    private void resetWindowIfNeeded(Bucket bucket, long now, int windowSeconds) {
        long windowMs = windowSeconds * 1000L;
        if (now - bucket.windowStartEpochMs > windowMs) {
            bucket.windowStartEpochMs = now;
            bucket.count.set(0);
            bucket.blockedUntilEpochMs = 0L;
        }
    }
}
