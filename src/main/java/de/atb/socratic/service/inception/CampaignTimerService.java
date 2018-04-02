/**
 *
 */
package de.atb.socratic.service.inception;

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

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.qualifier.Conversational;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@Conversational
@Singleton
@Startup
@ApplicationScoped
public class CampaignTimerService implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2046519379577851405L;

    private static final String PREFIX = "CampaignTimeout_";

    // inject a logger
    @Inject
    Logger logger;

    @Resource
    TimerService timerService;

    @Inject
    CampaignService campaignService;

    /**
     * @param timer the timer who timed out.
     */
    @Timeout
    public void timeout(Timer timer) {
        try {
            // get id of campaign to stop
            Long campaignId = Long.valueOf(timer.getInfo().toString().split("_")[1]);
            // stop it, you're killing me...
            campaignService.stopCampaign(campaignId);
            logger.infof("automatically stopped campaign with ID %s", campaignId);
        } catch (NumberFormatException | EJBException | IllegalStateException e) {
            logger.errorf("failed to stop timer %s ...", timer);
        }
    }

    /**
     * Starts or stops a timer, when campaign has been updated.
     */
    public void onCampaignUpdated(@Observes final Campaign campaign) {
        if (campaign != null) {
            logger.infof("received event to update timer for campaign %s ...", campaign.getName());
            Date dueDate = getCampaignDueDateBasedOnPhase(campaign);
            if (dueDate != null) {
                Timer timer = findTimerForCampaign(campaign.getId());
                if (timer != null) {
                    // if there is a timer for an active campaign --> update schedule
                    // if campaign has new end date --> start new timer
                    timer.cancel();
                    createTimer(campaign.getId(), dueDate);
                } else {
                    // start new timer
                    createTimer(campaign.getId(), dueDate);
                }
            }
        }
    }

    public Date getCampaignDueDateBasedOnPhase(Campaign campaign) {
        if (campaign.getInnovationStatus().equals(InnovationStatus.DEFINITION)) {
            return campaign.getChallengeOpenForDiscussionEndDate();
        } else if (campaign.getInnovationStatus().equals(InnovationStatus.INCEPTION)) {
            return campaign.getIdeationEndDate();
        } else if (campaign.getInnovationStatus().equals(InnovationStatus.PRIORITISATION)) {
            return campaign.getSelectionEndDate();
        } else {
            return null;
        }

    }

    /**
     * Cancels all timers for campaigns.
     */
    public void cancelAllTimers() {
        logger.info("cancelling all timers for campaigns ...");
        for (Timer timer : timerService.getTimers()) {
            if (timer.getInfo().toString().startsWith(PREFIX)) {
                timer.cancel();
            }
        }
    }

    private void createTimer(final Long id, final Date dueDate) {
        if (dueDate != null) {
            timerService.createSingleActionTimer(dueDate, new TimerConfig(PREFIX + id, true));
            logger.infof("started timer for campaign with ID %s", id);
        } else {
            logger.infof("due Date for campaign with ID %s should not be null", id);
        }
    }

    private Timer findTimerForCampaign(final Long campaignId) {
        for (Timer timer : timerService.getTimers()) {
            if (timer.getInfo().equals(PREFIX + campaignId)) {
                return timer;
            }
        }
        return null;
    }

}
