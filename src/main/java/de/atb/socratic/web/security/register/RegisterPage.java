/**
 *
 */
package de.atb.socratic.web.security.register;

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

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.exception.UserRegistrationException;
import de.atb.socratic.model.User;
import de.atb.socratic.model.validation.NickNameInputValidator;
import de.atb.socratic.service.employment.CompanyService;
import de.atb.socratic.service.notification.RegistrationMailService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.IndicatingAjaxSubmitLink;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.provider.UrlProvider;
import de.atb.socratic.web.security.login.LoginPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.PatternValidator;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class RegisterPage extends BasePage {

    private static final long serialVersionUID = 4896389217022780891L;

    @Inject
    AuthenticationService authenticationService;

    @Inject
    RegistrationMailService registrationMailService;

    @EJB
    CompanyService companyService;

    @Inject
    UrlProvider urlProvider;

    private User newUser = new User();
    private PasswordTextField passwordTextField;
    private StyledFeedbackPanel feedbackPanel;

    public RegisterPage(final PageParameters parameters) {

        super(parameters);

        // hide subNavContainer
        subNavContainer.setVisible(false);

        feedbackPanel = new StyledFeedbackPanel("feedback");
        add(feedbackPanel);
        Form<User> form = new InputValidationForm<>("form");
        add(form);
        form.add(newSubmitForm());

        // add text field for email inside a border component that performs bean
        // validation
        final TextField<String> emailTextField = new TextField<>("email", new PropertyModel<String>(newUser,
                "email"));
        form.add(new OnEventInputBeanValidationBorder<>("emailValidationBorder", emailTextField,
                new StringResourceModel("email.input.label", this, null), new StringResourceModel("email.input.help",
                this, null), HtmlEvent.ONCHANGE));

        // add text field for nickname inside a border component that performs bean validation
        final TextField<String> nickNameTextField = new TextField<>("nickName", new PropertyModel<String>(newUser,
                "nickName"));
        form.add(new OnEventInputBeanValidationBorder<>(
                "nickNameValidationBorder",
                nickNameTextField,
                new StringResourceModel("nickname.input.label", this, null),
                new StringResourceModel("nickname.input.help", this, null),
                HtmlEvent.ONCHANGE));
        form.add(new NickNameInputValidator(nickNameTextField));
        nickNameTextField.setOutputMarkupId(true);

        // add text field for first name inside a border component that performs
        // bean validation
        final TextField<String> firstNameTextField = new TextField<>("firstName", new PropertyModel<String>(
                newUser, "firstName"));
        form.add(new OnEventInputBeanValidationBorder<>("firstNameValidationBorder", firstNameTextField,
                new StringResourceModel("firstname.input.label", this, null), HtmlEvent.ONCHANGE));

        // add text field for last name inside a border component that performs
        // bean validation
        final TextField<String> lastNameTextField = new TextField<>("lastName", new PropertyModel<String>(
                newUser, "lastName"));
        form.add(new OnEventInputBeanValidationBorder<>("lastNameValidationBorder", lastNameTextField,
                new StringResourceModel("lastname.input.label", this, null), HtmlEvent.ONCHANGE));

        // add password input field for password inside a border component that
        // shows validation errors
        passwordTextField = new PasswordTextField("password", Model.of(""));
        passwordTextField.add(new PatternValidator(AuthenticationService.PW_PATTERN));
        form.add(new InputBorder<>("passwordValidationBorder", passwordTextField, new StringResourceModel(
                "password.input.label", this, null), new StringResourceModel("password.input.help", this, null)));

        // add password input field for password inside a border component that
        // shows validation errors
        PasswordTextField confirmPasswordTextField = new PasswordTextField("confirmPassword", Model.of(""));
        form.add(new InputBorder<>("confirmPasswordValidationBorder", confirmPasswordTextField,
                new StringResourceModel("password.confirm.input.label", this, null)));

        // add a validator that checks the two passwords for equality
        form.add(new EqualPasswordInputValidator(passwordTextField, confirmPasswordTextField));
        // add link to the login page
        form.add(new BookmarkablePageLink<LoginPage>("loginLink", LoginPage.class));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // adjust body padding to accommodate for variable navigation bar height
        response.render(OnDomReadyHeaderItem.forScript(JSTemplates.HIDE_SUBNAV));
    }

    /**
     *
     */
    private void register(AjaxRequestTarget target) {
        try {
            // check if email is already registered
            authenticationService.isEmailAlreadyRegistered(newUser.getEmail());

            // check if nickName is already registered            
            authenticationService.isNickNameAlreadyRegistered(newUser.getNickName());

            // all fine --> encrypt password and store user entity in db
            newUser = authenticationService.register(newUser, passwordTextField.getModelObject());

            registrationMailService.sendRegistrationConfirmationMessage(newUser,
                    urlProvider.urlFor(ConfirmRegistrationPage.class, "token", newUser.getRegistrationToken()),
                    urlProvider.urlFor(CancelRegistrationPage.class, "token", newUser.getRegistrationToken()),
                    urlProvider.urlFor(getApplication().getHomePage()));
            feedbackPanel.success(new StringResourceModel("message.confirm.success", this, null).getObject());
            // display info telling the user to confirm registration
            setResponsePage(new LoginPage(new PageParameters()
                    .set("msg", new StringResourceModel("message.registered", this, null).getObject()), target));
        } catch (UserRegistrationException | NotificationException e) {
            feedbackPanel.error(e.getMessage());
        }
        target.add(feedbackPanel);
        target.appendJavaScript("window.document.body.scrollTop = 0; window.document.documentElement.scrollTop = 0");
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

    private IndicatingAjaxSubmitLink newSubmitForm() {
        return new IndicatingAjaxSubmitLink("formSubmit") {

            private static final long serialVersionUID = -6868495773174350134L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                register(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.add(feedbackPanel);
                target.appendJavaScript("window.document.body.scrollTop = 0; window.document.documentElement.scrollTop = 0");
            }

        };
    }

}
