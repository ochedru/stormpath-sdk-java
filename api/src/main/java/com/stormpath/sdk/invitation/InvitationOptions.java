package com.stormpath.sdk.invitation;

import com.stormpath.sdk.query.Options;

public interface InvitationOptions<T> extends Options {

    T withCustomData();
    
}
