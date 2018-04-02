package de.atb.socratic.web.components;

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

import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.selection.SelectionPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * InnovationStatusPanel
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class InnovationStatusPanel extends GenericPanel<Campaign> {

    private static final long serialVersionUID = -1565282600463364006L;

    @Inject
    @LoggedInUser
    User loggedInUser;

    private static Campaign campaign;
    private Component definition;
    private Component inception;
    private Component prioritisation;
    private Component implementation;
    private Component followUp;

    public InnovationStatusPanel(String id, IModel<Campaign> camp) {
        super(id);
        if (camp != null) {
            campaign = camp.getObject();
        } else {
            campaign = null;
        }

        if (campaign != null) {
            InnovationStatus campaignStatus = campaign.getInnovationStatus(); // InnovationStatus.randomize();
            definition = newShowIdeasLink(InnovationStatus.DEFINITION, campaign, campaignStatus);
            inception = newShowIdeasLink(InnovationStatus.INCEPTION, campaign, campaignStatus);
            prioritisation = newShowIdeasLink(InnovationStatus.PRIORITISATION, campaign, campaignStatus);
            implementation = newShowIdeasLink(InnovationStatus.IMPLEMENTATION, campaign, campaignStatus);
            followUp = newShowIdeasLink(InnovationStatus.FOLLOW_UP, campaign, campaignStatus);
        } else {
            definition = new BookmarkablePageLink<SelectionPage>("definition", SelectionPage.class, null);
            inception = new BookmarkablePageLink<SelectionPage>("inception", SelectionPage.class, null);
            prioritisation = new BookmarkablePageLink<SelectionPage>("prioritisation", SelectionPage.class, null);
            implementation = new BookmarkablePageLink<SelectionPage>("implementation", SelectionPage.class, null);
            followUp = new BookmarkablePageLink<SelectionPage>("follow_up", SelectionPage.class, null);
        }
        definition.add(new TooltipBehavior(new StringResourceModel("definition.status", this, null), new TooltipConfig().withPlacement(TooltipConfig.Placement.top)));
        inception.add(new TooltipBehavior(new StringResourceModel("inception.status", this, null), new TooltipConfig().withPlacement(TooltipConfig.Placement.top)));
        prioritisation.add(new TooltipBehavior(new StringResourceModel("prioritisation.status", this, null), new TooltipConfig().withPlacement(TooltipConfig.Placement.top)));
        implementation.add(new TooltipBehavior(new StringResourceModel("implementation.status", this, null), new TooltipConfig().withPlacement(TooltipConfig.Placement.top)));
        followUp.add(new TooltipBehavior(new StringResourceModel("follow_up.status", this, null), new TooltipConfig().withPlacement(TooltipConfig.Placement.top)));
        add(definition);
        add(inception);
        add(prioritisation);
        add(implementation);
        add(followUp);
    }

    private BookmarkablePageLink<?> newShowIdeasLink(final InnovationStatus componentStatus, final Campaign campaign,
                                                     final InnovationStatus campaignStatus) {
        BookmarkablePageLink<?> link = componentStatus.getLinkToCorrespondingStage(componentStatus.name().toLowerCase(), campaign, loggedInUser);
        if ((campaignStatus == componentStatus) || componentStatus.isPredecessorOf(campaignStatus)) {
            link.add(new CssClassNameAppender("enabled"));
            link.add(new AttributeModifier("style", "pointer-events:all"));
        } else {
            link.add(new CssClassNameAppender("disabled"));
            link.add(new AttributeModifier("style", "pointer-events:none"));
        }
        return link;
    }

    @Override
    protected void onConfigure() {
        if (campaign == null) {
            inception.add(new CssClassNameAppender("disabled"));
            prioritisation.add(new CssClassNameAppender("disabled"));
            implementation.add(new CssClassNameAppender("disabled"));
            followUp.add(new CssClassNameAppender("disabled"));
        }
        super.onConfigure();
    }
}
