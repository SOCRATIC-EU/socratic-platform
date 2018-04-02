package de.atb.socratic.service.notification;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.Network;
import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.Notification;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.notification.Notification_;

/**
 * NotificationService
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Named("notificationService")
@ApplicationScoped
public class NotificationService extends MailNotificationService<Notification> {

    private static final long serialVersionUID = 5394268376824683847L;

    NotificationService() {
        super(Notification.class);
    }

    public List<Notification> getAllNotificationsForUser(User user) {
        return getAllNotificationsForUser(user, null);
    }

    private List<Notification> getAllNotificationsForUser(User user, NotificationType type) {
        return getNotifications(user, type, null);
    }

    public List<Notification> getUnreadNotificationsForUser(User user) {
        return getUnreadNotificationsForUser(user, null);
    }

    private List<Notification> getUnreadNotificationsForUser(User user, NotificationType type) {
        return getNotifications(user, type, false);
    }

    public List<Notification> getReadNotificationsForUser(User user) {
        return getReadNotificationsForUser(user, null);
    }

    private List<Notification> getReadNotificationsForUser(User user, NotificationType type) {
        return getNotifications(user, null, true);
    }

    public Long countAllNotificationsForUser(User user) {
        return countAllNotificationsForUser(user, null);
    }

    private Long countAllNotificationsForUser(User user, NotificationType type) {
        return countNotifications(user, type, null);
    }

    public Long countUnreadNotificationsForUser(User user) {
        return countUnreadNotificationsForUser(user, null);
    }

    private Long countUnreadNotificationsForUser(User user, NotificationType type) {
        return countNotifications(user, type, false);
    }

    public Long countReadNotificationsForUser(User user) {
        return countReadNotificationsForUser(user, null);
    }

    private long countReadNotificationsForUser(User user, NotificationType type) {
        return countNotifications(user, null, true);
    }

    private List<Notification> getNotifications(User user, NotificationType type, Boolean read) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Notification> criteria = cb.createQuery(Notification.class);
        Root<Notification> root = criteria.from(Notification.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (read != null) {
            predicates.add(cb.not(cb.equal(root.get(Notification_.readDate), null)));
        }
        if (type != null) {
            predicates.add(cb.equal(root.get(Notification_.notificationType), type));
        }
        predicates.add(cb.equal(root.get(Notification_.user), user));
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.orderBy(cb.desc(root.get(Notification_.creationDate)));
        return em.createQuery(criteria).getResultList();
    }


    public List<Notification> getAllNotificationsForGivenUser(User user, int first, int count) {
        logger.infof("loading %d notifications starting from %d ...", count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Notification> criteria = cb.createQuery(Notification.class);
        Root<Notification> root = criteria.from(Notification.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Notification_.user), user));
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.orderBy(cb.desc(root.get(Notification_.creationDate)));
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public long countAllNotificationsForGivenUser(User user) {
        logger.infof("counting all notifications for given user with id %d...", user.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Notification> criteria = cb.createQuery(Notification.class);
        Root<Notification> root = criteria.from(Notification.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Notification_.user), user));
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    private long countNotifications(User user, NotificationType type, Boolean read) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Notification> root = criteria.from(Notification.class);
        criteria.select(cb.count(root));
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (read != null) {
            predicates.add(cb.not(cb.equal(root.get(Notification_.readDate), null)));
        }
        if (type != null) {
            predicates.add(cb.equal(root.get(Notification_.notificationType), type));
        }
        predicates.add(cb.equal(root.get(Notification_.user), user));
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.orderBy(cb.desc(root.get(Notification_.creationDate)));
        return em.createQuery(criteria).getSingleResult();
    }

    public void setAsReaded(Notification notif, User user) {
        StringBuilder jpql = new StringBuilder("UPDATE Notification SET readDate=:readDate WHERE user=:user AND DTYPE=:dType");
        jpql.append(" AND " + notif.getColumnName() + "=:obj");
        Query q = em.createQuery(jpql.toString());
        q.setParameter("readDate", new Date());
        q.setParameter("user", user);
        notif.setParameters(q);
        q.executeUpdate();
    }

    public Date getNotificationReadDateByCampaign(Campaign camp, User user) {
        StringBuilder jpql = new StringBuilder("SELECT readDate FROM Notification n WHERE n.user=:user AND campaign=:camp");
        TypedQuery<Date> q = em.createQuery(jpql.toString(), Date.class);
        q.setParameter("user", user);
        q.setParameter("camp", camp);
        return q.setMaxResults(1).getSingleResult();
    }

    public Date getNotificationReadDateByIdea(Idea idea, User user) {
        StringBuilder jpql = new StringBuilder("SELECT readDate FROM Notification n WHERE n.user=:user AND idea=:idea");
        TypedQuery<Date> q = em.createQuery(jpql.toString(), Date.class);
        q.setParameter("user", user);
        q.setParameter("idea", idea);
        return q.setMaxResults(1).getSingleResult();
    }

    public Date getNotificationCreationDateByCampaign(Campaign camp, User user) {
        StringBuilder jpql = new StringBuilder("SELECT creationDate FROM Notification n WHERE n.user=:user AND campaign=:camp");
        TypedQuery<Date> q = em.createQuery(jpql.toString(), Date.class);
        q.setParameter("user", user);
        q.setParameter("camp", camp);
        return q.setMaxResults(1).getSingleResult();
    }

    public Date getNotificationCreationDateByIdea(Idea idea, User user) {
        StringBuilder jpql = new StringBuilder("SELECT creationDate FROM Notification n WHERE n.user=:user AND idea=:idea");
        TypedQuery<Date> q = em.createQuery(jpql.toString(), Date.class);
        q.setParameter("user", user);
        q.setParameter("idea", idea);
        // sometimes there are more than one dates returned, don't know why...
        List<Date> dates = q.getResultList();
        if (!dates.isEmpty()) {
            return dates.get(0);
        } else {
            return null;
        }
    }

    public Date getNotificationCreationDateByNetwork(Network network, User user) {
        StringBuilder jpql = new StringBuilder("SELECT creationDate FROM Notification n WHERE n.user=:user AND network=:network");
        TypedQuery<Date> q = em.createQuery(jpql.toString(), Date.class);
        q.setParameter("user", user);
        q.setParameter("network", network);
        return q.setMaxResults(1).getSingleResult();
    }

    public Date getNotificationReadDateByNetwork(Network network, User user) {
        StringBuilder jpql = new StringBuilder("SELECT readDate FROM Notification n WHERE n.user=:user AND network=:network");
        TypedQuery<Date> q = em.createQuery(jpql.toString(), Date.class);
        q.setParameter("user", user);
        q.setParameter("network", network);
        return q.setMaxResults(1).getSingleResult();
    }

    public Notification getNotificationForUserAndCampaign(Campaign campaign, User user) {
        StringBuilder jpql = new StringBuilder("FROM Notification n WHERE n.user=:user AND campaign=:campaign");
        TypedQuery<Notification> q = em.createQuery(jpql.toString(), Notification.class);
        q.setParameter("user", user);
        q.setParameter("campaign", campaign);
        return q.setMaxResults(1).getSingleResult();
    }

    public void deleteOldNotifications() {
        StringBuilder jpql = new StringBuilder("DELETE FROM Notification WHERE  creationDate < :lastWeek");
        Query q = em.createQuery(jpql.toString());
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        q.setParameter("lastWeek", cal.getTime());
        q.executeUpdate();
    }

    public Date getNotificationReadDateByPlatformInvitation(User user) {
        StringBuilder jpql = new StringBuilder("SELECT readDate FROM Notification n WHERE n.user=:user");
        TypedQuery<Date> q = em.createQuery(jpql.toString(), Date.class);
        q.setParameter("user", user);
        return q.setMaxResults(1).getSingleResult();
    }

    public Date getNotificationCreationDateByPlatformInvitation(User user) {
        //
        StringBuilder jpql = new StringBuilder("SELECT creationDate FROM Notification n WHERE n.user=:user");
        TypedQuery<Date> q = em.createQuery(jpql.toString(), Date.class);
        q.setParameter("user", user);
        return q.setMaxResults(1).getSingleResult();
    }
}
