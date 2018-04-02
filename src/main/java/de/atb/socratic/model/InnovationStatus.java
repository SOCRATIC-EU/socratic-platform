package de.atb.socratic.model;

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

import java.util.Random;

import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.dashboard.iLead.challenge.AdminIdeaSelectionPage;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.selection.SelectionPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public enum InnovationStatus {

    DEFINITION("innovation-status.definition"),

    INCEPTION("innovation-status.inception"),

    PRIORITISATION("innovation-status.prioritisation"),

    IMPLEMENTATION("innovation-status.implementation"),

    FOLLOW_UP("innovation-status.follow-up");

    private static final Random random = new Random();

    private final String messageKey;

    InnovationStatus(final String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return this.messageKey;
    }

    public boolean isPredecessorOf(InnovationStatus other) {
        switch (this) {
            case DEFINITION:
                return (other == INCEPTION) || (other == PRIORITISATION) || (other == IMPLEMENTATION) || (other == FOLLOW_UP);
            case INCEPTION:
                return (other == PRIORITISATION) || (other == IMPLEMENTATION) || (other == FOLLOW_UP);
            case PRIORITISATION:
                return (other == IMPLEMENTATION) || (other == FOLLOW_UP);
            case IMPLEMENTATION:
                return other == FOLLOW_UP;
            default:
                return false;
        }
    }

    public boolean isFollowerOf(InnovationStatus other) {
        switch (this) {
            case PRIORITISATION:
                return other == INCEPTION;
            case IMPLEMENTATION:
                return (other == PRIORITISATION) || (other == INCEPTION);
            case FOLLOW_UP:
                return (other == IMPLEMENTATION) || (other == PRIORITISATION) || (other == INCEPTION);
            default:
                return false;
        }
    }

    public static InnovationStatus randomize() {
        int pick = random.nextInt(InnovationStatus.values().length);
        return InnovationStatus.values()[pick];
    }

    public CharSequence getLinkToCorrespondingStage(Campaign campaign, User user) {
        BookmarkablePageLink<?> link = this.getLinkToCorrespondingStage(
                "dummy", campaign, user);
        return link.urlFor(link.getPageClass(), link.getPageParameters());
    }

    @SuppressWarnings("unchecked")
    public BookmarkablePageLink<? extends BasePage> getLinkToCorrespondingStage(
            final String id, Campaign campaign, User user) {
        BookmarkablePageLink<? extends BasePage> link = new BookmarkablePageLink<>(id, IdeasPage.class, forCampaign(campaign));
        boolean isAdmin = user != null && user.hasAnyRoles(UserRole.MANAGER, UserRole.ADMIN, UserRole.SUPER_ADMIN);
        switch (this) {
            case DEFINITION:
                if (isAdmin) {
                    link = new BookmarkablePageLink<>(id, ChallengeDefinitionPage.class, forCampaign(campaign));
                    break;
                } else {
                    link = new BookmarkablePageLink<>(id, ChallengeDefinitionPage.class, forCampaign(campaign));
                    break;
                }
            case INCEPTION:
                break;
            case PRIORITISATION:
                link = new BookmarkablePageLink<>(id, SelectionPage.class, forCampaign(campaign));
                break;
            case IMPLEMENTATION:
                if (campaign.getCreatedBy().equals(user)) {
                    link = new BookmarkablePageLink<>(id, AdminIdeaSelectionPage.class, forCampaign(campaign));
                    break;
                } else {
                    link = new BookmarkablePageLink<>(id, SelectionPage.class, forCampaign(campaign));
                    break;
                }
            default:
                link = new BookmarkablePageLink<>(id, IdeasPage.class, forCampaign(campaign));
                break;
        }
        return link;
    }

    public BookmarkablePageLink<? extends BasePage> getLinkToCorrespondingStage(
            final String id, Idea idea, User user) {
        return getLinkToCorrespondingStage(id, idea.getCampaign(), user);
    }

    protected PageParameters forCampaign(Campaign campaign) {
        return new PageParameters().set("id", campaign.getId());
    }

}
