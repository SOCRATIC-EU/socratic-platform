package de.atb.socratic.web.dashboard.iLead;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.action.ActionAddEditPage;
import de.atb.socratic.web.dashboard.Dashboard;
import de.atb.socratic.web.dashboard.iLead.action.AdminActionShowAllPage;
import de.atb.socratic.web.dashboard.iLead.challenge.AdminChallengeShowAllPage;
import de.atb.socratic.web.dashboard.iLead.idea.AdminIdeaShowAllPage;
import de.atb.socratic.web.dashboard.panels.ActionListPanel;
import de.atb.socratic.web.dashboard.panels.ChallengeListPanel;
import de.atb.socratic.web.dashboard.panels.IdeasListPanel;
import de.atb.socratic.web.inception.idea.IdeaDevelopmentPhase;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
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
public class UserLeadDashboardPage extends Dashboard {
    private static final long serialVersionUID = 1077916738572445762L;

    private final ChallengeListPanel challengeListPanel;
    private final IdeasListPanel ideaListPanel;
    private final ActionListPanel actionListPanel;

    @EJB
    IdeaService ideaService;

    @EJB
    UserService userService;

    @Inject
    ParticipateNotificationService participateNotifier;

    // how many challenges do we show initially
    private static final int challengesPerPage = 3;

    // how many ideas do we show initially
    private static final int ideasPerPage = 3;

    // how many actions do we show initially
    private static final int actionsPerPage = 3;

    @Inject
    @LoggedInUser
    protected User loggedInUser;

    // how many selected ideas do we show initially
    private static final int selectedIdeasPerPage = 1;

    private final DropDownChoice<EntitiySortingCriteria> challengeSortingCriteriaChoice;
    private final DropDownChoice<EntitiySortingCriteria> ideaSortingCriteriaChoice;
    private final DropDownChoice<EntitiySortingCriteria> actionSortingCriteriaChoice;

