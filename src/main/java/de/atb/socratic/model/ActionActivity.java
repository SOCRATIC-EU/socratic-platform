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
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
@DiscriminatorValue(value = "AA")
public class ActionActivity extends Activity {

    private static final long serialVersionUID = 2507866711447237867L;

    @ManyToOne
    @NotNull
    private Action action;

    @ManyToOne
    private ActionIteration iteration;

    @OneToOne
    private BusinessModel businessModel;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public ActionIteration getIteration() {
        return iteration;
    }

    public void setIteration(ActionIteration iteration) {
        this.iteration = iteration;
    }

    public BusinessModel getBusinessModel() {
        return businessModel;
    }

    public void setBusinessModel(BusinessModel businessModel) {
        this.businessModel = businessModel;
    }

    public static ActionActivity ofActionCommentAdd(final Comment comment, Action action) {
        ActionActivity act = new ActionActivity();
        act.setActivityType(ActivityType.ADD_COMMENT_TO_ACTION);
        act.setAction(action);
        act.setPerformedAt(comment.getPostedAt());
        act.setPerformedBy(comment.getPostedBy());
        act.setCommentId(comment.getId());
        return act;
    }

    public static ActionActivity ofActionLiked(Action action, User user) {
        ActionActivity act = new ActionActivity();
        act.setActivityType(ActivityType.ACTION_LIKE);
        act.setAction(action);
        act.setPerformedAt(new Date());
        act.setPerformedBy(user);
        return act;
    }

    public static ActionActivity ofActionIterationCommentAdd(final Comment comment, Action action, ActionIteration iteration) {
        ActionActivity act = new ActionActivity();
        act.setActivityType(ActivityType.ADD_COMMENT_TO_ITERATION);
        act.setAction(action);
        act.setIteration(iteration);
        act.setPerformedAt(comment.getPostedAt());
        act.setPerformedBy(comment.getPostedBy());
        act.setCommentId(comment.getId());
        return act;
    }

    public static ActionActivity ofActionBusinessModelCommentAdd(final Comment comment, Action action, BusinessModel businessModel) {
        ActionActivity act = new ActionActivity();
        act.setActivityType(ActivityType.ADD_COMMENT_TO_BUSINESS_MODEL);
        act.setAction(action);
        act.setBusinessModel(businessModel);
        act.setPerformedAt(comment.getPostedAt());
        act.setPerformedBy(comment.getPostedBy());
        act.setCommentId(comment.getId());
        return act;
    }

    public static ActionActivity ofActionIterationLiked(Action action, ActionIteration iteration, User user) {
        ActionActivity act = new ActionActivity();
        act.setActivityType(ActivityType.ITERATION_LIKE);
        act.setAction(action);
        act.setIteration(iteration);
        act.setPerformedAt(new Date());
        act.setPerformedBy(user);
        return act;
    }

    public static ActionActivity ofActionBusinessModelLiked(Action action, BusinessModel businessModel, User user) {
        ActionActivity act = new ActionActivity();
        act.setActivityType(ActivityType.BUSINESS_MODEL_LIKE);
        act.setAction(action);
        act.setBusinessModel(businessModel);
        act.setPerformedAt(new Date());
        act.setPerformedBy(user);
        return act;
    }
}
