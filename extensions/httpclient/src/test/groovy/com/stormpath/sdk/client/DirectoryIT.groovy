/*
 * Copyright 2014 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.client

import com.stormpath.sdk.account.Account
import com.stormpath.sdk.account.Accounts
import com.stormpath.sdk.directory.AccountCreationPolicy
import com.stormpath.sdk.directory.Directories
import com.stormpath.sdk.directory.Directory
import com.stormpath.sdk.directory.DirectoryOptions
import com.stormpath.sdk.directory.PasswordPolicy
import com.stormpath.sdk.impl.provider.AbstractOAuthProvider
import com.stormpath.sdk.impl.resource.AbstractCollectionResource
import com.stormpath.sdk.impl.resource.AbstractResource
import com.stormpath.sdk.lang.Duration
import com.stormpath.sdk.mail.EmailStatus
import com.stormpath.sdk.organization.Organization
import com.stormpath.sdk.organization.OrganizationStatus
import com.stormpath.sdk.organization.Organizations
import com.stormpath.sdk.provider.*
import com.stormpath.sdk.provider.saml.SamlProvider
import com.stormpath.sdk.provider.social.SocialUserInfoMappingRules
import com.stormpath.sdk.provider.social.UserInfoMappingRule
import com.stormpath.sdk.provider.social.UserInfoMappingRules
import com.stormpath.sdk.saml.AttributeStatementMappingRule
import com.stormpath.sdk.saml.AttributeStatementMappingRules
import com.stormpath.sdk.saml.SamlAttributeStatementMappingRules
import com.stormpath.sdk.schema.Schema
import org.testng.annotations.Test

import java.lang.reflect.Field
import java.util.concurrent.TimeUnit

import static org.testng.Assert.assertNull
import static org.testng.Assert.assertNotNull
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertNotEquals
import static org.testng.Assert.assertNotSame
import static org.testng.Assert.assertTrue
import static org.testng.Assert.assertFalse
import static org.testng.Assert.fail

/**
 *
 * @since 0.8.1
 */
class DirectoryIT extends ClientIT {

    /**
     * Asserts fix for <a href="https://github.com/stormpath/stormpath-sdk-java/pull/22">Pull Request 22</a>.
     */
    @Test
    void testCreateAndDeleteDirectory() {

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateAndDeleteDirectory")
        dir = client.currentTenant.createDirectory(dir);
        deleteOnTeardown(dir)

        assertNotNull dir.href
    }

    /**
     * Asserts fix for <a href="https://github.com/stormpath/stormpath-sdk-java/issues/12">Issue #12</a>
     */
    @Test
    void testDeleteAccount() {

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testDeleteAccount")
        dir = client.currentTenant.createDirectory(dir)
        deleteOnTeardown(dir)

        def email = 'johndeleteme@testmail.stormpath.com'

        Account account = client.instantiate(Account)
        account = account.setGivenName('John')
                .setSurname('DELETEME')
                .setEmail(email)
                .setPassword('Changeme1!')

        dir.createAccount(account)

        String href = account.href

        //verify it was created:
        Account retrieved = dir.getAccounts(Accounts.where(Accounts.email().eqIgnoreCase(email))).iterator().next()
        assertEquals(href, retrieved.href)

        //test delete:
        retrieved.delete()

        def list = dir.getAccounts(Accounts.where(Accounts.email().eqIgnoreCase(email)))
        assertFalse list.iterator().hasNext() //no results
    }