    public UserLeadDashboardPage(PageParameters parameters) {
        super(parameters);

        // load all ideas which are selected.
        add(selectedIdeasProvider(parameters));

        // add show all challenges link
        AjaxLink<AdminChallengeShowAllPage> adminChallengeShowAllPageLink = new AjaxLink<AdminChallengeShowAllPage>("showAllChallenges") {

            private static final long serialVersionUID = -2547036150493181142L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminChallengeShowAllPage.class, new PageParameters().set("state", StateForDashboard.Lead));
            }
        };
        add(adminChallengeShowAllPageLink);

        // Add sorting form
        Form<Void> challengeSortingCriteriaForm = new Form<Void>("challengeSortingCriteriaForm");
        add(challengeSortingCriteriaForm);
        challengeSortingCriteriaForm.add(challengeSortingCriteriaChoice = newSortingCriteriaDropDownChoice("challengeSortingCriteriaChoice"));

        // add panel with list of existing challenges
        challengeListPanel = new ChallengeListPanel("challengesList", Model.of(loggedInUser), feedbackPanel,
                StateForDashboard.Lead, challengesPerPage) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(challengeListPanel.setOutputMarkupId(true));

        // add show all ideas link
        AjaxLink<AdminIdeaShowAllPage> adminIdeaShowAllPageLink = new AjaxLink<AdminIdeaShowAllPage>("showAllIdeas") {

            private static final long serialVersionUID = -2547036150493181142L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminIdeaShowAllPage.class, new PageParameters().set("state", StateForDashboard.Lead));
            }
        };
        add(adminIdeaShowAllPageLink);

        // Add sorting form
        Form<Void> ideaSortingCriteriaForm = new Form<Void>("ideaSortingCriteriaForm");
        add(ideaSortingCriteriaForm);
        ideaSortingCriteriaForm.add(ideaSortingCriteriaChoice = newSortingCriteriaDropDownChoice("ideaSortingCriteriaChoice"));

        // add panel with list of existing ideas
        ideaListPanel = new IdeasListPanel("ideasList", Model.of(loggedInUser), feedbackPanel, StateForDashboard.Lead,
                ideasPerPage) {
            private static final long serialVersionUID = -4602672523322280660L;
        };
        add(ideaListPanel.setOutputMarkupId(true));

        // add show all actions link
        AjaxLink<AdminActionShowAllPage> adminActionShowAllPageLink = new AjaxLink<AdminActionShowAllPage>("showAllActions") {

            private static final long serialVersionUID = -2547036150493181142L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminActionShowAllPage.class, new PageParameters().set("state", StateForDashboard.Lead));
            }
        };
        add(adminActionShowAllPageLink);

        // Add sorting form
        Form<Void> actionSortingCriteriaForm = new Form<Void>("actionSortingCriteriaForm");
        add(actionSortingCriteriaForm);
        actionSortingCriteriaForm.add(actionSortingCriteriaChoice = newSortingCriteriaDropDownChoice("actionSortingCriteriaChoice"));

        // add panel with list of existing action
        actionListPanel = new ActionListPanel("actionsList", Model.of(loggedInUser), feedbackPanel, StateForDashboard.Lead, actionsPerPage) {
            private static final long serialVersionUID = 6393614856591095877L;
        };
        add(actionListPanel.setOutputMarkupId(true));
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "leadTab");
    }

    /**
     * @return
     */
    private DropDownChoice<EntitiySortingCriteria> newSortingCriteriaDropDownChoice(String wicketId) {
        final DropDownChoice<EntitiySortingCriteria> sortingCriteriaChoice = new DropDownChoice<>(wicketId,
                new Model<EntitiySortingCriteria>(), Arrays.asList(EntitiySortingCriteria.values()),
                new IChoiceRenderer<EntitiySortingCriteria>() {
                    private static final long serialVersionUID = -3507943582789662873L;

                    @Override
                    public Object getDisplayValue(EntitiySortingCriteria object) {
                        return new StringResourceModel(object.getKey(), UserLeadDashboardPage.this, null).getString();
                    }

                    @Override
                    public String getIdValue(EntitiySortingCriteria object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(EntitiySortingCriteria.created);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = 4630143654574571697L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateComponents(target);
            }
        });
        return sortingCriteriaChoice;
    }

    /**
     * @param target
     */
    private void updateComponents(AjaxRequestTarget target) {
        target.add(challengeListPanel.setSortingCriteria(challengeSortingCriteriaChoice.getModelObject()));
        target.add(ideaListPanel.setSortingCriteria(ideaSortingCriteriaChoice.getModelObject()));
        target.add(actionListPanel.setSortingCriteria(actionSortingCriteriaChoice.getModelObject()));
    }

    private WebMarkupContainer selectedIdeasProvider(final PageParameters parameters) {
        // add container with list of existing ideas
        WebMarkupContainer ideasContainer = new WebMarkupContainer("ideasContainer");
        add(ideasContainer.setOutputMarkupId(true));

        // add repeating view with list of existing ideas
        IdeaProvider ideaProvider = new IdeaProvider(loggedInUser);
        final DataView<Idea> ideasRepeater = new DataView<Idea>("ideasRepeater", ideaProvider, selectedIdeasPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Idea> item) {
                item.setOutputMarkupId(true);
                //item.add(new Label("congratulations", "Congratulations!"));
                item.add(new Label("titleOfIdea", new PropertyModel<>(item.getModelObject(), "shortText")));
                //item.add(new Label("normalText", " has been selected as a promising solution. \n Let's go to the action!!"));
                parameters.set("ideaId", item.getModelObject().getId());
                item.add(newCreateActionLink(parameters));
                item.add(newCancelActionLink(item.getModelObject()));
            }
        };
        ideasContainer.add(ideasRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", ideasRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(ideasRepeater.getPageCount() > 1);
            }
        });

        return ideasContainer;
    }

    private Link<ActionAddEditPage> newCreateActionLink(final PageParameters parameters) {
        return new Link<ActionAddEditPage>("createAction") {
            private static final long serialVersionUID = -310533532532643267L;

            @Override
            public void onClick() {
                setResponsePage(ActionAddEditPage.class, parameters);
            }
        };
    }

    private Link<Void> newCancelActionLink(final Idea idea) {
        return new Link<Void>("cancelAction") {
            private static final long serialVersionUID = -310533532532643267L;

            @Override
            public void onClick() {
                // 1. set idea development phase to OnHalt
                idea.setIdeaPhase(IdeaDevelopmentPhase.OnHalt);

                // 2. Save idea
                ideaService.update(idea);

                // 3. Send notification to CO and followers of challenge and idea
                // send notification to all idea follower and challenge follower
                List<User> followers = userService.getAllUsersByGivenFollowedChallenge(idea.getCampaign(), Integer.MAX_VALUE, Integer.MAX_VALUE);
                followers.addAll(userService.getAllUsersByGivenFollowedIdea(idea, Integer.MAX_VALUE, Integer.MAX_VALUE));
                Set<User> noRepeatedUsers = new HashSet<>(followers);
                for (User follower : noRepeatedUsers) {
                    participateNotifier.addParticipationNotification(idea, follower, NotificationType.ACTION_REJECTION);
                }

                // 4. Refresh page
                setResponsePage(getPage());
            }
        };
    }

    /**
     * @author ATB
     */
    private final class IdeaProvider extends EntityProvider<Idea> {
        private static final long serialVersionUID = -1727094205049792307L;

        private final User user;

        public IdeaProvider(User user) {
            super();
            this.user = user;
        }

        @Override
        public Iterator<? extends Idea> iterator(long first, long count) {
            Collection<Idea> ideas = new LinkedList<>();
            ideas = ideaService.getAllSelectedIdeasByDescendingCreationDateAndPostedBy(Long.valueOf(first).intValue(), Long
                    .valueOf(count).intValue(), user);
            return ideas.iterator();
        }

        @Override
        public long size() {
            return ideaService.countSelectedIdeasForUser(user);
        }

    }
}
