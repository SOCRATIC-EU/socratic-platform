/**
 *
 */
package de.atb.socratic.web.inception.idea;

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
import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.BusinessModel;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.ActionBusinessModelService;
import de.atb.socratic.service.inception.ActionIterationService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.CSSAppender;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.security.login.LoginPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @param <T>
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class VotingPanel<T> extends GenericPanel<T> implements IVotable {

    /**
     *
     */
    private static final long serialVersionUID = 5838418435916266078L;

    // inject the conversation
    @Inject
    protected Conversation conversation;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // inject the EJB for managing ideas
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    IdeaService ideaService;

    @EJB
    CampaignService campaignService;

    @EJB
    ActionService actionService;

    @EJB
    ActionIterationService actionIterationService;

    @EJB
    ActionBusinessModelService actionBusinessModelService;

    @EJB
    UserService userService;

    private Idea theIdea;

    private Campaign theCampaign;

    private Action theAction;

    private ActionIteration theIteration;

    private BusinessModel theBusinessModel;

    private Label upVotes;
    IModel<T> model;

    public VotingPanel(final String id, final IModel<T> model) {
        super(id, model);
        this.setOutputMarkupId(true);

        this.model = model;
        // join conversation...
        if (conversation.isTransient()) {
            conversation.begin();
        }

        if (model.getObject() instanceof Campaign) {
            theCampaign = (Campaign) getModelObject();
            upVotes = new Label("upVotes", Model.of(theCampaign.getNoOfUpVotes()));
        } else if (model.getObject() instanceof Idea) {
            theIdea = (Idea) getModelObject();
            upVotes = new Label("upVotes", Model.of(theIdea.getNoOfUpVotes()));
        } else if (model.getObject() instanceof Action) {
            theAction = (Action) getModelObject();
            upVotes = new Label("upVotes", Model.of(theAction.getNoOfUpVotes()));
        } else if (model.getObject() instanceof ActionIteration) {
            theIteration = (ActionIteration) getModelObject();
            upVotes = new Label("upVotes", Model.of(theIteration.getNoOfUpVotes()));
        } else if (model.getObject() instanceof BusinessModel) {
            theBusinessModel = (BusinessModel) getModelObject();
            upVotes = new Label("upVotes", Model.of(theBusinessModel.getNoOfUpVotes()));
        }

        AjaxLink<T> thumbsUpLink = new AjaxLink<T>("thumbsUp", model) {
            private static final long serialVersionUID = 3382441892828917997L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (loggedInUser == null) {
                    setResponsePage(LoginPage.class);
                    return;
                }
                if (model.getObject() instanceof Campaign) {
                    campaignService.voteUp(theCampaign, loggedInUser.getId());
                    upVotes.setDefaultModelObject(theCampaign.getNoOfUpVotes());
                } else if (model.getObject() instanceof Idea) {
                    ideaService.voteUp(theIdea, loggedInUser.getId());
                    upVotes.setDefaultModelObject(theIdea.getNoOfUpVotes());
                } else if (model.getObject() instanceof Action) {
                    actionService.voteUp(theAction, loggedInUser.getId());
                    upVotes.setDefaultModelObject(theAction.getNoOfUpVotes());
                } else if (model.getObject() instanceof ActionIteration) {
                    actionIterationService.voteUp(theIteration, loggedInUser.getId());
                    upVotes.setDefaultModelObject(theIteration.getNoOfUpVotes());
                } else if (model.getObject() instanceof BusinessModel) {
                    actionBusinessModelService.voteUp(theBusinessModel, loggedInUser.getId());
                    upVotes.setDefaultModelObject(theBusinessModel.getNoOfUpVotes());
                }
                target.add(VotingPanel.this);
                if (votedForUp()) {
                    VotingPanel.this.voteUpOnClick(target, VotingPanel.this);

                    // once user clicks votedUp, increase no of likes given by loggedInUser
                    userService.increaseNoOfLikesGiven(loggedInUser);

                    if (model.getObject() instanceof Campaign) {
                        // once challenge is liked, increase no of likes received by challenge leader
                        userService.increaseNoOfLikesReceived(theCampaign.getCreatedBy());
                    } else if (model.getObject() instanceof Idea) {
                        // once Idea is liked, increase no of likes received for idea leader
                        userService.increaseNoOfLikesReceived(theIdea.getPostedBy());
                    } else if (model.getObject() instanceof Action) {
                        // once action is liked, increase no of likes received by action leader
                        userService.increaseNoOfLikesReceived(theAction.getPostedBy());
                    } else if (model.getObject() instanceof ActionIteration) {
                        // once iteration is liked, increase no of likes received by theIteration leader
                        userService.increaseNoOfLikesReceived(theIteration.getPostedBy());
                    } else if (model.getObject() instanceof BusinessModel) {
                        // once business model is liked, increase no of likes received by action leader
                        theAction = actionService.getActionFromBusinessModel(theBusinessModel);
                        userService.increaseNoOfLikesReceived(theAction.getPostedBy());
                    }

                } else {
                    VotingPanel.this.voteDownOnClick(target, VotingPanel.this);

                    // once user clicks votedDown, decrease no of likes given by loggedInUser
                    userService.decreaseNoOfLikesGiven(loggedInUser);

                    if (model.getObject() instanceof Campaign) {
                        // once challenge is un-liked, decrease no of likes received by challenge leader
                        userService.decreaseNoOfLikesReceived(theCampaign.getCreatedBy());
                    } else if (model.getObject() instanceof Idea) {
                        // once idea is un-liked, decrease no of likes received by idea leader
                        userService.decreaseNoOfLikesReceived(theIdea.getPostedBy());
                    } else if (model.getObject() instanceof Action) {
                        // once action is un-liked, decrease no of likes received by action leader
                        userService.decreaseNoOfLikesReceived(theAction.getPostedBy());
                    } else if (model.getObject() instanceof ActionIteration) {
                        // once iteration is un-liked, decrease no of likes received by theIteration leader
                        userService.decreaseNoOfLikesReceived(theIteration.getPostedBy());
                    } else if (model.getObject() instanceof BusinessModel) {
                        // once business model is un-liked, decrease no of likes received by action leader
                        theAction = actionService.getActionFromBusinessModel(theBusinessModel);
                        userService.decreaseNoOfLikesReceived(theAction.getPostedBy());
                    }

                }
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (loggedInUser != null) {
                    setEnabled(isButtonEnabled());
                    if (votedForUp()) {
                        add(new CSSAppender(Model.of("votingThumbGreen")));
                    }
                } else {
                    // let user see an active like button and if he presses it when logged out then redirect user to LoginPage
                    setEnabled(true);
                }
            }
        };
        thumbsUpLink.setOutputMarkupId(true);
        add(thumbsUpLink);
        add(upVotes.setOutputMarkupId(true));
    }

    private boolean votedForUp() {
        if (model.getObject() instanceof Campaign) {
            return theCampaign.getUpVotes().contains(loggedInUser.getId());
        } else if (model.getObject() instanceof Idea) {
            return theIdea.getUpVotes().contains(loggedInUser.getId());
        } else if (model.getObject() instanceof Action) {
            return theAction.getUpVotes().contains(loggedInUser.getId());
        } else if (model.getObject() instanceof ActionIteration) {
            return theIteration.getUpVotes().contains(loggedInUser.getId());
        } else if (model.getObject() instanceof BusinessModel) {
            return theBusinessModel.getUpVotes().contains(loggedInUser.getId());
        }
        return false;
    }

    /**
     * only enable buttons if campaign is active and user hasn't voted yet
     *
     * @return
     */
    private boolean isButtonEnabled() {
        if (model.getObject() instanceof Campaign) {
            return theCampaign.getDefinitionActive() != null
                    && theCampaign.getDefinitionActive()
                    // users should not be able to vote for their own challenges
                    && !theCampaign.getCreatedBy().equals(loggedInUser);
        } else if (model.getObject() instanceof Idea) {
            return theIdea.getCampaign().getIdeationActive() != null
                    && theIdea.getCampaign().getIdeationActive()
                    // issue EFF-525: users should not be able to vote for their own ideas
                    && !theIdea.getPostedBy().equals(loggedInUser);
        } else if (model.getObject() instanceof Action) {
            return !theAction.getPostedBy().equals(loggedInUser);
        } else if (model.getObject() instanceof ActionIteration) {
            return !theIteration.getPostedBy().equals(loggedInUser);
        } else if (model.getObject() instanceof BusinessModel) {
            return !actionService.getActionFromBusinessModel(theBusinessModel).getPostedBy().equals(loggedInUser);
        }

        return false;
    }
}
