package de.atb.socratic.model.votes;

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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import de.atb.socratic.model.AbstractEntity;

@Entity
@XmlRootElement
@Table(name = "votes")
public class Votes extends AbstractEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @NotNull
    private int relevanceVote;

    @NotNull
    private int feasibilityVote;

    public Votes() {
        super();
        // Auto-generated constructor stub
    }

    public Votes(int relevanceVote, int feasibilityVote) {
        super();
        this.relevanceVote = relevanceVote;
        this.feasibilityVote = feasibilityVote;
    }

    public int getRelevanceVote() {
        return relevanceVote;
    }

    public void setRelevanceVote(int relevanceVote) {
        this.relevanceVote = relevanceVote;
    }

    public int getFeasibilityVote() {
        return feasibilityVote;
    }

    public void setFeasibilityVote(int feasibilityVote) {
        this.feasibilityVote = feasibilityVote;
    }

}
