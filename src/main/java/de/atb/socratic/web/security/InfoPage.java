/**
 *
 */
package de.atb.socratic.web.security;

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

import de.atb.socratic.exception.NoPendingRegistrationException;
import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.User;
import de.atb.socratic.service.notification.RegistrationMailService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.provider.UrlProvider;
import de.atb.socratic.web.security.login.LoginPage;
import de.atb.socratic.web.security.register.CancelRegistrationPage;
import de.atb.socratic.web.security.register.ConfirmRegistrationPage;
import de.atb.socratic.web.security.register.RegisterPage;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class InfoPage extends BasePage {

    /**
     *
     */
    private static final long serialVersionUID = -8633457007449195875L;

    /**
     * @return
     */
    public static InfoPage registeredMessage() {
        return new InfoPage("headerRegistered", "messageRegistered",
                InfoFragmentType.EMPTY, null, new PageParameters());
    }

    /**
     * @return
     */
    public static InfoPage registrationConfirmedMessage() {
        return new InfoPage("headerConfirmSuccess", "messageConfirmSuccess",
                InfoFragmentType.LOGIN, null, new PageParameters());
    }

    /**
     * @return
     */
    public static InfoPage registrationCancelledMessage() {
        return new InfoPage("headerCancelSuccess", "messageCancelSuccess",
                InfoFragmentType.EMPTY, null, new PageParameters());
    }

    /**
     * @return
     */
    public static InfoPage registrationNotPendingMessage() {
        return new InfoPage("headerRegistrationFailure",
                "messageRegistrationFailureNotPending",
                InfoFragmentType.LOGIN_REGISTER, null, new PageParameters());
    }

    /**
     * @param model
     * @return
     */
    public static InfoPage registrationTimeoutMessage(final IModel<String> model) {
        return new InfoPage("headerRegistrationFailure",
                "messageRegistrationFailureTimeout",
                InfoFragmentType.RESEND_REGISTRATION, model,
                new PageParameters());
    }

    /**
     * @return
     */
    public static InfoPage recoverPWEmailSentMessage() {
        return new InfoPage("headerRecoverPassword", "messageRecoverPassword",
                InfoFragmentType.EMPTY, null, new PageParameters());
    }

    /**
     * @return
     */
    public static InfoPage noSuchUserMessage() {
        return new InfoPage("headerNoSuchUser", "messageNoSuchUser",
                InfoFragmentType.REGISTER, null, new PageParameters());
    }

    /**
     * @return
     */
    public static InfoPage resetPWRequestNotPendingMessage() {
        return new InfoPage("headerResetPWFailure", "messageResetPWNotPending",
                InfoFragmentType.EMPTY, null, new PageParameters());
    }

    /**
     * @return
     */
    public static InfoPage resetPWRequestTimeoutMessage() {
        return new InfoPage("headerResetPWFailure", "messageResetPWTimeout",
                InfoFragmentType.FORGOT_PASSWORD, null, new PageParameters());
    }

    /**
     * @param model
     * @return
     */
    public static InfoPage resendRegistrationConfirmationMessage(
            final IModel<String> model) {
        return new InfoPage("headerResendRegistrationConfirmation",
                "messageResendRegistrationConfirmation",
                InfoFragmentType.RESEND_REGISTRATION, model,
                new PageParameters());
    }

    /**
     * @return
     */
    public static InfoPage registrationConfirmationSentAgainMessage() {
        return new InfoPage("headerRegistrationConfirmationSentAgain",
                "messageRegistrationConfirmationSentAgain",
                InfoFragmentType.EMPTY, null, new PageParameters());
    }

    /**
     * @return
     */
    public static InfoPage resendRegistrationFailureMessage() {
        return new InfoPage("headerResendRegistrationFailure",
                "messageResendRegistrationFailure",
                InfoFragmentType.LOGIN_REGISTER, null, new PageParameters());
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.socratic.web.BasePage#getPageTitleModel()
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }

    /**
     * @param headerMarkupId
     * @param messageMarkupId
     * @param infoFragmentType
     * @param model
     * @param parameters
     */
    private InfoPage(final String headerMarkupId,
                     final String messageMarkupId,
                     final InfoFragmentType infoFragmentType,
                     final IModel<String> model,
                     final PageParameters parameters) {

        super(parameters);

        add(new Fragment("header", headerMarkupId, this));
        add(new Fragment("message", messageMarkupId, this, model));

        switch (infoFragmentType.ordinal()) {
            case 0:
                add(new EmptyInfoFragment(this));
                break;

            case 1:
                add(new LoginLinkFragment(this));
                break;

            case 2:
                add(new RegisterLinkFragment(this));
                break;

            case 3:
                add(new LoginRegisterLinkFragment(this));
                break;

            case 4:
                add(new ResendRegistrationLinkFragment(this, model.getObject()));
                break;

            case 5:
                add(new Fragment("info", "forgotPasswordInfo", this));
                break;
        }
    }

    /**
     * @author ATB
     */
    private static enum InfoFragmentType {
        EMPTY, LOGIN, REGISTER, LOGIN_REGISTER, RESEND_REGISTRATION, FORGOT_PASSWORD;
    }

    private static class EmptyInfoFragment extends Fragment {

        /**
         *
         */
        private static final long serialVersionUID = 1160829122649467153L;

        public EmptyInfoFragment(final MarkupContainer markupProvider) {
            super("info", "empty", markupProvider);
        }

    }

    /**
     * @author ATB
     */
    private static class LoginLinkFragment extends Fragment {

        /**
         *
         */
        private static final long serialVersionUID = -2158476919741722089L;

        public LoginLinkFragment(final MarkupContainer markupProvider) {
            super("info", "loginInfo", markupProvider);

            // add login link
            add(new BookmarkablePageLink<LoginPage>("loginLink",
                    LoginPage.class));
        }

    }

    /**
     * @author ATB
     */
    private static class RegisterLinkFragment extends Fragment {

        /**
         *
         */
        private static final long serialVersionUID = 5530137478910282540L;

        public RegisterLinkFragment(final MarkupContainer markupProvider) {
            super("info", "registerInfo", markupProvider);

            // add register link
            add(new BookmarkablePageLink<RegisterPage>("registerLink",
                    RegisterPage.class));
        }

    }

    /**
     * @author ATB
     */
    private static class LoginRegisterLinkFragment extends Fragment {

        /**
         *
         */
        private static final long serialVersionUID = 431378188094728560L;

        public LoginRegisterLinkFragment(final MarkupContainer markupProvider) {
            super("info", "loginRegisterInfo", markupProvider);

            // add login link
            add(new BookmarkablePageLink<LoginPage>("loginLink",
                    LoginPage.class));

            // add register link
            add(new BookmarkablePageLink<RegisterPage>("registerLink",
                    RegisterPage.class));
        }

    }

    /**
     * @author ATB
     */
    @SuppressWarnings("CdiManagedBeanInconsistencyInspection")
    private static class ResendRegistrationLinkFragment extends Fragment {

        /**
         *
         */
        private static final long serialVersionUID = 4210241478105940579L;

        @Inject
        AuthenticationService authenticationService;

        @Inject
        RegistrationMailService registrationMailService;

        @Inject
        UrlProvider urlProvider;

        public ResendRegistrationLinkFragment(
                final MarkupContainer markupProvider, final String email) {
            super("info", "resendRegistrationInfo", markupProvider);

            // add register link
            PageParameters parameters = new PageParameters();
            parameters.add("email", email);
            add(new Link<Void>("resendRegistrationLink") {

                private static final long serialVersionUID = -5958845052833917370L;

                @Override
                public void onClick() {
                    resendRegistrationConfirmationEmail(email);
                }

            });
        }

        /**
         * @param email
         */
        private void resendRegistrationConfirmationEmail(final String email) {
            try {
                // reset the registration information
                User user = authenticationService
                        .resetRegistrationInformation(email);
                // send the confirm registration email again
                registrationMailService.sendRegistrationConfirmationMessage(
                        user, urlProvider.urlFor(ConfirmRegistrationPage.class,
                                "token", user.getRegistrationToken()),
                        urlProvider.urlFor(CancelRegistrationPage.class,
                                "token", user.getRegistrationToken()),
                        urlProvider.urlFor(getApplication().getHomePage()));
                // continue to info page telling the user to confirm
                // registration
                setResponsePage(registrationConfirmationSentAgainMessage());
            } catch (NotificationException e) {
                error(e.getMessage());
            } catch (NoPendingRegistrationException e) {
                // user doesn't exist or has already confirmed/cancelled
                // registration
                setResponsePage(resendRegistrationFailureMessage());
            }
        }

    }

}
