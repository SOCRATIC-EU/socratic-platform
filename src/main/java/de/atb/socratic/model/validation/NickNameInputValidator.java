package de.atb.socratic.model.validation;

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

import java.util.Collections;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.model.User;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class NickNameInputValidator extends AbstractFormValidator {

    private static final long serialVersionUID = -5080899725946600240L;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    UserService userService;

    final FormComponent<String> nickNameComponent;

    public NickNameInputValidator(final FormComponent<String> nickNameComponent) {
        if (nickNameComponent == null) {
            throw new IllegalArgumentException("argument nickNameComponent cannot be null");
        }

        CdiContainer.get().getNonContextualManager().inject(this);
        this.nickNameComponent = nickNameComponent;
    }

    public void validate(Form<?> form) {
        String nickName = nickNameComponent.getInput();
        // if given nickName contains blank spance in between eg "Abc xyz"
        if (nickName.contains(" ")) {
            error(nickNameComponent, "nickName.input.validation.blankSpace.error",
                    Collections.<String, Object>singletonMap("nickName", nickName));
            return;
        }

        if (isValid(nickName)) {
            error(nickNameComponent, "nickName.input.validation.error",
                    Collections.<String, Object>singletonMap("nickName", nickName));
        }
    }

    @Override
    public FormComponent<?>[] getDependentFormComponents() {
        return new FormComponent<?>[]{nickNameComponent};
    }

    protected boolean isValid(final String email) {
        return userService.isOtherUsersWithNickNameExists(loggedInUser, email);
    }

}
