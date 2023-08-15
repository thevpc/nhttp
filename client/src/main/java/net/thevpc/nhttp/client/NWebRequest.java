package net.thevpc.nhttp.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.thevpc.nuts.web.NHttpMethod;
import net.thevpc.nuts.web.NHttpUrlEncoder;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

public class NWebRequest {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private String url;
    private NHttpMethod method;
    private Map<String, List<String>> headers;
    private Map<String, List<String>> parameters;
    private NWebData body;
    private boolean oneWay;
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

    public NWebRequest setUrl(String url, Object... vars) {
        NAssert.requireNonNull(url, "url");
        NAssert.requireNonNull(vars, "vars");
        NStringBuilder sb = new NStringBuilder();
        char[] charArray = url.toCharArray();
        char last = '\0';
        int index = 0;
        boolean inParams = false;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (c) {
                case '?': {
                    inParams = true;
                    last = '?';
                    sb.append(c);
                    break;
                }
                case '/': {
                    if (inParams) {
                        sb.append(c);
                    } else {
                        if (sb.endsWith(":/")) {
                            // okkay
                        } else if (sb.endsWith('/')) {
                            // ignore
                        } else {
                            sb.append('/');
                            last = c;
                        }
                    }
                    break;
                }
                case '{': {
                    if (inParams) {
                        sb.append(c);
                    } else {
                        if (i + 1 < charArray.length) {
                            switch (charArray[i + 1]) {
                                case '}': {
                                    last = 's';
                                    if(index>=vars.length){
                                        throw new IllegalArgumentException(NMsg.ofC("missing var at index %s in %s",index,url).toString());
                                    }
                                    if (!NBlankable.isBlank(vars[index])) {
                                        sb.append(NHttpUrlEncoder.encodeObject(vars[index]));
                                    } else {
                                        if (!sb.endsWith("://") && sb.endsWith('/')) {
                                            sb.removeLast();
                                        }
                                    }
                                    i++;
                                    index++;
                                    break;
                                }
                                default: {
                                    sb.append('{').append(charArray[i + 1]);
                                    i++;
                                    last = 'a';
                                    break;
                                }
                            }
                        } else {
                            sb.append(c);
                            last = 'a';
                        }
                    }
                    break;
                }
                case '%': {
                    if (inParams) {
                        sb.append(c);
                    } else {
                        if (i + 1 < charArray.length) {
                            switch (charArray[i + 1]) {
                                case 's': {
                                    last = 's';
                                    if(index>=vars.length){
                                        throw new IllegalArgumentException(NMsg.ofC("missing var at index %s in %s",index,url).toString());
                                    }
                                    if (!NBlankable.isBlank(vars[index])) {
                                        sb.append(NHttpUrlEncoder.encodeObject(vars[index]));
                                    } else {
                                        if (!sb.endsWith("://") && sb.endsWith('/')) {
                                            sb.removeLast();
                                        }
                                    }
                                    i++;
                                    index++;
                                    break;
                                }
                                default: {
                                    sb.append('%').append(charArray[i + 1]);
                                    i++;
                                    last = 'a';
                                    break;
                                }
                            }
                        } else {
                            sb.append(c);
                            last = 'a';
                        }
                    }
                    break;
                }
                default: {
                    sb.append(c);
                    last = 'a';
                }
            }
        }
        this.url = sb.toString();
        return this;
    }

    public NWebRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public NHttpMethod getMethod() {
        return method;
    }

    public NWebRequest setMethod(NHttpMethod method) {
        this.method = method;
        return this;
    }

    public NWebRequest get() {
        return setMethod(NHttpMethod.GET);
    }

    public NWebRequest post() {
        return setMethod(NHttpMethod.POST);
    }

    public NWebRequest patch() {
        return setMethod(NHttpMethod.PATCH);
    }

    public NWebRequest options() {
        return setMethod(NHttpMethod.OPTIONS);
    }

    public NWebRequest put() {
        return setMethod(NHttpMethod.PUT);
    }

    public NWebRequest delete() {
        return setMethod(NHttpMethod.DELETE);
    }

    public String getHeader(String name) {
        if (headers != null) {
            List<String> values = headers.get(name);
            if (values != null) {
                for (String value : values) {
                    return value;
                }
            }
        }
        return null;
    }

    public List<String> getHeaders(String name) {
        List<String> all = new ArrayList<>();
        if (headers != null) {
            List<String> values = headers.get(name);
            if (values != null) {
                all.addAll(values);
            }
        }
        return all;
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

    public NWebData getBody() {
        return body;
    }

    public NWebRequest setJsonBody(Object body) {
        if (body == null) {
            this.body = null;
        } else {
            this.body = NWebData.of(GSON.toJson(body).getBytes());
        }
        setContentType("application/json");
        return this;
    }

    public NWebRequest setBody(byte[] body) {
        this.body = body == null ? null : NWebData.of(body);
        return this;
    }

    public NWebRequest setBody(NWebData body) {
        this.body = body;
        return this;
    }

    public NWebRequest setBody(NPath body) {
        this.body = body == null ? null : NWebData.of(body);
        return this;
    }

    public NWebRequest setContentLanguage(String contentLanguage) {
        return setHeader("Content-Language", contentLanguage);
    }

    public NWebRequest setAuthorizationBearer(String authorizationBearer) {
        authorizationBearer = NStringUtils.trimToNull(authorizationBearer);
        if (authorizationBearer != null) {
            authorizationBearer = "Bearer " + authorizationBearer;
        }
        return setAuthorization(authorizationBearer);
    }

    public NWebRequest setAuthorization(String authorization) {
        return setHeader("Authorization", NStringUtils.trimToNull(authorization));
    }

    public String getAuthorization() {
        return getHeader("Authorization");
    }

    public String getAuthorizationBearer() {
        String b = getHeader("Authorization");
        if (b != null && b.toLowerCase().startsWith("bearer ")) {
            return b.substring("bearer ".length()).trim();
        }
        return b;
    }

    public String getContentLanguage() {
        return getHeader("Content-Language");
    }

    public String getContentType() {
        return getHeader("Content-Type");
    }

    public NWebRequest setContentTypeForm() {
        return setContentType("application/x-www-form-urlencoded");
    }

    public NWebRequest setContentType(String contentType) {
        return setHeader("Content-Type", contentType);
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

