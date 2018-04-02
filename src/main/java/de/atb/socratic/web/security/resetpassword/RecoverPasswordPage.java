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

import de.atb.socratic.exception.NoSuchUserException;
import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.User;
import de.atb.socratic.service.notification.ResetPasswordMailService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.provider.UrlProvider;
import de.atb.socratic.web.security.register.RegisterPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class RecoverPasswordPage extends BasePage {

    private static final long serialVersionUID = -6472406447506403733L;

    // inject a logger
    @Inject
    Logger logger;

    @Inject
    AuthenticationService authenticationService;

    @Inject
    ResetPasswordMailService resetPasswordMailService;

    @Inject
    UrlProvider urlProvider;

    private StyledFeedbackPanel feedbackPanel;

    private String email;

    public RecoverPasswordPage(final PageParameters parameters) {

        super(parameters);
        // add form to enter email address and send password reset link email
        Form<User> form = new InputValidationForm<User>("form");
        add(form);
        feedbackPanel = new StyledFeedbackPanel("feedback");
        add(feedbackPanel);

        // add link to the register page
        form.add(new BookmarkablePageLink<RegisterPage>("registerLink",
                RegisterPage.class));

        form.add(newSubmitForm());

        // add text field for email inside a border component that performs bean
        // validation
        final TextField<String> emailTextField = new TextField<>("email", new PropertyModel<String>(this, "email"));
        form.add(new OnEventInputBeanValidationBorder<>(
                "emailValidationBorder", emailTextField,
                new StringResourceModel("email.input.label", this, null),
                new StringResourceModel("email.input.help", this, null),
                HtmlEvent.ONCHANGE));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // adjust body padding to accommodate for variable navigation bar height
        response.render(OnDomReadyHeaderItem.forScript(JSTemplates.HIDE_SUBNAV));
    }

    private void sendResetPasswordMail(AjaxRequestTarget target) {
        try {
            final User user = authenticationService.requestNewPassword(email);
            resetPasswordMailService.sendResetPWLink(
                    user,
                    urlProvider.urlFor(ResetPasswordPage.class, "token", user.getResetPWRequestToken()),
                    urlProvider.urlFor(getApplication().getHomePage()));
            logger.info(String.format("password reset email sent to %s", user.getEmail()));
            feedbackPanel.success(new StringResourceModel("message.recover.password", this, null).getObject());
        } catch (NoSuchUserException e) {
            feedbackPanel.error(new StringResourceModel("message.no.such.user", this, null).getObject());
        } catch (NotificationException e) {
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

    private AjaxLink<String> newSubmitForm() {
        return new AjaxLink<String>("formSubmit") {

            private static final long serialVersionUID = -6868495773174350134L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                sendResetPasswordMail(target);
            }

        };
    }

}
