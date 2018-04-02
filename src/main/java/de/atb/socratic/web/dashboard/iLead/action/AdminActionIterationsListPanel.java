package de.atb.socratic.web.dashboard.iLead.action;

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
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.ActionIterationState;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.action.detail.ActionIterationDetailPage;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.ActionIterationPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class AdminActionIterationsListPanel extends GenericPanel<Action> {

    private static final long serialVersionUID = 1242441524328939708L;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    ActionService actionService;

    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many Iterations do we show per page
    private static final int itemsPerPage = 10;

    // container holding the list of Iterations
    private final WebMarkupContainer iterationsContainer;

    private StyledFeedbackPanel feedbackPanel;

    private IterationProvider iterationProvider;

    private NonCachingImage iterationProfilePicture;
    private final Action theAction;

    // Repeating view showing the list of existing iterations
    private final DataView<ActionIteration> iterationsRepeater;

    public AdminActionIterationsListPanel(final String id, final IModel<Action> model, final StyledFeedbackPanel feedbackPanel) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;
        this.theAction = model.getObject();

        // add container with list of existing actions
        iterationsContainer = new WebMarkupContainer("iterationsContainer");
        add(iterationsContainer.setOutputMarkupId(true));

        // add repeating view with list of existing actions
        iterationProvider = new IterationProvider();
        iterationsRepeater = new DataView<ActionIteration>("iterations", iterationProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<ActionIteration> item) {
                item.setOutputMarkupId(true);
                AdminActionIterationsListPanel.this.populateItem(item, item.getModelObject());
            }
        };
        iterationsContainer.add(iterationsRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", iterationsRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(iterationsRepeater.getPageCount() > 1);
            }
        });

    }

    /**
     * @return
     */
    private BookmarkablePageLink<AdminActionIterationAddEditPage> newActionIterationEditLink(final PageParameters parameter) {
        final BookmarkablePageLink<AdminActionIterationAddEditPage> editLink = new BookmarkablePageLink<AdminActionIterationAddEditPage>(
                "viewEditLink", AdminActionIterationAddEditPage.class, parameter) {
            private static final long serialVersionUID = 1L;
        };
        editLink.setOutputMarkupId(true);

        editLink.add(new Label("iteration.viewEditButton.Label", "Edit"));
        return editLink;
    }

    /**
     * @return
     */
    private BookmarkablePageLink<ActionIterationDetailPage> newActionIterationViewLink(final PageParameters parameter) {
        BookmarkablePageLink<ActionIterationDetailPage> viewLink = new BookmarkablePageLink<ActionIterationDetailPage>(
                "viewEditLink", ActionIterationDetailPage.class, parameter) {
            private static final long serialVersionUID = 1L;
        };
        viewLink.setOutputMarkupId(true);
        viewLink.add(new Label("iteration.viewEditButton.Label", "View"));
        return viewLink;
    }

    protected void populateItem(final WebMarkupContainer item, final ActionIteration iteration) {
        final PageParameters params = new PageParameters().set("id", iteration.getId());
        item.setOutputMarkupId(true);
        item.add(newIterationProfilePicturePreview(iteration));
        item.add(new Label("title", new PropertyModel<String>(iteration, "title")));
        item.add(new Label("iterationStateLabel", new PropertyModel<String>(iteration, "state")));
        item.add(new Label("iterationNumberLabel", actionService.getIterationNumber(theAction.getId(), iteration)));

        if (!iteration.getState().equals(ActionIterationState.Finished)) {
            item.add(newActionIterationEditLink(new PageParameters().set("iterationId", iteration.getId()).set("id",
                    theAction.getId())));
        } else {
            item.add(newActionIterationViewLink(new PageParameters().set("iterationId", iteration.getId()).set("id",
                    theAction.getId())));
        }

        //Add number of iteration comments 
        Label iterationCommentLabel;
        int commentSize = 0;
        if (iteration.getComments() != null || !iteration.getComments().isEmpty()) {
            commentSize = iteration.getComments().size();
        }
        iterationCommentLabel = new Label("iterationCommentLabel", Model.of(commentSize));
        iterationCommentLabel.setOutputMarkupId(true);
        item.add(iterationCommentLabel);

        item.add(new Label("thumbsUpVotes", iteration.getNoOfUpVotes()));
    }

    protected NonCachingImage newIterationProfilePicturePreview(ActionIteration actionIteration) {
        iterationProfilePicture = new NonCachingImage("profilePicturePreview", ActionIterationPictureResource.get(
                PictureType.PROFILE, actionIteration));
        iterationProfilePicture.setOutputMarkupId(true);
        return iterationProfilePicture;
    }

    /**
     * @return
     */
    private WebMarkupContainer newIterationsContainer() {
        return new WebMarkupContainer("iterationsContainer") {
            private static final long serialVersionUID = -5090615480759122784L;

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
            }
        };
    }

    /**
     * @author ATB
     */
    private final class IterationProvider extends EntityProvider<ActionIteration> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends ActionIteration> iterator(long first, long count) {

            List<ActionIteration> allTopicCampaigns = actionService.getAllActionIterationsByDescendingCreationDate(
                    theAction.getId(), Long.valueOf(first).intValue(), Long.valueOf(count).intValue());

            return allTopicCampaigns.iterator();
        }

        @Override
        public long size() {
            return actionService.countAllActionIterationsByDescendingCreationDate(theAction.getId());
        }
    }
}
