package net.thevpc.nhttp.server.api;

public interface NWebServlet {
    void init(NWebConfig config);

    void service(NWebHttpHandlerContext controller);
}
