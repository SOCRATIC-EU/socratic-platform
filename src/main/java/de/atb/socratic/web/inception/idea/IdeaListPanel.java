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

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonType;
import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.web.components.Effects;
import de.atb.socratic.web.components.ModalActionButton;
import de.atb.socratic.web.components.NotificationModal;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.IdeaPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.inception.idea.details.IdeaDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.selection.IdeaSelectionDetailsPage;
import de.atb.socratic.web.selection.IdeaVotingPanel;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class IdeaListPanel extends GenericPanel<Campaign> implements IVotable {

    /**
     *
     */
    private static final long serialVersionUID = -159659429992094182L;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many ideas do we show initially
    private static final int itemsPerPage = 6;

    // container holding the list of ideas
    private final WebMarkupContainer ideasContainer;

    // Repeating view showing the list of existing ideas
    private final DataView<Idea> ideasRepeater;

    private final EntityProvider<Idea> ideaProvider;

    private final Modal deleteConfirmationModal;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private Date lastDate = new Date();

    private Idea ideaToHandle;

    private String itemIdToHandle;

    private NonCachingImage ideaProfilePicture;

    private IdeaSortingCriteria sortingCriteria;

    /**
     * Indicates whether the panel contains a form to post new ideas. In Inception phase, this shall be set to true, in
     * Prioritisation to false.
     */
    protected boolean allowIdeaCreation = true;

    public IdeaListPanel(final String id, final IModel<Campaign> model,
                         final StyledFeedbackPanel feedbackPanel, final boolean allowIdeaCreation, Class<?> theClass) {
        this(id, model, null, feedbackPanel, allowIdeaCreation, theClass);
    }

    public IdeaListPanel(final String id, final IModel<Campaign> model,
                         final Comparator<Idea> comparator,
                         final StyledFeedbackPanel feedbackPanel, final boolean allowIdeaCreation, final Class<?> theClass) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;

        this.allowIdeaCreation = allowIdeaCreation;

        // get the campaigns ideas
        final Campaign campaign = getModelObject();

        // add container with list of existing ideas
        ideasContainer = new WebMarkupContainer("ideasContainer");
        add(ideasContainer.setOutputMarkupId(true));

        // add repeating view with list of existing ideas
        ideaProvider = new IdeaProvider(campaign);
        ideasRepeater = new DataView<Idea>("ideasRepeater", ideaProvider,
                itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Idea> item) {
                item.setOutputMarkupId(true);
                IdeaListPanel.this.populateItem(item, item.getModelObject(), theClass);
            }
        };
        ideasContainer.add(ideasRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", ideasRepeater) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(ideasRepeater.getPageCount() > 1);
            }
        });

        // add confirmation modal for deleting ideas
        add(deleteConfirmationModal = newDeleteConfirmationModal());
    }


    /**
     * Builds a component to display an idea and prepends it to the list of
     * ideas.
     *
     * @param target
     * @param idea
     */
    protected void prependIdeaToList(AjaxRequestTarget target, Idea idea) {

        // build new item for idea
        Component item = buildItem(idea);

        // prepend idea to list
        Effects.prependAndFadeIn(target, item, "li", ideasContainer.getMarkupId());
    }

    /**
     * @return
     */
    public long getIdeaListSize() {
        return ideaProvider.size();
    }

    public Component newIdeaAnchor(final Long id) {
        return new WebMarkupContainer("ideaAnchorMan")
                .setMarkupId(IdeasPage.IDEA_ANCHOR_PREFIX + id)
                .setOutputMarkupId(true);
    }


    /**
     * @param idea
     * @return
     */
    private Component buildItem(final Idea idea) {
        WebMarkupContainer item = new WebMarkupContainer(ideasRepeater.newChildId());
        item.setOutputMarkupPlaceholderTag(true);
        item.setOutputMarkupId(true);

        ideasRepeater.add(item);

        populateItem(item, idea, IdeaDetailsPage.class);

        return item;
    }

    protected void populateItem(final WebMarkupContainer item, final Idea idea, final Class<?> theClass) {
        final PageParameters params = new PageParameters().set("id", idea.getCampaign().getId()).set("ideaId", idea.getId());
        item.setOutputMarkupId(true);
        // add idea detail page link
        item.add(new AjaxEventBehavior("onclick") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                if (IdeaDetailsPage.class.equals(theClass)) {
                    setResponsePage(IdeaDetailsPage.class, params);
                } else {
                    setResponsePage(IdeaSelectionDetailsPage.class, params);
                }
            }
        });
        item.add(newIdeaProfilePicturePreview(idea));
        item.add(newIdeaAnchor(idea.getId()));
        item.add(newIdeaPanel("ideaPanel", item, idea));
        item.add(newIdeaVotingPanel("ideaVotingPanel", item, idea));

        WebMarkupContainer wmc = new WebMarkupContainer("container") {
            private static final long serialVersionUID = -4942153744808959652L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // do not show call to action and explore button when challenge is in selection phase
                if (IdeaSelectionDetailsPage.class.equals(theClass)) {
                    setVisible(false);
                }
            }
        };
        wmc.setOutputMarkupId(true);

        // call to action 
        wmc.add(newHelpText(idea));

        // add explore button
        wmc.add(newButton(idea));
        item.add(wmc);
    }

    private Label newHelpText(final Idea idea) {
        return new Label("helpText", new PropertyModel<String>(idea, "callToAction")) {
            private static final long serialVersionUID = -5698161997030273822L;
        };
    }

    private AjaxLink<?> newButton(final Idea idea) {
        return new AjaxLink<Void>("exploreButton") {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(IdeaDetailsPage.class,
                        new PageParameters().set("id", idea.getCampaign().getId()).set("ideaId", idea.getId()));
            }

            @Override
            protected void onConfigure() {
                this.add(new Label("buttonLabel", new StringResourceModel("ideas.explore.button", this, null).getString()));
                setOutputMarkupId(true);
            }

            ;

        };
    }

    protected abstract IdeaVotingPanel newIdeaVotingPanel(final String id, final MarkupContainer item, final Idea idea);

    protected NonCachingImage newIdeaProfilePicturePreview(Idea idea) {
        ideaProfilePicture = new NonCachingImage("profilePicturePreview", IdeaPictureResource.get(PictureType.PROFILE, idea));
        ideaProfilePicture.setOutputMarkupId(true);
        return ideaProfilePicture;
    }

    /**
     * @param idea
     * @return
     */
    public IdeaPanel newIdeaPanel(final String id, final MarkupContainer item,
                                  final Idea idea) {
        return new IdeaPanel(id, Model.of(idea), InnovationStatus.INCEPTION) {
            private static final long serialVersionUID = -1078593562271992866L;

            @Override
            protected boolean isEditLinkVisible() {
                return IdeaListPanel.this.isEditLinkVisible(idea);
            }

            @Override
            protected boolean isDeleteLinkVisible() {
                return IdeaListPanel.this.isDeleteLinkVisible(idea);
            }

            @Override
            protected void editLinkOnClick(AjaxRequestTarget target, Component component) {
                setResponsePage(
                        IdeaAddEditPage.class,
                        new PageParameters()
                                .set("id", idea.getId())
                                .set("campaignId", idea.getCampaign().getId()));
            }

            @Override
            protected void deleteLinkOnClick(AjaxRequestTarget target, Component component) {
                itemIdToHandle = item.getMarkupId();
                ideaToHandle = idea;
                deleteConfirmationModal.appendShowDialogJavaScript(target);
            }

            @Override
            public void voteUpOnClick(AjaxRequestTarget target, Component component) {
                IdeaListPanel.this.voteUpOnClick(target, IdeaListPanel.this);
            }

            @Override
            public void voteDownOnClick(AjaxRequestTarget target, Component component) {
                IdeaListPanel.this.voteDownOnClick(target, IdeaListPanel.this);
            }

        };
    }

    /**
     * @param idea
     * @return
     */
    private boolean isEditLinkVisible(final Idea idea) {
        return allowIdeaCreation && loggedInUser != null && idea.isEditableBy(loggedInUser);
    }

    /**
     * @param idea
     * @return
     */
    private boolean isDeleteLinkVisible(final Idea idea) {
        return allowIdeaCreation && loggedInUser != null && loggedInUser.equals(idea.getPostedBy());
    }

    /**
     * @return
     */
    private IdeaFormPanel newIdeaFormPanel(final String id, final Idea idea) {
        return new IdeaFormPanel(id, getModel(), idea, feedbackPanel) {

            private static final long serialVersionUID = 2906932451808310845L;


            @Override
            protected void onAfterUpdate(AjaxRequestTarget target, Idea idea,
                                         Component component) {
                replaceFormWithPanel(target, idea, component);
            }

            @Override
            protected void onAfterUpdateCancelled(AjaxRequestTarget target,
                                                  Idea idea, Component component) {
                replaceFormWithPanel(target, idea, component);
            }
        };
    }

    /**
     * @param target
     * @param idea
     * @param component
     */
    private void replaceFormWithPanel(AjaxRequestTarget target, Idea idea, Component component) {
        component = component.replaceWith(newIdeaPanel(component.getId(), component.getParent(), idea));
        Effects.replaceWithSliding(target, component);
        target.add(feedbackPanel);
    }

    /**
     * @param target
     */
    private void deleteIdea(AjaxRequestTarget target) {
        campaignService.removeIdea(IdeaListPanel.this.getModelObject(), ideaToHandle);
        Effects.fadeOutAndRemove(target, itemIdToHandle);
        // update page
        onAfterDelete(target);
    }

    /**
     * @return
     */
    private NotificationModal newDeleteConfirmationModal() {
        final NotificationModal notificationModal = new NotificationModal(
                "deleteConfirmationModal", new StringResourceModel(
                "delete.confirmation.modal.header", this, null),
                new StringResourceModel("delete.confirmation.modal.message",
                        this, null), false);
        notificationModal.addButton(new ModalActionButton(notificationModal,
                ButtonType.Primary, new StringResourceModel(
                "delete.confirmation.modal.submit.text", this, null),
                true) {
            private static final long serialVersionUID = -8579196626175159237L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                // confirmed --> delete
                deleteIdea(target);
                // close modal
                closeDeleteConfirmationModal(notificationModal, target);
            }
        });
        notificationModal.addButton(new ModalActionButton(notificationModal,
                ButtonType.Default, new StringResourceModel(
                "delete.confirmation.modal.cancel.text", this, null),
                true) {
            private static final long serialVersionUID = 8931306355855637710L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                // Cancel clicked --> do nothing, close modal
                closeDeleteConfirmationModal(notificationModal, target);
            }
        });
        return notificationModal;
    }

    /**
     * @param modal
     * @param target
     */
    private void closeDeleteConfirmationModal(final Modal modal,
                                              AjaxRequestTarget target) {
        // reset
        ideaToHandle = null;
        itemIdToHandle = null;
        // close
        modal.appendCloseDialogJavaScript(target);
    }

    /**
     * Overwrite this to update stuff after deleting an idea.
     *
     * @param target
     */
    protected abstract void onAfterDelete(AjaxRequestTarget target);

    /**
     * @param sortingCriteria
     * @return
     */
    public IdeaListPanel setSortingCriteria(IdeaSortingCriteria sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
        return this;
    }

    /**
     * @author ATB
     */
    private final class IdeaProvider extends EntityProvider<Idea> {

        /**
         *
         */
        private static final long serialVersionUID = -1727094205049792307L;

        private final Campaign campaign;

        public IdeaProvider(Campaign campaign) {
            super();
            this.campaign = campaign;
        }

        /**
         * Instead of using index first to define which is the first result, we
         * just get count ideas that have been posted before the postDate of the
         * current last element (idea). If there is no last element, use current
         * date.
         */
        @Override
        public Iterator<? extends Idea> iterator(long first, long count) {
            List<Idea> ideas;
            if (sortingCriteria != null && sortingCriteria.compareTo(IdeaSortingCriteria.Date) != 0) {
                if (sortingCriteria.compareTo(IdeaSortingCriteria.Comments) == 0) {
                    ideas = campaignService.getIdeasForCampaignFromToByDescendingNoOfComments(
                            campaign,
                            Long.valueOf(first).intValue(),
                            Long.valueOf(count).intValue());
                } else {
                    ideas = campaignService.getIdeasForCampaignFromToByDescendingNoOfLikes(
                            campaign,
                            Long.valueOf(first).intValue(),
                            Long.valueOf(count).intValue());
                }
            } else
                ideas = campaignService.getIdeasForCampaignFromToByDescendingPostDate(
                        campaign,
                        Long.valueOf(first).intValue(),
                        Long.valueOf(count).intValue());
            return ideas.iterator();
        }

        @Override
        public long size() {
            return campaignService.countIdeasForCampaign(campaign.getId());
        }

    }

}
