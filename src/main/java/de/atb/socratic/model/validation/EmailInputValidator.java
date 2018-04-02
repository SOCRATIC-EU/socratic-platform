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
import java.util.regex.Pattern;

import javax.inject.Inject;

import de.atb.socratic.model.User;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class EmailInputValidator extends AbstractFormValidator {

    private static final long serialVersionUID = -5080899725946600240L;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Inject
    @LoggedInUser
    User loggedInUser;

    final FormComponent<String> emailComponent;

    private final Pattern pattern;

    public EmailInputValidator(final FormComponent<String> emailComponent) {
        if (emailComponent == null) {
            throw new IllegalArgumentException("argument emailComponent cannot be null");
        }

        CdiContainer.get().getNonContextualManager().inject(this);

        this.pattern = Pattern.compile(EMAIL_PATTERN);
        this.emailComponent = emailComponent;
    }

    public void validate(Form<?> form) {
        String emailAddress = emailComponent.getInput();
        if (loggedInUser != null && loggedInUser.getEmail().equals(emailAddress)) {
            error(emailComponent, "email.input.validation.own.error",
                    Collections.<String, Object>singletonMap("email", emailAddress));
        } else if (!isValid(emailAddress)) {
            error(emailComponent, "email.input.validation.error",
                    Collections.<String, Object>singletonMap("email", emailAddress));
        }
    }

    @Override
    public FormComponent<?>[] getDependentFormComponents() {
        return new FormComponent<?>[]{emailComponent};
    }

    protected boolean isValid(final String email) {
        return pattern.matcher(email).matches();
    }

}
