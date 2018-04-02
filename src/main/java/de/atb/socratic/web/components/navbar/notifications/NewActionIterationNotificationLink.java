package de.atb.socratic.web.components.navbar.notifications;

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

import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.notification.NewActionIterationNotification;
import de.atb.socratic.web.action.detail.ActionIterationDetailPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Created by Spindler on 25.08.2016.
 */
public class NewActionIterationNotificationLink extends NotificationBookmarkablePageLink<NewActionIterationNotification> {

    private static final long serialVersionUID = -3687349718251374845L;

    public NewActionIterationNotificationLink(String id, IModel<NewActionIterationNotification> model) {
        this(id, new PageParameters(), model);
    }

    public NewActionIterationNotificationLink(String id, PageParameters parameters, IModel<NewActionIterationNotification> model) {
        super(id, ActionIterationDetailPage.class, new PageParameters().set("id",
                model.getObject().getActionIteration().getAction().getId()).set("iterationId",
                model.getObject().getActionIteration().getId()), model);
        add(new Label("actionIterationName", getActionIteration().getTitle())); // get actionIteration title
        add(new Label("actionName", getActionIteration().getAction().getShortText())); // get action name
    }

    public ActionIteration getActionIteration() {
        return getModelObject() != null ? getModelObject().getActionIteration() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy() {
        return new PanelMarkupSourcingStrategy(true);
    }
}
