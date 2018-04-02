/*-
 * #%L
 * socratic-platform
 * %%
 * Copyright (C) 2016 - 2018 Institute for Applied Systems Technology Bremen GmbH (ATB)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
/**
 * Facebook javascript API
 */
function initProfile() {
    var loginProfile = {};
    loginProfile.first_name = '';
    loginProfile.last_name = '';
    loginProfile.id = '';
    loginProfile.emailAddress = '';
    loginProfile.pictureUrl = '';
    loginProfile.company = '';
    return loginProfile;
};

var loginProfile = initProfile();

// This function is called when someone finishes with the Login
// Button.
function checkLoginState() {
    FB.getLoginStatus(function (response) {
        if (response.status === 'connected') {
            // Logged into your app and Facebook.
            testAPI();
        } else if (response.status === 'not_authorized') {
            // The person is logged into Facebook, but not your app.
            document.getElementById('status').innerHTML = 'Please log '
                + 'into this app.';
        } else {
            // The person is not logged into Facebook, so we're not sure if
            // they are logged into this app or not.
            document.getElementById('status').innerHTML = 'Please log '
                + 'into Facebook.';
        }
    });
};

function fbpublish(linkUrl) {
    FB.ui({
        method: "feed",
        link: linkUrl,
        caption: "SOCRATIC"
    });
};

function fbSendMessage(linkUrl) {
    FB.ui({
        method: 'send',
        link: linkUrl,
        caption: "SOCRATIC"
    });
};

// do stuff here
window.fbAsyncInit = function () {
    FB.init({
        appId: '${fb-app-id}', // app id
        cookie: false, // enable cookies to allow the server to access
        // the session
        scrape: true,
        xfbml: true, // parse social plugins on this page
        version: 'v2.10' // use graph api version 2.10
    });
};

// Load the SDK asynchronously
(function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id))
        return;
    js = d.createElement(s);
    js.id = id;
    js.src = "//connect.facebook.net/en_US/sdk.js";
    fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));

// Here we run a very simple test of the Graph API after login is
// successful. See statusChangeCallback() for when this call is made.
function testAPI() {
    console.log('Welcome!  Fetching your information.... ');
    FB.api('/me', {
        fields: 'id,name,email,first_name,last_name,work,picture.type(large)'
    }, function (response) {
        if (response && !response.error) {
            loginProfile.first_name = response.first_name;
            loginProfile.last_name = response.last_name;
            loginProfile.name = response.name;
            loginProfile.id = response.id;
            loginProfile.emailAddress = response.email;
            loginProfile.pictureUrl = response.picture.data.url;
            // loginProfile.company = response.work;

        }
        console.log('Successful login for: ' + response.name);
        document.getElementById('status').innerHTML = 'Thanks for logging in, '
            + response.name + '!';

        jQuery('body').trigger("facebooklogincomplete");
    });
};
