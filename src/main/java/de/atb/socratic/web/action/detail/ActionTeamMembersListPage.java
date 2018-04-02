package de.atb.socratic.web.action.detail;

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

import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ActionTeamMembersListPage extends BasePage {

    private static final long serialVersionUID = 7378255719158818040L;

    private Action action;

    @EJB
    ActionService actionService;

    // how many members do we show per page
    private static final int itemsPerPage = 20;

    // container holding the list of members
    private final WebMarkupContainer membersContainer;

    private TeamMemberProvider memberProvider;

    // Repeating view showing the list of existing members
    private final DataView<User> membersRepeater;

    final CommonActionResourceHeaderPanel<Action> headerPanel;

    public ActionTeamMembersListPage(final PageParameters parameters) {
        super(parameters);

        final Long actionId = parameters.get("id").toOptionalLong();
        if (actionId != null) {
            action = actionService.getById(parameters.get("id").toOptionalLong());
        } else {
            action = null;
        }

        headerPanel = new CommonActionResourceHeaderPanel<Action>("commonHeaderPanel", Model.of(action), feedbackPanel) {
            private static final long serialVersionUID = 4494582353460389258L;

            @Override
            protected void onFollowersUpdate(AjaxRequestTarget target) {
            }
        };
        add(headerPanel);

        // add container with list of existing members
        membersContainer = new WebMarkupContainer("membersContainer");
        add(membersContainer.setOutputMarkupId(true));

        // add repeating view with list of existing members
        memberProvider = new TeamMemberProvider();

        membersRepeater = new DataView<User>("members", memberProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<User> item) {
                item.setOutputMarkupId(true);
                ActionTeamMembersListPage.this.populateItem(item, item.getModelObject());
            }
        };
        membersContainer.add(membersRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", membersRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(membersRepeater.getPageCount() > 1);
            }
        });

    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "actionsTab");
    }

    protected void populateItem(final WebMarkupContainer item, final User user) {
        item.setOutputMarkupId(true);
        AjaxLink<Void> link = userImageLink("userProfileDetailLink", user);
        link.add(new NonCachingImage("con_img", ProfilePictureResource.get(PictureType.THUMBNAIL, user)));
        item.add(link);
        item.add(new Label("con_name", user.getNickName()));
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
     * @author ATB
     */
    private final class TeamMemberProvider extends EntityProvider<User> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends User> iterator(long first, long count) {

            List<User> allTopicCampaigns = actionService.getAllTeamMembersByAscFirstNameAndByAction(action, Long.valueOf(first)
                    .intValue(), Long.valueOf(count).intValue());

            return allTopicCampaigns.iterator();
        }

        @Override
        public long size() {
            return actionService.countTeamMembersByAscFirstNameAndByAction(action);
        }
    }
}
