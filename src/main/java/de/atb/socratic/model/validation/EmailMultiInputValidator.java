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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;

public class EmailMultiInputValidator extends EmailInputValidator {
    private static final long serialVersionUID = 3610885216640184204L;

    public EmailMultiInputValidator(final FormComponent<String> emailComponent) {
        super(emailComponent);
    }

    public void validate(Form<?> form) {
        if (StringUtils.isBlank(emailComponent.getInput())) {
            return;
        }
        String[] emailAddressesArray = emailComponent.getInput().split("\\s+");
        List<String> invalid = new ArrayList<>();
        for (String emailAddress : emailAddressesArray) {
            if (loggedInUser != null && loggedInUser.getEmail().equals(emailAddress)) {
                error(emailComponent, "email.input.validation.own.error",
                        Collections.<String, Object>singletonMap("email", emailAddress));
                return;
            } else if (!isValid(emailAddress)) {
                invalid.add(emailAddress);
            }
        }
        if (!invalid.isEmpty()) {
            error(emailComponent,
                    "email.input.validation.error",
                    Collections.<String, Object>singletonMap("email", StringUtils.join(invalid, ", ")));
        }
    }

}
