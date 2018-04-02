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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.ActionPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import de.atb.socratic.web.dashboard.Dashboard.StateForDashboard;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ActionListPanel extends GenericPanel<User> {

    /**
     *
     */
    private static final long serialVersionUID = -159659429992094182L;

    // inject the EJB for managing campaigns
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

    // container holding the list of actions
    private final WebMarkupContainer actionsContainer;

    // Repeating view showing the list of existing actions
    private final DataView<Action> actionsRepeater;

    private final EntityProvider<Action> actionProvider;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private NonCachingImage actionProfilePicture;

    private EntitiySortingCriteria sortingCriteria = EntitiySortingCriteria.created; // by default

    public static final String ACTION_ANCHOR_PREFIX = "_action_";
    final User user;

    public ActionListPanel(final String id, final IModel<User> model, final StyledFeedbackPanel feedbackPanel,
                           final StateForDashboard keyword, final int itemsPerPage) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;

        // get the user
        user = getModelObject();

        // add container with list of existing actions
        actionsContainer = new WebMarkupContainer("actionsContainer");
        add(actionsContainer.setOutputMarkupId(true));

        // add repeating view with list of existing actions
        actionProvider = new ActionProvider(user, keyword);
        actionsRepeater = new DataView<Action>("actionsRepeater", actionProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Action> item) {
                item.setOutputMarkupId(true);
                ActionListPanel.this.populateItem(item, item.getModelObject(), keyword);
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
     * @return
     */
    public long getActionListSize() {
        return actionProvider.size();
    }

    public Component newActionAnchor(final Long id) {
        return new WebMarkupContainer("actionAnchorMan").setMarkupId(ACTION_ANCHOR_PREFIX + id).setOutputMarkupId(true);
    }

    protected void populateItem(final WebMarkupContainer item, final Action action, final StateForDashboard dashboardState) {
        item.setOutputMarkupId(true);
        item.add(newActionProfilePicturePreview(action));
        item.add(newActionAnchor(action.getId()));
        // change wiket id in html
        item.add(newActionPanel("actionPanel", item, action, dashboardState));
    }

    protected NonCachingImage newActionProfilePicturePreview(Action action) {
        actionProfilePicture = new NonCachingImage("profilePicturePreview", ActionPictureResource.get(PictureType.PROFILE,
                action));
        actionProfilePicture.setOutputMarkupId(true);
        return actionProfilePicture;
    }

    /**
     * @param sortingCriteria
     * @return
     */
    public ActionListPanel setSortingCriteria(EntitiySortingCriteria sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
        return this;
    }

    /**
     * @param action
     * @return
     */
    public ActionPanel newActionPanel(final String id, final MarkupContainer item, final Action action,
                                      final StateForDashboard dashboardState) {
        return new ActionPanel(id, Model.of(action), dashboardState) {
            private static final long serialVersionUID = -1078593562271992866L;
        };
    }

    /**
     * @author ATB
     */
    private final class ActionProvider extends EntityProvider<Action> {
        private static final long serialVersionUID = -1727094205049792307L;

        private final User user;
        private final StateForDashboard keyword;

        public ActionProvider(User user, StateForDashboard keyword) {
            super();
            this.user = user;
            this.keyword = keyword;
        }

        @Override
        public Iterator<? extends Action> iterator(long first, long count) {
            Collection<Action> actions = new LinkedList<>();
            if (keyword.equals(StateForDashboard.Lead)) {
                actions = actionService.getAllActionsByDescendingCreationDateAndPostedBy(Long.valueOf(first).intValue(), Long
                        .valueOf(count).intValue(), user, sortingCriteria);
            } else if (keyword.equals(StateForDashboard.TakePart)) {
                actions = activityService.getAllActionsByActionActivityCreator(user, Long.valueOf(first).intValue(), Long
                        .valueOf(count).intValue(), sortingCriteria);
            }
            return actions.iterator();
        }

        @Override
        public long size() {
            if (keyword.equals(StateForDashboard.Lead)) {
                return actionService.countActionsForUser(user);
            } else if (keyword.equals(StateForDashboard.TakePart)) {
                return activityService.countAllActionsByActionActivityCreator(user);
            }
            return 0;
        }

    }

}
