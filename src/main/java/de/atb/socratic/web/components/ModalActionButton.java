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

import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonType;
import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

/**
 * @author ATB
 */
public class ModalActionButton extends AjaxLink<Void> {

    /**
     *
     */
    private static final long serialVersionUID = 6914370101769512451L;

    private static final String id = "button";

    private ButtonType buttonType;

    private IModel<?> bodyModel;

    private Modal modal;

    private boolean closeModalAfterSubmit = true;

    /**
     * @param modal
     * @param buttonType
     * @param bodyModel
     * @param closeModalAfterSubmit
     */
    public ModalActionButton(final Modal modal,
                             final ButtonType buttonType,
                             final IModel<?> bodyModel,
                             final boolean closeModalAfterSubmit) {
        super(id);

        this.modal = modal;
        this.buttonType = buttonType;
        this.bodyModel = bodyModel;
        this.closeModalAfterSubmit = closeModalAfterSubmit;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.Component#onConfigure()
     */
    @Override
    protected void onConfigure() {
        super.onConfigure();
        setBody(bodyModel);
        add(new ButtonBehavior(buttonType));
    }

    /*
     * (non-Javadoc)
     * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
     */
    @Override
    public void onClick(AjaxRequestTarget target) {
        // if configured close modal window
        if (closeModalAfterSubmit) {
            Effects.hideModal(target, modal.getMarkupId());
        }
        onAfterClick(target);
    }

    /**
     * Override for actions after clicking the button.
     *
     * @param target
     */
    protected void onAfterClick(AjaxRequestTarget target) {
    }

}
