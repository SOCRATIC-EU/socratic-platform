package de.atb.socratic.service.notification;

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

import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.ActionCommentsLikesNotification;
import de.atb.socratic.model.notification.ActionRejectionNotification;
import de.atb.socratic.model.notification.ActionTeamMemberInvitationNotification;
import de.atb.socratic.model.notification.CampaignCommentsLikesNotification;
import de.atb.socratic.model.notification.IdeaCommentsLikesNotification;
import de.atb.socratic.model.notification.IdeaNotSelectedFollowerNotification;
import de.atb.socratic.model.notification.IdeaNotSelectedNotification;
import de.atb.socratic.model.notification.IdeaOnHaltFollowerNotification;
import de.atb.socratic.model.notification.IdeaOnHaltNotification;
import de.atb.socratic.model.notification.IdeaSelectionFollowerNotification;
import de.atb.socratic.model.notification.IdeaSelectionNotification;
import de.atb.socratic.model.notification.NewActionIterationNotification;
import de.atb.socratic.model.notification.NewActionNotification;
import de.atb.socratic.model.notification.NewCampaignNotification;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.notification.ParticipateInCampaignNotification;
import de.atb.socratic.model.notification.ParticipateInCampaignPrioritisationNotification;
import de.atb.socratic.model.notification.ParticipateInPlatformNotification;
import de.atb.socratic.model.notification.ParticipateInPrioritisationNotification;
import de.atb.socratic.model.notification.UpdateActionNotification;
import de.atb.socratic.model.notification.UpdateActionStatusNotification;
import de.atb.socratic.model.notification.UpdateCampaignIdeationNotification;
import de.atb.socratic.model.notification.UpdateCampaignImplementationNotification;
import de.atb.socratic.model.notification.UpdateCampaignNotification;
import de.atb.socratic.model.notification.UpdateCampaignSelectionNotification;
import de.atb.socratic.model.notification.UpdateIdeaNotification;
import de.atb.socratic.model.notification.UserActionFollowerNotification;
import de.atb.socratic.model.notification.UserChallengeFollowerNotification;
import de.atb.socratic.model.notification.UserIdeaCreationNotification;
import de.atb.socratic.model.notification.UserIdeaFollowerNotification;
import de.atb.socratic.model.notification.UserSkillsOrInterestsUpdateNotification;
import de.atb.socratic.service.user.UserService;

