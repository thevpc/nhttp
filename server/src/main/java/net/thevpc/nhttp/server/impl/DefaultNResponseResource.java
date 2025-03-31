//package net.thevpc.nhttp.server.impl;
//
//import net.thevpc.nhttp.server.api.NResponseResource;
//import net.thevpc.nhttp.server.util.JsonUtils;
//import net.thevpc.nuts.io.NPath;
//import net.thevpc.nuts.util.NBlankable;
//import net.thevpc.nuts.util.NMsgCode;
//import net.thevpc.nuts.util.NStringUtils;
//import net.thevpc.nuts.web.NHttpCode;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//public class DefaultNResponseResource implements NResponseResource {
//
//    public DefaultNResponseResource(NWebServerHttpContextImpl ctx) {
//        this.ctx = ctx;
//    }
//
//
//    @Override
//    public String getContentType() {
//        return contentType;
//    }
//
//    @Override
//    public NResponseResource setContentType(String contentType) {
//        this.contentType = contentType;
//        return this;
//    }
//
//    @Override
//    public NHttpCode getCode() {
//        return code;
//    }
//
//    @Override
//    public NResponseResource setCode(NHttpCode code) {
//        this.code = code;
//        return this;
//    }
//}
