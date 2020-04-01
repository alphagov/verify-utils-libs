package uk.gov.ida.common;

public class ThreadSleepExponentialBackOffProvider {
    private final long base;
    private final double multiplier;

    public ThreadSleepExponentialBackOffProvider(long base, double multiplier) {
        this.base = base;
        this.multiplier = multiplier;
    }

    public void backOffWait(int retryCounter) throws InterruptedException {
        Thread.sleep((base * Long.parseLong(Double.toString(Math.pow(retryCounter, multiplier)))));
    }
}
