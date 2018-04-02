package de.atb.socratic.web.dashboard;

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

import javax.inject.Inject;

import de.atb.socratic.model.User;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.iLead.UserLeadDashboardPage;
import de.atb.socratic.web.dashboard.iParticipate.UserParticipationDashboardPage;
import de.atb.socratic.web.dashboard.message.AdminMessageInboxPage;
import de.atb.socratic.web.dashboard.settings.UserSettingsDashboardPage;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.rating.RatingPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.io.IClusterable;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class Dashboard extends BasePage {
    private static final long serialVersionUID = -9092430357787574242L;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    protected User loggedInUser;
    RatingPanel panel;

    protected final Link<UserDashboardPage> userDashboardPageLink;
    protected final Link<UserParticipationDashboardPage> userParticipationDashboardPageLink;
    protected final Link<UserLeadDashboardPage> userLeadDashboardPageLink;
    protected final Link<UserSettingsDashboardPage> userSettingsDashboardPageLink;
    protected final Link<AdminMessageInboxPage> adminMessageInboxPageLink;

    public Dashboard(final PageParameters parameters) {
        super(parameters);

        AjaxLink<Void> link = userImageLink("link", loggedInUser);
        link.add(new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.PROFILE, loggedInUser)));
        add(link);

        add(new Label("nickName", new PropertyModel<String>(loggedInUser, "nickName")));

        add(new Label("city", new PropertyModel<String>(loggedInUser, "city")));
        add(new Label("country", new PropertyModel<String>(loggedInUser, "country")));

        // getUserRating here
        add(newRatingPanel(3));

        add(userDashboardPageLink = new BookmarkablePageLink<>("dashboard", UserDashboardPage.class));
        userDashboardPageLink.setOutputMarkupId(true);

        add(userParticipationDashboardPageLink =
                new BookmarkablePageLink<>("participation", UserParticipationDashboardPage.class));
        userParticipationDashboardPageLink.setOutputMarkupId(true);

        add(userLeadDashboardPageLink = new BookmarkablePageLink<>("lead", UserLeadDashboardPage.class));
        userLeadDashboardPageLink.setOutputMarkupId(true);

        add(userSettingsDashboardPageLink = new BookmarkablePageLink<>("settings", UserSettingsDashboardPage.class));
        userSettingsDashboardPageLink.setOutputMarkupId(true);

        add(adminMessageInboxPageLink = new BookmarkablePageLink<>("messages", AdminMessageInboxPage.class));
        adminMessageInboxPageLink.setOutputMarkupId(true);
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

    protected void activateCurrentTab(IHeaderResponse response, final String currentTabId) {
        // make current tab "active", all others "inactive"
        response.render(OnDomReadyHeaderItem.forScript("$('#tabs > li').removeClass('active');$('#" + currentTabId + "').addClass('active');"));
    }

    private RatingPanel newRatingPanel(int voteSize) {

        final double total = voteSize;
        final RatingModel totalVotes = new RatingModel(total);
        RatingPanel p = new RatingPanel("votingPanel", new PropertyModel<Double>(totalVotes, "rating"), 5, null, false) {

            private static final long serialVersionUID = -4520654999230191343L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(false);
            }

            @Override
            protected boolean onIsStarActive(int star) {
                return totalVotes.isActive(star);
            }

            @Override
            protected void onRated(int rating, AjaxRequestTarget target) {
                totalVotes.addRating(rating);

            }
        };
        p.setRatingLabelVisible(false);
        p.setEnabled(false);
        p.setOutputMarkupId(true);
        return p;
    }

    public class RatingModel implements IClusterable {

        private static final long serialVersionUID = -7181296497895432958L;

        private double rating;

        public RatingModel(double votes) {
            this.rating = votes;
        }

        public boolean isActive(int star) {
            return star < ((int) (rating + 0.5));
        }

        public void addRating(int nrOfStars) {
            rating = (double) nrOfStars;
        }

        public Double getRating() {
            if (Double.isNaN(rating)) {
                return 0.0;
            } else {
                return rating;
            }
        }
    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }

    public enum StateForDashboard {
        Lead,
        TakePart;
    }

    public enum EntitiySortingCriteria {
        created("entitiySortingCriteria.created"),
        lastChanged("entitiySortingCriteria.lastChanged");

        private String key;

        EntitiySortingCriteria(String key) {
            this.setKey(key);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
