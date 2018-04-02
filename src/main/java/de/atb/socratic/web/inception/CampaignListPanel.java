/**
 *
 */
package de.atb.socratic.web.inception;

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

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.authorization.strategies.metadata.EffMetaDataRoleAuthorizationStrategy;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.CampaignType;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.scope.ScopeType;
import de.atb.socratic.model.tour.TourHelper;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.votes.ToursService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.EFFSession;
import de.atb.socratic.web.components.InnovationStatusIndicatorPanel;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.ajax.panel.AjaxLazyLoadDensityPanel;
import de.atb.socratic.web.components.resource.ChallengePictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.inception.CampaignsPage.SortingCriteria;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.seam.transaction.TransactionPropagation;
import org.jboss.seam.transaction.Transactional;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class CampaignListPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = 1242441524328939708L;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    ToursService toursService;

    // how many campaigns do we show per page
    private static final int itemsPerPage = 10;
    // container holding the list of campaigns
    private final WebMarkupContainer campaignsContainer;
    private StyledFeedbackPanel feedbackPanel;
    private SortingCriteria sortingCriteria = SortingCriteria.Date; // by default 
    private int daysLeft;
    private Date filterStartDate;
    private Date filterEndDate;
    private Campaign campaignToHandle;
    private CampaignProvider campaignProvider;
    EFFSession session = (EFFSession) EFFSession.get();
    private AbstractDefaultAjaxBehavior behave;
    private TourHelper tourHelper = new TourHelper(this);

    static final String Session_Filter_InnovationObjective = CampaignListPanel.class.getSimpleName() + ".filter.innovation.objective";
    static final String Session_Filter_StartDate = CampaignListPanel.class.getSimpleName() + ".filter.date.start";
    static final String Session_Filter_EndDate = CampaignListPanel.class.getSimpleName() + ".filter.date.end";
    static final String Session_SortingCriteria = CampaignListPanel.class.getSimpleName() + ".sortingCriteria";
    private NonCachingImage challengeProfilePicture;

    /**
     * @param id
     * @param feedbackPanel
     */
    public CampaignListPanel(final String id, final StyledFeedbackPanel feedbackPanel) {
        super(id);
        this.feedbackPanel = feedbackPanel;

        final Form<Campaign> deleteForm = new Form<>("deleteForm");
        add(deleteForm);

        campaignsContainer = newCampaignsContainer();
        deleteForm.add(campaignsContainer);

        // add button to create new campaign
        final BookmarkablePageLink<CampaignAddEditPage> addCamp =
                new BookmarkablePageLink<>("addCampaignLink", CampaignAddEditPage.class);
        add(addCamp);

        campaignProvider = new CampaignProvider();
        DataView<Campaign> campaigns = new DataView<Campaign>("campaigns", campaignProvider, itemsPerPage) {

            private static final long serialVersionUID = 789669450347695209L;

            @Override
            protected void populateItem(final Item<Campaign> item) {
                final Campaign campaign = item.getModelObject();

                WebMarkupContainer tdCampaign = new WebMarkupContainer("td.campaign");
                tdCampaign.add(newGotoCorrespondingStageLink(campaign));

                tdCampaign.add(newProfilePicture(campaign.getCreatedBy()));
                tdCampaign.add(new Label("createdBy.nickName", new PropertyModel<String>(campaign, "createdBy.nickName")));
                // challenge picture
                tdCampaign.add(newChallengeProfilePicturePreview(campaign));

                LocalDate dueDate = new LocalDate(getCorrespondingDateOfPhase(campaign));
                daysLeft = new Period(new LocalDate(), dueDate, PeriodType.days()).getDays();

                // this is to avoid "-x days" message 
                if (daysLeft >= 0) {
                    tdCampaign.add(new Label("dueDate", daysLeft + " "
                            + new StringResourceModel("days.to.go", this, null).getString() + " "
                            + new StringResourceModel(campaign.getInnovationStatus().getMessageKey(), this, null).getString()));
                } else { // if days are less than zero then change the message
                    tdCampaign.add(
                            new Label("dueDate", campaign.getInnovationStatus().getMessageKey() + " is finished!"));
                }
                tdCampaign.add(new Label("elevatorPitch", new PropertyModel<String>(campaign, "elevatorPitch")));

                tdCampaign.add(new Label("helpText", new PropertyModel<String>(campaign, "callToAction")));
                tdCampaign.add(campaign.getInnovationStatus().getLinkToCorrespondingStage("takePartLink", campaign, loggedInUser));

                //Add number of challenge comments 
                Label challengeCommentLabel;
                int commentSize = 0;
                if (campaign.getComments() != null || !campaign.getComments().isEmpty()) {
                    commentSize = campaign.getComments().size();
                }
                challengeCommentLabel = new Label("challengeCommentLabel", Model.of(commentSize));
                challengeCommentLabel.setOutputMarkupId(true);
                tdCampaign.add(challengeCommentLabel);

                tdCampaign.add(new Label("thumbsUpVotes", campaign.getNoOfUpVotes()));

                tdCampaign.add(newStageIndicator(campaign));

                item.add(tdCampaign);
            }
        };
        // add list of campaigns to container
        campaignsContainer.setOutputMarkupId(true);
        campaigns.setOutputMarkupId(true);
        campaignsContainer.add(campaigns);

        // add links for table pagination
        campaignsContainer.add(new BootstrapAjaxPagingNavigator(
                "navigatorHead", campaigns));

        behave = tourHelper.getAjaxBehavior(TourHelper.TOUR_INCEPTION_NAME, toursService, loggedInUser);
        add(behave);
    }

    protected NonCachingImage newChallengeProfilePicturePreview(Campaign challenge) {
        challengeProfilePicture = new NonCachingImage("profilePicturePreview", ChallengePictureResource.get(PictureType.PROFILE, challenge));
        challengeProfilePicture.setOutputMarkupId(true);
        return challengeProfilePicture;
    }

    private Date getCorrespondingDateOfPhase(Campaign campaign) {
        switch (campaign.getInnovationStatus()) {
            case DEFINITION:
                return campaign.getChallengeOpenForDiscussionEndDate();
            case INCEPTION:
                return campaign.getIdeationEndDate();
            case PRIORITISATION:
                return campaign.getSelectionEndDate();
            default:
                return new Date();
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        String create = tourHelper.createInceptionTour(campaignProvider.size(), toursService, loggedInUser, this);
        if (create == null) {
            return;
        }
        response.render(JavaScriptHeaderItem.forScript(tourHelper.getAjaxCallbackJS(behave), ""));
        response.render(JavaScriptHeaderItem.forUrl(JSTemplates.BOOTSTRAP_TOUR, JSTemplates.BOOTSTRAP_TOUR_REF_ID));
        response.render(OnDomReadyHeaderItem.forScript(create));
    }

    protected WebMarkupContainer getCommentsDensityPanel(final Campaign campaign) {
        WebMarkupContainer densityPanel = null;
        if (campaign.getScope() != null) {
            final String resOpen = campaign.getScope().getScopeType() == ScopeType.OPEN ? ".open" : "";
            final String resIdentifier = "density.comments" + resOpen + ".title.text";
            densityPanel = new AjaxLazyLoadDensityPanel<Float>("commentsDensity", new StringResourceModel(resIdentifier, this, null)) {
                private static final long serialVersionUID = 1L;

                @Override
                @Transactional(TransactionPropagation.REQUIRED)
                public Float getDensity() {
                    if (Float.isNaN(campaign.getCommentDensity())) {
                        campaign.setCommentDensity(campaignService.getCommentDensity(campaign));
                    }
                    return campaign.getCommentDensity();
                }

                @Override
                public void onClick(AjaxRequestTarget target) {
                    System.out.println("comment density clicked for campaign " + campaign.getId());
                }
            }.setIcon("comments-alt").addCssClass(Model.of("details"));
        } else {
            densityPanel = new WebMarkupContainer("commentsDensity");
        }
        EffMetaDataRoleAuthorizationStrategy.authorize(densityPanel, RENDER, UserRole.ADMIN, UserRole.MANAGER, UserRole.SUPER_ADMIN);
        return densityPanel;
    }

    protected WebMarkupContainer getIdeasDensityPanel(final Campaign campaign) {
        WebMarkupContainer densityPanel = null;
        if (campaign.getScope() != null) {
            final String resOpen = campaign.getScope().getScopeType() == ScopeType.OPEN ? ".open" : "";
            final String resIdentifier = "density.ideas" + resOpen + ".title.text";
            densityPanel = new AjaxLazyLoadDensityPanel<Float>("ideasDensity", new StringResourceModel(resIdentifier, this, null)) {
                private static final long serialVersionUID = 1L;

                @Override
                @Transactional(TransactionPropagation.REQUIRED)
                public Float getDensity() {
                    if (Float.isNaN(campaign.getIdeaDensity())) {
                        campaign.setIdeaDensity(campaignService.getIdeaDensity(campaign));
                    }
                    return campaign.getIdeaDensity();
                }

                @Override
                public void onClick(AjaxRequestTarget target) {
                    System.out.println("idea density clicked for campaign " + campaign.getId());
                }
            }.setIcon(IconType.tasks).addCssClass(Model.of("details"));
        } else {
            densityPanel = new WebMarkupContainer("ideasDensity");
        }
        EffMetaDataRoleAuthorizationStrategy.authorize(densityPanel, RENDER, UserRole.ADMIN, UserRole.MANAGER, UserRole.SUPER_ADMIN);
        return densityPanel;
    }

    /**
     * @param sortingCriteria
     * @return
     */
    public CampaignListPanel setSortingCriteria(SortingCriteria sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
        return this;

    }

    /**
     * @return
     */
    private WebMarkupContainer newCampaignsContainer() {
        return new WebMarkupContainer("campaignsContainer") {
            private static final long serialVersionUID = -5090615480759122784L;

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
                // add javascript to load tagsinput plugin
                response.render(OnLoadHeaderItem.forScript(String.format(
                        JSTemplates.LOAD_TABLE_SORTER, "campaign-list")));
            }
        };
    }

    private InnovationStatusIndicatorPanel newStageIndicator(Campaign campaign) {
        InnovationStatusIndicatorPanel innovationStatusIndicatorPanel = new InnovationStatusIndicatorPanel("status", Model.of(campaign));
        innovationStatusIndicatorPanel.setVisible(campaign.getCampaignType() != CampaignType.FREE_FORM);
        return innovationStatusIndicatorPanel;
    }

    /**
     * @param campaign
     * @return
     */
    private BookmarkablePageLink<? extends BasePage> newGotoCorrespondingStageLink(
            final Campaign campaign) {
        final BookmarkablePageLink<? extends BasePage> link =
                campaign.getInnovationStatus().getLinkToCorrespondingStage("ideasLink", campaign, loggedInUser);
        link.add(new Label("name", new PropertyModel<String>(campaign, "name")));
        return link;
    }

    private NonCachingImage newProfilePicture(User user) {
        NonCachingImage picture = new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, user));
        picture.setOutputMarkupId(true);
        return picture;
    }

    /**
     * @param user
     * @return
     */
    private AjaxLink<Void> userImageLink(final User user) {
        AjaxLink<Void> link = new AjaxLink<Void>("link") {
            private static final long serialVersionUID = -6633994061963892062L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                // add(new AttributeModifier("class", "active"));
                setResponsePage(UserProfileDetailsPage.class, new PageParameters().set("id", user.getId()));
            }
        };

        link.add(AttributeModifier.append("title", user.getNickName()));
        link.add(newProfilePicture(user));
        return link;
    }

    /**
     * @author ATB
     */
    private final class CampaignProvider extends EntityProvider<Campaign> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends Campaign> iterator(long first, long count) {

            List<Campaign> allTopicCampaigns = new LinkedList<>();
            if (sortingCriteria.equals(SortingCriteria.Date)) {
                allTopicCampaigns = campaignService.getAllCampaignsByDescendingCreationDate(Long.valueOf(first).intValue(),
                        Long.valueOf(count).intValue());
            } else if (sortingCriteria.equals(SortingCriteria.DefinitionPhase)) {
                allTopicCampaigns = campaignService.getAllCampaignsWithInnovationStatusByDescendingCreationDate(
                        Long.valueOf(first).intValue(), Long.valueOf(count).intValue(), InnovationStatus.DEFINITION);
            } else if (sortingCriteria.equals(SortingCriteria.IdeationPhase)) {
                allTopicCampaigns = campaignService.getAllCampaignsWithInnovationStatusByDescendingCreationDate(
                        Long.valueOf(first).intValue(), Long.valueOf(count).intValue(), InnovationStatus.INCEPTION);
            } else if (sortingCriteria.equals(SortingCriteria.SelectionPhase)) {
                allTopicCampaigns = campaignService.getAllCampaignsWithInnovationStatusByDescendingCreationDate(
                        Long.valueOf(first).intValue(), Long.valueOf(count).intValue(), InnovationStatus.PRIORITISATION);
            }

            return allTopicCampaigns.iterator();

        }

        @Override
        public long size() {
            if (sortingCriteria.equals(SortingCriteria.Date)) {
                return campaignService.countAllCampaignsWithInnovationStatus(null);
            } else if (sortingCriteria.equals(SortingCriteria.DefinitionPhase)) {
                return campaignService.countAllCampaignsWithInnovationStatus(InnovationStatus.DEFINITION);
            } else if (sortingCriteria.equals(SortingCriteria.IdeationPhase)) {
                return campaignService.countAllCampaignsWithInnovationStatus(InnovationStatus.INCEPTION);
            } else if (sortingCriteria.equals(SortingCriteria.SelectionPhase)) {
                return campaignService.countAllCampaignsWithInnovationStatus(InnovationStatus.PRIORITISATION);
            }
            return 0;
        }
    }
}
