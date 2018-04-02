/**
 *
 */
package de.atb.socratic.web.inception;

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

import javax.ejb.EJB;

import de.atb.socratic.model.AbstractEntity;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.web.components.BlockquotePanel;
import de.atb.socratic.web.components.ExpandableBlockquotePanel;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.inception.idea.IdeasPage;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class SimilarCampaignPanel extends GenericPanel<Campaign> {

    /**
     *
     */
    private static final long serialVersionUID = -8296257440516309824L;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    /**
     * @param id
     * @param model
     */
    public SimilarCampaignPanel(final String id, final IModel<Campaign> model,
                                final int index) {
        super(id, model);

        // show campaign data
        Campaign campaign = getModelObject();
        add(new Label("similarCampaignNo", Model.of(index)));
        add(new Label("name", new PropertyModel<String>(campaign, "name")));
        add(new Label("createdBy.nickName", new PropertyModel<String>(campaign,
                "createdBy.nickName")));
        add(new Label("dueDate", new PropertyModel<Date>(campaign, "dueDate")));
        add(new Label("innovationObjective.name", new PropertyModel<String>(
                campaign, "innovationObjective.name")));
        add(newDescriptionPanel());
        if (!campaign.getTags().isEmpty()) {
            showTagsFragment(campaign);
        } else {
            add(new Fragment("tags", "emptyFragment", this));
        }

        // add button to get to campaign page
        add(new BookmarkablePageLink<IdeasPage>("inspectButton",
                IdeasPage.class, forCampaign(campaign)));

        // show data of campaign's chosen idea
        displayChosenIdea(campaign);
    }

    /**
     * FIXME: display the actually chosen idea.
     */
    private void displayChosenIdea(final Campaign campaign) {
        Idea idea = campaign.getIdeas().get(0);
        add(new Label("postedAt", new PropertyModel<Date>(idea, "postedAt")));
        add(new Label("postedBy.nickName", new PropertyModel<String>(idea,
                "postedBy.nickName")));
        add(new Label("shortText", new PropertyModel<String>(idea, "shortText")));
    }

    /**
     * @return
     */
    private Panel newDescriptionPanel() {
        if (getModelObject().getDescription() == null
                || getModelObject().getDescription().length() <= 500) {
            return new BlockquotePanel<Campaign>("descriptionPanel",
                    getModel(), "description");
        } else {
            return new ExpandableBlockquotePanel<String>("descriptionPanel",
                    Model.of(getModelObject().getDescription()
                            .substring(0, 500) + " ..."),
                    new PropertyModel<String>(getModelObject(), "description"),
                    Model.of("span11"));
        }
    }

    /**
     *
     */
    private void showTagsFragment(final AbstractEntity entity) {
        final Fragment showTagsFragment = new Fragment("tags",
                "showTagsFragment", this);
        final TextField<String> tagsAsString = new TextField<String>(
                "tagsAsString", new PropertyModel<String>(entity,
                "tagsAsString"));
        tagsAsString.add(new Behavior() {

            private static final long serialVersionUID = 8636536869964190406L;

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                // add javascript to load tagsinput plugin
                response.render(OnLoadHeaderItem.forScript(String.format(String
                        .format(JSTemplates.SHOW_TAGS,
                                tagsAsString.getMarkupId()))));
            }
        });
        showTagsFragment.add(tagsAsString.setOutputMarkupId(true));
        add(showTagsFragment);
    }

    /**
     * @param campaign
     * @return
     */
    private static PageParameters forCampaign(Campaign campaign) {
        return new PageParameters().set("id", campaign.getId());
    }

}
