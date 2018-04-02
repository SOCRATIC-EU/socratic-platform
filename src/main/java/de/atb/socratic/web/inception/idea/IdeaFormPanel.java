/**
 *
 */
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import com.vaynberg.wicket.select2.Select2MultiChoice;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.CampaignType;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.inception.ScopeService;
import de.atb.socratic.service.other.TagService;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.TinyMCETextArea;
import de.atb.socratic.web.provider.TagProvider;
import de.atb.socratic.web.provider.UserChoiceProvider;
import de.atb.socratic.web.qualifier.AllUsers;
import de.atb.socratic.web.qualifier.FileUploadCache;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.upload.FileUploadInfo;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.lang.Bytes;
import org.jboss.solder.logging.Logger;
import wicket.contrib.tinymce4.ajax.TinyMceAjaxSubmitLink;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class IdeaFormPanel extends GenericPanel<Campaign> {

    private static final long serialVersionUID = -1019606983552845317L;

    // inject a logger
    @Inject
    Logger logger;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    @Inject
    TagService tagService;

    @Inject
    ScopeService scopeService;

    @Inject
    @AllUsers
    List<User> allUsers;

    @Inject
    @FileUploadCache
    FileUploadInfo fileUploadInfo;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    @EJB
    IdeaService ideaService;

    // Form to create new idea
    private final Form<Void> ideaForm;

    private final TextField<String> shortText;

    private final TextArea<String> description;

    private final Select2MultiChoice<User> collaborators;

    private Select2MultiChoice<Tag> tags;

    private final FileUploadField fileUpload;

    private final HiddenField<String> fileUploadHiddenField;
    private final WebMarkupContainer fileTableBody, startVotingEntreIdea;

    private boolean fileUploadVisible, startVoting = false;

    private final OnEventInputBeanValidationBorder<String> shortTextValidationBorder;

    private final OnEventInputBeanValidationBorder<String> descriptionValidationBorder;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    // the new idea to add
    private Idea theIdea;

    /**
     * @param id
     * @param model
     * @param idea
     * @param feedbackPanel
     */
    public IdeaFormPanel(final String id, final IModel<Campaign> model, final Idea idea,
                         final StyledFeedbackPanel feedbackPanel) {
        super(id, model);

        if (idea == null) {
            theIdea = new Idea();
        } else {
            theIdea = idea;
        }

        this.feedbackPanel = feedbackPanel;

        // add form to create a new idea
        ideaForm = new InputValidationForm<Void>("ideaForm") {
            private static final long serialVersionUID = -5239179051581887072L;

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);

                // toggle fileUpload div collapse
                response.render(OnDomReadyHeaderItem.forScript(getToggleFileUploadDivScript()));

                // add javascript to load jquery file upload plugin
                response.render(OnDomReadyHeaderItem.forScript(getFileUploadScript()));
            }
        };
        add(ideaForm.setOutputMarkupId(true));

        // add header
        String label = "";
        if ((theIdea.getId() == null) && (model.getObject().getCampaignType() == CampaignType.FREE_FORM)) {
            label = "post.entrepreneurial.header";
        } else if (theIdea.getId() == null) {
            label = "post.header";
        } else {
            label = "edit.header";
        }
        ideaForm.add(new Label("formHeader", new StringResourceModel(label, this, null)));

        // add text field for idea's text inside a border component that
        // performs bean validation
        shortText = new TextField<>("shortText", new PropertyModel<String>(theIdea, "shortText"));
        shortTextValidationBorder = new OnEventInputBeanValidationBorder<>("shortTextValidationBorder", shortText, HtmlEvent.ONBLUR);
        ideaForm.add(shortTextValidationBorder);

        description = new TinyMCETextArea("description", new PropertyModel<String>(theIdea, "description"));
        description.setOutputMarkupId(true);
        descriptionValidationBorder = new OnEventInputBeanValidationBorder<>("descriptionValidationBorder", description, HtmlEvent.ONBLUR);
        ideaForm.add(descriptionValidationBorder);

        ideaForm.add(new AjaxLink<Void>("showAttachmentsForm") {
            private static final long serialVersionUID = 2414445280074210649L;

            // add a link to display file upload form
            @Override
            public void onClick(AjaxRequestTarget target) {
                toggleAttachmentsForm(target);
                fileUploadVisible = !fileUploadVisible;
            }
        });

        // add text input field for tags
        ideaForm.add(tags = newTagsSelect2MultiChoice());

        // add multi select input field for collaborators
        collaborators = new Select2MultiChoice<>(
                "collaborators",
                new PropertyModel<Collection<User>>(theIdea, "collaborators"),
                new UserChoiceProvider(allUsers, loggedInUser));
        collaborators.getSettings().setMinimumInputLength(1);
        ideaForm.add(collaborators);

        // for file uploads
        fileTableBody = new WebMarkupContainer("fileTableBody");
        ideaForm.add(fileTableBody.setOutputMarkupId(true));
        fileUploadHiddenField = new HiddenField<>("uploadCacheId", new PropertyModel<String>(theIdea, "attachmentsCacheId"));
        ideaForm.add(fileUploadHiddenField.setOutputMarkupId(true));
        ideaForm.setMaxSize(Bytes.megabytes(10));
        ideaForm.setMultiPart(true);
        fileUpload = new FileUploadField("fileupload");
        ideaForm.add(fileUpload.setOutputMarkupId(true));

        // fill the fileUploadInfo cache with any existing attachments
        updateAttachmentsCache();

        // start prioritization?
        startVotingEntreIdea = new WebMarkupContainer("startVotingEntreIdea") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getModel().getObject().getCampaignType() == CampaignType.FREE_FORM);
            }
        };
        startVotingEntreIdea.setOutputMarkupId(true);
        startVotingEntreIdea.add(new CheckBox("startwithprio", new PropertyModel<Boolean>(this, "startVoting")).setRequired(false));
        ideaForm.add(startVotingEntreIdea);

        // add submit link
        ideaForm.add(new TinyMceAjaxSubmitLink("submit", ideaForm) {
            private static final long serialVersionUID = -5554788824019768407L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                add(new AttributeModifier("value",
                        new StringResourceModel((theIdea.getId() == null)
                                ? "submit.text"
                                : "edit.text", IdeaFormPanel.this, null)));
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                submitIdea(target);
                logger.info("The user " + loggedInUser.getNickName() + " submitted an idea to the challenge " + model.getObject().getName());
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                showErrors(target);
            }
        });

        // add a cancel link
        ideaForm.add(new AjaxLink<Void>("cancel") {
            private static final long serialVersionUID = 7283063710515765932L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                cancel(target);
            }
        });
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
        theIdea = ideaService.detach(theIdea);
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
     * Override this to do ajax updates after idea creation has been cancelled.
     *
     * @param target
     */
    protected void onAfterCreateCancel(AjaxRequestTarget target) {
        // empty by default. implement this!
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
     * Override this to do ajax updates after idea update has been cancelled.
     *
     * @param target
     * @param idea
     * @param component
     */
    protected void onAfterUpdateCancelled(AjaxRequestTarget target, Idea idea, Component component) {
        // empty by default. implement this!
    }

    /**
     * @return
     */
    private Select2MultiChoice<Tag> newTagsSelect2MultiChoice() {
        Select2MultiChoice<Tag> select2MultiChoice = new Select2MultiChoice<>(
                "tags",
                new PropertyModel<Collection<Tag>>(theIdea, "tags"),
                new TagProvider(tagService.getAll()));
        select2MultiChoice.getSettings().setTokenSeparators(new String[]{","});
        select2MultiChoice.getSettings().setMinimumInputLength(1);
        select2MultiChoice.getSettings().setCreateSearchChoice(
                "function(term) { if (term.length > 1) { return { id: term, text: term }; } }");
        return select2MultiChoice;
    }

    /**
     * @return
     */
    private String getFileUploadScript() {
        return String.format(JSTemplates.LOAD_FILE_UPLOAD, JSTemplates.getContextPath(), ideaForm.getMarkupId(),
                fileUploadHiddenField.getMarkupId(), fileTableBody.getMarkupId());
    }

    /**
     * @return
     */
    private String getToggleFileUploadDivScript() {
        if (fileUploadVisible) {
            return String.format(JSTemplates.SHOW_COLLAPSE, ".attachments");
        } else {
            return String.format(JSTemplates.CLOSE_COLLAPSE, ".attachments");
        }
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
            fileUploadInfo.addFileInfo(fileUploadHiddenField.getModelObject(),
                    fileInfos.toArray(new FileInfo[fileInfos.size()]));
        }
    }

    /**
     * @param target
     */
    private void toggleAttachmentsForm(AjaxRequestTarget target) {
        target.appendJavaScript(String.format(JSTemplates.TOGGLE_COLLAPSE, ".attachments"));
    }

    /**
     * @param target
     */
    private void submitIdea(AjaxRequestTarget target) {
        Page page = getPage();
        // check first if file is attached for Entrepreneurial Idea or not
        if (getModel().getObject().getCampaignType() == CampaignType.FREE_FORM) {
            if (fileUploadInfo.getFileInfos(fileUploadHiddenField.getModelObject()).size() == 0) {
                page.error(new StringResourceModel("attach.file.entre.idea", this, null).getString());
                target.add(feedbackPanel);
                onAfterCreateCancel(target);
            } else {
                if (theIdea.getId() == null) {
                    createIdea(target);
                    startVoting(startVoting, page, getModel().getObject());
                } else {
                    updateIdea(target);
                    startVoting(startVoting, page, getModel().getObject());
                }
            }
        } else {
            if (theIdea.getId() == null) {
                createIdea(target);
            } else {
                updateIdea(target);
            }
        }
        tags.setProvider(new TagProvider(tagService.getAll()));
        target.add(tags);

    }

    /**
     * @param start
     */
    private void startVoting(Boolean start, Page page, Campaign campaign) {
        if (start) {
            InnovationStatus componentStatus = InnovationStatus.PRIORITISATION;
            BookmarkablePageLink<?> priolink =
                    componentStatus.getLinkToCorrespondingStage(componentStatus.name().toLowerCase(), campaign, loggedInUser);
            setResponsePage(priolink.getPageClass(), page.getPageParameters());
        }
    }

    /**
     * @param target
     */
    private void createIdea(AjaxRequestTarget target) {
        // and add idea to the campaign
        updateIdeaFromForm();
        theIdea = campaignService.addIdea(getModelObject(), theIdea);
        // reset file uploads
        clearFileUploadCache();
        // show success message
        getPage().success(new StringResourceModel("created.message", this, null).getString());
        // show feedback panel
        target.add(feedbackPanel);
        // prepend the new idea to the list of ideas
        onAfterCreate(target, theIdea);
        // clear form
        resetIdeaForm(target);
    }

    /**
     * @param target
     */
    private void updateIdea(AjaxRequestTarget target) {

        // update idea
        updateIdeaFromForm();
        final Idea idea = ideaService.update(theIdea);

        // notify about updated ideas
        ideaService.notifyIdeaFollowersAboutIdeaUpdates(theIdea);

        // reset file uploads
        clearFileUploadCache();
        // set file upload field to hidden
        fileUploadVisible = false;
        // show feedback panel
        target.add(feedbackPanel);
        // replace form with IdeaPanel
        onAfterUpdate(target, idea, this);
    }

    /**
     *
     */
    private void updateIdeaFromForm() {
        theIdea.setShortText(shortText.getModelObject());
        theIdea.setDescription(description.getModelObject());
        theIdea.setCollaborators((List<User>) collaborators.getModelObject());
        theIdea.setKeywords((List<Tag>) tags.getModelObject());
        theIdea.setAttachments(fileUploadInfo.getFileInfos(fileUploadHiddenField.getModelObject()));
        theIdea.setPostedBy(loggedInUser);
    }

    /**
     * @param target
     */
    private void showErrors(AjaxRequestTarget target) {
        // retain the already entered values
        collaborators.updateModel();
        target.add(shortText);
        target.add(shortTextValidationBorder);
        target.add(descriptionValidationBorder);
        target.add(description);
        target.add(tags);
        target.add(collaborators);
        target.add(fileUpload);
        target.add(fileUploadHiddenField);
        target.add(fileTableBody);
        target.add(ideaForm);
        // also reload feedback panel
        target.add(feedbackPanel);
        // do not hide file upload panel if it was shown before
        if (fileUploadVisible) {
            toggleAttachmentsForm(target);
        }
    }

    /**
     * @param target
     */
    private void cancel(AjaxRequestTarget target) {
        // re-render feedback panel to clear existing messages
        target.add(feedbackPanel);
        // cancel idea processing
        if (theIdea.getId() == null) {
            cancelCreate(target);
        } else {
            cancelUpdate(target);
        }
    }

    /**
     * @param target
     */
    private void cancelCreate(AjaxRequestTarget target) {
        // reset file uploads
        clearFileUploadCache();
        // do other updates
        onAfterCreateCancel(target);
        // clear form
        resetIdeaForm(target);
    }

    /**
     * @param target
     */
    private void cancelUpdate(AjaxRequestTarget target) {
        // reset file uploads
        clearFileUploadCache();
        // set file upload field to hidden
        fileUploadVisible = false;
        // reset to original state
        final Idea idea = ideaService.getById(theIdea.getId());
        // replace form with idea panel
        onAfterUpdateCancelled(target, idea, this);
    }

    /**
     * Clears the file upload cache and deletes all uploaded files.
     */
    private void clearFileUploadCache() {
        fileUploadInfo.cleanup(fileUploadHiddenField.getModelObject());
    }

    /**
     *
     */
    private void resetIdeaForm(AjaxRequestTarget target) {
        // set file upload field to hidden
        fileUploadVisible = false;

        // reset idea object
        theIdea = new Idea();
        shortText.setDefaultModelObject(theIdea.getShortText());
        description.setDefaultModelObject(theIdea.getDescription());
        tags.setDefaultModelObject(theIdea.getKeywords());
        collaborators.setDefaultModelObject(theIdea.getCollaborators());
        fileUploadHiddenField.setDefaultModelObject(theIdea.getAttachmentsCacheId());

        // add
        target.add(shortText);
        target.add(shortTextValidationBorder);
        target.add(descriptionValidationBorder);
        target.add(description);
        target.add(tags);
        target.add(collaborators);
        target.add(fileUpload);
        target.add(fileUploadHiddenField);
        target.add(fileTableBody);
        target.add(ideaForm);
    }
}
