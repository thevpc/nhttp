package net.thevpc.nhttp.server.api;

public interface HttpHandlerController {
    void init(NWebConfig config);

    void service(NWebHttpHandlerContext controller);
}
