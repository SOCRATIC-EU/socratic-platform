package de.atb.socratic.web.inception;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.EJB;
import javax.inject.Inject;

import com.vaynberg.wicket.select2.Select2MultiChoice;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.form.DateTextField;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.form.DateTextFieldConfig;
import de.atb.socratic.authorization.strategies.annotations.AuthorizeInstantiation;
import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Company;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.InnovationObjective;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.UNGoalType;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.scope.ScopeType;
import de.atb.socratic.model.scope.StaffScope;
import de.atb.socratic.model.validation.DateInputValidator;
import de.atb.socratic.model.validation.EmailMultiInputValidator;
import de.atb.socratic.model.validation.Inception;
import de.atb.socratic.model.validation.ProfilePictureInputValidator;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.InnovationObjectiveService;
import de.atb.socratic.service.inception.ScopeService;
import de.atb.socratic.service.notification.InvitationMailService;
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
import de.atb.socratic.web.components.resource.ChallengePictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.provider.TagProvider;
import de.atb.socratic.web.provider.UrlProvider;
import de.atb.socratic.web.qualifier.AllUsers;
import de.atb.socratic.web.qualifier.FileUploadCache;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.security.register.RegisterPage;
import de.atb.socratic.web.upload.FileUploadHelper;
import de.atb.socratic.web.upload.FileUploadHelper.UploadType;
import de.atb.socratic.web.upload.FileUploadInfo;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
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
import org.joda.time.DateTime;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@AuthorizeInstantiation({UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN})
public class CampaignAddEditPage extends BasePage {

    /**
     *
     */
    private static final long serialVersionUID = -6414649996746082119L;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    @Inject
    @AllUsers
    List<User> allUsers;

    @Inject
    Logger logger;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    @EJB
    ScopeService scopeService;

    @EJB
    UserService userService;

    @EJB
    InnovationObjectiveService innovationObjectiveService;

    @EJB
    TagService tagService;

    @Inject
    InvitationMailService invitationMailService;

    @Inject
    UrlProvider urlProvider;

    private final Form<Void> campaignForm;

    protected final InputBorder<String> nameValidationBorder;
    protected final InputBorder<String> elevatorPitchValidationBorder;
    protected final InputBorder<String> socialChallengeValidationBorder;
    protected final InputBorder<String> beneficiariesValidationBorder;
    protected final InputBorder<String> potentialImpactValidationBorder;
    protected final InputBorder<String> levelOfSupportValidationBorder;
    protected final InputBorder<String> ideasProposedValidationBorder;
    protected final InputBorder<Collection<Tag>> keywordsValidationBorder;

    protected final InputBorder<String> referencesTitleValidationBorder;
    protected final InputBorder<String> referencesDescriptionValidationBorder;
    protected final TextField<String> referencesLinkTextField;

    protected final TextArea<String> nameTextArea;
    protected final TextArea<String> elevatorPitchTextArea;
    protected final TextArea<String> socialChallengeTextArea;
    protected final TextArea<String> beneficiariesTextArea;
    protected final TextArea<String> potentialImpactTextArea;
    protected final TextArea<String> levelOfSupportTextArea;
    protected final TextArea<String> ideasProposedTextArea;
    protected final TextArea<String> referencesTitleTextArea;
    protected final TextArea<String> referencesDescriptionTextArea;

    private final TextField<String> emailTextField;
    private final TextArea<String> messageToInvitedContactTextArea;
    private String invitationEmails;

    private DateTextField ideationStartDateField;
    private DateTextField ideationEndDateField;
    private DateTextField selectionStartDateField;
    private DateTextField selectionEndDateField;
    private DateTextField challengeOpenForDiscussionStartDateField;
    private DateTextField challengeOpenForDiscussionEndDateField;

    private Label challengeOpenForDiscussionStartLabel;
    private Label challengeOpenForDiscussionStopLabel;

    private final FileUploadField fileUploadField;
    private NonCachingImage challengeProfilePicture;
    private final FileUploadHelper uploadHelper = new FileUploadHelper(UploadType.CHALLENGE);
    private final AjaxLink<Void> clearPictureLink;

    private final Select2MultiChoice<Tag> keywords;

    // The campaign to edit/save
    private Campaign theCampaign;

    private Company company;
    private RadioGroup<Boolean> yesNoGroup;
    private ArrayList<String> uNGoalTypes = new ArrayList<>();
    private final CheckBoxMultipleChoice<String> listOfUNGoalTypes;
    private RadioGroup<Boolean> publicPrivateGroup;
    private static final JavaScriptResourceReference UPLOADED_IMAGE_PREVIEW_JS = new JavaScriptResourceReference(CampaignAddEditPage.class, "uploadImagePreview.js");

    // Upload Supporting Documents
    private boolean campaignSupportingDocUploadFileUploadVisible;
    private final WebMarkupContainer campaignSupportingDocUploadFileTableBody;
    private final HiddenField<String> campaignSupportingDocUploadHiddenField;
    private final FileUploadField campaignSupportingDocUploadField;

    @Inject
    @FileUploadCache
    FileUploadInfo campaignSupportingDocFileUploadInfo;

