package de.atb.socratic.web.definition.challenge;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.ChallengeActivity;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.UNGoalType;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.common.comment.CommonCommentListPanel;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.facebook.share.FacebookSharePanel;
import de.atb.socratic.web.components.linkedin.share.LinkedInSharePanel;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.components.resource.header.CommonResourceHeaderPanel;
import de.atb.socratic.web.dashboard.message.CreateMessagePanel;
import de.atb.socratic.web.inception.campaign.CampaignActionsPanel;
import de.atb.socratic.web.inception.campaign.StopCampaignNotificationModal;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.inception.idea.VotingPanel;
import de.atb.socratic.web.learningcenter.LearningCenterDefiningChallengePage;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.security.login.LoginPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
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
public class ChallengeDefinitionPage extends BasePage {

    private static final long serialVersionUID = 7378255719158818040L;

    @EJB
    CampaignService campaignService;

    @EJB
    UserService userService;

    @EJB
    ActivityService activityService;

    private Campaign challenge;

    @Inject
    @LoggedInUser
    User loggedInUser;

    private VotingPanel votingPanel;

    private Label followersTotalNoLabel;
    private Modal stopNotificationModal;
    private AjaxLink<Void> addFollowersLink;
    private DataView<User> contributorsView;
    // container holding the list of contributors
    private final WebMarkupContainer contributorsContainer;

    private static final int itemsPerPage = 4;

    private CreateMessagePanel messageModelPanel;
    private ModalWindow createMessageModalWindow;

    private ContribuotrsProvider contribuotrsProvider;

    private int pageNumber;

    public ChallengeDefinitionPage(final PageParameters parameters) {
        super(parameters);

        final Long challengeId = parameters.get("id").toOptionalLong();
        if (challengeId != null) {
            challenge = campaignService.getById(parameters.get("id").toOptionalLong());
        } else {
            challenge = null;
        }


        // challengeDefinition Header
        final CommonResourceHeaderPanel<Campaign> headerPanel = new CommonResourceHeaderPanel<Campaign>("commonHeaderPanel",
                Model.of(challenge)) {
            private static final long serialVersionUID = 4494582353460389258L;

            @Override
            public CampaignActionsPanel getCampaignActionsPanel() {
                CampaignActionsPanel actions = new CampaignActionsPanel("actions", new Model<Campaign>(challenge),
                        InnovationStatus.DEFINITION) {
                    private static final long serialVersionUID = 2703501332693542319L;

                    @Override
                    public void stopClicked(AjaxRequestTarget target, Campaign campaignToHandle) {
                        stopNotificationModal.appendShowDialogJavaScript(target);
                    }

                    @Override
                    public void startClicked(AjaxRequestTarget target, Campaign campaignToHandle) {
                        campaignService.startDefinitionPhase(challenge);
                        setResponsePage(ChallengeDefinitionPage.class, forCampaign(challenge));
                    }
                };
                actions.setVisible(loggedInUser != null && challenge.isEditableBy(loggedInUser));
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
                        campaignService.stopDefinitionPhase(challenge);
                        setResponsePage(IdeasPage.class, forCampaign(challenge));
                    }
                };
                return stopNotificationModal;
            }

