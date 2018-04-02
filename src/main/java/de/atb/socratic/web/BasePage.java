/**
 *
 */
package de.atb.socratic.web;

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

import java.util.Calendar;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import javax.servlet.http.Cookie;

import com.jcabi.manifests.Manifests;
import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonList;
import de.agilecoders.wicket.markup.html.bootstrap.button.dropdown.DropDownButton;
import de.agilecoders.wicket.markup.html.bootstrap.button.dropdown.MenuBookmarkablePageLink;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.markup.html.bootstrap.html.ChromeFrameMetaTag;
import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.AbstractNavbarComponent;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.Navbar.ComponentPosition;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.Navbar.Position;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarAjaxLink;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarComponents;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarDropDownButton;
import de.atb.socratic.authorization.strategies.metadata.IAuthorizationCondition;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.service.employment.EmploymentService;
import de.atb.socratic.service.inception.MessageService;
import de.atb.socratic.web.action.detail.ActionsListPage;
import de.atb.socratic.web.components.CSSAppender;
import de.atb.socratic.web.components.EFFBehavior;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.MenuDivider;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.navbar.NotificationDropDownMenu;
import de.atb.socratic.web.dashboard.UserDashboardPage;
import de.atb.socratic.web.dashboard.coordinator.messages.CoordinatorDashboardMessagesInboxPage;
import de.atb.socratic.web.dashboard.coordinator.overview.CoordinatorDashboardOverviewPage;
import de.atb.socratic.web.dashboard.iLead.UserLeadDashboardPage;
import de.atb.socratic.web.dashboard.iParticipate.UserParticipationDashboardPage;
import de.atb.socratic.web.dashboard.message.AdminMessageInboxPage;
import de.atb.socratic.web.dashboard.settings.UserSettingsDashboardPage;
import de.atb.socratic.web.impactStories.ScalingPage;
import de.atb.socratic.web.inception.CampaignsPage;
import de.atb.socratic.web.learningcenter.LearningCenterPresentation;
import de.atb.socratic.web.observatory.ObservatoryPage;
import de.atb.socratic.web.provider.LoggedInUserProvider;
import de.atb.socratic.web.security.login.LoginPage;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.https.RequireHttps;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@RequireHttps
public abstract class BasePage extends WebPage {

    private static final long serialVersionUID = 6323920767950541998L;

    public static final String MESSAGE_PARAM = "message";

    public static final String LEVEL_PARAM = "level";

    private static final String FEEDBACKPANEL_ID = "feedbackPanel";

    private static final String COOKIE_NAME = "cookieDisplyInformation";

    // inject a logger
    @Inject
    Logger logger;

    // inject the conversation
    @Inject
    protected Conversation conversation;

    @EJB
    EmploymentService employmentService;

    @EJB
    MessageService messageService;

    @Inject
    protected LoggedInUserProvider loggedInUserProvider;

    protected final StyledFeedbackPanel feedbackPanel;

    private Navbar navbar;

    protected WebMarkupContainer subNavContainer;

    /**
     * @param parameters
     */
    public BasePage(final PageParameters parameters) {
        // for IE compatibility ???
        add(new ChromeFrameMetaTag("chrome-frame"));

        // always add the bootstrap resources (css, js, icons)
        // add(new BootstrapResourcesBehavior());

        // add js and css for tags input
        add(new EFFBehavior());

        // begin conversation
        if (conversation.isTransient()) {
            conversation.begin();
        }

        // If the title is not set in any sub-page this will be the default
        setPageTitle(getPageTitleModel());

        // add the menu at the top
        navbar = newNavbar("navbar");
        add(navbar);

        subNavContainer = newSubNav();
        subNavContainer.setOutputMarkupId(true);
        add(subNavContainer);

        // add global feedback panel
        feedbackPanel = new StyledFeedbackPanel(FEEDBACKPANEL_ID, new ComponentFeedbackMessageFilter(this));
        add(feedbackPanel.setOutputMarkupId(true));

        // display any message that may exist in the parameters
        displayMessage(parameters);

        // display cookies information
        add(displayCookiesInformation());

        // display elements on the footer
        add(displayFooter());
    }

