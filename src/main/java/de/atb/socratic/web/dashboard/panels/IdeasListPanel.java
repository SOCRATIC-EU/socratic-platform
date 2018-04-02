package de.atb.socratic.web.dashboard.panels;

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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.IdeaPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import de.atb.socratic.web.dashboard.Dashboard.StateForDashboard;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class IdeasListPanel extends GenericPanel<User> {

    /**
     *
     */
    private static final long serialVersionUID = -159659429992094182L;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    IdeaService ideaService;

    @EJB
    ActivityService activityService;

    // container holding the list of ideas
    private final WebMarkupContainer ideasContainer;

    // Repeating view showing the list of existing ideas
    private final DataView<Idea> ideasRepeater;

    private final EntityProvider<Idea> ideaProvider;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private NonCachingImage ideaProfilePicture;

    private EntitiySortingCriteria sortingCriteria = EntitiySortingCriteria.created; // by default

    @Inject
    @LoggedInUser
    protected User loggedInUser;

    // private IdeaSortingCriteria sortingCriteria;

    public static final String IDEA_ANCHOR_PREFIX = "_idea_";
    final User user;

    public IdeasListPanel(final String id, final IModel<User> model, final StyledFeedbackPanel feedbackPanel,
                          final StateForDashboard keyword, final int itemsPerPage) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;

        // get the user
        user = getModelObject();

        // add container with list of existing ideas
        ideasContainer = new WebMarkupContainer("ideasContainer");
        add(ideasContainer.setOutputMarkupId(true));

        // add repeating view with list of existing ideas
        ideaProvider = new IdeaProvider(user, keyword);
        ideasRepeater = new DataView<Idea>("ideasRepeater", ideaProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Idea> item) {
                item.setOutputMarkupId(true);
                IdeasListPanel.this.populateItem(item, item.getModelObject(), keyword);
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
    }

    /**
     * @return
     */
    public long getIdeaListSize() {
        return ideaProvider.size();
    }

    public Component newIdeaAnchor(final Long id) {
        return new WebMarkupContainer("ideaAnchorMan").setMarkupId(IDEA_ANCHOR_PREFIX + id)
                .setOutputMarkupId(true);
    }

    protected void populateItem(final WebMarkupContainer item, final Idea idea, final StateForDashboard keyword) {
        item.setOutputMarkupId(true);
        item.add(newIdeaProfilePicturePreview(idea));
        item.add(newIdeaAnchor(idea.getId()));
        // change wiket id in html
        item.add(newIdeaPanel("ideaPanel", item, idea, keyword));
    }

    protected NonCachingImage newIdeaProfilePicturePreview(Idea idea) {
        ideaProfilePicture = new NonCachingImage("profilePicturePreview", IdeaPictureResource.get(PictureType.PROFILE, idea));
        ideaProfilePicture.setOutputMarkupId(true);
        return ideaProfilePicture;
    }

    /**
     * @param idea
     * @return
     */
    public IdeasPanel newIdeaPanel(final String id, final MarkupContainer item, final Idea idea, final StateForDashboard dashboardState) {
        return new IdeasPanel(id, Model.of(idea), dashboardState) {
            private static final long serialVersionUID = -1078593562271992866L;
        };
    }

    /**
     * @param sortingCriteria
     * @return
     */
    public IdeasListPanel setSortingCriteria(EntitiySortingCriteria sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
        return this;
    }

    /**
     * @author ATB
     */
    private final class IdeaProvider extends EntityProvider<Idea> {
        private static final long serialVersionUID = -1727094205049792307L;

        private final User user;
        private final StateForDashboard keyword;

        public IdeaProvider(User user, StateForDashboard keyword) {
            super();
            this.user = user;
            this.keyword = keyword;
        }

        @Override
        public Iterator<? extends Idea> iterator(long first, long count) {
            Collection<Idea> ideas = new LinkedList<>();
            if (keyword.equals(StateForDashboard.Lead)) {
                ideas = ideaService.getAllIdeasByDescendingCreationDateAndPostedBy(Long.valueOf(first).intValue(), Long
                        .valueOf(count).intValue(), user, sortingCriteria);
            } else if (keyword.equals(StateForDashboard.TakePart)) {
                ideas = activityService.getAllIdeasByIdeaActivityCreator(user, Long.valueOf(first).intValue(), Long
                        .valueOf(count).intValue(), sortingCriteria);
            }
            return ideas.iterator();
        }

        @Override
        public long size() {
            if (keyword.equals(StateForDashboard.Lead)) {
                return ideaService.countIdeasForUser(user);
            } else if (keyword.equals(StateForDashboard.TakePart)) {
                return activityService.countAllIdeasByIdeaActivityCreator(user);
            }
            return 0;
        }

    }

}
