package com.stormpath.sdk.invitation;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.organization.Organization;
import com.stormpath.sdk.resource.Auditable;
import com.stormpath.sdk.resource.Deletable;
import com.stormpath.sdk.resource.Extendable;
import com.stormpath.sdk.resource.Resource;
import com.stormpath.sdk.resource.Saveable;

public interface Invitation extends Resource, Saveable, Deletable, Extendable, Auditable {

    String getEmail();

    String getCallbackUri();

    Application getApplication();

    Account getFromAccount();

    Organization getOrganization();

    Invitation setEmail(String email);

    Invitation setCallbackUri(String callbackUri);

    Invitation setApplication(Application application);

    Invitation setFromAccount(Account fromAccount);

    Invitation setOrganization(Organization organization);
}
