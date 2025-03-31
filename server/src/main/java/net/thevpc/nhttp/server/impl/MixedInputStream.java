package net.thevpc.nhttp.server.impl;

import net.thevpc.nuts.io.NIOException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MixedInputStream implements AutoCloseable {
    public static class Line {
        byte[] content;
        byte[] separator;

        public Line(byte[] content, byte[] separator) {
            this.content = content;
            this.separator = separator;
        }

        public String getContentString() {
            return new String(content, StandardCharsets.UTF_8);
        }

        public String getSeparatorString() {
            return new String(separator, StandardCharsets.UTF_8);
        }

        public byte[] getContent() {
            return content;
        }

        public byte[] getSeparator() {
            return separator;
        }
    }

    private BufferedInputStream in;

    public MixedInputStream(InputStream in) {
        this.in = new BufferedInputStream(in);
    }

    public Line readLine(int maxLineLength) {
        try {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            ByteArrayOutputStream separator = new ByteArrayOutputStream();
            while (true) {
                int r = in.read();
                if (r == -1) {
                    byte[] ba = content.toByteArray();
                    byte[] bs = separator.toByteArray();
                    if (ba.length == 0 && bs.length == 0) {
                        return null;
                    }return new Line(content.toByteArray(), separator.toByteArray());
                }
                if (r == 13) {
                    in.mark(1);
                    int r2 = in.read();
                    if (r2 < 0) {
                        separator.write(r);
                        return new Line(content.toByteArray(), separator.toByteArray());
                    } else if (r2 == 10) {
                        separator.write(r);
                        separator.write(r2);
                        return new Line(content.toByteArray(), separator.toByteArray());
                    } else {
                        in.reset();
                        separator.write(r);
                        return new Line(content.toByteArray(), separator.toByteArray());
                    }
                } else if (r == 10) {
                    separator.write(r);
                    return new Line(content.toByteArray(), separator.toByteArray());
                } else {
                    content.write(r);
                    if (content.size() > maxLineLength) {
                        return new Line(content.toByteArray(), separator.toByteArray());
                    }
                }
            }
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public void close()  {
        try {
            in.close();
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }
}
