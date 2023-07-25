package net.thevpc.nhttp.server.error;

import net.thevpc.nuts.NOptional;

public interface NWebErrorCodeAware {
    static NOptional<NWebErrorCode> codeOf(Object any){
        if(any instanceof NWebErrorCodeAware){
            return NOptional.of(((NWebErrorCodeAware) any).getAppErrorCode());
        }
        return NOptional.ofEmpty();
    }

    NWebErrorCode getAppErrorCode();
}
