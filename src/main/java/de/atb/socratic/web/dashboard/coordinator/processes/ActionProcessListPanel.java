package de.atb.socratic.web.dashboard.coordinator.processes;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.ActionPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.coordinator.processes.CoordinatorDashboardProcessesPage.ProcessSortingCriteria;
import de.atb.socratic.web.dashboard.coordinator.processes.CoordinatorDashboardProcessesPage.TypeOfprocess;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ActionProcessListPanel extends GenericPanel<Action> {

    /**
     *
     */
    private static final long serialVersionUID = -159659429992094182L;

    // inject the EJB for managing actions
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    ActionService actionService;

    @EJB
    ActivityService activityService;

    @EJB
    UserService userService;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many actions do we show initially
    private static final int itemsPerPage = 10;

    // container holding the list of actions
    private final WebMarkupContainer actionsContainer;

    // Repeating view showing the list of existing actions
    private final DataView<Action> actionsRepeater;

    private final EntityProvider<Action> actionProvider;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private NonCachingImage actionProfilePicture;

    private TypeOfprocess processType;

    private ProcessSortingCriteria processSortingCriteria;

    private CheckGroup<Action> actionGroup;
    private final Form<Action> form;

    public ActionProcessListPanel(final String id, final StyledFeedbackPanel feedbackPanel, TypeOfprocess typeOfprocess) {
        super(id);

        this.feedbackPanel = feedbackPanel;

        this.processType = typeOfprocess;

        form = new Form<Action>("form");
        add(form);
        form.add(actionGroup = newUserCheckGroup());

        // add container with list of existing actions
        actionsContainer = new WebMarkupContainer("actionsContainer");
        actionGroup.add(actionsContainer.setOutputMarkupId(true));

        CheckGroupSelector checkGroupSelector = new CheckGroupSelector("actionGroupSelector", actionGroup);
        actionsContainer.add(checkGroupSelector);

        // add repeating view with list of existing actions
        actionProvider = new ActionProvider();

        actionsRepeater = new DataView<Action>("actionsRepeater", actionProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Action> item) {
                item.setOutputMarkupId(true);
                ActionProcessListPanel.this.populateItem(item, item.getModelObject());
            }
        };

        actionsContainer.add(actionsRepeater);

        actionGroup.add(new BootstrapAjaxPagingNavigator("pagination", actionsRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(actionsRepeater.getPageCount() > 1);
            }
        });

    }

    /**
     * @return
     */
    private CheckGroup<Action> newUserCheckGroup() {
        CheckGroup<Action> checkGroup = new CheckGroup<Action>("actionGroup", new ArrayList<Action>());
        checkGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            private static final long serialVersionUID = -8193184672687169923L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // updateDeleteSelectedButton(target);
            }
        });

        return checkGroup;
    }

    /**
     * @param processType
     * @param target
     * @return
     */
    public ActionProcessListPanel setProcessType(TypeOfprocess processType, AjaxRequestTarget target) {
        this.processType = processType;
        target.add(actionsContainer);
        target.add(actionGroup);
        return this;
    }

    /**
     * @param processSortingCriteria
     * @return
     */
    public ActionProcessListPanel setProcessSortingCriteria(ProcessSortingCriteria processSortingCriteria,
                                                            AjaxRequestTarget target) {
        this.processSortingCriteria = processSortingCriteria;
        target.add(actionsContainer);
        target.add(actionGroup);
        return this;
    }

    /**
     * @return
     */
    public long getActionListSize() {
        return actionProvider.size();
    }

    protected void populateItem(final WebMarkupContainer item, final Action action) {

        item.setOutputMarkupId(true);
        item.add(newSelectionCheck(action));
        item.add(new Label("createdOn", new PropertyModel<String>(action, "postedAt")));
        item.add(new Label("name", new PropertyModel<String>(action, "shortText")));

        // add follower label locally
        Label followersTotalNoLabel = new Label("followersTotalNoLabel", userService.countAllUsersByGivenFollowedAction(action));
        followersTotalNoLabel.setOutputMarkupId(true);
        item.add(followersTotalNoLabel);

        // action contributors
        Label contributors = new Label("contributors", activityService.countAllActionContributorsBasedOnActionActivity(action));
        item.add(contributors.setOutputMarkupId(true));

        // action comments label
        Label actionCommentLabel;
        int commentSize = 0;
        if (action.getComments() != null || !action.getComments().isEmpty()) {
            commentSize = action.getComments().size();
        }
        actionCommentLabel = new Label("actionCommentLabel", Model.of(commentSize));
        actionCommentLabel.setOutputMarkupId(true);
        item.add(actionCommentLabel);

        item.add(new Label("thumbsUpVotes", action.getNoOfUpVotes()));

        // action process column
        item.add(new Label("processStatus", new StringResourceModel("action.iteration.text", this, null).getString() + " "
                + action.getActionIterations().size()));

        // Leader
        AjaxLink<Void> link = userImageLink("userProfileDetailLink", action.getPostedBy());
        link.add(new NonCachingImage("leader_img", ProfilePictureResource.get(PictureType.THUMBNAIL, action.getPostedBy())));
        item.add(link);
        item.add(new Label("leader_name", action.getPostedBy().getNickName()));
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
     * @param action
     * @return
     */
    private Check<Action> newSelectionCheck(final Action action) {
        Check<Action> check = new Check<Action>("actionCheck", new Model<>(action), actionGroup) {

            private static final long serialVersionUID = -5802376197291247523L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        return check;
    }

    protected NonCachingImage newActionProfilePicturePreview(Action action) {
        actionProfilePicture = new NonCachingImage("profilePicturePreview", ActionPictureResource.get(
                PictureType.PROFILE, action));
        actionProfilePicture.setOutputMarkupId(true);
        return actionProfilePicture;
    }

    public CheckGroup<Action> getActionGroup() {
        return actionGroup;
    }

    /**
     * @author ATB
     */
    private final class ActionProvider extends EntityProvider<Action> {
        private static final long serialVersionUID = -1727094205049792307L;

        @Override
        public Iterator<? extends Action> iterator(long first, long count) {
            List<Action> actions = new LinkedList<>();
            // based on status, call different methods
            if (processType.equals(TypeOfprocess.Action_Solution) || processType.equals(TypeOfprocess.All)) {
                // all actions
                actions = actionService.getAll(Long.valueOf(first).intValue(), Long.valueOf(count).intValue());
            }
            return actions.iterator();
        }

        @Override
        public long size() {
            if (processType.equals(TypeOfprocess.Action_Solution) || processType.equals(TypeOfprocess.All)) {
                return actionService.countAll();
            }
            return 0;
        }

    }
}
