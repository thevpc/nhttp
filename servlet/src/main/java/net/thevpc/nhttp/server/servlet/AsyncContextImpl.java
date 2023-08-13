package net.thevpc.nhttp.server.servlet;

import javax.servlet.*;

class AsyncContextImpl implements AsyncContext {
    private HttpServletRequestImpl request;
    private HttpServletResponseImpl response;

    public AsyncContextImpl(HttpServletRequestImpl request, HttpServletResponseImpl response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public ServletRequest getRequest() {
        return request;
    }

    @Override
    public ServletResponse getResponse() {
        return response;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return false;
    }

    @Override
    public void dispatch() {

    }

    @Override
    public void dispatch(String path) {

    }

    @Override
    public void dispatch(ServletContext context, String path) {

    }

    @Override
    public void complete() {

    }

    @Override
    public void start(Runnable run) {
        run.run();
    }

    @Override
    public void addListener(AsyncListener listener) {

    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {

    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public void setTimeout(long timeout) {

    }

    @Override
    public long getTimeout() {
        return 0;
    }
}
