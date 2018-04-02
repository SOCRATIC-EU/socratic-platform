/**
 *
 */
package de.atb.socratic.web.security.resetpassword;

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

import de.atb.socratic.exception.NoPendingResetPWRequestException;
import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.exception.ResetPWRequestTimeoutException;
import de.atb.socratic.model.User;
import de.atb.socratic.service.notification.ResetPasswordMailService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.security.SecurityPage;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.PatternValidator;

import static de.atb.socratic.web.security.InfoPage.resetPWRequestNotPendingMessage;
import static de.atb.socratic.web.security.InfoPage.resetPWRequestTimeoutMessage;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ResetPasswordPage extends SecurityPage {

    private static final long serialVersionUID = -1381970567619066043L;

    @Inject
    ResetPasswordMailService resetPasswordMailService;

    private User user = new User();
    private final PasswordTextField passwordTextField;
    private StyledFeedbackPanel feedbackPanel;

    public ResetPasswordPage(final PageParameters parameters) {

        super(parameters);

        feedbackPanel = new StyledFeedbackPanel("feedback");
        add(feedbackPanel);

        String token = parameters.get("token").toString();
        try {
            user = authenticationService.checkResetPWToken(token);
        } catch (NoPendingResetPWRequestException e) {
            // there's no user with this token and a pending reset password request
            throw new RestartResponseException(resetPWRequestNotPendingMessage());
        } catch (ResetPWRequestTimeoutException e) {
            // request for resetting password has been too long ago
            throw new RestartResponseException(resetPWRequestTimeoutMessage());
        }

        // no exception --> add form components
        Form<User> form = new InputValidationForm<User>("form") {
            @Override
            protected void onSubmit() {
                resetPassword();
            }
        };
        add(form);

        // add password input field for password inside a border component that
        // shows validation errors
        passwordTextField = new PasswordTextField("password", Model.of(""));
        passwordTextField.add(new PatternValidator(AuthenticationService.PW_PATTERN));
        form.add(new InputBorder<>(
                "passwordValidationBorder",
                passwordTextField,
                new StringResourceModel("password.input.label", this, null),
                new StringResourceModel("password.input.help", this, null)));

        // add password input field for password confirmation inside a border
        // component that shows validation errors
        PasswordTextField confirmPasswordTextField = new PasswordTextField("confirmPassword", Model.of(""));
        form.add(new InputBorder<>(
                "confirmPasswordValidationBorder",
                confirmPasswordTextField,
                new StringResourceModel("password.confirm.input.label", this, null)));

        // add a validator that checks the two passwords for equality
        form.add(new EqualPasswordInputValidator(passwordTextField, confirmPasswordTextField));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // adjust body padding to accommodate for variable navigation bar height
        response.render(OnDomReadyHeaderItem.forScript(JSTemplates.HIDE_SUBNAV));
    }

    @Override
    protected void onRegistrationNotConfirmed() {
        // nothing to do here
    }

    /**
     *
     */
    private void resetPassword() {
        try {
            // encrypt password and store user entity in db
            authenticationService.resetPassword(user, passwordTextField.getModelObject());
            // send the confirmation email
            resetPasswordMailService.sendResetPWSuccessMessage(user);
            // continue to home page telling the user that his password has been changed
            feedbackPanel.success(new StringResourceModel("message.reset.success", this, null).getObject());
            login(user.getEmail(), passwordTextField.getModelObject());
        } catch (NotificationException e) {
            feedbackPanel.error(e.getMessage());
        }
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

}
