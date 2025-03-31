package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.io.NInputSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormDataItem {
    private String name;
    private String filename;
    private String contentType;
    private Map<String, List<String>> properties;
    private Map<String, List<String>> headers = new HashMap<>();
    private NInputSource source;

    public String getName() {
        return name;
    }

    public FormDataItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public FormDataItem setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public FormDataItem setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Map<String, List<String>> getProperties() {
        return properties;
    }

    public FormDataItem setProperties(Map<String, List<String>> properties) {
        this.properties = properties;
        return this;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public FormDataItem setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    public NInputSource getSource() {
        return source;
    }

    public FormDataItem setSource(NInputSource source) {
        this.source = source;
        return this;
    }
}