    /**
     * Asserts <a href="https://github.com/stormpath/stormpath-sdk-java/issues/58">Issue 58</a>.
     * @since 1.0.RC
     */
    @Test
    void testCreateDirectoryViaTenantActions() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateDirectoryViaTenantActions")
        dir = client.createDirectory(dir);
        deleteOnTeardown(dir)
        assertNotNull dir.href
    }

    /**
     * @since 1.2.0
     */
    @Test
    void testFilterDirectories() {
        def tenant = client.currentTenant

        Directory dir1 = client.instantiate(Directory)
        dir1.name = uniquify("Java SDK: DirectoryIT.testFilterDirectories01")
        dir1.description = 'testFilterDirectories01'
        dir1 = client.createDirectory(dir1);
        deleteOnTeardown(dir1)
        assertNotNull dir1.href

        Directory dir2 = client.instantiate(Directory)
        dir2.name = uniquify("Java SDK: DirectoryIT.testFilterDirectories02")
        dir2.description = 'testFilterDirectories02'
        dir2 = client.createDirectory(dir2);
        deleteOnTeardown(dir2)
        assertNotNull dir2.href

        //verify that the filter search works with a combination of criteria
        def foundDirs2 = tenant.getDirectories(Directories.where(Directories.filter('testFilterDirectories02')).and(Directories.description().endsWithIgnoreCase('02')))
        def foundDir2 = foundDirs2.iterator().next()
        assertEquals(foundDir2.href, dir2.href)

        //verify that the filter search works
        def allDirs = tenant.getDirectories(Directories.where(Directories.filter('testFilterDirectories')))
        assertEquals(allDirs.size(), 2)

        //verify that the filter search returns an empty collection if there is no match
        def emptyCollection = tenant.getDirectories(Directories.where(Directories.filter('not_found')))
        assertTrue(emptyCollection.size() == 0)

        //verify that a non matching criteria added to a matching criteria is working as a final non matching criteria
        //ie. there are no properties matching 'not_found' but there are 1 account matching 'description=02'
        def emptyCollection2 = tenant.getDirectories(Directories.where(Directories.filter('not_found')).and(Directories.description().endsWithIgnoreCase('02')))
        assertTrue(emptyCollection2.size() == 0)

        //verify that the filter search match with substrings
        def allOrgs2 = tenant.getDirectories(Directories.where(Directories.filter("FilterDirectories")))
        assertEquals(allOrgs2.size(), 2)

        //test delete:
        for (def dir : allDirs){
            dir.delete()
        }
    }

    /**
     * @since 1.0.RC
     */
    @Test
    void testCreateDirectoryRequestViaTenantActions() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateDirectoryRequestViaTenantActions")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.GOOGLE.builder()
                .setClientId("616598318417021")
                .setClientSecret("c0ad961d45fdc0310c1c7d67c8f1d800")
                .setRedirectUri("http://localhost")
                .build()
        ).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
    }

    /**
     * @since 1.0.RC8
     */
    @Test
    void testCreateSamlDirectoryWithNoAttributeStatementMappingRules() {

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateSamlDirectoryWithNoAttributeStatementMappingRules")

        def validX509Cert = '''-----BEGIN CERTIFICATE-----
            MIIDBjCCAe4CCQDkkfBwuV3jqTANBgkqhkiG9w0BAQUFADBFMQswCQYDVQQGEwJV
            UzETMBEGA1UECBMKU29tZS1TdGF0ZTEhMB8GA1UEChMYSW50ZXJuZXQgV2lkZ2l0
            cyBQdHkgTHRkMB4XDTE1MTAxNDIyMDUzOFoXDTE2MTAxMzIyMDUzOFowRTELMAkG
            A1UEBhMCVVMxEzARBgNVBAgTClNvbWUtU3RhdGUxITAfBgNVBAoTGEludGVybmV0
            IFdpZGdpdHMgUHR5IEx0ZDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB
            ALuZBSfp4ecigQGFL6zawVi9asVstXHy3cpj3pPXjDx5Xj4QlbBL7KbZhVd4B+j3
            Paacetpn8N0g06sYe1fIeddZE7PZeD2vxTLglriOCB8exH9ZAcYNHIGy3pMFdXHY
            lS7xXYWb+BNLVU7ka3tJnceDjhviAjICzQJs0JXDVQUeYxB80a+WtqJP+ZMbAxvA
            QbPzkcvK8CMctRSRqKkpC4gWSxUAJOqEmyvQVQpaLGrI2zFroD2Bgt0cZzBHN5tG
            wC2qgacDv16qyY+90rYgX/WveA+MSd8QKGLcpPlEzzVJp7Z5Boc3T8wIR29jaDtR
            cK4bWQ2EGLJiJ+Vql5qaOmsCAwEAATANBgkqhkiG9w0BAQUFAAOCAQEAmCND/4tB
            +yVsIZBAQgul/rK1Qj26FlyO0i0Rmm2OhGRhrd9JPQoZ+xCtBixopNICKG7kvUeQ
            Sk8Bku6rQ3VquxKtqAjNFeiLykd9Dn2HUOGpNlRcpzFXHtX+L1f34lMaT54qgWAh
            PgWkzh8xo5HT4M83DaG+HT6BkaVAQwIlJ26S/g3zJ00TrWRP2E6jlhR5KHLN+8eE
            D7/ENlqO5ThU5uX07/Bf+S0q5NK0NPuy0nO2w064kHdIX5/O64ktT1/MgWBV6yV7
            mg1osHToeo4WXGz2Yo6+VFMM3IKRqMDbkR7N4cNKd1KvEKrMaRE7vC14H/G5NSOh
            yl85oFHAdkguTA==
            -----END CERTIFICATE-----''';

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(
                Providers.SAML.builder()
                        .setEncodedX509SigningCert(validX509Cert)
                        .setRequestSignatureAlgorithm("RSA-SHA256")
                        .setSsoLoginUrl("https://idp.whatever.com/saml2/sso/login")
                        .setSsoLogoutUrl("https://idp.whatever.com/saml2/sso/logout")
                        .build())
                .build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href

        def provider = dir.provider
        assertNotNull provider.href
        assertNotNull provider.serviceProviderMetadata.href
        assertEquals provider.providerId, "saml"
        assertEquals provider.ssoLoginUrl, "https://idp.whatever.com/saml2/sso/login"
        assertEquals provider.ssoLogoutUrl, "https://idp.whatever.com/saml2/sso/logout"
    }

    /**
     * @since 1.0.RC8
     */
    @Test
    void testCreateSamlDirectoryWithAttributeStatementMappingRules() {

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateSamlDirectoryWithAttributeStatementMappingRules")


        AttributeStatementMappingRule rule1 = SamlAttributeStatementMappingRules.ruleBuilder()
                .setName("name1")
                .setNameFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified")
                .setAccountAttributes("customData.name1", "customData.otherName1")
                .build()

        AttributeStatementMappingRule rule2 = SamlAttributeStatementMappingRules.ruleBuilder()
                .setName("name2")
                .setNameFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified")
                .setAccountAttributes("customData.name2")
                .build()

        AttributeStatementMappingRules attributeStatementMappingRules = SamlAttributeStatementMappingRules.rulesBuilder()
                .addAttributeStatementMappingRule(rule1)
                .addAttributeStatementMappingRule(rule2)
                .build()

        def validX509Cert = '''-----BEGIN CERTIFICATE-----
            MIIDBjCCAe4CCQDkkfBwuV3jqTANBgkqhkiG9w0BAQUFADBFMQswCQYDVQQGEwJV
            UzETMBEGA1UECBMKU29tZS1TdGF0ZTEhMB8GA1UEChMYSW50ZXJuZXQgV2lkZ2l0
            cyBQdHkgTHRkMB4XDTE1MTAxNDIyMDUzOFoXDTE2MTAxMzIyMDUzOFowRTELMAkG
            A1UEBhMCVVMxEzARBgNVBAgTClNvbWUtU3RhdGUxITAfBgNVBAoTGEludGVybmV0
            IFdpZGdpdHMgUHR5IEx0ZDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB
            ALuZBSfp4ecigQGFL6zawVi9asVstXHy3cpj3pPXjDx5Xj4QlbBL7KbZhVd4B+j3
            Paacetpn8N0g06sYe1fIeddZE7PZeD2vxTLglriOCB8exH9ZAcYNHIGy3pMFdXHY
            lS7xXYWb+BNLVU7ka3tJnceDjhviAjICzQJs0JXDVQUeYxB80a+WtqJP+ZMbAxvA
            QbPzkcvK8CMctRSRqKkpC4gWSxUAJOqEmyvQVQpaLGrI2zFroD2Bgt0cZzBHN5tG
            wC2qgacDv16qyY+90rYgX/WveA+MSd8QKGLcpPlEzzVJp7Z5Boc3T8wIR29jaDtR
            cK4bWQ2EGLJiJ+Vql5qaOmsCAwEAATANBgkqhkiG9w0BAQUFAAOCAQEAmCND/4tB
            +yVsIZBAQgul/rK1Qj26FlyO0i0Rmm2OhGRhrd9JPQoZ+xCtBixopNICKG7kvUeQ
            Sk8Bku6rQ3VquxKtqAjNFeiLykd9Dn2HUOGpNlRcpzFXHtX+L1f34lMaT54qgWAh
            PgWkzh8xo5HT4M83DaG+HT6BkaVAQwIlJ26S/g3zJ00TrWRP2E6jlhR5KHLN+8eE
            D7/ENlqO5ThU5uX07/Bf+S0q5NK0NPuy0nO2w064kHdIX5/O64ktT1/MgWBV6yV7
            mg1osHToeo4WXGz2Yo6+VFMM3IKRqMDbkR7N4cNKd1KvEKrMaRE7vC14H/G5NSOh
            yl85oFHAdkguTA==
            -----END CERTIFICATE-----''';

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(
                Providers.SAML.builder()
                        .setEncodedX509SigningCert(validX509Cert)
                        .setRequestSignatureAlgorithm("RSA-SHA256")
                        .setSsoLoginUrl("https://idp.whatever.com/saml2/sso/login")
                        .setSsoLogoutUrl("https://idp.whatever.com/saml2/sso/logout")
                        .setAttributeStatementMappingRules(attributeStatementMappingRules)
                        .build())
                .build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href

        SamlProvider provider = dir.provider
        assertNotNull provider.href
        assertNotNull provider.serviceProviderMetadata.href
        assertEquals provider.providerId, "saml"
        assertEquals provider.ssoLoginUrl, "https://idp.whatever.com/saml2/sso/login"
        assertEquals provider.ssoLogoutUrl, "https://idp.whatever.com/saml2/sso/logout"

        // TODO : fix to be able to update SAML attributeStatementMappingRules for the provider
        /*AttributeStatementMappingRules rules = provider.attributeStatementMappingRules
        assertNotNull rules
        assertNotNull rules.items
        assertEquals rules.items.size() , 2
        UserInfoMappingRule userInfoMappingRule =
                SocialUserInfoMappingRules.ruleBuilder().setName("locale").setAccountAttributes("customData.locale").build()
        List<UserInfoMappingRule> mappingRules = new ArrayList<>();
        mappingRules.add(userInfoMappingRule)
        rules.setItems(SamlAttributeStatementMappingRules.rulesBuilder().addAttributeStatementMappingRule(
                SamlAttributeStatementMappingRules.ruleBuilder().setName("name3")
                .setNameFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified")
                .setAccountAttributes("customData.name3", "customData.otherName3")
                .build()
        ).build())
        def updatedRules = provider.attributeStatementMappingRules
        assertNotNull updatedRules
        assertNotNull updatedRules.items
        assertEquals updatedRules.items.size() , 1*/
    }

    /**
     * @since 1.0.0
     */
    @Test
    void testCreateLinkedInDirectoryRequestViaTenantActions() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateLinkedInDirectoryRequest")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.LINKEDIN.builder()
                .setClientId("73i1dq2fko01s2")
                .setClientSecret("wJhXc81l63qEOc43")
                .build()
        ).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
    }

    /**
     * @since 1.3.0
     */
    @Test
    void testCreateLinkedInDirectoryWithUserInfoMappingRules() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateLinkedInDirectoryRequest")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.LINKEDIN.builder()
                .setClientId("73i1dq2fko01s2")
                .setClientSecret("wJhXc81l63qEOc43").setUserInfoMappingRules(buildSampleUserInfoMappingRules()).build()).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
        assertUserInfoMappingRuleWasCreatedAndUpdate((AbstractOAuthProvider<LinkedInProvider>) dir.provider)
    }

    /**
     * @since 1.3.0
     */
    @Test
    void testCreateGoogleDirectoryWithUserInfoMappingRules() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateGoogleDirectoryRequest")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.GOOGLE.builder()
                .setClientId("73i1dq2fko01s2")
                .setClientSecret("wJhXc81l63qEOc43").setRedirectUri("https://myApp.com").
                setUserInfoMappingRules(buildSampleUserInfoMappingRules()).build()).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
        assertUserInfoMappingRuleWasCreatedAndUpdate((AbstractOAuthProvider<GoogleProvider>) dir.provider)
    }


    /**
     * @since 1.3.0
     */
    @Test
    void testCreateGithubDirectoryWithUserInfoMappingRules() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateGithubDirectoryRequest")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.GITHUB.builder()
                .setClientId("73i1dq2fko01s2")
                .setClientSecret("wJhXc81l63qEOc43").
                setUserInfoMappingRules(buildSampleUserInfoMappingRules()).build()).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
        assertUserInfoMappingRuleWasCreatedAndUpdate((AbstractOAuthProvider<GithubProvider>) dir.provider)
    }

    /**
     * @since 1.3.0
     */
    @Test
    void testCreateFacebookDirectoryWithUserInfoMappingRules() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateFacebookDirectoryRequest")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.FACEBOOK.builder()
                .setClientId("73i1dq2fko01s2")
                .setClientSecret("wJhXc81l63qEOc43")
                .setUserInfoMappingRules(buildSampleUserInfoMappingRules()).build()).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
        assertUserInfoMappingRuleWasCreatedAndUpdate((AbstractOAuthProvider<FacebookProvider>) dir.provider)

    }

    /**
     * @since 1.3.0
     */
    @Test
    void testCreateTwitterDirectoryWithUserInfoMappingRules() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateTwitterDirectoryRequest")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.TWITTER.builder()
                .setClientId("73i1dq2fko01s2")
                .setClientSecret("wJhXc81l63qEOc43")
                .setUserInfoMappingRules(buildSampleUserInfoMappingRules()).build()).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
        assertUserInfoMappingRuleWasCreatedAndUpdate((AbstractOAuthProvider<TwitterProvider>) dir.provider)

    }

    /**
     * @since 1.3.0
     */
    @Test(enabled = false) //TODO : enable this when the generic OAuth2 changes are there in prod (api.stormpath.com)
    void testCreateGenericOAuth2DirectoryWithUserInfoMappingRules() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateGenericOAuth2DirectoryRequest")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.OAUTH2.builder().setProviderId("imgur")
                .setClientId("73i1dq2fko01s2")
                .setClientSecret("wJhXc81l63qEOc43")
                .setAccessTokenType(AccessTokenType.BEARER)
                .setAuthorizationEndpoint("https://api.imgur.com/oauth2/authorize")
                .setTokenEndpoint("https://api.imgur.com/oauth2/token")
                .setResourceEndpoint("https://api.imgur.com/oauth2/token")
                .setUserInfoMappingRules(buildSampleUserInfoMappingRules()).build()).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
        assertUserInfoMappingRuleWasCreatedAndUpdate((AbstractOAuthProvider<TwitterProvider>) dir.provider)

    }

    void assertUserInfoMappingRuleWasCreatedAndUpdate(AbstractOAuthProvider provider) {

        UserInfoMappingRules rules = provider.getUserInfoMappingRules()
        assertNotNull rules
        assertNotNull rules.items
        assertEquals rules.items.size() , 2
        UserInfoMappingRule userInfoMappingRule =
                SocialUserInfoMappingRules.ruleBuilder().setName("locale").setAccountAttributes("customData.locale").build()
        List<UserInfoMappingRule> mappingRules = new ArrayList<>();
        mappingRules.add(userInfoMappingRule)
        rules.setItems(mappingRules)
        rules.save()
        def updatedRules = provider.userInfoMappingRules
        assertNotNull updatedRules
        assertNotNull updatedRules.items
        assertEquals updatedRules.items.size() , 1

    }

    UserInfoMappingRules buildSampleUserInfoMappingRules() {

        Set<String> accountAttributesId = new HashSet<>();
        accountAttributesId.add("customData.id")

        Set<String> accountAttributesName = new HashSet<>();
        accountAttributesName.add("customData.fullName")

        UserInfoMappingRule userInfoMappingRuleForId =
                SocialUserInfoMappingRules.ruleBuilder().setName("id").setAccountAttributes(accountAttributesId).build();

        UserInfoMappingRule userInfoMappingRuleForName =
                SocialUserInfoMappingRules.ruleBuilder().setName("name").setAccountAttributes(accountAttributesName).build();

        UserInfoMappingRules userInfoMappingRules =
                SocialUserInfoMappingRules.rulesBuilder().
                        addUserInfoMappingRule(userInfoMappingRuleForId).addUserInfoMappingRule(userInfoMappingRuleForName).build();

        userInfoMappingRules

    }


    /**
     * @since 1.0.RC
     */
    @Test
    void testGetDirectoriesViaTenantActions() {
        def dirList = client.getDirectories()
        assertNotNull dirList.href
    }

    /**
     * @since 1.0.RC
     */
    @Test(enabled = false)
    //ignoring because of sporadic Travis failures
    void testGetDirectoriesWithMapViaTenantActions() {
        def map = new HashMap<String, Object>()
        def dirList = client.getDirectories(map)
        assertNotNull dirList.href
    }

    /**
     * @since 1.0.RC
     */
    @Test
    void testGetDirectoriesWithDirCriteriaViaTenantActions() {
        def dirCriteria = Directories.criteria()
        def dirList = client.getDirectories(dirCriteria)
        assertNotNull dirList.href
    }

    /**
     * @since 1.0.0
     */
    @Test
    void testGetDirectoriesWithCustomData() {
        Directory directory = client.instantiate(Directory)
        directory.name = uniquify("Java SDK: DirectoryIT.testGetDirectoriesWithCustomData")
        directory.customData.put("someKey", "someValue")
        directory = client.createDirectory(directory);
        deleteOnTeardown(directory)
        assertNotNull directory.href

        def dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.getName())).withCustomData())

        def count = 0
        for (Directory dir : dirList) {
            count++
            assertNotNull(dir.getHref())
            assertEquals(dir.getCustomData().size(), 4)
        }
        assertEquals(count, 1)
    }

    /**
     * @since 1.0.RC4
     */
    @Test
    void testPasswordPolicy() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testPasswordPolicy")
        dir = client.createDirectory(dir);
        deleteOnTeardown(dir)
        def passwordPolicy = dir.getPasswordPolicy()
        assertNotNull passwordPolicy.href
        assertEquals passwordPolicy.getResetTokenTtlHours(), 24
        assertEquals passwordPolicy.getResetEmailStatus(), EmailStatus.ENABLED
        assertEquals passwordPolicy.getResetSuccessEmailStatus(), EmailStatus.ENABLED
        passwordPolicy.setResetTokenTtlHours(100)
                .setResetEmailStatus(EmailStatus.DISABLED)
                .setResetSuccessEmailStatus(EmailStatus.DISABLED)
        passwordPolicy.save()

        //Let's check that the new state is properly retrieved in a new instance
        def retrievedPasswordPolicy = client.getResource(passwordPolicy.href, PasswordPolicy.class)
        assertEquals retrievedPasswordPolicy.getResetTokenTtlHours(), 100
        assertEquals retrievedPasswordPolicy.getResetEmailStatus(), EmailStatus.DISABLED
        assertEquals retrievedPasswordPolicy.getResetSuccessEmailStatus(), EmailStatus.DISABLED
    }

    /**
     * @since 1.0.RC4
     */
    @Test
    void testListSize() {

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testListSize")
        dir = client.currentTenant.createDirectory(dir)
        deleteOnTeardown(dir)

        Account account01 = client.instantiate(Account)
        account01 = account01.setGivenName(uniquify('John01'))
                .setSurname('DELETEME')
                .setEmail(uniquify("john01deleteme") + "@testmail.stormpath.com")
                .setPassword('Changeme1!')

        dir.createAccount(account01)

        assertEquals(dir.getAccounts().getSize(), 1)

        def account02 = client.instantiate(Account)
        account02 = account02.setGivenName(uniquify('John02'))
                .setSurname('DELETEME')
                .setEmail(uniquify("john01deleteme") + "@testmail.stormpath.com")
                .setPassword('Changeme1!')

        dir.createAccount(account02)

        assertEquals(dir.getAccounts().getSize(), 2)

        def list = dir.getAccounts(Accounts.where(Accounts.email().eqIgnoreCase(account01.email)))

        assertEquals(list.getSize(), 1)

        list = dir.getAccounts(Accounts.where(Accounts.email().eqIgnoreCase("listMustBeEmpty")))

        assertEquals(list.getSize(), 0)

        list = dir.getAccounts(Accounts.criteria().limitTo(1))
        int count = 0

        def firstAccount = null
        def firstPage = null
        for (Account account : list) {
            def acrlist = (AbstractCollectionResource) list
            assertEquals(acrlist.currentPage.items.size(), 1)
            assertEquals(acrlist.currentPage.size, 2)

            assertNotNull(account.getHref())
            if (count == 0) {
                firstAccount = account
                firstPage = acrlist.currentPage
            } else {
                assertNotEquals(account.getHref(), firstAccount.getHref())
                //let's check that the items are actually moving
                assertNotSame(acrlist.currentPage, firstPage) //let's check that pages are actually moving
            }

            count++
        }
        assertEquals(count, 2)

        account01.delete()
        account02.delete()

        assertEquals(dir.getAccounts().getSize(), 0)
    }

    /**
     * @since 1.0.RC4.5
     */
    @Test
    void testAccountCreationPolicy() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testAccountCreationPolicy")
        dir = client.createDirectory(dir);
        deleteOnTeardown(dir)
        def accountPolicy = dir.getAccountCreationPolicy()
        assertNotNull accountPolicy.href

        // Validate default values
        assertEquals accountPolicy.getVerificationEmailStatus(), EmailStatus.DISABLED
        assertEquals accountPolicy.getVerificationSuccessEmailStatus(), EmailStatus.DISABLED
        assertEquals accountPolicy.getWelcomeEmailStatus(), EmailStatus.DISABLED

        //Set new values
        accountPolicy.setVerificationEmailStatus(EmailStatus.ENABLED)
        accountPolicy.setVerificationSuccessEmailStatus(EmailStatus.ENABLED)
        accountPolicy.setWelcomeEmailStatus(EmailStatus.ENABLED)
        accountPolicy.save()

        //Validate new values
        def retrievedAccountCreationPolicy = client.getResource(accountPolicy.href, AccountCreationPolicy.class)
        assertEquals(retrievedAccountCreationPolicy.getVerificationEmailStatus(), EmailStatus.ENABLED)
        assertEquals(retrievedAccountCreationPolicy.getVerificationSuccessEmailStatus(), EmailStatus.ENABLED)
        assertEquals(retrievedAccountCreationPolicy.getWelcomeEmailStatus(), EmailStatus.ENABLED)
    }

    /**
     * @since 1.0.RC4.6
     */
    @Test
    void testDirectoryExpansionWithoutCache() {

        Client client = buildClient(false);

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testDirectoryExpansion")
        dir = client.currentTenant.createDirectory(dir)
        deleteOnTeardown(dir)

        DirectoryOptions options = Directories.options().withAccounts()

        // test options created successfully
        assertNotNull options
        assertEquals options.expansions.size(), 1

        //Test the expansion worked by reading the internal properties of the directory
        Directory retrieved = client.getResource(dir.href, Directory.class, options)
        Map dirProperties = getValue(AbstractResource, retrieved, "properties")
        assertTrue dirProperties.get("accounts").size() > 1
        assertTrue dirProperties.get("accounts").get("size") == 0

        Account account = client.instantiate(Account)
        account = account.setGivenName('John')
                .setSurname('Doe')
                .setEmail('johndoe@testmail.stormpath.com')
                .setPassword('Changeme1!')
        dir.createAccount(account)

        //Test the expansion worked by reading the internal properties of the directory, it must contain the recently created account now
        retrieved = client.getResource(dir.href, Directory.class, options)
        dirProperties = getValue(AbstractResource, retrieved, "properties")
        assertTrue dirProperties.get("accounts").size() > 1
        assertTrue dirProperties.get("accounts").get("size") == 1
        assertEquals dirProperties.get("accounts").get("items")[0].get("givenName"), "John"
    }

    /**
     * Test for https://github.com/stormpath/stormpath-sdk-java/issues/164
     * @since 1.0.RC4.6
     */
    @Test
    void testDirectoryExpansionWithCache() {

        Client client = buildCountingClient()

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testDirectoryExpansion")
        dir = client.currentTenant.createDirectory(dir)
        deleteOnTeardown(dir)

        DirectoryOptions options = Directories.options().withAccounts() //collection resource

        // test options created successfully
        assertNotNull options
        assertEquals options.expansions.size(), 1

        //Test the expansion worked by reading the internal properties of the directory
        Directory retrieved = client.getResource(dir.href, Directory.class, options)
        Map dirProperties = getValue(AbstractResource, retrieved, "properties")
        assertTrue dirProperties.get("accounts").size() > 1
        assertTrue dirProperties.get("accounts").get("size") == 0

        Account account = client.instantiate(Account)
        account = account.setGivenName('John')
                .setSurname('Doe')
                .setEmail('johndoe@testmail.stormpath.com')
                .setPassword('Changeme1!')
        dir.createAccount(account)

        //Test the expansion worked by reading the internal properties of the directory, it must contain the recently created account now
        retrieved = client.getResource(dir.href, Directory.class, options)
        dirProperties = getValue(AbstractResource, retrieved, "properties")
        assertTrue dirProperties.get("accounts").size() > 1
        assertTrue dirProperties.get("accounts").get("size") == 1
        assertEquals dirProperties.get("accounts").get("items")[0].get("givenName"), "John"

    }

    /**
     * @since 1.0.RC4.6
     */
    private Object getValue(Class clazz, Object object, String fieldName) {
        Field field = clazz.getDeclaredField(fieldName)
        field.setAccessible(true)
        return field.get(object)
    }

    /**
     * @since 1.0.RC4.6
     */
    @Test
    void testGetDirectoriesWithDateCriteria() {

        Directory directory = client.instantiate(Directory)
        directory.name = uniquify("Java SDK: DirectoryIT.testGetDirectoriesWithDateCriteria")
        directory = client.createDirectory(directory);
        deleteOnTeardown(directory)

        Date dirCreationTimestamp = directory.createdAt

        //equals
        def dirList = client.getDirectories(Directories.where(Directories.createdAt().equals(directory.createdAt)))
        assertNotNull dirList.href

        def retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals retrieved.createdAt, directory.createdAt

        //gt
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().gt(dirCreationTimestamp)))
        assertNotNull dirList.href
        assertFalse dirList.iterator().hasNext()

        //gte
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().gte(dirCreationTimestamp)))
        assertNotNull dirList.href
        assertTrue dirList.iterator().hasNext()
        retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals retrieved.name, directory.name
        assertEquals retrieved.createdAt, directory.createdAt

        //lt
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().lt(dirCreationTimestamp)))
        assertNotNull dirList.href
        assertFalse dirList.iterator().hasNext()

        //lte
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().lte(dirCreationTimestamp)))
        assertNotNull dirList.href
        assertTrue dirList.iterator().hasNext()
        retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals retrieved.name, directory.name
        assertEquals retrieved.createdAt, directory.createdAt

        //in
        Calendar cal = Calendar.getInstance()
        cal.setTime(dirCreationTimestamp)
        cal.add(Calendar.SECOND, 2)
        Date afterCreationDate = cal.getTime()

        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().in(dirCreationTimestamp, afterCreationDate)))
        assertNotNull dirList.href
        assertTrue dirList.iterator().hasNext()
        retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals retrieved.name, directory.name
        assertEquals retrieved.createdAt, directory.createdAt

        //in
        cal.setTime(dirCreationTimestamp)
        cal.add(Calendar.SECOND, -10)
        Date newDate = cal.getTime()
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().in(newDate, new Duration(1, TimeUnit.SECONDS))))
        assertNotNull dirList.href
        assertFalse dirList.iterator().hasNext()

        //in
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().in(dirCreationTimestamp, new Duration(1, TimeUnit.MINUTES))))
        assertNotNull dirList.href
        assertTrue dirList.iterator().hasNext()
        retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals retrieved.name, directory.name
        assertEquals retrieved.createdAt, directory.createdAt
    }

    /**
     * @since 1.0.RC4.6
     */
    @Test
    void testGetAccountsWithDateCriteria() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testGetAccountsWithDateCriteria")
        dir = client.currentTenant.createDirectory(dir)
        deleteOnTeardown(dir)

        Account account = client.instantiate(Account)
        account = account.setGivenName('John')
                .setSurname(uniquify("testGetAccountsWithDateCriteria"))
                .setEmail('johntestme@testmail.stormpath.com')
                .setPassword('Changeme1!')

        dir.createAccount(account)

        Date accountCreationTimestamp = account.createdAt

        //equals
        def accList = dir.getAccounts(Accounts.where(Accounts.createdAt().equals(accountCreationTimestamp)))
        assertNotNull accList.href

        def retrieved = accList.iterator().next()
        assertEquals retrieved.href, account.href
        assertEquals retrieved.createdAt, account.createdAt

        //gt
        accList = dir.getAccounts(Accounts.where(Accounts.surname().eqIgnoreCase(account.surname))
                .and(Accounts.createdAt().gt(accountCreationTimestamp)))
        assertNotNull accList.href
        assertFalse accList.iterator().hasNext()

        //gte
        accList = dir.getAccounts(Accounts.where(Accounts.surname().eqIgnoreCase(account.surname))
                .and(Accounts.createdAt().gte(accountCreationTimestamp)))
        assertNotNull accList.href
        assertTrue accList.iterator().hasNext()
        retrieved = accList.iterator().next()
        assertEquals retrieved.href, account.href
        assertEquals retrieved.surname, account.surname
        assertEquals retrieved.createdAt, account.createdAt

        //lt
        accList = dir.getAccounts(Accounts.where(Accounts.surname().eqIgnoreCase(account.surname))
                .and(Accounts.createdAt().lt(accountCreationTimestamp)))
        assertNotNull accList.href
        assertFalse accList.iterator().hasNext()

        //lte
        accList = dir.getAccounts(Accounts.where(Accounts.surname().eqIgnoreCase(account.surname))
                .and(Accounts.createdAt().lte(accountCreationTimestamp)))
        assertNotNull accList.href
        assertTrue accList.iterator().hasNext()
        retrieved = accList.iterator().next()
        assertEquals retrieved.href, account.href
        assertEquals retrieved.surname, account.surname
        assertEquals retrieved.createdAt, account.createdAt

        //in
        Calendar cal = Calendar.getInstance()
        cal.setTime(accountCreationTimestamp)
        cal.add(Calendar.SECOND, 2)
        Date afterCreationDate = cal.getTime()

        accList = dir.getAccounts(Accounts.where(Accounts.surname().eqIgnoreCase(account.surname))
                .and(Accounts.createdAt().in(accountCreationTimestamp, afterCreationDate)))
        assertNotNull accList.href
        assertTrue accList.iterator().hasNext()
        retrieved = accList.iterator().next()
        assertEquals retrieved.href, account.href
        assertEquals retrieved.surname, account.surname
        assertEquals retrieved.createdAt, account.createdAt

        //in
        cal.setTime(accountCreationTimestamp)
        cal.add(Calendar.SECOND, -10)
        Date newDate = cal.getTime()
        accList = dir.getAccounts(Accounts.where(Accounts.surname().eqIgnoreCase(account.surname))
                .and(Accounts.createdAt().in(newDate, new Duration(1, TimeUnit.SECONDS))))
        assertNotNull accList.href
        assertFalse accList.iterator().hasNext()

        //in
        accList = dir.getAccounts(Accounts.where(Accounts.surname().eqIgnoreCase(account.surname))
                .and(Accounts.createdAt().in(accountCreationTimestamp, new Duration(1, TimeUnit.MINUTES))))
        assertNotNull accList.href
        assertTrue accList.iterator().hasNext()
        retrieved = accList.iterator().next()
        assertEquals retrieved.href, account.href
        assertEquals retrieved.surname, account.surname
        assertEquals retrieved.createdAt, account.createdAt
    }

    /**
     * @since 1.0.RC4.6
     */
    @Test
    void testSaveWithResponseOptions() {

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testSaveWithResponseOptions")
        dir = client.currentTenant.createDirectory(dir);
        deleteOnTeardown(dir)
        def href = dir.getHref()

        dir.getCustomData().put("testKey", "testValue")

        Account account01 = client.instantiate(Account)
        account01 = account01.setGivenName(uniquify('John'))
                .setSurname('Doe')
                .setEmail(uniquify("johndoe") + "@testmail.stormpath.com")
                .setPassword('Changeme1!')

        dir.createAccount(account01)
        deleteOnTeardown(account01)

        Account account02 = client.instantiate(Account)
        account02 = account02.setGivenName(uniquify('John'))
                .setSurname('Doe 2')
                .setEmail(uniquify("johndoe2") + "@testmail.stormpath.com")
                .setPassword('Changeme1!')

        dir.createAccount(account02)
        deleteOnTeardown(account02)

        def retrieved = dir.saveWithResponseOptions(Directories.options().withAccounts().withCustomData())

        assertEquals retrieved.getHref(), href
        assertEquals retrieved.getCustomData().get("testKey"), "testValue"
        assertTrue retrieved.getAccounts().iterator().hasNext()
        assertTrue retrieved.getAccounts().iterator().hasNext()
    }

    /**
     * @since 1.0.RC7.7
     */
    @Test
    void testGetOrganizations() {

        Directory directory = client.instantiate(Directory)
        directory.setName(uniquify("JSDK.DirectoryIT.testGetOrganizations"))
        directory = client.createDirectory(directory);
        assertNotNull directory.href
        deleteOnTeardown(directory)

        def org = client.instantiate(Organization)
        org.setName(uniquify("JSDK.DirectoryIT.testGetOrganizations_First"))
                .setDescription("Organization Description")
                .setNameKey(uniquify("test").substring(2, 8))
                .setStatus(OrganizationStatus.ENABLED)
        org = client.createOrganization(org)
        assertNotNull org.href
        deleteOnTeardown(org)

        org.addAccountStore(directory)

        def orgList = directory.getOrganizations()
        assertTrue orgList.iterator().hasNext()
        assertEquals orgList.iterator().next().href, org.href

        def org2 = client.instantiate(Organization)
        org2.setName(uniquify("JSDK.DirectoryIT.testGetOrganizations.Second"))
                .setDescription("Organization Description")
                .setNameKey(uniquify("test").substring(2, 8))
                .setStatus(OrganizationStatus.ENABLED)
        org2 = client.createOrganization(org2)
        assertNotNull org2.href
        deleteOnTeardown(org2)

        org2.addAccountStore(directory)

        orgList = directory.getOrganizations()
        assertEquals orgList.size, 2

        orgList = directory.getOrganizations(Organizations.where(Organizations.name().eqIgnoreCase(org2.name)))
        assertEquals orgList.size, 1
        assertEquals orgList.iterator().next().href, org2.href
    }

    /**
     * @since 1.2.0
     */
    @Test
    void testGetAccountSchema() {

        Directory directory = client.instantiate(Directory)
        directory.setName(uniquify("JSDK.DirectoryIT.testGetAccountSchema"))
        directory = client.createDirectory(directory);
        assertNotNull directory.href
        deleteOnTeardown(directory)

        def accountSchema = directory.getAccountSchema()
        assertNotNull accountSchema
        assertNotNull accountSchema.getHref()

        def accountSchema1 = client.getResource(accountSchema.getHref(), Schema)

        assertFalse(accountSchema.equals(accountSchema1))  //not expanded -> must be different
    }

    /**
     * @since 1.2.0
     */
    @Test
    void testGetDirectoryWithExpandedAccountSchema() {

        Directory directory = client.instantiate(Directory)
        directory.setName(uniquify("JSDK.DirectoryIT.testGetDirectoryWithExpandedAccountSchema"))
        directory = client.createDirectory(directory);
        assertNotNull directory.href
        deleteOnTeardown(directory)

        def dirOptions = Directories.options().withAccountSchema()

        directory = client.getResource(directory.getHref(), Directory, dirOptions)

        //accountSchema must be expanded
        def accountSchema = directory.getAccountSchema()
        assertNotNull accountSchema
        assertNotNull accountSchema.getHref()
        //Let's check the schema is really materialized (ie. was properly expanded)
        assertTrue accountSchema.toString().contains("modifiedAt")
        assertTrue accountSchema.toString().contains("createdAt")
        assertTrue accountSchema.toString().contains("/fields")

        def accountSchema1 = client.getResource(accountSchema.getHref(), Schema)

        assertEquals(accountSchema, accountSchema1)  //expanded -> must be equals
    }

    /**
     * @since 1.2.0
     */
    @Test
    public void testCreateAccountWithDefaultAccountSchema() {

        def app = createTempApp()
        def username = uniquify('Stormpath-SDK-Test-App-Acct1')
        def account = client.instantiate(Account)
                .setPassword("Changeme1!")
        try {
            app.createAccount(Accounts.newCreateRequestFor(account).setRegistrationWorkflowEnabled(false).build())
            fail("Account requires email according to the account schema.")
        } catch (com.stormpath.sdk.resource.ResourceException e) {
            assertEquals(e.stormpathError.code, 2000)
        }

        account = client.instantiate(Account)
                .setEmail(username + "@testmail.stormpath.com")
        try {
            app.createAccount(Accounts.newCreateRequestFor(account).setRegistrationWorkflowEnabled(false).build())
            fail("Account requires username according to the account schema.")
        } catch (com.stormpath.sdk.resource.ResourceException e) {
            assertEquals(e.stormpathError.code, 2000)
        }

        //By default the email and password are the solely required fields
        account = client.instantiate(Account)
                .setPassword("Changeme1!")
                .setEmail(username + "@testmail.stormpath.com")
        account = app.createAccount(Accounts.newCreateRequestFor(account).setRegistrationWorkflowEnabled(false).build())
        deleteOnTeardown(account)

        assertNull(account.getGivenName())
        assertNull(account.getSurname())
    }

    /**
     * @since 1.2.0
     */
    @Test
    void testCreateAccountWithGivenNameSchemaFieldRequired() {
        def app = createTempApp()

        Directory directory = app.getDefaultAccountStore() as Directory
        directory.accountSchema.fields.each { field ->
            if ("givenName" == field.name) {
                field.required = true
                field.save()
            }
        }

        def username = uniquify('Stormpath-SDK-Test-App-Acct1')
        def account = client.instantiate(Account)
                .setUsername(username)
                .setPassword("Changeme1!")
                .setEmail(username + "@testmail.stormpath.com")
        try {
            app.createAccount(Accounts.newCreateRequestFor(account).setRegistrationWorkflowEnabled(false).build())
            fail("Account requires givenName according to the account schema.")
        } catch (com.stormpath.sdk.resource.ResourceException e) {
            assertEquals(e.stormpathError.code, 2000)
        }
    }

    /**
     * @since 1.2.0
     */
    @Test
    void testRequiredFieldTrueAndFalseResultsInDifferentResults() {
        def app = createTempApp()

        Directory directory = app.getDefaultAccountStore() as Directory
        directory.accountSchema.fields.each { field ->
            if ("surname" == field.name) {
                field.required = true
                field.save()
            }
        }

        def username = uniquify('Stormpath-SDK-Test-App-Acct1')
        def account = client.instantiate(Account)
                .setUsername(username)
                .setPassword("Changeme1!")
                .setEmail(username + "@testmail.stormpath.com")
        try {
            app.createAccount(Accounts.newCreateRequestFor(account).setRegistrationWorkflowEnabled(false).build())
            fail("Account requires surname according to the account schema.")
        } catch (com.stormpath.sdk.resource.ResourceException e) {
            assertEquals(e.stormpathError.code, 2000)
        }

        //Let's now set surname to false and try if we can add the account without the surname one more tiem
        directory.accountSchema.fields.each { field ->
            if ("surname" == field.name) {
                field.required = false
                field.save()
            }
        }

        account = app.createAccount(Accounts.newCreateRequestFor(account).setRegistrationWorkflowEnabled(false).build())

        deleteOnTeardown(account)

        assertNull(account.getGivenName())
        assertNull(account.getSurname())
    }

}
