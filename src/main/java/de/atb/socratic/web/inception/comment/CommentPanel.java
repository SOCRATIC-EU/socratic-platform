/**
 *
 */
package de.atb.socratic.web.inception.comment;

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

import javax.inject.Inject;

import de.atb.socratic.authorization.strategies.metadata.EffMetaDataRoleAuthorizationStrategy;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.util.authorization.UserIsManagerCondition;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.inception.replies.ReplyListPanel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class CommentPanel extends GenericPanel<Comment> {

    private static final long serialVersionUID = -4066337869848946854L;

    @Inject
    IdeaService ideaService;

    //private ReplyListPanel replyPanel;
    private final Comment comment;
    private final Idea idea;
    private boolean allowCommenting;

    public CommentPanel(final String id, final IModel<Comment> model, boolean allowCommenting) {
        super(id, model);

        setOutputMarkupId(true);

        comment = getModelObject();
        idea = ideaService.getIdeaFromComment(comment);
        this.allowCommenting = allowCommenting;
        add(new Label("comment.text", new PropertyModel<String>(comment,
                "commentText")).setOutputMarkupId(true).setEscapeModelStrings(false));
        add(new Label("comment.postedAt", new PropertyModel<Date>(comment,
                "postedAt")).setOutputMarkupId(true));
        add(newEditLink());
        add(newDeleteLink());

        //challenge owner
        add(newProfilePicture(comment.getPostedBy()));
        add(new Label("owner", comment.getPostedBy().getNickName()));
    }

    private NonCachingImage newProfilePicture(User user) {
        NonCachingImage picture = new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, user));
        picture.setOutputMarkupId(true);
        return picture;
    }


    private ReplyListPanel newReplyListPanel(final Comment comment) {
        return new ReplyListPanel("replyListPanel", Model.of(comment), allowCommenting, "idea");
    }

    private AjaxLink<Void> newShowReplies(final String repliesDivId) {

        AjaxLink<Void> showRepliesLink = new AjaxLink<Void>("showReplies") {

            private static final long serialVersionUID = 202363713959040288L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                target.appendJavaScript(String.format(JSTemplates.TOGGLE_COLLAPSE, "#" + repliesDivId));
            }
        };
        // don't add <em> tags when setting to disabled
        showRepliesLink.setBeforeDisabledLink("");
        showRepliesLink.setAfterDisabledLink("");
        return showRepliesLink;
    }

    /**
     * @param target
     * @param component
     */
    protected abstract void editLinkOnClick(AjaxRequestTarget target,
                                            Component component);

    /**
     * @param target
     */
    protected abstract void deleteLinkOnClick(AjaxRequestTarget target,
                                              Component component);

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
    private AbstractLink newEditLink() {
        AjaxLink<String> link = new AjaxLink<String>("editCommentLink") {
            private static final long serialVersionUID = -2590603199147884645L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                editLinkOnClick(target, CommentPanel.this);
            }
        };
        if (isEditLinkVisible()) {
            EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.USER);
        }
        if (idea != null) {
            EffMetaDataRoleAuthorizationStrategy.authorizeIf(UserIsManagerCondition.get(idea.getCampaign().getCompany()), link, RENDER, UserRole.MANAGER);
            EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.ADMIN, UserRole.SUPER_ADMIN);
        }
        return link;
    }

    /**
     * @return
     */
    private AbstractLink newDeleteLink() {
        AjaxLink<String> link = new AjaxLink<String>("deleteCommentLink") {
            private static final long serialVersionUID = -6256868464388132947L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                deleteLinkOnClick(target, CommentPanel.this);
            }
        };

        if (isDeleteLinkVisible()) {
            EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.USER);
        }
        if (idea != null) {
            EffMetaDataRoleAuthorizationStrategy.authorizeIf(UserIsManagerCondition.get(idea.getCampaign().getCompany()), link, RENDER, UserRole.MANAGER);
            EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.ADMIN, UserRole.SUPER_ADMIN);
        }
        return link;
    }

}
