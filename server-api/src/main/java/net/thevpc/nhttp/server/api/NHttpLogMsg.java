package net.thevpc.nhttp.server.api;

import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;

public class NHttpLogMsg {
    private Instant date;
    private Level level;
    private String source;
    private String method;
    private String url;
    private Double memoryUsage;
    private NMsg message;
    private String prefix;
    private NDuration duration;

    public NDuration getDuration() {
        return duration;
    }

    public NHttpLogMsg setDuration(NDuration duration) {
        this.duration = duration;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public NHttpLogMsg setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public Instant getDate() {
        return date;
    }

    public NHttpLogMsg setDate(Instant date) {
        this.date = date;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    public NHttpLogMsg setLevel(Level level) {
        this.level = level;
        return this;
    }

    public String getSource() {
        return source;
    }

    public NHttpLogMsg setSource(String source) {
        this.source = source;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public NHttpLogMsg setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NHttpLogMsg setUrl(String url) {
        this.url = url;
        return this;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public NHttpLogMsg setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
        return this;
    }

    public NMsg getMessage() {
        return message;
    }

    public NHttpLogMsg setMessage(NMsg message) {
        this.message = message;
        return this;
    }

    public NHttpLogMsg validate() {
        if (getDate() == null) {
            setDate(Instant.now());
        }
        if (getMemoryUsage() == null) {
            Runtime rt = Runtime.getRuntime();
            double m = ((rt.totalMemory() - rt.freeMemory()) * 100.0 / rt.maxMemory());
            setMemoryUsage(m);
        }
        if (level == null) {
            level = Level.INFO;
        }
        String effTraceMessagePrefix = getPrefix();
        if (effTraceMessagePrefix == null) {
            effTraceMessagePrefix = "";
        }
        if (!effTraceMessagePrefix.isEmpty() && !Character.isWhitespace(effTraceMessagePrefix.charAt(effTraceMessagePrefix.length() - 1))) {
            effTraceMessagePrefix = effTraceMessagePrefix + " ";
        }
        setPrefix(effTraceMessagePrefix);
        return this;
    }

    public NMsg buildMessage() {
        validate();
        String effTraceMessagePrefix = this.getPrefix();
        if (effTraceMessagePrefix == null) {
            effTraceMessagePrefix = "";
        }
        if (!effTraceMessagePrefix.isEmpty() && !Character.isWhitespace(effTraceMessagePrefix.charAt(effTraceMessagePrefix.length() - 1))) {
            effTraceMessagePrefix = effTraceMessagePrefix + " ";
        }
        NDuration d = duration;
        String e = "";
        if (d != null) {
            d = d.withSmallestUnit(ChronoUnit.MILLIS);
            e = NStringUtils.repeat(' ', Math.max(10 - d.toString().length(), 0));
        } else {
            e = NStringUtils.repeat(' ', 10);
        }
        return NMsg.ofC(
                "[%s][%s%s][M%.3f%%] %8s %s %6s %s %s%s",
                Instant.now(),
                e,
                d == null ? "" : d,
                this.getMemoryUsage(),
                this.getLevel(),
                this.getSource(),
                NMsg.ofStyled(NStringUtils.firstNonNull(this.getMethod(), "INTERN"), NTextStyle.primary1()),
                NMsg.ofStyled(NStringUtils.firstNonNull(this.getUrl(), "|"), NTextStyle.path()),
                effTraceMessagePrefix,
                this.getMessage()
        );
    }

    public NHttpLogMsg appendPrefix(String traceMessagePrefix) {
        String old = this.prefix;
        this.prefix = (old == null ? "" : old) + (traceMessagePrefix == null ? "" : traceMessagePrefix);
        return this;
    }

}
