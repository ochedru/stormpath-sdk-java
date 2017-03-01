package com.stormpath.sdk.impl.invitation;

import com.stormpath.sdk.impl.query.DefaultOptions;
import com.stormpath.sdk.invitation.InvitationOptions;

public class DefaultInvitationOptions extends DefaultOptions<InvitationOptions> implements InvitationOptions<InvitationOptions>{

    @Override
    public InvitationOptions withCustomData() {
        return expand(DefaultInvitation.CUSTOM_DATA);
    }

}
