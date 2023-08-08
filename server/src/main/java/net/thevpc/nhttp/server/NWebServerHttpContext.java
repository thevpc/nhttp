package net.thevpc.nhttp.server;

import net.thevpc.nhttp.commons.HttpCode;
import net.thevpc.nhttp.commons.HttpMethod;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.thevpc.nhttp.server.error.*;
import net.thevpc.nhttp.server.model.NWebErrorResult;
import net.thevpc.nhttp.server.model.NWebToken;
import net.thevpc.nhttp.server.security.*;
import net.thevpc.nhttp.server.util.NWebResponseHeaders;
import net.thevpc.nhttp.server.util.NWebLogger;
import net.thevpc.nhttp.server.util.JsonUtils;
import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.util.NUnsafeRunnable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

public class NWebServerHttpContext {
    public static InheritableThreadLocal<NWebServerHttpContext> current = new InheritableThreadLocal<>();
    private HttpServer server;
    private HttpExchange httpExchange;
    private NSession session;
    private byte[] requestBody = null;
    private HttpMethod method;
    private NWebUser user;
    private NWebToken token;
    private NWebUserResolver userResolver;
    private String[] pathParts;
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private Map<String, String> queryParams;
    private NWebLogger logger;


    public NWebServerHttpContext(HttpServer server, HttpExchange httpExchange,
                                 NWebUserResolver userResolver,
                                 NSession session, NWebLogger logger) {
        this.server = server;
        this.userResolver = userResolver;
        this.httpExchange = httpExchange;
        this.session = session;
        this.logger = logger;
        if (httpExchange != null) {
            this.pathParts = Arrays.stream(getPath().split("/")).filter(x -> x.length() > 0).toArray(String[]::new);
        } else {
            this.pathParts = new String[0];
        }
    }

    public HttpServer getServer() {
        return server;
    }

    public String getFirstPath() {
        return pathParts.length > 0 ? pathParts[0] : "";
    }

    public boolean isEmptyPath() {
        return pathParts.length == 0;
    }

    public int getPathSize() {
        return pathParts.length;
    }

    public String[] getPathParts() {
        return pathParts;
    }

    public String getPathPart(int pos) {
        if (pos < 0 || pos >= pathParts.length) {
            return "";
        }
        return pathParts[pos];
    }

    public NSession getSession() {
        return session;
    }

    public <T> T getBodyAs(Class<T> cl) {
        String bodyAsString = getBodyAsString();
        if (bodyAsString.isEmpty()) {
            if (cl == String.class) {
                return (T) "";
            }
            return null;
        }
        return JsonUtils.fromJson(bodyAsString, cl, session);
    }

    public String getBodyAsString() {
        if (requestBody == null) {
            try {
                requestBody = NCp.of(session).from(httpExchange.getRequestBody()).getByteArrayResult();
            } catch (RuntimeException e) {
                requestBody = new byte[0];
                throw e;
            }
        }
        return new String(requestBody);
    }

    public void sendPlainText(String ex, HttpCode code, NWebResponseHeaders headers) {
        String json = String.valueOf(ex);
        if (headers == null) {
            headers = new NWebResponseHeaders();
        }
        headers.setContentType("plain/text");
        sendBytes(json.getBytes(), code, headers);
    }

    public void sendXml(String value) {
        sendXml(value, HttpCode.OK, new NWebResponseHeaders());
    }

    public void sendXml(String value, HttpCode code, NWebResponseHeaders headers) {
        String json = String.valueOf(value);
        if (headers == null) {
            headers = new NWebResponseHeaders();
        }
        headers.setContentType("application/xml");
        sendBytes(json.getBytes(), code, headers);
    }

    public void sendJson(Object ex, NWebResponseHeaders headers) {
        sendJson(ex, HttpCode.OK, headers);
    }

    public void sendJson(Object ex) {
        sendJson(ex, HttpCode.OK, null);
    }

    public void sendJson(Object ex, HttpCode code, NWebResponseHeaders headers) {
        String json = JsonUtils.toJson(ex, session);
        if (headers == null) {
            headers = new NWebResponseHeaders();
        }
        headers.setContentType("application/json");
        sendBytes(json.getBytes(), code, headers);
    }

    public String getPath() {
        return httpExchange.getRequestURI().getPath();
    }

    public void sendError(Throwable ex) {
        if (ex instanceof NWebHttpException) {
            sendError((NWebHttpException) ex);
        } else if (ex instanceof NoSuchElementException) {
            sendError(new NWebHttpException(ex.getMessage(),
                    NWebErrorCodeAware.codeOf(ex).orElse(new NWebErrorCode("NotFound")), HttpCode.NOT_FOUND));
        } else if (ex instanceof NWebUnauthorizedSecurityException) {
            sendError(new NWebHttpException(ex.getMessage(), NWebErrorCodeAware.codeOf(ex).get(), HttpCode.UNAUTHORIZED));
        } else if (ex instanceof SecurityException) {
            sendError(new NWebHttpException(ex.getMessage(), NWebErrorCodeAware.codeOf(ex).get(), HttpCode.FORBIDDEN));
        } else {
            ex.printStackTrace();
            sendError(new NWebHttpException(ex.getMessage(), NWebErrorCodeAware.codeOf(ex).orElse(new NWebErrorCode("Error")), HttpCode.INTERNAL_SERVER_ERROR));
        }
    }

    public void sendError(NWebHttpException ex) {
        String message = ex.getMessage();
        if (message == null) {
            message = "Error";
        }
        NWebErrorResult o = new NWebErrorResult(message);
        NWebResponseHeaders z = new NWebResponseHeaders();
        z.setErrorCode(ex.getAppErrorCode());
        sendJson(o, ex.getHttpCode(), z);
    }