/**
 * ParticipateInCampaignNotificationService
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Named("ParticipateInCampaign")
@ApplicationScoped
public class ParticipateNotificationService extends NotificationService {

    private static final long serialVersionUID = -1596875267920578623L;
    private static final String CAMPAIGN_TEMPLATE = "participate_in_campaign_template.html";
    private static final String PRIORITISATION_TEMPLATE = "participate_in_prioritisation_template.html";
    private static final String CAMP_PRIORITISATION_TEMPLATE = "participate_in_campaign_prioritisation_template.html";
    private static final String FULLNAME = "FULLNAME";
    private static final String EMAIL = "EMAIL";
    private static String CAMPAIGN_SUBJECT = "Participate in campaign";
    private static String PRIORITISATION_SUBJECT = "Participate in prioritisation";
    private static String CAMPAIGN_PRIORITISATION_SUBJECT = "Participate in campaign prioritisation";
    private static String NEW_CAMPAIGN_BASED_ON_SKILLS_AND_INTERESTS = "New Challenge based on skills and interests";

    private static final String CAMPAIGN_NAME = "CAMPAIGN_NAME";
    private static final String EFF_LOGO = "EFF_LOGO";
    private static final String CAMPAIGN_DESCRIPTION = "CAMPAIGN_DESCRIPTION";
    private static final String CAMPAIGN_LINK = "CAMPAIGN_LINK";
    private static final String IDEA_NAME = "IDEA_NAME";
    private static final String IDEA_DESCRIPTION = "IDEA_DESCRIPTION";
    private static final String IDEAS_LINK = "IDEAS_LINK";

    @EJB
    UserService userService;

    public ParticipateNotificationService() {

    }

    public void addParticipationNotification(Object model, User user, NotificationType type) {
        if (!user.isReceivingNotifications()) {
            return;
        }
        switch (type) {
            case CAMPAIGN_PARTICIPATE:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {
                    this.template = CAMPAIGN_TEMPLATE;
                    ParticipateInCampaignNotification camp = new ParticipateInCampaignNotification();
                    camp.setCampaign((Campaign) model);
                    camp.setUser(user);
                    update(camp);
                }
                break;
            case PRIORITISATION_PARTICIPATE:
                this.template = PRIORITISATION_TEMPLATE;
                ParticipateInPrioritisationNotification prio = new ParticipateInPrioritisationNotification();
                prio.setIdea((Idea) model);
                prio.setUser(user);
                update(prio);
                break;
            case CAMPAIGN_PRIORITISATION_PARTICIPATE:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {
                    this.template = CAMP_PRIORITISATION_TEMPLATE;
                    ParticipateInCampaignPrioritisationNotification campPrio = new ParticipateInCampaignPrioritisationNotification();
                    campPrio.setCampaign((Campaign) model);
                    campPrio.setUser(user);
                    update(campPrio);
                }
                break;
            case PLATFORM_PARTICIPATE:
                ParticipateInPlatformNotification platform = new ParticipateInPlatformNotification();
                platform.setUser(user);
                platform.setEmail(model.toString());
                update(platform);
                break;
            case CAMPAIGN_NEW:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {
                    NewCampaignNotification newCampaignNotification = new NewCampaignNotification();
                    newCampaignNotification.setCampaign((Campaign) model);
                    newCampaignNotification.setUser(user);
                    update(newCampaignNotification);
                    break;
                }

            case CAMPAIGN_COMMENTS_LIKES:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {
                    CampaignCommentsLikesNotification campaignCommentsLikesNotification = new CampaignCommentsLikesNotification();
                    campaignCommentsLikesNotification.setCampaign((Campaign) model);
                    campaignCommentsLikesNotification.setUser(user);
                    update(campaignCommentsLikesNotification);
                    break;
                }

            case CAMPAIGN_UPDATE:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {
                    UpdateCampaignNotification updateCampaignNotification = new UpdateCampaignNotification();
                    updateCampaignNotification.setCampaign((Campaign) model);
                    updateCampaignNotification.setUser(user);
                    update(updateCampaignNotification);
                    break;
                }

            case CAMPAIGN_UPDATE_IDEATION:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {
                    UpdateCampaignIdeationNotification updateCampaignIdeationNotification = new UpdateCampaignIdeationNotification();
                    updateCampaignIdeationNotification.setCampaign((Campaign) model);
                    updateCampaignIdeationNotification.setUser(user);
                    update(updateCampaignIdeationNotification);
                    break;
                }

            case CAMPAIGN_UPDATE_SELECTION:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {
                    UpdateCampaignSelectionNotification updateCampaignSelectionNotification = new UpdateCampaignSelectionNotification();
                    updateCampaignSelectionNotification.setCampaign((Campaign) model);
                    updateCampaignSelectionNotification.setUser(user);
                    update(updateCampaignSelectionNotification);
                    break;
                }

            case CAMPAIGN_UPDATE_IMPLEMENTATION:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {
                    UpdateCampaignImplementationNotification updateCampaignImplementationNotification = new UpdateCampaignImplementationNotification();
                    updateCampaignImplementationNotification.setCampaign((Campaign) model);
                    updateCampaignImplementationNotification.setUser(user);
                    update(updateCampaignImplementationNotification);
                    break;
                }
            case CAMPAIGN_FOLLOWED:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {
                    UserChallengeFollowerNotification userChallengeFollowerNotification = new UserChallengeFollowerNotification(
                            NotificationType.CAMPAIGN_FOLLOWED);
                    userChallengeFollowerNotification.setCampaign((Campaign) model);
                    userChallengeFollowerNotification.setUser(user);
                    update(userChallengeFollowerNotification);
                    break;
                }
            case IDEA_CREATED:
                if (!user.isReceivingChallengeNotifications()) {
                    break;
                } else {

                    // when idea is created, add loggedIn user to challenge participants list and create a notification
                    Idea idea = (Idea) model;
                    UserChallengeFollowerNotification userChallengeFollowerNotification = new UserChallengeFollowerNotification(
                            NotificationType.IDEA_CREATED);
                    userChallengeFollowerNotification.setCampaign(idea.getCampaign());
                    userChallengeFollowerNotification.setUser(user);
                    update(userChallengeFollowerNotification);

                    // additionally, create a notification for challenge owner and followers of challenge and send him this
                    // notification that idea is created
                    // at first get followers of the challenge, campaign leader is already a follower of campaign
                    List<User> campaignFollowers = userService.getAllUsersByGivenFollowedChallenge(idea.getCampaign(),
                            Integer.MAX_VALUE, Integer.MAX_VALUE);
                    for (User follower : campaignFollowers) {
                        // do not send this notification to idea leader
                        if (!follower.equals(idea.getPostedBy())) {
                            UserIdeaCreationNotification userIdeaCreationNotification = new UserIdeaCreationNotification();
                            userIdeaCreationNotification.setIdea(idea);
                            userIdeaCreationNotification.setUser(follower);
                            update(userIdeaCreationNotification);
                        }
                    }
                    break;
                }

            case IDEA_UPDATE:
                if (!user.isReceivingIdeaNotifications()) {
                    break;
                } else {
                    UpdateIdeaNotification updateIdeaNotification = new UpdateIdeaNotification();
                    updateIdeaNotification.setIdea((Idea) model);
                    updateIdeaNotification.setUser(user);
                    update(updateIdeaNotification);
                    break;
                }

            case IDEA_COMMENTS_LIKES:
                if (!user.isReceivingIdeaNotifications()) {
                    break;
                } else {
                    IdeaCommentsLikesNotification ideaCommentsLikesNotification = new IdeaCommentsLikesNotification();
                    ideaCommentsLikesNotification.setIdea((Idea) model);
                    ideaCommentsLikesNotification.setUser(user);
                    update(ideaCommentsLikesNotification);
                    break;
                }

            case IDEA_FOLLOWED:
                if (!user.isReceivingIdeaNotifications()) {
                    break;
                } else {
                    UserIdeaFollowerNotification userIdeaFollowerNotification = new UserIdeaFollowerNotification();
                    userIdeaFollowerNotification.setIdea((Idea) model);
                    userIdeaFollowerNotification.setUser(user);
                    update(userIdeaFollowerNotification);
                    break;
                }

            case IDEA_SELECTION_LEADER_UPDATE:
                if (!user.isReceivingIdeaNotifications()) {
                    break;
                } else {
                    IdeaSelectionNotification ideaSelectionNotification = new IdeaSelectionNotification();
                    ideaSelectionNotification.setIdea((Idea) model);
                    ideaSelectionNotification.setUser(user);
                    update(ideaSelectionNotification);
                    break;
                }

            case IDEA_NOT_SELECTION_LEADER_UPDATE:
                if (!user.isReceivingIdeaNotifications()) {
                    break;
                } else {
                    IdeaNotSelectedNotification ideaNotSelectionNotification = new IdeaNotSelectedNotification();
                    ideaNotSelectionNotification.setIdea((Idea) model);
                    ideaNotSelectionNotification.setUser(user);
                    update(ideaNotSelectionNotification);
                    break;
                }

            case IDEA_ON_HALT_LEADER_UPDATE:
                if (!user.isReceivingIdeaNotifications()) {
                    break;
                } else {
                    IdeaOnHaltNotification ideaOnHaltNotification = new IdeaOnHaltNotification();
                    ideaOnHaltNotification.setIdea((Idea) model);
                    ideaOnHaltNotification.setUser(user);
                    update(ideaOnHaltNotification);
                    break;
                }

            case IDEA_SELECTION_FOLLOWER_UPDATE:
                if (!user.isReceivingIdeaNotifications()) {
                    break;
                } else {
                    IdeaSelectionFollowerNotification ideaSelectionFollowerNotification = new IdeaSelectionFollowerNotification();
                    ideaSelectionFollowerNotification.setIdea((Idea) model);
                    ideaSelectionFollowerNotification.setUser(user);
                    update(ideaSelectionFollowerNotification);
                    break;
                }

            case IDEA_NOT_SELECTION_FOLLOWER_UPDATE:
                if (!user.isReceivingIdeaNotifications()) {
                    break;
                } else {
                    IdeaNotSelectedFollowerNotification ideaNotSelectedFollowerNotification = new IdeaNotSelectedFollowerNotification();
                    ideaNotSelectedFollowerNotification.setIdea((Idea) model);
                    ideaNotSelectedFollowerNotification.setUser(user);
                    update(ideaNotSelectedFollowerNotification);
                    break;
                }

            case IDEA_ON_HALT_FOLLOWER_UPDATE:
                if (!user.isReceivingIdeaNotifications()) {
                    break;
                } else {
                    IdeaOnHaltFollowerNotification ideaOnHaltFollowerNotification = new IdeaOnHaltFollowerNotification();
                    ideaOnHaltFollowerNotification.setIdea((Idea) model);
                    ideaOnHaltFollowerNotification.setUser(user);
                    update(ideaOnHaltFollowerNotification);
                    break;
                }

            case ACTION_CREATION:
                if (!user.isReceiveActionNotifications()) {
                    break;
                } else {
                    NewActionNotification newActionNotification = new NewActionNotification();
                    newActionNotification.setAction((Action) model);
                    newActionNotification.setUser(user);
                    update(newActionNotification);
                }
                break;

            case ACTION_UPDATE:
                if (!user.isReceiveActionNotifications()) {
                    break;
                } else {
                    UpdateActionNotification updateActionNotification = new UpdateActionNotification();
                    updateActionNotification.setAction((Action) model);
                    updateActionNotification.setUser(user);
                    update(updateActionNotification);
                }
                break;

            case ACTION_REJECTION:
                if (!user.isReceiveActionNotifications()) {
                    break;
                } else {
                    ActionRejectionNotification actionRejectionNotification = new ActionRejectionNotification();
                    actionRejectionNotification.setIdea((Idea) model);
                    actionRejectionNotification.setUser(user);
                    update(actionRejectionNotification);
                }
                break;

            case ACTION_STATUS_UPDATE:
                if (!user.isReceiveActionNotifications()) {
                    break;
                } else {
                    UpdateActionStatusNotification updateActionStatusNotification = new UpdateActionStatusNotification();
                    updateActionStatusNotification.setAction((Action) model);
                    updateActionStatusNotification.setUser(user);
                    update(updateActionStatusNotification);
                }
                break;

            case ACTION_COMMENTS_LIKES:
                if (!user.isReceiveActionNotifications()) {
                    break;
                } else {
                    ActionCommentsLikesNotification actionCommentsLikesNotification = new ActionCommentsLikesNotification();
                    actionCommentsLikesNotification.setAction((Action) model);
                    actionCommentsLikesNotification.setUser(user);
                    update(actionCommentsLikesNotification);
                }
                break;

            case ACTION_FOLLOWED:
                if (!user.isReceiveActionNotifications()) {
                    break;
                } else {
                    UserActionFollowerNotification userActionFollowerNotification = new UserActionFollowerNotification();
                    userActionFollowerNotification.setAction((Action) model);
                    userActionFollowerNotification.setUser(user);
                    update(userActionFollowerNotification);
                }
                break;

            case ACTION_TEAM_MEMBER_INVITATION:
                if (!user.isReceiveActionNotifications()) {
                    break;
                } else {
                    ActionTeamMemberInvitationNotification actionTeamMemberInvitationNotification = new ActionTeamMemberInvitationNotification();
                    actionTeamMemberInvitationNotification.setAction((Action) model);
                    actionTeamMemberInvitationNotification.setUser(user);
                    update(actionTeamMemberInvitationNotification);
                }
                break;

            case ITERATION_CREATION:
                if (!user.isReceiveActionNotifications()) {
                    break;
                } else {
                    NewActionIterationNotification newActionIterationNotification = new NewActionIterationNotification();
                    newActionIterationNotification.setActionIteration((ActionIteration) model);
                    newActionIterationNotification.setUser(user);
                    update(newActionIterationNotification);
                }
                break;

            case USER_SKILLS_INTEREST:
                UserSkillsOrInterestsUpdateNotification userSkillsOrInterestsUpdateNotification = new UserSkillsOrInterestsUpdateNotification();
                userSkillsOrInterestsUpdateNotification.setUser(user);
                update(userSkillsOrInterestsUpdateNotification);
                break;
        }
    }

    public void sendParticipationMail(Object model, User user, String link, String logoLink, NotificationType type) throws NotificationException {
        if (!user.isReceivingNotifications()) {
            return;
        }

        setValue(EFF_LOGO, logoLink);
        setHomepageLink();
        setValue(FULLNAME, user.getNickName());
        setValue(EMAIL, user.getEmail());

        switch (type) {
            case CAMPAIGN_PARTICIPATE:
                Campaign campaign = (Campaign) model;
                setValue(CAMPAIGN_NAME, campaign.getName());
                setValue(CAMPAIGN_DESCRIPTION, campaign.getDescription());
                setValue(CAMPAIGN_LINK, link);
                this.subject = CAMPAIGN_SUBJECT + " '" + campaign.getName() + "'";
                logger.infof("Would sent a mail to %s for invitation to campaign %s", user.getEmail(), campaign.getName());
                // send mail actually!
                // sendMessage(user.getEmail());
                break;
            case PRIORITISATION_PARTICIPATE:
                Idea idea = (Idea) model;
                setValue(IDEA_NAME, idea.getShortText());
                setValue(IDEA_DESCRIPTION, idea.getDescription());
                setValue(IDEAS_LINK, link);
                this.subject = PRIORITISATION_SUBJECT + " '" + idea.getShortText() + "'";
                logger.infof("Would sent a mail to %s for invitation to participate on prioritisation of idea %s", user.getEmail(), idea.getShortText());
                // send mail actually!
                // sendMessage(user.getEmail());
                break;
            case CAMPAIGN_PRIORITISATION_PARTICIPATE:
                Campaign camp = (Campaign) model;
                setValue(CAMPAIGN_NAME, camp.getName());
                setValue(CAMPAIGN_DESCRIPTION, camp.getDescription());
                setValue(CAMPAIGN_LINK, link);

                this.subject = CAMPAIGN_PRIORITISATION_SUBJECT + " '" + camp.getName() + "'";
                logger.infof("Would sent a mail to %s for invitation to participate on campaign %s prioritisation", user.getEmail(), camp.getName());
                // send mail actually!
                // sendMessage(user.getEmail());
                break;

            case CAMPAIGN_NEW:
                Campaign newCamp = (Campaign) model;
                setValue(CAMPAIGN_NAME, newCamp.getName());
                setValue(CAMPAIGN_DESCRIPTION, newCamp.getDescription());
                setValue(CAMPAIGN_LINK, link);

                this.subject = NEW_CAMPAIGN_BASED_ON_SKILLS_AND_INTERESTS + " '" + newCamp.getName() + "'";
                logger.infof("Would sent a mail to %s for invitation to participate on campaign %s , since the user has matching interests and skills", user.getEmail(), newCamp.getName());
                // sending works: We NEED TO UPDATE/DELETE THE OLD EEF Mails addresses
                //sendMessage(user.getEmail());
                break;


        }


    }

}
