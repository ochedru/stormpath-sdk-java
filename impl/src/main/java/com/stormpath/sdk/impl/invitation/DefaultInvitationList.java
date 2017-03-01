/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stormpath.sdk.impl.invitation;

import com.stormpath.sdk.impl.ds.InternalDataStore;
import com.stormpath.sdk.impl.resource.AbstractCollectionResource;
import static com.stormpath.sdk.impl.resource.AbstractCollectionResource.LIMIT;
import static com.stormpath.sdk.impl.resource.AbstractCollectionResource.OFFSET;
import com.stormpath.sdk.impl.resource.ArrayProperty;
import com.stormpath.sdk.impl.resource.Property;
import com.stormpath.sdk.invitation.Invitation;
import com.stormpath.sdk.invitation.InvitationList;
import java.util.Map;

/**
 *
 * @author Maxime
 */
public class DefaultInvitationList extends AbstractCollectionResource<Invitation> implements InvitationList{

    private static final ArrayProperty<Invitation> ITEMS = new ArrayProperty<Invitation>("items", Invitation.class);

    private static final Map<String,Property> PROPERTY_DESCRIPTORS = createPropertyDescriptorMap(OFFSET, LIMIT, ITEMS);

    public DefaultInvitationList(InternalDataStore dataStore) {
        super(dataStore);
    }

    public DefaultInvitationList(InternalDataStore dataStore, Map<String, Object> properties) {
        super(dataStore, properties);
    }

    public DefaultInvitationList(InternalDataStore dataStore, Map<String, Object> properties, Map<String, Object> queryParams) {
        super(dataStore, properties, queryParams);
    }

    @Override
    protected Class<Invitation> getItemType() {
        return Invitation.class;
    }

    @Override
    public Map<String, Property> getPropertyDescriptors() {
        return PROPERTY_DESCRIPTORS;
    }


}