    public void sendBytes(byte[] bytes, HttpCode code, NWebResponseHeaders headers) {
        if (headers != null) {
            if (!NBlankable.isBlank(headers.getContentType())) {
                httpExchange.getResponseHeaders().add("Content-Type", headers.getContentType());
            }
            if (headers.getErrorCode() != null && !NBlankable.isBlank(headers.getErrorCode())) {
                String json = JsonUtils.toJson(headers.getErrorCode(), session);
                String b64 = Base64.getEncoder().encodeToString(json.getBytes());
                httpExchange.getResponseHeaders().add("X-APP-ERROR", b64);
            }
        }
        try {
            bos = new ByteArrayOutputStream();
            bos.write(bytes);
            httpExchange.sendResponseHeaders(code.getCode(), bos.size());
            OutputStream os = httpExchange.getResponseBody();
            os.write(bos.toByteArray());
            os.close();
        } catch (IOException e) {
            throw new ApplicationException(new NWebErrorCode("IO.SendFailed"), "send byte failed", e);
        }
    }

    public HttpMethod getMethod() {
        if (method == null) {
            String m = httpExchange.getRequestMethod();
            switch (NStringUtils.trim(m).toUpperCase()) {
                case "GET":
                    return method = HttpMethod.GET;
                case "POST":
                    return method = HttpMethod.POST;
                case "PUT":
                    return method = HttpMethod.PUT;
                case "OPTIONS":
                    return method = HttpMethod.OPTIONS;
                case "PATCH":
                    return method = HttpMethod.PATCH;
                case "DELETE":
                    return method = HttpMethod.DELETE;
                default:
                    return method = HttpMethod.UNKNOWN;
            }
        }
        return method;
    }

    public NWebServerHttpContext requireAuth() {
        List<String> authorization = httpExchange.getRequestHeaders().get("Authorization");
        NWebUser user = null;
        NWebToken token = null;
        boolean someToken = false;
        if (authorization != null) {
            try {
                for (String s : authorization) {
                    if (s != null) {
                        if (s.toLowerCase().startsWith("bearer")) {
                            someToken = true;
                            String yy = s.substring("bearer" .length()).trim();
                            token = userResolver.parseToken(yy);
                            if (token != null) {
                                user = userResolver.loadUser(token);
                                if (user != null) {
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (RuntimeException ex) {
                if (ex instanceof NWebErrorCodeAware) {
                    throw ex;
                }
                throw new ApplicationException(new NWebErrorCode("Security.AuthorizationFailed"), ex.toString(), ex);
            } catch (Throwable ex) {
                throw new ApplicationException(new NWebErrorCode("Security.AuthorizationFailed"), ex.toString(), ex);
            }
        }
        if (user == null) {
            if (someToken) {
                throw new NWebUnauthorizedSecurityException(new NWebErrorCode("Security.InvalidToken"), "invalid token");
            } else {
                throw new NWebUnauthorizedSecurityException(new NWebErrorCode("Security.MissingToken"), "missing token");
            }
        }
        trace(Level.INFO, NMsg.ofC("authenticated %s %s", user.getId(), user.getUserName()));
        setUser(user);
        setToken(token);
        return this;
    }

    public void trace(Level level, NMsg msg) {
        logger.out(NMsg.ofC(
                "[%s] %8s %s %6s %s %s",
                Instant.now(),
                level,
                httpExchange.getRemoteAddress(),
                httpExchange.getRequestMethod(),
                httpExchange.getRequestURI(),
                msg
        ));
    }

    public NWebServerHttpContext requireMethod(HttpMethod... m) {
        HttpMethod c = getMethod();
        for (HttpMethod httpMethod : m) {
            if (httpMethod == c) {
                return this;
            }
        }
        throw new NWebHttpException("Not Allowed " + c, new NWebErrorCode("HttpMethodNotAllowed", String.valueOf(c)), HttpCode.METHOD_NOT_ALLOWED);
    }

    public NWebServerHttpContext throwNoFound() {
        throw new NWebHttpException("Not Found", new NWebErrorCode("NotFound"), HttpCode.NOT_FOUND);
    }

    public NWebPrincipal getPrincipal() {
        if (user != null) {
            return new NWebPrincipalSimple(user, "admin" .equals(user.getUserName()));
        }
        return new NWebPrincipalAnonymous();
    }

    public NOptional<NWebUser> getUser() {
        return NOptional.ofNamed(user, "user");
    }

    public NWebServerHttpContext setUser(NWebUser user) {
        this.user = user;
        return this;
    }

    public NOptional<NWebToken> getToken() {
        return NOptional.ofNamed(token, "token");
    }

    public NWebServerHttpContext setToken(NWebToken token) {
        this.token = token;
        return this;
    }

    public static NOptional<NWebServerHttpContext> current() {
        return NOptional.ofNamed(current.get(), "context");
    }

    public NWebServerHttpContext runWithUnsafe(NUnsafeRunnable callable) throws Throwable {
        NWebServerHttpContext t = current.get();
        current.set(this);
        try {
            callable.run();
        } finally {
            current.set(t);
        }
        return this;
    }

    public Map<String, String> getQueryParams() {
        if (queryParams == null) {
            Map<String, String> m = NStringMapFormat.URL_FORMAT.parse(
                    httpExchange.getRequestURI().getQuery()
            ).orNull();
            if (m == null) {
                m = new LinkedHashMap<>();
            }
            queryParams = m;
        }
        return queryParams;
    }

    public String getQueryParam(String queryParam) {
        return getQueryParams().get(queryParam);
    }

    public boolean containsQueryParam(String queryParam) {
        return getQueryParams().containsKey(queryParam);
    }
}
