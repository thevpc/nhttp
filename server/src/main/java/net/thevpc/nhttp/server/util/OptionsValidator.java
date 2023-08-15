package net.thevpc.nhttp.server.util;

import net.thevpc.nhttp.server.api.NWebServerOptions;

public class OptionsValidator {

    public static int validateBacklog(Integer backLog) {
        int backlog = backLog == null ? -1 : backLog;
        if (backlog < 0) {
            return 0;
        }
        return backlog;
    }

    public static int validatePort(Integer port) {
        int p=port==null?8080:port;
        if(port<1024){
            p=8080;
        }
        return p;
    }

    public static NWebServerOptions validateOptions(NWebServerOptions options) {
        if(options==null) {
            options = new NWebServerOptions();
        }else{
            options = options.copy();
        }
        int port = options.getPort() == null ? -1 : options.getPort();
        if (port <= 0) {
            if (options.getSsl() == null || !options.getSsl()) {
                options.setSsl(false);
                port = 8080;
            } else {
                port = 8443;
            }
        } else {
            if (options.getSsl() == null) {
                if (port == 433
                        || (port >= 8400 && port <= 8499)
                        || (port >= 4000 && port <= 4999)
                ) {
                    options.setSsl(true);
                } else {
                    options.setSsl(false);
                }
            }
        }

        options.setPort(port);
        int backlog = options.getBacklog() == null ? -1 : options.getBacklog();
        if (backlog < 0) {
            backlog = 0;
        }
        options.setBacklog(backlog);
        ExecutorBuilder executorBuilder = new ExecutorBuilder()
                .setIdlTimeSeconds(options.getIdlTimeSeconds())
                .setQueueSize(options.getQueueSize())
                .setMaxConnexions(options.getMaxConnexions())
                .setMinConnexions(options.getMinConnexions())
                .validateOptions();

        options.setIdlTimeSeconds(executorBuilder.getIdlTimeSeconds());
        options.setMinConnexions(executorBuilder.getMinConnexions());
        options.setMaxConnexions(executorBuilder.getMaxConnexions());
        options.setQueueSize(executorBuilder.getQueueSize());
        return options;
    }
}
