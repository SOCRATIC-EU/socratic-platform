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

import java.util.Date;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.joda.time.DateTime;

public class DateInputValidator extends AbstractFormValidator {

    private static final long serialVersionUID = 2790214229554869460L;
    private final FormComponent<Date> dateComponent;
    /**
     * this is used for checking the related dates values
     **/
    private final FormComponent<Date> relatedDateComponent;

    public DateInputValidator(FormComponent<Date> dateComponent, FormComponent<Date> relatedDateComponent) {
        if (dateComponent == null) {
            throw new IllegalArgumentException("argument dateComponent cannot be null");
        }
        this.dateComponent = dateComponent;
        this.relatedDateComponent = relatedDateComponent;
    }

    public void validate(Form<?> form) {

        if (dateComponent.getModelObject() == null) {
            error(dateComponent, "notNullError");
            return;
        } else if (relatedDateComponent.getModelObject() == null) {
            error(relatedDateComponent, "notNullError");
            return;
        }

        Date date = dateComponent.getModelObject();
        Date relatedDate = relatedDateComponent.getModelObject();

        if (new DateTime(date.getTime()).isBefore(new DateTime().toDateMidnight())) {
            error(dateComponent);
        }

        // check if the given date is not greater than related date,
        // i.e. ideation start date should not be less than definition end date.
        if (new DateTime(date.getTime()).isBefore(relatedDate.getTime())) {
            error(dateComponent, "relatedDateError");
        }
    }

    @Override
    public FormComponent<?>[] getDependentFormComponents() {
        return new FormComponent<?>[]{dateComponent, relatedDateComponent};
    }

}
