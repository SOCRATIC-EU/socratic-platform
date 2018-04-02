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
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.AbstractEntity;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.AnchorableBookmarkablePageLink;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.resource.IdeaPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.iLead.idea.AdminIdeaActivityPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.inception.idea.details.IdeaDetailsPage;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.commons.lang.StringUtils;
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
public class IdeaSearchResultsPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = -5976541535877728570L;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    IdeaService ideaService;

    @EJB
    ActivityService activityService;

    private NonCachingImage ideaProfilePicture;

    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many ideas do we show per page
    private static final int itemsPerPage = 10;

    // container holding the list of campaigns
    private final WebMarkupContainer ideasContainer = new WebMarkupContainer("ideasContainer") {
        private static final long serialVersionUID = 589878346655766913L;

        public void renderHead(IHeaderResponse response) {
            super.renderHead(response);
            // add javascript to load tagsinput plugin
            response.render(OnLoadHeaderItem.forScript(String.format(JSTemplates.LOAD_TABLE_SORTER, "idea-list")));
        }

        ;
    };

    private String searchQuery;

    /**
     * @param id
     */
    public IdeaSearchResultsPanel(final String id) {
        super(id);

        // add the container holding list of existing campaigns
        add(ideasContainer.setOutputMarkupId(true));
        final DataView<Idea> ideas = new DataView<Idea>("ideas", new IdeaProvider(), itemsPerPage) {
            private static final long serialVersionUID = 1918076240043442975L;

            @Override
            protected void populateItem(final Item<Idea> item) {
                final Idea idea = item.getModelObject();
                item.setOutputMarkupId(true);
                item.add(newIdeaProfilePicturePreview(idea));

                item.add(new Label("shortText", new PropertyModel<String>(idea, "shortText")));

                // idea owner
                AjaxLink<Void> link = userImageLink("link", idea.getPostedBy());
                link.add(new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, idea.getPostedBy())));
                item.add(link);
                item.add(new Label("postedBy.nickName", new PropertyModel<String>(idea, "postedBy.nickName")));

                Label contributors = new Label("contributors", activityService.countAllContributorsByIdea(idea));
                item.add(contributors.setOutputMarkupId(true));

                item.add(newExploreLink(idea));
            }
        };
        // how many campaign to show per table page
        ideasContainer.add(ideas);
        add(new BootstrapAjaxPagingNavigator("navigatorHead", ideas) {
            private static final long serialVersionUID = 9098656057022095560L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(ideas.getPageCount() > 1);
            }
        });

        add(new BootstrapAjaxPagingNavigator("navigatorFoot", ideas) {
            private static final long serialVersionUID = 9098656057022095560L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(ideas.getPageCount() > 1);
            }
        });
    }

    private BookmarkablePageLink<? extends BasePage> newExploreLink(final Idea idea) {
        if (idea.getPostedBy().equals(loggedInUser)) {
            return new BookmarkablePageLink<>("exploreLink", IdeaDetailsPage.class, new PageParameters().set("id",
                    idea.getCampaign().getId()).set("ideaId", idea.getId()));
        } else {
            return new BookmarkablePageLink<>("exploreLink", AdminIdeaActivityPage.class, new PageParameters().set("id",
                    idea.getId()));
        }
    }

    protected NonCachingImage newIdeaProfilePicturePreview(Idea idea) {
        ideaProfilePicture = new NonCachingImage("profilePicturePreview", IdeaPictureResource.get(
                PictureType.PROFILE, idea));
        ideaProfilePicture.setOutputMarkupId(true);
        return ideaProfilePicture;
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

    private static PageParameters forId(final AbstractEntity e) {
        return new PageParameters().set("id", e.getId());
    }

    private static String ideaAnchor(final Idea idea) {
        return IdeasPage.IDEA_ANCHOR_PREFIX + idea.getId();
    }

    private AnchorableBookmarkablePageLink<IdeasPage> newShowIdeaLink(final Idea idea) {
        AnchorableBookmarkablePageLink<IdeasPage> link
                = new AnchorableBookmarkablePageLink<IdeasPage>("ideaLink",
                IdeasPage.class,
                forId(idea.getCampaign()),
                ideaAnchor(idea));
        link.add(new Label("shortText", new PropertyModel<String>(idea, "shortText")));
        return link;
    }

    /**
     * @param campaign
     * @return
     */
    private BookmarkablePageLink<IdeasPage> newShowCampaignLink(final Campaign campaign) {
        BookmarkablePageLink<IdeasPage> link
                = new BookmarkablePageLink<IdeasPage>("campaignLink", IdeasPage.class, forId(campaign));
        link.add(new Label("name", new PropertyModel<String>(campaign, "name")));
        return link;
    }

    /**
     * Test1!
     *
     * @param collaborators
     * @return
     */
    private String getCollaboratorNames(final List<User> collaborators) {
        List<String> colNames = new LinkedList<String>();
        for (User collaborator : collaborators) {
            colNames.add(collaborator.getNickName());
        }
        return StringUtils.join(colNames, ", ");
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
    private final class IdeaProvider extends EntityProvider<Idea> {

        private static final long serialVersionUID = -2732454318709176432L;

        @Override
        public Iterator<? extends Idea> iterator(long first, long count) {
            return ideaService.fullTextSearch(Long.valueOf(first).intValue(),
                    Long.valueOf(count).intValue(), searchQuery, loggedInUser).iterator();
        }

        @Override
        public long size() {
            return ideaService.countFullTextResults(searchQuery, loggedInUser);
        }
    }

}
