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

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.model.Comment;
import de.atb.socratic.model.Reply;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.web.components.Effects;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ReplyListPanel extends GenericPanel<Comment> {

    private static final long serialVersionUID = 3151720232594558972L;

    protected boolean allowReplying = true;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    IdeaService ideaService;

    private final DataView<Reply> repliesRepeater;
    private Label repliesNumber;
    private final WebMarkupContainer repliesContainer;
    private Comment comment;
    private String ideaOrCampaign = "";

    public ReplyListPanel(final String id, final IModel<Comment> model, final boolean allowReplying, String ideaOrCampaign) {
        super(id, model);
        this.setOutputMarkupId(true);
        this.allowReplying = allowReplying;
        this.ideaOrCampaign = ideaOrCampaign;

        this.comment = model.getObject();
        this.repliesNumber = new Label("replies", new StringResourceModel("replies.text", null, comment.getRepliesCount()));
        this.repliesNumber.setOutputMarkupId(true);

        repliesRepeater = newRepliesRepeater();

        // add list of replies
        repliesContainer = newRepliesContainer(repliesRepeater);
        add(repliesContainer);
        // add a form to post a new reply
        add(newReplyFormFragment());
    }

    /**
     * Builds a component to display a reply and appends it to the list of
     * replies.
     *
     * @param target
     * @param reply
     */
    protected void appendReplyToList(AjaxRequestTarget target, final Reply reply) {

        // build new item for reply
        Component item = buildItem(reply);

        // append reply to list
        Effects.appendAndFadeIn(target, item, "div", repliesContainer.getMarkupId());
    }

    /**
     * @return
     */
    private Fragment newReplyFormFragment() {
        if (!this.allowReplying) {
            return new Fragment("replyForm", "emptyFragment", this);
        }
        final Fragment commentFormFragment = new Fragment("replyForm", "replyFormFragment", this);
        commentFormFragment.add(newReplyFormPanel("replyFormPanel", getModel(), null, ideaOrCampaign));
        commentFormFragment.setOutputMarkupId(true);
        return commentFormFragment;
    }

    /**
     * @param id
     * @param model
     * @return
     */
    private ReplyFormPanel newReplyFormPanel(final String id, final IModel<Comment> model, final Reply reply, final String ideaOrCampaign) {
        return new ReplyFormPanel(id, model, reply, ideaOrCampaign) {
            private static final long serialVersionUID = 6288306520583049323L;

            @Override
            protected void onAfterCreate(AjaxRequestTarget target, Reply reply) {
                appendReplyToList(target, reply);
                target.add(repliesNumber.setDefaultModel(new StringResourceModel("replies.text", this, null, comment.getRepliesCount())));
            }

            @Override
            protected void onAfterUpdate(AjaxRequestTarget target, Reply reply, Component component) {
                replaceFormWithPanel(target, reply, component);
            }

            @Override
            protected void onAfterUpdateCancelled(AjaxRequestTarget target, Reply reply, Component component) {
                replaceFormWithPanel(target, reply, component);
            }
        };
    }

    /**
     * @param target
     * @param reply
     * @param component
     */
    private void replaceFormWithPanel(AjaxRequestTarget target, Reply reply, Component component) {
        component = component.replaceWith(newReplyPanel(component.getId(), reply, ideaOrCampaign));
        Effects.replaceWithFading(target, component);
    }

    /**
     * @param repliesRepeater
     * @return
     */
    private WebMarkupContainer newRepliesContainer(final DataView<Reply> repliesRepeater) {
        // the list of existing replies
        final WebMarkupContainer repliesContainer = new WebMarkupContainer("repliesContainer");
        repliesContainer.add(repliesRepeater);
        repliesContainer.setOutputMarkupPlaceholderTag(true);
        return repliesContainer;
    }

    /**
     * @return
     */
    private DataView<Reply> newRepliesRepeater() {
        return new DataView<Reply>("replies", new ListDataProvider<Reply>(comment.getReplies())) {

            private static final long serialVersionUID = 4262463875665272868L;

            @Override
            protected void populateItem(final Item<Reply> item) {
                ReplyListPanel.this.populateItem(item, item.getModelObject());
            }
        };
    }

    /**
     * @param item
     * @param reply
     */
    private void populateItem(final WebMarkupContainer item, final Reply reply) {
        item.add(newReplyPanel("replyPanel", reply, ideaOrCampaign));
    }

    /**
     * @param id
     * @param reply
     * @return
     */
    private ReplyPanel newReplyPanel(final String id, final Reply reply, final String ideaOrCampaign) {
        return new ReplyPanel(id, Model.of(reply), ideaOrCampaign) {
            private static final long serialVersionUID = 3807070161673518305L;

            @Override
            protected boolean isEditLinkVisible() {
                return ReplyListPanel.this.isEditLinkVisible(reply);
            }

            @Override
            protected boolean isDeleteLinkVisible() {
                return ReplyListPanel.this.isDeleteLinkVisible(reply);
            }

            @Override
            protected void editLinkOnClick(AjaxRequestTarget target, Component component) {
                component = component.replaceWith(newReplyFormPanel(component.getId(), ReplyListPanel.this.getModel(), reply, ideaOrCampaign));
                Effects.replaceWithFading(target, component);
            }

            @Override
            protected void deleteLinkOnClick(AjaxRequestTarget target, Component component) {
                deleteReply(reply);
                Effects.fadeOutAndRemove(target, component.getMarkupId());
                target.add(repliesNumber.setDefaultModelObject(comment.getRepliesCount()));
            }
        };
    }

    /**
     * @param reply
     * @return
     */
    private boolean isEditLinkVisible(final Reply reply) {
        return loggedInUser.getId().equals(reply.getPostedBy().getId());

    }

    /**
     * @param reply
     * @return
     */
    private boolean isDeleteLinkVisible(final Reply reply) {
        return loggedInUser.getId().equals(reply.getPostedBy().getId());

    }

    /**
     * @param reply
     * @return
     */
    private Component buildItem(final Reply reply) {
        WebMarkupContainer item = new WebMarkupContainer(repliesRepeater.newChildId());
        item.setOutputMarkupPlaceholderTag(true);
        repliesRepeater.add(item);
        populateItem(item, reply);
        return item;
    }

    /**
     * @param reply
     */
    private void deleteReply(final Reply reply) {
        ideaService.removeReply(comment.getId(), reply);
    }

    public Label getRepliesNumber() {
        return repliesNumber;
    }

}
