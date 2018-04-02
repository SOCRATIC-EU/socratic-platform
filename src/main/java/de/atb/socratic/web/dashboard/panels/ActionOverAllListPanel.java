package de.atb.socratic.web.dashboard.panels;

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.action.detail.ActionSolutionPage;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import de.atb.socratic.web.dashboard.UserDashboardPage.Status;
import de.atb.socratic.web.dashboard.iLead.action.AdminActionActivityPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ActionOverAllListPanel extends GenericPanel<User> {

    /**
     *
     */
    private static final long serialVersionUID = -159659429992094182L;

    // inject the EJB for managing actions
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    ActionService actionService;

    @EJB
    ActivityService activityService;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many actions do we show initially
    private static final int itemsPerPage = 2;

    // container holding the list of actions
    private final WebMarkupContainer actionsContainer;

    // Repeating view showing the list of existing actions
    private final DataView<Action> actionsRepeater;

    private final EntityProvider<Action> actionProvider;

    private EntitiySortingCriteria sortingCriteria = EntitiySortingCriteria.created; // by default

    public ActionOverAllListPanel(final String id, final IModel<User> model, final Status status) {
        super(id, model);

        // get the user
        final User user = getModelObject();

        // add container with list of existing actions
        actionsContainer = new WebMarkupContainer("actionsContainer");
        add(actionsContainer.setOutputMarkupId(true));

        // add repeating view with list of existing actions
        actionProvider = new ActionProvider(user, status);
        actionsRepeater = new DataView<Action>("actionsRepeater", actionProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Action> item) {
                item.setOutputMarkupId(true);
                ActionOverAllListPanel.this.populateItem(item, item.getModelObject(), status);
            }
        };


        actionsContainer.add(actionsRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", actionsRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(actionsRepeater.getPageCount() > 1);
            }
        });

    }

    /**
     * @param sortingCriteria
     * @return
     */
    public ActionOverAllListPanel setSortingCriteria(EntitiySortingCriteria sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
        return this;
    }

    private Link<ActionSolutionPage> newExploreLink(final Action action, final Status dashboardState) {
        final Link<ActionSolutionPage> link = new Link<ActionSolutionPage>("exploreLink") {
            private static final long serialVersionUID = -310533532532643267L;

            @Override
            public void onClick() {
                if (Status.LEAD == dashboardState) {
                    setResponsePage(AdminActionActivityPage.class, new PageParameters().set("id", action.getId()));
                } else {
                    setResponsePage(ActionSolutionPage.class, new PageParameters().set("id", action.getId()));
                }
            }
        };
        final String key = Status.PARTICIPATE == dashboardState ? "explore.text" : "manage.text";
        link.add(new Label("exploreLinkLabel", new StringResourceModel(key, this, null)));
        return link;
    }

    /**
     * @return
     */
    public long getActionListSize() {
        return actionProvider.size();
    }

    protected void populateItem(final WebMarkupContainer item,
                                final Action action,
                                final Status dashboardState) {

        item.setOutputMarkupId(true);
        item.add(new Label("shortText", new PropertyModel<String>(action, "shortText")));
        item.add(new Label("elevatorPitch", new PropertyModel<String>(action, "elevatorPitch")));
        item.add(newExploreLink(action, dashboardState));
        item.add(new Label("exploreMsg", new PropertyModel<String>(action, "callToAction")));
    }

    /**
     * @author ATB
     */
    private final class ActionProvider extends EntityProvider<Action> {
        private static final long serialVersionUID = -1727094205049792307L;

        private final User user;
        private final Status status;

        public ActionProvider(User user, Status status) {
            super();
            this.user = user;
            this.status = status;
        }

        @Override
        public Iterator<? extends Action> iterator(long first, long count) {
            List<Action> actions = new LinkedList<>();
            // based on status, call different methods
            if (status.equals(Status.PARTICIPATE)) {
                actions = activityService.getAllActionsByActionActivityCreator(user, Long
                        .valueOf(first).intValue(), Long.valueOf(count).intValue(), sortingCriteria);
            } else if (status.equals(Status.LEAD)) {
                actions = actionService.getAllActionsByDescendingCreationDateAndPostedBy(Long.valueOf(first).intValue(),
                        Long.valueOf(count).intValue(), user, sortingCriteria);
            } else if (status.equals(Status.RECOMMENDED)) {

            }
            return actions.iterator();
        }

        @Override
        public long size() {
            if (status.equals(Status.PARTICIPATE)) {
                return activityService.countAllActionsByActionActivityCreator(user);
            } else if (status.equals(Status.LEAD)) {
                return actionService.countActionsForUser(user);
            } else if (status.equals(Status.RECOMMENDED)) {
                return 0;
            }
            return 0;
        }

    }
}
