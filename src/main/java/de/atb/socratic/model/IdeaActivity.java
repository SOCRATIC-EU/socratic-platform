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

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@DiscriminatorValue(value = "IA")
public class IdeaActivity extends Activity {

    private static final long serialVersionUID = 2507866711447237867L;

    @ManyToOne
    @NotNull
    private Idea idea;

    public Idea getIdea() {
        return idea;
    }

    public void setIdea(Idea idea) {
        this.idea = idea;
    }

    public static IdeaActivity ofCommentAdd(final Comment comment, Idea idea) {
        IdeaActivity act = new IdeaActivity();
        act.setActivityType(ActivityType.ADD_COMMENT_TO_IDEA);
        act.setIdea(idea);
        act.setPerformedAt(comment.getPostedAt());
        act.setPerformedBy(comment.getPostedBy());
        act.setCommentId(comment.getId());
        return act;
    }

    public static IdeaActivity ofLiked(Idea idea, User user) {
        IdeaActivity act = new IdeaActivity();
        act.setActivityType(ActivityType.IDEA_LIKE);
        act.setIdea(idea);
        act.setPerformedAt(new Date());
        act.setPerformedBy(user);
        return act;
    }

    public static IdeaActivity ofVoted(Idea idea, User user) {
        IdeaActivity act = new IdeaActivity();
        act.setActivityType(ActivityType.IDEA_VOTE);
        act.setIdea(idea);
        act.setPerformedAt(new Date());
        act.setPerformedBy(user);
        return act;
    }
}
