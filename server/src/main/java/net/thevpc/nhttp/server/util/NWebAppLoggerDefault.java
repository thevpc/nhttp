package net.thevpc.nhttp.server.util;

import net.thevpc.nhttp.server.api.NWebLogger;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTexts;

import java.io.*;

public class NWebAppLoggerDefault implements NWebLogger {
    private PrintStream out;
    private NSession session;
    private NTexts txt;
    private File file;
    private File roll1;
    private long maxFileSize = 1024;
    private long fileSize;

    public NWebAppLoggerDefault(File file, long maxFileSize,NSession session) {
        try {
            this.file = file.getCanonicalFile();
        } catch (IOException e) {
            this.file = file.getAbsoluteFile();
        }
        this.maxFileSize=maxFileSize<=0?Long.MAX_VALUE:maxFileSize;
        this.roll1 = new File(this.file.getParent(), this.file.getName() + ".1");
        this.session = session;
        this.txt = NTexts.of(session);
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
    public synchronized void out(NMsg msg) {
        session.out().println(msg);
        _out(msg);
    }

    @Override
    public synchronized void err(NMsg msg) {
        session.err().println(msg);
        _out(msg);
    }

    private void _out(NMsg msg) {
        String string = txt.ofText(msg).filteredText();
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
            out=null;
        }
    }
}

