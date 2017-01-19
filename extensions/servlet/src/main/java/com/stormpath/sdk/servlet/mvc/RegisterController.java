/*
 * Copyright 2015 Stormpath, Inc.
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
package com.stormpath.sdk.servlet.mvc;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountStatus;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.cache.Cache;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.directory.AccountStore;
import com.stormpath.sdk.directory.AccountStoreVisitorAdapter;
import com.stormpath.sdk.directory.Directory;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.organization.Organization;
import com.stormpath.sdk.servlet.account.event.impl.DefaultRegisteredAccountRequestEvent;
import com.stormpath.sdk.servlet.application.ApplicationResolver;
import com.stormpath.sdk.servlet.authc.impl.TransientAuthenticationResult;
import com.stormpath.sdk.servlet.form.Field;
import com.stormpath.sdk.servlet.form.Form;
import com.stormpath.sdk.servlet.http.Saver;
import com.stormpath.sdk.servlet.http.authc.AccountStoreResolver;
import com.stormpath.sdk.servlet.mvc.provider.AccountStoreModelFactory;
import com.stormpath.sdk.servlet.mvc.provider.ExternalAccountStoreModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 1.0.RC4
 */
public class RegisterController extends FormController {

    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);
    public static final List<String> ACCOUNT_PROPERTIES = Collections.unmodifiableList(Arrays.asList("email", "username", "password", "confirmPassword", "givenName", "middleName", "surname"));

    private boolean autoLogin;
    private String loginUri;
    private String verifyViewName;

    private Client client;
    //only used if account does not need email verification:
    private Saver<AuthenticationResult> authenticationResultSaver;
    private AccountModelFactory accountModelFactory;
    private AccountStoreModelFactory accountStoreModelFactory;
    private ErrorModelFactory errorModelFactory;
    private WebHandler preRegisterHandler;
    private WebHandler postRegisterHandler;
    private AccountStoreResolver accountStoreResolver;

    public void setAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    public void setLoginUri(String loginUri) {
        this.loginUri = loginUri;
    }

    public void setVerifyViewName(String verifyViewName) {
        this.verifyViewName = verifyViewName;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setAuthenticationResultSaver(Saver<AuthenticationResult> authenticationResultSaver) {
        this.authenticationResultSaver = authenticationResultSaver;
    }

    public void setAccountModelFactory(AccountModelFactory accountModelFactory) {
        this.accountModelFactory = accountModelFactory;
    }

    public void setAccountStoreModelFactory(AccountStoreModelFactory accountStoreModelFactory) {
        this.accountStoreModelFactory = accountStoreModelFactory;
    }

    public void setErrorModelFactory(ErrorModelFactory errorModelFactory) {
        this.errorModelFactory = errorModelFactory;
    }

    public void setPreRegisterHandler(WebHandler preRegisterHandler) {
        this.preRegisterHandler = preRegisterHandler;
    }

    public void setPostRegisterHandler(WebHandler postRegisterHandler) {
        this.postRegisterHandler = postRegisterHandler;
    }

    public AccountStoreResolver getAccountStoreResolver() {
        return accountStoreResolver;
    }

    public void setAccountStoreResolver(AccountStoreResolver accountStoreResolver) {
        this.accountStoreResolver = accountStoreResolver;
    }

    @Override
    public void init() throws Exception {
        super.init();

        if (this.accountModelFactory == null) {
            this.accountModelFactory = new DefaultAccountModelFactory();
        }
        if (this.accountStoreModelFactory == null) {
            this.accountStoreModelFactory = new ExternalAccountStoreModelFactory();
        }
        if (this.errorModelFactory == null) {
            this.errorModelFactory = new RegisterErrorModelFactory(this.messageSource);
        }

        Assert.notNull(this.client, "client cannot be null.");
        Assert.notNull(this.authenticationResultSaver, "authenticationResultSaver cannot be null.");
        Assert.hasText(this.loginUri, "loginUri cannot be null or empty.");
        Assert.hasText(this.verifyViewName, "verifyViewName cannot be null or empty.");
        Assert.notNull(this.preRegisterHandler, "preRegisterHandler cannot be null.");
        Assert.notNull(this.postRegisterHandler, "postRegisterHandler cannot be null.");
        Assert.notNull(this.accountModelFactory, "accountModelFactory cannot be null.");
        Assert.notNull(this.accountStoreModelFactory, "accountStoreModelFactory cannot be null.");
        Assert.notNull(this.errorModelFactory, "errorModelFactory cannot be null.");
        Assert.notNull(this.accountStoreResolver, "accountStoreResolver cannot be null.");
    }

    @Override
    public boolean isNotAllowedIfAuthenticated() {
        return true;
    }

    @Override
    protected void appendModel(HttpServletRequest request, HttpServletResponse response, Form form, List<ErrorModel> errors,
                               Map<String, Object> model) {
        if (!isJsonPreferred(request, response)) {
            model.put("loginUri", loginUri);
        } else {
            model.put("accountStores", accountStoreModelFactory.getAccountStores(request));
        }
    }

    @Override
    protected List<ErrorModel> toErrors(HttpServletRequest request, Form form, Exception e) {
        log.debug("Unable to register account.", e);

        return Collections.singletonList(errorModelFactory.toError(request, e));
    }

    protected void validate(HttpServletRequest request, HttpServletResponse response, Form form) {
        super.validate(request, response, form);

        Field confirmPasswordField = form.getField("confirmPassword");

        if (confirmPasswordField != null && confirmPasswordField.isEnabled()) {
            //ensure passwords match:
            String password = form.getFieldValue("password");
            String confirmPassword = form.getFieldValue("confirmPassword");

            if (!password.equals(confirmPassword) && confirmPasswordField.isRequired()) {
                String key = "stormpath.web.register.form.errors.passwordMismatch";
                String msg = i18n(request, key);
                throw new MismatchedPasswordException(msg);
            }
        }
    }

    @Override
    protected ViewModel onValidSubmit(HttpServletRequest req, HttpServletResponse resp, Form form) throws Exception {

        //Create a new Account instance that will represent the submitted user information:
        Account account = client.instantiate(Account.class);

        String value = form.getFieldValue("email");
        if (value != null) {
            account.setEmail(value);
        }

        value = form.getFieldValue("username");
        if (value != null) {
            account.setUsername(value);
        }

        value = form.getFieldValue("password");
        if (value != null) {
            account.setPassword(value);
        }

        value = form.getFieldValue("givenName");
        account.setGivenName(value != null ? value : "UNKNOWN");

        value = form.getFieldValue("middleName");
        if (value != null) {
            account.setMiddleName(value);
        }

        value = form.getFieldValue("surname");
        account.setSurname(value != null ? value : "UNKNOWN");

        account.getCustomData().putAll(getCustomData(req, form));

        //Get the Stormpath Application instance corresponding to this web app:
        Application app = ApplicationResolver.INSTANCE.getApplication(req);

        if (preRegisterHandler != null) {
            if (!preRegisterHandler.handle(req, resp, account)) {
                return null;
            }
        }

        AccountStore accountStore = accountStoreResolver.getAccountStore(req, resp);

        if (accountStore == null) {
            //now persist the new account, and ensure our account reference points to the newly created/returned instance:
            account = app.createAccount(account);
        } else {
            final Account[] accountHolder = new Account[]{account};

            accountStore.accept(new AccountStoreVisitorAdapter() {
                @Override
                public void visit(Directory directory) {
                    Account createdAccount = directory.createAccount(accountHolder[0]);
                    accountHolder[0] = createdAccount;
                }

                @Override
                public void visit(Organization organization) {
                    Account createdAccount = organization.createAccount(accountHolder[0]);
                    accountHolder[0] = createdAccount;
                }
            });

            account = accountHolder[0];
        }

        publishRequestEvent(new DefaultRegisteredAccountRequestEvent(req, resp, account));

        if (postRegisterHandler != null) {
            if (!postRegisterHandler.handle(req, resp, account)) {
                return null;
            }
        }

        AccountStatus status = account.getStatus();
        if (status == AccountStatus.UNVERIFIED) {
            // purge account from cache in case status is updated on the backend
            invalidateAccountCache(account);
        }

        if (isJsonPreferred(req, resp)) {
            //noinspection unchecked
            return new DefaultViewModel(STORMPATH_JSON_VIEW_NAME, java.util.Collections.singletonMap("account", accountModelFactory.toMap(account, Collections.EMPTY_LIST)));
        }

        if (status == AccountStatus.ENABLED) {
            if (autoLogin) {
                //the user does not need to verify their email address, so just assume they are authenticated
                //(since they specified their password during registration):
                final AuthenticationResult result = new TransientAuthenticationResult(account);
                this.authenticationResultSaver.set(req, resp, result);
            } else {
                return new DefaultViewModel(loginUri + "?status=created").setRedirect(true);
            }
        } else if (status == AccountStatus.UNVERIFIED) {
            return new DefaultViewModel(loginUri + "?status=unverified").setRedirect(true);
        }
        return new DefaultViewModel(nextUri).setRedirect(true);
    }

    // resolves https://github.com/stormpath/stormpath-sdk-java/issues/1198
    @SuppressWarnings("unchecked")
    private void invalidateAccountCache(Account account) {
        Cache accountCache = client.getCacheManager().getCache(Account.class.getName());
        accountCache.remove(account.getHref());
    }

    private Map<String, Object> getCustomData(HttpServletRequest request, Form form) {
        //Custom fields are either declared as form fields which shouldn't not be account fields or through a customField attribute
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        for (Field field : form.getFields()) {
            //Field is not part of the default account properties then is a custom field
            if (!field.getName().equals(getCsrfTokenManager().getTokenName()) && !ACCOUNT_PROPERTIES.contains(field.getName())) {
                result.put(field.getName(), field.getValue());
            }
        }

        Object customData = getFieldValueResolver().getAllFields(request).get("customData");
        if (customData instanceof Map) {
            //noinspection unchecked
            result.putAll((Map<? extends String, ?>) customData);
        } //If not a map ignore, the spec doesn't cover this case

        return result;
    }
}
