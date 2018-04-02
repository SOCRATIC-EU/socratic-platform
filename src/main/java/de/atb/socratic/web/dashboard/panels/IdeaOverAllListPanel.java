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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import de.atb.socratic.web.dashboard.UserDashboardPage.Status;
import de.atb.socratic.web.dashboard.iLead.idea.AdminIdeaActivityPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.inception.idea.details.IdeaDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class IdeaOverAllListPanel extends GenericPanel<User> {

    /**
     *
     */
    private static final long serialVersionUID = -159659429992094182L;

    // inject the EJB for managing ideas
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    IdeaService ideaService;

    @EJB
    ActivityService activityService;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many ideas do we show initially
    private static final int itemsPerPage = 2;

    // container holding the list of ideas
    private final WebMarkupContainer ideasContainer;

    // Repeating view showing the list of existing ideas
    private final DataView<Idea> ideasRepeater;

    private final EntityProvider<Idea> ideaProvider;

    private EntitiySortingCriteria sortingCriteria = EntitiySortingCriteria.created; // by default

    public IdeaOverAllListPanel(final String id, final IModel<User> model, final Status status) {
        super(id, model);

        // get the user
        final User user = getModelObject();

        // add container with list of existing ideas
        ideasContainer = new WebMarkupContainer("ideasContainer");
        add(ideasContainer.setOutputMarkupId(true));

        // add repeating view with list of existing ideas
        ideaProvider = new IdeaProvider(user, status);
        ideasRepeater = new DataView<Idea>("ideasRepeater", ideaProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Idea> item) {
                item.setOutputMarkupId(true);
                IdeaOverAllListPanel.this.populateItem(item, item.getModelObject(), status);
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
     * @param sortingCriteria
     * @return
     */
    public IdeaOverAllListPanel setSortingCriteria(EntitiySortingCriteria sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
        return this;
    }

    private Link<IdeasPage> newExploreLink(final Idea idea, final Status dashboardState) {
        final Link<IdeasPage> link = new Link<IdeasPage>("exploreLink") {
            private static final long serialVersionUID = -310533532532643267L;

            @Override
            public void onClick() {
                if (Status.LEAD == dashboardState) {
                    setResponsePage(AdminIdeaActivityPage.class, new PageParameters().set("id", idea.getId()));
                } else {
                    setResponsePage(IdeaDetailsPage.class,
                            new PageParameters().set("id", idea.getCampaign().getId()).set("ideaId", idea.getId()));
                }
            }
        };
        final String key = Status.PARTICIPATE == dashboardState ? "explore.text" : "manage.text";
        link.add(new Label("exploreLinkLabel", new StringResourceModel(key, this, null)));
        return link;
    }

    /**
     * @return
     */
    public long getIdeaListSize() {
        return ideaProvider.size();
    }

    protected void populateItem(final WebMarkupContainer item,
                                final Idea idea,
                                final Status dashboardState) {

        item.setOutputMarkupId(true);
        item.add(new Label("shortText", new PropertyModel<String>(idea, "shortText")));
        item.add(new Label("elevatorPitch", new PropertyModel<String>(idea, "elevatorPitch")));
        item.add(newExploreLink(idea, dashboardState));
        item.add(new Label("exploreMsg", new PropertyModel<String>(idea, "callToAction")));
    }

    /**
     * @author ATB
     */
    private final class IdeaProvider extends EntityProvider<Idea> {
        private static final long serialVersionUID = -1727094205049792307L;

        private final User user;
        private final Status status;

        public IdeaProvider(User user, Status status) {
            super();
            this.user = user;
            this.status = status;
        }

        @Override
        public Iterator<? extends Idea> iterator(long first, long count) {
            List<Idea> ideas = new LinkedList<>();
            // based on status, call different methods
            if (status.equals(Status.PARTICIPATE)) {
                ideas = activityService.getAllIdeasByIdeaActivityCreator(user, Long
                        .valueOf(first).intValue(), Long.valueOf(count).intValue(), sortingCriteria);
            } else if (status.equals(Status.LEAD)) {
                ideas = ideaService.getAllIdeasByDescendingCreationDateAndPostedBy(Long.valueOf(first).intValue(),
                        Long.valueOf(count).intValue(), user, sortingCriteria);
            } else if (status.equals(Status.RECOMMENDED)) {

            }
            return ideas.iterator();
        }

        @Override
        public long size() {
            if (status.equals(Status.PARTICIPATE)) {
                return activityService.countAllIdeasByIdeaActivityCreator(user);
            } else if (status.equals(Status.LEAD)) {
                return ideaService.countIdeasForUser(user);
            } else if (status.equals(Status.RECOMMENDED)) {
                return 0;
            }
            return 0;
        }

    }
}
