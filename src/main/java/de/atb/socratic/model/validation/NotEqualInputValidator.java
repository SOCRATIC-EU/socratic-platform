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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.util.lang.Objects;

public class NotEqualInputValidator extends AbstractFormValidator {

    private static final long serialVersionUID = 1L;

    /**
     * form components to be checked.
     */
    private final FormComponent<?>[] components;

    /**
     * Construct.
     *
     * @param formComponent1 a form component
     * @param formComponent2 a form component
     */
    public NotEqualInputValidator(FormComponent<?> formComponent1,
                                  FormComponent<?> formComponent2) {
        if (formComponent1 == null) {
            throw new IllegalArgumentException(
                    "argument formComponent1 cannot be null");
        }
        if (formComponent2 == null) {
            throw new IllegalArgumentException(
                    "argument formComponent2 cannot be null");
        }
        components = new FormComponent[]{formComponent1, formComponent2};
    }

    public FormComponent<?>[] getDependentFormComponents() {
        return components;
    }

    public void validate(Form<?> form) {
        // we have a choice to validate the type converted values or the raw
        // input values, we validate the raw input
        final FormComponent<?> formComponent1 = components[0];
        final FormComponent<?> formComponent2 = components[1];

        if (Objects.equal(formComponent1.getInput(), formComponent2.getInput())) {
            error(formComponent2);
        }
    }

}
