package net.thevpc.nhttp.server.util;

//import com.google.gson.GsonBuilder;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.GsonBuilder;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.text.NContentType;

import java.io.BufferedWriter;
import java.io.Reader;

public class JsonUtils {
    private static final boolean useNuts = false;

    public static String toJson(Object object) {
        if (useNuts) {
            return NElementWriter.ofJson().formatPlain(object);
        }
        return getGsonBuilder().setPrettyPrinting().create().toJson(object);
    }

    private static GsonBuilder getGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        Converters.registerAll(builder);
        return builder;
    }

    public static <T> T fromJson(String json, Class<T> type) {
        if (useNuts) {
            T r = NElementReader.ofJson().read(json, type);
            return r;
        }
        return getGsonBuilder().setPrettyPrinting().create().fromJson(json, type);
    }

    public static <T> T fromContentType(String json, Class<T> type, NContentType contentType) {
        return NElementReader.of().contentType(contentType).ntf(false).read(json, type);
    }

    public static <T> T fromJson(Reader json, Class<T> type) {
        if (useNuts) {
            T r = NElementReader.ofJson().read(json, type);
            return r;
        }
        return getGsonBuilder().setPrettyPrinting().create().fromJson(json, type);
    }

    public static void toJson(Object object, BufferedWriter r) {
        if (useNuts) {
            NElementWriter.ofJson().write(object,r);
        } else {
            getGsonBuilder().setPrettyPrinting().create().toJson(object, r);
        }
    }

    public static <T> T copy(T t, NSession session) {
        return (T) fromJson(toJson(t), t.getClass());
    }
}
