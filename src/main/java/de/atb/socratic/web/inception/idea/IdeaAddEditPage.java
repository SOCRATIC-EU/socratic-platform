package de.atb.socratic.web.inception.idea;

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
import javax.inject.Inject;

import com.vaynberg.wicket.select2.Select2MultiChoice;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.authorization.strategies.annotations.AuthorizeInstantiation;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.CampaignType;
import de.atb.socratic.model.ChallengeActivity;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.IdeaType;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.validation.ProfilePictureInputValidator;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.inception.InnovationObjectiveService;
import de.atb.socratic.service.inception.ScopeService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.other.TagService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.components.FileUploadBehavior;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.components.resource.IdeaPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.inception.CampaignAddEditPage;
import de.atb.socratic.web.inception.idea.details.IdeaDetailsPage;
import de.atb.socratic.web.provider.TagProvider;
import de.atb.socratic.web.qualifier.FileUploadCache;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.upload.FileUploadHelper;
import de.atb.socratic.web.upload.FileUploadHelper.UploadType;
import de.atb.socratic.web.upload.FileUploadInfo;
import org.apache.tika.mime.MediaType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.StringValue;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@AuthorizeInstantiation({UserRole.MANAGER, UserRole.USER, UserRole.SUPER_ADMIN, UserRole.ADMIN})
public class IdeaAddEditPage extends BasePage {

    /**
     *
     */
    private static final long serialVersionUID = -6414649996746082119L;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // inject the EJB for managing Ideas
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    IdeaService ideaService;

    @EJB
    ActivityService activityService;

    @EJB
    ScopeService scopeService;

    @EJB
    UserService userService;

    @EJB
    InnovationObjectiveService innovationObjectiveService;

    @EJB
    TagService tagService;

    @Inject
    ParticipateNotificationService participateNotifier;

    private final Form<Void> ideaForm;

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

    // Upload Supporting Documents
    private boolean ideaSupportingDocUploadFileUploadVisible;
    private final WebMarkupContainer ideaSupportingDocUploadFileTableBody;
    private final HiddenField<String> ideaSupportingDocUploadHiddenField;
    private final FileUploadField ideaSupportingDocUploadField;
    @Inject
    @FileUploadCache
    FileUploadInfo ideaSupportingDocFileUploadInfo;

    // IdeaType
    private CheckBox Product, Service, Policy, Framework, Training, Others;
    protected TextArea<String> ideaTypeTextArea;
    protected InputBorder<String> ideaTypeBorder;
    private ArrayList<IdeaType> ideaTypes = new ArrayList<>();
    private final CheckBoxMultipleChoice<IdeaType> listOfIdeaType;

    // Idea Image
    private final FileUploadField ideaImageUploadField;
    private NonCachingImage ideaImageFile;
    private final FileUploadHelper ideaImageUploadHelper = new FileUploadHelper(UploadType.CHALLENGE);
    private final AjaxLink<Void> clearIdeaImageLink;

    private static final JavaScriptResourceReference UPLOADED_IMAGE_PREVIEW_JS = new JavaScriptResourceReference(CampaignAddEditPage.class, "uploadImagePreview.js");

    // The idea to edit/save
    private Idea theIdea;
    private Campaign theCampaign;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    @Inject
    Logger logger;

