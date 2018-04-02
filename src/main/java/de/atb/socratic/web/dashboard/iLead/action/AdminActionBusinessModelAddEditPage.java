package de.atb.socratic.web.dashboard.iLead.action;

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

import java.util.Date;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.authorization.strategies.annotations.AuthorizeInstantiation;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.BusinessModel;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.service.inception.ActionBusinessModelService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.action.detail.ActionBusinessModelDetailPage;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.jboss.solder.logging.Logger;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@AuthorizeInstantiation({UserRole.MANAGER, UserRole.USER, UserRole.SUPER_ADMIN, UserRole.ADMIN})
public class AdminActionBusinessModelAddEditPage extends AdminAction {
    private static final long serialVersionUID = 1L;

    private final Form<Void> businessModelForm;
    // The action to edit/save
    private Action theAction;
    private BusinessModel theBusinessModel;

    protected final InputBorder<String> valuePropositionsValidationBorder;
    protected final InputBorder<String> customerSegmentsValidationBorder;
    protected final InputBorder<String> customerRelationshipsValidationBorder;
    protected final InputBorder<String> channelsValidationBorder;
    protected final InputBorder<String> keyPartnersValidationBorder;
    protected final InputBorder<String> keyActivitiesValidationBorder;
    protected final InputBorder<String> keyResourcesValidationBorder;
    protected final InputBorder<String> revenueStreamValidationBorder;
    protected final InputBorder<String> costStructureValidationBorder;

    protected final TextArea<String> valuePropositionsTextArea;
    protected final TextArea<String> customerSegmentsTextArea;
    protected final TextArea<String> customerRelationshipsTextArea;
    protected final TextArea<String> channelsTextArea;
    protected final TextArea<String> keyPartnersTextArea;
    protected final TextArea<String> keyActivitiesTextArea;
    protected final TextArea<String> keyResourcesTextArea;
    protected final TextArea<String> revenueStreamTextArea;
    protected final TextArea<String> costStructureTextArea;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    ActionService actionService;

    @EJB
    ActionBusinessModelService actionBusinessModelService;

    @Inject
    Logger logger;

    @Inject
    @LoggedInUser
    User loggedInUser;