    protected void activateCurrentTab(IHeaderResponse response, final String currentTabId) {
        // make current tab "active", all others "inactive"
        response.render(OnDomReadyHeaderItem.forScript("$('#tabs > li').removeClass('active');$('#" + currentTabId + "').addClass('active');"));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        if (loggedInUserProvider.getLoggedInUser() != null) {
            logger.info("The user "
                    + loggedInUserProvider.getLoggedInUser().getNickName()
                    + " is on page: "
                    + getPage().getClass().getName());
        } else {
            logger.info("The user is not logged in on page: " + getPage().getClass().getName());
        }
    }

    /**
     * Requires the given condition to be authorized otherwise an exception is
     * thrown.
     *
     * @param condition the condition that is required to be authorized. If this
     *                  returns <code>false</code>, a
     *                  {@link UnauthorizedInstantiationException} will be thrown.
     * @throws UnauthorizedInstantiationException if {@link IAuthorizationCondition#isAuthorized()} returns
     *                                            <code>false</code>.
     */
    public void requireCondition(IAuthorizationCondition condition) {
        requireAllConditions(condition);
    }

    /**
     * Requires at least one of the given conditions to be authorized otherwise
     * an exception is thrown.
     *
     * @param conditions the list of conditions where at least one has to be
     *                   authorized.
     * @throws UnauthorizedInstantiationException if <b>NONE</b> of the given conditions return
     *                                            <code>true</code> (calling
     *                                            {@link IAuthorizationCondition#isAuthorized()}.
     */
    public void requireAtLeastOneCondition(IAuthorizationCondition... conditions) {
        for (IAuthorizationCondition condition : conditions) {
            if (condition.isAuthorized()) {
                return;
            }
        }
        throw new UnauthorizedInstantiationException(getClass());
    }

    /**
     * Requires all given conditions to be authorized otherwise an exception is
     * thrown.
     *
     * @param conditions the list of conditions where every single one has to be
     *                   authorized.
     * @throws UnauthorizedInstantiationException if <b>ANY</b> of the given conditions return
     *                                            <code>false</code> (calling
     *                                            {@link IAuthorizationCondition#isAuthorized()}.
     */
    public void requireAllConditions(IAuthorizationCondition... conditions) {
        for (IAuthorizationCondition condition : conditions) {
            if (!condition.isAuthorized()) {
                throw new UnauthorizedInstantiationException(getClass());
            }
        }
    }

    /**
     * Replace the {@link NavbarDropDownButton} with a new one that has the
     * current user name.
     */
    protected final void updateNavbar() {
        navbar = newNavbar("navbar");
        replace(navbar);
        AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
        if (target != null) {
            target.add(navbar);
        } else {
            addOrReplace(navbar);
        }
    }

    /**
     * @param titleModel
     */
    protected final void setPageTitle(final IModel<String> titleModel) {
        addOrReplace(new Label("pageTitle", titleModel));
    }

