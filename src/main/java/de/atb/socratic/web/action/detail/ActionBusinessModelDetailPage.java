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

import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionTeamTool;
import de.atb.socratic.model.BusinessModel;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionBusinessModelService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.common.comment.CommonCommentListPanel;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.inception.idea.VotingPanel;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ActionBusinessModelDetailPage extends BasePage {

    private static final long serialVersionUID = 7378255719158818040L;

    @Inject
    Logger logger;

    @EJB
    ActionBusinessModelService actionBusinessModelService;

    @EJB
    ActivityService activityService;

    @EJB
    ActionService actionService;

    @EJB
    UserService userService;

    private BusinessModel theBusinessModel;
    private Action theAction;

    private VotingPanel votingPanel;

    private DataView<User> contributorsView;
    // container holding the list of contributors
    private final WebMarkupContainer contributorsContainer;

    private static final int itemsPerPage = 3;
    private ContribuotrsProvider contribuotrsProvider;
    private final WebMarkupContainer commentsContainer;
    private final ListView<ActionTeamTool> teamToolsListView;

    @Inject
    @LoggedInUser
    User loggedInUser;

    final CommonActionResourceHeaderPanel<Action> headerPanel;

    public ActionBusinessModelDetailPage(final PageParameters parameters) {
        super(parameters);

        loadActionAndBusinessModel(parameters);

        // challengeDefinition Header
        headerPanel = new CommonActionResourceHeaderPanel<Action>("commonHeaderPanel", Model.of(theAction), feedbackPanel) {
            private static final long serialVersionUID = 4494582353460389258L;

            @Override
            protected void onFollowersUpdate(AjaxRequestTarget target) {
            }
        };
        add(headerPanel);

        add(addToolTipWebMarkupContainer("businessModelHelpText",
                new StringResourceModel("businessModelText.help", this, null), TooltipConfig.Placement.right));

        // valuePropositions
        addBusinessModelField("valuePropositions.header.label.desc", "valuePropositions", "valuePropositionsPicture",
                "img/value_propositions_image.png", theBusinessModel.getValuePropositions() != null);

        // customerSegments
        addBusinessModelField("customerSegments.header.label.desc", "customerSegments", "customerSegmentsPicture",
                "img/customer_segments_image.png", theBusinessModel.getCustomerSegments() != null);

        // customerRelationships
        addBusinessModelField("customerRelationships.header.label.desc", "customerRelationships",
                "customerRelationshipsPicture", "img/customer_relationships_image.png",
                theBusinessModel.getCustomerRelationships() != null);

        // customerRelationships
        addBusinessModelField("channels.header.label.desc", "channels", "channelsPicture", "img/channels_image.png",
                theBusinessModel.getChannels() != null);

        // keyPartners
        addBusinessModelField("keyPartners.header.label.desc", "keyPartners", "keyPartnersPicture",
                "img/key_partners_image.png", theBusinessModel.getKeyPartners() != null);

        // keyActivities
        addBusinessModelField("keyActivities.header.label.desc", "keyActivities", "keyActivitiesPicture",
                "img/key_activities_image.png", theBusinessModel.getKeyActivities() != null);

        // keyResources
        addBusinessModelField("keyResources.header.label.desc", "keyResources", "keyResourcesPicture",
                "img/key_resources_image.png", theBusinessModel.getKeyResources() != null);

        // revenueStream
        addBusinessModelField("revenueStream.header.label.desc", "revenueStream", "revenueStreamPicture",
                "img/revenue_stream_image.png", theBusinessModel.getRevenueStream() != null);

        // revenueStream
        addBusinessModelField("costStructure.header.label.desc", "costStructure", "costStructurePicture",
                "img/cost_structure_image.png", theBusinessModel.getCostStructure() != null);

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

        add(new BootstrapAjaxPagingNavigator("pagination", contributorsView) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(contributorsView.getPageCount() > 1);
            }
        });

        // commentsPanel
        commentsContainer = new WebMarkupContainer("commentsContainer");
        add(commentsContainer.setOutputMarkupId(true));

        CommonCommentListPanel<BusinessModel> commentsPanel = new CommonCommentListPanel<BusinessModel>("commentsPanel",
                Model.of(theBusinessModel), true) {
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
                actionBusinessModelService.removeComment(this.getModelObject().getId(), comment);
            }

            @Override
            protected void onCommentListChanged(AjaxRequestTarget target) {
                target.add(contributorsContainer);
                target.add(commentsContainer);

                // Update AjaxLink
                headerPanel.addFollowersLink.configure();
                target.add(headerPanel.addFollowersLink);
                target.add(headerPanel);
            }
        };

        commentsContainer.add(commentsPanel);
        commentsContainer.add(commentsPanel.getCommentsNumberTopOnTheButton());
        add(commentsPanel.getCommentsNumberLeftSideOfTheButton());

        // Business Model voting
        votingPanel = new VotingPanel<BusinessModel>("thumbsUpVoting", Model.of(theBusinessModel)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void voteUpOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting up");
            }

            @Override
            public void voteDownOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting down");
            }
        };
        add(votingPanel);

        // pop up window
        final WebMarkupContainer wmc = new WebMarkupContainer("popupContainer") {
            /**
             *
             */
            private static final long serialVersionUID = 7936000079422415081L;

            @Override
            protected void onConfigure() {
                super.onConfigure();

                // Display pop up message to Action Leader, do so until he does not set all fields of BM
                boolean allParaSet = (theBusinessModel.getChannels() != null && theBusinessModel.getCostStructure() != null
                        && theBusinessModel.getCustomerRelationships() != null && theBusinessModel.getKeyActivities() != null
                        && theBusinessModel.getKeyPartners() != null && theBusinessModel.getKeyResources() != null && theBusinessModel
                        .getRevenueStream() != null) ? true : false;

                if (loggedInUser.equals(theAction.getPostedBy()) && !allParaSet) {
                    setVisible(true);
                } else {
                    setVisible(false);
                }

            }
        };
        wmc.add(new Label("popupHeader", new StringResourceModel("popup.model.window.header", this, null)));
        wmc.add(new Label("popupPara1", new StringResourceModel("popup.model.window.para1", this, null)));
        wmc.add(new Label("popupPara2", new StringResourceModel("popup.model.window.para2", this, null)));
        wmc.add(new Label("popupList1", new StringResourceModel("popup.model.window.list1", this, null)));
        wmc.add(new Label("popupList2", new StringResourceModel("popup.model.window.list2", this, null)));
        add(wmc);

        // Add team tool list view
        teamToolsListView = headerPanel.newListViewForTeamTools("allTeamTools", "toolDescriptionViewLabel", Model.ofList(theAction.getActionTeamTools()));
        add(teamToolsListView.setOutputMarkupId(true));

    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "actionsTab");
        headerPanel.activateCurrentActionTab(response, "businessModelTab");
    }

    /**
     * @param wicketFieldHeaderTextLabelId, wicket field header id and resourceKey for StringResourceModel as well
     * @param wicketFieldTextLabelId,       field wicket id and expression for property model as well
     * @param wicketFieldImageId,           image wicket id for field
     * @param imagePath,                    image path for field
     */

    private void addBusinessModelField(String wicketFieldHeaderTextLabelId, String wicketFieldTextLabelId,
                                       String wicketFieldImageId, String imagePath, boolean visibilityFlag) {

        // display field if and only if it is set by Action Leader
        Label fieldHeaderTextLabel = new Label(wicketFieldHeaderTextLabelId, new StringResourceModel(
                wicketFieldHeaderTextLabelId, this, null));
        fieldHeaderTextLabel.setOutputMarkupId(true);
        fieldHeaderTextLabel.setVisible(visibilityFlag);
        add(fieldHeaderTextLabel);

        Label fieldTextLabel = new Label(wicketFieldTextLabelId, new PropertyModel<String>(theBusinessModel,
                wicketFieldTextLabelId));
        fieldTextLabel.setOutputMarkupId(true);
        fieldTextLabel.setVisible(visibilityFlag);
        add(fieldTextLabel);

        NonCachingImage picture = getImageForField(wicketFieldImageId, imagePath);
        picture.setVisible(visibilityFlag);
        add(picture);
    }

    private NonCachingImage getImageForField(String wicketId, String imagePath) {
        PackageResourceReference ref = new PackageResourceReference(ActionBusinessModelDetailPage.class, imagePath);
        return new NonCachingImage(wicketId, ref.getResource());
    }

    private void loadActionAndBusinessModel(PageParameters parameters) {
        final Long businessModelId = parameters.get("businessModelId").toOptionalLong();
        if (businessModelId != null) {
            theBusinessModel = actionBusinessModelService.getById(businessModelId);
        } else {
            theBusinessModel = null;
        }

        final Long actionId = parameters.get("id").toOptionalLong();
        if (actionId != null) {
            theAction = actionService.getById(actionId);
        } else {
            theAction = null;
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
    private final class ContribuotrsProvider extends EntityProvider<User> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends User> iterator(long first, long count) {

            List<User> contributors = activityService.getAllActionContributorsBasedOnActionActivity(theAction, (int) first,
                    (int) count);
            return contributors.iterator();
        }

        @Override
        public long size() {
            return activityService.countAllActionContributorsBasedOnActionActivity(theAction);
        }
    }
}
