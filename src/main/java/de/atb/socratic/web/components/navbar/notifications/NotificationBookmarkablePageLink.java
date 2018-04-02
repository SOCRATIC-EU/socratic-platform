package de.atb.socratic.web.components.navbar.notifications;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.agilecoders.wicket.markup.html.bootstrap.image.Icon;
import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.notification.Notification;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.CSSAppender;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import org.apache.wicket.Page;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * NotificationBookmarkablePageLink
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public abstract class NotificationBookmarkablePageLink<T extends Notification> extends Link<T> {

    private static final long serialVersionUID = 9134635901931928968L;

    // what about the locale?! Auto-detect?
    protected static DateFormat df = new SimpleDateFormat("E, dd. MMMM yyyy", Locale.ENGLISH);

    /**
     * The page class that this link links to.
     */
    protected final String pageClassName;

    /**
     * The parameters to pass to the class constructor when instantiated.
     */
    protected PageParameters parameters;

    protected Icon icon;

    public <C extends Page> NotificationBookmarkablePageLink(String id, Class<C> pageClass, IModel<T> model) {
        this(id, pageClass, new PageParameters(), model);
    }

    public <C extends Page> NotificationBookmarkablePageLink(final String id, final Class<C> pageClass, final PageParameters parameters,
                                                             IModel<T> model) {
        super(id, model);
        this.parameters = parameters;

        if (pageClass == null) {
            throw new IllegalArgumentException("Page class for bookmarkable link cannot be null");
        } else if (!Page.class.isAssignableFrom(pageClass)) {
            throw new IllegalArgumentException("Page class must be derived from " + Page.class.getName());
        }
        pageClassName = pageClass.getName();

        final String className = (model != null) && (model.getObject() != null) ? model.getObject().getClass().getSimpleName() : this
                .getClass().getSimpleName();
        add(CSSAppender.append("class", "notification"));
        add(CSSAppender.append("class", className));
        TransparentWebMarkupContainer container = new TransparentWebMarkupContainer("notificationContainer");
        add(container);
        WebMarkupContainer link = new WebMarkupContainer("link");
        Notification n = (Notification) model.getObject();
        Date readDate = n.getReadDate();
        if (readDate == null) {
            link.add(CSSAppender.append("class", "newNotif"));
        }
        container.add(link);
        Date createDate = n.getCreationDate();
        if (createDate == null) {
            link.add(new Label("campaignDate", df.format(new Date())));
        } else {
            link.add(new Label("campaignDate", df.format(createDate)));
        }
        container.add(icon = new Icon("icon", new IconType("group")));

    }

    public NotificationBookmarkablePageLink<T> setIcon(IconType iconType) {
        icon.setType(iconType);
        return this;
    }

    public NotificationBookmarkablePageLink<T> setIcon(String iconName) {
        icon.setType(new IconType(iconName));
        return this;
    }

    /**
     * This method should only return phase based on challenge creation. It should either return Definition or Ideation. Once
     * challenge has been created and moved to next stages method should not change challenge phase.
     *
     * @param campaign
     * @return
     */
    protected static Class<? extends BasePage> getTargetPageClassForCampaign(Campaign campaign) {
        if (campaign.getOpenForDiscussion()) {
            return ChallengeDefinitionPage.class;
        } else {
            return IdeasPage.class;
        }
    }

    protected static PageParameters forCampaign(Campaign campaign) {
        return new PageParameters().set("id", campaign.getId());
    }

    /**
     * @return page parameters
     */
    public PageParameters getPageParameters() {
        if (parameters == null) {
            parameters = new PageParameters();
        }
        return parameters;
    }

    public void setPageParameters(PageParameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Get the page class registered with the link
     *
     * @return Page class
     */
    public final Class<? extends Page> getPageClass() {
        return WicketObjects.resolveClass(pageClassName);
    }

    /**
     * Whether this link refers to the given page.
     *
     * @param page the page
     * @see org.apache.wicket.markup.html.link.Link#linksTo(org.apache.wicket.Page)
     */
    @Override
    public boolean linksTo(final Page page) {
        return page.getClass() == getPageClass();
    }

    @Override
    protected boolean getStatelessHint() {
        return true;
    }

    /**
     * THIS METHOD IS NOT USED! Bookmarkable links do not have a click handler.
     * It is here to satisfy the interface only, as bookmarkable links will be
     * dispatched by the handling servlet.
     *
     * @see org.apache.wicket.markup.html.link.Link#onClick()
     */
    @Override
    public final void onClick() {
        // Bookmarkable links do not have a click handler.
        // Instead they are dispatched by the request handling servlet.
    }

    /**
     * Gets the url to use for this link.
     *
     * @return The URL that this link links to
     * @see org.apache.wicket.markup.html.link.Link#getURL()
     */
    @Override
    protected CharSequence getURL() {
        PageParameters parameters = getPageParameters();

        return urlFor(getPageClass(), parameters);
    }

    @Override
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy() {
        return new PanelMarkupSourcingStrategy(true);
    }
}
