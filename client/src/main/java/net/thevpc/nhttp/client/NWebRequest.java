package net.thevpc.nhttp.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.thevpc.nhttp.commons.HttpMethod;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.io.NPath;

import java.util.*;

public class NWebRequest {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String url;
    private HttpMethod method;
    private Map<String, List<String>> headers;
    private Map<String, List<String>> parameters;
    private byte[] body;
    private boolean oneWay;
    private String authorizationBearer;
    private String contentLanguage;
    private String contentType;
    private Integer readTimeout;
    private Integer connectTimeout;

    public boolean isOneWay() {
        return oneWay;
    }

    public NWebRequest setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NWebRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public NWebRequest setMethod(HttpMethod method) {
        this.method = method;
        return this;
    }

    public NWebRequest get() {
        return setMethod(HttpMethod.GET);
    }

    public NWebRequest post() {
        return setMethod(HttpMethod.POST);
    }

    public NWebRequest patch() {
        return setMethod(HttpMethod.PATCH);
    }

    public NWebRequest options() {
        return setMethod(HttpMethod.OPTIONS);
    }

    public NWebRequest put() {
        return setMethod(HttpMethod.PUT);
    }

    public NWebRequest delete() {
        return setMethod(HttpMethod.DELETE);
    }

    public String getAuthorizationBearer() {
        return authorizationBearer;
    }

    public NWebRequest setAuthorizationBearer(String authorizationBearer) {
        this.authorizationBearer = authorizationBearer;
        return this;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public NWebRequest setHeaders(Map<String, List<String>> headers) {
        this.headers = headers == null ? new HashMap<>() : headers;
        return this;
    }

    public NWebRequest addHeaders(Map<String, List<String>> headers) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                String k = e.getKey();
                if (k != null && e.getValue() != null && e.getValue().size() > 0) {
                    for (String v : e.getValue()) {
                        addHeader(k, v);
                    }
                }
            }
        }
        return this;
    }

    public NWebRequest addParameters(Map<String, List<String>> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, List<String>> e : parameters.entrySet()) {
                String k = e.getKey();
                if (k != null && e.getValue() != null && e.getValue().size() > 0) {
                    for (String v : e.getValue()) {
                        addHeader(k, v);
                    }
                }
            }
        }
        return this;
    }

    public NWebRequest setPropsFileHeaders(NPath path) {
        setHeaders(_mapFromPropsFile(path));
        return this;
    }

    public NWebRequest addPropsFileHeaders(NPath path) {
        addHeaders(_mapFromPropsFile(path));
        return this;
    }

    public NWebRequest addJsonFileHeaders(NPath path) {
        Map<String, List<String>> newHeaders = _mapFromJsonFile(path);
        addHeaders(newHeaders);
        return this;
    }

    public NWebRequest setJsonFileHeaders(NPath path) {
        setHeaders(_mapFromJsonFile(path));
        return this;
    }

    public NWebRequest setPropsFileParameters(NPath path) {
        setParameters(_mapFromPropsFile(path));
        return this;
    }

    public NWebRequest addPropsFileParameters(NPath path) {
        addParameters(_mapFromPropsFile(path));
        return this;
    }

    public NWebRequest addJsonFileParameters(NPath path) {
        Map<String, List<String>> newHeaders = _mapFromJsonFile(path);
        addParameters(newHeaders);
        return this;
    }

    public NWebRequest setJsonFileParameters(NPath path) {
        setParameters(_mapFromJsonFile(path));
        return this;
    }

    private static Map<String, List<String>> _mapFromPropsFile(NPath path) {
        Map<String, List<String>> m = new LinkedHashMap<>();
        path.getLines().forEach(x -> {
            x = x.trim();
            if (!x.startsWith("#")) {
                NArg a = NArg.of(x);
                m.computeIfAbsent(a.key(), r -> new ArrayList<>()).add(String.valueOf(a.key()));
            }
        });
        return m;
    }

    private static Map<String, List<String>> _mapFromJsonFile(NPath path) {
        Map<String, Object> map = GSON.fromJson(
                path.getReader(),
                Map.class
        );
        Map<String, List<String>> newHeaders = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            if (v instanceof String) {
                newHeaders.computeIfAbsent(k, r -> new ArrayList<>()).add((String) v);
            } else if (v instanceof List) {
                for (Object o : ((List) v)) {
                    newHeaders.computeIfAbsent(k, r -> new ArrayList<>()).add(String.valueOf(o));
                }
            } else if (v instanceof Object[]) {
                for (Object o : ((Object[]) v)) {
                    newHeaders.computeIfAbsent(k, r -> new ArrayList<>()).add(String.valueOf(o));
                }
            }
        }
        return newHeaders;
    }


    public NWebRequest addHeader(String name, String value) {
        if (name != null && value != null) {
            if (this.headers == null) {
                this.headers = new LinkedHashMap<>();
            }
            this.headers.computeIfAbsent(name, s -> new ArrayList<>()).add(value);
        }
        return this;
    }

    public NWebRequest setHeader(String name, String value) {
        if (name != null) {
            if (value != null) {
                if (this.headers == null) {
                    this.headers = new LinkedHashMap<>();
                }
                List<String> list = this.headers.computeIfAbsent(name, s -> new ArrayList<>());
                list.clear();
                list.add(value);
            } else {
                if (this.headers != null) {
                    List<String> list = this.headers.computeIfAbsent(name, s -> new ArrayList<>());
                    list.clear();
                }
            }
        }
        return this;
    }


    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    public NWebRequest setParameters(Map<String, List<String>> parameters) {
        this.parameters = parameters == null ? new LinkedHashMap<>() : parameters;
        return this;
    }

    public NWebRequest addParameter(String name, String value) {
        if (value != null) {
            if (this.parameters == null) {
                this.parameters = new LinkedHashMap<>();
            }
            this.parameters.computeIfAbsent(name, s -> new ArrayList<>()).add(value);
        }
        return this;
    }

    public NWebRequest setParameter(String name, String value) {
        if (value != null) {
            if (this.parameters == null) {
                this.parameters = new LinkedHashMap<>();
            }
            List<String> list = this.parameters.computeIfAbsent(name, s -> new ArrayList<>());
            list.clear();
            list.add(value);
        } else {
            if (this.parameters != null) {
                List<String> list = this.parameters.computeIfAbsent(name, s -> new ArrayList<>());
                list.clear();
            }
        }
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public NWebRequest setJsonBody(Object body) {
        if (body == null) {
            this.body = null;
        } else {
            this.body = GSON.toJson(body).getBytes();
        }
        return this;
    }

    public NWebRequest setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public NWebRequest setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public NWebRequest setContentTypeForm() {
        return setContentType("application/x-www-form-urlencoded");
    }

    public NWebRequest setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public NWebRequest setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public NWebRequest setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
}