    /**
     * @param parameters
     */
    public IdeaAddEditPage(final PageParameters parameters) {
        super(parameters);

        // add js and css for file upload
        add(new FileUploadBehavior());

        loadCampaign(parameters.get("campaignId"));

        // extract id parameter and set page title, header and Idea
        // depending on whether we are editing an existing Idea or creating
        // a new one
        loadIdea(parameters.get("id"));


        // add form to create new Idea
        add(ideaForm = newIdeaForm());

        // Add Basic fields (required)
        // add text field for name inside a border component that performs bean
        // validation
        ideaForm.add(shortTextValidationBorder = newTextField("shortTextValidationBorder",
                shortTextTextArea = newTextArea("shortText")));
        ideaForm.add(elevatorPitchValidationBorder = newTextField("elevatorPitchValidationBorder",
                elevatorPitchTextArea = newTextArea("elevatorPitch")));
        ideaForm.add(descriptionValidationBorder = newTextField("descriptionValidationBorder",
                descriptionTextArea = newTextArea("description")));
        ideaForm.add(beneficiariesValidationBorder = newTextField("beneficiariesValidationBorder",
                beneficiariesTextArea = newTextArea("beneficiaries")));
        // add text input field for tags
        keywords = newKeywordsSelect2MultiChoice();
        ideaForm.add(keywordsValidationBorder = new InputBorder<>("keywordsValidationBorder", keywords));

        // add text input field for skills
        ideaForm.add(skillsValidationBorder = new InputBorder<>("skillsValidationBorder", skills = newSkillsSelect2MultiChoice()));


        /****** Add Optional fields *****/

        // Add Idea Image
        ideaImageUploadField = new FileUploadField("profilePicture");
        ideaForm.add(new InputBorder<>("profilePictureBorder", ideaImageUploadField, new Model<String>()));
        ideaForm.add(new ProfilePictureInputValidator(ideaImageUploadField));
        ideaForm.add(newProfilePicturePreview());
        ideaForm.add(clearIdeaImageLink = newProfileClearButton());

        ideaForm.add(valueForBeneficiariesValidationBorder = newTextField("valueForBeneficiariesValidationBorder",
                valueForBeneficiariesTextArea = newTextArea("valueForBeneficiaries")));
        ideaForm.add(impactStakeholdersValidationBorder = newTextField("impactStakeholdersValidationBorder",
                impactStakeholdersTextArea = newTextArea("impactStakeholders")));

        ideaForm.add(resourcesForIdeaImplementationValidationBorder = newTextField(
                "resourcesForIdeaImplementationValidationBorder",
                resourcesForIdeaImplementationTextArea = newTextArea("resourcesForIdeaImplementation")));
        ideaForm.add(implementationPlanValidationBorder = newTextField("implementationPlanValidationBorder",
                implementationPlanTextArea = newTextArea("implementationPlan")));
        ideaForm.add(locationValidationBorder = newTextField("locationValidationBorder",
                locationTextArea = newTextArea("location")));
        ideaForm.add(reasonForBringingIdeaForwardValidationBorder = newTextField(
                "reasonForBringingIdeaForwardValidationBorder",
                reasonForBringingIdeaForwardTextArea = newTextArea("reasonForBringingIdeaForward")));
        ideaForm.add(relatedInnovationsValidationBorder = newTextField("relatedInnovationsValidationBorder",
                relatedInnovationsTextArea = newTextArea("relatedInnovations")));

        // Add Supporting Documents (optional)
        ideaForm.add(new AjaxLink<Void>("showAttachmentsForm") {
            private static final long serialVersionUID = 2414445280074210649L;

            // add a link to display file upload form
            @Override
            public void onClick(AjaxRequestTarget target) {
                toggleAttachmentsForm(target);
                ideaSupportingDocUploadFileUploadVisible = !ideaSupportingDocUploadFileUploadVisible;
            }
        });

        // for file uploads
        ideaSupportingDocUploadFileTableBody = new WebMarkupContainer("fileTableBody");
        ideaForm.add(ideaSupportingDocUploadFileTableBody.setOutputMarkupId(true));
        ideaSupportingDocUploadHiddenField = new HiddenField<>("uploadCacheId", new PropertyModel<String>(theIdea,
                "attachmentsCacheId"));
        ideaForm.add(ideaSupportingDocUploadHiddenField.setOutputMarkupId(true));
        ideaForm.setMaxSize(Bytes.megabytes(10));
        ideaForm.setMultiPart(true);
        ideaSupportingDocUploadField = new FileUploadField("fileupload");
        ideaForm.add(ideaSupportingDocUploadField.setOutputMarkupId(true));
        // fill the fileUploadInfo cache with any existing attachments
        updateAttachmentsCache();

        // Add Idea Type
        listOfIdeaType =
                new CheckBoxMultipleChoice<>("ideaTypes", Model.<ArrayList<IdeaType>>of(ideaTypes), IdeaType.getAll());
        listOfIdeaType.setRequired(true);
        ideaForm.add(new InputBorder<>("ideaTypesValidationBorder", listOfIdeaType));
        // Add text area for Other Idea Type
        ideaForm.add(ideaTypeBorder = newIdeaTypeTextField("ideaTypeBorder",
                ideaTypeTextArea = newTextArea("ideaTypeText")));

        // Add all help texts for all the fields..
        shortTextValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaTitleHelpText",
                new StringResourceModel("basicFields.idea.title.desc.label", this, null),
                TooltipConfig.Placement.right));
        elevatorPitchValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaElevatorPitchHelpText",
                new StringResourceModel("basicFields.idea.elevatorpitch.desc.label", this, null),
                TooltipConfig.Placement.right));
        ideaTypeBorder.add(addToolTipWebMarkupContainer(
                "ideaTypeHelpText",
                new StringResourceModel("basicFields.idea.type.desc.label", this, null),
                TooltipConfig.Placement.right));
        descriptionValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaDescriptionHelpText",
                new StringResourceModel("basicFields.idea.description.desc.label", this, null),
                TooltipConfig.Placement.right));
        beneficiariesValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaBeneficiariesHelpText",
                new StringResourceModel("basicFields.idea.beneficiaries.desc.label", this, null),
                TooltipConfig.Placement.right));
        keywordsValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaKeywordsText",
                new StringResourceModel("basicFields.idea.keywords.desc.label", this, null),
                TooltipConfig.Placement.right));
        ideaForm.add(addToolTipWebMarkupContainer(
                "ideaImageHelpText",
                new StringResourceModel("idea.image.desc.label", this, null),
                TooltipConfig.Placement.right));
        valueForBeneficiariesValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaValueForTheBeneficiariesHelpText",
                new StringResourceModel("optionalFields.idea.valueforbeneficiaries.desc.label", this, null),
                TooltipConfig.Placement.right));
        impactStakeholdersValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaImpactHelpText",
                new StringResourceModel("optionalFields.idea.impactonstakeholders.desc.label", this, null),
                TooltipConfig.Placement.right));
        skillsValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaSkillsHelpText",
                new StringResourceModel("optionalFields.idea.skillsforideaimplementation.desc.label", this, null),
                TooltipConfig.Placement.right));
        resourcesForIdeaImplementationValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaResourcesHelpText",
                new StringResourceModel("optionalFields.idea.resourcesforideaimplementation.desc.label", this, null),
                TooltipConfig.Placement.right));
        implementationPlanValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaImplementationPlanHelpText",
                new StringResourceModel("optionalFields.idea.outlineforideaimplementation.desc.label", this, null),
                TooltipConfig.Placement.right));
        locationValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaLocationHelpText",
                new StringResourceModel("optionalFields.idea.location.desc.label", this, null),
                TooltipConfig.Placement.right));
        reasonForBringingIdeaForwardValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaPersonHelpText",
                new StringResourceModel("optionalFields.idea.person.desc.label", this, null),
                TooltipConfig.Placement.right));
        relatedInnovationsValidationBorder.add(addToolTipWebMarkupContainer(
                "ideaRelatedInnovationsHelpText",
                new StringResourceModel("optionalFields.idea.relatedInnovations.desc.label", this, null),
                TooltipConfig.Placement.right));

        // add a button to create new campaign
        ideaForm.add(newSubmitLink("submitTop"));
        ideaForm.add(newSubmitLink("submitBottom"));

        // add cancel button to return to campaign list page
        ideaForm.add(newCancelLink());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forReference(UPLOADED_IMAGE_PREVIEW_JS));
        activateCurrentTab(response, "challengesTab");
    }

    /**
     * @param idParam
     */
    protected void loadIdea(final StringValue idParam) {
        if (idParam.isEmpty()) {
            if (theCampaign.getCampaignType() == CampaignType.FREE_FORM) {
                setPageTitle(new StringResourceModel("page.create.entre.title", this, null));
                add(new Label("header", new StringResourceModel("form.create.entre.header", this, null)));
            } else {
                setPageTitle(new StringResourceModel("page.create.title", this, null));
                add(new Label("header", new StringResourceModel("form.create.header", this, null)));
            }
            theIdea = new Idea();
        } else {
            if (theCampaign.getCampaignType() == CampaignType.FREE_FORM) {
                setPageTitle(new StringResourceModel("page.edit.entre.title", this, null));
                add(new Label("header", new StringResourceModel("form.edit.entre.header", this, null)));
            } else {
                setPageTitle(new StringResourceModel("page.edit.title", this, null));
                add(new Label("header", new StringResourceModel("form.edit.header", this, null)));
            }
            // set the campaign we got from previous page
            try {
                theIdea = ideaService.getById(idParam.toOptionalLong());

                // check if idea leader is the loggedIn user, if not then redirect this page to details page
                if (!theIdea.isEditableBy(loggedInUser)) {
                    setResponsePage(IdeaDetailsPage.class,
                            new PageParameters().set("id", theCampaign.getId()).set("ideaId", theIdea.getId()));
                }

                //set IdeaTypes
                ideaTypes = new ArrayList<IdeaType>(theIdea.getIdeaType());
            } catch (Exception e) {
                throw new RestartResponseException(ErrorPage.class);
            }
        }
    }

    /**
     * @param idParam
     */
    protected void loadCampaign(final StringValue idParam) {
        if (idParam.isEmpty()) {
            theCampaign = new Campaign();
        } else {
            // set the campaign we got from previous page
            try {
                theCampaign = campaignService.getById(idParam.toOptionalLong());
            } catch (Exception e) {
                throw new RestartResponseException(ErrorPage.class);
            }
        }
    }

    /**
     * @return
     */
    private Form<Void> newIdeaForm() {
        Form<Void> form = new InputValidationForm<Void>("form") {
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

    private BookmarkablePageLink<IdeaAddEditPage> newLinkToIdeas(String markupid) {
        return new BookmarkablePageLink<>(markupid, IdeaAddEditPage.class, new PageParameters());
    }

    /**
     * @return
     */
    private InputBorder<String> newIdeaTypeTextField(String id, TextArea<String> textArea) {
        return new InputBorder<String>(id, textArea, new Model<String>());
    }

    /**
     * @return
     */
    private InputBorder<String> newTextField(String id, TextArea<String> textArea) {
        return new OnEventInputBeanValidationBorder<String>(id, textArea, new Model<String>(), HtmlEvent.ONCHANGE);
    }

    private TextArea<String> newTextArea(String id) {
        return new TextArea<String>(id, new PropertyModel<String>(theIdea, id));
    }

    /**
     * @return
     */
    private Select2MultiChoice<Tag> newKeywordsSelect2MultiChoice() {
        Select2MultiChoice<Tag> select2MultiChoice = new Select2MultiChoice<>(
                "keywords",
                new PropertyModel<Collection<Tag>>(theIdea, "keywords"),
                new TagProvider(tagService.getAll()));
        select2MultiChoice.getSettings().setMinimumInputLength(1);
        select2MultiChoice.getSettings().setCreateSearchChoice(
                "function(term) { if (term.length > 1) { return { id: term, text: term }; } }");
        select2MultiChoice.setRequired(true);
        return select2MultiChoice;
    }

    /**
     * @return
     */
    private Select2MultiChoice<Tag> newSkillsSelect2MultiChoice() {
        Select2MultiChoice<Tag> select2MultiChoice = new Select2MultiChoice<>(
                "skills",
                new PropertyModel<Collection<Tag>>(theIdea, "skills"),
                new TagProvider(tagService.getAll()));
        select2MultiChoice.getSettings().setMinimumInputLength(1);
        select2MultiChoice.getSettings().setCreateSearchChoice(
                "function(term) { if (term.length > 1) { return { id: term, text: term }; } }");
        return select2MultiChoice;
    }

    private NonCachingImage newProfilePicturePreview() {
        ideaImageFile = new NonCachingImage("profilePicturePreview", IdeaPictureResource.get(PictureType.PROFILE, theIdea));
        ideaImageFile.setOutputMarkupId(true);
        return ideaImageFile;
    }

    private AjaxLink<Void> newProfileClearButton() {
        return new AjaxLink<Void>("profilePictureClear") {
            private static final long serialVersionUID = -1439023360594387449L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(theIdea != null && theIdea.getIdeaImage() != null);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                theIdea.setIdeaImage(null);
                ideaImageFile.setImageResource(IdeaPictureResource.get(PictureType.PROFILE, theIdea));
                target.add(ideaImageFile);
                target.add(clearIdeaImageLink);
            }
        };
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
        Set<FileInfo> fileInfos = theIdea.getAttachments();
        if (!fileInfos.isEmpty()) {
            ideaSupportingDocFileUploadInfo.addFileInfo(ideaSupportingDocUploadHiddenField.getModelObject(),
                    fileInfos.toArray(new FileInfo[fileInfos.size()]));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.markup.html.WebPage#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender();
        // detach entity to avoid automatic update of changes in form.
        ideaService.detach(theIdea);
    }

    /**
     * @return
     */
    private String getFileUploadScript() {
        return String.format(
                JSTemplates.LOAD_FILE_UPLOAD,
                JSTemplates.getContextPath(),
                ideaSupportingDocUploadField.getMarkupId(),
                ideaSupportingDocUploadHiddenField.getMarkupId(),
                ideaSupportingDocUploadFileTableBody.getMarkupId());
    }

    /**
     * @return
     */
    private String getToggleFileUploadDivScript() {
        if (ideaSupportingDocUploadFileUploadVisible) {
            return String.format(JSTemplates.SHOW_COLLAPSE, ".attachments");
        } else {
            return String.format(JSTemplates.CLOSE_COLLAPSE, ".attachments");
        }
    }

    private AjaxSubmitLink newSubmitLink(String id) {
        return new AjaxSubmitLink(id, ideaForm) {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (theIdea.getId() == null) {
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
     * @return
     */
    private Link<IdeasPage> newCancelLink() {
        return new Link<IdeasPage>("cancel") {
            private static final long serialVersionUID = -310533532532643267L;

            @Override
            public void onClick() {
                setResponsePage(
                        IdeasPage.class,
                        new PageParameters().set("id", theCampaign.getId()));
            }
        };
    }

    /**
     *
     */
    protected void save(AjaxRequestTarget target) {
        submitIdea(target);
        // success message has to be associated to session so that it is shown
        // in the global feedback panel
        Session.get().success(new StringResourceModel("saved.message", this, Model.of(theIdea)).getString());
        // redirect to IdeaPage
        setResponsePage(
                IdeasPage.class,
                new PageParameters().set("id", theCampaign.getId())
                        .set(MESSAGE_PARAM, new StringResourceModel("saved.message", this, Model.of(theIdea)).getString())
                        .set(LEVEL_PARAM, FeedbackMessage.SUCCESS));
    }

    private void submitIdea(AjaxRequestTarget target) {
        if (theIdea.getId() == null) {
            createIdea(target);

            // once Idea is created, create an activity related to it.
            activityService.create(ChallengeActivity.ofIdeaAdd(theIdea));
        } else {
            updateIdea(target);
        }
        keywords.setProvider(new TagProvider(tagService.getAll()));
        target.add(keywords);

        skills.setProvider(new TagProvider(tagService.getAll()));
        target.add(skills);
    }

    /**
     * @param target
     */
    private void updateIdea(AjaxRequestTarget target) {

        // update idea
        updateIdeaFromForm(target);

        // set last modified date
        theIdea.setLastModified(new Date());

        theIdea = ideaService.update(theIdea);

        // notify about updated ideas
        ideaService.notifyIdeaFollowersAboutIdeaUpdates(theIdea);

        // reset file uploads
        clearFileUploadCache();
        // set file upload field to hidden
        ideaSupportingDocUploadFileUploadVisible = false;
        // show feedback panel
        target.add(feedbackPanel);
        // replace form with IdeaPanel
        onAfterUpdate(target, theIdea, this);

    }

    /**
     * Override this to do ajax updates after idea creation has been cancelled.
     *
     * @param target
     */
    protected void onAfterCreateCancel(AjaxRequestTarget target) {
        // empty by default. implement this!
    }

    /**
     * @param target
     */
    private void createIdea(AjaxRequestTarget target) {
        // and add idea to the campaign
        updateIdeaFromForm(target);
        theIdea = campaignService.addIdea(theCampaign, theIdea);
        // reset file uploads
        clearFileUploadCache();
        // show success message
        getPage().success(new StringResourceModel("created.message", this, null).getString());
        // show feedback panel
        target.add(feedbackPanel);
        // prepend the new idea to the list of ideas
        onAfterCreate(target, theIdea);

        // when being created, add idea to followedIdeas list by default
        loggedInUser = userService.addIdeaToFollowedIdeasList(theIdea, loggedInUser.getId());

        // once user creates an idea, he/she will become follower of the parent challenge
        if (!userService.isUserFollowsGivenChallenge(theCampaign, loggedInUser.getId())) {
            // Add challenge to list of challenges followed for loggedInUser
            loggedInUser = userService.addChallengeToFollowedChallegesList(theCampaign, loggedInUser.getId());

            // send him/her notification that they now becomes follower of this challenge and send CO that new idea is posted to
            // his challenge. 
            participateNotifier.addParticipationNotification(theIdea, loggedInUser, NotificationType.IDEA_CREATED);
        }
        // clear form
        // resetIdeaForm(target);
    }

    /**
     * Override this to do ajax updates after idea has been created.
     *
     * @param target
     * @param newIdea
     */
    protected void onAfterCreate(AjaxRequestTarget target, Idea newIdea) {
        // empty by default. implement this!
    }

    /**
     * Clears the file upload cache and deletes all uploaded files.
     */
    private void clearFileUploadCache() {
        ideaSupportingDocFileUploadInfo.cleanup(ideaSupportingDocUploadHiddenField.getModelObject());
    }

    /**
     * Override this to do ajax updates after idea has been update.
     *
     * @param target
     * @param idea
     * @param component
     */
    protected void onAfterUpdate(AjaxRequestTarget target, Idea idea, Component component) {
        // empty by default. implement this!
    }

    /**
     *
     */
    private void updateIdeaFromForm(AjaxRequestTarget target) {
        theIdea.setShortText(shortTextTextArea.getModelObject());
        theIdea.setDescription(descriptionTextArea.getModelObject());

        // Idea image update
        final FileInfo profilePicture = saveUploadedPicture(target);
        if (profilePicture != null) {
            theIdea.setIdeaImage(profilePicture);
        }
        ideaImageFile.setImageResource(IdeaPictureResource.get(PictureType.PROFILE, theIdea));
        target.add(ideaImageFile);

        // if idea type text exists that means idea Type can be Others..
        if (ideaTypeTextArea.getDefaultModelObject() != null) {
            theIdea.setIdeaTypeText(ideaTypeTextArea.getModelObject());
            if (!ideaTypes.contains(IdeaType.Others)) {
                ideaTypes.add(IdeaType.Others);
            }
        } else { // idea type text needs to be reset once text box is empty
            theIdea.setIdeaTypeText(null);
        }

        theIdea.setIdeaType(ideaTypes);

        // set all other textAreas..
        // theIdea.setCollaborators((List<User>) collaborators.getModelObject());
        theIdea.setKeywords((List<Tag>) keywords.getModelObject());
        theIdea.setSkills((List<Tag>) skills.getModelObject());
        theIdea.setAttachments(ideaSupportingDocFileUploadInfo.getFileInfos(ideaSupportingDocUploadHiddenField.getModelObject()));
        theIdea.setPostedBy(loggedInUser);
    }

    /**
     * @param target
     * @return
     */
    private FileInfo saveUploadedPicture(AjaxRequestTarget target) {
        FileUpload image = ideaImageUploadField.getFileUpload();
        if (image != null) {
            if (isUploadedFileAnImage(image)) {
                final File uploadFolder = ideaImageUploadHelper.createUploadFolderWithDir(UUID.randomUUID().toString(), null);
                try {
                    IdeaPictureResource.createProfilePictureFromUpload(image, uploadFolder, theIdea, PictureType.PROFILE);
                } catch (IOException e) {
                    logger.error(e);
                }

                try {
                    IdeaPictureResource.createProfilePictureFromUpload(image, uploadFolder, theIdea, PictureType.THUMBNAIL);
                } catch (IOException e) {
                    logger.error(e);
                }

                try {
                    return IdeaPictureResource.createProfilePictureFromUpload(image, uploadFolder, theIdea, PictureType.BIG);
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
        target.add(ideaForm);
        target.add(shortTextTextArea);
        target.add(shortTextValidationBorder);
        target.add(descriptionValidationBorder);
        target.add(descriptionTextArea);
        target.add(keywords);
        target.add(skills);
        // target.add(collaborators);
        target.add(ideaSupportingDocUploadField);
        target.add(ideaSupportingDocUploadHiddenField);
        target.add(ideaSupportingDocUploadFileTableBody);

        // also reload feedback panel
        target.add(feedbackPanel);
        // do not hide file upload panel if it was shown before
        if (ideaSupportingDocUploadFileUploadVisible) {
            toggleAttachmentsForm(target);
        }
    }

}
