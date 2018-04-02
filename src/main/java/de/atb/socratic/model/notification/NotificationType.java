package de.atb.socratic.model.notification;

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

/**
 * NotificationType
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public enum NotificationType {

    UNDEFINED,

    CAMPAIGN_PARTICIPATE,

    CAMPAIGN_NEAR_DUE_DATE,

    PRIORITISATION_PARTICIPATE,

    PRIORITISATION_NEAR_DUE_DATE,

    CAMPAIGN_PRIORITISATION_PARTICIPATE,

    CAMPAIGN_PRIORITISATION_NEAR_DUE_DATE,

    PROJECT_PARTICIPATE,

    PROJECT_NEAR_DUE_DATE,

    NETWORK,

    PLATFORM_PARTICIPATE,

    CAMPAIGN_NEW,

    CAMPAIGN_UPDATE,

    CAMPAIGN_COMMENTS_LIKES,

    CAMPAIGN_UPDATE_IDEATION,

    CAMPAIGN_UPDATE_SELECTION,

    CAMPAIGN_UPDATE_IMPLEMENTATION,

    CAMPAIGN_FOLLOWED,

    IDEA_CREATED,

    IDEA_UPDATE,

    IDEA_COMMENTS_LIKES,

    IDEA_FOLLOWED,

    IDEA_SELECTION_LEADER_UPDATE,

    IDEA_NOT_SELECTION_LEADER_UPDATE,

    IDEA_ON_HALT_LEADER_UPDATE,

    IDEA_SELECTION_FOLLOWER_UPDATE,

    IDEA_NOT_SELECTION_FOLLOWER_UPDATE,

    IDEA_ON_HALT_FOLLOWER_UPDATE,

    ACTION_CREATION,

    ACTION_UPDATE,

    ACTION_REJECTION,

    ACTION_STATUS_UPDATE,

    ACTION_COMMENTS_LIKES,

    ACTION_FOLLOWED,

    ACTION_TEAM_MEMBER_INVITATION,

    ITERATION_CREATION,

    SHOW_ALL,

    USER_SKILLS_INTEREST

}
