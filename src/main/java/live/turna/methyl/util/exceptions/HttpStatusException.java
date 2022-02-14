package live.turna.methyl.util.exceptions;

public class HttpStatusException extends Exception {
    private final int httpStatus;

    public HttpStatusException(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }
}
