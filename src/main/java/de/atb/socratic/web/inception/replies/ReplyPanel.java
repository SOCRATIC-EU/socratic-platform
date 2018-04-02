/**
 *
 */
package de.atb.socratic.web.inception.replies;

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

import de.atb.socratic.authorization.strategies.metadata.EffMetaDataRoleAuthorizationStrategy;
import de.atb.socratic.model.Reply;
import de.atb.socratic.model.UserRole;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * @author ATB
 */
public abstract class ReplyPanel extends GenericPanel<Reply> {


    private static final long serialVersionUID = -4066337869848946854L;

    private final Reply reply;
    private String ideaOrCampaign = "";

    public ReplyPanel(final String id, final IModel<Reply> model, String ideaOrCampaign) {
        super(id, model);

        this.ideaOrCampaign = ideaOrCampaign;
        setOutputMarkupId(true);

        reply = getModelObject();

        add(new Label("reply.text", new PropertyModel<String>(reply,
                "replyText")).setOutputMarkupId(true).setEscapeModelStrings(false));
        add(new Label("reply.postedAt", new PropertyModel<Date>(reply,
                "postedAt")).setOutputMarkupId(true));
        add(new Label("reply.postedBy.nickName", new PropertyModel<String>(
                reply, "postedBy.nickName")).setOutputMarkupId(true));
        add(newEditLink());
        add(newDeleteLink());
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
        AjaxLink<String> link = new AjaxLink<String>("editReplyLink") {
            private static final long serialVersionUID = -2590603199147884645L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                editLinkOnClick(target, ReplyPanel.this);
            }
        };
        if (isEditLinkVisible()) {
            EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.USER);
        }
        EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.MANAGER);
        EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.ADMIN, UserRole.SUPER_ADMIN);
        return link;
    }

    /**
     * @return
     */
    private AbstractLink newDeleteLink() {
        AjaxLink<String> link = new AjaxLink<String>("deleteReplyLink") {
            private static final long serialVersionUID = -6256868464388132947L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                deleteLinkOnClick(target, ReplyPanel.this);
            }
        };

        if (isDeleteLinkVisible()) {
            EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.USER);
        }
        EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.MANAGER);
        EffMetaDataRoleAuthorizationStrategy.authorize(link, RENDER, UserRole.ADMIN, UserRole.SUPER_ADMIN);
        return link;
    }

}
