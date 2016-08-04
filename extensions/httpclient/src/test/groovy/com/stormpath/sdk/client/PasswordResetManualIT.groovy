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
import com.stormpath.sdk.application.Application
import com.stormpath.sdk.application.ApplicationList
import com.stormpath.sdk.authc.UsernamePasswordRequests
import com.stormpath.sdk.tenant.Tenant
import org.testng.annotations.Test

import static org.testng.Assert.assertEquals
import static org.testng.Assert.fail

/**
 * Password Reset Integration Tests for Stormpath developers.
 *
 * To function, this test must be enabled (enabled=true) and the {@code email} and {@code spToken] values must be set
 * to values reflected in the developer's local development datastore.
 *
 * @since 0.4
 */
@Test
class PasswordResetManualIT extends ClientIT {

    def email = 'changeme@somewhere.com' //change for your own testing needs.
    def spToken = 'changeMeToSomeResetKeyValue' //update based on your locally stored value

    @Test //sends an email based on 'email' property.  After sent, you need to look for the reset token in the datastore
    void testSendPasswordResetEmail() {

        Tenant tenant = client.getCurrentTenant();

        Application appToTest = null;

        ApplicationList applications = tenant.getApplications()
        for (Application app : applications) {
            if (app.name != "Stormpath IAM") {
                appToTest = app;
                break;
            }
        }

        Account account = appToTest.sendPasswordResetEmail(email).getAccount()

        assertEquals email, account.email
    }

    @Test //spToken must be set to the password_reset_key value in the datastore for the specified account
    void testPasswordResetAfterEmail() {

        Tenant tenant = client.getCurrentTenant();

        Application appToTest = null;

        ApplicationList applications = tenant.getApplications()
        for (Application app : applications) {
            if (app.name != "Stormpath IAM") {
                appToTest = app;
                break;
            }
        }

        def acct = appToTest.verifyPasswordResetToken(spToken)

        assertEquals(email, acct.email, "Could not retrieve correct account during password reset.")

        def newPassword = 'Aabc1234'

        acct.password = newPassword
        acct.save()

        def request = UsernamePasswordRequests.builder().setUsernameOrEmail(email).setPassword(newPassword).build()
        Account authenticated = appToTest.authenticateAccount(request).account

        assertEquals acct.href, authenticated.href
    }

    /**
     * @since 1.0.RC
     */
    @Test //spToken must be set to the password_reset_key value in the datastore for the specified account
    void testResetPasswordAfterEmail() {

        Tenant tenant = client.getCurrentTenant();

        Application appToTest = null;

        ApplicationList applications = tenant.getApplications()
        for (Application app : applications) {
            if (app.name != "Stormpath") {
                appToTest = app;
                break;
            }
        }

        def newPassword = 'Aabc1234'
        def acct = appToTest.resetPassword(spToken, newPassword)

        assertEquals(email, acct.email, "Could not retrieve correct account during password reset.")

        def request = UsernamePasswordRequests.builder().setUsernameOrEmail(email).setPassword(newPassword).build()
        Account authenticated = appToTest.authenticateAccount(request).account

        assertEquals acct.href, authenticated.href

        //Let's validate the reset token url has disappeared after being successfully used
        try {
            appToTest.resetPassword(spToken, newPassword)
            fail("should have thrown")
        } catch (com.stormpath.sdk.resource.ResourceException e) {
            //exception expected
        }

    }
}
