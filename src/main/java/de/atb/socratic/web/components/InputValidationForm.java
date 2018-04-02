/**
 *
 */
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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * A simple validation form that will add twitter bootstrap specific error and
 * success CSS to the class attribute of InputBorders contained in this form,
 * depending on whether the border's form component contains invalid or valid
 * input respectively. So far this form only works correctly with
 * {@link de.atb.socratic.web.components.InputBorder} and its subclasses.
 *
 * @author ATB
 */
public class InputValidationForm<T> extends Form<T> {

    /**
     *
     */
    private static final long serialVersionUID = 8724766263334650533L;

    /**
     * @param id
     */
    public InputValidationForm(final String id) {
        super(id);
    }

    /**
     * @param id
     * @param model
     */
    public InputValidationForm(final String id, final IModel<T> model) {
        super(id, model);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.markup.html.form.Form#onError()
     */
    @Override
    protected void onError() {
        super.onError();
        addErrorCSS();
        onErrorHandling();
    }

    /**
     * Go through this form's InputValidationBorder components and add error CSS
     * to their class attribute if the border's inputComponent has a validation
     * error.
     */
    protected void addErrorCSS() {
        IVisitor<? extends InputBorder<?>, Object> visitor = new IVisitor<InputBorder<?>, Object>() {

            @Override
            public void component(InputBorder<?> object, IVisit<Object> visit) {
                if (object.inputComponent.hasErrorMessage()) {
                    object.add(new CSSAppender("error"));
                }
            }
        };
        this.visitChildren(InputBorder.class, visitor);
    }

    /**
     * Override this method for further specific error handling.
     */
    protected void onErrorHandling() {
    }

}
