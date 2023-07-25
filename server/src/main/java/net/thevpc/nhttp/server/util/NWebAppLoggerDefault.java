package net.thevpc.nhttp.server.util;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTexts;

import java.io.*;

public class NWebAppLoggerDefault implements NWebLogger {
    private PrintStream out;
    private NSession session;
    private NTexts txt;
    private File file;
    private long fileSize;

    public NWebAppLoggerDefault(File file, NSession session) {
        try {
            this.file = file.getCanonicalFile();
        } catch (IOException e) {
            this.file = file.getAbsoluteFile();
        }
        this.session = session;
        this.txt = NTexts.of(session);
    }

    private PrintStream _out() {
        if (out == null) {
            if (this.file.getParentFile() != null) {
                this.file.getParentFile().mkdirs();
            }
            if(file.exists()){
                fileSize=file.length();
            }
            try {
                out = new PrintStream(new FileOutputStream(file, true));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
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

    private void _out(NMsg msg){
        String string = txt.ofText(msg).filteredText();
        fileSize+=string.getBytes().length;
        _out().println(string);
    }

    public String getFilePath() {
        return file.getPath();
    }

    @Override
    public String toString() {
        return getFilePath();
    }
}

