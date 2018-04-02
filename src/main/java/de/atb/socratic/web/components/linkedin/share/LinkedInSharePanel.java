package de.atb.socratic.web.components.linkedin.share;

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

import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.dashboard.settings.UserSettingsDashboardPage;
import de.atb.socratic.web.upload.FileUploadHelper;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.StringValue;

public abstract class LinkedInSharePanel<T> extends GenericPanel<T> {

    /**
     *
     */
    private static final long serialVersionUID = -6920007197540178133L;

    private static final JavaScriptResourceReference MYLINKEDINPAGE_JS = new JavaScriptResourceReference(LinkedInSharePanel.class, "linkedInShare.js");
    private IModel<T> entityModel;
    private final Url baseUrl;
    private String pictureUrl;
    private final String SOCRATIC_IMAGE = "https://socraticeitorg.files.wordpress.com/2016/03/socratic_logo1.jpg";
    private final PackageResourceReference defaultLinkedInLogo = new PackageResourceReference(FileUploadHelper.class, "img/linkedIn_logo.png");
    private String titleString = "";
    private String descriptionString = "";
    private String pictureidString = "";
    private String comment = "";
    private final String pageUrl;

    private Modal shareOnLinkedInModel;

    public LinkedInSharePanel(String id, final IModel<T> model, final StyledFeedbackPanel feedbackPanel) {
        super(id, model);
        entityModel = model;

        baseUrl = RequestCycle.get().getUrlRenderer().getBaseUrl();
        pageUrl = providePageLink();
        final WebMarkupContainer wmc = new WebMarkupContainer("container") {
            private static final long serialVersionUID = 2336021418193515895L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                add(new AttributeModifier("data-href", pageUrl));
            }
        };
        add(wmc);

        // confirmation model dialogue asking user to confirm his decision.
        shareOnLinkedInModel = new ShareOnLinkedInNotificationModel("shareOnLinkedInModel", new StringResourceModel(
                "share.notification.modal.header", this, null), new StringResourceModel("share.notification.modal.message", this,
                null), false) {
            private static final long serialVersionUID = 2096179879061520451L;

            @Override
            public void shareOnLIButtonClicked(AjaxRequestTarget target) {
                // on share button click

                String function = "onLinkedInLoad(\'" + titleString + "\' , \'" + descriptionString + "\', \'" + pageUrl
                        + "\', \'" + SOCRATIC_IMAGE + "\' , \'" + comment + "\');";
                target.appendJavaScript(function);
            }
        };

        add(shareOnLinkedInModel);

        wmc.add(newShareLink(model));

