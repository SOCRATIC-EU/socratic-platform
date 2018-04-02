package de.atb.socratic.model.tour;

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
import org.hibernate.search.annotations.Indexed;


@Entity
@XmlRootElement
@Table(name = "tour")
@Indexed
public class Tour extends AbstractEntity {

    private static final long serialVersionUID = 1499201937051681690L;

    public Tour() {
    }

    public Tour(String name, long user_id, int current_step, boolean ended) {
        super();
        this.name = name;
        this.user_id = user_id;
        this.current_step = current_step;
        this.ended = ended;
    }

    @NotNull
    private String name;

    @NotNull
    private long user_id;

    @NotNull
    private int current_step = 0;

    @NotNull
    private boolean ended = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public int getCurrent_step() {
        return current_step;
    }

    public void setCurrent_step(int current_step) {
        this.current_step = current_step;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }
}
