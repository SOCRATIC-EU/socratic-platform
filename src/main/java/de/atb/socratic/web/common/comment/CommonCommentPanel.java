package de.atb.socratic.web.common.comment;

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

import de.atb.socratic.model.Comment;
import de.atb.socratic.model.User;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * Created by Spindler on 24.10.2016.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class CommonCommentPanel extends GenericPanel<Comment> {

    private static final long serialVersionUID = 5866012243629955396L;

    @Inject
    @LoggedInUser
    private User loggedInUser;

    private final Comment comment;

    public CommonCommentPanel(final String id, final IModel<Comment> model) {
        super(id, model);

        setOutputMarkupId(true);

        comment = getModelObject();
        add(new Label("comment.text", new PropertyModel<String>(comment,
                "commentText")).setOutputMarkupId(true).setEscapeModelStrings(false));
        add(new Label("comment.postedAt", new PropertyModel<Date>(comment,
                "postedAt")).setOutputMarkupId(true));
        // comment owner
        add(newProfilePicture(comment.getPostedBy()));
        add(new Label("owner", comment.getPostedBy().getNickName()));

        add(newEditLink());
        add(newDeleteLink());
    }

    private NonCachingImage newProfilePicture(User user) {
        NonCachingImage picture = new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, user));
        picture.setOutputMarkupId(true);
        return picture;
    }

    /**
     * @param target
     * @param component
     */
    protected abstract void editLinkOnClick(AjaxRequestTarget target, Component component);

    /**
     * @param target
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
    private AbstractLink newEditLink() {
        AjaxLink<String> link = new AjaxLink<String>("editCommentLink") {
            private static final long serialVersionUID = -2590603199147884645L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(isEditLinkVisible());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                editLinkOnClick(target, CommonCommentPanel.this);
            }
        };
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
                setVisible(isDeleteLinkVisible());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                deleteLinkOnClick(target, CommonCommentPanel.this);
            }
        };
        return link;
    }

}
