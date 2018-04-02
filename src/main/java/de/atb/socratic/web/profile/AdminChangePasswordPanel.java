package de.atb.socratic.web.profile;

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

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.User;
import de.atb.socratic.service.notification.ResetPasswordMailService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.PatternValidator;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class AdminChangePasswordPanel extends Panel {

    private static final long serialVersionUID = -1938059690136384691L;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @Inject
    AuthenticationService authenticationService;

    @Inject
    ResetPasswordMailService resetPasswordMailService;

    private final Form<User> form;

    private final WebMarkupContainer buttonPanel;

    private final AjaxLink<Void> editLink;

    private final PasswordTextField newPasswordTextField;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private boolean formEnabled = false;

    /**
     * @param id
     * @param feedbackPanel
     */
    public AdminChangePasswordPanel(final String id,
                                    final StyledFeedbackPanel feedbackPanel, final User user) {
        super(id);

        this.feedbackPanel = feedbackPanel;

        // initially form is disabled
        formEnabled = false;

        // add form
        form = new InputValidationForm<User>("changePasswordForm", Model.of(user)) {

            private static final long serialVersionUID = 2232912774424351448L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(formEnabled);
            }
        };
        add(form);

        // add password input field for new password inside a border component
        // that shows validation errors
        newPasswordTextField = new PasswordTextField("newPassword",
                Model.of(""));
        newPasswordTextField.add(new PatternValidator(
                AuthenticationService.PW_PATTERN));
        form.add(new InputBorder<String>("newPasswordValidationBorder",
                newPasswordTextField, new StringResourceModel(
                "new.password.input.label", this, null),
                new StringResourceModel("new.password.input.help", this, null)));

        // add password input field for new password confirmation inside a
        // border component that shows validation errors
        PasswordTextField confirmPasswordTextField = new PasswordTextField(
                "confirmPassword", Model.of(""));
        form.add(new InputBorder<String>("confirmPasswordValidationBorder",
                confirmPasswordTextField, new StringResourceModel(
                "password.confirm.input.label", this, null)));

        // add a validator that checks the two passwords for equality
        form.add(new EqualPasswordInputValidator(newPasswordTextField,
                confirmPasswordTextField));

        buttonPanel = new WebMarkupContainer("changePasswordButtonPanel") {

            private static final long serialVersionUID = -4188943652260510343L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // only show if form is enabled
                setVisible(formEnabled);
            }
        };
        form.add(buttonPanel.setOutputMarkupPlaceholderTag(true));

        // add a sumbit link
        buttonPanel.add(new AjaxSubmitLink("changePasswordSubmit", form) {

            private static final long serialVersionUID = -110798286014918845L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> f) {
                changePassword(target, form.getModelObject());
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> f) {
                showError(target);
            }
        });

        // add a cancel link
        buttonPanel.add(new AjaxLink<User>("changePasswordCancel") {

            private static final long serialVersionUID = 8388569613963520589L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                cancelUpdate(target);
            }
        });

        // add link to edit the form
        editLink = new AjaxLink<Void>("changePasswordLink") {

            private static final long serialVersionUID = 2741753626448934552L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // only show if form is disabled
                setVisible(!formEnabled);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                enableForm(target);
            }

        };
        add(editLink.setOutputMarkupPlaceholderTag(true));
    }

    /**
     * @param target
     */
    private void changePassword(AjaxRequestTarget target, User user) {
        try {

            // reset old with new password
            authenticationService.resetPassword(user, newPasswordTextField.getModelObject());

            // send the confirmation email
            resetPasswordMailService.sendResetPWSuccessMessage(user);

            // show feedback message
            getPage().success("Password has been changed!");
            target.add(feedbackPanel);
            target.appendJavaScript("window.document.body.scrollTop = 0; window.document.documentElement.scrollTop = 0");

            // disable form
            disableForm(target);

        } catch (NotificationException e) {
            getPage().info(getString("error.notification"));
            cancelUpdate(target);
        }
    }

    /**
     * @param target
     */
    private void cancelUpdate(AjaxRequestTarget target) {
        // re-render feedback panel to clear existing messages
        target.add(feedbackPanel);
        // disable form
        disableForm(target);
    }

    /**
     * @param target
     */
    private void showError(AjaxRequestTarget target) {
        // show feedback message
        target.add(feedbackPanel);
        // re-render form
        target.add(form);
    }

    /**
     * @param target
     */
    private void enableForm(AjaxRequestTarget target) {
        // enable form
        formEnabled = true;
        // re-render form and editLink
        target.add(form);
        target.add(editLink);
    }

    /**
     * @param target
     */
    private void disableForm(AjaxRequestTarget target) {
        // disable form
        formEnabled = false;
        // re-render form and editLink
        target.add(form);
        target.add(editLink);
    }

}
