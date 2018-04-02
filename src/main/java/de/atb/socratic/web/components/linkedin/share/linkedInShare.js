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
document.write("authorize: true");
document.write("</script>");

function authorizeUser(title, description, submittedUrl, submittedImageUrl,
                       comment) {
    if (IN.User.isAuthorized() === false) {
        IN.User.authorize(onLinkedInAuthorized);
    } else {
        onLinkedInAuthorized();
    }
};

function initParameters(title, description, submittedUrl, submittedImageUrl,
                        comment) {
    var loginParameters = {};
    loginParameters.title = title;
    loginParameters.description = description;
    loginParameters.submittedUrl = submittedUrl;
    loginParameters.submittedImageUrl = submittedImageUrl;
    loginParameters.comment = comment;
    return loginParameters;
}

var loginParameters;

function onLinkedInAuthorized() {
    IN.API.Profile("me").fields(
        ["id", "firstName", "lastName", "threeCurrentPositions",
            "emailAddress", "pictureUrls::(original)",
            "positions:(is-current,company:(name))"]).result(
        shareContent).error(onErrorCallback);
};

/** ************************ sharing content ************************* */
// Setup an event listener to make an API call once auth is complete
function onLinkedInLoad(title, description, submittedUrl, submittedImageUrl,
                        comment) {

    // initialize parameters at first
    loginParameters = initParameters(title, description, submittedUrl,
        submittedImageUrl, comment);

    authorizeUser(title, description, submittedUrl, submittedImageUrl, comment);
    IN.Event.on(IN, "auth", shareContent);
}

// Handle the successful return from the API call
function onSuccess(data) {
    console.log(data);
}

// Handle an error response from the API call
function onError(error) {
    console.log(error);
}

function onErrorCallback(error) {
    // what to do with errors?
    console.log(error);
};

var response;

// Use the API call wrapper to share content on LinkedIn
function shareContent() {
    // Build the JSON payload containing the content to be shared
    bodyRequest = JSON.stringify({
        "comment": loginParameters.comment,
        "content": {
            "title": loginParameters.title,
            "description": loginParameters.description,
            "submittedUrl": loginParameters.submittedUrl,
            "submittedImageUrl": loginParameters.submittedImageUrl,
        },

        "visibility": {
            "code": "anyone"
        }

    });

    IN.API.Raw("people/~/shares").method("POST").body(bodyRequest).result(
        function (result) {
            console.log("Success");
            response = 'Your post is successfully posted on linked in!';
            jQuery('body').trigger("response");
        }).error(
        function (result) {
            console.log(JSON.stringify(result));
            response = 'An error occured while posting on linked in:: '
                + result.message;
            jQuery('body').trigger("response");
        });
}
