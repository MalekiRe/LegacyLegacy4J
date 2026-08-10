package wily.legacy125.input;

/** Frame-rate-independent repeat gate for held controller actions. */
public final class RepeatCooldown125 {
    private final long intervalMillis;
    private long nextAllowedMillis;

    public RepeatCooldown125(long intervalMillis) {
        if (intervalMillis < 0L) throw new IllegalArgumentException("intervalMillis");
        this.intervalMillis = intervalMillis;
    }

    public boolean shouldFire(boolean down, long nowMillis) {
        if (!down || nowMillis < nextAllowedMillis) return false;
        nextAllowedMillis = nowMillis + intervalMillis;
        return true;
    }
}
