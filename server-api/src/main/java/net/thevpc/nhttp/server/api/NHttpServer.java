package net.thevpc.nhttp.server.api;

import com.sun.net.httpserver.HttpServer;

public interface NHttpServer {
    String getServerName();
    int getServerPort();
    NWebLogger getLogger();

    HttpServer getServer();

    NWebServerOptions getOptions();
}
