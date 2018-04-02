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

import java.io.File;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.atb.socratic.util.MimeTypeDetector;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "file_infos")
public class FileInfo extends AbstractEntity {

    /**
     *
     */
    private static final long serialVersionUID = 8101653696214887602L;

    private String path;
    private String internalName;
    private String displayName;
    private String contentType;

    @Column(name = "sz")
    private Long size;

    /**
     * Default no args constructor.
     */
    public FileInfo() {
    }

    /**
     * @param path
     * @param internalName
     * @param displayName
     * @param size
     * @param contentType
     */
    public FileInfo(String path, String internalName, String displayName, Long size, String contentType) {
        this.path = path;
        this.internalName = internalName;
        this.displayName = displayName;
        this.size = size;
        this.contentType = contentType;
        if (StringUtils.isBlank(this.contentType)) {
            this.contentType = MimeTypeDetector.getMimeType(this.path);
        }
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the internalName
     */
    public String getInternalName() {
        return internalName;
    }

    /**
     * @param internalName the internalName to set
     */
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the size
     */
    public Long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * @return
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public File getFile() {
        return new File(path);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileInfo other = (FileInfo) obj;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FileInfo [path=" + path + ", internalName=" + internalName
                + ", displayName=" + displayName + ", contentType="
                + contentType + ", size=" + size + ", getId()=" + getId() + "]";
    }
}
