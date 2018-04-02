package de.atb.socratic.web.selection;

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

import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.inception.idea.details.IdeaDetailsPage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class IdeaSelectionDetailsPage extends IdeaDetailsPage {

    @Inject
    @LoggedInUser
    private User loggedInUser;

    @EJB
    private IdeaService ideaService;

    @EJB
    private UserService userService;

    @EJB
    private CampaignService campaignService;

    private Idea idea;

    private final IdeaVotingPanel ideaVotingPanel;

    public IdeaSelectionDetailsPage(PageParameters parameters) {
        super(parameters);

        Long ideaId = parameters.get("ideaId").toOptionalLong();
        if (ideaId != null) {
            idea = ideaService.getById(ideaId);
        } else {
            error("Could not find challenge or idea!");
            // show proper error page
        }
        add(ideaVotingPanel = new IdeaVotingPanel("ideaVotingPanel", Model.of(idea), idea.getCampaign().getSelectionActive()) {
            private static final long serialVersionUID = 7745141044795733701L;

            @Override
            protected void onVotingPerformed(AjaxRequestTarget target) {
                // Update AjaxLink
                addFollowersLink.configure();
                target.add(addFollowersLink);

                // Update followersTotalNoLabel
                followersTotalNoLabel.setDefaultModelObject(userService.countAllUsersByGivenFollowedIdea(idea));
                target.add(followersTotalNoLabel);
            }
        });
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "challengesTab");
    }
}