    /**
     * @param parameters
     */
    public CampaignAddEditPage(final PageParameters parameters) {
        super(parameters);

        // add js and css for file upload
        add(new FileUploadBehavior());

        // extract id parameter and set page title, header and campaign
        // depending on whether we are editing an existing campaign or creating
        // a new one
        loadCampaign(parameters.get("id"));
        if (theCampaign.getCompany() != null) {
            company = theCampaign.getCompany();
        } else if (loggedInUser != null && loggedInUser.getCurrentCompany() != null) {
            company = loggedInUser.getCurrentCompany();
        }
        // add form to create new campaign
        add(campaignForm = newCampaignForm());

        // add text field for name inside a border component that performs bean
        // validation
        campaignForm.add(nameValidationBorder = newInputBorder("nameValidationBorder",
                nameTextArea = newTextAreaForChallenge("name")));
        campaignForm.add(elevatorPitchValidationBorder = newInputBorder("elevatorPitchValidationBorder",
                elevatorPitchTextArea = newTextAreaForChallenge("elevatorPitch")));
        // Challenge image upload
        fileUploadField = new FileUploadField("profilePicture");
        campaignForm.add(new InputBorder<>("profilePictureBorder", fileUploadField, new StringResourceModel(
                "profile.picture.label", this, null)));
        campaignForm.add(new ProfilePictureInputValidator(fileUploadField));

        campaignForm.add(new Label("profilePicturePreviewLabel", new StringResourceModel("profile.picture.preview.label", this,
                null)));
        campaignForm.add(newProfilePicturePreview());
        campaignForm.add(clearPictureLink = newProfileClearButton());

        campaignForm.add(socialChallengeValidationBorder = newInputBorder("socialChallengeValidationBorder",
                socialChallengeTextArea = newTextAreaForChallenge("socialChallenge")));

        campaignForm.add(beneficiariesValidationBorder = newInputBorder("beneficiariesValidationBorder",
                beneficiariesTextArea = newTextAreaForChallenge("beneficiaries")));

        campaignForm.add(potentialImpactValidationBorder = newInputBorder("potentialImpactValidationBorder",
                potentialImpactTextArea = newTextAreaForChallenge("potentialImpact")));

        campaignForm.add(levelOfSupportValidationBorder = newInputBorder("levelOfSupportValidationBorder",
                levelOfSupportTextArea = newTextAreaForChallenge("levelOfSupport")));

        campaignForm.add(ideasProposedValidationBorder = newInputBorder("ideasProposedValidationBorder",
                ideasProposedTextArea = newTextAreaForChallenge("ideasProposed")));

        campaignForm.add(referencesTitleValidationBorder = newInputBorder("referencesTitleValidationBorder",
                referencesTitleTextArea = newTextAreaForChallenge("referencesTitle")));

        campaignForm.add(referencesDescriptionValidationBorder = newInputBorder("referencesDescriptionValidationBorder",
                referencesDescriptionTextArea = newTextAreaForChallenge("referencesDescription")));

        campaignForm.add(referencesLinkTextField = newReferencesLinkTextField());

        // UNGoals
        listOfUNGoalTypes =
                new CheckBoxMultipleChoice<String>(
                        "unGoalsType", new Model(uNGoalTypes), UNGoalType.getAllAsString());
        listOfUNGoalTypes.setRequired(true);
        campaignForm.add(new InputBorder<>("uNGoalTypesValidationBorder", listOfUNGoalTypes));

        addRadioGroupRelatedToChallengeOpenForDiscussion();
        addRadioGroupRelatedToOpenessOfTheChallenge();
        addIdeationDates();
        addSelectionDates();

        // add text input field for keywords
        keywords = newKeywordsSelect2MultiChoice();
        campaignForm.add(keywordsValidationBorder = new InputBorder<>("keywordsValidationBorder", keywords));

        campaignForm.add(newLinkToChallenges("references.addreference.link"));

        emailTextField = newEmailTextField("emailTextField", new PropertyModel<String>(this, "invitationEmails"));
        campaignForm.add(newEmailTextFieldValidationBorder("emailTextFieldValidationBorder", emailTextField));
        campaignForm.add(new EmailMultiInputValidator(emailTextField));

        // messageToInvitedContact
        messageToInvitedContactTextArea = new TextArea<>("messageToInvitedContact", new Model<String>(null));
        campaignForm.add(messageToInvitedContactTextArea);

        // Add all help texts for all the fields..
        nameValidationBorder.add(addToolTipWebMarkupContainer(
                "challengeTitleHelpText",
                new StringResourceModel("challenge.title.desc.label", this, null),
                TooltipConfig.Placement.right));
        elevatorPitchValidationBorder.add(addToolTipWebMarkupContainer(
                "elevatorPitchHelpText",
                new StringResourceModel("challenge.elevatorPitch.desc.label", this, null),
                TooltipConfig.Placement.right));
        campaignForm.add(addToolTipWebMarkupContainer(
                "challengeImageHelpText",
                new StringResourceModel("challenge.image.desc.label", this, null),
                TooltipConfig.Placement.right));
        socialChallengeValidationBorder.add(addToolTipWebMarkupContainer(
                "challengeSocialChallengeTypeHelpText",
                new StringResourceModel("challenge.type.desc.label", this, null),
                TooltipConfig.Placement.right));
        beneficiariesValidationBorder.add(addToolTipWebMarkupContainer(
                "challengeBeneficiariesHelpText",
                new StringResourceModel("challenge.beneficiaries.desc.label", this, null),
                TooltipConfig.Placement.right));
        potentialImpactValidationBorder.add(addToolTipWebMarkupContainer(
                "challengePotentialImpactHelpText",
                new StringResourceModel("challenge.potentialImpact.desc.label", this, null),
                TooltipConfig.Placement.right));
        levelOfSupportValidationBorder.add(addToolTipWebMarkupContainer(
                "challengeLevelOfSupportHelpText",
                new StringResourceModel("challenge.support.desc.label", this, null),
                TooltipConfig.Placement.right));
        ideasProposedValidationBorder.add(addToolTipWebMarkupContainer(
                "challengeIdeasProposedHelpText",
                new StringResourceModel("challenge.ideasProposed.desc.label", this, null),
                TooltipConfig.Placement.right));
        keywordsValidationBorder.add(addToolTipWebMarkupContainer(
                "challengeKeywordsHelpText",
                new StringResourceModel("challenge.keywords.desc.label", this, null),
                TooltipConfig.Placement.right));

        // Add Supporting Documents (optional)
        campaignForm.add(new AjaxLink<Void>("showAttachmentsForm") {
            private static final long serialVersionUID = 2414445280074210649L;

            // add a link to display file upload form
            @Override
            public void onClick(AjaxRequestTarget target) {
                toggleAttachmentsForm(target);
                campaignSupportingDocUploadFileUploadVisible = !campaignSupportingDocUploadFileUploadVisible;
            }
        });

        // for file uploads
        campaignSupportingDocUploadFileTableBody = new WebMarkupContainer("fileTableBody");
        campaignForm.add(campaignSupportingDocUploadFileTableBody.setOutputMarkupId(true));
        campaignSupportingDocUploadHiddenField = new HiddenField<>("uploadCacheId", new PropertyModel<String>(theCampaign,
                "attachmentsCacheId"));
        campaignForm.add(campaignSupportingDocUploadHiddenField.setOutputMarkupId(true));
        campaignForm.setMaxSize(Bytes.megabytes(10));
        campaignForm.setMultiPart(true);
        campaignSupportingDocUploadField = new FileUploadField("fileupload");
        campaignForm.add(campaignSupportingDocUploadField.setOutputMarkupId(true));
        // fill the fileUploadInfo cache with any existing attachments
        updateAttachmentsCache();

        // add a button to create new campaign
        campaignForm.add(newSubmitLink("submitTop"));
        campaignForm.add(newSubmitLink("submitBottom"));

        // add cancel button to return to campaign list page
        campaignForm.add(newCancelLink());
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forReference(UPLOADED_IMAGE_PREVIEW_JS));
        activateCurrentTab(response, "challengesTab");
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
        Set<FileInfo> fileInfos = theCampaign.getAttachments();
        if (!fileInfos.isEmpty()) {
            campaignSupportingDocFileUploadInfo.addFileInfo(campaignSupportingDocUploadHiddenField.getModelObject(),
                    fileInfos.toArray(new FileInfo[fileInfos.size()]));
        }
    }

    /**
     * Clears the file upload cache and deletes all uploaded files.
     */
    private void clearFileUploadCache() {
        campaignSupportingDocFileUploadInfo.cleanup(campaignSupportingDocUploadHiddenField.getModelObject());
    }

    public void addIdeationDates() {
        // Ideation start and end dates
        ideationStartDateField = newIdeationDateTextField("ideationStartDate");
        campaignForm.add(newDateTextField("ideationStartDateValidationBorder",
                ideationStartDateField));
        campaignForm.add(new DateInputValidator(ideationStartDateField, challengeOpenForDiscussionEndDateField));
        campaignForm.add(newDateTextField("ideationEndDateValidationBorder",
                ideationEndDateField = newIdeationDateTextField("ideationEndDate")));
        campaignForm.add(new DateInputValidator(ideationEndDateField, ideationStartDateField));
        OnChangeAjaxBehavior onChangeAjaxBehaviorIdeationForStartDate = new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(ideationEndDateField);
            }
        };
        ideationStartDateField.add(onChangeAjaxBehaviorIdeationForStartDate);

        // check for ideation end date, if it is selected then arrange selection start date accordigly.
        OnChangeAjaxBehavior onChangeAjaxBehaviorIdeationForEndDate = new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(selectionStartDateField);
                target.add(selectionEndDateField);
            }
        };
        ideationEndDateField.add(onChangeAjaxBehaviorIdeationForEndDate);
    }

    private BookmarkablePageLink<CampaignAddEditPage> newLinkToChallenges(String markupid) {
        return new BookmarkablePageLink<>(markupid, CampaignAddEditPage.class, new PageParameters());
    }

    public void addSelectionDates() {
        // Selection start and end dates
        campaignForm.add(newDateTextField("selectionStartDateValidationBorder",
                selectionStartDateField = newSelectionDateTextField("selectionStartDate")));
        campaignForm.add(new DateInputValidator(selectionStartDateField, ideationEndDateField));

        campaignForm.add(newDateTextField("selectionEndDateValidationBorder",
                selectionEndDateField = newSelectionDateTextField("selectionEndDate")));
        campaignForm.add(new DateInputValidator(selectionEndDateField, selectionStartDateField));

        OnChangeAjaxBehavior onChangeAjaxBehaviorSlection = new OnChangeAjaxBehavior() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(selectionEndDateField);
            }
        };
        selectionStartDateField.add(onChangeAjaxBehaviorSlection);
    }

    public void addRadioGroupRelatedToChallengeOpenForDiscussion() {
        yesNoGroup = new RadioGroup<>("yesNoGroup", new PropertyModel<Boolean>(theCampaign, "openForDiscussion"));

        campaignForm.add(yesNoGroup);
        yesNoGroup.add(new Radio<>("yes", Model.of(Boolean.TRUE)));
        yesNoGroup.add(new Radio<>("no", Model.of(Boolean.FALSE)));

        // challengeOpenForDiscussion start and end dates
        final InputBorder<Date> inputBorderDefinitionStartDate = newDateTextField(
                "definitionStartDateValidationBorder",
                challengeOpenForDiscussionStartDateField = newChallengeOpenForDiscussionDateTextField("challengeOpenForDiscussionStartDate"));
        campaignForm
                .add(inputBorderDefinitionStartDate);
        //campaignForm.add(new DateInputValidator(challengeOpenForDiscussionStartDateField, null));

        final InputBorder<Date> inputBorderDefinitionEndDate = newDateTextField(
                "definitionEndDateValidationBorder",
                challengeOpenForDiscussionEndDateField = newChallengeOpenForDiscussionDateTextField("challengeOpenForDiscussionEndDate"));
        campaignForm
                .add(inputBorderDefinitionEndDate);
        campaignForm.add(new DateInputValidator(challengeOpenForDiscussionEndDateField, challengeOpenForDiscussionStartDateField));

        // when challenge is open for discussion and discussion is over, enable the start date field of ideation to follow the
        // discussion end date.
        OnChangeAjaxBehavior onChangeAjaxBehaviorIdeation = new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(ideationStartDateField);
                target.add(ideationEndDateField);
                target.add(selectionStartDateField);
                target.add(selectionEndDateField);
            }
        };
        challengeOpenForDiscussionEndDateField.add(onChangeAjaxBehaviorIdeation);

        inputBorderDefinitionStartDate.setOutputMarkupPlaceholderTag(true);
        inputBorderDefinitionEndDate.setOutputMarkupPlaceholderTag(true);
        challengeOpenForDiscussionStartDateField.setOutputMarkupPlaceholderTag(true);
        challengeOpenForDiscussionEndDateField.setOutputMarkupPlaceholderTag(true);

        campaignForm.add(challengeOpenForDiscussionStartLabel = new Label("challengeOpenForDiscussionStartLabel",
                new StringResourceModel("option.type.yes.startDate.input.label", this, null)) {
            private static final long serialVersionUID = 830442411576320359L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(theCampaign.getOpenForDiscussion());
            }
        });
        campaignForm.add(challengeOpenForDiscussionStopLabel = new Label("challengeOpenForDiscussionStopLabel",
                new StringResourceModel("option.type.yes.stopDate.input.label", this, null)) {
            private static final long serialVersionUID = 5426970149372698348L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(theCampaign.getOpenForDiscussion());
            }
        });
        challengeOpenForDiscussionStartLabel.setOutputMarkupPlaceholderTag(true);
        challengeOpenForDiscussionStopLabel.setOutputMarkupPlaceholderTag(true);

        // set all 4 components initially to not visible.
        // challengeOpenForDiscussionStartDateField.setVisible(false);
        // challengeOpenForDiscussionEndDateField.setVisible(false);
        // challengeOpenForDiscussionStartLabel.setVisible(false);
        // challengeOpenForDiscussionStopLabel.setVisible(false);
        OnChangeAjaxBehavior onChangeAjaxBehaviourChallengeOpenForDiscussion = new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(challengeOpenForDiscussionEndDateField);
            }
        };

        challengeOpenForDiscussionStartDateField.add(onChangeAjaxBehaviourChallengeOpenForDiscussion);
        yesNoGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            private static final long serialVersionUID = 3129987196932150976L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(inputBorderDefinitionStartDate);
                target.add(inputBorderDefinitionEndDate);

                challengeOpenForDiscussionStartDateField.setVisible(theCampaign.getOpenForDiscussion());
                challengeOpenForDiscussionEndDateField.setVisible(theCampaign.getOpenForDiscussion());
                challengeOpenForDiscussionStartLabel.setVisible(theCampaign.getOpenForDiscussion());
                challengeOpenForDiscussionStopLabel.setVisible(theCampaign.getOpenForDiscussion());
                challengeOpenForDiscussionStartDateField.setRequired(theCampaign.getOpenForDiscussion());
                challengeOpenForDiscussionEndDateField.setRequired(theCampaign.getOpenForDiscussion());

                challengeOpenForDiscussionStartDateField.clearInput();
                challengeOpenForDiscussionEndDateField.clearInput();
                ideationStartDateField.clearInput();
                ideationEndDateField.clearInput();
                selectionStartDateField.clearInput();
                selectionEndDateField.clearInput();
                challengeOpenForDiscussionStartDateField.updateModel();
                challengeOpenForDiscussionEndDateField.updateModel();
                ideationStartDateField.updateModel();
                ideationEndDateField.updateModel();
                selectionStartDateField.updateModel();
                selectionEndDateField.updateModel();

                target.add(challengeOpenForDiscussionStartDateField);
                target.add(challengeOpenForDiscussionEndDateField);
                target.add(challengeOpenForDiscussionStartLabel);
                target.add(challengeOpenForDiscussionStopLabel);
                target.add(ideationStartDateField);
                target.add(ideationEndDateField);
                target.add(selectionStartDateField);
                target.add(selectionEndDateField);
            }

        });
    }

    public void addRadioGroupRelatedToOpenessOfTheChallenge() {
        publicPrivateGroup = new RadioGroup<>("publicPrivateGroup", new PropertyModel<Boolean>(theCampaign, "isPublic"));
        campaignForm.add(publicPrivateGroup);
        publicPrivateGroup.add(new Radio<>("public", Model.of(Boolean.TRUE)));
        publicPrivateGroup.add(new Radio<>("private", Model.of(Boolean.FALSE)));
    }

    public Campaign getTheCampaign() {
        return theCampaign;
    }

    public void setTheCampaign(Campaign theCampaign) {
        this.theCampaign = theCampaign;
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
        campaignService.detach(theCampaign);
    }

    /**
     * @param idParam
     */
    protected void loadCampaign(final StringValue idParam) {
        if (idParam.isEmpty()) {
            setPageTitle(new StringResourceModel("page.create.title", this, null));
            add(new Label("header", new StringResourceModel("form.create.header", this, null)));
            theCampaign = new Campaign();
        } else {
            setPageTitle(new StringResourceModel("page.edit.title", this, null));
            add(new Label("header", new StringResourceModel("form.edit.header", this, null)));
            // set the campaign we got from previous page
            try {
                theCampaign = campaignService.getById(idParam.toOptionalLong());

                // check if campaign leader is the loggedIn user or SUPER_ADMIN, if not then redirect this page to details page
                if (!theCampaign.isEditableBy(loggedInUser)) {
                    setResponsePage(ChallengeDefinitionPage.class, new PageParameters().set("id", theCampaign.getId()));
                }
                //set UnGoals
                List<String> unGoalsString = new LinkedList<>();
                for (UNGoalType unGoal : theCampaign.getuNGoals()) {
                    unGoalsString.add(unGoal.getGoal());
                }
                uNGoalTypes = new ArrayList<String>(unGoalsString);
            } catch (Exception e) {
                throw new RestartResponseException(ErrorPage.class);
            }
        }
    }

    /**
     * @return
     */
    private Form<Void> newCampaignForm() {
        Form<Void> form = new InputValidationForm<Void>("campaignForm") {
            /**
             *
             */
            private static final long serialVersionUID = 2891050071795850361L;

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
        if (campaignSupportingDocUploadFileUploadVisible) {
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
                campaignSupportingDocUploadField.getMarkupId(),
                campaignSupportingDocUploadHiddenField.getMarkupId(),
                campaignSupportingDocUploadFileTableBody.getMarkupId());
    }

    private TextArea<String> newTextAreaForChallenge(String id) {
        return new TextArea<String>(id, new PropertyModel<String>(theCampaign, id));
    }

    /**
     * @return
     */
    private InputBorder<String> newInputBorder(String id, TextArea<String> textArea) {
        return new OnEventInputBeanValidationBorder<String>(id, textArea, new Model<String>(), HtmlEvent.ONCHANGE);
    }

    /**
     * @param email
     * @param model
     * @return
     */
    private TextField<String> newEmailTextField(String email, PropertyModel<String> model) {
        return new TextField<>(email, model);
    }

    private InputBorder<String> newEmailTextFieldValidationBorder(String id, final TextField<String> textField) {
        return new InputBorder<>(id, textField, new Model<String>());
    }

    /**
     * @return
     */
    private TextField<String> newReferencesLinkTextField() {
        return new TextField<>("referencesLink", new PropertyModel<String>(theCampaign, "referencesLink"));
    }

    /**
     * @param dateTextField
     * @return
     */
    private InputBorder<Date> newDateTextField(String id, final DateTextField dateTextField) {
        return new OnEventInputBeanValidationBorder<>(id, dateTextField, new Model<String>(), HtmlEvent.ONCHANGE);
    }

    /**
     * @return
     */
    private DateTextField newIdeationDateTextField(final String id) {
        final DateTextFieldConfig config;
        config = new DateTextFieldConfig().withFormat("dd.MM.yyyy").allowKeyboardNavigation(true).autoClose(true)
                .highlightToday(true).withStartDate(new DateTime(new Date())).showTodayButton(false);
        DateTextField dateTextField = new DateTextField(id, new PropertyModel<Date>(theCampaign, id), config) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            protected void onConfigure() {
                if (id.equals(ideationStartDateField.getId())) {
                    // if challenge is open for discussion, then ideation start date field has to be same as definition enddate.
                    // based on challenge is open for discussion or not?
                    if (!theCampaign.getOpenForDiscussion()) {
                        // if challenge is not open for discussion then do not allow user to input ideation start date
                        if (ideationStartDateField.getDefaultModelObject() == null) {
                            ideationStartDateField.setDefaultModelObject(new Date());
                        } else {
                            ideationStartDateField.setDefaultModelObject(ideationStartDateField.getDefaultModelObject());
                        }
                    } else {
                        ideationStartDateField.setDefaultModelObject(challengeOpenForDiscussionEndDateField.getModelObject());
                    }
                    // do not let user define start date
                    setEnabled(false);
                } else if (id.equals(ideationEndDateField.getId())) {
                    if (ideationStartDateField.getModelObject() != null) {
                        config.withStartDate(new DateTime(DateUtils.addDays(ideationStartDateField.getModelObject(), 0)))
                                .showTodayButton(true);
                    }
                }
            }
        };
        return dateTextField;
    }

    /**
     * @return
     */
    private DateTextField newSelectionDateTextField(final String id) {
        final DateTextFieldConfig config;
        config = new DateTextFieldConfig().withFormat("dd.MM.yyyy").allowKeyboardNavigation(true).autoClose(true)
                .highlightToday(true).withStartDate(new DateTime(new Date())).showTodayButton(false);
        DateTextField dateTextField = new DateTextField(id, new PropertyModel<Date>(theCampaign, id), config) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            protected void onConfigure() {
                // reset selection start date when ideation end date is selected.
                if (id.equals(selectionStartDateField.getId())) {
                    if (ideationEndDateField.getModelObject() != null) {
                        config.withStartDate(new DateTime(DateUtils.addDays(ideationEndDateField.getModelObject(), 0)))
                                .showTodayButton(true);
                        selectionStartDateField.setDefaultModelObject(ideationEndDateField.getModelObject());
                    }
                    // do not let user define start date
                    setEnabled(false);
                }

                // reset selection end date when selection start date is selected.
                if (id.equals(selectionEndDateField.getId())) {
                    if (selectionStartDateField.getModelObject() != null) {
                        config.withStartDate(new DateTime(DateUtils.addDays(selectionStartDateField.getModelObject(), 0)))
                                .showTodayButton(true);
                    }
                }
            }
        };
        return dateTextField;
    }

    /**
     * @return
     */
    private DateTextField newChallengeOpenForDiscussionDateTextField(final String id) {
        final DateTextFieldConfig config;

        config = new DateTextFieldConfig().withFormat("dd.MM.yyyy").allowKeyboardNavigation(true).autoClose(true)
                .highlightToday(true).withStartDate(new DateTime(new Date())).showTodayButton(false);


        DateTextField dateTextField = new DateTextField(id, new PropertyModel<Date>(theCampaign, id), config) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            protected void onConfigure() {
                if (theCampaign.getOpenForDiscussion()) {
                    if (id.equals(challengeOpenForDiscussionStartDateField.getId())) {
                        if (challengeOpenForDiscussionStartDateField.getDefaultModelObject() == null) {
                            challengeOpenForDiscussionStartDateField.setDefaultModelObject(new Date());
                        } else {
                            challengeOpenForDiscussionStartDateField.setDefaultModelObject(challengeOpenForDiscussionStartDateField
                                    .getDefaultModelObject());
                        }
                        // do not let user define start date
                        setEnabled(false);
                    } else if (id.equals(challengeOpenForDiscussionEndDateField.getId())) {
                        config.withStartDate(
                                new DateTime(DateUtils.addDays(challengeOpenForDiscussionStartDateField.getModelObject(), 0)))
                                .showTodayButton(true);
                    }
                } else {
                    challengeOpenForDiscussionStartDateField.setDefaultModelObject(null);
                    challengeOpenForDiscussionEndDateField.setDefaultModelObject(null);
                }

                setVisible(theCampaign.getOpenForDiscussion());
            }
        };
        return dateTextField;
    }

    /**
     * @return
     */
    private Select2MultiChoice<Tag> newKeywordsSelect2MultiChoice() {
        Select2MultiChoice<Tag> select2MultiChoice = new Select2MultiChoice<>("keywords", new PropertyModel<Collection<Tag>>(
                theCampaign, "tags"), new TagProvider(tagService.getAll()));
        select2MultiChoice.getSettings().setMinimumInputLength(1);
        select2MultiChoice.getSettings().setCreateSearchChoice(
                "function(term) { if (term.length > 1) { return { id: term, text: term }; } }");
        select2MultiChoice.setRequired(true);
        return select2MultiChoice;
    }

    /**
     * @return
     */
    private AjaxSubmitLink newSubmitLink(String id) {
        return new AjaxSubmitLink(id, campaignForm) {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (theCampaign.getId() == null) {
                    this.add(new AttributeModifier("value", new StringResourceModel("challenges.submitchallenge.button", this,
                            null).getString()));
                } else {
                    this.add(new AttributeModifier("value", new StringResourceModel("challenges.updatechallenge.button", this,
                            null).getString()));
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
    private Link<CampaignsPage> newCancelLink() {
        return new Link<CampaignsPage>("cancel") {
            private static final long serialVersionUID = -310533532532643267L;

            @Override
            public void onClick() {
                setResponsePage(CampaignsPage.class);
            }
        };
    }

    /**
     *
     */
    protected void save(AjaxRequestTarget target) {
        saveCampaign(target);
        Session.get().success(new StringResourceModel("saved.message", this, Model.of(theCampaign)).getString());

        try {
            sendInvitations();
            Session.get().warn(new StringResourceModel("send.email.message", this, null).getString());
        } catch (NotificationException e) {
            logger.error("Invitation can not be sent from Create Challenge Page", e);
            Session.get().warn(new StringResourceModel("send.email.message.fail", this, null).getString());
        }

        final PageParameters pageParameters = new PageParameters()
                .set(MESSAGE_PARAM, new StringResourceModel("saved.message", this, Model.of(theCampaign)).getString())
                .set(LEVEL_PARAM, FeedbackMessage.SUCCESS);
        setResponsePage(CampaignsPage.class, pageParameters);
    }

    /**
     *
     */
    private void saveCampaign(AjaxRequestTarget target) {
        // set UNGoal of challenge
        List<UNGoalType> goals = new LinkedList<>();
        if (listOfUNGoalTypes.getConvertedInput() != null) {
            for (String unGoal : listOfUNGoalTypes.getConvertedInput()) {
                if (unGoal.compareToIgnoreCase(UNGoalType.GOAL3.getGoal()) == 0) {
                    goals.add(UNGoalType.GOAL3);
                } else if (unGoal.compareToIgnoreCase(UNGoalType.GOAL4.getGoal()) == 0) {
                    goals.add(UNGoalType.GOAL4);
                } else if (unGoal.compareToIgnoreCase(UNGoalType.GOAL8.getGoal()) == 0) {
                    goals.add(UNGoalType.GOAL8);
                }
            }
        }
        theCampaign.setuNGoals(goals);

        if (theCampaign.getOpenForDiscussion()) {
            theCampaign.setStartDate(theCampaign.getChallengeOpenForDiscussionStartDate());
            theCampaign.setDueDate(theCampaign.getChallengeOpenForDiscussionEndDate());
        } else {
            theCampaign.setStartDate(theCampaign.getIdeationStartDate());
            theCampaign.setDueDate(theCampaign.getIdeationEndDate());
        }

        // logic for Challenge Innovation Status
        theCampaign.setInnovationStatusBasedOnDates();

        theCampaign.getTags().addAll(keywords.getConvertedInput());

        theCampaign.setAttachments(campaignSupportingDocFileUploadInfo.getFileInfos(campaignSupportingDocUploadHiddenField.getModelObject()));

        if (theCampaign.getId() == null) {
            // new campaign => create
            if (!theCampaign.getLcPhase().equals(Inception.class)) {
                if (theCampaign.getActive()) {
                    theCampaign.setLcPhase(Inception.class);
                }
            }

            // to be deleted later?
            theCampaign.setDescription("Social Innovation for a better world");

            // there is no scope yet, so set default one!
            if (theCampaign.getScope() == null && loggedInUser.getCurrentCompany() != null) {
                StaffScope staffScope = new StaffScope();
                staffScope.setCompany(loggedInUser.getCurrentCompany());
                staffScope.setScopeType(ScopeType.STAFF_ALL);
                staffScope = (StaffScope) scopeService.create(staffScope);
                theCampaign.setScope(staffScope);
            }

            // Challenge image update
            FileInfo profilePicture = saveUploadedPicture(target);
            if (profilePicture != null) {
                theCampaign.setChallengeImage(profilePicture);
            }

            challengeProfilePicture.setImageResource(ChallengePictureResource.get(PictureType.PROFILE, theCampaign));
            target.add(challengeProfilePicture);

            // To be removed later everywhere, since this is not anymore needed in SOCRATIC
            // added to avoid null pointer exceptions
            InnovationObjective io = new InnovationObjective();
            io.setCompany(loggedInUser.getCurrentCompany());
            io.setName("Social Innovation");
            io.setCampaigns(Collections.singletonList(theCampaign));
            theCampaign.setInnovationObjective(io);

            // set yesNoState to no when dates for discussion is over
            if (challengeOpenForDiscussionEndDateField.getDefaultModelObject() != null
                    && theCampaign.getChallengeOpenForDiscussionEndDate().compareTo(new Date()) < 0) { // if ideation has started
                theCampaign.setOpenForDiscussion(false);
            }

            if (theCampaign.getActive() != null && theCampaign.getActive()) {
                // set start date to now!
                theCampaign.setStartDate(new Date());
            }
            theCampaign.setCreatedBy(loggedInUser);
            theCampaign.setCompanyFromCurrentUserEmployment(loggedInUser);
            theCampaign.setCompany(loggedInUser.getCurrentCompany());
            // c.setSocialChallenge("socialChallenge");
            theCampaign = campaignService.create(theCampaign);

            // when being created, add campaign to followedCampaign list by default
            loggedInUser = userService.addChallengeToFollowedChallegesList(theCampaign, loggedInUser.getId());

            // Notification
            List<User> usersInScope = scopeService.getAllUsersInScope(theCampaign.getScope());
            campaignService.notifyAboutParticipation(usersInScope, theCampaign);
            // Notify everyone based on skills&interests
            List<User> allRegisteredUsers = userService.getAllRegisteredUsers();
            campaignService.notifyAboutParticipationBasedOnSkillsAndIntersts(allRegisteredUsers, theCampaign,
                    NotificationType.CAMPAIGN_NEW);

            logger.info("The user " + loggedInUser.getNickName() + " created the challenge " + theCampaign.getName());

        } else {
            // set last modified date
            theCampaign.setLastModified(new Date());

            // set file upload field to hidden
            campaignSupportingDocUploadFileUploadVisible = false;

            // Challenge image update
            FileInfo profilePicture = saveUploadedPicture(target);
            if (profilePicture != null) {
                theCampaign.setChallengeImage(profilePicture);
            }
            challengeProfilePicture.setImageResource(ChallengePictureResource.get(PictureType.PROFILE, theCampaign));
            target.add(challengeProfilePicture);

            // existing campaign => update
            theCampaign = campaignService.update(theCampaign);
            logger.info("The user " + loggedInUser.getNickName() + " updated the challenge " + theCampaign.getName());

            // notify about updated campaign
            campaignService.notifyCampaignFollowersAboutCampaignUpdates(theCampaign);
        }
    }

    private void sendInvitations() throws NotificationException {
        // Do not allow message to be sent if user is already registered on platform
        if (StringUtils.isNotBlank(invitationEmails)) {
            // avoid duplicate emails..
            Set<String> noRepeatedEmails = new HashSet<>(Arrays.asList(invitationEmails.split("\\s+")));
            String message = messageToInvitedContactTextArea.getModelObject();
            invitationMailService.sendInvitationMessageFromChallenge(
                    loggedInUser,
                    urlProvider.urlFor(RegisterPage.class, new PageParameters()),
                    urlProvider.urlFor(
                            ChallengeDefinitionPage.class,
                            new PageParameters().set("id", theCampaign.getId())),
                    message,
                    theCampaign.getName(),
                    noRepeatedEmails.toArray(new String[noRepeatedEmails.size()]));
        }
    }

    /**
     * @param target
     */
    protected void showErrors(AjaxRequestTarget target) {
        // in case of errors (e.g. validation errors) show error
        // messages in form
        target.add(campaignForm);
        target.add(campaignSupportingDocUploadField);
        target.add(campaignSupportingDocUploadHiddenField);
        target.add(campaignSupportingDocUploadFileTableBody);

        // also reload feedback panel
        target.add(feedbackPanel);
        // do not hide file upload panel if it was shown before
        if (campaignSupportingDocUploadFileUploadVisible) {
            toggleAttachmentsForm(target);
        }
    }

    private NonCachingImage newProfilePicturePreview() {
        challengeProfilePicture = new NonCachingImage("profilePicturePreview", ChallengePictureResource.get(
                PictureType.PROFILE, theCampaign));
        challengeProfilePicture.setOutputMarkupId(true);
        return challengeProfilePicture;
    }

    private AjaxLink<Void> newProfileClearButton() {
        return new AjaxLink<Void>("profilePictureClear") {
            private static final long serialVersionUID = -1439023360594387449L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(theCampaign != null && theCampaign.getChallengeImage() != null);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                theCampaign.setChallengeImage(null);
                challengeProfilePicture.setImageResource(ChallengePictureResource.get(PictureType.PROFILE, theCampaign));
                target.add(challengeProfilePicture);
                target.add(clearPictureLink);
            }
        };
    }

    /**
     * @param target
     * @return
     */
    private FileInfo saveUploadedPicture(AjaxRequestTarget target) {
        FileUpload image = fileUploadField.getFileUpload();
        if (image != null) {
            if (isUploadedFileAnImage(image)) {
                final File uploadFolder = uploadHelper.createUploadFolderWithDir(UUID.randomUUID().toString(), null);
                try {
                    ChallengePictureResource.createProfilePictureFromUpload(image, uploadFolder, theCampaign,
                            PictureType.THUMBNAIL);
                } catch (IOException e) {
                    logger.error(e);
                }

                try {
                    ChallengePictureResource.createProfilePictureFromUpload(image, uploadFolder, theCampaign,
                            PictureType.PROFILE);
                } catch (IOException e) {
                    logger.error(e);
                }

                try {
                    return ChallengePictureResource.createProfilePictureFromUpload(image, uploadFolder, theCampaign,
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
}
