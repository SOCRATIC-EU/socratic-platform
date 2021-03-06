package de.atb.socratic.web.dashboard.message;

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

import javax.inject.Inject;

import de.atb.socratic.model.User;
import de.atb.socratic.web.dashboard.message.AdminMessageListPanel.MessageState;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class AdminMessageSentPage extends AdminMessage {
    private static final long serialVersionUID = 1396143962757493308L;

    private final AdminMessageListPanel adminMessageListPanel;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    protected User loggedInUser;

    public AdminMessageSentPage(PageParameters parameters) {
        super(parameters);

        adminMessageListPanel = new AdminMessageListPanel("messagesList", Model.of(loggedInUser), feedbackPanel, MessageState.sending) {

            private static final long serialVersionUID = 8087201252939474804L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }

            @Override
            protected void onUnreadMessagesNoChanged(AjaxRequestTarget target) {
            }
        };
        add(adminMessageListPanel.setOutputMarkupId(true));
    }
}
