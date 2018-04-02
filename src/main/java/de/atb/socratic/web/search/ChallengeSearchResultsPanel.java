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
import de.atb.socratic.model.AbstractEntity;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.resource.ChallengePictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.inception.idea.IdeasPage;
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
public class ChallengeSearchResultsPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = -5976541535877728570L;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    @EJB
    ActivityService activityService;

    private NonCachingImage campaignProfilePicture;

    // how many ideas do we show per page
    private static final int itemsPerPage = 10;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // container holding the list of campaigns
    private final WebMarkupContainer campaignContainer = new WebMarkupContainer("campaignsContainer") {
        private static final long serialVersionUID = 589878346655766913L;

        public void renderHead(IHeaderResponse response) {
            super.renderHead(response);
            // add javascript to load tagsinput plugin
            response.render(OnLoadHeaderItem.forScript(String.format(JSTemplates.LOAD_TABLE_SORTER, "campaign-list")));
        }

        ;
    };

    private String searchQuery;

    /**
     * @param id
     */
    public ChallengeSearchResultsPanel(final String id) {
        super(id);

        // add the container holding list of existing campaigns
        add(campaignContainer.setOutputMarkupId(true));
        final DataView<Campaign> campaigns = new DataView<Campaign>("campaigns", new CampaignProvider(), itemsPerPage) {
            private static final long serialVersionUID = 1918076240043442975L;

            @Override
            protected void populateItem(final Item<Campaign> item) {
                final Campaign campaign = item.getModelObject();
                item.setOutputMarkupId(true);
                item.add(newCampaignProfilePicturePreview(campaign));
                item.add(new Label("name", new PropertyModel<String>(campaign, "name")));
                // challenge owner
                AjaxLink<Void> link = userImageLink("link", campaign.getCreatedBy());
                link.add(new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, campaign.getCreatedBy())));
                item.add(link);
                item.add(new Label("createdBy.nickName", new PropertyModel<String>(campaign, "createdBy.nickName")));

                Label contributors = new Label("contributors", activityService.countAllChallengeContributorsByCampaign(campaign));
                item.add(contributors.setOutputMarkupId(true));

                item.add(campaign.getInnovationStatus().getLinkToCorrespondingStage("exploreChallengeLink", campaign, loggedInUser));
            }
        };
        // how many campaign to show per table page
        campaignContainer.add(campaigns);

        add(new BootstrapAjaxPagingNavigator("navigatorHead", campaigns) {
            private static final long serialVersionUID = 9098656057022095560L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(campaigns.getPageCount() > 1);
            }
        });

        add(new BootstrapAjaxPagingNavigator("navigatorFoot", campaigns) {
            private static final long serialVersionUID = 9098656057022095560L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(campaigns.getPageCount() > 1);
            }
        });
    }

    protected NonCachingImage newCampaignProfilePicturePreview(Campaign campaign) {
        campaignProfilePicture = new NonCachingImage("profilePicturePreview", ChallengePictureResource.get(
                PictureType.PROFILE, campaign));
        campaignProfilePicture.setOutputMarkupId(true);
        return campaignProfilePicture;
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
     * @param campaign
     * @return
     */
    private BookmarkablePageLink<IdeasPage> newShowCampaignLink(final Campaign campaign) {
        BookmarkablePageLink<IdeasPage> link
                = new BookmarkablePageLink<IdeasPage>("campaignLink", IdeasPage.class, forId(campaign));
        link.add(new Label("name", new PropertyModel<String>(campaign, "name")));
        return link;
    }

    private static PageParameters forId(final AbstractEntity e) {
        return new PageParameters().set("id", e.getId());
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
    private final class CampaignProvider extends EntityProvider<Campaign> {

        private static final long serialVersionUID = -2732454318709176432L;

        @Override
        public Iterator<? extends Campaign> iterator(long first, long count) {
            return campaignService.fullTextSearch(Long.valueOf(first).intValue(),
                    Long.valueOf(count).intValue(), searchQuery, loggedInUser).iterator();
        }

        @Override
        public long size() {
            return campaignService.countFullTextResults(searchQuery, loggedInUser);
        }
    }

}
