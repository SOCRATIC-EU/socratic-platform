package de.atb.socratic.util.authorization;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;

import de.atb.socratic.authorization.strategies.metadata.IAuthorizationCondition;
import de.atb.socratic.model.User;
import de.atb.socratic.web.provider.LoggedInUserProvider;
import org.apache.wicket.cdi.CdiContainer;

/**
 * UserBasedAuthorizationCondition
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class UserBasedAuthorizationCondition<Type> implements IAuthorizationCondition {

    private static final long serialVersionUID = -8099532283118722619L;

    protected Type conditionalObject;
    protected User user;

    @Inject
    protected LoggedInUserProvider userProvider;

    public UserBasedAuthorizationCondition(final Type conditionalObject) {
        this.conditionalObject = conditionalObject;
        CdiContainer.get().getNonContextualManager().inject(this);
    }

    public void setConditionalObject(final Type object) {
        this.conditionalObject = object;
    }

    public Type getConditionalObject() {
        return this.conditionalObject;
    }

    protected void fillInUser() {
        CdiContainer.get().getNonContextualManager().inject(this);
        setUser(userProvider.getLoggedInUser());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static <Condition extends UserBasedAuthorizationCondition<Type>, Type> Condition get(Class<Condition> clazz,
                                                                                                Class<Type> typeClass, User user,
                                                                                                Type object) {
        try {
            Constructor<Condition> constructor = clazz.getConstructor(typeClass);
            Condition newInstance = constructor.newInstance(object);
            newInstance.setUser(user);
            return newInstance;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <Condition extends UserBasedAuthorizationCondition<Type>, Type> Condition get(Class<Condition> clazz,
                                                                                                Class<Type> typeClass, Type object) {
        try {
            Constructor<Condition> constructor = clazz.getConstructor(typeClass);
            Condition newInstance = constructor.newInstance(object);
            newInstance.fillInUser();
            return newInstance;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
