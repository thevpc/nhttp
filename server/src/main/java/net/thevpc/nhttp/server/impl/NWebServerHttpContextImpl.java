package net.thevpc.nhttp.server.impl;

import net.thevpc.nuts.web.NHttpCode;
import net.thevpc.nuts.web.NHttpMethod;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.thevpc.nhttp.server.api.*;
import net.thevpc.nhttp.server.error.*;
import net.thevpc.nhttp.server.model.NWebErrorResult;
import net.thevpc.nhttp.server.security.*;
import net.thevpc.nhttp.server.api.NWebLogger;
import net.thevpc.nhttp.server.util.JsonUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
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
import java.util.stream.Collectors;

public class NWebServerHttpContextImpl implements NWebServerHttpContext {
    private HttpServer server;
    private HttpExchange httpExchange;
    private NSession session;
    private byte[] requestBody = null;
    private NHttpMethod method;
    private NWebUser user;
    private NWebToken token;
    private NWebUserResolver userResolver;
    private String[] pathParts;
    //    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private Map<String, String> queryParams;
    private NWebLogger logger;


    public NWebServerHttpContextImpl(HttpServer server, HttpExchange httpExchange,
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
    public NSession getSession() {
        return session;
    }

    @Override
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

    @Override
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

    @Override
    public void sendPlainText(String ex, NHttpCode code, NWebResponseHeaders headers) {
        String json = String.valueOf(ex);
        if (headers == null) {
            headers = new NWebResponseHeaders();
        }
        headers.setContentType("plain/text");
        sendBytes(json.getBytes(), code, headers);
    }

    @Override
    public void sendXml(String value) {
        sendXml(value, NHttpCode.OK, new NWebResponseHeaders());
    }

    @Override
    public void sendXml(String value, NHttpCode code, NWebResponseHeaders headers) {
        String json = String.valueOf(value);
        if (headers == null) {
            headers = new NWebResponseHeaders();
        }
        headers.setContentType("application/xml");
        sendBytes(json.getBytes(), code, headers);
    }

    @Override
    public void sendJson(Object ex, NWebResponseHeaders headers) {
        sendJson(ex, NHttpCode.OK, headers);
    }

    @Override
    public void sendJson(Object ex) {
        sendJson(ex, NHttpCode.OK, null);
    }

    @Override
    public void sendJson(Object ex, NHttpCode code, NWebResponseHeaders headers) {
        String json = JsonUtils.toJson(ex, session);
        if (headers == null) {
            headers = new NWebResponseHeaders();
        }
        headers.setContentType("application/json");
        sendBytes(json.getBytes(), code, headers);
    }

    @Override
    public String getPath() {
        return httpExchange.getRequestURI().getPath();
    }

    @Override
    public void sendError(Throwable ex) {
        if (ex instanceof NWebHttpException) {
            sendError((NWebHttpException) ex);
        } else if (ex instanceof NoSuchElementException) {
            sendError(new NWebHttpException(ex.getMessage(),
                    NWebErrorCodeAware.codeOf(ex).orElse(new NWebErrorCode("NotFound")), NHttpCode.NOT_FOUND));
        } else if (ex instanceof NWebUnauthorizedSecurityException) {
            sendError(new NWebHttpException(ex.getMessage(), NWebErrorCodeAware.codeOf(ex).get(), NHttpCode.UNAUTHORIZED));
        } else if (ex instanceof SecurityException) {
            sendError(new NWebHttpException(ex.getMessage(), NWebErrorCodeAware.codeOf(ex).get(), NHttpCode.FORBIDDEN));
        } else if (ex instanceof ApplicationException) {
            sendError(new NWebHttpException(ex.getMessage(), NWebErrorCodeAware.codeOf(ex).get(), NHttpCode.FORBIDDEN));
        } else if (ex instanceof NWebErrorCodeAware) {
            sendError(new NWebHttpException(ex.getMessage(), NWebErrorCodeAware.codeOf(ex).get(), NHttpCode.FORBIDDEN));
        } else {
            ex.printStackTrace();
            sendError(new NWebHttpException(ex.getMessage(), NWebErrorCodeAware.codeOf(ex).orElse(new NWebErrorCode("Error")), NHttpCode.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
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

    @Override
    public void sendBytes(byte[] bytes, NHttpCode code, NWebResponseHeaders headers) {
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
            httpExchange.sendResponseHeaders(code.getCode(), bytes.length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (IOException e) {
            throw new ApplicationException(new NWebErrorCode("IO.SendFailed"), "send byte failed", e);
        }
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
                            String yy = s.substring("bearer".length()).trim();
                            token = userResolver.parseToken(yy);
                            if (token != null) {
                                try {
                                    user = userResolver.loadUser(token);
                                } catch (Exception e) {
                                    throw new NWebUnauthorizedSecurityException(new NWebErrorCode("Security.InvalidToken"), e.toString());
                                }
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

    @Override
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

    @Override
    public NWebServerHttpContext requireMethod(NHttpMethod... m) {
        NHttpMethod c = getMethod();
        for (NHttpMethod httpMethod : m) {
            if (httpMethod == c) {
                return this;
            }
        }
        String requiredStr = " (required "
                + (m.length == 1 ? String.valueOf(m[0]) : Arrays.stream(m).map(Enum::name).collect(Collectors.joining(","))) + ")";
        throw new NWebHttpException(
                "Not Allowed : [" + getMethod() + " ] " + requiredStr + " " + getPath() + requiredStr, new NWebErrorCode("HttpMethodNotAllowed", String.valueOf(c)), NHttpCode.METHOD_NOT_ALLOWED);
    }

    @Override
    public NWebServerHttpContext throwNoFound() {
        throw new NWebHttpException("Not Found : [" + getMethod() + "] " + getPath(), new NWebErrorCode("NotFound"), NHttpCode.NOT_FOUND);
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
    public NWebServerHttpContext setUser(NWebUser user) {
        this.user = user;
        return this;
    }

    @Override
    public NOptional<NWebToken> getToken() {
        return NOptional.ofNamed(token, "token");
    }

    @Override
    public NWebServerHttpContext setToken(NWebToken token) {
        this.token = token;
        return this;
    }

    @Override
    public NWebServerHttpContext runWithUnsafe(NUnsafeRunnable callable) throws Throwable {
        NWebServerHttpContext t = NWebServerHttpContextHolder.current.get();
        NWebServerHttpContextHolder.current.set(this);
        try {
            callable.run();
        } finally {
            NWebServerHttpContextHolder.current.set(t);
        }
        return this;
    }

    @Override
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

    @Override
    public String getQueryParam(String queryParam) {
        return getQueryParams().get(queryParam);
    }

    @Override
    public boolean containsQueryParam(String queryParam) {
        return getQueryParams().containsKey(queryParam);
    }
}
