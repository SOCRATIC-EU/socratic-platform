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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * InnovationStatusIndicatorPanel
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class InnovationStatusIndicatorPanel extends Panel {

    private static final long serialVersionUID = -1820367362008657788L;

    @Inject
    @LoggedInUser
    User loggedInUser;

    public InnovationStatusIndicatorPanel(String id, IModel<Campaign> campaignModel) {
        super(id, campaignModel);
        Campaign campaign = campaignModel.getObject();
        InnovationStatus campaignStatus = campaign.getInnovationStatus(); //InnovationStatus.randomize();
        BookmarkablePageLink<?> definition = newShowIdeasLink(InnovationStatus.DEFINITION, campaign, campaignStatus);
        BookmarkablePageLink<?> inception = newShowIdeasLink(InnovationStatus.INCEPTION, campaign, campaignStatus);
        BookmarkablePageLink<?> prioritisation = newShowIdeasLink(InnovationStatus.PRIORITISATION, campaign, campaignStatus);
        BookmarkablePageLink<?> implementation = newShowIdeasLink(InnovationStatus.IMPLEMENTATION, campaign, campaignStatus);
        BookmarkablePageLink<?> followUp = newShowIdeasLink(InnovationStatus.FOLLOW_UP, campaign, campaignStatus);
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

}
