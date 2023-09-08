package net.thevpc.nhttp.server.util;

//import com.google.gson.GsonBuilder;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.GsonBuilder;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElements;

import java.io.BufferedWriter;
import java.io.Reader;

public class JsonUtils {
    private static final boolean useNuts = false;

    public static String toJson(Object object, NSession session) {
        if (useNuts) {
            return NElements.of(session).json().setNtf(false).setValue(object).format().filteredText();
        }
        return getGsonBuilder().setPrettyPrinting().create().toJson(object);
    }

    private static GsonBuilder getGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();
        Converters.registerAll(builder);
        return builder;
    }

    public static <T> T fromJson(String json, Class<T> type, NSession session) {
        if (useNuts) {
            T r = NElements.of(session).json().setNtf(false).parse(json, type);
            return r;
        }
        return getGsonBuilder().setPrettyPrinting().create().fromJson(json, type);
    }

    public static <T> T fromJson(Reader json, Class<T> type, NSession session) {
        if (useNuts) {
            T r = NElements.of(session).json().setNtf(false).parse(json, type);
            return r;
        }
        return getGsonBuilder().setPrettyPrinting().create().fromJson(json, type);
    }

    public static void toJson(Object object, BufferedWriter r, NSession session) {
        if (useNuts) {
            NElements.of(session).json().setNtf(false).setValue(object).println(r);
        } else {
            getGsonBuilder().setPrettyPrinting().create().toJson(object, r);
        }
    }

    public static <T> T copy(T t, NSession session) {
        return (T) fromJson(toJson(t, session), t.getClass(), session);
    }
}
