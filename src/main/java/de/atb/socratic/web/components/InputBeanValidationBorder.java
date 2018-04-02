package de.atb.socratic.web.components;

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

import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidator;

/**
 * Extends {@link de.atb.socratic.web.components.InputBorder} and uses Bean
 * Validation via {@link org.apache.wicket.bean.validation.PropertyValidator}
 * to determine if a form component's input is valid or not.
 *
 * @param <T>
 * @author ATB
 */
public class InputBeanValidationBorder<T> extends InputBorder<T> {

    /**
     *
     */
    private static final long serialVersionUID = 7672294641405104492L;

    /**
     * Constructor.
     *
     * @param id
     * @param inputComponent
     */
    public InputBeanValidationBorder(final String id, final FormComponent<T> inputComponent) {
        this(id, inputComponent, new Model<String>(), new Model<String>());
    }

    /**
     * Constructor.
     *
     * @param id
     * @param inputComponent
     * @param labelModel     optional
     */
    public InputBeanValidationBorder(final String id, final FormComponent<T> inputComponent, final IModel<String> labelModel) {
        this(id, inputComponent, labelModel, new Model<String>());
    }

    /**
     * Constructor.
     *
     * @param id
     * @param inputComponent
     * @param labelModel     optional
     * @param helpModel      optional
     */
    public InputBeanValidationBorder(
            final String id,
            final FormComponent<T> inputComponent,
            final IModel<String> labelModel,
            final IModel<String> helpModel) {
        super(id, inputComponent, labelModel, helpModel);

        // remove wicket validation
        this.inputComponent.setRequired(false);

        // add a property validator if the component does not already have one
        addPropertyValidator(inputComponent);
    }

    /**
     * Add a property validator to the component if it does not already have one
     * and the component is using an appropriate model, e.g. PropertyModel.
     *
     * @param fc
     */
    protected void addPropertyValidator(FormComponent<T> fc) {
        if (!hasPropertyValidator(fc)) {
            final PropertyValidator<T> propertyValidator = new PropertyValidator<T>();
            fc.add(propertyValidator);
        }
    }

    /**
     * Checks if the component already has a PropertyValidator.
     *
     * @param fc
     * @return
     */
    protected boolean hasPropertyValidator(FormComponent<T> fc) {
        for (IValidator<?> validator : fc.getValidators()) {
            if (validator instanceof PropertyValidator) {
                return true;
            }
        }
        return false;
    }

}
