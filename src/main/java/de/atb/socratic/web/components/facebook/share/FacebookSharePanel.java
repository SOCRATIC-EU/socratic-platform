package de.atb.socratic.web.components.facebook.share;

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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public abstract class FacebookSharePanel<T> extends GenericPanel<T> {

    /**
     *
     */
    private static final long serialVersionUID = -6920007197540178133L;
    private static final JavaScriptResourceReference MYFACEBOOKPAGE_JS = new JavaScriptResourceReference(
            FacebookAjaxSignInPanel.class, "facebook.js");

    public FacebookSharePanel(String id, final IModel<T> model) {
        super(id, model);

        final String pageUrl = providePageLink();
        WebMarkupContainer wmc = new WebMarkupContainer("container") {
            private static final long serialVersionUID = 4730918309499370085L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                add(new AttributeModifier("data-href", pageUrl));
            }
        };
        add(wmc);
        wmc.add(newShareLink(pageUrl));

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forReference(MYFACEBOOKPAGE_JS));
        super.renderHead(response);

        final String titleString = new StringResourceModel("facebook.share.title", this, null).getString();
        final StringHeaderItem headerTitleItem = StringHeaderItem.forString("<meta property=\"og:title\" content=\""
                + titleString + "\" />");
        response.render(headerTitleItem);

        final String descriptionString = new StringResourceModel("facebook.share.description", this, null).getString();
        final StringHeaderItem headerDescriptionItem = StringHeaderItem
                .forString("<meta property=\"og:description\" content=\"" + descriptionString + "\" />");
        response.render(headerDescriptionItem);

        // set type meta tag
        final StringHeaderItem headerTypeItem = StringHeaderItem.forString("<meta property=\"og:type\" content=\"" + "website"
                + "\" />");
        response.render(headerTypeItem);

        // create image Link for given image
        final String pictureUrl = "https://socraticeitorg.files.wordpress.com/2016/03/socratic_logo1.jpg";

        final StringHeaderItem headerImageItem = StringHeaderItem.forString("<meta property=\"og:image\" content=\""
                + pictureUrl + "\" />");
        response.render(headerImageItem);

    }

    /**
     * @return
     */
    private AjaxLink<Void> newShareLink(final String pageUrl) {
        AjaxLink<Void> shareLink = new AjaxLink<Void>("shareLink") {
            private static final long serialVersionUID = 202363713959040288L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                String pageUrlLocal = pageUrl;
                if (!pageUrlLocal.contains("https:")) {
                    pageUrlLocal = pageUrlLocal.replace("http:", "https:");
                }
                pageUrlLocal += "&amp;src=sdkpreparse";
                add(new AttributeModifier("href", pageUrlLocal));
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                String pageUrlLocal = pageUrl;

                if (!pageUrlLocal.contains("https:")) {
                    pageUrlLocal = pageUrlLocal.replace("http:", "https:");
                }
                pageUrlLocal += "/";
                String function = "fbpublish(\'" + pageUrlLocal + "\');";
                target.appendJavaScript(function);
            }

            ;
        };
        return shareLink;
    }

    protected abstract String providePageLink();

}
