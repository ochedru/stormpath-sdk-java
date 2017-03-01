package com.stormpath.sdk.invitation;

import com.stormpath.sdk.lang.Classes;
import com.stormpath.sdk.query.Criterion;

public class Invitations {

    public static InvitationOptions<InvitationOptions> options() {
        return (InvitationOptions) Classes.newInstance("com.stormpath.sdk.impl.invitation.DefaultInvitationOptions");
    }

    public static InvitationCriteria criteria() {
        return (InvitationCriteria) Classes.newInstance("com.stormpath.sdk.impl.invitation.DefaultInvitationCriteria");
    }

    public static InvitationCriteria where(Criterion criterion) {
        return criteria().add(criterion);
    }
}