    /**
     * @return
     */
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }

    protected Component addToolTipWebMarkupContainer(
            final String wicketId,
            final IModel<String> textModel,
            final TooltipConfig.Placement placement) {
        return new WebMarkupContainer(wicketId)
                .setOutputMarkupPlaceholderTag(true)
                .add(new TooltipBehavior(textModel, new TooltipConfig().withPlacement(placement)));
    }

    /**
     * @param id
     * @return
     */
    private Navbar newNavbar(final String id) {
        String logoUrl;

        // Custom Logo
        logoUrl = "img/soc_logo_124x32px_horizontal.png";

        // navbar at top
        Navbar navbar = new Navbar(id) {
            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
                // adjust body padding to accommodate for variable navigation bar height
                response.render(OnDomReadyHeaderItem.forScript(
                        String.format(JSTemplates.ADJUST_BODY_PADDING_TOP, this.getMarkupId())));
            }
        };
        navbar.setPosition(Position.TOP).setBrandImage(
                new PackageResourceReference(BasePage.class, logoUrl),
                new StringResourceModel("menu.home.link.text", this, null));

        DropDownButton helpMenu = newHelpDropDownButton();
        NavbarButton<AboutPage> about = new NavbarButton<AboutPage>(AboutPage.class, new StringResourceModel("menu.about.link.text", this, null));
        NavbarButton<LearningCenterPresentation> lc = new NavbarButton<LearningCenterPresentation>(LearningCenterPresentation.class, new StringResourceModel("menu.learning.center.text", this, null));
        NavbarButton<ObservatoryPage> observatory = new NavbarButton<ObservatoryPage>(ObservatoryPage.class, new StringResourceModel("menu.observatory.link.text", this, null));

        navbar.addComponents(NavbarComponents.transform(ComponentPosition.RIGHT, observatory));
        navbar.addComponents(NavbarComponents.transform(ComponentPosition.RIGHT, lc));
        navbar.addComponents(NavbarComponents.transform(ComponentPosition.RIGHT, about));

        navbar.addComponents(NavbarComponents.transform(ComponentPosition.LEFT, helpMenu));

        // add search form
        navbar.addComponents(newNavbarSearchForm());

        // add login/logout stuff
        if (((EFFSession) Session.get()).isAuthenticated()) {
            DropDownButton userMenu = newNavbarUserDropDownButton();
            DropDownButton notifications = new NotificationDropDownMenu();

            NavbarButton<Class<?>> unreadMessageButton = newUnreadMessageButton();
            navbar.addComponents(NavbarComponents.transform(ComponentPosition.RIGHT, unreadMessageButton, notifications, userMenu));
        } else {
            NavbarButton<LoginPage> loginButton = new NavbarButton<LoginPage>(LoginPage.class, new StringResourceModel(
                    "menu.login.link.text", this, null));
            navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.RIGHT, loginButton));
        }

        // for ajax replace
        navbar.setOutputMarkupId(true);

        return navbar;
    }

    private WebMarkupContainer newSubNav() {
        final WebMarkupContainer subnavContainer = new WebMarkupContainer("subnavContainer");

        final BookmarkablePageLink<CampaignsPage> challenges =
                new BookmarkablePageLink<>("challenges", CampaignsPage.class, new PageParameters());
        subnavContainer.add(challenges);

        final BookmarkablePageLink<ActionsListPage> actions =
                new BookmarkablePageLink<>("actions", ActionsListPage.class, new PageParameters());
        subnavContainer.add(actions);

        final BookmarkablePageLink<ScalingPage> impactStories =
                new BookmarkablePageLink<>("impactStories", ScalingPage.class, new PageParameters());
        subnavContainer.add(impactStories);

        return subnavContainer;
    }

    public NavbarButton<Class<?>> newUnreadMessageButton() {
        String totalNoOfUnreadMessages = "" + messageService.countAllUnreadMessages(loggedInUserProvider.getLoggedInUser());
        if (loggedInUserProvider.getLoggedInUser().hasAnyRoles(UserRole.SUPER_ADMIN)) {
            return new NavbarButton<Class<?>>(CoordinatorDashboardMessagesInboxPage.class, Model.of(totalNoOfUnreadMessages))
                    .setIconType(IconType.envelope);
        } else {
            return new NavbarButton<Class<?>>(AdminMessageInboxPage.class, Model.of(totalNoOfUnreadMessages))
                    .setIconType(IconType.envelope);
        }
    }

    /**
     * @return
     */
    private AbstractNavbarComponent newNavbarSearchForm() {
        return new AbstractNavbarComponent(ComponentPosition.RIGHT) {
            private static final long serialVersionUID = 4462437740704835525L;

            @Override
            public Component create(String markupId) {
                return new NavbarSearchForm(markupId);
            }
        };
    }

    /**
     * @return
     */
    private DropDownButton newHelpDropDownButton() {

        MenuBookmarkablePageLink<ErrorPage> issue = new MenuBookmarkablePageLink<ErrorPage>(ErrorPage.class,
                new StringResourceModel("menu.bug.report.link.text", this, null));
        issue.setIconType(IconType.eyeopen);
        issue.add(new TooltipBehavior(new StringResourceModel("menu.bug.report.link.tooltip", this, null),
                new TooltipConfig().withPlacement(TooltipConfig.Placement.left)));

        MenuBookmarkablePageLink<ContactPage> contact = new MenuBookmarkablePageLink<ContactPage>(ContactPage.class, new StringResourceModel(
                "menu.contact.link.text", this, null));
        contact.setIconType(IconType.globe);
        contact.add(new TooltipBehavior(new StringResourceModel("menu.contact.link.tooltip", this, null), new TooltipConfig().withPlacement(TooltipConfig.Placement.left)));

        DropDownButton dropdown = new NavbarDropDownButton(new StringResourceModel("menu.help.link.text", this, null))
                .setIconType(IconType.questionsign).addButton(issue).addButton(contact);
        return dropdown;
    }

    /**
     * @return
     */
    private DropDownButton newNavbarUserDropDownButton() {
        loggedInUserProvider.retrieveLoggedInUser();
        User user = loggedInUserProvider.getLoggedInUser();
        Employment curr = loggedInUserProvider.getLoggedInUser().getCurrentEmployment();
        if (curr != null) {
            user.setCurrentEmployment(curr);
        }
        final DropDownButton dropdown =
                new NavbarDropDownButton(new PropertyModel<String>(user, "nickName")).setIconType(IconType.user);

        dropdown.addButton(
                new MenuBookmarkablePageLink<>(
                        UserDashboardPage.class,
                        new PageParameters(),
                        new StringResourceModel("menu.profile.dashboard.link.text", this, null))
                        .setIconType(IconType.certificate));

        dropdown.addButton(
                new MenuBookmarkablePageLink<>(UserParticipationDashboardPage.class, new PageParameters(),
                        new StringResourceModel("menu.profile.participate.link.text", this,
                                null)).setIconType(IconType.eyeopen));


        dropdown.addButton(
                new MenuBookmarkablePageLink<>(
                        UserLeadDashboardPage.class,
                        new PageParameters(),
                        new StringResourceModel("menu.profile.lead.link.text", this, null))
                        .setIconType(IconType.globe));

        dropdown.addButton(
                new MenuBookmarkablePageLink<>(
                        UserSettingsDashboardPage.class,
                        new PageParameters().set("id", user.getId()),
                        new StringResourceModel("menu.profile.settings.link.text", this, null))
                        .setIconType(IconType.edit));

        dropdown.addButton(
                new MenuBookmarkablePageLink<>(
                        CoordinatorDashboardOverviewPage.class,
                        new PageParameters(),
                        new StringResourceModel("menu.profile.coordinatorDashboard.link.text", this, null))
                        .setIconType(IconType.check));

        dropdown.addButton(
                new MenuBookmarkablePageLink<>(
                        AdminMessageInboxPage.class,
                        new PageParameters().set("id", user.getId()),
                        new StringResourceModel("menu.profile.messages.link.text", this, null))
                        .setIconType(IconType.envelope))
                .addButton(new MenuDivider()).addButton(newLogoutLink());

        return dropdown;
    }

    /**
     * @param employment
     * @return
     */
    private NavbarAjaxLink<String> newEmploymentLink(final Employment employment) {
        final User user = loggedInUserProvider.getLoggedInUser();
        NavbarAjaxLink<String> profileLink = new NavbarAjaxLink<String>(ButtonList.getButtonMarkupId(),
                new Model<String>(employment.toMenuBarString())) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                user.setCurrentEmployment(employment);
                setResponsePage(getApplication().getHomePage());
            }

            @Override
            protected void onConfigure() {
                if (employment.equals(user.getCurrentEmployment())) {
                    add(new CSSAppender("active"));
                    add(new TooltipBehavior(new StringResourceModel("menu.employments.active.text", this, null), new TooltipConfig().withPlacement(TooltipConfig.Placement.left)));
                    setIconType(IconType.check);
                }
                super.onConfigure();
            }

        };
        return profileLink;
    }

    /**
     * @return
     */
    private NavbarAjaxLink<String> newLogoutLink() {
        return new NavbarAjaxLink<String>(ButtonList.getButtonMarkupId(), new StringResourceModel(
                "menu.logout.link.text", this, null)) {
            private static final long serialVersionUID = 3512934733687535081L;

            /*
             * (non-Javadoc)
             *
             * @see org.apache.wicket.ajax.markup.html.AjaxLink#onInitialize()
             */
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setIconType(IconType.off);
            }

            /*
             * (non-Javadoc)
             *
             * @see
             * org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache
             * .wicket.ajax.AjaxRequestTarget)
             */
            @Override
            public void onClick(AjaxRequestTarget target) {
                logout();
            }
        };
    }

    /**
     * @return
     */
    private Label newBuildNumberLabel() {
        String buildNumber = "";
        try {
            Manifests.append(WebApplication.get().getServletContext());
            buildNumber = Manifests.read("SCM-Revision");
            logger.info("build number is: " + buildNumber);
        } catch (Exception ex) {
            // ignore if we can't get any version number
            if (logger.isDebugEnabled()) {
                logger.error("Could not read SCM-Revision from Manifest!", ex);
            }
        }
        return new Label("buildNumber", buildNumber);
    }

    /**
     * logout user.
     */
    private void logout() {
        // clear session
        getSession().invalidateNow();
        // redirect to Login page
        setResponsePage(getApplication().getHomePage());
    }

    ;

    /**
     * @param parameters
     */
    private void displayMessage(final PageParameters parameters) {
        logger.info("displaying messages");
        if (parameters != null) {
            String message = parameters.get(MESSAGE_PARAM).toString(null);
            int messageLevel = parameters.get(LEVEL_PARAM).toInt(-1);
            if ((message != null) && (messageLevel != -1)) {
                switch (messageLevel) {
                    case FeedbackMessage.SUCCESS:
                        success(message);
                        break;
                    case FeedbackMessage.INFO:
                        info(message);
                        break;
                    case FeedbackMessage.WARNING:
                        warn(message);
                        break;
                    case FeedbackMessage.ERROR:
                        error(message);
                        break;
                }
            }
        }
    }

    /**
     * @return the credits to be displayed on footer
     */
    private WebMarkupContainer effCredits() {
        WebMarkupContainer creditsEff = new WebMarkupContainer("credits");

        // add EFF Rights Reserved text
        creditsEff.add(new Label("copyright", (new StringResourceModel("footer.text", this, null).getString())));

        // add SVN revision number
        creditsEff.add(newBuildNumberLabel());

        return creditsEff;
    }

    /**
     * @return the logos to be displayed on footer
     */
    private WebMarkupContainer displayFooter() {
        // display Logo on the footer
        WebMarkupContainer footer = new WebMarkupContainer("footer");

        footer.add(effCredits());

        return footer;
    }

    /**
     * This method will look for particular cookie with specified name and if it is not available, then it will create a new one
     * with age of one month. Also if cookie is created new then message will be displayed.
     *
     * @return the container with cookies information
     */
    private WebMarkupContainer displayCookiesInformation() {
        // display cookie information
        WebRequest webRequest = (WebRequest) RequestCycle.get().getRequest();

        // Get cookie
        List<Cookie> cookiesList = webRequest.getCookies();

        // cookies container
        final WebMarkupContainer cookiesContainer = new WebMarkupContainer("cookiesContainer");
        add(cookiesContainer);
        cookiesContainer.setOutputMarkupId(true);
        cookiesContainer.setVisible(false);

        AjaxLink<Void> cookieCloseButton = new AjaxLink<Void>("cookieCloseButton") {
            private static final long serialVersionUID = 1753342774141422305L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                cookiesContainer.setVisible(false);
                target.add(cookiesContainer);
            }
        };
        cookiesContainer.add(cookieCloseButton);
        boolean flag = false;
        for (Cookie co : cookiesList) {
            if (co.getName().equalsIgnoreCase(COOKIE_NAME)) {
                flag = true;
            }
        }

        if (!flag) {
            WebResponse webResponse = (WebResponse) RequestCycle.get().getResponse();

            // Create cookie and add it to the response
            Cookie cookie = new Cookie(COOKIE_NAME, "cookieValue");
            // get total no of days in current month
            Calendar cal = Calendar.getInstance();
            int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            // set cookie age exactly as one month in seconds
            cookie.setMaxAge(days * 24 * 60 * 60); // days; expressed in seconds
            webResponse.addCookie(cookie);

            // show cookie message
            cookiesContainer.setVisible(true);
        }

        return cookiesContainer;
    }

}
