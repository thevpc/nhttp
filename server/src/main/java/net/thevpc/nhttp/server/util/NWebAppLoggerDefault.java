package net.thevpc.nhttp.server.util;

import net.thevpc.nhttp.server.api.NHttpLogMsg;
import net.thevpc.nhttp.server.api.NWebLogger;
import net.thevpc.nuts.NErr;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.text.NTexts;

import java.io.*;
import java.util.logging.Level;

public class NWebAppLoggerDefault implements NWebLogger {
    private PrintStream out;
    private NTexts txt;
    private File file;
    private File roll1;
    private long maxFileSize = 1024;
    private long fileSize;
    private File baseFile;
    private long baseMaxFileSize;

    public NWebAppLoggerDefault(File file, long maxFileSize) {
        this.baseFile = file;
        this.maxFileSize = maxFileSize;
        try {
            this.file = file.getCanonicalFile();
        } catch (IOException e) {
            this.file = file.getAbsoluteFile();
        }
        this.maxFileSize = maxFileSize <= 0 ? Long.MAX_VALUE : maxFileSize;
        this.roll1 = new File(this.file.getParent(), this.file.getName() + ".1");
        this.txt = NTexts.of();
    }

    public File getBaseFile() {
        return baseFile;
    }

    public long getBaseMaxFileSize() {
        return baseMaxFileSize;
    }

    private synchronized void open() {
        if (out == null) {
            if (this.file.getParentFile() != null) {
                this.file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                fileSize = file.length();
            } else {
                fileSize = 0;
            }
            try {
                out = new PrintStream(new FileOutputStream(file, true));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private PrintStream _out() {
        open();
        return out;
    }

    @Override
    public void log(NHttpLogMsg msg) {
        NMsg m = msg.buildMessage();
        if (msg.getLevel().intValue() >= Level.SEVERE.intValue()) {
            NErr.println(m);
            _out(m);
        } else {
            NOut.println(m);
            _out(m);
        }
    }

    private void _out(NMsg msg) {
        String string = txt.of(msg).filteredText();
        long newBytesCount = string.getBytes().length;
        if (fileSize >= maxFileSize || fileSize + newBytesCount >= maxFileSize) {
            roll();
        }
        fileSize += newBytesCount;
        _out().println(string);

    }

    public String getFilePath() {
        return file.getPath();
    }

    @Override
    public String toString() {
        return getFilePath();
    }

    public synchronized void roll() {
        close();
        try {
            if (this.roll1.exists()) {
                this.roll1.delete();
            }
        } catch (Exception e) {
            //
        }
        try {
            file.renameTo(this.roll1);
        } catch (Exception e) {
            //
        }
        try {
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            //
        }
        open();
    }

    public synchronized void close() {
        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                //
            }
            out = null;
        }
    }
}

