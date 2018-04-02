/**
 *
 */
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

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

@Entity
@XmlRootElement
@Table(name = "networks")
@Indexed
public class Network extends AbstractEntity {

    private static final long serialVersionUID = -5444683099028756092L;

    @NotNull
    @ManyToOne(targetEntity = Company.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "company_id")
    private Company company;

    @NotNull
    @ManyToOne(targetEntity = Company.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "companyNetworked_id")
    private Company companyNetworked;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NetworkState networkState = NetworkState.NOT_CONNECTED;

    @ManyToOne
    @IndexedEmbedded
    private User invitedBy;

    @ManyToOne
    @IndexedEmbedded
    private Company blockedBy;

    @NotNull
    private Date inviteDate;

    public Network() {
    }

    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * @return the companyNetworked
     */
    public Company getCompanyNetworked() {
        return companyNetworked;
    }

    /**
     * @param companyNetworked the companyNetworked to set
     */
    public void setCompanyNetworked(Company companyNetworked) {
        this.companyNetworked = companyNetworked;
    }

    /**
     * @return the campaignType
     */
    public NetworkState getNetworkState() {
        return networkState;
    }

    /**
     * @param networkState the campaignType to set
     */
    public void setNetworkState(NetworkState networkState) {
        this.networkState = networkState;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }

    /**
     * @return the inviteDate
     */
    public Date getInviteDate() {
        return inviteDate;
    }

    /**
     * @param inviteDate the inviteDate to set
     */
    public void setInviteDate(Date inviteDate) {
        this.inviteDate = inviteDate;
    }

    public Company getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(Company blockedBy) {
        this.blockedBy = blockedBy;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Network [company=" + company + ", networkState=" + networkState
                + ", invitedBy=" + invitedBy + ", inviteDate=" + inviteDate + "]";
    }

}
