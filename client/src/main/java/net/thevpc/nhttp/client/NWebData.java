package net.thevpc.nhttp.client;

import net.thevpc.nuts.io.NPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public interface NWebData {
    static NWebData of(byte[] bytes) {
        return new NWebDataAsBytes(bytes);
    }

    static NWebData of(NPath bytes) {
        return new NWebDataAsNPath(bytes);
    }

    long length();

    InputStream stream();

    byte[] bytes();

    default void writeTo(OutputStream os) {
        byte[] buffer = new byte[4096];
        int r;
        try (InputStream stream = stream()) {
            while ((r = stream.read(buffer)) > 0) {
                os.write(buffer, 0, r);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
