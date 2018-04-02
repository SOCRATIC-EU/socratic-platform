/**
 *
 */
package de.atb.socratic.web.search;

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

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.action.detail.ActionIterationDetailPage;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.resource.ActionPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.iLead.action.AdminActionIterationsListPage;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ActionSearchResultsPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = -5976541535877728570L;

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

    // how many actions do we show per page
    private static final int itemsPerPage = 10;

    private NonCachingImage actionProfilePicture;

    // container holding the list of actions
    private final WebMarkupContainer actionsContainer = new WebMarkupContainer("actionsContainer") {
        private static final long serialVersionUID = 589878346655766913L;

        public void renderHead(IHeaderResponse response) {
            super.renderHead(response);
            // add javascript to load tagsinput plugin
            response.render(OnLoadHeaderItem.forScript(String.format(JSTemplates.LOAD_TABLE_SORTER, "action-list")));
        }

        ;
    };

    private String searchQuery;

    /**
     * @param id
     */
    public ActionSearchResultsPanel(final String id) {
        super(id);

        // add the container holding list of existing actions
        add(actionsContainer.setOutputMarkupId(true));
        final DataView<Action> actions = new DataView<Action>("actions", new ActionProvider(), itemsPerPage) {
            private static final long serialVersionUID = 1918076240043442975L;

            @Override
            protected void populateItem(final Item<Action> item) {
                final Action action = item.getModelObject();
                item.setOutputMarkupId(true);
                item.add(newActionProfilePicturePreview(action));
                item.add(new Label("shortText", new PropertyModel<String>(action, "shortText")));
                item.add(new Label("callToAction", new PropertyModel<String>(action, "callToAction")));

                item.add(newActionExplorePageLink(action, loggedInUser));

                // action owner
                AjaxLink<Void> link = userImageLink("link", action.getPostedBy());
                link.add(new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, action.getPostedBy())));
                item.add(link);
                item.add(new Label("postedBy.nickName", new PropertyModel<String>(action, "postedBy.nickName")));

                Label contributors = new Label("contributors", activityService.countAllActionContributorsBasedOnActionActivity(action));
                item.add(contributors.setOutputMarkupId(true));
            }
        };
        // how many campaign to show per table page
        actionsContainer.add(actions);
        add(new BootstrapAjaxPagingNavigator("navigatorHead", actions) {
            private static final long serialVersionUID = 9098656057022095560L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(actions.getPageCount() > 1);
            }
        });

        add(new BootstrapAjaxPagingNavigator("navigatorFoot", actions) {
            private static final long serialVersionUID = 9098656057022095560L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(actions.getPageCount() > 1);
            }
        });
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
                private static final long serialVersionUID = -7836728713227454916L;

                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(action.getPostedBy().equals(loggedInUser));
                }
            };
        }
        exploreContactLink.setOutputMarkupId(true);
        return exploreContactLink;
    }

    /**
     * @param user
     * @return
     */
    private AjaxLink<Void> userImageLink(String wicketId, final User user) {
        AjaxLink<Void> link = new AjaxLink<Void>(wicketId) {
            private static final long serialVersionUID = -6633994061963892062L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (user != null) {
                    setResponsePage(UserProfileDetailsPage.class, new PageParameters().set("id", user.getId()));
                }
            }
        };

        if (user != null) {
            link.add(AttributeModifier.append("title", user.getNickName()));
        }
        return link;
    }

    /**
     * @param searchQuery the searchQuery to set
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * @author ATB
     */
    private final class ActionProvider extends EntityProvider<Action> {

        private static final long serialVersionUID = -2732454318709176432L;

        @Override
        public Iterator<? extends Action> iterator(long first, long count) {
            return actionService.fullTextSearch(Long.valueOf(first).intValue(),
                    Long.valueOf(count).intValue(), searchQuery, loggedInUser).iterator();
        }

        @Override
        public long size() {
            return actionService.countFullTextResults(searchQuery, loggedInUser);
        }
    }

}