            @Override
            public AjaxLink<Void> newAddFollowersLink() {
                addFollowersLink = new AjaxLink<Void>("addFollowers") {
                    private static final long serialVersionUID = 8719961604750452803L;

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(loggedInUser != null && !loggedInUser.equals(challenge.getCreatedBy()));
                        setEnabled(loggedInUser != null && !loggedInUser.equals(challenge.getCreatedBy()));

                        if (loggedInUser != null) {
                            if (!userService.isUserFollowsGivenChallenge(challenge, loggedInUser.getId())) {
                                // if loggedInUser is not a follower
                                add(new AttributeModifier(
                                        "value",
                                        new StringResourceModel("follow.button", this, null).getString()));
                            } else {
                                add(new AttributeModifier(
                                        "value",
                                        new StringResourceModel("unfollow.button", this, null).getString()));
                            }
                        }
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        if (loggedInUser != null) {
                            // if loggedInUsr is not a follower
                            if (!userService.isUserFollowsGivenChallenge(challenge, loggedInUser.getId())) {
                                // follow challenge
                                loggedInUser = userService.addChallengeToFollowedChallegesList(challenge, loggedInUser.getId());
                            } else {
                                // Unfollow challenge
                                loggedInUser = userService.removeChallengeFromFollowedChallegesList(challenge, loggedInUser.getId());
                            }
                            target.add(this);
                            // Update followersTotalNoLabel
                            followersTotalNoLabel.setDefaultModelObject(userService
                                    .countAllUsersByGivenFollowedChallenge(challenge));
                            target.add(followersTotalNoLabel);
                        }
                    }
                };
                addFollowersLink.setOutputMarkupId(true);
                return addFollowersLink;
            }

            @Override
            public FacebookSharePanel<Campaign> addFacebookSharePanel() {
                // facebook share button panel
                FacebookSharePanel<Campaign> facebookSharePanel = new FacebookSharePanel<Campaign>("facebookShare", Model.of(challenge)) {
                    private static final long serialVersionUID = 5783650552807954153L;

                    @Override
                    protected String providePageLink() {
                        int pagenumber = pageNumber;
                        String url = RequestCycle.get()
                                .getUrlRenderer()
                                .renderFullUrl(Url.parse(
                                        RequestCycle.get().urlFor(ChallengeDefinitionPage.class, parameters).toString())) + "?" + pagenumber;

                        return url;
                    }
                };

                return facebookSharePanel;
            }

            @Override
            public LinkedInSharePanel<Campaign> addLinkedInSharePanel() {
                // linkedIn share button panel
                LinkedInSharePanel<Campaign> linkedInSharePanel = new LinkedInSharePanel<Campaign>("linkedInShare", Model.of(challenge), feedbackPanel) {
                    private static final long serialVersionUID = 5783650552807954153L;

                    @Override
                    protected String providePageLink() {
                        int pagenumber = pageNumber;
                        String url = RequestCycle.get()
                                .getUrlRenderer()
                                .renderFullUrl(Url.parse(
                                        RequestCycle.get().urlFor(ChallengeDefinitionPage.class, parameters).toString())) + "?" + pagenumber;

                        return url;
                    }
                };

                return linkedInSharePanel;
            }

        };

        add(headerPanel);

        // check if challenge is active? if not calculate noOfDays.
        if (challenge.getInnovationStatus().equals(InnovationStatus.DEFINITION)) {
            if (challenge.getDefinitionActive() != null && !challenge.getDefinitionActive()
                    && challenge.getChallengeOpenForDiscussionStartDate() != null) {
                DateMidnight startDate = new DateTime().toDateMidnight(); // current date
                DateTime endDate = new DateTime(challenge.getChallengeOpenForDiscussionStartDate());
                Days days = Days.daysBetween(startDate, endDate);
                if (days.getDays() <= 0) {
                    Hours hours = Hours.hoursBetween(startDate, endDate);
                    add(new Label("noOfDays", String.format(getString("challenge.definition.deactive.info.hours"),
                            hours.getHours())));
                } else {
                    add(new Label("noOfDays", String.format(getString("challenge.definition.deactive.info.days"),
                            days.getDays())));
                }

            } else {
                LocalDate dueDate = new LocalDate(challenge.getChallengeOpenForDiscussionEndDate());
                int daysLeft = new Period(new LocalDate(), dueDate, PeriodType.days()).getDays();
                add(new Label("noOfDays", daysLeft + " "
                        + new StringResourceModel("challenge.definition.active.info", this, null).getString() + " "
                        + new StringResourceModel(challenge.getInnovationStatus().getMessageKey(), this, null).getString()));
            }
        } else {
            add(new Label("noOfDays", new StringResourceModel("challenge.definition.finished.info", this, null)));
        }

        votingPanel = new VotingPanel<Campaign>("thumbsUpVoting", Model.of(challenge)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void voteUpOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting up");

                // once challenge is liked, create an activity related to it.
                activityService.create(ChallengeActivity.ofLiked(challenge, loggedInUser));

                // notify about campaign comments and likes
                campaignService.notifyCampaignFollowersAboutCampaignCommentsAndLikes(challenge);
            }

            @Override
            public void voteDownOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting down");
            }
        };
        add(votingPanel);

        // Definition guidelines
        add(new BookmarkablePageLink<LearningCenterDefiningChallengePage>("definitionGuidelines",
                LearningCenterDefiningChallengePage.class));

        // descriptions
        add(new Label("elevatorPitch", new PropertyModel<String>(challenge, "elevatorPitch")));
        add(new Label("socialChallenge", new PropertyModel<String>(challenge, "socialChallenge")));
        add(new Label("beneficiaries", new PropertyModel<String>(challenge, "beneficiaries")));
        add(new Label("potentialImpact", new PropertyModel<String>(challenge, "potentialImpact")));
        add(new Label("levelOfSupport", new PropertyModel<String>(challenge, "levelOfSupport")));
        add(new Label("ideasProposed", new PropertyModel<String>(challenge, "ideasProposed")));

        // ungoals
        List<UNGoalType> goals = challenge.getuNGoals();
        List<String> goalsStrings = new ArrayList<>();
        for (UNGoalType goal : goals) {
            goalsStrings.add(goal.getGoal());
        }
        add(new MultiLineLabel("unGoals", goalsStrings.toString().replace("[", "").replace("]", "").replace(", ", "\n")));

        // challenge owner
        AjaxLink<Void> link = userImageLink("link", challenge.getCreatedBy());
        link.add(new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, challenge.getCreatedBy())));
        add(link);
        add(new Label("nickName", new PropertyModel<String>(challenge, "createdBy.nickName")));

        // send message button
        add(createMessageButton(challenge.getCreatedBy()));
        add(createMessageModelWindow());

        // contributors: add repeating view
        contributorsContainer = new WebMarkupContainer("contributorsContainer");
        add(contributorsContainer.setOutputMarkupId(true));

        contribuotrsProvider = new ContribuotrsProvider();

        contributorsView = new DataView<User>("contributors", contribuotrsProvider, itemsPerPage) {
            private static final long serialVersionUID = -1540809427150384449L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }

            @Override
            protected void populateItem(Item<User> item) {
                AjaxLink<Void> link = userImageLink("userProfileDetailLink", item.getModelObject());
                link.add(new NonCachingImage("con_img",
                        ProfilePictureResource.get(PictureType.THUMBNAIL, item.getModelObject())));
                item.add(link);
                item.add(new Label("con_name", item.getModelObject().getNickName()));

            }
        };
        contributorsContainer.add(contributorsView);

        // attachments
        add(newAttachtmentsList(challenge));

        add(new BootstrapAjaxPagingNavigator("pagination", contributorsView) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(contributorsView.getPageCount() > 1);
            }
        });

        // add follower label locally
        this.followersTotalNoLabel = new Label("followersTotalNoLabel",
                userService.countAllUsersByGivenFollowedChallenge(challenge));
        this.followersTotalNoLabel.setOutputMarkupId(true);
        add(this.followersTotalNoLabel);

        // comments
        final WebMarkupContainer commentsContainer = new WebMarkupContainer("commentsContainer");
        add(new WebMarkupContainer("noDiscussion") {
            private static final long serialVersionUID = -5731175990844263366L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (commentsContainer.isVisible()) {
                    setVisible(false);
                } else {
                    setVisible(true);
                }
            }
        }.add(new Label("noDiscussionLabel", "This Challenge is not open for discussion!")));

        commentsContainer.setOutputMarkupId(true);
        add(commentsContainer);

        final CommonCommentListPanel<Campaign> commentsPanel = new CommonCommentListPanel<Campaign>(
                "commentsPanel",
                Model.of(challenge),
                (loggedInUser != null && challenge.getDefinitionActive() != null && challenge.getDefinitionActive())) {
            private static final long serialVersionUID = -3657798483746156593L;

            @Override
            protected int getTotalCommentSize() {
                return this.getModelObject().getComments().size();
            }

            @Override
            protected List<Comment> getListOfComments() {
                return this.getModelObject().getComments();
            }

            @Override
            protected void deleteComment(final Comment comment) {
                campaignService.removeComment(this.getModelObject().getId(), comment);
            }

            @Override
            protected void onCommentListChanged(AjaxRequestTarget target) {
                // Update AjaxLink
                addFollowersLink.configure();
                target.add(addFollowersLink);

                // Update followersTotalNoLabel
                followersTotalNoLabel.setDefaultModelObject(userService.countAllUsersByGivenFollowedChallenge(challenge));
                target.add(followersTotalNoLabel);
                target.add(headerPanel);

                // update contributors list view
                target.add(contributorsContainer);
            }
        };
        commentsPanel.setOutputMarkupId(true);
        commentsContainer.add(commentsPanel);
        commentsContainer.add(commentsPanel.getCommentsNumberTopOnTheButton());

        add(commentsPanel.getCommentsNumberLeftSideOfTheButton());

        final AjaxLink<Void> commentsButton = new AjaxLink<Void>("commentsButtonTop") {
            private static final long serialVersionUID = -8208113004095587248L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (loggedInUser == null) {
                    setResponsePage(LoginPage.class);
                } else {
                    // if user is logged in then scroll to comments sections at the bottom of the page.
                    target.appendJavaScript("document.getElementById('" + commentsPanel.getMarkupId() + "').scrollIntoView();");
                }
            }
        };
        commentsButton.setOutputMarkupId(true);
        add(commentsButton);
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

    private ListView<FileInfo> newAttachtmentsList(final Campaign challenge) {
        return new ListView<FileInfo>("attachments", new ArrayList<>(challenge.getAttachments())) {
            private static final long serialVersionUID = 202363713959040288L;

            @Override
            protected void populateItem(ListItem<FileInfo> item) {
                FileInfo file = item.getModelObject();
                item.add(new DownloadLink("fileLink", new Model<>(file.getFile())) {
                    private static final long serialVersionUID = 5621866025141144279L;

                    @Override
                    public void onClick() {
                        super.onClick();
                        File file = getModelObject();
                        IResourceStream resourceStream = new FileResourceStream(new org.apache.wicket.util.file.File(file));
                        getRequestCycle().scheduleRequestHandlerAfterCurrent(
                                new ResourceStreamRequestHandler(resourceStream, file.getName()));
                    }
                }.add(new Label("fileLabel", file.getDisplayName())));
            }
        };
    }

    /**
     * @param
     * @return
     */
    private AjaxLink<Void> createMessageButton(final User user) {
        AjaxLink<Void> ajaxSubmitLink = new AjaxLink<Void>("createMessage") {

            private static final long serialVersionUID = 7675565566981782898L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(createMessageModalWindow);
                if (loggedInUser == null) {
                    setResponsePage(LoginPage.class);
                    return;
                }
                Set<User> recipients = new HashSet<>();

                recipients.add(user);
                messageModelPanel = new CreateMessagePanel(createMessageModalWindow.getContentId(), createMessageModalWindow,
                        recipients) {
                    private static final long serialVersionUID = 1L;
                };

                createMessageModalWindow.setContent(messageModelPanel.setOutputMarkupId(true));
                createMessageModalWindow.show(target);

            }
        };
        return ajaxSubmitLink;
    }

    /**
     * @param
     * @return
     */
    private ModalWindow createMessageModelWindow() {
        createMessageModalWindow = new ModalWindow("createMessageModalWindow") {

            private static final long serialVersionUID = -6118683848343086655L;

            @Override
            public void show(AjaxRequestTarget target) {
                super.show(target);
                target.appendJavaScript(""//
                        + "var thisWindow = Wicket.Window.get();\n"
                        + "if (thisWindow) {\n"
                        + "thisWindow.window.style.width = \"1500px\";\n"
                        + "thisWindow.content.style.height = \"1000px\";\n"
                        + "thisWindow.center();\n" + "}");
                setOutputMarkupId(true);
            }
        };

        // createMessageModalWindow.setTitle("Compose Message");
        createMessageModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {

            private static final long serialVersionUID = -9143847141081283640L;

            @Override
            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                return true;
            }
        }).setOutputMarkupId(true);

        createMessageModalWindow.setResizable(true);
        createMessageModalWindow.setAutoSize(true);
        createMessageModalWindow.setInitialWidth(1400);
        createMessageModalWindow.setInitialHeight(1400);

        return createMessageModalWindow;
    }

    private AjaxLink<Void> newShowCommentsLink(final Campaign challenge, final String commentsDivId) {
        AjaxLink<Void> showCommentsLink = new AjaxLink<Void>("showComments") {
            private static final long serialVersionUID = -1819207994419342476L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                target.appendJavaScript("location.hash = \"#" + commentsDivId + "\";");
                target.appendJavaScript(String.format(JSTemplates.TOGGLE_COLLAPSE, "#" + commentsDivId));
            }
        };
        // don't add <em> tags when setting to disabled
        showCommentsLink.setBeforeDisabledLink("");
        showCommentsLink.setAfterDisabledLink("");
        return showCommentsLink;
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

    private static PageParameters forCampaign(Campaign campaign) {
        return new PageParameters().set("id", campaign.getId());
    }

    /**
     * @author ATB
     */
    private final class ContribuotrsProvider extends EntityProvider<User> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends User> iterator(long first, long count) {

            List<User> contributors = activityService.getAllChallengeContributorsByAscNickNameAndCampaign(challenge,
                    (int) first, (int) count);
            return contributors.iterator();
        }

        @Override
        public long size() {
            return activityService.countAllChallengeContributorsByCampaign(challenge);
        }
    }

}
