package de.atb.socratic.web.action;

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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.authorization.strategies.annotations.AuthorizeInstantiation;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.validation.ProfilePictureInputValidator;
import de.atb.socratic.service.inception.ActionIterationService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.components.resource.ActionIterationPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.inception.CampaignAddEditPage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.upload.FileUploadHelper;
import de.atb.socratic.web.upload.FileUploadHelper.UploadType;
import org.apache.tika.mime.MediaType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.jboss.solder.logging.Logger;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@AuthorizeInstantiation({UserRole.MANAGER, UserRole.USER, UserRole.SUPER_ADMIN, UserRole.ADMIN})
public class ActionIterationAddPage extends BasePage {
    private static final long serialVersionUID = 1L;

    private final Form<Void> actionForm;
    // The action to edit/save
    private Action theAction;
    private ActionIteration theIteration;

    protected final InputBorder<String> titleValidationBorder;
    protected final InputBorder<String> aimOfExperimentValidationBorder;
    protected final InputBorder<String> methodologyValidationBorder;
    protected final InputBorder<String> planValidationBorder;
    protected final InputBorder<String> lessonsLearntValidationBorder;

    protected final TextArea<String> titleTextArea;
    protected final TextArea<String> aimOfExperimentTextArea;
    protected final TextArea<String> methodologyTextArea;
    protected final TextArea<String> planTextArea;
    protected final TextArea<String> lessonsLearntTextArea;
    private static final JavaScriptResourceReference UPLOADED_IMAGE_PREVIEW_JS = new JavaScriptResourceReference(CampaignAddEditPage.class, "uploadImagePreview.js");
    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    ActionService actionService;

    @EJB
    ActionIterationService actionIterationService;

    @Inject
    Logger logger;

    @Inject
    @LoggedInUser
    User loggedInUser;

    // iteration Image
    private final FileUploadField iterationImageUploadField;
    private NonCachingImage iterationImageFile;
    private final FileUploadHelper iterationImageUploadHelper = new FileUploadHelper(UploadType.CHALLENGE);
    private final AjaxLink<Void> clearIterationImageLink;

