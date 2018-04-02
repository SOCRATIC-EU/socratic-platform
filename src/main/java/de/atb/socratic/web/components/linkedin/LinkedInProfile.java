package de.atb.socratic.web.components.linkedin;

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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import de.atb.socratic.model.RegistrationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.service.security.AuthenticationService;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;

public class LinkedInProfile {

    private String id;
    private String firstName;
    private String lastName;
    private String nickName;
    private String eMail;
    private String pictureUrl;
    private String companyName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public static LinkedInProfile fromJSONString(final String jsonString)
            throws JSONException {
        return fromJSONObject(new JSONObject(jsonString));
    }

    public static LinkedInProfile fromJSONObject(final JSONObject object)
            throws JSONException {
        LinkedInProfile profile = new LinkedInProfile();
        profile.setId(object.getString("id"));
        profile.setFirstName(object.getString("firstName"));
        profile.setLastName(object.getString("lastName"));
        // use linkedIn first and last name as nickname
        profile.setNickName(profile.getFirstName() + " " + profile.getLastName());
        profile.setEMail(object.getString("emailAddress"));
        profile.setPictureUrl(object.getString("pictureUrl"));
        profile.setCompanyName(object.getString("company"));
        return profile;
    }

    public User toEFFUser() {
        User user = new User();
        byte[] salt = new byte[0];
        try {
            salt = AuthenticationService.generateSalt();
        } catch (NoSuchAlgorithmException e) {
        }
        byte[] encryptedPassword = new byte[0];
        try {
            encryptedPassword = AuthenticationService.getEncryptedPassword("IAmComingFromLinkedInIDontNeedAPassword", salt);
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeySpecException e) {
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // replace black space by "-"
        nickName = nickName.replace(" ", "-");
        user.setNickName(nickName);
        user.setEmail(eMail);
        user.setPassword(encryptedPassword);
        user.setPwSalt(salt);
        user.setRegistrationStatus(RegistrationStatus.CONFIRMED);
        user.setRegistrationDate(new Date());
        user.setLinkedInId(id);

        return user;
    }

    public String toString() {
        return "LinkedInProfile [id=" + getId() + ", firstName=" + getFirstName() + ", lastName=" + getLastName() + ", emailAddress=" + getEMail() + ", pictureUrl=" + getPictureUrl() + ", companyName=" + getCompanyName() + "]";
    }
}
