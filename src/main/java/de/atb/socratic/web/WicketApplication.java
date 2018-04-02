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

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import de.agilecoders.wicket.Bootstrap;
import de.agilecoders.wicket.settings.BootstrapSettings;
import de.atb.socratic.web.action.detail.ActionBusinessModelDetailPage;
import de.atb.socratic.web.action.detail.ActionIterationDetailPage;
import de.atb.socratic.web.action.detail.ActionIterationsListPage;
import de.atb.socratic.web.action.detail.ActionSolutionPage;
import de.atb.socratic.web.dashboard.DashboardManager;
import de.atb.socratic.web.dashboard.iLead.UserLeadDashboardPage;
import de.atb.socratic.web.dashboard.iParticipate.UserParticipationDashboardPage;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.inception.CampaignAddEditPage;
import de.atb.socratic.web.inception.CampaignCopyPage;
import de.atb.socratic.web.inception.CampaignsPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.inception.idea.details.IdeaDetailsPage;
import de.atb.socratic.web.invitations.InvitationPage;
import de.atb.socratic.web.learningcenter.LearningCenterPresentation;
import de.atb.socratic.web.notification.ManageNotificationsPage;
import de.atb.socratic.web.observatory.ObservatoryPage;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.security.EFFAuthorizationStrategy;
import de.atb.socratic.web.security.InfoPage;
import de.atb.socratic.web.security.login.LoginPage;
import de.atb.socratic.web.security.register.CancelRegistrationPage;
import de.atb.socratic.web.security.register.ConfirmRegistrationPage;
import de.atb.socratic.web.security.register.RegisterPage;
import de.atb.socratic.web.security.resetpassword.RecoverPasswordPage;
import de.atb.socratic.web.security.resetpassword.ResetPasswordPage;
import de.atb.socratic.web.selection.SelectionPage;
import de.atb.socratic.web.upload.FileUploader;
import de.atb.socratic.web.welcome.WelcomePage;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.bean.validation.BeanValidationConfiguration;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.settings.IExceptionSettings;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;
import org.wicketstuff.javaee.naming.global.ModuleJndiNamingStrategy;

/**
 * @author ATB
 */
