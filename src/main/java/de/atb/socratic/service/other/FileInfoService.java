package de.atb.socratic.service.other;

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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.FileInfo_;
import de.atb.socratic.service.AbstractService;

@Stateless
public class FileInfoService extends AbstractService<FileInfo> {

    public FileInfoService() {
        super(FileInfo.class);
    }

    /**
     *
     */
    private static final long serialVersionUID = 632625335365363627L;

    public FileInfo getByInternalName(final String internalName) {
        logger.infof("getting file by internal name %s...", internalName);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<FileInfo> criteria = cb.createQuery(FileInfo.class);
        Root<FileInfo> root = criteria.from(FileInfo.class);
        criteria.where(cb.equal(root.get(FileInfo_.internalName), internalName));
        return em.createQuery(criteria).getSingleResult();
    }
}
