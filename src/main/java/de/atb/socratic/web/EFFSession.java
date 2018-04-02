/**
 *
 */
package de.atb.socratic.web;

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

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.InnovationObjective;
import de.atb.socratic.model.votes.PrioritisationStatusEnum;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;

/**
 * @author ATB
 */
public class EFFSession extends WebSession {

    /**
     *
     */
    private static final long serialVersionUID = -7549020699087586634L;

    /**
     * The logged in user's identifier
     */
    private Long loggedInUserId;

    private InnovationObjective innvocationObjective;

    private Campaign campaign;

    private String tagChoice;

    private PrioritisationStatusEnum status;

    public String getTagChoice() {
        return tagChoice;
    }

    public void setTagChoice(String tagChoice) {
        this.tagChoice = tagChoice;
    }

    public InnovationObjective getInnvocationObjective() {
        return innvocationObjective;
    }

    public void setInnvocationObjective(InnovationObjective innvocationObjective) {
        this.innvocationObjective = innvocationObjective;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public PrioritisationStatusEnum getStatus() {
        return status;
    }

    public void setStatus(PrioritisationStatusEnum status) {
        this.status = status;
    }

    /**
     * Constructor
     *
     * @param request
     */
    public EFFSession(Request request) {
        super(request);
    }

    /**
     * @return the loggedInUserId
     */
    public synchronized Long getLoggedInUserId() {
        return loggedInUserId;
    }

    /**
     * @param loggedInUserId the loggedInUserId to set
     */
    public synchronized void setLoggedInUserId(Long loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    /**
     * @return
     */
    public synchronized boolean isAuthenticated() {
        return loggedInUserId != null;
    }

}
