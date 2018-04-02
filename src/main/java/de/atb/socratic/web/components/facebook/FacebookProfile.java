package de.atb.socratic.web.components.facebook;

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

public class FacebookProfile {

    private String id;
    private String first_name;
    private String last_name;
    private String name;
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
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public static FacebookProfile fromJSONString(final String jsonString)
            throws JSONException {
        return fromJSONObject(new JSONObject(jsonString));
    }

    public static FacebookProfile fromJSONObject(final JSONObject object) throws JSONException {
        FacebookProfile profile = new FacebookProfile();
        profile.setId(object.getString("id"));
        profile.setFirstName(object.getString("first_name"));
        profile.setLastName(object.getString("last_name"));

        // use fb full name as nick name, replace black space by "-"
        String nickName = object.getString("name");
        nickName = nickName.replace(" ", "-");
        profile.setName(nickName);
        profile.setEMail(object.getString("emailAddress"));
        profile.setPictureUrl(object.getString("pictureUrl"));
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
            encryptedPassword = AuthenticationService.getEncryptedPassword("IAmComingFromFacebookIDontNeedAPassword", salt);
        } catch (NoSuchAlgorithmException e) {
        } catch (InvalidKeySpecException e) {
        }
        user.setFirstName(first_name);
        user.setLastName(last_name);
        user.setNickName(name);
        user.setEmail(eMail);
        user.setPassword(encryptedPassword);
        user.setPwSalt(salt);
        user.setRegistrationStatus(RegistrationStatus.CONFIRMED);
        user.setRegistrationDate(new Date());
        user.setFacebookId(id);

        return user;
    }

    public String toString() {
        return "FacebookProfile [id=" + getId() + ", firstName=" + getFirstName() + ", lastName=" + getLastName() + ", emailAddress=" + getEMail() + ", pictureUrl=" + getPictureUrl() + ", companyName=" + getCompanyName() + "]";
    }
}