        // ajaxcall back 
        AbstractDefaultAjaxBehavior ajaxCallBack = new AbstractDefaultAjaxBehavior() {
            private static final long serialVersionUID = 668149801280507745L;

            @Override
            protected void respond(AjaxRequestTarget target) {
                RequestCycle cycle = RequestCycle.get();
                WebRequest webRequest = (WebRequest) cycle.getRequest();
                StringValue response = webRequest.getQueryParameters().getParameterValue("response");

                if (response.toString().contains("error")) {
                    getPage().error(response);
                    feedbackPanel.error(response);
                } else if (response.toString().contains("success")) {
                    getPage().success(response);
                    feedbackPanel.success(response);
                }
                target.add(feedbackPanel);
            }

            @Override
            protected void updateAjaxAttributes(
                    AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.getExtraParameters().put("response", "PLACEHOLDER1");
            }

            @Override
            public void renderHead(Component component,
                                   IHeaderResponse response) {
                super.renderHead(component, response);
                response.render(new OnEventHeaderItem("window", "response", getCallbackScript()));
            }

            @Override
            public CharSequence getCallbackScript() {
                String script = super.getCallbackScript().toString();
                script = script.replace("\"PLACEHOLDER1\"", "JSON.stringify(response)");
                return script;
            }
        };
        add(ajaxCallBack);

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forReference(MYLINKEDINPAGE_JS));
        super.renderHead(response);


        if (entityModel.getObject() instanceof Campaign) {
            Campaign campaign = (Campaign) entityModel.getObject();
            titleString = campaign.getName();
            descriptionString = campaign.getElevatorPitch();
            if (campaign.getChallengeImage() != null) {
                pictureidString = campaign.getChallengeImage().getId().toString();
            }

            comment = "Check out challenge on SOCRATIC Platform!";
        } else if (entityModel.getObject() instanceof Idea) {
            Idea idea = (Idea) entityModel.getObject();
            titleString = idea.getShortText();
            descriptionString = idea.getElevatorPitch();
            if (idea.getIdeaImage() != null) {
                pictureidString = idea.getIdeaImage().getId().toString();
            }

            comment = "Check out idea on SOCRATIC Platform!";
        } else if (entityModel.getObject() instanceof Action) {
            Action action = (Action) entityModel.getObject();
            titleString = action.getShortText();
            descriptionString = action.getElevatorPitch();
            if (action.getActionImage() != null) {
                pictureidString = action.getActionImage().getId().toString();
            }

            comment = "Check out action on SOCRATIC Platform!";
        } else if (entityModel.getObject() instanceof UserSettingsDashboardPage) {

            titleString = "SOCRATIC Platform for social innovation in Europe!";
            descriptionString = "";
            pictureidString = "";
            comment = "Check out SOCRATIC Platform, register and explore world of social innovation!";
        }

        final StringHeaderItem headerTitleItem = StringHeaderItem.forString("<meta property=\"og:title\" content=\""
                + titleString + "\" />");
        response.render(headerTitleItem);

        final StringHeaderItem headerDescriptionItem = StringHeaderItem
                .forString("<meta property=\"og:description\" content=\"" + descriptionString + "\" />");
        response.render(headerDescriptionItem);

        // create page Link for redirection
        final StringHeaderItem headerPageUrlItem = StringHeaderItem.forString("<meta property=\"og:url\" content=\"" + pageUrl
                + "\" />");
        response.render(headerPageUrlItem);

        // create image Link for given image
        if (pictureidString != null) {
            pictureUrl = "https://" + baseUrl.getHost() + "/socratic-platform/rest/images/"
                    + pictureidString;
        } else {
            pictureUrl = "https://socraticeitorg.files.wordpress.com/2016/03/socratic_logo1.jpg";
        }

        final StringHeaderItem headerImageItem = StringHeaderItem.forString("<meta property=\"og:image\" content=\""
                + pictureUrl + "\" />");
        response.render(headerImageItem);

    }

    /**
     * @return
     */
    private AjaxLink<Void> newShareLink(final IModel<T> model) {
        AjaxLink<Void> shareLink = new AjaxLink<Void>("shareLink") {
            private static final long serialVersionUID = 202363713959040288L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // do not let users see a button if it is settings page
                if (model.getObject() instanceof UserSettingsDashboardPage) {
                    add(new AttributeModifier("class", ""));
                }
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                shareOnLinkedInModel.appendShowDialogJavaScript(target);
            }

            ;
        };

        // add link label
        if (model.getObject() instanceof UserSettingsDashboardPage) {
            shareLink.add(new Label("linkedInPost", new StringResourceModel("linkedInPost.invite.label.text", this, null)));
        } else {
            shareLink.add(new Label("linkedInPost", new StringResourceModel("linkedInShare.label.text", this, null)));
        }

        // add linkedIn image
        NonCachingImage image = new NonCachingImage("picture", defaultLinkedInLogo) {
            /**
             *
             */
            private static final long serialVersionUID = 4989230229561319188L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (model.getObject() instanceof UserSettingsDashboardPage) {
                    setVisible(false);
                } else {
                    setVisible(true);
                }
            }
        };
        image.setOutputMarkupId(true);
        shareLink.add(image);

        add(shareLink);

        return shareLink;
    }

    protected abstract String providePageLink();
}
