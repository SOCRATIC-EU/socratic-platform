/**
 *
 */
package de.atb.socratic.web.profile;

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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.vaynberg.wicket.select2.Select2MultiChoice;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.form.DateTextField;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.form.DateTextFieldConfig;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.model.event.UserTagAdded;
import de.atb.socratic.model.validation.NickNameInputValidator;
import de.atb.socratic.model.validation.ProfilePictureInputValidator;
import de.atb.socratic.service.employment.CompanyService;
import de.atb.socratic.service.employment.EmploymentService;
import de.atb.socratic.service.other.TagService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.components.TinyMCETextArea;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.settings.UserSettingsDashboardPage;
import de.atb.socratic.web.inception.CampaignAddEditPage;
import de.atb.socratic.web.inception.CampaignsPage;
import de.atb.socratic.web.provider.LoggedInUserProvider;
import de.atb.socratic.web.provider.TagProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.upload.FileUploadHelper;
import de.atb.socratic.web.upload.FileUploadHelper.UploadType;
import org.apache.tika.mime.MediaType;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.lang.Bytes;
import org.jboss.solder.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class EditUserPage extends BasePage {

    private static final long serialVersionUID = 7987374272110834557L;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @Inject
    LoggedInUserProvider loggedInUserProvider;

    @Inject
    Event<User> userEventSrc;

    @Inject
    TagService tagService;

    @EJB
    UserService userService;

    @EJB
    EmploymentService employmentService;

    @EJB
    CompanyService companyService;

    @Inject
    Logger logger;

    private final Form<User> form;
    private final TextField<String> emailTextField;
    private final TextField<String> nickNameTextField;
    private final TextField<String> firstNameTextField;
    private final TextField<String> lastNameTextField;

    private final TextField<String> countryTextField;
    private final TextField<String> cityTextField;
    private final TextField<String> websiteTextField;

    private final Select2MultiChoice<Tag> skills, interests;
    private final AjaxLink<Void> clearPictureLink;
    private final FileUploadField fileUploadField;
    private NonCachingImage userProfilePicture;
    private final FileUploadHelper uploadHelper = new FileUploadHelper(UploadType.USER);
    private User profileUser;
    private Long userId;
    private final DateTextField birthDateField;
    private final Label ageLabel;
    private final Label ageUnitLabel;
    private final InputBorder<String> conditionTextAreaDisabled;
    private final InputBorder<String> conditionTextArea;

    private final TextField<String> facebookUrlTextArea;
    private final TextField<String> linkedInUrlTextArea;
    private final TextField<String> twitterUrlTextArea;

    protected final InputBorder<String> facebookUrlTextAreaBorder;
    protected final InputBorder<String> linkedInUrlTextAreaBorder;
    protected final InputBorder<String> twitterUrlTextAreaBorder;

    @Inject
    Event<UserTagAdded> userTagEventSource;

    private static final JavaScriptResourceReference UPLOADED_IMAGE_PREVIEW_JS = new JavaScriptResourceReference(CampaignAddEditPage.class, "uploadImagePreview.js");

    /**
     * @param parameters
     */
    public EditUserPage(PageParameters parameters) {
        super(parameters);

        this.userId = loggedInUser.getId();
        if (userId != null) {
            // detach entity to avoid automatic update of changes in form.
            profileUser = userService.getById(userId);
        }
        if (profileUser == null) {
            throw new RestartResponseException(CampaignsPage.class);
        }
        // add form
        form = new InputValidationForm<User>("form") {
            private static final long serialVersionUID = -6453044254742009238L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(true);
            }

            @Override
            protected void onAfterRender() {
                super.onAfterRender();
            }

            @Override
            protected void onErrorHandling() {
                super.onErrorHandling();
                // copy form error message caused by breach of max file upload size to file upload field
                for (FeedbackMessage message : getFeedbackMessages()) {
                    if (message.getLevel() >= FeedbackMessage.ERROR) {
                        fileUploadField.error(message.getMessage());
                    }
                }
                // and remove the error messages from the form
                // so that on subsequent submits the form dies not think it is still in error
                getFeedbackMessages().clear();
                // also clear the file upload field - just in case
                clearFileUploadOnError();
            }
        };
        add(form);

        // add text field for email inside a border component that performs bean
        // validation
        emailTextField = new TextField<String>("email", new PropertyModel<String>(profileUser, "email")) {
            private static final long serialVersionUID = 6571239714254783337L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // email can never be changed
                setEnabled(false);
            }
        };
        form.add(new OnEventInputBeanValidationBorder<>(
                "emailValidationBorder",
                emailTextField,
                new StringResourceModel("email.input.label", this, null),
                HtmlEvent.ONCHANGE));

        // add text field for nick name inside a border component that performs
        // bean validation
        nickNameTextField = new TextField<>("nickName", new PropertyModel<String>(profileUser, "nickName"));
        form.add(new OnEventInputBeanValidationBorder<>(
                "nickNameValidationBorder",
                nickNameTextField,
                new StringResourceModel("nickname.input.label", this, null),
                HtmlEvent.ONCHANGE));
        form.add(new NickNameInputValidator(nickNameTextField));
        nickNameTextField.setOutputMarkupId(true);

        // add text field for first name inside a border component that performs
        // bean validation
        firstNameTextField = new TextField<>("firstName", new PropertyModel<String>(profileUser, "firstName"));
        form.add(new OnEventInputBeanValidationBorder<>(
                "firstNameValidationBorder",
                firstNameTextField,
                new StringResourceModel("firstname.input.label", this, null),
                HtmlEvent.ONCHANGE));

        // add text field for last name inside a border component that performs
        // bean validation
        lastNameTextField = new TextField<>("lastName", new PropertyModel<String>(profileUser, "lastName"));
        form.add(new OnEventInputBeanValidationBorder<>(
                "lastNameValidationBorder",
                lastNameTextField,
                new StringResourceModel("lastname.input.label", this, null),
                HtmlEvent.ONCHANGE));

        // Skills
        skills = newTagsSelect2MultiChoice("skills");
        form.add(new InputBorder<>(
                "skillsBorder",
                skills,
                new StringResourceModel("skills.input.label", this, null)));

        //Interests
        interests = newTagsSelect2MultiChoice("interests");
        form.add(new InputBorder<>(
                "interestsBorder",
                interests,
                new StringResourceModel("interests.input.label", this, null)));

        //birthDate
        form.add(newBirthDateTextField(birthDateField = newDateTextField()));

        // add a label displaying the Age based on selected birth date
        ageLabel = new Label("age", computeAge());
        form.add(ageLabel.setOutputMarkupId(true));
        ageUnitLabel = new Label("ageUnit", computeAgeUnit());
        form.add(ageUnitLabel.setOutputMarkupId(true));

        // country & city
        countryTextField = new TextField<>("country", new PropertyModel<String>(profileUser, "country"));
        form.add(new OnEventInputBeanValidationBorder<>(
                "countryValidationBorder",
                countryTextField,
                new StringResourceModel("country.input.label", this, null),
                HtmlEvent.ONCHANGE));

        cityTextField = new TextField<>("city", new PropertyModel<String>(profileUser, "city"));
        form.add(new OnEventInputBeanValidationBorder<>(
                "cityValidationBorder",
                cityTextField,
                new StringResourceModel("city.input.label", this, null),
                HtmlEvent.ONCHANGE));

        // add website
        websiteTextField = new TextField<>("website", new PropertyModel<String>(profileUser, "website"));
        form.add(new OnEventInputBeanValidationBorder<>(
                "websiteValidationBorder",
                websiteTextField,
                new StringResourceModel("website.input.label", this, null),
                HtmlEvent.ONCHANGE));

        // add text area input for user condition
        conditionTextAreaDisabled = newConditionTextArea(true);
        conditionTextArea = newConditionTextArea(false);
        form.add(conditionTextArea);

        facebookUrlTextArea = newTextField("facebookUrl");
        form.add(facebookUrlTextAreaBorder = new InputBorder<>("facebookUrlTextAreaBorder", facebookUrlTextArea, new StringResourceModel(
                "user.facebookUrl.input.label", this, null)));

        linkedInUrlTextArea = newTextField("linkedInUrl");
        form.add(linkedInUrlTextAreaBorder = new InputBorder<>("linkedInUrlTextAreaBorder", linkedInUrlTextArea, new StringResourceModel(
                "user.linkedInUrl.input.label", this, null)));

        twitterUrlTextArea = newTextField("twitterUrl");
        form.add(twitterUrlTextAreaBorder = new InputBorder<>("twitterUrlTextAreaBorder", twitterUrlTextArea, new StringResourceModel(
                "user.twitterUrl.input.label", this, null)));

        // help text for social media urls
        facebookUrlTextAreaBorder.add(addToolTipWebMarkupContainer("facebookUrlHelpText", new StringResourceModel("facebookUrl.helpText.desc.label",
                this, null), TooltipConfig.Placement.right));

        linkedInUrlTextAreaBorder.add(addToolTipWebMarkupContainer("linkedInUrlHelpText", new StringResourceModel("linkedInUrl.helpText.desc.label",
                this, null), TooltipConfig.Placement.right));

        twitterUrlTextAreaBorder.add(addToolTipWebMarkupContainer("twitterUrlHelpText", new StringResourceModel("twitterUrl.helpText.desc.label",
                this, null), TooltipConfig.Placement.right));

        final WebMarkupContainer buttonPanel = new WebMarkupContainer("buttonPanel") {
            private static final long serialVersionUID = 5935440415189378322L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // only show if form is enabled
                setVisible(true);
            }
        };
        form.add(buttonPanel.setOutputMarkupPlaceholderTag(true));

        // add a submit link
        buttonPanel.add(new AjaxSubmitLink("submit", form) {
            private static final long serialVersionUID = 4874502607304626862L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                updateUser(target);
                setResponsePage(UserProfileDetailsPage.class, new PageParameters().set("id", profileUser.getId()));
                logger.info("The user " + loggedInUser.getNickName() + " updated the user profile.");
            }

            protected void onError(AjaxRequestTarget target, Form<?> form) {
                showError(target);
            }
        });


        // add a back link
        buttonPanel.add(new AjaxLink<User>("back") {
            private static final long serialVersionUID = -4776506958975416730L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(UserSettingsDashboardPage.class);
            }

            @Override
            protected void onConfigure() {
                setVisible(true);
                super.onConfigure();
            }
        });

        fileUploadField = new FileUploadField("profilePicture");
        form.add(new InputBorder<>(
                "profilePictureBorder",
                fileUploadField,
                new StringResourceModel("profile.picture.label", this, null)));
        form.add(new ProfilePictureInputValidator(fileUploadField));

        form.add(new Label(
                "profilePicturePreviewLabel",
                new StringResourceModel("profile.picture.preview.label", this, null)));
        form.add(newProfilePicturePreview());
        form.add(clearPictureLink = newProfileClearButton());

        form.setMultiPart(true);
        form.setMaxSize(Bytes.megabytes(1));
    }

    private Integer computeAge() {
        if (birthDateField.getModelObject() != null) {
            //Birth date  
            LocalDate birthday = new LocalDate(birthDateField.getModelObject());
            Period period = new Period(birthday, new LocalDate(), PeriodType.years());
            return period.getYears();
        }
        return null;
    }

    private IModel<String> computeAgeUnit() {
        final Integer age = computeAge();
        return age == null ?
                Model.of("") :
                new StringResourceModel("age.unit." + (age <= 1 ? "singular" : "plural"), this, null);
    }

    private TextField<String> newTextField(String id) {
        return new TextField<String>(id, new PropertyModel<String>(profileUser, id));
    }

    /**
     * @param birthDateTextField
     * @return
     */
    private InputBorder<Date> newBirthDateTextField(final DateTextField birthDateTextField) {
        return new OnEventInputBeanValidationBorder<>(
                "birthDateValidationBorder",
                birthDateTextField,
                new StringResourceModel("birth.input.label", this, null),
                HtmlEvent.ONCHANGE);
    }

    private InputBorder<String> newConditionTextArea(final boolean readOnly) {
        return new OnEventInputBeanValidationBorder<String>(
                "conditionValidationBorder",
                new TinyMCETextArea("condition", new PropertyModel<String>(profileUser, "condition"), readOnly),
                new StringResourceModel("condi.input.label", this, null),
                HtmlEvent.ONBLUR) {
            private static final long serialVersionUID = 6442485386896348450L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                //do not show at the moment, since its unclear what it is
                setVisible(false);
            }
        };
    }

    /**
     * @return
     */
    private DateTextField newDateTextField() {
        DateTextFieldConfig config = new DateTextFieldConfig()
                .withFormat("dd.MM.yyyy")
                .allowKeyboardNavigation(true)
                .autoClose(true)
                .highlightToday(true)
                .showTodayButton(false)
                .withEndDate(new DateTime())
                .withStartDate(new DateTime(1900, 1, 1, 0, 0));
        DateTextField dateTextField =
                new DateTextField("birthDate", new PropertyModel<Date>(profileUser, "birthDate"), config);
        dateTextField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = -5456811027994150783L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                ageLabel.setDefaultModelObject(computeAge());
                ageUnitLabel.setDefaultModel(computeAgeUnit());
                target.add(ageLabel);
                target.add(ageUnitLabel);
            }
        });
        return dateTextField;
    }

    private void clearFileUploadOnError() {
        fileUploadField.clearInput();
        if (fileUploadField.getFileUploads() != null) {
            fileUploadField.getFileUploads().clear();
        }
        fileUploadField.updateModel();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        skills.getSettings().setPlaceholder(
                new StringResourceModel("skills.input.placeholder", this.getPage(), null).getString());
        interests.getSettings().setPlaceholder(
                new StringResourceModel("interests.input.placeholder", this.getPage(), null).getString());
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();
        userService.detach(profileUser);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptReferenceHeaderItem.forReference(UPLOADED_IMAGE_PREVIEW_JS));
    }

    private NonCachingImage newProfilePicturePreview() {
        userProfilePicture = new NonCachingImage("profilePicturePreview", ProfilePictureResource.get(PictureType.PROFILE, profileUser));
        userProfilePicture.setOutputMarkupId(true);
        return userProfilePicture;
    }

    private AjaxLink<Void> newProfileClearButton() {
        return new AjaxLink<Void>("profilePictureClear") {
            private static final long serialVersionUID = -1439023360594387449L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(profileUser != null && profileUser.getProfilePictureFile() != null);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                profileUser.setProfilePictureFile(null);
                userProfilePicture.setImageResource(ProfilePictureResource.get(PictureType.PROFILE, profileUser));
                target.add(userProfilePicture);
                target.add(clearPictureLink);
            }
        };
    }

    /**
     * @param target
     */
    private void updateUser(AjaxRequestTarget target) {
        // update user
        profileUser.setEmail(emailTextField.getModelObject());
        profileUser.setNickName(nickNameTextField.getModelObject());
        profileUser.setFirstName(firstNameTextField.getModelObject());
        profileUser.setLastName(lastNameTextField.getModelObject());
        // profileUser.setReceiveNotifications(notificationsCheckBox.getModelObject());

        FileInfo profilePicture = saveUploadedPicture(target);
        if (profilePicture != null) {
            profileUser.setProfilePictureFile(profilePicture);
        }
        userProfilePicture.setImageResource(ProfilePictureResource.get(PictureType.PROFILE, profileUser));
        target.add(userProfilePicture);

        // skills & interests
        profileUser.setSkills((List<Tag>) skills.getModelObject());
        profileUser.setInterests((List<Tag>) interests.getModelObject());

        //BirthDate
        if (birthDateField.getModelObject() != null) {
            profileUser.setBirthDate(birthDateField.getModelObject());
        }

        // social media urls
        profileUser.setFacebookUrl(facebookUrlTextArea.getModelObject());
        profileUser.setLinkedInUrl(linkedInUrlTextArea.getModelObject());
        profileUser.setTwitterUrl(twitterUrlTextArea.getModelObject());

        profileUser = userService.update(profileUser);

        // fire event to tell LoggedInUserProvider to reload user from database
        userEventSrc.fire(profileUser);

        // fire an event to check if given user skills and interests matches to Action skills and interests?
        UserTagAdded userSkillAdded = new UserTagAdded();
        userSkillAdded.setUser(profileUser);
        if (skills.getModelObject() != null && !skills.getModelObject().isEmpty()) {
            userSkillAdded.getAddedSkillsOrInterest().addAll((List<Tag>) skills.getModelObject());
        }

        if (interests.getModelObject() != null && !interests.getModelObject().isEmpty()) {
            userSkillAdded.getAddedSkillsOrInterest().addAll((List<Tag>) interests.getModelObject());
        }

        if (userSkillAdded.getAddedSkillsOrInterest() != null && !userSkillAdded.getAddedSkillsOrInterest().isEmpty()) {
            userTagEventSource.fire(userSkillAdded);
        }

        // show feedback message
        getPage().success("Your profile has been updated!");
        target.add(feedbackPanel);

        // clear form
        clearForm();

        updateNavbar();
    }

    /**
     * @param target
     * @return
     */
    private FileInfo saveUploadedPicture(AjaxRequestTarget target) {
        FileUpload image = fileUploadField.getFileUpload();
        if (image != null) {
            if (isUploadedFileAnImage(image)) {
                final File uploadFolder = uploadHelper.createUploadFolderWithDir(profileUser.getUploadCacheId(), null);
                try {
                    ProfilePictureResource.createProfilePictureFromUpload(image, uploadFolder, profileUser, PictureType.PROFILE);
                } catch (IOException e) {
                    logger.error(e);
                }

                try {
                    return ProfilePictureResource.createProfilePictureFromUpload(image, uploadFolder, profileUser, PictureType.THUMBNAIL);
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
        if (!"image".equalsIgnoreCase(type.getType())) return false;
        boolean gif = "gif".equalsIgnoreCase(type.getSubtype());
        boolean png = "png".equalsIgnoreCase(type.getSubtype());
        boolean jpg = "jpeg".equalsIgnoreCase(type.getSubtype());
        return gif || png || jpg;
    }

    /**
     * @param target
     */
    private void showError(AjaxRequestTarget target) {
        // show feedback message
        target.add(feedbackPanel);
        // re-render form
        target.add(form);
    }

    /**
     *
     */
    private void clearForm() {
        form.clearInput();
        form.modelChanged();
        emailTextField.setModelObject(profileUser.getEmail());
        nickNameTextField.setModelObject(profileUser.getNickName());
        firstNameTextField.setModelObject(profileUser.getFirstName());
        lastNameTextField.setModelObject(profileUser.getLastName());
        skills.setModelObject(profileUser.getSkills());
        interests.setModelObject(profileUser.getInterests());
        birthDateField.setModelObject(profileUser.getBirthDate());
        ageLabel.setDefaultModelObject(computeAge());
        ageUnitLabel.setDefaultModel(computeAgeUnit());
    }

    /**
     * @param target
     */
    private void enableForm(AjaxRequestTarget target) {
        // add editable textarea for condition
        conditionTextAreaDisabled.replaceWith(conditionTextArea);
        // re-render form and editLink
        target.add(form);
        target.add(clearPictureLink);
    }


    /**
     * @return
     */
    private Select2MultiChoice<Tag> newTagsSelect2MultiChoice(String property) {
        Select2MultiChoice<Tag> select2MultiChoice = new Select2MultiChoice<>(
                property,
                new PropertyModel<Collection<Tag>>(profileUser, property),
                new TagProvider(tagService.getAll()));
        select2MultiChoice.getSettings().setTokenSeparators(new String[]{","});
        select2MultiChoice.getSettings().setMinimumInputLength(1);
        select2MultiChoice.getSettings().setCreateSearchChoice(
                "function(term) { if (term.length > 1) { return { id: term, text: term }; } }");
        return select2MultiChoice;
    }

}
