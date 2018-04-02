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

import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;

/**
 * A simple border around a form component that will add twitter bootstrap
 * specific error and success CSS to the border's class attribute if the form
 * component contains invalid input (based on wicket's own validation mechanism.
 * The corresponding HTML mark-up obviously must contain a feedback panel
 * compatible component with wicket:id = "inputErrors" in order for this border
 * to work.
 *
 * @param <T>
 * @author ATB
 */
public class InputBorder<T> extends Border implements IFeedback {

    /**
     *
     */
    private static final long serialVersionUID = -672647697815368300L;

    // the feedback panel's wicket ID in HTML markup
    private static final String feedbackPanelID = "inputErrors";

    // the input label's wicket ID in HTML markup
    private static final String labelID = "inputLabel";

    // the input label'S wicket ID in HTML markup
    private static final String helpID = "helpLabel";

    // this borders feedback panel
    protected final FeedbackPanel feedback;

    // the form component to validate
    protected final FormComponent<T> inputComponent;

    /**
     * Constructor.
     *
     * @param id
     * @param inputComponent
     */
    public InputBorder(final String id, final FormComponent<T> inputComponent) {
        this(id, inputComponent, new Model<String>(), new Model<String>());
    }

    /**
     * Constructor.
     *
     * @param id
     * @param inputComponent
     * @param labelModel
     */
    public InputBorder(final String id, final FormComponent<T> inputComponent, final IModel<String> labelModel) {
        this(id, inputComponent, labelModel, new Model<String>());
    }

    /**
     * Constructor.
     *
     * @param id
     * @param inputComponent
     * @param labelModel
     * @param helpModel
     */
    public InputBorder(
            final String id,
            final FormComponent<T> inputComponent,
            final IModel<String> labelModel,
            final IModel<String> helpModel) {
        super(id);

        Args.notNull(labelModel, "labelModel");
        Args.notNull(helpModel, "helpModel");

        // set html id so that this border can be refreshed by ajax
        this.setOutputMarkupId(true);

        // add the form component to the border
        this.inputComponent = inputComponent;
        add(this.inputComponent);

        // add the label
        addToBorder(new Label(labelID, labelModel));

        // add the help label
        addToBorder(new Label(helpID, helpModel));

        // add the feedback panel with filter so that it only shows messages
        // relevant for this input component
        this.feedback = new FeedbackPanel(feedbackPanelID, new ContainerFeedbackMessageFilter(this));
        addToBorder(this.feedback.setOutputMarkupId(true));
    }

}