    public AdminActionBusinessModelAddEditPage(PageParameters parameters) {
        super(parameters);

        // add form to edit new business model
        add(businessModelForm = newBusinessModelForm());

        loadActionAndBusinessModel(parameters.get("id"), parameters.get("businessModelId"));

        // text fields...
        businessModelForm.add(valuePropositionsValidationBorder = newTextField("valuePropositionsValidationBorder",
                valuePropositionsTextArea = newTextArea("valuePropositions")));

        businessModelForm.add(customerSegmentsValidationBorder = newTextField("customerSegmentsValidationBorder",
                customerSegmentsTextArea = newTextArea("customerSegments")));

        businessModelForm.add(customerRelationshipsValidationBorder = newTextField("customerRelationshipsValidationBorder",
                customerRelationshipsTextArea = newTextArea("customerRelationships")));

        businessModelForm.add(channelsValidationBorder = newTextField("channelsValidationBorder",
                channelsTextArea = newTextArea("channels")));

        businessModelForm.add(keyPartnersValidationBorder = newTextField("keyPartnersValidationBorder",
                keyPartnersTextArea = newTextArea("keyPartners")));

        businessModelForm.add(keyActivitiesValidationBorder = newTextField("keyActivitiesValidationBorder",
                keyActivitiesTextArea = newTextArea("keyActivities")));

        businessModelForm.add(keyResourcesValidationBorder = newTextField("keyResourcesValidationBorder",
                keyResourcesTextArea = newTextArea("keyResources")));

        businessModelForm.add(revenueStreamValidationBorder = newTextField("revenueStreamValidationBorder",
                revenueStreamTextArea = newTextArea("revenueStream")));

        businessModelForm.add(costStructureValidationBorder = newTextField("costStructureValidationBorder",
                costStructureTextArea = newTextArea("costStructure")));

        // Add all help texts for all the fields..
        valuePropositionsValidationBorder.add(addToolTipWebMarkupContainer("valuePropositionsHelpText",
                new StringResourceModel("valuePropositions.desc.label", this, null), TooltipConfig.Placement.right));

        customerSegmentsValidationBorder.add(addToolTipWebMarkupContainer("customerSegmentsHelpText", new StringResourceModel(
                "customerSegments.desc.label", this, null), TooltipConfig.Placement.right));

        customerRelationshipsValidationBorder.add(addToolTipWebMarkupContainer("customerRelationshipsHelpText",
                new StringResourceModel("customerRelationships.desc.label", this, null), TooltipConfig.Placement.right));

        channelsValidationBorder.add(addToolTipWebMarkupContainer("channelsHelpText", new StringResourceModel(
                "channels.desc.label", this, null), TooltipConfig.Placement.right));

        keyPartnersValidationBorder.add(addToolTipWebMarkupContainer("keyPartnersHelpText", new StringResourceModel(
                "keyPartners.desc.label", this, null), TooltipConfig.Placement.right));

        keyActivitiesValidationBorder.add(addToolTipWebMarkupContainer("keyActivitiesHelpText", new StringResourceModel(
                "keyActivities.desc.label", this, null), TooltipConfig.Placement.right));

        keyResourcesValidationBorder.add(addToolTipWebMarkupContainer("keyResourcesHelpText", new StringResourceModel(
                "keyResources.desc.label", this, null), TooltipConfig.Placement.right));

        revenueStreamValidationBorder.add(addToolTipWebMarkupContainer("revenueStreamHelpText", new StringResourceModel(
                "revenueStream.desc.label", this, null), TooltipConfig.Placement.right));

        costStructureValidationBorder.add(addToolTipWebMarkupContainer("costStructureHelpText", new StringResourceModel(
                "costStructure.desc.label", this, null), TooltipConfig.Placement.right));

        // pop up window
        final WebMarkupContainer wmc = new WebMarkupContainer("popupContainer") {
            /**
             *
             */
            private static final long serialVersionUID = 7936000079422415081L;

            @Override
            protected void onConfigure() {
                super.onConfigure();

                // Display pop up message to Action Leader, do so until he does not set all fields of BM
                boolean allParaSet = (theBusinessModel.getChannels() != null && theBusinessModel.getCostStructure() != null
                        && theBusinessModel.getCustomerRelationships() != null && theBusinessModel.getKeyActivities() != null
                        && theBusinessModel.getKeyPartners() != null && theBusinessModel.getKeyResources() != null && theBusinessModel
                        .getRevenueStream() != null) ? true : false;

                if (loggedInUser.equals(theAction.getPostedBy()) && !allParaSet) {
                    setVisible(true);
                } else {
                    setVisible(false);
                }
            }
        };
        wmc.add(new Label("popupHeader", new StringResourceModel("popup.model.window.header", this, null)));
        wmc.add(new Label("popupPara1", new StringResourceModel("popup.model.window.para1", this, null)));
        wmc.add(new Label("popupPara2", new StringResourceModel("popup.model.window.para2", this, null)));
        wmc.add(new Label("popupList1", new StringResourceModel("popup.model.window.list1", this, null)));
        wmc.add(new Label("popupList2", new StringResourceModel("popup.model.window.list2", this, null)));
        add(wmc);

        // add a button to create new campaign
        businessModelForm.add(newSubmitLink("submitTop"));
        businessModelForm.add(newSubmitLink("submitBottom"));
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentSideTab(response, "businessModelTab");
    }

    private TextArea<String> newTextArea(String id) {
        return new TextArea<String>(id, new PropertyModel<String>(theBusinessModel, id));
    }

    /**
     * @return
     */
    private InputBorder<String> newTextField(String id, TextArea<String> textArea) {
        return new OnEventInputBeanValidationBorder<String>(id, textArea, new Model<String>(), HtmlEvent.ONCHANGE);
    }

    /**
     * @param actionId
     * @param businessModelId
     */
    protected void loadActionAndBusinessModel(final StringValue actionId, final StringValue businessModelId) {
        try {
            theAction = actionService.getById(actionId.toOptionalLong());

            if (businessModelId == null || businessModelId.isEmpty()) {
                businessModelForm.add(new Label("header", new StringResourceModel("form.create.entre.header", this, null)));
                theBusinessModel = new BusinessModel();
            } else {
                businessModelForm.add(new Label("header", new StringResourceModel("form.edit.entre.header", this, null)));

                try {
                    theBusinessModel = actionBusinessModelService.getById(businessModelId.toOptionalLong());
                    // check if action leader is the loggedIn user, if not then redirect this page to details page
                    if (!theAction.isEditableBy(loggedInUser)) {
                        setResponsePage(ActionBusinessModelDetailPage.class, new PageParameters().set("id", theAction.getId())
                                .set("businessModelId", theBusinessModel.getId()));
                    }
                } catch (Exception e) {
                    throw new RestartResponseException(ErrorPage.class);
                }
            }

        } catch (Exception e) {
            throw new RestartResponseException(ErrorPage.class);
        }
    }

