package de.atb.socratic.web.components.linkedin;

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

import java.util.UUID;

import de.atb.socratic.web.components.StyledFeedbackPanel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.RequestHandlerStack.ReplaceHandlerException;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public abstract class LinkedInAjaxSignInPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = -6920007197540178133L;

    private final String CSRF_Token = UUID.randomUUID().toString();
    private static final JavaScriptResourceReference MYLINKEDINPAGE_JS = new JavaScriptResourceReference(LinkedInAjaxSignInPanel.class, "linkedIn.js");

    public LinkedInAjaxSignInPanel(String id, final StyledFeedbackPanel feedbackPanel) {
        super(id);

        add(new Label("linkedin-login"));

        AbstractDefaultAjaxBehavior ajaxCallBack = new AbstractDefaultAjaxBehavior() {
            private static final long serialVersionUID = 668149801280507745L;

            @Override
            protected void respond(AjaxRequestTarget target) {
                try {
                    org.apache.wicket.util.string.StringValue token = getRequest().getRequestParameters().getParameterValue("csrf_token");
                    if (token.isNull() || token.isEmpty() || !CSRF_Token.equals(token.toString())) {
                        getPage().error("Wrong CSRF token submitted, cross site scripting is not allowed!");
                        target.add(feedbackPanel);
                    } else {
                        LinkedInProfile profile = LinkedInProfile.fromJSONString(getRequest().getRequestParameters().getParameterValue("profile").toString());
                        authenticate(profile, target);
                    }
                } catch (Exception e) {
                    if (e instanceof ReplaceHandlerException) {
                        // if authenticate threw a RestartResponseException because user was already logged in,
                        // continue as planned.
                        continueToOriginalDestination();
                        // if we get here there was no previous request and we can continue
                        // to home page
                        setResponsePage(getApplication().getHomePage());
                    } else {
                        onError(target, e);
                    }
                }
            }

            @Override
            protected void updateAjaxAttributes(
                    AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.getExtraParameters().put("profile", "PLACEHOLDER1");
                attributes.getExtraParameters().put("csrf_token", CSRF_Token);
            }

            @Override
            public void renderHead(Component component,
                                   IHeaderResponse response) {
                super.renderHead(component, response);
                response.render(new OnEventHeaderItem("window", "linkedinlogincomplete", getCallbackScript()));
            }

            @Override
            public CharSequence getCallbackScript() {
                String script = super.getCallbackScript().toString();
                script = script.replace("\"PLACEHOLDER1\"", "JSON.stringify(loginProfile)");
                return script;
            }
        };
        add(ajaxCallBack);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forReference(MYLINKEDINPAGE_JS));
    }

    public abstract void authenticate(final LinkedInProfile profile, final AjaxRequestTarget target) throws Exception;

    public abstract void onError(final AjaxRequestTarget target, Exception error);

}
