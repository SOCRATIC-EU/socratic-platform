package de.atb.socratic.web.selection;

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

import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.atb.socratic.authorization.strategies.metadata.IAuthorizationCondition;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.util.authorization.UserInScopeCondition;
import de.atb.socratic.util.authorization.UserIsManagerCondition;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.components.FileUploadBehavior;
import de.atb.socratic.web.components.facebook.share.FacebookSharePanel;
import de.atb.socratic.web.components.linkedin.share.LinkedInSharePanel;
import de.atb.socratic.web.components.resource.ChallengePictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.header.CommonResourceHeaderPanel;
import de.atb.socratic.web.dashboard.iLead.challenge.AdminIdeaSelectionPage;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.inception.campaign.CampaignActionsPanel;
import de.atb.socratic.web.inception.campaign.StopCampaignNotificationModal;
import de.atb.socratic.web.inception.idea.IdeaListPanel;
import de.atb.socratic.web.inception.idea.IdeaSortingCriteria;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.learningcenter.LearningCenterIdeasToProjectsPage;
import de.atb.socratic.web.qualifier.FileUploadCache;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.upload.FileUploadInfo;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class SelectionPage extends BasePage {

    /**
     *
     */
    private static final long serialVersionUID = 2297102597885259622L;

    public static final String IDEA_ANCHOR_PREFIX = "_selection_";

    @Inject
    Logger logger;

    @Inject
    @FileUploadCache
    FileUploadInfo fileUploadInfo;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    @EJB
    UserService userService;

    @Inject
    @LoggedInUser
    User loggedInUser;
    // the current campaign
    private final Campaign campaign;
    // Shows number of posted ideas
    private final Label noOfIdeas;
    private final Label noOfIdeasLabel;
    private Long ideaListSize;
    private final IdeaListPanel ideaListPanel;
    private final IAuthorizationCondition userInScopeCondition;
    private final IAuthorizationCondition userIsManagerCondition;
    private Modal stopNotificationModal;
    private NonCachingImage challengeProfilePicture;
    private final DropDownChoice<IdeaSortingCriteria> sortingCriteriaChoice;
    private int pageNumber;

    /**
     * Constructor building the page
     *
     * @param parameters
     */
    public SelectionPage(final PageParameters parameters) {

        super(parameters);

        // add js and css for file upload
        add(new FileUploadBehavior());

        // set the campaign we got from previous page
        try {
            campaign = campaignService.getById(parameters.get("id").toOptionalLong());
        } catch (Exception e) {
            throw new RestartResponseException(ErrorPage.class);
        }

        userInScopeCondition = UserInScopeCondition.get(campaign);
        userIsManagerCondition = UserIsManagerCondition.get(campaign);

        // check if challenge is active? if not calculate noOfDays.
        if (campaign.getInnovationStatus().equals(InnovationStatus.PRIORITISATION)) {
            if (campaign.getSelectionActive() != null && !campaign.getSelectionActive()
                    && campaign.getSelectionStartDate() != null) {
                DateMidnight startDate = new DateTime().toDateMidnight(); //current date
                DateTime endDate = new DateTime(campaign.getSelectionStartDate());
                Days days = Days.daysBetween(startDate, endDate);
                if (days.getDays() <= 0) {
                    Hours hours = Hours.hoursBetween(startDate, endDate);
                    add(new Label("noOfDays", String.format(getString("challenge.selection.deactive.info.hours"), hours.getHours())));
                } else {
                    add(new Label("noOfDays", String.format(getString("challenge.selection.deactive.info.days"), days.getDays())));
                }

            } else {
                LocalDate dueDate = new LocalDate(campaign.getSelectionEndDate());
                int daysLeft = new Period(new LocalDate(), dueDate, PeriodType.days()).getDays();
                add(new Label("noOfDays", daysLeft + " "
                        + new StringResourceModel("challenge.selection.active.info", this, null).getString() + " "
                        + new StringResourceModel(campaign.getInnovationStatus().getMessageKey(), this, null).getString()));
            }
        } else {
            add(new Label("noOfDays", new StringResourceModel(
                    "challenge.selection.finished.info", this, null)));
        }

        // Challenge's Ideas Selection Header
        final CommonResourceHeaderPanel<Campaign> headerPanel = new CommonResourceHeaderPanel<Campaign>("commonHeaderPanel",
                Model.of(campaign)) {
            private static final long serialVersionUID = 1L;

            @Override
            public AjaxLink<Void> newAddFollowersLink() {
                AjaxLink<Void> addFollowersLink = new AjaxLink<Void>("addFollowers") {
                    private static final long serialVersionUID = -7267198155746140167L;

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(loggedInUser != null && !loggedInUser.equals(campaign.getCreatedBy()));
                        setEnabled(loggedInUser != null && !loggedInUser.equals(campaign.getCreatedBy()));
                        if (loggedInUser != null) {
                            if (!userService.isUserFollowsGivenChallenge(campaign, loggedInUser.getId())) {
                                // if loggedInUser is not a follower
                                this.add(new AttributeModifier("value", new StringResourceModel("follow.button", this, null)
                                        .getString()));
                            } else {
                                this.add(new AttributeModifier("value", new StringResourceModel("unfollow.button", this, null)
                                        .getString()));
                            }
                        }
                    }

                    ;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        // if loggedInUsr is not a follower
                        if (loggedInUser != null) {
                            if (!userService.isUserFollowsGivenChallenge(campaign, loggedInUser.getId())) {
                                // follow challenge
                                loggedInUser = userService.addChallengeToFollowedChallegesList(campaign, loggedInUser.getId());
                            } else {
                                // Unfollow challenge
                                loggedInUser = userService.removeChallengeFromFollowedChallegesList(campaign, loggedInUser.getId());
                            }
                        }
                        target.add(this);
                    }
                };
                addFollowersLink.setOutputMarkupId(true);
                return addFollowersLink;
            }

            @Override
            public CampaignActionsPanel getCampaignActionsPanel() {
                CampaignActionsPanel actions = new CampaignActionsPanel("actions", new Model<Campaign>(campaign), InnovationStatus.PRIORITISATION) {
                    private static final long serialVersionUID = 2703501332693542319L;

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(loggedInUser != null && campaign.isEditableBy(loggedInUser));
                    }

                    @Override
                    public void stopClicked(AjaxRequestTarget target, Campaign campaignToHandle) {
                        stopNotificationModal.appendShowDialogJavaScript(target);
                    }

                    @Override
                    public void startClicked(AjaxRequestTarget target, Campaign campaignToHandle) {
                        campaignService.startSelectionPhase(campaignToHandle);
                        if (campaign.getInnovationStatus().equals(InnovationStatus.INCEPTION) && campaign.getIdeationActive()) {
                            setResponsePage(IdeasPage.class, forCampaign(campaign));
                        } else {
                            setResponsePage(SelectionPage.class, forCampaign(campaign));
                        }
                    }
                };
                return actions;
            }

            @Override
            public Modal getStopNotificationModal() {
                stopNotificationModal = new StopCampaignNotificationModal("stopNotificationModal", new StringResourceModel(
                        "stop.notification.modal.header", this, null), new StringResourceModel("stop.notification.modal.message",
                        this, null), false) {
                    private static final long serialVersionUID = 2096179879061520451L;

                    @Override
                    public void stopCampaignClicked(AjaxRequestTarget target) {
                        stopNotificationModal.appendCloseDialogJavaScript(target);
                        campaignService.stopSelectionPhase(campaign);
                        setResponsePage(AdminIdeaSelectionPage.class, forCampaign(campaign));
                    }
                };
                return stopNotificationModal;
            }

            @Override
            public FacebookSharePanel<Campaign> addFacebookSharePanel() {
                // facebook share button panel
                FacebookSharePanel<Campaign> facebookSharePanel = new FacebookSharePanel<Campaign>("facebookShare", Model.of(campaign)) {
                    private static final long serialVersionUID = 5783650552807954153L;

                    @Override
                    protected String providePageLink() {
                        int pagenumber = pageNumber;
                        String url = RequestCycle.get()
                                .getUrlRenderer()
                                .renderFullUrl(Url.parse(
                                        RequestCycle.get().urlFor(SelectionPage.class, parameters).toString())) + "?" + pagenumber;
                        return url;
                    }
                };

                return facebookSharePanel;
            }

            @Override
            public LinkedInSharePanel<Campaign> addLinkedInSharePanel() {
                // linkedIn share button panel
                LinkedInSharePanel<Campaign> linkedInSharePanel = new LinkedInSharePanel<Campaign>("linkedInShare", Model.of(campaign), feedbackPanel) {
                    private static final long serialVersionUID = 5783650552807954153L;

                    @Override
                    protected String providePageLink() {
                        int pagenumber = pageNumber;
                        String url = RequestCycle.get()
                                .getUrlRenderer()
                                .renderFullUrl(Url.parse(
                                        RequestCycle.get().urlFor(SelectionPage.class, parameters).toString())) + "?" + pagenumber;
                        return url;
                    }
                };

                return linkedInSharePanel;
            }
        };

        add(headerPanel);

        // link to definition
        add(new BookmarkablePageLink<ChallengeDefinitionPage>("linkToDefinition", ChallengeDefinitionPage.class, forCampaign(campaign)));
        // selection guidelines
        add(new BookmarkablePageLink<LearningCenterIdeasToProjectsPage>("selectionGuidelines", LearningCenterIdeasToProjectsPage.class));

        // add panel with list of existing ideas
        ideaListPanel = new IdeaListPanel("ideaList", Model.of(campaign), feedbackPanel, false, IdeaSelectionDetailsPage.class) {
            private static final long serialVersionUID = 8513797732098055780L;

            @Override
            protected void onAfterDelete(AjaxRequestTarget target) {
                // update the # of existing ideas
                updateNoOfIdeasAndButtons(target);
            }

            @Override
            public void voteUpOnClick(AjaxRequestTarget target, Component component) {
                ideaListPanel.add(new AttributeModifier("class", "tab-pane active"));
                target.add(ideaListPanel);
            }

            @Override
            public void voteDownOnClick(AjaxRequestTarget target, Component component) {
                ideaListPanel.add(new AttributeModifier("class", "tab-pane active"));
                target.add(ideaListPanel);
            }

            @Override
            protected IdeaVotingPanel newIdeaVotingPanel(String id, MarkupContainer item, Idea idea) {
                return new IdeaVotingPanel(id, Model.of(idea), true) {
                    private static final long serialVersionUID = 7879132965391367302L;

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                    }

                    @Override
                    protected void onVotingPerformed(AjaxRequestTarget target) {
                    }
                };
            }
        };
        add(ideaListPanel.setOutputMarkupId(true));

        // display # of ideas in list
        ideaListSize = ideaListPanel.getIdeaListSize();
        noOfIdeas = new Label("noOfIdeas", new PropertyModel<Integer>(this, "ideaListSize"));
        add(noOfIdeas.setOutputMarkupId(true));

        //add singular/plural label
        noOfIdeasLabel = new Label("noOfIdeasLabel", Model.of(ideaListSize)) {
            private static final long serialVersionUID = -4480384528388854127L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (ideaListSize == 1) {
                    this.setDefaultModelObject("Idea");
                } else {
                    this.setDefaultModelObject("Ideas");
                }
            }
        };
        noOfIdeasLabel.setOutputMarkupId(true);
        add(noOfIdeasLabel);

        // Add sorting form
        Form<Void> sortingCriteriaForm = new Form<Void>("sortingCriteriaForm");
        add(sortingCriteriaForm);
        sortingCriteriaForm.add(sortingCriteriaChoice = newSortingCriteriaDropDownChoice());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        pageNumber = getPage().getPageId();
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "challengesTab");
    }

    protected NonCachingImage newChallengeProfilePicturePreview(Campaign challenge) {
        challengeProfilePicture = new NonCachingImage("profilePicturePreview", ChallengePictureResource.get(PictureType.PROFILE, challenge));
        challengeProfilePicture.setOutputMarkupId(true);
        return challengeProfilePicture;
    }

    /**
     * @return
     */
    private DropDownChoice<IdeaSortingCriteria> newSortingCriteriaDropDownChoice() {
        List<IdeaSortingCriteria> listOfSorrtingCriteria = Arrays.asList(IdeaSortingCriteria.values());
        final DropDownChoice<IdeaSortingCriteria> sortingCriteriaChoice = new DropDownChoice<IdeaSortingCriteria>(
                "sortingCriteriaChoice", new Model<IdeaSortingCriteria>(), listOfSorrtingCriteria);

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = 8166683739237783452L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // reset any search term we may have entered before
                target.add(ideaListPanel.setSortingCriteria(sortingCriteriaChoice.getModelObject()));
            }
        });
        return sortingCriteriaChoice;
    }

    private static PageParameters forCampaign(Campaign campaign) {
        return new PageParameters().set("id", campaign.getId());
    }

    /**
     * @param target
     */
    private void updateNoOfIdeasAndButtons(AjaxRequestTarget target) {
        // update the # of existing ideas
        ideaListSize = ideaListPanel.getIdeaListSize();
        target.add(noOfIdeasLabel);
        target.add(noOfIdeas);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.Component#onBeforeRender()
     */
    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.eff.web.BasePage#getPageTitleModel()
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }

}
