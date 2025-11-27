package net.thevpc.nhttp.server.api;

public class NWebServerOptions implements Cloneable{
    private String hostName;
    private Integer port;
    private boolean reset;
    private boolean ignoreExistingPidFile;
    private Boolean tls;
    private Integer backlog;
    private Integer minConnections;
    private Integer maxConnections;
    private Integer queueSize;
    private Integer idlTimeSeconds;
    private String pidFile;
    private String logFile;
    private Long logFileMaxSize;
    private String contextPath;

    public String getHostName() {
        return hostName;
    }

    public NWebServerOptions setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public boolean isReset() {
        return reset;
    }

    public boolean isIgnoreExistingPidFile() {
        return ignoreExistingPidFile;
    }

    public NWebServerOptions setIgnoreExistingPidFile(boolean ignoreExistingPidFile) {
        this.ignoreExistingPidFile = ignoreExistingPidFile;
        return this;
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

    public Boolean getTls() {
        return tls;
    }

    public NWebServerOptions setTls(Boolean tls) {
        this.tls = tls;
        return this;
    }

    public Integer getBacklog() {
        return backlog;
    }

    public NWebServerOptions setBacklog(Integer backlog) {
        this.backlog = backlog;
        return this;
    }

    public Integer getMinConnections() {
        return minConnections;
    }

    public NWebServerOptions setMinConnections(Integer minConnections) {
        this.minConnections = minConnections;
        return this;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public NWebServerOptions setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
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

    public Long getLogFileMaxSize() {
        return logFileMaxSize;
    }

    public NWebServerOptions setLogFileMaxSize(Long logFileMaxSize) {
        this.logFileMaxSize = logFileMaxSize;
        return this;
    }
}
