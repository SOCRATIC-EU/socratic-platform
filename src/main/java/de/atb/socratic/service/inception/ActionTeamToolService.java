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

import javax.ejb.Stateless;

import de.atb.socratic.model.ActionTeamTool;
import de.atb.socratic.service.AbstractService;

/**
 * @author ATB
 */
@Stateless
public class ActionTeamToolService extends AbstractService<ActionTeamTool> {

    private static final long serialVersionUID = -6777330638891953695L;

    public ActionTeamToolService() {
        super(ActionTeamTool.class);
    }

    public ActionTeamTool createOrUpdate(ActionTeamTool actionTeamTool) {
        actionTeamTool = em.merge(actionTeamTool);
        return actionTeamTool;
    }
}
