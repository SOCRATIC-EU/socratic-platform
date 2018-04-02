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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.EJB;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.vaynberg.wicket.select2.Select2MultiChoice;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.authorization.strategies.annotations.AuthorizeInstantiation;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.BusinessModel;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.IdeaType;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.event.ActionTagAdded;
import de.atb.socratic.model.validation.ProfilePictureInputValidator;
import de.atb.socratic.service.inception.ActionBusinessModelService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.ActionTeamToolService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.other.TagService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.action.detail.ActionSolutionPage;
import de.atb.socratic.web.components.FileUploadBehavior;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.components.resource.ActionPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.dashboard.iLead.action.AdminActionTeamToolPanel;
import de.atb.socratic.web.inception.CampaignAddEditPage;
import de.atb.socratic.web.provider.TagProvider;
import de.atb.socratic.web.qualifier.FileUploadCache;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.upload.FileUploadHelper;
import de.atb.socratic.web.upload.FileUploadHelper.UploadType;
import de.atb.socratic.web.upload.FileUploadInfo;
import org.apache.tika.mime.MediaType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.StringValue;
import org.jboss.solder.logging.Logger;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@AuthorizeInstantiation({UserRole.MANAGER, UserRole.USER, UserRole.SUPER_ADMIN, UserRole.ADMIN})
public class ActionAddEditPage extends BasePage {
    private static final long serialVersionUID = 1L;

    private final Form<Void> actionForm;

    protected final InputBorder<String> shortTextValidationBorder;
    protected final InputBorder<String> elevatorPitchValidationBorder;
    protected final InputBorder<String> descriptionValidationBorder;
    protected final InputBorder<String> beneficiariesValidationBorder;
    protected final InputBorder<Collection<Tag>> keywordsValidationBorder;
    protected final InputBorder<Collection<Tag>> skillsValidationBorder;

    protected final TextArea<String> shortTextTextArea;
    protected final TextArea<String> elevatorPitchTextArea;
    protected final TextArea<String> descriptionTextArea;
    protected final TextArea<String> beneficiariesTextArea;

    protected final InputBorder<String> reasonForBringingIdeaForwardValidationBorder;
    protected final InputBorder<String> locationValidationBorder;
    protected final InputBorder<String> implementationPlanValidationBorder;
    protected final InputBorder<String> resourcesForIdeaImplementationValidationBorder;
    protected final InputBorder<String> impactStakeholdersValidationBorder;
    protected final InputBorder<String> valueForBeneficiariesValidationBorder;
    protected final InputBorder<String> relatedInnovationsValidationBorder;

    protected final TextArea<String> valueForBeneficiariesTextArea;
    protected final TextArea<String> impactStakeholdersTextArea;
    protected final TextArea<String> resourcesForIdeaImplementationTextArea;
    protected final TextArea<String> implementationPlanTextArea;
    protected final TextArea<String> locationTextArea;
    protected final TextArea<String> reasonForBringingIdeaForwardTextArea;
    protected final TextArea<String> relatedInnovationsTextArea;

    private final Select2MultiChoice<Tag> keywords, skills;

    protected TextArea<String> actionTypeTextArea;
    protected InputBorder<String> actionTypeBorder;
    private ArrayList<IdeaType> actionTypes = new ArrayList<>();
    private final CheckBoxMultipleChoice<IdeaType> listOfActionType;

    // action Image
    private final FileUploadField actionImageUploadField;
    private NonCachingImage actionImageFile;
    private final FileUploadHelper actionImageUploadHelper = new FileUploadHelper(UploadType.CHALLENGE);
    private final AjaxLink<Void> clearActionImageLink;
    private static final JavaScriptResourceReference UPLOADED_IMAGE_PREVIEW_JS = new JavaScriptResourceReference(CampaignAddEditPage.class, "uploadImagePreview.js");

    // action team tools
    private final AdminActionTeamToolPanel adminActionTeamToolPanel;

    // The action to edit/save
    private Action theAction;
    private Idea theIdea;
    //private Campaign theCampaign;