public class WicketApplication extends WebApplication {

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends Page> getHomePage() {
        return WelcomePage.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.protocol.http.WebApplication#init()
     */
    @Override
    public void init() {
        super.init();

        // wicketstuff-javaee-inject configuration for injecting EJBs into
        // Wicket pages
        configureJavaEEInject();

        // CDI configuration for injection CDI components into Wicket pages
        configureCDIInjecttion();

        // Bean Validation configuration
        configureBeanValidation();

        // add authorization strategy
        getSecuritySettings().setAuthorizationStrategy(new EFFAuthorizationStrategy());

        // add bootstrap stuff
        configureBootstrap();

        // mount pages
        mountPages();

        // set custom access denied page
        registerCustomAccessDeniedPage();

        // set custom page expired error page
        registerCustomPageExpiredErrorPage();

        if (usesDeploymentConfig()) {
            // use custom error page
            registerCustomErrorPage();

            //setRootRequestMapper(new HttpsMapper(getRootRequestMapper(), new HttpsConfig(8080, 8443)));
        }
    }

    private void registerCustomPageExpiredErrorPage() {
        getApplicationSettings().setPageExpiredErrorPage(PageExpiredErrorPage.class);
    }

    /**
     *
     */
    private void registerCustomAccessDeniedPage() {
        getApplicationSettings().setAccessDeniedPage(AccessDeniedPage.class);
    }

    /**
     *
     */
    private void registerCustomErrorPage() {
        // show custom error page
        getApplicationSettings().setInternalErrorPage(ErrorPage.class);
        getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.
     * wicket.request.Request, org.apache.wicket.request.Response)
     */
    @Override
    public Session newSession(Request request, Response response) {
        return new EFFSession(request);
    }

    /**
     * Mounts certain pages to user-friendly, bookmarkable links.
     */
    private void mountPages() {
        mountPage("/about", AboutPage.class);
        mountPage("/contact", ContactPage.class);
        mountPage("/info", InfoPage.class);
        mountPage("/issue", ErrorPage.class);
        // match “/error404” to error page “ErrorPage404.html“.
        mountPage("/error404", ErrorPage404.class);
        mountPage("/login", LoginPage.class);
        mountPage("/observatory", ObservatoryPage.class);
        mountPage("/recoverPassword", RecoverPasswordPage.class);
        mountPage("/resetPassword", ResetPasswordPage.class);
        mountPage("/register", RegisterPage.class);
        mountPage("/confirmRegistration", ConfirmRegistrationPage.class);
        mountPage("/cancelRegistration", CancelRegistrationPage.class);
        mountPage("/profile/${id}", UserProfileDetailsPage.class);
        mountPage("/invitations/${id}", InvitationPage.class);
        mountPage("/manageNotifications/${id}", ManageNotificationsPage.class);
        mountPage("/challenges", CampaignsPage.class);
        mountPage("/challenges/${id}", CampaignAddEditPage.class);
        mountPage("/challenges/${id}/ideas", IdeasPage.class);
        mountPage("/challenges/${id}/ideas/${ideaId}/details/", IdeaDetailsPage.class);
        mountPage("/challenges/${id}/definition", ChallengeDefinitionPage.class);
        mountPage("/challenges/${id}/copy", CampaignCopyPage.class);

        mountPage("/dashboardManager", DashboardManager.class);
        mountPage("/learningcenter", LearningCenterPresentation.class);

        // register FileUploadRequestHandler for, well file uploads, obviously
        mount(new FileUploader("/fileupload"));

        mountPage("/challenges/${id}/ideasSelection", SelectionPage.class);

        mountPage("/adminDashboardParticipationPage", UserParticipationDashboardPage.class);
        mountPage("/adminDashboardLeadPage", UserLeadDashboardPage.class);

        /****** action ****/
        mountPage("/actions/${id}/solution", ActionSolutionPage.class);
        mountPage("/actions/${id}/businessModel", ActionBusinessModelDetailPage.class);
        mountPage("/actions/iterations/${id}/iterationDetail", ActionIterationDetailPage.class);
        mountPage("/actions/iterations/", ActionIterationsListPage.class);
    }

    /**
     * Configures wicket-bootstrap.
     */
    private void configureBootstrap() {
        // Remove Wicket markup as it may lead to strange UI problems because
        // CSS selectors may not match anymore.
        getMarkupSettings().setStripWicketTags(true);

        BootstrapSettings settings = new BootstrapSettings();
        // use minimized version of all bootstrap references when in deployment
        // mode
        settings.minify(usesDeploymentConfig()).useModernizr(true);
        Bootstrap.install(this, settings);
    }

    /**
     *
     */
    private void configureJavaEEInject() {
        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this, new ModuleJndiNamingStrategy()));
    }

    /**
     *
     */
    private void configureCDIInjecttion() {
        BeanManager manager = null;
        InitialContext ic = null;
        try {
            ic = new InitialContext();
            // Standard JNDI binding
            manager = (BeanManager) ic.lookup("java:comp/BeanManager");
        } catch (NameNotFoundException e) {
            if (ic == null) {
                throw new RuntimeException("No InitialContext");
            }
            // Weld/Tomcat
            try {
                manager = (BeanManager) ic.lookup("java:comp/env/BeanManager");
            } catch (Exception e1) {
                // JBoss 5/6 (maybe obsolete in Weld 1.0+)
                try {
                    manager = (BeanManager) ic.lookup("java:app/BeanManager");
                } catch (Exception e2) {
                    throw new RuntimeException("Could not find Bean Manager", e2);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not find Bean Manager", e);
        }

        new CdiConfiguration(manager).configure(this);
    }

    /**
     *
     */
    private void configureBeanValidation() {
        new BeanValidationConfiguration().configure(this);
    }

}
