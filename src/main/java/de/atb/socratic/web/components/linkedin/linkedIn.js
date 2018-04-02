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
 * LinkedIn javascript API
 */

document
    .write("<script type='text/javascript' src='https://platform.linkedin.com/in.js'>");
document.writeln("\n");
document.write("api_key: 780fml2gu2upeg");
document.writeln("\n");
document.write("scope: r_basicprofile r_emailaddress w_share");
document.writeln("\n");
document.write("authorize: false");
document.write("</script>");

function initProfile() {
    var loginProfile = {};
    loginProfile.firstName = '';
    loginProfile.lastName = '';
    loginProfile.id = '';
    loginProfile.emailAddress = '';
    loginProfile.pictureUrl = '';
    loginProfile.company = '';
    return loginProfile;
};

var loginProfile = initProfile();

function authorizeUser() {
    if (IN.User.isAuthorized() === false) {
        IN.User.authorize(onLinkedInAuthorized);
    } else {
        onLinkedInAuthorized();
    }
};

function onLinkedInAuthorized() {
    IN.API.Profile("me").fields(
        ["id", "firstName", "lastName", "threeCurrentPositions",
            "emailAddress", "pictureUrls::(original)",
            "positions:(is-current,company:(name))"]).result(
        onProfileCallback).error(onErrorCallback);
};

function onProfileCallback(profiles) {
    var member = profiles.values[0];
    loginProfile.firstName = member.firstName;
    loginProfile.lastName = member.lastName;
    loginProfile.id = member.id;
    loginProfile.emailAddress = member.emailAddress;
    if (member.pictureUrls !== undefined && member.pictureUrls._total > 0) {
        loginProfile.pictureUrl = member.pictureUrls.values[0];
    }
    if (member.positions !== undefined && member.positions._total > 0) {
        var position = member.positions.values[0];
        loginProfile.company = position.company.name;
    }
    console.log(loginProfile);
    jQuery('body').trigger("linkedinlogincomplete");
};

function onErrorCallback(error) {
    // what to do with errors?
    console.log(error);
};

// Handle the successful return from the API call
function onSuccess(data) {
    console.log(data);
}

// Handle an error response from the API call
function onError(error) {
    console.log(error);
}
