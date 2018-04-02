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
import de.agilecoders.wicket.markup.html.bootstrap.extensions.form.DateTextField;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.form.DateTextFieldConfig;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.event.UserTagAdded;
import de.atb.socratic.model.validation.NickNameInputValidator;
import de.atb.socratic.model.validation.ProfilePictureInputValidator;
import de.atb.socratic.service.employment.CompanyService;
import de.atb.socratic.service.employment.EmploymentService;
import de.atb.socratic.service.other.TagService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.TinyMCETextArea;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
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
public abstract class EditUserPanel extends Panel {

    private static final long serialVersionUID = 7987374272110834557L;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @Inject
    LoggedInUserProvider loggedInUserProvider;

    @Inject
    Event<User> userEventSrc;

    @Inject
    Event<UserTagAdded> userTagEventSource;

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

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;
    private final Form<User> form;
    private final TextField<String> emailTextField;
    private final TextField<String> nickNameTextField;
    private final TextField<String> firstNameTextField;
    private final TextField<String> lastNameTextField;
    private final Select2MultiChoice<Tag> skills, interests;
    private final AjaxLink<String> editLink;
    private final AjaxLink<Void> clearPictureLink;
    private final FileUploadField fileUploadField;
    private NonCachingImage userProfilePicture;
    private final FileUploadHelper uploadHelper = new FileUploadHelper(UploadType.USER);
    private boolean formEnabled = false;
    private boolean originEdit;
    private User profileUser;
    private Long userId;
    private final DateTextField birthDateField;
    private final Label ageLabel;
    private final Label ageUnitLabel;
    private final InputBorder<String> conditionTextAreaDisabled;
    private final InputBorder<String> conditionTextArea;

    /**
     * @param id
     * @param feedbackPanel
     * @param userId
     * @param edit
     */
    EditUserPanel(final String id, final StyledFeedbackPanel feedbackPanel, final Long userId, boolean edit) {
        super(id);

        this.userId = userId;
        if (userId != null) {
            // detach entity to avoid automatic update of changes in form.
            profileUser = userService.getById(userId);
        }
        if (profileUser == null) {
            throw new RestartResponseException(CampaignsPage.class);
        }

        this.feedbackPanel = feedbackPanel;
        this.originEdit = edit;
        // initially form is disabled
        formEnabled = edit;

        // add form
        form = new InputValidationForm<User>("form") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(formEnabled);
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

        // add text area input for user condition
        conditionTextAreaDisabled = newConditionTextArea(true);
        conditionTextArea = newConditionTextArea(false);
        if (formEnabled) {
            form.add(conditionTextArea);
        } else {
            form.add(conditionTextAreaDisabled);
        }

        final WebMarkupContainer buttonPanel = new WebMarkupContainer("buttonPanel") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                // only show if form is enabled
                setVisible(formEnabled);
            }
        };
        form.add(buttonPanel.setOutputMarkupPlaceholderTag(true));

        // add a sumbit link
        buttonPanel.add(new AjaxSubmitLink("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                updateUser(target);
                logger.info("The user " + loggedInUser.getNickName() + " updated the user profile.");
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                showError(target);
            }
        });

        // add a cancel link
        buttonPanel.add(new AjaxLink<User>("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                cancelUpdate(target);
            }
        });

        // add a cancel link
        buttonPanel.add(new AjaxLink<User>("back") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.appendJavaScript("window.history.back()");
            }

            @Override
            protected void onConfigure() {
                if (originEdit) {
                    setVisible(true);
                } else {
                    setVisible(false);
                }
                super.onConfigure();
            }
        });

        // add link to edit the form
        editLink = new AjaxLink<String>("editLink") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (loggedInUser.getId().equals(userId) || loggedInUser.hasAnyRoles(UserRole.ADMIN, UserRole.SUPER_ADMIN)) {
                    setVisible(!formEnabled);
                } else {
                    setVisible(false);
                }
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                enableForm(target);
            }
        };
        add(editLink.setOutputMarkupPlaceholderTag(true));

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

    /**
     * @param target
     */
    protected abstract void onAfterSubmit(AjaxRequestTarget target);

    /**
     * @param target
     */
    protected abstract void onAfterCancel(AjaxRequestTarget target);

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
                setVisible(formEnabled && profileUser != null && profileUser.getProfilePictureFile() != null);
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
        // disable form
        disableForm(target);

        onAfterSubmit(target);
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
    private void cancelUpdate(AjaxRequestTarget target) {
        // fire event to tell LoggedInUserProvider to reload user from database
        userEventSrc.fire(profileUser);
        // re-render feedback panel to clear existing messages
        target.add(feedbackPanel);
        // clear form
        if (userId != null) {
            profileUser = userService.getById(userId);
        }
        clearForm();
        // disable form
        disableForm(target);

        onAfterCancel(target);
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
        // enable form
        formEnabled = true;
        // add editable textarea for condition
        conditionTextAreaDisabled.replaceWith(conditionTextArea);
        // re-render form and editLink
        target.add(form);
        target.add(editLink);
        target.add(clearPictureLink);
    }

    /**
     * @param target
     */
    private void disableForm(AjaxRequestTarget target) {
        // disable form
        formEnabled = false;
        // add non-editable textarea for condition
        conditionTextArea.replaceWith(conditionTextAreaDisabled);
        // re-render form and editLink
        target.add(form);
        target.add(editLink);
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
