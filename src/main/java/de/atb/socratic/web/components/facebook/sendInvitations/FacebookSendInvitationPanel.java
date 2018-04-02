package de.atb.socratic.web.components.facebook.sendInvitations;

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

import de.atb.socratic.web.components.facebook.FacebookAjaxSignInPanel;
import de.atb.socratic.web.security.login.LoginPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public abstract class FacebookSendInvitationPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = -6920007197540178133L;

    private static final JavaScriptResourceReference MYFACEBOOKPAGE_JS = new JavaScriptResourceReference(
            FacebookAjaxSignInPanel.class, "facebook.js");

    public FacebookSendInvitationPanel(String id) {
        super(id);
        WebMarkupContainer wmc = new WebMarkupContainer("container");
        add(wmc);
        wmc.add(newShareLink());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptReferenceHeaderItem.forReference(MYFACEBOOKPAGE_JS));
    }

    /**
     * @return
     */
    private AjaxLink<Void> newShareLink() {
        AjaxLink<Void> shareLink = new AjaxLink<Void>("shareLink") {
            private static final long serialVersionUID = 202363713959040288L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                String loginPageUrl = RequestCycle.get().getUrlRenderer()
                        .renderFullUrl(Url.parse(RequestCycle.get().urlFor(LoginPage.class, new PageParameters()).toString()));
                String function = "fbSendMessage(\'" + loginPageUrl + "\');";
                target.appendJavaScript(function);
            }

            ;
        };
        return shareLink;
    }

}
