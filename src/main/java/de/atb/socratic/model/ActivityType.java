package de.atb.socratic.model;

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

public enum ActivityType {

    // Activity types related to challenge
    ADD_IDEA_TO_CHALLENGE("activityType.AddIdeaToChallenge"),
    ADD_REFERENCE_TO_CHALLENGE("activityType.AddReferenceToChallenge"),
    CHALLENGE_LIKE("activityType.LikeChallenge"),
    ADD_COMMENT_TO_CHALLENGE("activityType.AddCommentToChallenge"),

    // Activity types related to Idea
    ADD_COMMENT_TO_IDEA("activityType.AddCommentToIdea"),
    IDEA_LIKE("activityType.LikeIdea"),
    IDEA_VOTE("activityType.VoteIdea"),

    // Activity types related to Action/Project
    ADD_COMMENT_TO_ACTION("activityType.AddCommentToAction"),
    ACTION_LIKE("activityType.LikeAction"),

    // Activity types related to Action Iteration
    ADD_COMMENT_TO_ITERATION("activityType.AddCommentToIteration"),
    ADD_COMMENT_TO_BUSINESS_MODEL("activityType.AddCommentToBusinessModel"),
    ITERATION_LIKE("activityType.LikeIteration"),
    BUSINESS_MODEL_LIKE("activityType.LikeBusinessModel");

    private final String nameKey;

    ActivityType(final String nameKey) {
        this.nameKey = nameKey;
    }

    public String getNameKey() {
        return nameKey;
    }
}
