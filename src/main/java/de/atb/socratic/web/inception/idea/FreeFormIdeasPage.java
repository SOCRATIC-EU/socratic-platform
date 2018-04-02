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

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.components.FileUploadBehavior;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.inception.CampaignsPage;
import de.atb.socratic.web.inception.idea.details.IdeaDetailsPage;
import de.atb.socratic.web.qualifier.FileUploadCache;
import de.atb.socratic.web.selection.IdeaVotingPanel;
import de.atb.socratic.web.upload.FileUploadInfo;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class FreeFormIdeasPage extends BasePage {

    /**
     *
     */
    private static final long serialVersionUID = -7028070163358260072L;

    @Inject
    @FileUploadCache
    FileUploadInfo fileUploadInfo;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    // the current campaign
    private final Campaign campaign;

    private Fragment ideaFormFragment;

    // Shows number of posted ideas
    private final Label noOfIdeas;

    @SuppressWarnings("unused")
    private Long ideaListSize;

    // panel for displaying list of existing ideas
    private final IdeaListPanel ideaListPanel;

    /**
     * Constructor building the page
     *
     * @param parameters
     */
    public FreeFormIdeasPage(final PageParameters parameters) {

        super(parameters);

        // add js and css for file upload
        add(new FileUploadBehavior());

        // set the campaign we got from previous page
        try {
            campaign = campaignService.getFreeFormCampaign();
        } catch (Exception e) {
            if ((e.getCause() != null)
                    && (e.getCause() instanceof NoResultException)) {
                // if there is no FreeFormCampaign yet, redirect to
                // CampaignsPage
                throw new RestartResponseException(CampaignsPage.class);
            } else {
                // in case of "real" error redirect to ErroPage
                throw new RestartResponseException(ErrorPage.class);
            }
        }

        // add link to campaigns page
        add(new BookmarkablePageLink<CampaignsPage>("campaignsPageLink",
                CampaignsPage.class));

        // add panel with form to add new ideas
        add(ideaFormFragment = newIdeaFormFragment());

        // add panel with list of existing ideas
        ideaListPanel = new IdeaListPanel("ideaList", Model.of(campaign),
                feedbackPanel, true, IdeaDetailsPage.class) {
            private static final long serialVersionUID = -759598947330229477L;

            @Override
            protected void onAfterDelete(AjaxRequestTarget target) {
                // update the # of existing ideas
                updateNoOfIdeas(target);
            }

            @Override
            public void voteUpOnClick(AjaxRequestTarget target, Component component) {
            }

            @Override
            public void voteDownOnClick(AjaxRequestTarget target, Component component) {
            }

            @Override
            protected IdeaVotingPanel newIdeaVotingPanel(String id, MarkupContainer item, Idea idea) {
                return new IdeaVotingPanel(id, Model.of(idea), false) {
                    private static final long serialVersionUID = 7879132965391367302L;

                    protected void onConfigure() {
                        setVisible(false);
                    }

                    @Override
                    protected void onVotingPerformed(AjaxRequestTarget target) {
                    }
                };
            }
        };
        add(ideaListPanel.setOutputMarkupId(true));

        // add label with campaign name
        add(new Label("campaign.name", new PropertyModel<String>(campaign,
                "name")));

        // display # of ideas in list
        ideaListSize = ideaListPanel.getIdeaListSize();
        noOfIdeas = new Label("noOfIdeas", new PropertyModel<Integer>(this,
                "ideaListSize"));
        add(noOfIdeas.setOutputMarkupId(true));
    }

    /**
     * @return
     */
    private Fragment newIdeaFormFragment() {
        if (campaign.getActive()) {
            final Fragment ideaFormFragment = new Fragment("ideaForm",
                    "ideaFormFragment", this);
            // add panel with form to add new ideas
            ideaFormFragment.add(newIdeaFormPanel("ideaFormPanel"));

            // add link to show idea form
            ideaFormFragment.add(newIdeaFormLink());

            ideaFormFragment.setOutputMarkupId(true);
            return ideaFormFragment;
        } else {
            return new Fragment("ideaForm", "emptyFragment", this);
        }
    }

    /**
     * @param id
     * @return
     */
    private IdeaFormPanel newIdeaFormPanel(final String id) {
        return new IdeaFormPanel(id, Model.of(campaign), null, feedbackPanel) {
            private static final long serialVersionUID = 2906932451808310845L;

            @Override
            protected void onAfterCreate(AjaxRequestTarget target, Idea newIdea) {
                // hide form
                toggleForm(target);

                // prepend the new idea to the list of ideas
                ideaListPanel.prependIdeaToList(target, newIdea);
                // update the # of existing ideas
                updateNoOfIdeas(target);
            }

            @Override
            protected void onAfterCreateCancel(AjaxRequestTarget target) {
                // hide form
                toggleForm(target);
            }
        };
    }

    /**
     * @return
     */
    private AjaxLink<Void> newIdeaFormLink() {
        AjaxLink<Void> showIdeaFormLink = new AjaxLink<Void>("showIdeaFormLink") {
            private static final long serialVersionUID = 7636319330765712335L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                toggleForm(target);
            }
        };
        return showIdeaFormLink;
    }

    /**
     * @param target
     */
    private void toggleForm(AjaxRequestTarget target) {
        target.appendJavaScript(String.format(JSTemplates.TOGGLE_COLLAPSE,
                ".idea-form"));
    }

    /**
     * @param target
     */
    private void updateNoOfIdeas(AjaxRequestTarget target) {
        // update the # of existing ideas
        ideaListSize = ideaListPanel.getIdeaListSize();
        target.add(noOfIdeas);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.Component#onBeforeRender()
     */
    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        // reset ideaFormFragment if we are returning to this page via browser
        // back/forward button
        if (ideaFormFragment.hasBeenRendered()) {
            ideaFormFragment = newIdeaFormFragment();
            replace(ideaFormFragment);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.eff.web.BasePage#getPageTitleModel()
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }

}
