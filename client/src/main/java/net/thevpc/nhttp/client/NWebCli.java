package net.thevpc.nhttp.client;

import net.thevpc.nhttp.commons.HttpMethod;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIOUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class NWebCli {
    private String prefix;
    private Function<NWebResponse, NWebResponse> responsePostProcessor;
    private Integer readTimeout;
    private Integer connectTimeout;

    public NWebCli(String prefix) {
        this.prefix = prefix;
    }

    public NWebCli() {
    }

    public Function<NWebResponse, NWebResponse> getResponsePostProcessor() {
        return responsePostProcessor;
    }


    public NWebCli setResponsePostProcessor(Function<NWebResponse, NWebResponse> responsePostProcessor) {
        this.responsePostProcessor = responsePostProcessor;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public NWebCli setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public NWebResponse run(NWebRequest r) {
        NAssert.requireNonNull(r, "request");
        NAssert.requireNonNull(r.getMethod(), "method");
        HttpMethod method = r.getMethod();
        String p = r.getUrl();
        StringBuilder u = new StringBuilder();
        if (prefix == null || p.startsWith("http:") || p.startsWith("https:")) {
            u.append(p);
        } else {
            if (p.isEmpty() || p.equals("/")) {
                u.append(prefix);
            } else {
                if (!p.startsWith("/") && !prefix.endsWith("/")) {
                    u.append(prefix).append("/").append(p);
                } else {
                    u.append(prefix).append(p);
                }
            }
        }
        String bu = u.toString().trim();
        if (bu.isEmpty() || bu.equals("/")) {
            throw new IllegalArgumentException("missing url : " + bu);
        }
        if (
                !bu.startsWith("http://")
                        && !bu.startsWith("https://")
        ) {
            throw new IllegalArgumentException("unsupported url : " + bu);
        }

        if (r.getParameters() != null && r.getParameters().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> e : r.getParameters().entrySet()) {
                String k = e.getKey();
                List<String> values = e.getValue();
                if (values != null && values.size() > 0) {
                    for (String v : values) {
                        if (sb.length() > 0) {
                            sb.append("&");
                        }
                        try {
                            sb.append(URLEncoder.encode(k, StandardCharsets.UTF_8.toString()))
                                    .append("=")
                                    .append(URLEncoder.encode(v, StandardCharsets.UTF_8.toString()))
                            ;
                        } catch (UnsupportedEncodingException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            if (sb.length() < 0) {
                if (u.indexOf("?") >= 0) {
                    u.append("&").append(sb);
                } else {
                    u.append("?").append(sb);
                }
            }
        }
        try {
            URL h = new URL(u.toString());
            HttpURLConnection uc = null;
            try {
                uc = (HttpURLConnection) h.openConnection();

                Integer readTimeout1 = r.getReadTimeout();
                if (readTimeout1 == null) {
                    readTimeout1 = getReadTimeout();
                }
                if (readTimeout1 != null) {
                    uc.setReadTimeout(readTimeout1);
                }

                Integer connectTimeout1 = r.getConnectTimeout();
                if (connectTimeout1 == null) {
                    connectTimeout1 = getConnectTimeout();
                }

                if (connectTimeout1 != null) {
                    uc.setConnectTimeout(connectTimeout1);
                }
                Map<String, List<String>> headers = new LinkedHashMap<>();
                Map<String, List<String>> rHeaders = r.getHeaders();
                if (rHeaders != null) {
                    for (Map.Entry<String, List<String>> e : rHeaders.entrySet()) {
                        if (e.getKey() != null && e.getValue() != null) {
                            headers.computeIfAbsent(e.getKey(), g -> new ArrayList<>())
                                    .addAll(e.getValue());
                        }
                    }
                }
                for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                    for (String s : e.getValue()) {
                        uc.setRequestProperty(e.getKey(), s);
                    }
                }
                uc.setRequestMethod(method.toString());
                if (r.getAuthorizationBearer() != null) {
                    String basicAuth = "Bearer " + r.getAuthorizationBearer();
                    uc.setRequestProperty("Authorization", basicAuth);
                }
                if (r.getContentType() != null) {
                    uc.setRequestProperty("Content-Type", r.getContentType());
                }
                if (r.getContentLanguage() != null) {
                    uc.setRequestProperty("Content-Language", r.getContentLanguage());
                }
                uc.setUseCaches(false);
                uc.setDoInput(true);
                uc.setDoOutput(false);
                byte[] requestBody = r.getBody();
                if (requestBody != null && requestBody.length > 0) {
                    uc.setRequestProperty("Content-Length", "" + requestBody.length);
                    uc.getOutputStream().write(requestBody);
                }
                byte[] bytes = null;
                if (!r.isOneWay()) {
                    bytes = NIOUtils.readBytes(uc.getInputStream());
                }
                NWebResponse httpResponse = new NWebResponse(
                        uc.getResponseCode(),
                        uc.getResponseMessage(),
                        uc.getHeaderFields(),
                        bytes
                );
                if (responsePostProcessor != null) {
                    NWebResponse newResp = responsePostProcessor.apply(httpResponse);
                    if (newResp != null) {
                        httpResponse = newResp;
                    }
                }
                return httpResponse;
            } finally {
                if (uc != null) {
                    try {
                        uc.disconnect();
                    } catch (Exception e) {
                        //
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public NWebCli setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public NWebCli setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
}
