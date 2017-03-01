package com.stormpath.sdk.invitation;

import com.stormpath.sdk.query.Criteria;

public interface InvitationCriteria extends Criteria<InvitationCriteria>, InvitationOptions<InvitationCriteria>{

    InvitationCriteria orderByEmail();

}
