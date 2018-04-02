package de.atb.socratic.web.inception.idea.details;

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

import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonType;
import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.IdeaActivity;
import de.atb.socratic.model.IdeaType;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.common.comment.CommonCommentListPanel;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.ModalActionButton;
import de.atb.socratic.web.components.NotificationModal;
import de.atb.socratic.web.components.facebook.share.FacebookSharePanel;
import de.atb.socratic.web.components.linkedin.share.LinkedInSharePanel;
import de.atb.socratic.web.components.resource.ChallengePictureResource;
import de.atb.socratic.web.components.resource.IdeaPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.components.resource.header.CommonResourceHeaderPanel;
import de.atb.socratic.web.dashboard.message.CreateMessagePanel;
import de.atb.socratic.web.inception.campaign.CampaignActionsPanel;
import de.atb.socratic.web.inception.campaign.StopCampaignNotificationModal;
import de.atb.socratic.web.inception.idea.IdeaAddEditPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.inception.idea.VotingPanel;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
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
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class IdeaDetailsPage extends BasePage {

    private static final long serialVersionUID = 2839424925522841147L;

    @Inject
    @LoggedInUser
    private User loggedInUser;

    @EJB
    private IdeaService ideaService;

    @EJB
    private UserService userService;

    @EJB
    ActivityService activityService;

    @EJB
    private CampaignService campaignService;

    private Campaign challenge;
    private Long challengeId;
    private Idea idea;
    private Long ideaId;

    private NonCachingImage ideaProfilePicture;
    private VotingPanel votingPanel;

    private CreateMessagePanel messageModelPanel;
    private ModalWindow createMessageModalWindow;

    private final Modal deleteConfirmationModal;
    private NonCachingImage challengeProfilePicture;
    private Modal stopNotificationModal;
    protected Label followersTotalNoLabel;
    protected AjaxLink<Void> addFollowersLink;

    private DataView<User> buildersView;
    private static final int itemsPerPage = 6;
    private BuildersProvider buildersProvider;

    private int pageNumber;

    public IdeaDetailsPage(final PageParameters parameters) {
        super(parameters);

        // load campaign and idea
        loadCampaignAndIdea(parameters);

        // Idea Detail Page Header
        final CommonResourceHeaderPanel<Campaign> headerPanel = new CommonResourceHeaderPanel<Campaign>("commonHeaderPanel", Model.of(challenge)) {
            private static final long serialVersionUID = 7625431390638805241L;

            @Override
            public AjaxLink<Void> newAddFollowersLink() {
                addFollowersLink = new AjaxLink<Void>("addFollowers") {
                    private static final long serialVersionUID = -2532847242859356026L;

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(!loggedInUser.equals(idea.getPostedBy()));
                        setEnabled(!loggedInUser.equals(idea.getPostedBy()));

                        // is loggedInUser Follows given Idea?
                        if (!userService.isUserFollowsGivenIdea(idea, loggedInUser.getId())) {
                            // if loggedInUser is not a follower
                            add(new AttributeModifier("value", new StringResourceModel("follow.button", this, null)
                                    .getString()));
                        } else {
                            add(new AttributeModifier("value", new StringResourceModel("unfollow.button", this, null)
                                    .getString()));
                        }
                    }

                    ;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        // is loggedInUser Follows given Idea?
                        if (!userService.isUserFollowsGivenIdea(idea, loggedInUser.getId())) {
                            // follow idea
                            loggedInUser = userService.addIdeaToFollowedIdeasList(idea, loggedInUser.getId());
                        } else {
                            // Unfollow idea
                            loggedInUser = userService.removeIdeaFromFollowedIdeasList(idea, loggedInUser.getId());
                        }
                        target.add(this);
                        // Update followersTotalNoLabel
                        followersTotalNoLabel.setDefaultModelObject(userService.countAllUsersByGivenFollowedIdea(idea));
                        target.add(followersTotalNoLabel);
                    }
                };
                addFollowersLink.setOutputMarkupId(true);
                return addFollowersLink;
            }

            @Override
            public CampaignActionsPanel getCampaignActionsPanel() {
                CampaignActionsPanel actions = new CampaignActionsPanel("actions", new Model<Campaign>(challenge), InnovationStatus.INCEPTION) {
                    private static final long serialVersionUID = 2703501332693542319L;

                    @Override
                    public void stopClicked(AjaxRequestTarget target, Campaign campaignToHandle) {

                    }

                    @Override
                    public void startClicked(AjaxRequestTarget target, Campaign campaignToHandle) {

                    }
                };

                actions.setVisible(false);
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
                    }

                };
                stopNotificationModal.setVisible(false);
                return stopNotificationModal;
            }

            @Override
            public FacebookSharePanel<Idea> addFacebookSharePanel() {
                // facebook share button panel
                FacebookSharePanel<Idea> facebookSharePanel = new FacebookSharePanel<Idea>("facebookShare", Model.of(idea)) {
                    private static final long serialVersionUID = 5783650552807954153L;

                    @Override
                    protected String providePageLink() {
                        int pagenumber = pageNumber;
                        String url = RequestCycle.get().getUrlRenderer()
                                .renderFullUrl(Url.parse(RequestCycle.get().urlFor(IdeaDetailsPage.class, parameters).toString()))
                                + "?" + pagenumber;
                        return url;
                    }
                };
                return facebookSharePanel;
            }

            @Override
            public LinkedInSharePanel<Idea> addLinkedInSharePanel() {
                // linkedIn share button panel
                LinkedInSharePanel<Idea> linkedInSharePanel = new LinkedInSharePanel<Idea>("linkedInShare", Model.of(idea), feedbackPanel) {
                    private static final long serialVersionUID = 5783650552807954153L;

                    @Override
                    protected String providePageLink() {
                        int pagenumber = pageNumber;
                        String url = RequestCycle.get().getUrlRenderer()
                                .renderFullUrl(Url.parse(RequestCycle.get().urlFor(IdeaDetailsPage.class, parameters).toString()))
                                + "?" + pagenumber;
                        return url;
                    }
                };

                return linkedInSharePanel;
            }
        };

        add(headerPanel);

        // add follower label locally
        this.followersTotalNoLabel = new Label("followersTotalNoLabel", userService.countAllUsersByGivenFollowedIdea(idea));
        this.followersTotalNoLabel.setOutputMarkupId(true);
        add(this.followersTotalNoLabel);

        add(new Label("titleOfIdea", new PropertyModel<>(idea, "shortText")));
        add(new Label("elevatorPitchOfIdea", new PropertyModel<>(idea, "elevatorPitch")));
        add(new Label("descriptionOfIdea", new PropertyModel<>(idea, "description")));
        add(new Label("beneficiaries", new PropertyModel<>(idea, "beneficiaries")));

        // optional fields, hide them if not provided by idea leader        
        add(newLabelForOptionalFields("valueForBeneficiariesHeader", "idea.definition.label.value",
                idea.getValueForBeneficiaries() != null && !idea.getValueForBeneficiaries().isEmpty()));
        add(new Label("valueForBeneficiaries", new PropertyModel<>(idea, "valueForBeneficiaries")));

        add(newLabelForOptionalFields("impactStakeholdersHeader", "idea.definition.label.impact",
                idea.getImpactStakeholders() != null && !idea.getImpactStakeholders().isEmpty()));
        add(new Label("impactStakeholders", new PropertyModel<>(idea, "impactStakeholders")));

        add(newLabelForOptionalFields("resourcesForIdeaImplementationHeader", "idea.definition.label.resources",
                idea.getResourcesForIdeaImplementation() != null && !idea.getResourcesForIdeaImplementation().isEmpty()));
        add(new Label("resourcesForIdeaImplementation", new PropertyModel<>(idea, "resourcesForIdeaImplementation")));

        add(newLabelForOptionalFields("implementationPlanHeader", "idea.definition.label.implementation",
                idea.getImplementationPlan() != null && !idea.getImplementationPlan().isEmpty()));
        add(new Label("implementationPlan", new PropertyModel<>(idea, "implementationPlan")));

        add(newLabelForOptionalFields("locationHeader", "idea.definition.label.location", idea.getLocation() != null
                && !idea.getLocation().isEmpty()));
        add(new Label("location", new PropertyModel<>(idea, "location")));

        add(newLabelForOptionalFields("reasonForBringingIdeaForwardHeader", "idea.definition.label.rightperson",
                idea.getReasonForBringingIdeaForward() != null && !idea.getReasonForBringingIdeaForward().isEmpty()));
        add(new Label("reasonForBringingIdeaForward", new PropertyModel<>(idea, "reasonForBringingIdeaForward")));

        add(newLabelForOptionalFields("relatedInnovationsHeader", "idea.definition.label.innovation",
                idea.getRelatedInnovations() != null && !idea.getRelatedInnovations().isEmpty()));
        add(new Label("relatedInnovations", new PropertyModel<>(idea, "relatedInnovations")));

        add(newIdeaProfilePicturePreview(idea));

        final WebMarkupContainer ideaBuildersContainer = new WebMarkupContainer("ideaBuildersContainer");
        ideaBuildersContainer.setOutputMarkupId(true);
        add(ideaBuildersContainer);

        buildersProvider = new BuildersProvider();

        buildersView = new DataView<User>("builders", buildersProvider, itemsPerPage) {
            private static final long serialVersionUID = -8481637303939977007L;

            @Override
            protected void populateItem(Item<User> item) {
                AjaxLink<Void> link = userImageLink("userProfileDetailLink", item.getModelObject());
                link.add(new NonCachingImage("builders_img", ProfilePictureResource.get(PictureType.THUMBNAIL,
                        item.getModelObject())));
                item.add(link);
                item.add(new Label("builders_name", item.getModelObject().getNickName()));
            }
        };
        buildersView.setOutputMarkupId(true);
        ideaBuildersContainer.add(buildersView);

        add(new BootstrapAjaxPagingNavigator("pagination", buildersView) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(buildersView.getPageCount() > 1);
            }
        });

        votingPanel = new VotingPanel<Idea>("thumbsUpVoting", Model.of(idea)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void voteUpOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting up");

                // once Idea is liked, create an activity related to it.
                activityService.create(IdeaActivity.ofLiked(idea, loggedInUser));
                // notify about idea comments and likes
                ideaService.notifyIdeaFollowersAboutIdeaCommentsAndLikes(idea);
            }

            @Override
            public void voteDownOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting down");
            }
        };
        add(votingPanel);

        // ideatypes
        final List<IdeaType> ideaTypes = idea.getIdeaType();

        // non repeating set of ideaTypes
        Set<String> ideaTypesStrings = new HashSet<>();
        for (IdeaType type : ideaTypes) {

            if (type.equals(IdeaType.Others)) {
                // if idea type text is not null then use it...
                if (idea.getIdeaTypeText() != null) {
                    ideaTypesStrings.add(idea.getIdeaTypeText());
                } else {
                    ideaTypesStrings.add(type.getType());
                }
            } else {
                ideaTypesStrings.add(type.getType());
            }
        }
        add(new MultiLineLabel("ideaTypes", ideaTypesStrings.toString().replace("[", "").replace("]", "").replace(", ", "\n")));

        add(new BookmarkablePageLink<IdeaAddEditPage>("editIdeaLink", IdeaAddEditPage.class, new PageParameters().set("id",
                idea.getId()).set("campaignId", idea.getCampaign().getId())) {
            private static final long serialVersionUID = 234728369857820635L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(idea.isEditableBy(loggedInUser) && challenge.getIdeationActive() != null
                        && challenge.getIdeationActive());
            }
        });

        // add confirmation modal for deleting campaigns
        add(deleteConfirmationModal = newDeleteConfirmationModal());

        add(new AjaxLink<Void>("deleteIdeaLink") {
            private static final long serialVersionUID = 280048510416695622L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(loggedInUser.equals(idea.getPostedBy()) && challenge.getIdeationActive() != null
                        && challenge.getIdeationActive());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                deleteConfirmationModal.appendShowDialogJavaScript(target);
            }
        });

        // idea owner
        AjaxLink<Void> link = userImageLink("link", idea.getPostedBy());
        link.add(new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, idea.getPostedBy())));
        add(link);
        add(new Label("nickName", new PropertyModel<String>(idea, "postedBy.nickName")));

        // send message button
        add(createMessageButton(idea.getPostedBy()));
        add(createMessageModelWindow());

        // keywords
        add(newIdeaKeywordsPanel(idea));

        // skills
        add(newIdeaSkillsPanel(idea));

        // commentsPanel
        final WebMarkupContainer commentsContainer = new WebMarkupContainer("commentsContainer");
        add(commentsContainer.setOutputMarkupId(true));

        CommonCommentListPanel<Idea> commentsPanel = new CommonCommentListPanel<Idea>(
                "commentsPanel",
                Model.of(idea),
                (challenge.getIdeationActive() != null && challenge.getIdeationActive())) {
            private static final long serialVersionUID = -2329905767924123369L;

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
                ideaService.removeComment(this.getModelObject().getId(), comment);
            }

            @Override
            protected void onCommentListChanged(AjaxRequestTarget target) {
                // Update AjaxLink
                addFollowersLink.configure();
                target.add(addFollowersLink);

                // Update followersTotalNoLabel
                followersTotalNoLabel.setDefaultModelObject(userService.countAllUsersByGivenFollowedIdea(idea));
                target.add(followersTotalNoLabel);
                target.add(headerPanel);

                target.add(ideaBuildersContainer);
            }
        };

        commentsContainer.add(commentsPanel);
        commentsContainer.add(commentsPanel.getCommentsNumberTopOnTheButton());
        add(commentsPanel.getCommentsNumberLeftSideOfTheButton());
        // attachments
        add(newAttachtmentsList(idea));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        pageNumber = getPage().getPageId();
    }

    private Label newLabelForOptionalFields(String wicketId, String resourceModelId, final boolean isVisible) {
        return new Label(wicketId, new StringResourceModel(resourceModelId, this, null)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setOutputMarkupId(true);
                setVisible(isVisible);
            }
        };
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "challengesTab");
    }

    protected NonCachingImage newChallengeProfilePicturePreview(Campaign challenge) {
        challengeProfilePicture = new NonCachingImage("challengeProfilePicturePreview", ChallengePictureResource.get(
                PictureType.PROFILE, challenge));
        challengeProfilePicture.setOutputMarkupId(true);
        return challengeProfilePicture;
    }

    /**
     * @return
     */
    private NotificationModal newDeleteConfirmationModal() {
        final NotificationModal notificationModal = new NotificationModal("deleteConfirmationModal", new StringResourceModel(
                "delete.confirmation.modal.header", this, null), new StringResourceModel("delete.confirmation.modal.message", this,
                null), false);

        notificationModal.addButton(new ModalActionButton(notificationModal, ButtonType.Primary, new StringResourceModel(
                "delete.confirmation.modal.submit.text", this, null), true) {
            private static final long serialVersionUID = 8428059415301413268L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                // confirmed --> delete
                campaignService.removeIdea(challengeId, ideaId);
                // close modal
                notificationModal.appendCloseDialogJavaScript(target);
                // redirect to ideas list page
                setResponsePage(IdeasPage.class, new PageParameters().set("id", challenge.getId()));
            }
        });
        notificationModal.addButton(new ModalActionButton(notificationModal, ButtonType.Default, new StringResourceModel(
                "delete.confirmation.modal.cancel.text", this, null), true) {
            private static final long serialVersionUID = 6853909160603145725L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                // Cancel clicked --> do nothing, close modal
                notificationModal.appendCloseDialogJavaScript(target);
            }
        });
        return notificationModal;
    }

    private NonCachingImage newIdeaProfilePicturePreview(Idea idea) {
        ideaProfilePicture = new NonCachingImage("profilePicturePreview", IdeaPictureResource.get(PictureType.PROFILE, idea));
        ideaProfilePicture.setOutputMarkupId(true);
        return ideaProfilePicture;
    }

    private ListView<FileInfo> newAttachtmentsList(final Idea idea) {
        return new ListView<FileInfo>("attachments", new ArrayList<>(idea.getAttachments())) {
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

    private AjaxLink<Void> newShowCommentsLink(final String markUpId, final String commentsDivId) {
        AjaxLink<Void> showCommentsLink = new AjaxLink<Void>(markUpId) {
            private static final long serialVersionUID = 202363713959040288L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                // jump
                target.appendJavaScript("location.hash = \"#" + commentsDivId + "\";");
                target.appendJavaScript(String.format(JSTemplates.TOGGLE_COLLAPSE, "#" + commentsDivId));
            }
        };
        // don't add <em> tags when setting to disabled
        showCommentsLink.setBeforeDisabledLink("");
        showCommentsLink.setAfterDisabledLink("");
        return showCommentsLink;
    }

    private WebMarkupContainer newIdeaKeywordsPanel(final Idea idea) {
        final WebMarkupContainer tagsContainer = new WebMarkupContainer("keywordsContainer") {
            private static final long serialVersionUID = 7869210537650985756L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!idea.getKeywords().isEmpty());
            }
        };
        tagsContainer.setOutputMarkupPlaceholderTag(true);
        tagsContainer.add(new AttributeModifier("title", new StringResourceModel("keywords.title.text", this, null)));

        tagsContainer.add(new ListView<Tag>("keywords", new ListModel<>(idea.getKeywords())) {
            private static final long serialVersionUID = -707257134087075019L;

            @Override
            protected void populateItem(ListItem<Tag> item) {
                item.add(new Label("keyword", item.getModelObject().getTag()));
            }
        });
        return tagsContainer;
    }

    private WebMarkupContainer newIdeaSkillsPanel(final Idea idea) {
        final WebMarkupContainer tagsContainer = new WebMarkupContainer("skillsContainer") {
            private static final long serialVersionUID = 7869210537650985756L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!idea.getKeywords().isEmpty());
            }
        };
        tagsContainer.setOutputMarkupPlaceholderTag(true);
        tagsContainer.add(new AttributeModifier("title", new StringResourceModel("skills.title.text", this, null)));

        tagsContainer.add(new ListView<Tag>("skills", new ListModel<>(idea.getSkills())) {
            private static final long serialVersionUID = -707257134087075019L;

            @Override
            protected void populateItem(ListItem<Tag> item) {
                item.add(new Label("skill", item.getModelObject().getTag()));
            }
        });
        return tagsContainer;
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
                Set<User> recipients = new HashSet<>();

                recipients.add(user);
                messageModelPanel = new CreateMessagePanel(createMessageModalWindow.getContentId(), createMessageModalWindow, recipients) {
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

    private void loadCampaignAndIdea(PageParameters parameters) {
        challengeId = parameters.get("id").toOptionalLong();
        ideaId = parameters.get("ideaId").toOptionalLong();
        if (challengeId != null && ideaId != null) {
            challenge = campaignService.getById(challengeId);
            idea = ideaService.getById(ideaId);
        } else {
            error("Could not find challenge or idea!");
            // show proper error page
        }
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
     * @author ATB
     */
    private final class BuildersProvider extends EntityProvider<User> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends User> iterator(long first, long count) {

            List<User> builders = activityService.getAllContributorsByIdea(idea, (int) first, (int) count);
            return builders.iterator();
        }

        @Override
        public long size() {
            return activityService.countAllContributorsByIdea(idea);
        }
    }
}
