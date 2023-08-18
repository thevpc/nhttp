package net.thevpc.nhttp.server.api;

public class NWebServerOptions implements Cloneable{
    private Integer port;
    private boolean reset;
    private Boolean ssl;
    private Integer backlog;
    private Integer minConnexions;
    private Integer maxConnexions;
    private Integer queueSize;
    private Integer idlTimeSeconds;
    private String pidFile;
    private String logFile;
    private String contextPath;

    public boolean isReset() {
        return reset;
    }

    public String getPidFile() {
        return pidFile;
    }

    public NWebServerOptions setPidFile(String pidFile) {
        this.pidFile = pidFile;
        return this;
    }

    public String getLogFile() {
        return logFile;
    }

    public NWebServerOptions setLogFile(String logFile) {
        this.logFile = logFile;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public NWebServerOptions setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public NWebServerOptions setSsl(Boolean ssl) {
        this.ssl = ssl;
        return this;
    }

    public Integer getBacklog() {
        return backlog;
    }

    public NWebServerOptions setBacklog(Integer backlog) {
        this.backlog = backlog;
        return this;
    }

    public Integer getMinConnexions() {
        return minConnexions;
    }

    public NWebServerOptions setMinConnexions(Integer minConnexions) {
        this.minConnexions = minConnexions;
        return this;
    }

    public Integer getMaxConnexions() {
        return maxConnexions;
    }

    public NWebServerOptions setMaxConnexions(Integer maxConnexions) {
        this.maxConnexions = maxConnexions;
        return this;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public NWebServerOptions setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public Integer getIdlTimeSeconds() {
        return idlTimeSeconds;
    }

    public NWebServerOptions setIdlTimeSeconds(Integer idlTimeSeconds) {
        this.idlTimeSeconds = idlTimeSeconds;
        return this;
    }

    public boolean getReset() {
        return reset;
    }

    public NWebServerOptions setReset(boolean reset) {
        this.reset = reset;
        return this;
    }

    public NWebServerOptions copy() {
        try {
            return (NWebServerOptions) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getContextPath() {
        return contextPath;
    }

    public NWebServerOptions setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }
}
