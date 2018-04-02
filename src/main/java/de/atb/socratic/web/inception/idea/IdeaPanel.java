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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import javax.inject.Inject;

import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.web.components.AJAXDownload;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.upload.ThumbnailResource;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class IdeaPanel extends GenericPanel<Idea> implements IVotable {

    /**
     *
     */
    private static final long serialVersionUID = -522989596052194414L;

    @Inject
    Logger logger;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    /**
     * Initializes the IdeaPanel.
     *
     * @param id
     * @param model
     * @param innovationStatus define in which innovation phase this panel is used
     */
    public IdeaPanel(final String id, final IModel<Idea> model, final InnovationStatus innovationStatus) {
        super(id, model);

        setOutputMarkupId(true);

        final Idea idea = getModelObject();

        // add idea data
        WebMarkupContainer headLine = new WebMarkupContainer("headline");
        add(headLine);
        headLine.add(new Label("shortText", new PropertyModel<String>(idea, "shortText")));

        add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.THUMBNAIL, idea.getPostedBy())));
        add(new Label("elevatorPitch", new PropertyModel<String>(idea, "elevatorPitch")));
        add(new Label("postedAt", new PropertyModel<Date>(idea, "postedAt")));
        add(new Label("postedBy.nickName", new PropertyModel<String>(idea, "postedBy.nickName")));
        add(new Label("commentsNumber", idea.getComments().size()));
        add(new Label("thumbsUpVotes", idea.getNoOfUpVotes()));


        // add a dropdown button with edit/delete buttons
        headLine.add(newEditIdeaButton(this.getMarkupId(), idea));
        headLine.add(newDeleteIdeaButton(this.getMarkupId(), idea));


    }

    /**
     * @param target
     * @param component
     */
    protected abstract void editLinkOnClick(AjaxRequestTarget target, Component component);

    /**
     * @param target
     * @param component
     */
    protected abstract void deleteLinkOnClick(AjaxRequestTarget target, Component component);

    /**
     * @return
     */
    protected boolean isEditLinkVisible() {
        return true;
    }

    /**
     * @return
     */
    protected boolean isDeleteLinkVisible() {
        return true;
    }

    /**
     * @return
     */
    private MultiLineLabel newDescription() {
        if ((getModelObject().getDescription() == null) || (getModelObject().getDescription().length() <= 40)) {
            return new MultiLineLabel("description", Model.of(getModelObject().getDescription().replace("<p>", "").replace("</p>", "")));
        } else {
            return new MultiLineLabel("description", Model.of(getModelObject().getDescription().substring(0, 40).replace("<p>", "") + " ..."));
        }
    }

    /**
     * @param idea
     * @param attachmentsDivId
     * @param commentsDivId
     * @return
     */
    private AjaxLink<Void> newShowAttachmentsLink(final Idea idea,
                                                  final String attachmentsDivId, final String commentsDivId) {
        AjaxLink<Void> attachmentsLink = new AjaxLink<Void>("showAttachments") {
            private static final long serialVersionUID = -3293186294600689958L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!idea.getAttachments().isEmpty());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                target.appendJavaScript(String.format(JSTemplates.CLOSE_COLLAPSE, "#" + commentsDivId));
                target.appendJavaScript(String.format(JSTemplates.TOGGLE_COLLAPSE, "#" + attachmentsDivId));
            }
        };
        attachmentsLink.add(new Label("attachmentsNumber", Model.of("(" + idea.getAttachments().size() + ")")));
        return attachmentsLink;
    }

    /**
     * @param attachments
     * @return
     */
    private WebMarkupContainer newAttachmentsDiv(final Set<FileInfo> attachments) {
        WebMarkupContainer attachmentsDiv = new WebMarkupContainer("attachmentsDiv") {
            private static final long serialVersionUID = 9171140743771084791L;

            @Override
            public boolean isVisible() {
                return !attachments.isEmpty();
            }
        };
        attachmentsDiv.setOutputMarkupPlaceholderTag(true);

        attachmentsDiv.add(new ListView<FileInfo>("attachmentLinks", new ArrayList<FileInfo>(attachments)) {
            private static final long serialVersionUID = -5548403993526690094L;

            @Override
            protected void populateItem(final ListItem<FileInfo> item) {
                newDownloadLink(item);
            }
        });

        return attachmentsDiv;
    }

    /**
     * @param item
     */
    private void newDownloadLink(final ListItem<FileInfo> item) {
        final AJAXDownload download = new AJAXDownload() {
            private static final long serialVersionUID = -4040698872921909376L;

            @Override
            protected IResourceStream getResourceStream() {
                final IResourceStream stream = new FileResourceStream(new File(item.getModelObject().getPath()));
                // let stream determine file's content type so that browser
                // plugins may be used if enabled at the client
                String contentType = stream.getContentType();
                logger.info("STREAM CONTENT TYPE IS: " + contentType);
                return stream;
            }

            @Override
            protected String getFileName() {
                return item.getModelObject().getDisplayName();
            }

            @Override
            protected boolean isImageFile() {
                String contentType = item.getModelObject().getContentType();
                return StringUtils.isNotBlank(contentType) && contentType.startsWith("image/");
            }
        };
        item.add(download);
        AjaxLink<Void> link = new AjaxLink<Void>("downloadLink") {
            private static final long serialVersionUID = 8563526985376710277L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                // initiate the download
                download.initiate(target);
            }
        };
        link.add(new NonCachingImage("thumb", new ThumbnailResource(item.getModelObject(), 24)));
        link.add(new Label("downloadLinkLabel", new PropertyModel<String>(item.getModelObject(), "displayName")));
        item.add(link);
    }

    /**
     * @param idea
     * @param commentsDivId
     * @return
     */
    private AjaxLink<Void> newShowCommentsLink(final Idea idea,
                                               final String commentsDivId) {
        AjaxLink<Void> showCommentsLink = new AjaxLink<Void>("showComments") {
            private static final long serialVersionUID = 202363713959040288L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (!IdeaPanel.this.getModelObject().getCampaign().getActive() && idea.getComments().isEmpty()) {
                    setEnabled(false);
                    add(new AttributeModifier("class", ""));
                }
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                target.appendJavaScript(String.format(JSTemplates.TOGGLE_COLLAPSE, "#" + commentsDivId));
            }
        };
        // don't add <em> tags when setting to disabled
        showCommentsLink.setBeforeDisabledLink("");
        showCommentsLink.setAfterDisabledLink("");
        return showCommentsLink;
    }

    /**
     * @param itemId
     * @param idea
     * @return
     */
    private AjaxLink<String> newEditIdeaButton(final String itemId, final Idea idea) {

        AjaxLink<String> editLink = new AjaxLink<String>("editLink") {
            private static final long serialVersionUID = 647877101758774046L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(isEditLinkVisible());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                editLinkOnClick(target, IdeaPanel.this);
            }
        };

        return editLink;
    }

    /**
     * @param itemId
     * @param idea
     * @return
     */
    private AjaxLink<String> newDeleteIdeaButton(final String itemId, final Idea idea) {
        AjaxLink<String> deleteLink = new AjaxLink<String>("deleteLink") {
            private static final long serialVersionUID = 2699270987811534432L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(isDeleteLinkVisible());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                deleteLinkOnClick(target, IdeaPanel.this);
            }
        };

        return deleteLink;
    }

}
