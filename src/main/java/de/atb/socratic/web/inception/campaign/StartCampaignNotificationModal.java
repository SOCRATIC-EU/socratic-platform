package de.atb.socratic.web.inception.campaign;

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

public abstract class StartCampaignNotificationModal extends NotificationModal {

    private static final long serialVersionUID = -9084096797729290014L;

    public StartCampaignNotificationModal(String id, IModel<String> headerModel, IModel<String> messageModel,
                                          boolean showImmediately) {
        super(id, headerModel, messageModel, showImmediately);

        addButton(new ModalActionButton(this, ButtonType.Primary, new StringResourceModel(
                "start.notification.modal.show.text", this, null), true) {
            private static final long serialVersionUID = 7910019200661709975L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                startCampaignClicked(target);
            }
        });
        addButton(new ModalActionButton(this, ButtonType.Default, new StringResourceModel(
                "start.notification.modal.ignore.text", this, null), true) {
            private static final long serialVersionUID = -8579196626175159237L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                closeNotification(target);
            }
        });
    }

    public abstract void startCampaignClicked(AjaxRequestTarget target);

    protected void closeNotification(AjaxRequestTarget target) {
        this.appendCloseDialogJavaScript(target);
    }
}
