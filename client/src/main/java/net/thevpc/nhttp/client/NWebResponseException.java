package net.thevpc.nhttp.client;

public class NWebResponseException extends RuntimeException {
    public int code;
    public String responseMessage;
    public String userMessage;

    public NWebResponseException(int code, String responseMessage, String userMessage) {
        super(userMessage != null ? userMessage : responseMessage);
        this.code = code;
        this.responseMessage = responseMessage;
        this.userMessage = userMessage;
    }

    public int getCode() {
        return code;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
