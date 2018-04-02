package de.atb.socratic.service.periodicTimer;

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

import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import de.atb.socratic.qualifier.Conversational;
import de.atb.socratic.service.notification.NotificationService;
import org.jboss.solder.logging.Logger;
import org.joda.time.DateTime;

@Conversational
@Singleton
@Startup
@ApplicationScoped
public class PeriodicTimerService implements Serializable {

    private static final long serialVersionUID = 721696912362922326L;

    @Resource
    TimerService timerService;

    @Inject
    @Named("notificationService")
    NotificationService notificationService;

    @Inject
    Logger logger;

    @Timeout
    public void timeout(Timer timer) {
        executeTasks();
        createTimer();
    }

    public void createTimer() {
        //Nest timer for next day at 00:00
        timerService.createTimer(new DateTime(System.currentTimeMillis()).plusDays(1).withTimeAtStartOfDay().toDate(), null);
    }

    private void executeTasks() {
        //Executing periodical tasks
        logger.info("Executing periodical tasks");
        removeOldNotifications();
    }

    private void removeOldNotifications() {
        notificationService.deleteOldNotifications();
    }

}
