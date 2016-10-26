/*
 * Copyright 2016 Stormpath, Inc.
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
$('.btn-google').click(function (event) {
    event.preventDefault();
    googleLogin($('.btn-google').attr('id'));
});

$('.btn-facebook').click(function (event) {
    event.preventDefault();
    facebookLogin($('.btn-facebook').attr('id'));
});

$('.btn-github').click(function (event) {
    event.preventDefault();
    githubLogin($('.btn-github').attr('id'));
});

$('.btn-linkedin').click(function (event) {
    event.preventDefault();
    linkedinLogin($('.btn-linkedin').attr('id'));
});

$('.btn-saml').click(function (event) {
    event.preventDefault();
    samlLogin(event.target.id);
});

function baseUrl() {
    return $('#baseUrl').val();
}

function linkedinLogin(clientId) {
    window.location.replace(
        buildUrl('https://www.linkedin.com/uas/oauth2/authorization',
            {
                client_id: clientId,
                response_type: 'code',
                scope: 'r_emailaddress r_basicprofile',
                redirect_uri: baseUrl() + '/callbacks/linkedin',
                state: 'oauthState' //linkedin requires state to be pass all the time.
            }
        )
    );
}

function googleLogin(clientId) {
    window.location.replace(
        buildUrl(
            'https://accounts.google.com/o/oauth2/auth',
            {
                response_type: 'code',
                client_id: clientId,
                scope: 'email',
                redirect_uri: baseUrl() + '/callbacks/google'
            }
        )
    );
}

function githubLogin(clientId) {
    window.location.replace(buildUrl('https://github.com/login/oauth/authorize', {'client_id': clientId}));
}

function samlLogin(href) {
    window.location.replace(buildUrl(baseUrl() + '/saml', {'href': href}));
}

function facebookLogin(appId) {
    var FB = window.FB;
    FB.init({
        appId: appId,
        cookie: true,
        xfbml: true,
        version: 'v2.4'
    });
    FB.login(function (response) {
        if (response.status === 'connected') {
            var queryStr = window.location.search.replace('?', '');
            if (queryStr) {
                window.location.replace(buildUrl(baseUrl() + '/callbacks/facebook?queryStr', {accessToken: FB.getAuthResponse()['accessToken']}));
            } else {
                window.location.replace(buildUrl(baseUrl() + '/callbacks/facebook', {accessToken: FB.getAuthResponse()['accessToken']}));
            }
        }
    }, {scope: 'email'});
}

(function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) {
        return;
    }
    js = d.createElement(s);
    js.id = id;
    js.src = '//connect.facebook.net/en_US/sdk.js';
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));

function getParameterByName(name) {
    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}

function buildUrl(url, params) {
    var next = getParameterByName('next');

    if (next !== undefined && next !== '') {
        params.state = next;
    }

    if (url.includes('?')) {
        return url + '&' + $.param(params);
    }
    return url + '?' + $.param(params);
}