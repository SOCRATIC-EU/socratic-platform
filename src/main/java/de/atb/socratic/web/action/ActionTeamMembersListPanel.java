package de.atb.socratic.web.action;

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
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.action.ActionAddTeamPage.TeamMemberRoles;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ActionTeamMembersListPanel extends GenericPanel<Action> {
    private static final long serialVersionUID = -257930933985282429L;
    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;
    // how many participants do we show initially
    private static final int itemsPerPage = 3;

    // container holding the list of participants
    private final WebMarkupContainer participantsContainer;

    // Repeating view showing the list of existing participants
    private final DataView<User> participantsRepeater;

    private final EntityProvider<User> participantProvider;

    private Date lastDate = new Date();

    private String itemIdToHandle;

    @EJB
    IdeaService ideaService;

    @EJB
    UserService userService;

    @EJB
    ActivityService activityService;

    @EJB
    ActionService actionService;

    @Inject
    ParticipateNotificationService participateNotifier;

    final Action action;

    private TeamMemberRoles teamMemberRoles;

    public ActionTeamMembersListPanel(final String id, final IModel<Action> model, final StyledFeedbackPanel feedbackPanel,
                                      TeamMemberRoles teamMemberRoles) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;

        this.teamMemberRoles = teamMemberRoles;

        // get the ideas participants
        action = getModelObject();

        // add container with list of existing participants
        participantsContainer = new WebMarkupContainer("participantsContainer");
        add(participantsContainer.setOutputMarkupId(true));

        // add repeating view with list of existing participants
        participantProvider = new UserProvider(action);
        participantsRepeater = new DataView<User>("participantsRepeater", participantProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<User> item) {
                item.setOutputMarkupId(true);
                ActionTeamMembersListPanel.this.populateItem(item, item.getModelObject());
            }
        };
        participantsContainer.add(participantsRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", participantsRepeater) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(participantsRepeater.getPageCount() > 1);
            }
        });
    }

    protected void populateItem(final WebMarkupContainer item, final User user) {
        item.setOutputMarkupId(true);
        item.add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.PROFILE, user)));
        final BookmarkablePageLink<UserProfileDetailsPage> profileLink = new BookmarkablePageLink<>(
                "profileLink",
                UserProfileDetailsPage.class,
                new PageParameters().set("id", user.getId()));
        profileLink.add(new Label("nickName", new PropertyModel<String>(user, "nickName")));
        item.add(profileLink);
        item.add(new Label("noOfCommentsToIdea", ideaService.countCommentsForIdeaByUser(action.getIdea(), user)));
        item.add(deleteAddButton(user, item));
    }

    private AjaxButton deleteAddButton(final User user, final WebMarkupContainer item) {
        final AjaxButton deleteAddButton = new AjaxButton("addDeleteButton") {
            private static final long serialVersionUID = -3059572465020964931L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (teamMemberRoles.equals(TeamMemberRoles.PotentialTeamMember)) {
                    // check if current user is already in action team?
                    if (action.getTeamMembers() != null && action.getTeamMembers().contains(user)) {
                        this.add(new AttributeModifier("value", new StringResourceModel("actions.members.already.button", this,
                                null).getString()));

                        // do not allow action leader to press this button
                        setEnabled(false);
                    } else if (action.getInvitedTeamMembers() != null && action.getInvitedTeamMembers().contains(user)) {
                        this.add(new AttributeModifier("value", new StringResourceModel("actions.members.invited.button", this,
                                null).getString()));

                        // do not allow action leader to press this button
                        setEnabled(false);
                    } else {
                        this.add(new AttributeModifier("value", new StringResourceModel("actions.members.add.button", this,
                                null).getString()));
                    }
                } else if (teamMemberRoles.equals(TeamMemberRoles.TeamMember)) {
                    this.add(new AttributeModifier("value", new StringResourceModel("actions.members.delete.button", this, null)
                            .getString()));
                }
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (teamMemberRoles.equals(TeamMemberRoles.PotentialTeamMember)) {
                    // Invite User to be team member
                    actionService.inviteUserToBeTeamMember(action, user);
                } else if (teamMemberRoles.equals(TeamMemberRoles.TeamMember)) {
                    // Delete member from the list
                    actionService.deleteTeamMemberFromList(action, user);
                    item.setVisible(false);
                    target.add(item);
                }
                setResponsePage(getPage());
            }

        };

        return deleteAddButton;
    }

    /**
     * @author ATB
     */
    private final class UserProvider extends EntityProvider<User> {

        /**
         *
         */
        private static final long serialVersionUID = -1727094205049792307L;

        public UserProvider(Action action) {
            super();
        }

        @Override
        public Iterator<? extends User> iterator(long first, long count) {
            List<User> users = null;
            if (teamMemberRoles.equals(TeamMemberRoles.PotentialTeamMember)) {
                users = activityService.getAllIdeaActivityUsersByAscNickNameAndByIdea(action.getIdea(), Long.valueOf(first).intValue(), Long
                        .valueOf(count).intValue());
            } else if (teamMemberRoles.equals(TeamMemberRoles.TeamMember)) {
                users = actionService.getAllTeamMembersByAscFirstNameAndByAction(action, Long.valueOf(first).intValue(), Long.valueOf(count)
                        .intValue());
            }

            return users.iterator();
        }

        @Override
        public long size() {
            if (teamMemberRoles.equals(TeamMemberRoles.PotentialTeamMember)) {
                return activityService.countAllIdeaActivityUsersByIdea(action.getIdea());
            } else if (teamMemberRoles.equals(TeamMemberRoles.TeamMember)) {
                return actionService.countTeamMembersByAscFirstNameAndByAction(action);
            }

            return 0;
        }

    }
}
