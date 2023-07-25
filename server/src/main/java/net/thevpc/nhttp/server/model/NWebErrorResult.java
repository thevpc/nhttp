package net.thevpc.nhttp.server.model;

public class NWebErrorResult {
    private boolean error;
    private String message;

    public NWebErrorResult() {
    }
    public NWebErrorResult(String message) {
        this.error = true;
        this.message = message;
    }

    public boolean isError() {
        return error;
    }

    public NWebErrorResult setError(boolean error) {
        this.error = error;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public NWebErrorResult setMessage(String message) {
        this.message = message;
        return this;
    }
}
