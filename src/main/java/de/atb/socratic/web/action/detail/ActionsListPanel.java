package de.atb.socratic.web.action.detail;

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
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.ActionPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.iLead.action.AdminActionIterationsListPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ActionsListPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = 1242441524328939708L;

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

    // how many Actions do we show per page
    private static final int itemsPerPage = 10;

    // container holding the list of Actions
    private final WebMarkupContainer actionsContainer;

    private StyledFeedbackPanel feedbackPanel;

    private ActionProvider actionProvider;

    private NonCachingImage actionProfilePicture;

    // Repeating view showing the list of existing actions
    private final DataView<Action> actionsRepeater;

    /**
     * @param id
     * @param feedbackPanel
     */
    public ActionsListPanel(final String id, final StyledFeedbackPanel feedbackPanel) {
        super(id);

        this.feedbackPanel = feedbackPanel;
        // add container with list of existing actions
        actionsContainer = new WebMarkupContainer("actionsContainer");
        add(actionsContainer.setOutputMarkupId(true));

        // add repeating view with list of existing actions
        actionProvider = new ActionProvider();
        actionsRepeater = new DataView<Action>("actions", actionProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Action> item) {
                item.setOutputMarkupId(true);
                ActionsListPanel.this.populateItem(item, item.getModelObject());
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
    private BookmarkablePageLink<? extends BasePage> newActionExplorePageLink(final Action action, final User loggedInUser) {
        final ActionIteration iteration = actionService.getLatestIterationOfAction(action.getId());
        final BookmarkablePageLink<? extends BasePage> exploreContactLink;
        if (iteration != null) {
            exploreContactLink = new BookmarkablePageLink<>(
                    "exploreActionLink",
                    ActionIterationDetailPage.class,
                    new PageParameters().set("iterationId", iteration.getId()).set("id", action.getId()));
        } else {
            exploreContactLink = new BookmarkablePageLink<AdminActionIterationsListPage>(
                    "exploreActionLink",
                    AdminActionIterationsListPage.class,
                    new PageParameters().set("id", action.getId())) {
                private static final long serialVersionUID = -2657582144102784773L;

                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(loggedInUser != null && action.getPostedBy().equals(loggedInUser));
                }
            };
        }
        exploreContactLink.setOutputMarkupId(true);
        return exploreContactLink;
    }

    protected void populateItem(final WebMarkupContainer item, final Action action) {
        final PageParameters params = new PageParameters().set("id", action.getId());
        item.setOutputMarkupId(true);
        item.add(newActionProfilePicturePreview(action));
        item.add(new Label("shortText", new PropertyModel<String>(action, "shortText")));
        item.add(new Label("elevatorPitch", new PropertyModel<String>(action, "elevatorPitch")));
        item.add(new Label("callToAction", new PropertyModel<String>(action, "callToAction")));

        item.add(newActionExplorePageLink(action, loggedInUser));

        // PostedBy and Contributors
        item.add(newProfilePicture(action.getPostedBy()));
        item.add(new Label("postedBy.nickName", new PropertyModel<String>(action, "postedBy.nickName")));

        // Add number of actions comments
        Label actionCommentLabel;
        int commentSize = 0;
        if (action.getComments() != null || !action.getComments().isEmpty()) {
            commentSize = action.getComments().size();
        }
        actionCommentLabel = new Label("actionCommentLabel", Model.of(commentSize));
        actionCommentLabel.setOutputMarkupId(true);
        item.add(actionCommentLabel);

        item.add(new Label("thumbsUpVotes", action.getNoOfUpVotes()));

        Label contributors = new Label("contributors", activityService.countAllActionContributorsBasedOnActionActivity(action));
        item.add(contributors.setOutputMarkupId(true));
    }

    private NonCachingImage newProfilePicture(User user) {
        NonCachingImage picture = new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, user));
        picture.setOutputMarkupId(true);
        return picture;
    }

    protected NonCachingImage newActionProfilePicturePreview(Action action) {
        actionProfilePicture = new NonCachingImage("profilePicturePreview", ActionPictureResource.get(
                PictureType.PROFILE, action));
        actionProfilePicture.setOutputMarkupId(true);
        return actionProfilePicture;
    }

    /**
     * @return
     */
    private WebMarkupContainer newIterationsContainer() {
        return new WebMarkupContainer("iterationsContainer") {
            private static final long serialVersionUID = -5090615480759122784L;

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
            }
        };
    }

    /**
     * @author ATB
     */
    private final class ActionProvider extends EntityProvider<Action> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends Action> iterator(long first, long count) {

            List<Action> allTopicCampaigns = actionService.getAllActionsByDescendingCreationDate(Long.valueOf(first)
                    .intValue(), Long.valueOf(count).intValue());

            return allTopicCampaigns.iterator();
        }

        @Override
        public long size() {
            return actionService.countAllActionsByDescendingCreationDate();
        }
    }
}
