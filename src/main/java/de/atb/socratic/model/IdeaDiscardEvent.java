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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.IndexedEmbedded;

@Entity
@XmlRootElement
@Table(name = "idea_discardevent")
public class IdeaDiscardEvent extends AbstractEntity {
    /**
     *
     */
    private static final long serialVersionUID = 7040047517826224372L;

    @NotNull
    @ManyToOne
    @IndexedEmbedded
    private User discardedBy;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date discarded;

    public IdeaDiscardEvent() {
        super();
    }

    public User getDiscardedBy() {
        return discardedBy;
    }

    public void setDiscardedBy(User discardedBy) {
        this.discardedBy = discardedBy;
    }

    public Date getDiscarded() {
        return discarded;
    }

    public void setDiscarded(Date discarded) {
        this.discarded = discarded;
    }

}