    public ActionIterationAddPage(PageParameters parameters) {
        super(parameters);

        // add form to create new action
        add(actionForm = newActionForm());

        loadAction(parameters.get("actionid"), parameters.get("iterationId"));

        actionForm.add(titleValidationBorder = newTextField("titleValidationBorder", titleTextArea = newTextArea("title")));

        actionForm.add(aimOfExperimentValidationBorder = newTextField("aimOfExperimentValidationBorder",
                aimOfExperimentTextArea = newTextArea("aimOfExperiment")));

        actionForm.add(methodologyValidationBorder = newTextField("methodologyValidationBorder",
                methodologyTextArea = newTextArea("methodology")));

        actionForm.add(planValidationBorder = newTextField("planValidationBorder", planTextArea = newTextArea("plan")));

        // check if iteration is in edit mode?
        Label lessonsLearntLabel = new Label("lessonsLearntLabel", new StringResourceModel(
                "actionIteration.lessonsLearnt.input.label", this, null));
        lessonsLearntLabel.setVisible(theIteration.getId() != null);
        actionForm.add(lessonsLearntLabel);

        lessonsLearntTextArea = newTextArea("lessonsLearnt");
        lessonsLearntTextArea.setVisible(theIteration.getId() != null);
        actionForm.add(lessonsLearntValidationBorder = newTextField("lessonsLearntValidationBorder", lessonsLearntTextArea));

        // Add all help texts for all the fields..
        titleValidationBorder.add(addToolTipWebMarkupContainer("iteraionTitleHelpText", new StringResourceModel(
                "actionIteration.title.desc.label", this, null), TooltipConfig.Placement.right));
        aimOfExperimentValidationBorder.add(addToolTipWebMarkupContainer("iteraionAimOfExperimentHelpText",
                new StringResourceModel("actionIteration.aimOfExperiment.desc.label", this, null), TooltipConfig.Placement.right));
        methodologyValidationBorder.add(addToolTipWebMarkupContainer("iteraionMethodologyHelpText", new StringResourceModel(
                "actionIteration.methodology.desc.label", this, null), TooltipConfig.Placement.right));
        planValidationBorder.add(addToolTipWebMarkupContainer("iteraionPlanHelpText", new StringResourceModel(
                "actionIteration.plan.desc.label", this, null), TooltipConfig.Placement.right));

        lessonsLearntValidationBorder.add(addToolTipWebMarkupContainer("iteraionLessonsLearntHelpText",
                new StringResourceModel("actionIteration.lessonsLearnt.desc.label", this, null), TooltipConfig.Placement.right)
                .setVisible(theIteration.getId() != null));

        actionForm.add(addToolTipWebMarkupContainer("iterationImageHelpText", new StringResourceModel("iteration.image.desc.label", this,
                null), TooltipConfig.Placement.right));

        // Add iteration Image
        iterationImageUploadField = new FileUploadField("profilePicture");
        actionForm.add(new InputBorder<>("profilePictureBorder", iterationImageUploadField, new Model<String>()));
        actionForm.add(new ProfilePictureInputValidator(iterationImageUploadField));
        actionForm.add(newProfilePicturePreview());
        actionForm.add(clearIterationImageLink = newProfileClearButton());

        // add a button to create new campaign
        actionForm.add(newSubmitLink("submitTop"));
        actionForm.add(newSubmitLink("submitBottom"));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forReference(UPLOADED_IMAGE_PREVIEW_JS));
    }

    private NonCachingImage newProfilePicturePreview() {
        iterationImageFile = new NonCachingImage("profilePicturePreview",
                ActionIterationPictureResource.get(PictureType.PROFILE, theIteration));
        iterationImageFile.setOutputMarkupId(true);
        return iterationImageFile;
    }

    private AjaxLink<Void> newProfileClearButton() {
        return new AjaxLink<Void>("profilePictureClear") {
            private static final long serialVersionUID = -1439023360594387449L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(theIteration != null && theIteration.getIterationImage() != null);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                theIteration.setIterationImage(null);
                iterationImageFile.setImageResource(ActionIterationPictureResource.get(PictureType.PROFILE, theIteration));
                target.add(iterationImageFile);
                target.add(clearIterationImageLink);
            }
        };
    }

    private TextArea<String> newTextArea(String id) {
        return new TextArea<String>(id, new PropertyModel<String>(theIteration, id));
    }

    /**
     * @return
     */
    private InputBorder<String> newTextField(String id, TextArea<String> textArea) {
        return new OnEventInputBeanValidationBorder<String>(id, textArea, new Model<String>(), HtmlEvent.ONCHANGE);
    }

    /**
     * @param actionId
     */
    protected void loadAction(final StringValue actionId, final StringValue iterationId) {
        try {
            theAction = actionService.getById(actionId.toOptionalLong());

            if (iterationId == null || iterationId.isEmpty()) {
                actionForm.add(new Label("header", new StringResourceModel("form.create.entre.header", this, null)));
                theIteration = new ActionIteration();
            } else {
                actionForm.add(new Label("header", new StringResourceModel("form.edit.entre.header", this, null)));

                try {
                    theIteration = actionIterationService.getById(iterationId.toOptionalLong());
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
    private Form<Void> newActionForm() {
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
        return new AjaxSubmitLink(id, actionForm) {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                this.add(new AttributeModifier("value", new StringResourceModel("actions.continue.button", this, null)
                        .getString()));
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
        submitActionIteration(target);
        // success message has to be associated to session so that it is shown
        // in the global feedback panel
        Session.get().success(new StringResourceModel("saved.message", this, Model.of(theAction)).getString());
        // redirect to actionPage
        setResponsePage(ActionStatusAddPage.class, new PageParameters().set("id", theAction.getId()));
    }

    private void submitActionIteration(AjaxRequestTarget target) {
        if (theIteration.getId() == null) {
            createActionIteration(target);
        } else {
            updateActionIteration(target);
        }

    }

    /**
     * @param target
     */
    private void updateActionIteration(AjaxRequestTarget target) {

        // update actionIteration
        updateActionIterationFromForm(target);
        theIteration = actionIterationService.update(theIteration);

        // show feedback panel
        target.add(feedbackPanel);
    }

    /**
     * @param target
     */
    private void createActionIteration(AjaxRequestTarget target) {
        // and add action to the campaign
        updateActionIterationFromForm(target);
        theIteration = actionIterationService.create(theIteration);

        // add Iteration to an Action
        actionService.addActionIterationToList(theAction, theIteration);

        // show success message
        getPage().success(new StringResourceModel("created.message", this, null).getString());

        // show feedback panel
        target.add(feedbackPanel);
    }

    /**
     *
     */
    private void updateActionIterationFromForm(AjaxRequestTarget target) {

        // Iteration image update
        final FileInfo profilePicture = saveUploadedPicture(target);
        if (profilePicture != null) {
            theIteration.setIterationImage(profilePicture);
        }
        iterationImageFile.setImageResource(ActionIterationPictureResource.get(PictureType.PROFILE, theIteration));
        target.add(iterationImageFile);

        theIteration.setTitle(titleTextArea.getModelObject());
        theIteration.setAimOfExperiment(aimOfExperimentTextArea.getModelObject());
        theIteration.setMethodology(methodologyTextArea.getModelObject());
        theIteration.setPlan(planTextArea.getModelObject());
        theIteration.setLessonsLearnt(lessonsLearntTextArea.getModelObject());
        theIteration.setPostedBy(loggedInUser);

        // add action to Iteration
        theIteration.setAction(theAction);

    }

    /**
     * @param target
     * @return
     */
    private FileInfo saveUploadedPicture(AjaxRequestTarget target) {
        FileUpload image = iterationImageUploadField.getFileUpload();
        if (image != null) {
            if (isUploadedFileAnImage(image)) {
                final File uploadFolder = iterationImageUploadHelper.createUploadFolderWithDir(UUID.randomUUID().toString(), null);
                try {
                    ActionIterationPictureResource.createProfilePictureFromUpload(image, uploadFolder, theIteration, PictureType.PROFILE);
                } catch (IOException e) {
                    logger.error(e);
                }

                try {
                    return ActionIterationPictureResource.createProfilePictureFromUpload(image, uploadFolder, theIteration,
                            PictureType.THUMBNAIL);
                } catch (IOException e) {
                    logger.error(e);
                }

            } else {
                getPage().warn("Only GIF, PNG and JPG files are allowed to be uploaded!");
                target.add(feedbackPanel);
            }
        }
        return null;
    }

    private boolean isUploadedFileAnImage(FileUpload image) {
        MediaType type = MediaType.parse(image.getContentType());
        if (!"image".equalsIgnoreCase(type.getType()))
            return false;
        boolean gif = "gif".equalsIgnoreCase(type.getSubtype());
        boolean png = "png".equalsIgnoreCase(type.getSubtype());
        boolean jpg = "jpeg".equalsIgnoreCase(type.getSubtype());
        return gif || png || jpg;
    }

    /**
     * @param target
     */
    protected void showErrors(AjaxRequestTarget target) {
        // in case of errors (e.g. validation errors) show error
        // messages in form
        target.add(titleTextArea);
        target.add(aimOfExperimentTextArea);
        target.add(methodologyTextArea);
        target.add(planTextArea);
        target.add(lessonsLearntTextArea);

        target.add(actionForm);
        // also reload feedback panel
        target.add(feedbackPanel);
    }
}
