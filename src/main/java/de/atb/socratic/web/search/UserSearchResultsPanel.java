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
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
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
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class UserSearchResultsPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = -5976541535877728570L;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    UserService userService;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many user do we show per page
    private static final int itemsPerPage = 10;

    // container holding the list of campaigns
    private final WebMarkupContainer userContainer = new WebMarkupContainer("usersContainer") {
        private static final long serialVersionUID = 589878346655766913L;

        public void renderHead(IHeaderResponse response) {
            super.renderHead(response);
            // add javascript to load tagsinput plugin
            response.render(OnLoadHeaderItem.forScript(String.format(JSTemplates.LOAD_TABLE_SORTER, "user-list")));
        }

        ;
    };

    private String searchQuery;

    /**
     * @param id
     */
    public UserSearchResultsPanel(final String id) {
        super(id);

        // add the container holding list of existing users
        add(userContainer.setOutputMarkupId(true));
        final DataView<User> users = new DataView<User>("users", new UserProvider(), itemsPerPage) {
            private static final long serialVersionUID = 1918076240043442975L;

            @Override
            protected void populateItem(final Item<User> item) {
                final User user = item.getModelObject();

                AjaxLink<Void> link = userImageLink("link", user);
                link.add(new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, user)));
                item.add(link);
                item.add(new Label("nickName", new PropertyModel<String>(user, "nickName")));

                // User leads, ideas, contribution etc panel
                int totalNoOfProcessesLeads = user.getNoOfCampaignsLeads() + user.getNoOfIdeasLeads() + user.getNoOfActionsLeads();
                item.add(new Label("leadLabel", totalNoOfProcessesLeads));
                item.add(new Label("ideasLabel", user.getNoOfIdeasLeads()));
                item.add(new Label("contributionsLabel", user.getNoOfCommentsPosts()));
                item.add(new Label("likesLabel", user.getNoOfLikesReceived()));

                // skills
                item.add(newIdeaSkillsPanel(user));

                // interests
                item.add(newIdeaInterestsPanel(user));
            }
        };
        // how many campaign to show per table page
        userContainer.add(users);

        userContainer.add(new BootstrapAjaxPagingNavigator("navigatorHead", users) {
            private static final long serialVersionUID = 9098656057022095560L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(users.getPageCount() > 1);
            }
        });
        userContainer.add(new BootstrapAjaxPagingNavigator("navigatorFoot", users) {
            private static final long serialVersionUID = 9098656057022095560L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(users.getPageCount() > 1);
            }
        });
    }

    private WebMarkupContainer newIdeaSkillsPanel(final User user) {
        final WebMarkupContainer tagsContainer = new WebMarkupContainer("skillsContainer") {
            private static final long serialVersionUID = 7869210537650985756L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!user.getSkills().isEmpty());
            }
        };
        tagsContainer.setOutputMarkupPlaceholderTag(true);
        tagsContainer.add(new AttributeModifier("title", new StringResourceModel("skills.title.text", this, null)));

        tagsContainer.add(new ListView<Tag>("skills", new ListModel<>(user.getSkills())) {
            private static final long serialVersionUID = -707257134087075019L;

            @Override
            protected void populateItem(ListItem<Tag> item) {
                item.add(new Label("skill", item.getModelObject().getTag()));
            }
        });
        return tagsContainer;
    }


    private WebMarkupContainer newIdeaInterestsPanel(final User user) {
        final WebMarkupContainer tagsContainer = new WebMarkupContainer("interestsContainer") {
            private static final long serialVersionUID = 7869210537650985756L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!user.getInterests().isEmpty());
            }
        };
        tagsContainer.setOutputMarkupPlaceholderTag(true);
        tagsContainer.add(new AttributeModifier("title", new StringResourceModel("interests.title.text", this, null)));

        tagsContainer.add(new ListView<Tag>("interests", new ListModel<>(user.getInterests())) {
            private static final long serialVersionUID = -707257134087075019L;

            @Override
            protected void populateItem(ListItem<Tag> item) {
                item.add(new Label("interest", item.getModelObject().getTag()));
            }
        });
        return tagsContainer;
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
    private final class UserProvider extends EntityProvider<User> {

        private static final long serialVersionUID = -2732454318709176432L;

        @Override
        public Iterator<? extends User> iterator(long first, long count) {
            return userService.fullTextSearch(Long.valueOf(first).intValue(), Long.valueOf(count).intValue(), searchQuery,
                    loggedInUser).iterator();
        }

        @Override
        public long size() {
            return userService.countFullTextResults(searchQuery, loggedInUser);
        }
    }

}
