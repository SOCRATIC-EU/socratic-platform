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

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.authorization.strategies.annotations.AuthorizeInstantiation;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.other.TagService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.dashboard.iLead.UserLeadDashboardPage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.jboss.solder.logging.Logger;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@AuthorizeInstantiation({UserRole.MANAGER, UserRole.USER, UserRole.SUPER_ADMIN, UserRole.ADMIN})
public class ActionAddTeamPage extends BasePage {
    private static final long serialVersionUID = 1L;

    private final Form<Void> actionForm;
    // The action to edit/save
    private Action theAction;
    private Idea theIdea;

    @EJB
    ActivityService activityService;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    ActionService actionService;

    @EJB
    IdeaService ideaService;

    @EJB
    UserService userService;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    TagService tagService;

    @Inject
    Logger logger;

    private final ActionTeamMembersListPanel actionTeamMembersListPanel;
    private final ActionTeamMembersListPanel actionPotentialTeamMembersListPanel;

    public ActionAddTeamPage(PageParameters parameters) {
        super(parameters);

        // extract id parameter and set page title, header and action
        // depending on whether we are editing an existing action or creating
        // a new one
        loadAction(parameters.get("id"), parameters.get("ideaId"));

        // add form to create new action
        add(actionForm = newActionForm());

        actionForm.add(new Label("header", new StringResourceModel("form.create.entre.header", this, null)));

        // show list of team members
        actionTeamMembersListPanel = new ActionTeamMembersListPanel("teamMemberList", Model.of(theAction), feedbackPanel,
                TeamMemberRoles.TeamMember) {

            private static final long serialVersionUID = 8087201252939474804L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        actionForm.add(actionTeamMembersListPanel.setOutputMarkupId(true));

        // show list of team members
        actionPotentialTeamMembersListPanel = new ActionTeamMembersListPanel("potentialTeamMemberList", Model.of(theAction),
                feedbackPanel, TeamMemberRoles.PotentialTeamMember) {

            private static final long serialVersionUID = 3793743146136276235L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        actionForm.add(actionPotentialTeamMembersListPanel.setOutputMarkupId(true));

        // add a button to create new campaign
        actionForm.add(newSubmitLink("submitTop"));
        actionForm.add(newSubmitLink("submitBottom"));

        // add cancel button to return to campaign list page
        actionForm.add(newCancelLink());
    }

    /**
     * @param actionId
     */
    protected void loadAction(final StringValue actionId, final StringValue ideaId) {
        try {
            theAction = actionService.getById(actionId.toOptionalLong());
            theIdea = ideaService.getById(ideaId.toOptionalLong());
        } catch (Exception e) {
            throw new RestartResponseException(ErrorPage.class);
        }
    }

    /**
     * @return
     */
    private Form<Void> newActionForm() {
        Form<Void> form = new InputValidationForm<Void>("form") {
            private static final long serialVersionUID = -7704060490162231565L;

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
            }
        };
        form.setOutputMarkupId(true);
        return form;
    }

    private AjaxSubmitLink newSubmitLink(String id) {
        return new AjaxSubmitLink(id, actionForm) {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                this.add(new AttributeModifier("value", new StringResourceModel("actions.continue.button", this, null)
                        .getString()));
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                save(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                showErrors(target);
            }
        };
    }

    /**
     * @return
     */
    private Link<UserLeadDashboardPage> newCancelLink() {
        return new Link<UserLeadDashboardPage>("cancel") {
            private static final long serialVersionUID = -310533532532643267L;

            @Override
            public void onClick() {
                setResponsePage(UserLeadDashboardPage.class);
            }
        };
    }

    /**
     *
     */
    protected void save(AjaxRequestTarget target) {
        submitAction(target);
        // success message has to be associated to session so that it is shown
        // in the global feedback panel
        Session.get().success(new StringResourceModel("saved.message", this, Model.of(theAction)).getString());
        // redirect to actionPage
        setResponsePage(ActionIterationAddPage.class, new PageParameters().set("actionid", theAction.getId()));
    }

    private void submitAction(AjaxRequestTarget target) {
        updateAction(target);
    }

    /**
     * @param target
     */
    private void updateAction(AjaxRequestTarget target) {
        // update action
        theAction = actionService.update(theAction);

        // show feedback panel
        target.add(feedbackPanel);
    }


    /**
     * @param target
     */
    protected void showErrors(AjaxRequestTarget target) {
        // in case of errors (e.g. validation errors) show error
        // messages in form
        target.add(actionForm);
        // also reload feedback panel
        target.add(feedbackPanel);

    }

    public enum TeamMemberRoles {
        TeamMember, // is already a member of Action Team
        PotentialTeamMember,    // can be candidate for Action Team
    }
}
