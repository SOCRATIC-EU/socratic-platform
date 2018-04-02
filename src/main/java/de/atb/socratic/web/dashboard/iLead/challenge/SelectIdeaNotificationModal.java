package de.atb.socratic.web.dashboard.iLead.challenge;

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

import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonType;
import de.atb.socratic.web.components.ModalActionButton;
import de.atb.socratic.web.components.NotificationModal;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

public abstract class SelectIdeaNotificationModal extends NotificationModal {

    private static final long serialVersionUID = -3095966901995207916L;

    public SelectIdeaNotificationModal(String id, IModel<String> headerModel, IModel<String> messageModel,
                                       boolean showImmediately) {
        super(id, headerModel, messageModel, showImmediately);

        addButton(new ModalActionButton(this, ButtonType.Primary, new StringResourceModel(
                "selectIdea.notification.modal.show.text", this, null), true) {
            private static final long serialVersionUID = 8931306355855637710L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                selectIdeaClicked(target);
            }
        });

        addButton(new ModalActionButton(this, ButtonType.Default, new StringResourceModel(
                "selectIdea.notification.modal.ignore.text", this, null), true) {
            private static final long serialVersionUID = 6658207871656326413L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                closeNotification(target);
            }

            ;
        });

        setOutputMarkupId(true);
    }

    public abstract void selectIdeaClicked(AjaxRequestTarget target);

    protected void closeNotification(AjaxRequestTarget target) {
        this.appendCloseDialogJavaScript(target);
    }

}
