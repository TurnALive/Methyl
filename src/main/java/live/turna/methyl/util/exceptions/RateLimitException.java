package live.turna.methyl.util.exceptions;

public class RateLimitException extends Exception {
    private final int delay;

    public RateLimitException(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return this.delay;
    }
}
