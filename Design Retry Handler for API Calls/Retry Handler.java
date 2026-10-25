import java.util.concurrent.*;

interface ApiCall {
    boolean execute();
}

class RetryHandler {
    private int maxRetries;
    private long initialDelayMs;

    RetryHandler(int maxRetries, long initialDelayMs) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
    }

    public boolean executeWithRetry(ApiCall apiCall) {
        int attempt = 0;

        while (true) {
            try {
                if (apiCall.execute()) {
                    System.out.println("API call succeeded");
                    return true;
                }
            } catch (Exception e) {
                System.out.println("API call failed: " + e.getMessage());
            }

            if (attempt >= maxRetries) {
                System.out.println("Max retries exceeded");
                return false;
            }

            long delay = initialDelayMs * (1L << attempt);
            System.out.println("Retrying in " + delay + " ms");

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            attempt++;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        RetryHandler handler = new RetryHandler(3, 500);

        ApiCall apiCall = new ApiCall() {
            int failures = 0;

            @Override
            public boolean execute() {
                if (failures++ < 2) {
                    System.out.println("API call failed");
                    return false;
                }
                return true;
            }
        };

        boolean success = handler.executeWithRetry(apiCall);
        System.out.println("Final result: " + success);
    }
}