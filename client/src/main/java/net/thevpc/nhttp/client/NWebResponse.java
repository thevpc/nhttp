package net.thevpc.nhttp.client;

import com.google.gson.Gson;

import java.io.*;
import java.util.List;
import java.util.Map;

public class NWebResponse {
    private int code;
    private String msg;
    private Map<String, List<String>> headers;
    private NWebData content;
    private String userMessage;

    public NWebResponse(int code, String msg, Map<String, List<String>> headers, NWebData content) {
        this.code = code;
        this.msg = msg;
        this.headers = headers;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public NWebData getContent() {
        return content;
    }

    public <K,V> Map<K, V> getContentMapAsJson() {
        return getContentAsJson(Map.class);
    }

    public <T> List<T> getContentArrayAsJson() {
        return getContentAsJson(List.class);
    }

    public <T> T getContentAsJson(Class<T> clz) {
        if (content == null) {
            return null;
        }
        try (InputStream in = content.stream()) {
            return new Gson().fromJson(new InputStreamReader(in), clz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean isError() {
        return code >= 400;
    }

    public NWebResponse failFast() {
        if (isError()) {
            throw new NWebResponseException(code, msg, userMessage);
        }
        return this;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public NWebResponse setUserMessage(String userMessage) {
        this.userMessage = userMessage;
        return this;
    }

    public String getContentType() {
        if (headers != null) {
            List<String> list = headers.get("Content-Type");
            if (list != null) {
                for (String s : list) {
                    if (s != null && !s.isEmpty()) {
                        return s;
                    }
                }
            }
        }
        return null;
    }
}
