package de.atb.socratic.web.dashboard.iLead.challenge;

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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.iLead.challenge.AdminIdeaSelectionPage.IdeaSelectionSortingCriteria;
import de.atb.socratic.web.inception.idea.IdeaDevelopmentPhase;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.selection.IdeaVotingPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class AdminIdeaListPanel extends GenericPanel<Campaign> {
    private static final long serialVersionUID = -257930933985282429L;
    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;
    // how many ideas do we show initially
    private static final int itemsPerPage = 6;

    // container holding the list of ideas
    private final WebMarkupContainer ideasContainer;

    // Repeating view showing the list of existing ideas
    private final DataView<Idea> ideasRepeater;

    private final EntityProvider<Idea> ideaProvider;

    private Date lastDate = new Date();

    private Idea theSelectedIdea;

    private String itemIdToHandle;

    private Modal selectIdeaNotificationModal;

    private IdeaDevelopmentPhase ideaDevelopmentPhase;

    private IdeaSelectionSortingCriteria sortingCriteria;

    private IdeaVotingPanel ideaVotingPanel;

    @EJB
    CampaignService campaignService;

    @EJB
    UserService userService;

    @EJB
    IdeaService ideaService;

    @Inject
    ParticipateNotificationService participateNotifier;

    public AdminIdeaListPanel(final String id, final IModel<Campaign> model, final StyledFeedbackPanel feedbackPanel) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;

        // get the campaigns ideas
        final Campaign campaign = getModelObject();

        // add container with list of existing ideas
        ideasContainer = new WebMarkupContainer("ideasContainer");
        add(ideasContainer.setOutputMarkupId(true));

        // add repeating view with list of existing ideas
        ideaProvider = new IdeaProvider(campaign);
        ideasRepeater = new DataView<Idea>("ideasRepeater", ideaProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Idea> item) {
                item.setOutputMarkupId(true);
                AdminIdeaListPanel.this.populateItem(item, item.getModelObject());
            }
        };
        ideasContainer.add(ideasRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", ideasRepeater) {
            private static final long serialVersionUID = -3010767611341072485L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(ideasRepeater.getPageCount() > 1);
            }
        });

        // confirmation model dialogue asking user to confirm his decision.
        selectIdeaNotificationModal = new SelectIdeaNotificationModal("selectIdeaNotificationModal", new StringResourceModel(
                "selectIdea.notification.modal.header", this, null), new StringResourceModel("selectIdea.notification.modal.message", this,
                null), false) {
            private static final long serialVersionUID = 2096179879061520451L;

            @Override
            public void selectIdeaClicked(AjaxRequestTarget target) {
                // update Idea's development Phase
                theSelectedIdea.setIdeaPhase(ideaDevelopmentPhase);
                ideaService.update(theSelectedIdea);

                List<User> followers = userService.getAllUsersByGivenFollowedChallenge(theSelectedIdea.getCampaign(),
                        Integer.MAX_VALUE, Integer.MAX_VALUE);
                followers.addAll(userService.getAllUsersByGivenFollowedIdea(theSelectedIdea, Integer.MAX_VALUE, Integer.MAX_VALUE));
                Set<User> noRepeatedUsers = new HashSet<>(followers);
                if (theSelectedIdea.getIdeaPhase() == IdeaDevelopmentPhase.Selected) {
                    // send notification to all idea leader
                    participateNotifier.addParticipationNotification(theSelectedIdea, theSelectedIdea.getPostedBy(),
                            NotificationType.IDEA_SELECTION_LEADER_UPDATE);

                    // send notification to all idea follower and challenge follower
                    for (User follower : noRepeatedUsers) {
                        if (!follower.equals(theSelectedIdea.getPostedBy())) {
                            participateNotifier.addParticipationNotification(theSelectedIdea, follower,
                                    NotificationType.IDEA_SELECTION_FOLLOWER_UPDATE);
                        }
                    }
                } else if (theSelectedIdea.getIdeaPhase() == IdeaDevelopmentPhase.NotSelected) {
                    // send notification to all idea leader
                    participateNotifier.addParticipationNotification(theSelectedIdea, theSelectedIdea.getPostedBy(),
                            NotificationType.IDEA_NOT_SELECTION_LEADER_UPDATE);
                    // send notification to all idea follower and challenge follower
                    for (User follower : noRepeatedUsers) {
                        if (!follower.equals(theSelectedIdea.getPostedBy())) {
                            participateNotifier.addParticipationNotification(theSelectedIdea, follower,
                                    NotificationType.IDEA_NOT_SELECTION_FOLLOWER_UPDATE);
                        }
                    }
                } else if (theSelectedIdea.getIdeaPhase() == IdeaDevelopmentPhase.OnHalt) {
                    // send notification to all idea leader
                    participateNotifier.addParticipationNotification(theSelectedIdea, theSelectedIdea.getPostedBy(),
                            NotificationType.IDEA_ON_HALT_LEADER_UPDATE);
                    // send notification to all idea follower and challenge follower
                    for (User follower : noRepeatedUsers) {
                        if (!follower.equals(theSelectedIdea.getPostedBy())) {
                            participateNotifier.addParticipationNotification(theSelectedIdea, follower,
                                    NotificationType.IDEA_ON_HALT_FOLLOWER_UPDATE);
                        }
                    }
                }

                // close the model window
                selectIdeaNotificationModal.appendCloseDialogJavaScript(target);
                setResponsePage(getPage());
            }
        };

        add(selectIdeaNotificationModal);
    }

    /**
     * @return
     */
    private DropDownChoice<IdeaDevelopmentPhase> newIdeaDevelopmentPhaseDropDownChoice(final Idea idea) {
        List<IdeaDevelopmentPhase> listOfSorrtingCriteria = Arrays.asList(IdeaDevelopmentPhase.values());
        final DropDownChoice<IdeaDevelopmentPhase> ideaDevelopmentPhaseChoice = new DropDownChoice<IdeaDevelopmentPhase>(
                "ideaDevelopmentPhase", new Model<IdeaDevelopmentPhase>(), listOfSorrtingCriteria,
                new IChoiceRenderer<IdeaDevelopmentPhase>() {
                    private static final long serialVersionUID = -3507943582789662873L;

                    @Override
                    public Object getDisplayValue(IdeaDevelopmentPhase object) {
                        return new StringResourceModel(object.getMessageKey(), AdminIdeaListPanel.this, null).getString();
                    }

                    @Override
                    public String getIdValue(IdeaDevelopmentPhase object, int index) {
                        return String.valueOf(index);
                    }
                });

        ideaDevelopmentPhaseChoice.setDefaultModelObject(idea.getIdeaPhase());
        // if action is already created by IdeaLeader or CO has already "not selected" idea, then do not allow CO to change
        // status of it.
        if (idea.isActionCreated() || idea.getIdeaPhase().equals(IdeaDevelopmentPhase.NotSelected)) {
            ideaDevelopmentPhaseChoice.setEnabled(false);
        }

        ideaDevelopmentPhaseChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = 4852295151360868123L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // on status update show model dialogue asking CO to confirm his decision.
                theSelectedIdea = idea;
                ideaDevelopmentPhase = ideaDevelopmentPhaseChoice.getModelObject();
                selectIdeaNotificationModal.appendShowDialogJavaScript(target);
            }
        });
        return ideaDevelopmentPhaseChoice;
    }

    /**
     * @param sortingCriteria
     * @return
     */
    public AdminIdeaListPanel setSortingCriteria(IdeaSelectionSortingCriteria sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
        return this;
    }

    protected void populateItem(final WebMarkupContainer item, final Idea idea) {
        item.setOutputMarkupId(true);

        item.add(new Label("shortText", new PropertyModel<String>(idea, "shortText")));
        item.add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.THUMBNAIL, idea.getPostedBy())));
        item.add(new Label("postedAt", new PropertyModel<Date>(idea, "postedAt")));
        item.add(new Label("postedBy.nickName", new PropertyModel<String>(idea, "postedBy.nickName")));
        item.add(new Label("commentsNumber", idea.getComments().size()));
        item.add(new Label("thumbsUpVotes", idea.getNoOfUpVotes()));

        item.add(ideaVotingPanel = new IdeaVotingPanel("ideaVotingPanel", Model.of(idea), false) {
            private static final long serialVersionUID = 7745141044795733701L;

            @Override
            protected void onVotingPerformed(AjaxRequestTarget target) {
            }
        });

        item.add(newIdeaDevelopmentPhaseDropDownChoice(idea));
    }

    /**
     * @author ATB
     */
    private final class IdeaProvider extends EntityProvider<Idea> {

        /**
         *
         */
        private static final long serialVersionUID = -1727094205049792307L;

        private final Campaign campaign;

        public IdeaProvider(Campaign campaign) {
            super();
            this.campaign = campaign;
        }

        /**
         * Instead of using index first to define which is the first result, we just get count ideas that have been posted
         * before the postDate of the current last element (idea). If there is no last element, use current date.
         */
        @Override
        public Iterator<? extends Idea> iterator(long first, long count) {
            List<Idea> ideas = null;
            if (sortingCriteria != null) {
                if (sortingCriteria.equals(IdeaSelectionSortingCriteria.Comments)) {
                    ideas = campaignService.getIdeasForCampaignFromToByDescendingNoOfComments(campaign, Long.valueOf(first)
                            .intValue(), Long.valueOf(count).intValue());
                } else if (sortingCriteria.equals(IdeaSelectionSortingCriteria.Likes)) {
                    ideas = campaignService.getIdeasForCampaignFromToByDescendingNoOfLikes(campaign, Long.valueOf(first)
                            .intValue(), Long.valueOf(count).intValue());
                } else if (sortingCriteria.equals(IdeaSelectionSortingCriteria.OverallRating)) {
                    ideas = campaignService.getIdeasForCampaignFromToByDescendingOverAllRating(campaign, Long.valueOf(first)
                            .intValue(), Long.valueOf(count).intValue());
                } else if (sortingCriteria.equals(IdeaSelectionSortingCriteria.Relevancy)) {
                    ideas = campaignService.getIdeasForCampaignFromToByDescendingRelevancy(campaign, Long.valueOf(first)
                            .intValue(), Long.valueOf(count).intValue());
                } else if (sortingCriteria.equals(IdeaSelectionSortingCriteria.Feasibility)) {
                    ideas = campaignService.getIdeasForCampaignFromToByDescendingFeasibility(campaign, Long.valueOf(first)
                            .intValue(), Long.valueOf(count).intValue());
                } else if (sortingCriteria.equals(IdeaSelectionSortingCriteria.Date)) {
                    ideas = campaignService.getIdeasForCampaignFromToByDescendingPostDate(campaign, Long.valueOf(first).intValue(),
                            Long.valueOf(count).intValue());
                }

            } else
                ideas = campaignService.getIdeasForCampaignFromToByDescendingPostDate(campaign, Long.valueOf(first).intValue(),
                        Long.valueOf(count).intValue());
            return ideas.iterator();
        }

        @Override
        public long size() {
            return campaignService.countIdeasForCampaign(campaign.getId());
        }

    }
}
