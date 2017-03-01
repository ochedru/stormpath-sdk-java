package com.stormpath.sdk.impl.invitation;

import com.stormpath.sdk.impl.query.DefaultCriteria;
import com.stormpath.sdk.invitation.InvitationCriteria;
import com.stormpath.sdk.invitation.InvitationOptions;

public class DefaultInvitationCriteria extends DefaultCriteria<InvitationCriteria, InvitationOptions> implements InvitationCriteria{

    public DefaultInvitationCriteria() {
        super(new DefaultInvitationOptions());
    }

    @Override
    public InvitationCriteria orderByEmail() {
        return orderBy(DefaultInvitation.EMAIL);
    }

    @Override
    public InvitationCriteria withCustomData() {
        getOptions().withCustomData();
        return this;
    }

}