    // Upload Supporting Documents
    private boolean actionSupportingDocUploadFileUploadVisible;
    private final WebMarkupContainer actionSupportingDocUploadFileTableBody;
    private final HiddenField<String> actionSupportingDocUploadHiddenField;
    private final FileUploadField actionSupportingDocUploadField;

    @Inject
    @FileUploadCache
    FileUploadInfo actionSupportingDocFileUploadInfo;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    ActionService actionService;

    @EJB
    ActionBusinessModelService actionBusinessModelService;

    @EJB
    IdeaService ideaService;

    @EJB
    UserService userService;

    @EJB
    ActionTeamToolService actionTeamToolService;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    TagService tagService;

    @Inject
    Logger logger;

    @Inject
    Event<ActionTagAdded> actionTagEventSource;

    public ActionAddEditPage(PageParameters parameters) {
        super(parameters);

        // add js and css for file upload
        add(new FileUploadBehavior());

        // add form to create new action
        add(actionForm = newActionForm());

        // extract id parameter and set page title, header and action
        // depending on whether we are editing an existing action or creating
        // a new one
        loadAction(parameters.get("id"), parameters.get("ideaId"));

        // Add Basic fields (required)
        // add text field for name inside a border component that performs bean
        // validation
        actionForm.add(shortTextValidationBorder = newTextField("shortTextValidationBorder",
                shortTextTextArea = newTextArea("shortText")));
        actionForm.add(elevatorPitchValidationBorder = newTextField("elevatorPitchValidationBorder",
                elevatorPitchTextArea = newTextArea("elevatorPitch")));
        actionForm.add(descriptionValidationBorder = newTextField("descriptionValidationBorder",
                descriptionTextArea = newTextArea("description")));
        actionForm.add(beneficiariesValidationBorder = newTextField("beneficiariesValidationBorder",
                beneficiariesTextArea = newTextArea("beneficiaries")));
        // add text input field for tags
        keywords = newKeywordsSelect2MultiChoice();
        actionForm.add(keywordsValidationBorder = new InputBorder<>("keywordsValidationBorder", keywords));

        // add text input field for skills
        actionForm.add(skillsValidationBorder = new InputBorder<>("skillsValidationBorder",
                skills = newSkillsSelect2MultiChoice()));

        /****** Add Optional fields *****/

        // Add action Image
        actionImageUploadField = new FileUploadField("profilePicture");
        actionForm.add(new InputBorder<>("profilePictureBorder", actionImageUploadField, new Model<String>()));
        actionForm.add(new ProfilePictureInputValidator(actionImageUploadField));
        // actionForm.add(new Label("profilePicturePreviewLabel", new StringResourceModel("profile.picture.preview.label", this,
        // null)));
        actionForm.add(newProfilePicturePreview());
        actionForm.add(clearActionImageLink = newProfileClearButton());

        actionForm.add(valueForBeneficiariesValidationBorder = newTextField("valueForBeneficiariesValidationBorder",
                valueForBeneficiariesTextArea = newTextArea("valueForBeneficiaries")));
        actionForm.add(impactStakeholdersValidationBorder = newTextField("impactStakeholdersValidationBorder",
                impactStakeholdersTextArea = newTextArea("impactStakeholders")));

        actionForm.add(resourcesForIdeaImplementationValidationBorder = newTextField(
                "resourcesForIdeaImplementationValidationBorder",
                resourcesForIdeaImplementationTextArea = newTextArea("resourcesForActionImplementation")));
        actionForm.add(implementationPlanValidationBorder = newTextField("implementationPlanValidationBorder",
                implementationPlanTextArea = newTextArea("implementationPlan")));
        actionForm.add(locationValidationBorder = newTextField("locationValidationBorder",
                locationTextArea = newTextArea("location")));
        actionForm.add(reasonForBringingIdeaForwardValidationBorder = newTextField(
                "reasonForBringingIdeaForwardValidationBorder",
                reasonForBringingIdeaForwardTextArea = newTextArea("reasonForBringingActionForward")));
        actionForm.add(relatedInnovationsValidationBorder = newTextField("relatedInnovationsValidationBorder",
                relatedInnovationsTextArea = newTextArea("relatedInnovations")));
        // Add Idea Type
        listOfActionType = new CheckBoxMultipleChoice<>("ideaTypes", Model.<ArrayList<IdeaType>>of(actionTypes),
                IdeaType.getAll());
        listOfActionType.setRequired(true);
        actionForm.add(new InputBorder<>("ideaTypesValidationBorder", listOfActionType));
        // Add text area for Other Idea Type
        actionForm.add(actionTypeBorder = newActionTypeTextField("ideaTypeBorder",
                actionTypeTextArea = newTextArea("actionTypeText")));

        // Add all help texts for all the fields..
        shortTextValidationBorder.add(addToolTipWebMarkupContainer("ideaTitleHelpText", new StringResourceModel(
                "basicFields.idea.title.desc.label", this, null), TooltipConfig.Placement.right));
        elevatorPitchValidationBorder.add(addToolTipWebMarkupContainer("ideaElevatorPitchHelpText", new StringResourceModel(
                "basicFields.idea.elevatorpitch.desc.label", this, null), TooltipConfig.Placement.right));
        actionTypeBorder.add(addToolTipWebMarkupContainer("ideaTypeHelpText", new StringResourceModel(
                "basicFields.idea.type.desc.label", this, null), TooltipConfig.Placement.right));
        descriptionValidationBorder.add(addToolTipWebMarkupContainer("ideaDescriptionHelpText", new StringResourceModel(
                "basicFields.idea.description.desc.label", this, null), TooltipConfig.Placement.right));
        beneficiariesValidationBorder.add(addToolTipWebMarkupContainer("ideaBeneficiariesHelpText", new StringResourceModel(
                "basicFields.idea.beneficiaries.desc.label", this, null), TooltipConfig.Placement.right));
        keywordsValidationBorder.add(addToolTipWebMarkupContainer("ideaKeywordsText", new StringResourceModel(
                "basicFields.idea.keywords.desc.label", this, null), TooltipConfig.Placement.right));
        actionForm.add(addToolTipWebMarkupContainer("ideaImageHelpText", new StringResourceModel("idea.image.desc.label", this,
                null), TooltipConfig.Placement.right));
        valueForBeneficiariesValidationBorder.add(addToolTipWebMarkupContainer("ideaValueForTheBeneficiariesHelpText",
                new StringResourceModel("optionalFields.idea.valueforbeneficiaries.desc.label", this, null),
                TooltipConfig.Placement.right));
        impactStakeholdersValidationBorder.add(addToolTipWebMarkupContainer("ideaImpactHelpText", new StringResourceModel(
                "optionalFields.idea.impactonstakeholders.desc.label", this, null), TooltipConfig.Placement.right));
        skillsValidationBorder.add(addToolTipWebMarkupContainer("ideaSkillsHelpText", new StringResourceModel(
                "optionalFields.idea.skillsforideaimplementation.desc.label", this, null), TooltipConfig.Placement.right));
        resourcesForIdeaImplementationValidationBorder.add(addToolTipWebMarkupContainer("ideaResourcesHelpText",
                new StringResourceModel("optionalFields.idea.resourcesforideaimplementation.desc.label", this, null),
                TooltipConfig.Placement.right));
        implementationPlanValidationBorder.add(addToolTipWebMarkupContainer("ideaImplementationPlanHelpText",
                new StringResourceModel("optionalFields.idea.outlineforideaimplementation.desc.label", this, null),
                TooltipConfig.Placement.right));
        locationValidationBorder.add(addToolTipWebMarkupContainer("ideaLocationHelpText", new StringResourceModel(
                "optionalFields.idea.location.desc.label", this, null), TooltipConfig.Placement.right));
        reasonForBringingIdeaForwardValidationBorder.add(addToolTipWebMarkupContainer("ideaPersonHelpText",
                new StringResourceModel("optionalFields.idea.person.desc.label", this, null), TooltipConfig.Placement.right));
        relatedInnovationsValidationBorder.add(addToolTipWebMarkupContainer("ideaRelatedInnovationsHelpText",
                new StringResourceModel("optionalFields.idea.relatedInnovations.desc.label", this, null),
                TooltipConfig.Placement.right));

        // action team tools
        adminActionTeamToolPanel = new AdminActionTeamToolPanel("adminActionTeamToolPanel", Model.of(theAction), feedbackPanel,
                actionForm) {
            private static final long serialVersionUID = -8442868380170663888L;
        };
        adminActionTeamToolPanel.setOutputMarkupId(true);
        actionForm.add(adminActionTeamToolPanel);

        // Add Supporting Documents (optional)
        actionForm.add(new AjaxLink<Void>("showAttachmentsForm") {
            private static final long serialVersionUID = 2414445280074210649L;

            // add a link to display file upload form
            @Override
            public void onClick(AjaxRequestTarget target) {
                toggleAttachmentsForm(target);
                actionSupportingDocUploadFileUploadVisible = !actionSupportingDocUploadFileUploadVisible;
            }
        });

        // for file uploads
        actionSupportingDocUploadFileTableBody = new WebMarkupContainer("fileTableBody");
        actionForm.add(actionSupportingDocUploadFileTableBody.setOutputMarkupId(true));
        actionSupportingDocUploadHiddenField = new HiddenField<>("uploadCacheId", new PropertyModel<String>(theAction,
                "attachmentsCacheId"));
        actionForm.add(actionSupportingDocUploadHiddenField.setOutputMarkupId(true));
        actionForm.setMaxSize(Bytes.megabytes(10));
        actionForm.setMultiPart(true);
        actionSupportingDocUploadField = new FileUploadField("fileupload");
        actionForm.add(actionSupportingDocUploadField.setOutputMarkupId(true));
        // fill the fileUploadInfo cache with any existing attachments
        updateAttachmentsCache();

        // add a button to create new campaign
        actionForm.add(newSubmitLink("submitTop"));
        actionForm.add(newSubmitLink("submitBottom"));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forReference(UPLOADED_IMAGE_PREVIEW_JS));
    }

    /**
     * @param target
     */
    private void toggleAttachmentsForm(AjaxRequestTarget target) {
        target.appendJavaScript(String.format(JSTemplates.TOGGLE_COLLAPSE, ".attachments"));
    }

    /**
     *
     */
    private void updateAttachmentsCache() {
        // cleanup the cache
        clearFileUploadCache();
        // add existing attachments to cache
        Set<FileInfo> fileInfos = theAction.getAttachments();
        if (!fileInfos.isEmpty()) {
            actionSupportingDocFileUploadInfo.addFileInfo(actionSupportingDocUploadHiddenField.getModelObject(),
                    fileInfos.toArray(new FileInfo[fileInfos.size()]));
        }
    }

    /**
     * Clears the file upload cache and deletes all uploaded files.
     */
    private void clearFileUploadCache() {
        actionSupportingDocFileUploadInfo.cleanup(actionSupportingDocUploadHiddenField.getModelObject());
    }

    /**
     * @param actionId
     */
    protected void loadAction(final StringValue actionId, final StringValue ideaId) {
        if (actionId == null || actionId.isEmpty()) {
            actionForm.add(new Label("header", new StringResourceModel("form.create.entre.header", this, null)));
            theAction = new Action();


            // for new Action, copy Idea's fields.... 
            if (ideaId != null && !ideaId.isEmpty()) {
                try {
                    theIdea = ideaService.getById(ideaId.toOptionalLong());
                    theAction.setIdea(theIdea);

                    // fill the content of action from an idea
                    theAction.setShortText(theIdea.getShortText());

                    // for action type, check if idea type includes "other"?
                    if (!theIdea.getIdeaType().isEmpty() && theIdea.getIdeaType().contains(IdeaType.Others)
                            && theIdea.getIdeaTypeText() != null) {
                        theAction.setActionTypeText(theIdea.getIdeaTypeText());
                    }

                    if (!theIdea.getIdeaType().isEmpty()) {
                        theAction.setActionType(theIdea.getIdeaType());
                    }

                    // also, preset actionTypes 
                    actionTypes = new ArrayList<IdeaType>(theIdea.getIdeaType());

                    theAction.setActionTypeText(theIdea.getIdeaTypeText());
                    theAction.setBeneficiaries(theIdea.getBeneficiaries());
                    theAction.setDescription(theIdea.getDescription());
                    theAction.setElevatorPitch(theIdea.getElevatorPitch());
                    theAction.setImplementationPlan(theIdea.getImplementationPlan());
                    theAction.setKeywords(theIdea.getKeywords());
                    theAction.setLocation(theIdea.getLocation());
                    theAction.setPostedAt(new Date());
                    theAction.setPostedBy(loggedInUser);
                    theAction.setReasonForBringingActionForward(theIdea.getReasonForBringingIdeaForward());
                    theAction.setRelatedInnovations(theIdea.getRelatedInnovations());
                    theAction.setResourcesForActionImplementation(theIdea.getResourcesForIdeaImplementation());
                    theAction.setSkills(theIdea.getSkills());
                    theAction.setValueForBeneficiaries(theIdea.getValueForBeneficiaries());

                    //actionService.update(theAction);
                } catch (Exception e) {
                    throw new RestartResponseException(ErrorPage.class);
                }
            }

        } else {
            actionForm.add(new Label("header", new StringResourceModel("form.edit.entre.header", this, null)));
            try {
                theAction = actionService.getById(actionId.toOptionalLong());

                theIdea = ideaService.getById(ideaId.toOptionalLong());

                // check if action leader is the loggedIn user, if not then redirect this page to details page
                if (!theAction.getPostedBy().equals(loggedInUser)) {
                    setResponsePage(ActionSolutionPage.class, new PageParameters().set("id", theAction.getId()));
                }

                // set IdeaTypes
                actionTypes = new ArrayList<IdeaType>(theAction.getActionType());
            } catch (Exception e) {
                throw new RestartResponseException(ErrorPage.class);
            }
        }
    }

    /**
     * @return
     */
    private Form<Void> newActionForm() {
        Form<Void> form = new InputValidationForm<Void>("form") {

            private static final long serialVersionUID = 8895354815385639572L;

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);

                // toggle fileUpload div collapse
                response.render(OnDomReadyHeaderItem.forScript(getToggleFileUploadDivScript()));

                // add javascript to load jquery file upload plugin
                response.render(OnDomReadyHeaderItem.forScript(getFileUploadScript()));
            }
        };
        form.setOutputMarkupId(true);
        return form;
    }

    /**
     * @return
     */
    private String getToggleFileUploadDivScript() {
        if (actionSupportingDocUploadFileUploadVisible) {
            return String.format(JSTemplates.SHOW_COLLAPSE, ".attachments");
        } else {
            return String.format(JSTemplates.CLOSE_COLLAPSE, ".attachments");
        }
    }

    /**
     * @return
     */
    private String getFileUploadScript() {
        return String.format(
                JSTemplates.LOAD_FILE_UPLOAD,
                JSTemplates.getContextPath(),
                actionSupportingDocUploadField.getMarkupId(),
                actionSupportingDocUploadHiddenField.getMarkupId(),
                actionSupportingDocUploadFileTableBody.getMarkupId());
    }

    /**
     * @return
     */
    private InputBorder<String> newTextField(String id, TextArea<String> textArea) {
        return new OnEventInputBeanValidationBorder<String>(id, textArea, new Model<String>(), HtmlEvent.ONCHANGE);
    }

    private TextArea<String> newTextArea(String id) {
        return new TextArea<String>(id, new PropertyModel<String>(theAction, id));
    }

    /**
     * @return
     */
    private InputBorder<String> newActionTypeTextField(String id, TextArea<String> textArea) {
        return new InputBorder<String>(id, textArea, new Model<String>());
    }

    /**
     * @return
     */
    private Select2MultiChoice<Tag> newSkillsSelect2MultiChoice() {
        Select2MultiChoice<Tag> select2MultiChoice = new Select2MultiChoice<>("skills", new PropertyModel<Collection<Tag>>(
                theAction, "skills"), new TagProvider(tagService.getAll()));
        select2MultiChoice.getSettings().setMinimumInputLength(1);
        select2MultiChoice.getSettings().setCreateSearchChoice(
                "function(term) { if (term.length > 1) { return { id: term, text: term }; } }");
        return select2MultiChoice;
    }

    /**
     * @return
     */
    private Select2MultiChoice<Tag> newKeywordsSelect2MultiChoice() {
        Select2MultiChoice<Tag> select2MultiChoice = new Select2MultiChoice<>("keywords", new PropertyModel<Collection<Tag>>(
                theAction, "keywords"), new TagProvider(tagService.getAll()));
        select2MultiChoice.getSettings().setMinimumInputLength(1);
        select2MultiChoice.getSettings().setCreateSearchChoice(
                "function(term) { if (term.length > 1) { return { id: term, text: term }; } }");
        select2MultiChoice.setRequired(true);
        return select2MultiChoice;
    }

    private NonCachingImage newProfilePicturePreview() {
        actionImageFile = new NonCachingImage("profilePicturePreview",
                ActionPictureResource.get(PictureType.PROFILE, theAction));
        actionImageFile.setOutputMarkupId(true);
        return actionImageFile;
    }

    private AjaxLink<Void> newProfileClearButton() {
        return new AjaxLink<Void>("profilePictureClear") {
            private static final long serialVersionUID = -1439023360594387449L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(theAction != null && theAction.getActionImage() != null);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                theAction.setActionImage(null);
                actionImageFile.setImageResource(ActionPictureResource.get(PictureType.PROFILE, theAction));
                target.add(actionImageFile);
                target.add(clearActionImageLink);
            }
        };
    }

    private AjaxSubmitLink newSubmitLink(String id) {
        return new AjaxSubmitLink(id, actionForm) {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (theAction.getId() == null) {
                    this.add(new AttributeModifier("value", new StringResourceModel("ideas.submitidea.button", this, null)
                            .getString()));
                } else {
                    this.add(new AttributeModifier("value", new StringResourceModel("ideas.updateidea.button", this, null)
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
        submitAction(target);
        // goto next page: Add Team to Action
        setResponsePage(ActionAddTeamPage.class,
                new PageParameters().set("id", theAction.getId()).set("ideaId", theIdea.getId()));
    }

    private void submitAction(AjaxRequestTarget target) {
        if (theAction.getId() == null) {
            createAction(target);
        } else {
            updateAction(target);
        }

        // fire an event to check if given Action skills and interests matches to any platform user's skills and interests?
        ActionTagAdded actionSkillAdded = new ActionTagAdded();
        actionSkillAdded.setAction(theAction);
        if (skills.getModelObject() != null && !skills.getModelObject().isEmpty()) {
            actionSkillAdded.getAddedSkillsOrInterest().addAll((List<Tag>) skills.getModelObject());
        }

        if (keywords.getModelObject() != null && !keywords.getModelObject().isEmpty()) {
            actionSkillAdded.getAddedSkillsOrInterest().addAll((List<Tag>) keywords.getModelObject());
        }

        if (actionSkillAdded.getAddedSkillsOrInterest() != null && !actionSkillAdded.getAddedSkillsOrInterest().isEmpty()) {
            actionTagEventSource.fire(actionSkillAdded);
        }

        // reset file uploads
        clearFileUploadCache();

        // set file upload field to hidden
        actionSupportingDocUploadFileUploadVisible = false;

        // Set Idea's isActionCreated boolean to true
        theIdea.setActionCreated(true);
        ideaService.update(theIdea);

        keywords.setProvider(new TagProvider(tagService.getAll()));
        target.add(keywords);

        skills.setProvider(new TagProvider(tagService.getAll()));
        target.add(skills);
    }

    /**
     * @param target
     */
    private void updateAction(AjaxRequestTarget target) {

        // update action
        updateActionFromForm(target);

        // set last modified date
        theAction.setLastModified(new Date());

        theAction = actionService.update(theAction);

        // notify about updated actions
        actionService.notifyActionFollowersAboutActionUpdates(theAction);

        // show feedback panel
        target.add(feedbackPanel);
    }

    /**
     * @param target
     */
    private void createAction(AjaxRequestTarget target) {
        // and add action to the campaign
        updateActionFromForm(target);

        // add business model to an action
        BusinessModel businessModel = new BusinessModel();
        businessModel.setValuePropositions(descriptionTextArea.getModelObject());
        businessModel.setCustomerSegments(beneficiariesTextArea.getModelObject());
        businessModel = actionBusinessModelService.create(businessModel);
        theAction.setBusinessModel(businessModel);

        theAction = actionService.create(theAction);

        // when being created, add Action to followedActions list by default
        loggedInUser = userService.addActionToFollowedActionsList(theAction, loggedInUser.getId());

        // show success message
        getPage().success(new StringResourceModel("created.message", this, null).getString());
        // show feedback panel
        target.add(feedbackPanel);
    }

    /**
     *
     */
    private void updateActionFromForm(AjaxRequestTarget target) {
        theAction.setShortText(shortTextTextArea.getModelObject());
        theAction.setDescription(descriptionTextArea.getModelObject());

        // Action image update
        final FileInfo profilePicture = saveUploadedPicture(target);
        if (profilePicture != null) {
            theAction.setActionImage(profilePicture);
        }
        actionImageFile.setImageResource(ActionPictureResource.get(PictureType.PROFILE, theAction));
        target.add(actionImageFile);

        // if Action type text exists that means ActionType can be Others..
        if (actionTypeTextArea.getDefaultModelObject() != null) {
            theAction.setActionTypeText(actionTypeTextArea.getModelObject());
            // this needs to be set manually if user does not set others option and only writes something in text box..
            if (!actionTypes.contains(IdeaType.Others)) {
                actionTypes.add(IdeaType.Others);
            }
        } else {
            theAction.setActionTypeText(null);
        }
        theAction.setActionType(actionTypes);

        // set all other textAreas..

        theAction.setKeywords((List<Tag>) keywords.getModelObject());
        theAction.setSkills((List<Tag>) skills.getModelObject());
        theAction.setPostedBy(loggedInUser);
        theAction.setAttachments(actionSupportingDocFileUploadInfo.getFileInfos(actionSupportingDocUploadHiddenField.getModelObject()));

        // once user creates an action, he/she will become follower of the parent challenge
        // userService.addChallengeToFollowedChallegesList(theCampaign, loggedInUser.getId());

        // set Action Team tools
        if (!this.adminActionTeamToolPanel.getTeamToolsMap().isEmpty()) {
            theAction.setActionTeamTools(adminActionTeamToolPanel.getTeamToolsMap());
        }
    }

    /**
     * @param target
     * @return
     */
    private FileInfo saveUploadedPicture(AjaxRequestTarget target) {
        FileUpload image = actionImageUploadField.getFileUpload();
        if (image != null) {
            if (isUploadedFileAnImage(image)) {
                final File uploadFolder = actionImageUploadHelper.createUploadFolderWithDir(UUID.randomUUID().toString(), null);
                try {
                    ActionPictureResource.createProfilePictureFromUpload(image, uploadFolder, theAction, PictureType.PROFILE);
                } catch (IOException e) {
                    logger.error(e);
                }

                try {
                    ActionPictureResource.createProfilePictureFromUpload(image, uploadFolder, theAction,
                            PictureType.THUMBNAIL);
                } catch (IOException e) {
                    logger.error(e);
                }

                try {
                    return ActionPictureResource.createProfilePictureFromUpload(image, uploadFolder, theAction,
                            PictureType.BIG);
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
        target.add(actionForm);
        target.add(shortTextTextArea);
        target.add(shortTextValidationBorder);
        target.add(descriptionValidationBorder);
        target.add(descriptionTextArea);
        target.add(keywords);
        target.add(skills);
        // also reload feedback panel
        target.add(feedbackPanel);

    }
}
