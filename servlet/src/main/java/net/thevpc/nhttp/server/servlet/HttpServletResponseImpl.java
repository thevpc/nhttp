package net.thevpc.nhttp.server.servlet;

import com.sun.net.httpserver.HttpExchange;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NAssert;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

class HttpServletResponseImpl implements HttpServletResponse {
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private ServletHttpHandler handler;
    private HttpExchange exchange;
    private boolean committed;
    private int bufferSize;
    private byte[] buffer;
    private long contentLength;
    private int status;
    private String statusText;
    private ServletOutputStreamImpl myServletOutputStream;
    private PrintWriter printWriter;

    public HttpServletResponseImpl(ServletHttpHandler handler, HttpExchange exchange) {
        this.handler = handler;
        this.exchange = exchange;
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String s) {
        return !getHeaders(s).isEmpty();
    }

    @Override
    public String encodeURL(String s) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String s) {
        return null;
    }

    @Override
    public String encodeUrl(String s) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return null;
    }

    @Override
    public void sendError(int i, String s) throws IOException {
        setStatus(i, s);
        doTryCommit();
    }

    @Override
    public void sendError(int i) throws IOException {
        setStatus(i);
        doTryCommit();
    }

    @Override
    public void sendRedirect(String url) throws IOException {
        this.setHeader("Location", url);
        this.setStatus(302);
        doTryCommit();
    }

    @Override
    public void setDateHeader(String s, long l) {
        setHeader(s, formatDate(new Date(l)));
    }

    @Override
    public void addDateHeader(String s, long l) {
        addHeader(s, formatDate(new Date(l)));
    }

    private String formatDate(Date d) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        dateFormat.setTimeZone(GMT);
        return dateFormat.format(d);
    }

    @Override
    public void setHeader(String s, String s1) {
        ensureNotCommitted();
        exchange.getResponseHeaders().set(s, s1);
    }

    @Override
    public void addHeader(String s, String s1) {
        exchange.getResponseHeaders().add(s, s1);
    }

    @Override
    public void setIntHeader(String s, int i) {
        setHeader(s, String.valueOf(i));
    }

    @Override
    public void addIntHeader(String s, int i) {
        addHeader(s, String.valueOf(i));
    }

    @Override
    public void setStatus(int i) {
        setStatus(i, null);
    }

    @Override
    public void setStatus(int i, String s) {
        ensureNotCommitted();
        this.status = i;
        if (NBlankable.isBlank(s)) {
            this.statusText = defaultStatusText(i);
        } else {
            this.statusText = s;
        }
    }

    private String defaultStatusText(int i) {
        switch (i) {
            case 200: {
                return "OK";
            }
        }
        return String.valueOf(i);
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return getHeader("Content-Type");
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (myServletOutputStream == null) {
            myServletOutputStream = new ServletOutputStreamImpl(exchange.getResponseBody(), () -> doTryCommit());
        }
        return myServletOutputStream;
    }

    private void ensureNotCommitted() {
        NAssert.requireFalse(isCommitted(), "commit");
    }

    private void doTryCommit() {
        if (!committed) {
            committed = true;
            try {
                exchange.sendResponseHeaders(status, contentLength);
                committed = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (printWriter == null) {
            printWriter = new PrintWriter(getOutputStream());
        }
        return printWriter;
    }

    @Override
    public void setCharacterEncoding(String s) {
        setHeader("Content-Type", s);
    }

    @Override
    public void setContentLength(int i) {
        setContentLengthLong(i);
    }

    @Override
    public void setContentType(String s) {
        setHeader("Content-Type", s);
    }

    @Override
    public void setBufferSize(int i) {
        this.bufferSize = i;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void flushBuffer() throws IOException {
        doTryCommit();
    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale locale) {
        setHeader("Content-Language", locale.toLanguageTag());
    }

    @Override
    public Locale getLocale() {
        String locale = getHeader("Content-Language");
        return NBlankable.isBlank(locale) ? null : new Locale(locale);
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String s) {
        Collection<String> headers = getHeaders(s);
        for (String header : headers) {
            return header;
        }
        return null;
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return exchange.getRequestHeaders().get(s);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return exchange.getRequestHeaders().keySet();
    }

    @Override
    public void setContentLengthLong(long len) {
        ensureNotCommitted();
        this.contentLength = len;
    }

    public void close() {
        if(printWriter!=null){
            printWriter.close();
            return;
        }
        try {
            this.getOutputStream().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