    /**
     * @return
     */
    private Form<Void> newBusinessModelForm() {
        Form<Void> form = new InputValidationForm<Void>("form") {
            private static final long serialVersionUID = -7704060490162231565L;

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
            }
        };
        form.setOutputMarkupId(true);
        return form;
    }

    private AjaxSubmitLink newSubmitLink(String id) {
        return new AjaxSubmitLink(id, businessModelForm) {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onConfigure() {
                super.onConfigure();

                if (theBusinessModel.getId() == null) {
                    this.add(new AttributeModifier("value", new StringResourceModel("businessModel.create.button", this, null)
                            .getString()));
                } else {
                    this.add(new AttributeModifier("value", new StringResourceModel("businessModel.update.button", this, null)
                            .getString()));
                }
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                save(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                showErrors(target);
            }
        };
    }

    /**
     *
     */
    protected void save(AjaxRequestTarget target) {
        submitActionBusinessModel(target);
        // success message has to be associated to session so that it is shown
        // in the global feedback panel
        Session.get().success(new StringResourceModel("saved.message", this, Model.of(theBusinessModel)).getString());
        // Redirect to ActionBusinessModelDetailPage
        setResponsePage(ActionBusinessModelDetailPage.class,
                new PageParameters().set("id", theAction.getId()).set("businessModelId", theBusinessModel.getId()));
    }

    private void submitActionBusinessModel(AjaxRequestTarget target) {

        // set last modified date
        theAction.setLastModified(new Date());

        if (theBusinessModel.getId() == null) {
            createActionBusinessModel(target);
        } else {
            updateActionBusinessModel(target);
        }

    }

    /**
     * @param target
     */
    private void updateActionBusinessModel(AjaxRequestTarget target) {

        // update actionBusinessModel
        updateActionBusinessModelFromForm(target);
        theBusinessModel = actionBusinessModelService.update(theBusinessModel);

        theAction = actionService.update(theAction);

        // show feedback panel
        target.add(feedbackPanel);
    }

    /**
     * @param target
     */
    private void createActionBusinessModel(AjaxRequestTarget target) {
        // and add action to the campaign
        updateActionBusinessModelFromForm(target);
        theBusinessModel = actionBusinessModelService.create(theBusinessModel);

        // add BusinessModeal to an Action
        theAction.setBusinessModel(theBusinessModel);
        actionService.update(theAction);

        // show success message
        getPage().success(new StringResourceModel("created.message", this, null).getString());

        // show feedback panel
        target.add(feedbackPanel);
    }

    /**
     * @param target
     */
    private void updateActionBusinessModelFromForm(AjaxRequestTarget target) {

        theBusinessModel.setValuePropositions(valuePropositionsTextArea.getModelObject());
        theBusinessModel.setCustomerSegments(customerSegmentsTextArea.getModelObject());
        theBusinessModel.setCustomerRelationships(customerRelationshipsTextArea.getModelObject());
        theBusinessModel.setChannels(channelsTextArea.getModelObject());
        theBusinessModel.setKeyPartners(keyPartnersTextArea.getModelObject());
        theBusinessModel.setKeyActivities(keyActivitiesTextArea.getModelObject());
        theBusinessModel.setKeyResources(keyResourcesTextArea.getModelObject());
        theBusinessModel.setRevenueStream(revenueStreamTextArea.getModelObject());
        theBusinessModel.setCostStructure(costStructureTextArea.getModelObject());
    }

    /**
     * @param target
     */
    protected void showErrors(AjaxRequestTarget target) {
        // in case of errors (e.g. validation errors) show error
        // messages in form
        target.add(valuePropositionsTextArea);
        target.add(customerSegmentsTextArea);
        target.add(customerRelationshipsTextArea);
        target.add(channelsTextArea);
        target.add(keyPartnersTextArea);
        target.add(keyActivitiesTextArea);
        target.add(keyResourcesTextArea);
        target.add(revenueStreamTextArea);
        target.add(costStructureTextArea);

        // also reload feedback panel
        target.add(feedbackPanel);
    }
}
