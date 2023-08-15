package net.thevpc.nhttp.server.servlet;

import com.sun.net.httpserver.HttpExchange;
import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringMapFormat;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

class HttpServletRequestImpl implements HttpServletRequest {
    private static final String[] DATE_FORMATS = new String[]{"EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM dd HH:mm:ss yyyy"};
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private ServletHttpHandler handler;
    private HttpExchange exchange;
    private Map<String, Object> attributes = new HashMap<>();
    private Map<String, List<String>> queryParams;
    HttpServletResponseImpl resp;
    private boolean asyncStarted;
    private AsyncContextImpl startedAsyncContext;

    public HttpServletRequestImpl(ServletHttpHandler handler, HttpExchange exchange) {
        this.handler = handler;
        this.exchange = exchange;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String s) {
        NLiteral li = NLiteral.of(getHeader(s));
        if (li.isLong()) {
            return li.asLong().get();
        }
        String ss = li.asString().orNull();
        if (NBlankable.isBlank(ss)) {
            return 0;
        }
        String[] var3 = DATE_FORMATS;
        int var4 = var3.length;
        int var5 = 0;

        while (var5 < var4) {
            String dateFormat = var3[var5];
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            simpleDateFormat.setTimeZone(GMT);

            try {
                return simpleDateFormat.parse(ss).getTime();
            } catch (ParseException var9) {
                ++var5;
            }
        }

        throw new IllegalArgumentException("unable to parse date value '" + ss + "' for '" + s + "' header");
    }

    @Override
    public String getHeader(String s) {
        return exchange.getRequestHeaders().getFirst(s);
    }

    @Override
    public Enumeration getHeaders(String s) {
        return Collections.enumeration(exchange.getRequestHeaders().get(s));
    }

    @Override
    public Enumeration getHeaderNames() {
        return Collections.enumeration(exchange.getRequestHeaders().keySet());
    }

    @Override
    public int getIntHeader(String s) {
        return NLiteral.of(getHeader(s)).asInt().orElse(0);
    }

    public long getLongHeader(String s) {
        return NLiteral.of(getHeader(s)).asLong().orElse(0L);
    }

    @Override
    public String getMethod() {
        return exchange.getRequestMethod();
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean b) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public Object getAttribute(String s) {
        return attributes.get(s);
    }

    @Override
    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return getIntHeader("Content-Length");
    }

    @Override
    public String getContentType() {
        return getHeader("Content-Type");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStreamImpl(exchange.getRequestBody());
    }

    private Map<String, List<String>> getQueryParams() {
        if (queryParams == null) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, List<String>> m = NStringMapFormat.URL_FORMAT.parseDuplicates(query).orNull();
            if (m == null) {
                m = new LinkedHashMap<>();
            }
            queryParams = m;
        }
        return queryParams;
    }

    @Override
    public String getParameter(String s) {
        List<String> list = getQueryParams().get(s);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(getQueryParams().keySet());
    }

    @Override
    public String[] getParameterValues(String s) {
        List<String> list = getQueryParams().get(s);
        return list == null ? new String[0] : list.toArray(new String[0]);
    }

    @Override
    public Map getParameterMap() {
        return getQueryParams().entrySet().stream().map(x -> new AbstractMap.SimpleEntry<>(x.getKey(), x.getValue().toArray(new String[0])))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    @Override
    public String getProtocol() {
        return exchange.getProtocol();
    }

    @Override
    public String getScheme() {
        return exchange.getRequestURI().getScheme();
    }

    @Override
    public String getServerName() {
        return handler.getServer().getServerName();
    }

    @Override
    public int getServerPort() {
        return handler.getServer().getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public String getRemoteAddr() {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public String getRemoteHost() {
        return exchange.getRemoteAddress().getAddress().getHostName();
    }

    @Override
    public void setAttribute(String s, Object o) {
        attributes.put(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        attributes.remove(s);
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        String r = getRequestURL().toString();
        return r.startsWith("https:");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    @Override
    public String getRealPath(String s) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return exchange.getRemoteAddress().getPort();
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return exchange.getLocalAddress().getAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return exchange.getLocalAddress().getPort();
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s1) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public long getContentLengthLong() {
        return getLongHeader("Content-Length");
    }

    @Override
    public ServletContext getServletContext() {
        return handler.getServletContext();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return startAsync(this,resp);
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        NAssert.requireFalse(asyncStarted,"asyncStarted");
        asyncStarted=true;
        return startedAsyncContext=new AsyncContextImpl(this, resp);
    }

    @Override
    public boolean isAsyncStarted() {
        return asyncStarted;
    }

    @Override
    public boolean isAsyncSupported() {
        return true;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return startedAsyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }

}
