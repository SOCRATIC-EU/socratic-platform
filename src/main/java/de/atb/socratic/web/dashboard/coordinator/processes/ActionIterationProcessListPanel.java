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
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionIterationService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.ActionIterationPictureResource;
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
public class ActionIterationProcessListPanel extends GenericPanel<ActionIteration> {

    /**
     *
     */
    private static final long serialVersionUID = -159659429992094182L;

    // inject the EJB for managing actionIterations
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    ActionIterationService actionIterationService;

    @EJB
    ActivityService activityService;

    @EJB
    ActionService actionService;

    @EJB
    UserService userService;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many actionIterations do we show initially
    private static final int itemsPerPage = 10;

    // container holding the list of actionIterations
    private final WebMarkupContainer actionIterationsContainer;

    // Repeating view showing the list of existing actionIterations
    private final DataView<ActionIteration> actionIterationsRepeater;

    private final EntityProvider<ActionIteration> actionIterationProvider;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private NonCachingImage actionIterationProfilePicture;

    private TypeOfprocess processType;

    private ProcessSortingCriteria processSortingCriteria;

    private CheckGroup<ActionIteration> actionIterationGroup;
    private final Form<ActionIteration> form;

    public ActionIterationProcessListPanel(final String id, final StyledFeedbackPanel feedbackPanel, TypeOfprocess typeOfprocess) {
        super(id);

        this.feedbackPanel = feedbackPanel;

        this.processType = typeOfprocess;

        form = new Form<ActionIteration>("form");
        add(form);
        form.add(actionIterationGroup = newUserCheckGroup());

        // add container with list of existing actionIterations
        actionIterationsContainer = new WebMarkupContainer("actionIterationsContainer");
        actionIterationGroup.add(actionIterationsContainer.setOutputMarkupId(true));

        CheckGroupSelector checkGroupSelector = new CheckGroupSelector("actionIterationGroupSelector", actionIterationGroup);
        actionIterationsContainer.add(checkGroupSelector);

        // add repeating view with list of existing actionIterations
        actionIterationProvider = new ActionIterationProvider();

        actionIterationsRepeater = new DataView<ActionIteration>("actionIterationsRepeater", actionIterationProvider,
                itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<ActionIteration> item) {
                item.setOutputMarkupId(true);
                ActionIterationProcessListPanel.this.populateItem(item, item.getModelObject());
            }
        };

        actionIterationsContainer.add(actionIterationsRepeater);

        actionIterationGroup.add(new BootstrapAjaxPagingNavigator("pagination", actionIterationsRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(actionIterationsRepeater.getPageCount() > 1);
            }
        });

    }

    /**
     * @return
     */
    private CheckGroup<ActionIteration> newUserCheckGroup() {
        CheckGroup<ActionIteration> checkGroup = new CheckGroup<ActionIteration>("actionIterationGroup",
                new ArrayList<ActionIteration>());
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
    public ActionIterationProcessListPanel setProcessType(TypeOfprocess processType, AjaxRequestTarget target) {
        this.processType = processType;
        target.add(actionIterationsContainer);
        target.add(actionIterationGroup);
        return this;
    }

    /**
     * @param processSortingCriteria
     * @return
     */
    public ActionIterationProcessListPanel setProcessSortingCriteria(ProcessSortingCriteria processSortingCriteria,
                                                                     AjaxRequestTarget target) {
        this.processSortingCriteria = processSortingCriteria;
        target.add(actionIterationsContainer);
        target.add(actionIterationGroup);
        return this;
    }

    /**
     * @return
     */
    public long getActionIterationListSize() {
        return actionIterationProvider.size();
    }

    protected void populateItem(final WebMarkupContainer item, final ActionIteration actionIteration) {

        item.setOutputMarkupId(true);
        item.add(newSelectionCheck(actionIteration));
        item.add(new Label("createdOn", new PropertyModel<String>(actionIteration, "postedAt")));
        item.add(new Label("name", new PropertyModel<String>(actionIteration, "title")));

        // add follower label locally
        Label followersTotalNoLabel = new Label("followersTotalNoLabel", userService.countAllUsersByGivenFollowedAction(actionIteration.getAction()));
        followersTotalNoLabel.setOutputMarkupId(true);
        item.add(followersTotalNoLabel);

        // actionIteration contributors
        Label contributors = new Label("contributors",
                activityService.countAllActionContributorsBasedOnActionActivity(actionIteration.getAction()));
        item.add(contributors.setOutputMarkupId(true));

        // actionIteration comments label
        Label actionIterationCommentLabel;
        int commentSize = 0;
        if (actionIteration.getComments() != null || !actionIteration.getComments().isEmpty()) {
            commentSize = actionIteration.getComments().size();
        }
        actionIterationCommentLabel = new Label("actionIterationCommentLabel", Model.of(commentSize));
        actionIterationCommentLabel.setOutputMarkupId(true);
        item.add(actionIterationCommentLabel);

        item.add(new Label("thumbsUpVotes", actionIteration.getNoOfUpVotes()));

        // actionIteration process column
        item.add(new Label("processStatus", new StringResourceModel("action.iteration.text", this, null).getString() + " "
                + actionService.getIterationNumber(actionIteration.getAction().getId(), actionIteration)));

        // Leader
        AjaxLink<Void> link = userImageLink("userProfileDetailLink", actionIteration.getPostedBy());
        link.add(new NonCachingImage("leader_img", ProfilePictureResource.get(PictureType.THUMBNAIL,
                actionIteration.getPostedBy())));
        item.add(link);
        item.add(new Label("leader_name", actionIteration.getPostedBy().getNickName()));
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
     * @param actionIteration
     * @return
     */
    private Check<ActionIteration> newSelectionCheck(final ActionIteration actionIteration) {
        Check<ActionIteration> check = new Check<ActionIteration>("actionIterationCheck", new Model<>(actionIteration),
                actionIterationGroup) {

            private static final long serialVersionUID = -5802376197291247523L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        return check;
    }

    protected NonCachingImage newActionIterationProfilePicturePreview(ActionIteration actionIteration) {
        actionIterationProfilePicture = new NonCachingImage("profilePicturePreview", ActionIterationPictureResource.get(
                PictureType.PROFILE, actionIteration));
        actionIterationProfilePicture.setOutputMarkupId(true);
        return actionIterationProfilePicture;
    }

    public CheckGroup<ActionIteration> getActionIterationGroup() {
        return actionIterationGroup;
    }

    /**
     * @author ATB
     */
    private final class ActionIterationProvider extends EntityProvider<ActionIteration> {
        private static final long serialVersionUID = -1727094205049792307L;

        @Override
        public Iterator<? extends ActionIteration> iterator(long first, long count) {
            List<ActionIteration> actionIterations = new LinkedList<>();
            // based on status, call different methods
            if (processType.equals(TypeOfprocess.Action_Iteration) || processType.equals(TypeOfprocess.All)) {
                // all actionIterations
                actionIterations = actionIterationService
                        .getAll(Long.valueOf(first).intValue(), Long.valueOf(count).intValue());
            }
            return actionIterations.iterator();
        }

        @Override
        public long size() {
            if (processType.equals(TypeOfprocess.Action_Iteration) || processType.equals(TypeOfprocess.All)) {
                return actionIterationService.countAll();
            }
            return 0;
        }

    }
}
