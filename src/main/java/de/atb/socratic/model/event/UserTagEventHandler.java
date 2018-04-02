package de.atb.socratic.model.event;

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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.notification.InvitationMailService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.action.detail.ActionSolutionPage;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import de.atb.socratic.web.provider.UrlProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SessionScoped
public class UserTagEventHandler implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 264726909589234495L;

    @Inject
    UserService userService;

    @Inject
    ActionService actionService;

    @Inject
    ActivityService activityService;

    @Inject
    InvitationMailService invitationMailService;

    @Inject
    UrlProvider urlProvider;

    // inject a logger
    @Inject
    Logger logger;

    /**
     * Notifies user via email if updated user skills and interests matches Action skills and keywords. Whenever the logged in
     * user changes his profile settings, the respective event should be fired.
     *
     * @param userTagAdded
     */
    public void skillsOrInterestsAddedListener(@Observes UserTagAdded userTagAdded) {
        User user = userTagAdded.getUser();
        List<Tag> addedTags = userTagAdded.getAddedSkillsOrInterest();
        Set<Tag> nonRepeateTag = new HashSet<>(addedTags);

        if (!nonRepeateTag.isEmpty()) {
            // 1. calculate matching projects
            List<Action> matchingActions = actionService.getAllActionsByTag(nonRepeateTag);

            // 2. get list of project, where user is already involved in
            List<Action> invovledActions = actionService.getAllActionsCreatedByUser(user);
            List<Action> participatedActions = activityService.getAllActionsByActionActivityCreator(user, Integer.MAX_VALUE,
                    Integer.MAX_VALUE, EntitiySortingCriteria.created);
            if (!participatedActions.isEmpty()) {
                invovledActions.addAll(participatedActions);
            }

            Set<Action> nonRepInvovledActions = new HashSet<>(invovledActions);

            // 3. create diff of 1 and 2
            List<Action> actionsWithoutInvolment = new LinkedList<>();
            for (Action matchedAction : matchingActions) {
                if (!nonRepInvovledActions.contains(matchedAction)) {
                    actionsWithoutInvolment.add(matchedAction);
                }
            }

            // 4. notify user about diff projects
            List<Map<String, String>> actionsList = new LinkedList<>();

            for (Action action : actionsWithoutInvolment) {
                Map<String, String> actionMap = new HashMap<>();
                actionMap.put("name", action.getShortText());
                actionMap.put("url",
                        urlProvider.urlFor(ActionSolutionPage.class, new PageParameters().set("id", action.getId())));
                actionsList.add(actionMap);
            }
            if (!actionsList.isEmpty()) {
                try {
                    invitationMailService.sendActionMatchSuggestionMessage(user.getEmail(), actionsList);
                } catch (NotificationException e) {
                    logger.error(e);
                    throw new RuntimeException("An unexpected error occurred!", e);
                }
            }
        }
    }
}
