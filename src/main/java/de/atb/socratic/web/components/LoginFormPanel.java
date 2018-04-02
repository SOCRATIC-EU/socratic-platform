/**
 *
 */
package de.atb.socratic.web.components;

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

import de.atb.socratic.model.User;
import de.atb.socratic.web.security.register.RegisterPage;
import de.atb.socratic.web.security.resetpassword.RecoverPasswordPage;
import de.atb.socratic.web.welcome.WelcomePage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public abstract class LoginFormPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = -5889512702982802794L;

    private String email;

    private String password;

    private BookmarkablePageLink<WelcomePage> backToWelcomePageLink;

    public LoginFormPanel(final String id, final boolean withForgotPasswordLink) {
        super(id);

        // add login form
        final Form<User> form = new InputValidationForm<User>("form") {

            private static final long serialVersionUID = 962359934825069181L;

            @Override
            protected void onSubmit() {
                doSubmit(email, password);
            }
        };
        add(form);

        // add text input field for user's email
        final TextField<String> emailTextField = new TextField<String>("email",
                new PropertyModel<String>(this, "email"));
        // set focus to email text field as soon as page gets rendered
        form.add(new InputBorder<String>("emailValidationBorder",
                emailTextField.setRequired(true), new StringResourceModel(
                "email.input.label", this, null)));

        // add password input field for user's password
        final PasswordTextField passwordTextField = new PasswordTextField(
                "password", new PropertyModel<String>(this, "password"));
        form.add(new InputBorder<String>("passwordValidationBorder",
                passwordTextField, new StringResourceModel(
                "password.input.label", this, null)));

        // add link to the register page
        form.add(new BookmarkablePageLink<RegisterPage>("registerLink", RegisterPage.class));

        // add forgot password link
        form.add(new BookmarkablePageLink<RecoverPasswordPage>(
                "forgotPasswordLink", RecoverPasswordPage.class) {

            private static final long serialVersionUID = -1025162405667482012L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(withForgotPasswordLink);
            }

            ;
        }.setOutputMarkupPlaceholderTag(true));


        backToWelcomePageLink = new BookmarkablePageLink<>("backToWelcomePageLink", WelcomePage.class, new PageParameters());
        form.add(backToWelcomePageLink);

    }

    /**
     * What to do when form is submitted.
     */
    protected abstract void doSubmit(final String email, final String password);

    public String getEmail() {
        return email;
    }

}
