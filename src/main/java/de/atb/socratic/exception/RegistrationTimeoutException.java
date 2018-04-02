/**
 *
 */
package de.atb.socratic.exception;

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

/**
 * @author ATB
 */
public class RegistrationTimeoutException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 511076453251805425L;

    private String email;

    /**
     *
     */
    public RegistrationTimeoutException(String email) {
        super();
        this.email = email;
    }

    /**
     * @param message
     */
    public RegistrationTimeoutException(String email, String message) {
        super(message);
        this.email = email;
    }

    /**
     * @param message
     * @param cause
     */
    public RegistrationTimeoutException(String email, String message,
                                        Throwable cause) {
        super(message, cause);
        this.email = email;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

}
