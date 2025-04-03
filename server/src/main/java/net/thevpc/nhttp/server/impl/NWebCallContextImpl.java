package net.thevpc.nhttp.server.impl;

import com.sun.net.httpserver.Headers;
import net.thevpc.nhttp.server.model.DefaultNWebContext;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.reserved.optional.NDetachedEmptyOptionalException;
import net.thevpc.nuts.reserved.optional.NDetachedErrorOptionalException;
import net.thevpc.nuts.reserved.optional.NEmptyOptionalException;
import net.thevpc.nuts.reserved.optional.NErrorOptionalException;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.web.NHttpCode;
import net.thevpc.nuts.web.NHttpMethod;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.thevpc.nhttp.server.api.*;
import net.thevpc.nhttp.server.error.*;
import net.thevpc.nhttp.server.model.NWebErrorResult;
import net.thevpc.nhttp.server.security.*;
import net.thevpc.nhttp.server.util.JsonUtils;
import sun.misc.IOUtils;

import java.io.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NWebCallContextImpl implements NWebCallContext {
    private static NStringMapFormat nStringMapFormat = NStringMapFormat.of("=", ";", "", false,NStringMapFormat.URL_ENCODER,NStringMapFormat.URL_DECODER);

    private HttpExchange httpExchange;
    private byte[] requestBody = null;
    private NHttpMethod method;
    private NWebUser user;
    private NWebToken token;
    private String[] pathParts;
    //    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private Map<String, List<String>> queryParams;
    private Map<String, FormDataItem> formData;
    private Map<String, List<String>> responseHeaders = new HashMap<>();
    private NHttpCode responseCode = NHttpCode.OK;
    private String responseMode = "bytes";
    private String contentType = null;
    private Object responseObject;
    private NWebCallContextImpl ctx;
    private boolean responseHeadersSent;
    int maxLineLength = 1024 * 1024;
    private NWebContext webContext;

    public NWebCallContextImpl(NWebContext webContext, HttpExchange httpExchange) {
        this.webContext = webContext;
        this.httpExchange = httpExchange;
        if (httpExchange != null) {
            this.pathParts = Arrays.stream(getPath().split("/")).filter(x -> x.length() > 0).toArray(String[]::new);
        } else {
            this.pathParts = new String[0];
        }
    }

    @Override
    public void initializeConfig() {
        NWebContext.Configurator c = ((DefaultNWebContext) webContext).getConfigurator();
        if (c != null) {
            c.initializeConfig();
        }
    }

    @Override
    public NWebContext getWebContext() {
        return webContext;
    }

    @Override
    public NWebCallContext addResponseHeader(String name, String value) {
        if (value != null) {
            List<String> strings = responseHeaders.computeIfAbsent(name, k -> new ArrayList<>());
            if (!strings.contains(value)) {
                strings.add(value);
            }
        }
        return this;
    }

    @Override
    public NWebCallContext setResponseHeader(String name, String value) {
        List<String> strings = responseHeaders.computeIfAbsent(name, k -> new ArrayList<>());
        strings.clear();
        if (value != null) {
            strings.add(value);
        }
        return this;
    }

    @Override
    public NHttpCode getResponseCode() {
        return responseCode;
    }

    @Override
    public NWebCallContext setResponseCode(NHttpCode responseCode) {
        this.responseCode = responseCode;
        return this;
    }


    public Map<String, List<String>> getRequestHeaders() {
        Headers rh = httpExchange.getRequestHeaders();
        Map<String, List<String>> map = new HashMap<>();
        for (Map.Entry<String, List<String>> r : rh.entrySet()) {
            map.put(r.getKey(), new ArrayList<>(r.getValue()));
        }
        return map;
    }

    @Override
    public URI getRequestURI() {
        return httpExchange.getRequestURI();
    }

    public HttpServer getServer() {
        return ((DefaultNWebContext) webContext).getServer();
    }

    @Override
    public String getFirstPath() {
        return pathParts.length > 0 ? pathParts[0] : "";
    }

    @Override
    public boolean isEmptyPath() {
        return pathParts.length == 0;
    }

    @Override
    public int getPathSize() {
        return pathParts.length;
    }

    @Override
    public String[] getPathParts() {
        return pathParts;
    }

    @Override
    public String getPathPart(int pos) {
        if (pos < 0 || pos >= pathParts.length) {
            return "";
        }
        return pathParts[pos];
    }

    @Override
    public <T> T getRequestBodyAs(Class<T> cl) {
        String bodyAsString = getRequestBodyAsString();
        if (bodyAsString.isEmpty()) {
            if (cl == String.class) {
                return (T) "";
            }
            return null;
        }
        return JsonUtils.fromJson(bodyAsString, cl);
    }

    @Override
    public <T> T getRequestBodyAs(Class<T> cl, NContentType contentType) {
        String bodyAsString = getRequestBodyAsString();
        if (bodyAsString.isEmpty()) {
            if (cl == String.class) {
                return (T) "";
            }
            return null;
        }
        return JsonUtils.fromJson(bodyAsString, cl, contentType);
    }

    @Override
    public String getRequestBodyAsString() {
        if (requestBody == null) {
            try {
                requestBody = NCp.of().from(httpExchange.getRequestBody()).getByteArrayResult();
            } catch (RuntimeException e) {
                requestBody = new byte[0];
                throw e;
            }
        }
        return new String(requestBody);
    }

    @Override
    public String getPath() {
        return httpExchange.getRequestURI().getPath();
    }

    public NWebHttpException wrapException(Throwable ex) {
        if (ex == null) {
            return new NWebHttpException("error", new NMsgCode("ERROR"), NHttpCode.BAD_REQUEST);
        }
        if (ex instanceof NWebHttpException) {
            return (NWebHttpException) ex;
        }
        NWebHttpException c = customWrapException(ex);
        if (c != null) {
            return c;
        }
        return wrapDefaultException(ex);
    }

    protected NWebHttpException customWrapException(Throwable ex) {
        return null;
    }

    private NWebHttpException wrapDefaultException(Throwable ex) {
        if (ex instanceof NWebHttpException) {
            return ((NWebHttpException) ex);
        } else if (ex instanceof NoSuchElementException) {
            return (new NWebHttpException(ex.getMessage(),
                    NMsgCodeAware.codeOf(ex).orElse(new NMsgCode("NotFound", ex.getMessage())), NHttpCode.NOT_FOUND));
        } else if (ex instanceof NWebUnauthorizedSecurityException) {
            return (new NWebHttpException(ex.getMessage(), NMsgCodeAware.codeOf(ex).get(), NHttpCode.UNAUTHORIZED));
        } else if (ex instanceof SecurityException) {
            return (new NWebHttpException(ex.getMessage(), NMsgCodeAware.codeOf(ex).get(), NHttpCode.FORBIDDEN));
        } else if (ex instanceof NMsgCodeException) {
            return (new NWebHttpException(ex.getMessage(), NMsgCodeAware.codeOf(ex).get(), NHttpCode.FORBIDDEN));
        } else if (ex instanceof NMsgCodeAware) {
            return (new NWebHttpException(ex.getMessage(), NMsgCodeAware.codeOf(ex).get(), NHttpCode.BAD_REQUEST));
        } else if (ex instanceof NErrorOptionalException) {
            return (new NWebHttpException(ex.getMessage(),
                    new NMsgCode("Error", ex.getMessage())
                    , NHttpCode.BAD_REQUEST));
        } else if (ex instanceof NDetachedErrorOptionalException) {
            return (new NWebHttpException(ex.getMessage(),
                    new NMsgCode("Error", ex.getMessage())
                    , NHttpCode.BAD_REQUEST));
        } else if (ex instanceof NDetachedEmptyOptionalException) {
            return (new NWebHttpException(ex.getMessage(),
                    new NMsgCode("Not Found", ex.getMessage())
                    , NHttpCode.NOT_FOUND));
        } else if (ex instanceof NEmptyOptionalException) {
            return (new NWebHttpException(ex.getMessage(),
                    new NMsgCode("Not Found", ex.getMessage())
                    , NHttpCode.NOT_FOUND));
        } else {
            NOptional<NMsgCode> codeOf = NMsgCodeAware.codeOf(ex);
            if (codeOf.isPresent()) {
                return (new NWebHttpException(ex.getMessage(), codeOf.get(), NHttpCode.BAD_REQUEST));
            } else {
                //ex.printStackTrace();
                return (new NWebHttpException(ex.getMessage(), new NMsgCode("Error"), NHttpCode.INTERNAL_SERVER_ERROR));
            }
        }
    }

    @Override
    public NWebCallContext setResponseContentType(String contentType) {
        return setResponseHeader("Content-Type", contentType);
    }

    @Override
    public NWebCallContext setErrorCode(NMsgCode errorCode) {
        if (errorCode != null) {
            String json = JsonUtils.toJson(errorCode);
            String b64 = Base64.getEncoder().encodeToString(json.getBytes());
            setResponseHeader("X-APP-ERROR", b64);
        } else {
            setResponseHeader("X-APP-ERROR", null);
        }
        return this;
    }

    @Override
    public NWebCallContext sendResponseHeaders() {
        for (Map.Entry<String, List<String>> e : responseHeaders.entrySet()) {
            String k = e.getKey();
            List<String> v = e.getValue();
            if (v.size() == 1) {
                httpExchange.getResponseHeaders().add(k, v.get(0));
            } else if (v.size() > 1) {
                // FIX ME LATER
                httpExchange.getResponseHeaders().add(k, v.get(0));
            }
        }
        return this;
    }


    public NWebCallContext sendResponseContent(byte[] bytes) {
        try {
            OutputStream os = httpExchange.getResponseBody();
            os.write(bytes);
            //os.close();
        } catch (IOException|NIOException|UncheckedIOException e) {
            throw new NMsgCodeException(new NMsgCode("IO.SendFailed"), NMsg.ofC("send byte failed : %s", e.toString()), e);
        }
        return this;
    }

    public NWebCallContext sendResponseContent(InputStream stream) {
        try {
            OutputStream os = httpExchange.getResponseBody();
            if (stream != null) {
                NIOUtils.copy(stream, os);
            }
            //os.close();
        } catch (NIOException|UncheckedIOException e) {
            throw new NMsgCodeException(new NMsgCode("IO.SendFailed"), NMsg.ofC("send byte failed : %s", e.toString()), e);
        }
        return this;
    }


    public OutputStream getResponseBody() {
        return httpExchange.getResponseBody();
    }

    @Override
    public NHttpMethod getMethod() {
        if (method == null) {
            String m = httpExchange.getRequestMethod();
            switch (NStringUtils.trim(m).toUpperCase()) {
                case "GET":
                    return method = NHttpMethod.GET;
                case "POST":
                    return method = NHttpMethod.POST;
                case "PUT":
                    return method = NHttpMethod.PUT;
                case "OPTIONS":
                    return method = NHttpMethod.OPTIONS;
                case "PATCH":
                    return method = NHttpMethod.PATCH;
                case "DELETE":
                    return method = NHttpMethod.DELETE;
                default:
                    return method = NHttpMethod.UNKNOWN;
            }
        }
        return method;
    }

    @Override
    public NWebCallContext requireAuth() {
        List<String> authorization = httpExchange.getRequestHeaders().get("Authorization");
        NWebUser user = null;
        NWebToken token = null;
        boolean someToken = false;
        if (authorization != null) {
            try {
                for (String s : authorization) {
                    if (s != null) {
                        if (s.toLowerCase().startsWith("bearer")) {
                            String yy = s.substring("bearer".length()).trim();
                            try {
                                user = authenticateWithAccessToken(yy);
                            } catch (Exception e) {
                                // just ignore...
                            }
                            if (user != null) {
                                break;
                            }
                        }
                    }
                }
            } catch (RuntimeException ex) {
                if (ex instanceof NMsgCodeAware) {
                    throw ex;
                }
                throw new NMsgCodeException(new NMsgCode("Security.AuthorizationFailed"), NMsg.ofPlain(ex.toString()), ex);
            } catch (Throwable ex) {
                throw new NMsgCodeException(new NMsgCode("Security.AuthorizationFailed"), NMsg.ofPlain(ex.toString()), ex);
            }
        }
        if (user == null) {
            if (someToken) {
                throw new NWebUnauthorizedSecurityException(new NMsgCode("Security.InvalidToken"), "invalid token");
            } else {
                throw new NWebUnauthorizedSecurityException(new NMsgCode("Security.MissingToken"), "missing token");
            }
        }
        trace(Level.INFO, NMsg.ofC("authenticated %s %s", user.getUserId(), user.getUserName()));
        setUser(user);
        setToken(token);
        return this;
    }

    @Override
    public NWebCallContext trace(Level level, NMsg msg) {
        Runtime rt = Runtime.getRuntime();
        double m = ((rt.totalMemory() - rt.freeMemory()) * 100.0 / rt.maxMemory());
        webContext.getLogger().out(NMsg.ofC(
                "[%s][M%.3f%%] %8s %s %6s %s %s",
                Instant.now(),
                m,
                level,
                httpExchange.getRemoteAddress(),
                NMsg.ofStyled(httpExchange.getRequestMethod(), NTextStyle.primary1()),
                NMsg.ofStyled(httpExchange.getRequestURI().toString(), NTextStyle.path()),
                msg
        ));
        return this;
    }

    @Override
    public NWebCallContext requireMethod(NHttpMethod... m) {
        NHttpMethod c = getMethod();
        for (NHttpMethod httpMethod : m) {
            if (httpMethod == c) {
                return this;
            }
        }
        String requiredStr = " (required "
                + (m.length == 1 ? String.valueOf(m[0]) : Arrays.stream(m).map(Enum::name).collect(Collectors.joining(","))) + ")";
        throw new NWebHttpException(
                "Not Allowed : [" + getMethod() + " ] " + requiredStr + " " + getPath(), new NMsgCode("HttpMethodNotAllowed", String.valueOf(c)), NHttpCode.METHOD_NOT_ALLOWED);
    }

    @Override
    public NWebCallContext throwNoFound() {
        throw new NWebHttpException("Not Found : [" + getMethod() + "] " + getPath(), new NMsgCode("NotFound"), NHttpCode.NOT_FOUND);
    }

    @Override
    public NWebPrincipal getPrincipal() {
        if (user != null) {
            return new NWebPrincipalSimple(user, "admin".equals(user.getUserName()));
        }
        return new NWebPrincipalAnonymous();
    }

    @Override
    public NOptional<NWebUser> getUser() {
        return NOptional.ofNamed(user, "user");
    }

    @Override
    public NWebCallContext setUser(NWebUser user) {
        this.user = user;
        return this;
    }

    @Override
    public NOptional<NWebToken> getToken() {
        return NOptional.ofNamed(token, "token");
    }

    @Override
    public NWebCallContext setToken(NWebToken token) {
        this.token = token;
        return this;
    }

    @Override
    public NWebCallContext runWithUnsafe(NUnsafeRunnable callable) throws Throwable {
        NWebCallContext t = NWebServerHttpContextHolder.current.get();
        NWebServerHttpContextHolder.current.set(this);
        try {
            callable.run();
        } finally {
            NWebServerHttpContextHolder.current.set(t);
        }
        return this;
    }

    @Override
    public Map<String, List<String>> getQueryParams() {
        if (queryParams == null) {
            Map<String, List<String>> m = NStringMapFormat.URL_FORMAT.parseDuplicates(
                    httpExchange.getRequestURI().getQuery()
            ).orNull();
            if (m == null) {
                m = new LinkedHashMap<>();
            }
            queryParams = m;
        }
        return queryParams;
    }

    @Override
    public NOptional<String> getQueryParam(String queryParam) {
        List<String> s = getQueryParams().get(queryParam);
        if (s != null && !s.isEmpty()) {
            return NOptional.of(s.get(0));
        }
        return NOptional.ofNamedEmpty(queryParam);
    }

    @Override
    public boolean containsQueryParam(String queryParam) {
        return getQueryParams().containsKey(queryParam);
    }

    private String normalizeHeaderKey(String key) {
        return NStringUtils.trim(key).toLowerCase();
    }

    @Override
    public List<String> getRequestHeaders(String header) {
        String normalizedHeader = normalizeHeaderKey(header);
        String s = getRequestHeaders().keySet().stream().filter(x -> normalizeHeaderKey(x).equals(normalizedHeader)).findFirst().orElse(null);
        if (s != null) {
            Headers rh = httpExchange.getRequestHeaders();
            List<String> h = rh.get(s);
            return h == null ? Collections.emptyList() : new ArrayList<>(h);
        }
        return Collections.emptyList();
    }

    public NOptional<String> getRequestHeader(String header) {
        String normalizedHeader = normalizeHeaderKey(header);
        String s = getRequestHeaders().keySet().stream().filter(x -> normalizeHeaderKey(x).equals(normalizedHeader)).findFirst().orElse(null);
        if (s != null) {
            List<String> v = httpExchange.getRequestHeaders().get(header);
            if (v != null) {
                return NOptional.of(v.get(0));
            }
        }
        return NOptional.ofNamedEmpty(header);
    }

    @Override
    public NOptional<String> getApiKeyRequestHeader() {
        return getRequestHeader("X-API-KEY");
    }

    @Override
    public NOptional<String> getRealmRequestHeader() {
        return getRequestHeader("X-REALM");
    }

    @Override
    public Map<String, FormDataItem> getFormaDataMap() {
        if (formData != null) {
            return formData;
        }
        String multipartRequestBoundary = this.getMultipartRequestBoundary().orNull();
        if (NBlankable.isBlank(multipartRequestBoundary)) {
            return formData = new HashMap<>();
        }
        Map<String, FormDataItem> formData = new LinkedHashMap<>();

//        byte[] rbBytes = NIOUtils.readBytes(getRequestBody());
//        InputStream rb = new ByteArrayInputStream(rbBytes);
//
        InputStream rb = getRequestBody();

        try (MixedInputStream br = new MixedInputStream(rb)) {
            MixedInputStream.Line line = null;
            line = br.readLine(maxLineLength);
            if (!isBoundaryLine(line, multipartRequestBoundary)) {
                throw new IllegalArgumentException("Invalid boundaries");
            }
            while (true) {
                FormDataItem fd = null;
                while ((line = br.readLine(maxLineLength)) != null) {
                    String sLine = line.getContentString();
                    if (sLine.contains(":")) {
                        int sep = sLine.indexOf(':');
                        String k = sLine.substring(0, sep).trim();
                        String v = sLine.substring(sep + 1).trim();
                        if (fd == null) {
                            fd = new FormDataItem();
                        }
                        fd.getHeaders().computeIfAbsent(k, e -> new ArrayList<>()).add(v);
                        switch (k) {
                            case "Content-Disposition": {
                                if (v.startsWith("form-data;")) {
                                    String cd = v.substring("form-data;".length()).trim();
                                    Map<String, List<String>> parsed = nStringMapFormat.parseDuplicates(cd).get();
                                    fd.setName(_get("name", parsed));
                                    fd.setFilename(_get("filename", parsed));
                                    fd.setProperties(parsed);
                                }
                                break;
                            }
                            case "Content-Type": {
                                fd.setContentType(v);
                                break;
                            }
                        }
                    } else if (sLine.trim().isEmpty()) {
                        break;
                    } else {
                        throw new NWebHttpException(
                                NMsg.ofC("Error reading request body : %s", line).toString(),
                                new NMsgCode("INVALID_FORM_DATA_BOUNDARY", sLine),
                                NHttpCode.BAD_REQUEST
                        );
                    }
                }
                if (fd != null && fd.getContentType() != null) {
                    formData.put(fd.getName(), fd);

                    String fdContentType = fd.getContentType();
                    fd.setSource(readBinaryPart(br, multipartRequestBoundary));
                } else if (fd != null) {
                    formData.put(fd.getName(), fd);
                    fd.setSource(readParamPart(br, multipartRequestBoundary));
                } else if (line == null) {
                    break;
                } else if (NBlankable.isBlank(line)) {
                    //okkay
                } else {
                    throw new NWebHttpException(
                            NMsg.ofC("Error reading request body").toString(),
                            new NMsgCode("INVALID_FORM_DATA_BOUNDARY"),
                            NHttpCode.BAD_REQUEST
                    );
                }
            }
        }
        return this.formData = formData;
    }

    private NInputSource readBinaryPart(MixedInputStream br, String multipartRequestBoundary) {
        NTempOutputStream outputStream = NIO.of().ofTempOutputStream();
        try {
            MixedInputStream.Line ll;
            while ((ll = br.readLine(maxLineLength)) != null) {
                if (isBoundaryLine(ll, multipartRequestBoundary)) {
                    break;
                } else {
                    outputStream.write(ll.getContent());
                    outputStream.write(ll.getSeparator());
                }
            }
        } catch (IOException e) {
            throw new NIOException(e);
        }
        // do not close
        return outputStream;
    }

    private NInputSource readParamPart(MixedInputStream br, String multipartRequestBoundary) {
        NInputSource src = readBinaryPart(br, multipartRequestBoundary);
        byte[] allBytes = src.readBytes();
        if (allBytes.length >= 2) {
            if (
                    allBytes[allBytes.length - 2] == 13
                            && allBytes[allBytes.length - 1] == 10
            ) {
                return NInputSource.of(Arrays.copyOfRange(allBytes, 0, allBytes.length - 2));
            }
        } else if (allBytes.length >= 1) {
            if (
                    allBytes[allBytes.length - 1] == 13
                            || allBytes[allBytes.length - 1] == 10
            ) {
                return NInputSource.of(Arrays.copyOfRange(allBytes, 0, allBytes.length - 1));
            }
        }
        return src;
    }

    public boolean isBoundaryLine(MixedInputStream.Line bline, String boundary) {
        for (byte b : bline.getContent()) {
            if (b == '-' || b == ' ' || (b >= '0' && b <= '9') || (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z')) {
                //ok
            } else {
                return false;
            }
        }
        String line = bline.getContentString().trim();
        boundary = boundary.trim();
        if (line.length() >= boundary.length()) {
            int i = line.indexOf(boundary);
            if (i >= 0) {
                String r = line.substring(0, i)
                        + "#"
                        + line.substring(i + boundary.length());
                return true;
            }
        }
        return false;
    }

    @Override
    public InputStream getRequestBody() {
        return httpExchange.getRequestBody();
    }

    @Override
    public NOptional<FormDataItem> getFormaData(String name) {
        Map<String, FormDataItem> u = getFormaDataMap();
        return NOptional.ofNamed(u == null ? null : u.get(name), name);
    }

    @Override
    public boolean isMultipartRequest() {
        for (String a : getRequestHeaders("Content-Type")) {
            a = a.trim();
            if (a.startsWith("multipart/form-data")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NOptional<String> getMultipartRequestBoundary() {
        for (String value : getRequestHeaders("Content-Type")) {
            value = value.trim();
            if (value.startsWith("multipart/form-data; boundary=")) {
                return NOptional.of(value.split("boundary=")[1]);
            }
        }
        return NOptional.ofNamedEmpty("multipart");
    }

    private String _get(String k, Map<String, List<String>> map) {
        List<String> v = map.get(k);
        if (v != null && v.size() > 0) {
            return v.get(0);
        }
        return null;
    }

    private static class LineAndNewLine {
        String line;
        String newLine;

        public LineAndNewLine(String line, String newLine) {
            this.line = line;
            this.newLine = newLine;
        }
    }

    private LineAndNewLine readLineAndNewLine(BufferedReader reader) {
        StringBuilder sb = new StringBuilder();
        StringBuilder nl = new StringBuilder();
        try {
            while (true) {
                int c = 0;
                c = reader.read();
                if (c == -1) {
                    if (sb.length() == 0 && nl.length() == 0) {
                        return null;
                    }
                    return new LineAndNewLine(sb.toString(), nl.toString());
                } else if (c == '\r') {
                    nl.append('\r');
                    return new LineAndNewLine(sb.toString(), nl.toString());
                } else if (c == '\n') {
                    nl.append('\n');
                    reader.mark(1);
                    int x = reader.read();
                    if (x == -1) {
                        // do nothing
                    } else if (x == '\r') {
                        nl.append('\r');
                    } else {
                        reader.reset();
                    }
                    return new LineAndNewLine(sb.toString(), nl.toString());
                } else {
                    sb.append((char) c);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public NWebCallContext setTextResponse(String value) {
        this.responseObject = value;
        this.contentType = "text/plain";
        this.responseMode = "string";
        return this;
    }

    @Override
    public NWebCallContext setXmlResponse(String value) {
        this.responseObject = value;
        this.contentType = "application/xml";
        this.responseMode = "string";
        return this;
    }

    @Override
    public NWebCallContext setErrorResponse(NWebHttpException ex) {
        this.responseObject = ex;
        this.responseMode = "throwable";
        return this;
    }

    @Override
    public NWebCallContext setErrorResponse(Throwable ex) {
        this.responseObject = ex;
        this.responseMode = "throwable";
        return this;
    }

    @Override
    public NWebCallContext setJsonResponse(Object value) {
        this.responseObject = value;
        this.contentType = "application/json";
        this.responseMode = "object";
        return this;
    }

    @Override
    public NWebCallContext setBytesResponse(byte[] value) {
        this.responseObject = value;
        this.contentType = "application/octet-stream";
        this.responseMode = "bytes";
        return this;
    }

    @Override
    public NWebCallContext setFileResponse(NPath value) {
        this.responseObject = value;
        this.contentType = "application/octet-stream";
        this.responseMode = "path";
        return this;
    }

    @Override
    public NWebCallContext setErrorResponse(NMsgCode errorCode) {
        this.responseObject = errorCode;
        this.contentType = "application/json";
        this.responseMode = "msgCode";
        return this;
    }

    @Override
    public NWebCallContext sendResponse() {
        if (responseHeadersSent) {
            throw new NIllegalArgumentException(NMsg.ofC("response headers already sent"));
        }
        switch (responseMode) {
            case "string": {
                String json = String.valueOf(responseObject);
                byte[] bytes = json.getBytes();
                this.setResponseContentType(NStringUtils.firstNonBlank(contentType, "text/plain"));
                this.sendResponseHeaders();
                try {
                    httpExchange.sendResponseHeaders(
                            ((responseCode == null ? NHttpCode.OK : responseCode))
                                    .getCode(), bytes.length
                    );
                } catch (IOException e) {
                    throw new NIOException(e);
                }
                responseHeadersSent = true;
                this.sendResponseContent(bytes);
                return this;
            }
            case "object": {
                String ct = NStringUtils.firstNonBlank(contentType, "application/json");
                byte[] bytes = new byte[0];
                switch (ct) {
                    case "application/json": {
                        bytes = JsonUtils.toJson(responseObject).getBytes();
                        break;
                    }
                    default: {
                        bytes = String.valueOf(responseObject).getBytes();
                        break;
                    }
                }
                this.setResponseContentType(NStringUtils.firstNonBlank(contentType, "text/plain"));
                this.sendResponseHeaders();
                try {
                    httpExchange.sendResponseHeaders(
                            ((responseCode == null ? NHttpCode.OK : responseCode))
                                    .getCode(), bytes.length
                    );
                } catch (IOException e) {
                    throw new NIOException(e);
                }
                responseHeadersSent = true;
                this.sendResponseContent(bytes);
                return this;
            }
            case "bytes": {
                String ct = NStringUtils.firstNonBlank(contentType, "application/octet-stream");
                byte[] bytes = new byte[0];
                switch (ct) {
                    case "application/json": {
                        bytes = JsonUtils.toJson(new String((byte[]) responseObject)).getBytes();
                        break;
                    }
                    default: {
                        bytes = (byte[]) responseObject;
                        break;
                    }
                }
                this.setResponseContentType(NStringUtils.firstNonBlank(contentType, "text/plain"));
                this.sendResponseHeaders();
                try {
                    httpExchange.sendResponseHeaders(
                            ((responseCode == null ? NHttpCode.OK : responseCode))
                                    .getCode(), bytes.length
                    );
                } catch (IOException e) {
                    throw new NIOException(e);
                }
                responseHeadersSent = true;
                this.sendResponseContent(bytes);
                return this;
            }
            case "path": {
                String ct = contentType;
                NPath file = (NPath) responseObject;

                if (file != null && file.exists() && file.isRegularFile()) {
                    if (NBlankable.isBlank(ct)) {
                        ct = file.contentType();
                        if (NBlankable.isBlank(ct)) {
                            ct = "application/octet-stream";
                        }
                    }
                    this.setResponseContentType(ct);
                    this.sendResponseHeaders();
                    try {
                        httpExchange.sendResponseHeaders(
                                ((responseCode == null ? NHttpCode.OK : responseCode))
                                        .getCode(), file.contentLength()
                        );
                    } catch (IOException e) {
                        throw new NIOException(e);
                    }
                    responseHeadersSent = true;
                    try (InputStream is = file.getInputStream()) {
                        this.sendResponseContent(is);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        httpExchange.sendResponseHeaders(
                                ((responseCode == null ? NHttpCode.OK : responseCode))
                                        .getCode(), 0
                        );
                    } catch (IOException e) {
                        throw new NIOException(e);
                    }

                    this.setResponseCode(NHttpCode.NOT_FOUND);
                    this.setErrorCode(new NMsgCode("FILE_NOT_FOUND", file == null ? null : file.getName()));
                    this.sendResponseHeaders();
                    responseHeadersSent = true;
                    this.sendResponseContent(new byte[0]);
                }
                return this;
            }
            case "msgCode": {
                NMsgCode mc = (NMsgCode) responseObject;
                if (mc == null) {
                    mc = new NMsgCode("ERROR");
                }
                this.setErrorCode(mc);
                this.sendResponseHeaders();
                try {
                    httpExchange.sendResponseHeaders(
                            (responseCode == null ? NHttpCode.INTERNAL_SERVER_ERROR : responseCode)
                                    .getCode(), 0
                    );
                } catch (IOException e) {
                    throw new NIOException(e);
                }
                responseHeadersSent = true;
                this.sendResponseContent(new byte[0]);
                return this;
            }
            case "throwable": {
                Throwable th = (Throwable) responseObject;
                NWebHttpException r = wrapException(th);
                String message = r.getMessage();
                if (message == null) {
                    message = "Error";
                }
                NMsgCode mcode = r.getNMsgCode();
                NWebErrorResult o = new NWebErrorResult(message);
                if (mcode != null) {
                    o.setCode(mcode.getCode());
                    o.setParams(mcode.getParams());
                }
                setErrorCode(mcode);
                byte[] bytes = null;
                String errCt = NStringUtils.firstNonBlank(contentType, "application/json");
                switch (errCt) {
                    case "application/json": {
                        bytes = JsonUtils.toJson(o).getBytes();
                        break;
                    }
                    default: {
                        //force
                        errCt = "application/json";
                        bytes = JsonUtils.toJson(o).getBytes();
                        break;
                    }
                }
                setResponseContentType(errCt);
                this.sendResponseHeaders();
                try {
                    httpExchange.sendResponseHeaders(
                            NUtils.firstNonNull(r.getHttpCode(), responseCode, NHttpCode.INTERNAL_SERVER_ERROR)
                                    .getCode(), bytes.length
                    );
                } catch (IOException e) {
                    throw new NIOException(e);
                }
                responseHeadersSent = true;
                this.sendResponseContent(bytes);
                return this;
            }
        }
        throw new RuntimeException("Unsupported responseMode: " + responseMode);
    }

    public void close() {
        if (formData != null) {
            for (FormDataItem value : formData.values()) {
                value.getSource().dispose();
            }
        }
    }

    @Override
    public boolean isResponseSent() {
        return responseHeadersSent;
    }

    @Override
    public NLoginResult authenticateWithCredentials(NAuthenticationRequest authenticationRequest) {
        NWebUser u = getWebContext().getUserResolver().loadUserAndAuthenticate(authenticationRequest);
        if (u != null) {
            NWebTokenBuilder tb = getWebContext().getTokenBuilder();
            NWebTokenRequest wr = new NWebTokenRequest()
                    .setType(NWebTokenType.ACCESS)
                    .setUser(u)
                    .setRealm(authenticationRequest.getRealm())
                    .setApiKey(authenticationRequest.getApiKey());

            NWebToken aToken = tb.createToken(wr.copy().setType(NWebTokenType.ACCESS), this);
            NWebToken rToken = tb.createToken(wr.copy().setType(NWebTokenType.REFRESH), this);
            String accessToken = getWebContext().getTokenEncoder().encode(aToken, this);
            String refreshToken = getWebContext().getTokenEncoder().encode(rToken, this);
            NLoginResult rr = new NLoginResult();
            rr.setUserId(u.getUserId());
            rr.setUserName(u.getUserName());
            rr.setAccessToken(accessToken);
            rr.setRefreshToken(refreshToken);
            rr.setAccessExpiryTime(aToken.getExpiryTime());
            rr.setRefreshExpiryTime(rToken.getExpiryTime());
            return rr;
        }
        return null;
    }

    public NWebUser authenticateWithAccessToken(String accessToken) {
        NWebToken tt = getWebContext().getTokenEncoder().decode(accessToken, this);
        if (tt != null) {
            if (tt.getType() == NWebTokenType.ACCESS && new Date(tt.getExpiryTime()).compareTo(new Date()) >= 0) {
                NWebUser u = getWebContext().getUserResolver().loadUser(tt.getUserId());
                if (u != null) {
                    return u;
                }
            }
        }
        return null;
    }

    public NLoginResult authenticateWithRefreshToken(String refreshToken) {
        NWebToken tt = getWebContext().getTokenEncoder().decode(refreshToken, this);
        if (tt != null) {
            if (tt.getType() == NWebTokenType.REFRESH && new Date(tt.getExpiryTime()).compareTo(new Date()) <= 0) {
                NWebUser u = getWebContext().getUserResolver().loadUser(tt.getUserId());
                if (u != null) {
                    NWebTokenRequest wr = new NWebTokenRequest()
                            .setType(NWebTokenType.ACCESS)
                            .setUser(u)
                            .setRealm(tt.getRealm())
                            .setApiKey(tt.getApiKey());
                    NWebTokenBuilder tb = getWebContext().getTokenBuilder();
                    NWebToken aToken = tb.createToken(wr.copy().setType(NWebTokenType.ACCESS), this);
                    NWebToken rToken = tb.createToken(wr.copy().setType(NWebTokenType.REFRESH), this);
                    String accessToken = getWebContext().getTokenEncoder().encode(aToken, this);
                    refreshToken = getWebContext().getTokenEncoder().encode(rToken, this);
                    NLoginResult rr = new NLoginResult();
                    rr.setUserId(u.getUserId());
                    rr.setUserName(u.getUserName());
                    rr.setAccessToken(accessToken);
                    rr.setRefreshToken(refreshToken);
                    rr.setAccessExpiryTime(aToken.getExpiryTime());
                    rr.setRefreshExpiryTime(rToken.getExpiryTime());
                    return rr;
                }
            }
        }
        return null;
    }
}
