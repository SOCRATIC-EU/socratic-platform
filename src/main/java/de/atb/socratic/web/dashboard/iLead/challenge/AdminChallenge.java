/**
 *
 */
package de.atb.socratic.web.dashboard.iLead.challenge;

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

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.CampaignType;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.components.InnovationStatusIndicatorPanel;
import de.atb.socratic.web.dashboard.Dashboard;
import de.atb.socratic.web.dashboard.iLead.UserLeadDashboardPage;
import de.atb.socratic.web.inception.CampaignAddEditPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * @author ATB
 */
public class AdminChallenge extends Dashboard {
    private static final long serialVersionUID = -5559578453943490669L;

    protected Campaign theChallenge;
    @EJB
    CampaignService campaignService;
    private int daysLeft;

    public AdminChallenge(final PageParameters parameters) {
        super(parameters);

        loadChallenge(parameters.get("id"));
        // name
        add(new Label("name", new PropertyModel<String>(theChallenge, "name")));

        // status
        add(newStageIndicator(theChallenge));

        LocalDate dueDate = new LocalDate(getCorrespondingDateOfPhase(theChallenge));
        daysLeft = new Period(new LocalDate(), dueDate, PeriodType.days()).getDays();

        // this is to avoid "-x days" message
        if (daysLeft >= 0) {
            add(new Label("dueDate", daysLeft + " " + new StringResourceModel("days.to.go", this, null).getString() + " "
                    + new StringResourceModel(theChallenge.getInnovationStatus().getMessageKey(), this, null).getString()));
        } else { // if days are less than zero then change the message
            add(new Label("dueDate", theChallenge.getInnovationStatus().getMessageKey() + " is finished!"));
        }

        add(new AjaxLink<Void>("recentActivity") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(AdminActivityPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("callToAction") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(AdminChallengeCallToActionEditPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("participants") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(AdminParticipantsPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("edit") {
            private static final long serialVersionUID = -3486178522694616314L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(CampaignAddEditPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("ideaSelection") {
            private static final long serialVersionUID = -3486178522694616314L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(AdminIdeaSelectionPage.class, parameters);
            }
        });

        // add a back link
        add(new AjaxLink<User>("back") {
            private static final long serialVersionUID = -4776506958975416730L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(UserLeadDashboardPage.class);
            }

            @Override
            protected void onConfigure() {
                setVisible(true);
                super.onConfigure();
            }
        });
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "leadTab");
    }

    private void loadChallenge(final StringValue id) {
        if (id != null && !id.isEmpty()) {
            try {
                theChallenge = campaignService.getById(id.toOptionalLong());
            } catch (Exception e) {
                throw new RestartResponseException(ErrorPage.class);
            }
        }
    }

    private InnovationStatusIndicatorPanel newStageIndicator(Campaign campaign) {
        InnovationStatusIndicatorPanel innovationStatusIndicatorPanel = new InnovationStatusIndicatorPanel("status",
                Model.of(campaign));
        innovationStatusIndicatorPanel.setVisible(campaign.getCampaignType() != CampaignType.FREE_FORM);
        return innovationStatusIndicatorPanel;
    }

    private Date getCorrespondingDateOfPhase(Campaign campaign) {
        switch (campaign.getInnovationStatus()) {
            case DEFINITION:
                return campaign.getChallengeOpenForDiscussionEndDate();
            case INCEPTION:
                return campaign.getIdeationEndDate();
            case PRIORITISATION:
                return campaign.getSelectionEndDate();
            default:
                return new Date();
        }
    }
}
