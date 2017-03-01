package com.stormpath.sdk.impl.invitation;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.impl.ds.InternalDataStore;
import com.stormpath.sdk.impl.resource.AbstractExtendableInstanceResource;
import com.stormpath.sdk.impl.resource.Property;
import com.stormpath.sdk.impl.resource.ResourceReference;
import com.stormpath.sdk.impl.resource.StringProperty;
import com.stormpath.sdk.invitation.Invitation;
import com.stormpath.sdk.organization.Organization;
import java.util.Map;

public class DefaultInvitation extends AbstractExtendableInstanceResource implements Invitation{

    static final StringProperty EMAIL = new StringProperty("email");
    static final StringProperty CALLBACK_URI = new StringProperty("callbackUri");
    static final ResourceReference<Application> APPLICATION = new ResourceReference<>("application", Application.class);
    static final ResourceReference<Organization> ORGANIZATION = new ResourceReference<>("organization", Organization.class);
    static final ResourceReference<Account> FROM_ACCOUNT = new ResourceReference<>("fromAccount", Account.class);

    static final Map<String, Property> PROPERTY_DESCRIPTORS = createPropertyDescriptorMap(EMAIL, CALLBACK_URI, APPLICATION, ORGANIZATION, FROM_ACCOUNT, CUSTOM_DATA
    );

    public DefaultInvitation(InternalDataStore dataStore) {
        super(dataStore);
    }

    public DefaultInvitation(InternalDataStore dataStore, Map<String, Object> properties) {
        super(dataStore, properties);
    }

    @Override
    public Map<String, Property> getPropertyDescriptors() {
        return PROPERTY_DESCRIPTORS;
    }

    @Override
    public String getEmail() {
        return getString(EMAIL);
    }

    @Override
    public String getCallbackUri() {
        return getString(CALLBACK_URI);
    }

    @Override
    public Application getApplication() {
        return getResourceProperty(APPLICATION);
    }

    @Override
    public Account getFromAccount() {
        return getResourceProperty(FROM_ACCOUNT);
    }

    @Override
    public Organization getOrganization() {
        return getResourceProperty(ORGANIZATION);
    }

    @Override
    public Invitation setEmail(String email) {
        setProperty(EMAIL, email);
        return this;
    }

    @Override
    public Invitation setCallbackUri(String callbackUri) {
        setProperty(CALLBACK_URI, callbackUri);
        return this;
    }

    @Override
    public Invitation setApplication(Application application) {
        setResourceProperty(APPLICATION, application);
        return this;
    }

    @Override
    public Invitation setFromAccount(Account fromAccount) {
        setResourceProperty(FROM_ACCOUNT, fromAccount);
        return this;
    }

    @Override
    public Invitation setOrganization(Organization organization) {
        setResourceProperty(ORGANIZATION, organization);
        return this;
    }

    @Override
    public void delete() {
        getDataStore().delete(this);
    }


}
