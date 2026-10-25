/*
Class Design: Rate Limiter

1. RateLimitService
   - Main service that checks whether a request is allowed.
   - Uses a RateLimiter strategy to make the decision.

2. RateLimiter
   - Interface defining the allowRequest() operation.

3. SlidingWindowRateLimiter
   - Implements the sliding window log algorithm.
   - Maintains a queue of request timestamps for each user.
   - Removes expired timestamps before checking the limit.

4. RateLimitConfig
   - Stores maximum requests and window duration.

5. RateLimitResult
   - Represents whether the request was allowed or rejected.

6. Main Flow
   - A request arrives for a user.
   - RateLimitService delegates to the configured limiter.
   - The limiter removes expired requests and checks the count.
   - The service returns ALLOWED or RATE_LIMIT_EXCEEDED.

Design Pattern: Strategy
Data Structures: HashMap + Queue
*/

import java.util.*;

interface RateLimiter {
    boolean allowRequest(String userId);
}

class RateLimitConfig {
    private final int maxRequests;
    private final long windowSizeMillis;

    public RateLimitConfig(int maxRequests, long windowSizeMillis) {
        if (maxRequests <= 0 || windowSizeMillis <= 0) {
            throw new IllegalArgumentException("Invalid rate limit configuration");
        }

        this.maxRequests = maxRequests;
        this.windowSizeMillis = windowSizeMillis;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public long getWindowSizeMillis() {
        return windowSizeMillis;
    }
}

class SlidingWindowRateLimiter implements RateLimiter {
    private final RateLimitConfig config;
    private final Map<String, Queue<Long>> requestHistory;

    public SlidingWindowRateLimiter(RateLimitConfig config) {
        this.config = config;
        this.requestHistory = new HashMap<>();
    }

    @Override
    public synchronized boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();

        Queue<Long> timestamps = requestHistory.computeIfAbsent(
            userId, key -> new LinkedList<>()
        );

        while (!timestamps.isEmpty()
                && currentTime - timestamps.peek()
                    >= config.getWindowSizeMillis()) {
            timestamps.poll();
        }

        if (timestamps.size() >= config.getMaxRequests()) {
            return false;
        }

        timestamps.offer(currentTime);
        return true;
    }
}

enum RateLimitStatus {
    ALLOWED,
    RATE_LIMIT_EXCEEDED
}

class RateLimitResult {
    private final RateLimitStatus status;
    private final String message;

    public RateLimitResult(RateLimitStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public RateLimitStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return status + ": " + message;
    }
}

class RateLimitService {
    private final RateLimiter rateLimiter;

    public RateLimitService(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public RateLimitResult processRequest(String userId) {
        if (rateLimiter.allowRequest(userId)) {
            return new RateLimitResult(
                RateLimitStatus.ALLOWED,
                "Request accepted for " + userId
            );
        }

        return new RateLimitResult(
            RateLimitStatus.RATE_LIMIT_EXCEEDED,
            "Too many requests for " + userId
        );
    }
}

public class Main {
    public static void main(String[] args) {
        RateLimitConfig config = new RateLimitConfig(3, 10_000);

        RateLimiter limiter = new SlidingWindowRateLimiter(config);
        RateLimitService service = new RateLimitService(limiter);

        System.out.println(service.processRequest("user1"));
        System.out.println(service.processRequest("user1"));
        System.out.println(service.processRequest("user1"));
        System.out.println(service.processRequest("user1"));

        System.out.println(service.processRequest("user2"));
    }
}