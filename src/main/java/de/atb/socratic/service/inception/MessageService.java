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

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Message;
import de.atb.socratic.model.Message_;
import de.atb.socratic.model.User;
import de.atb.socratic.service.AbstractService;

@Stateless
public class MessageService extends AbstractService<Message> {
    private static final long serialVersionUID = -1289388511788336778L;

    public List<Message> getAllSentMessages(User sender, int first, int count) {
        logger.infof(
                "loading %d messages starting from %d sent by user with Id %d and ordered by descending Creation date ...", count,
                first, sender.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Message> criteria = cb.createQuery(Message.class);
        Root<Message> root = criteria.from(Message.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Message_.sender), sender));

        predicates.add(cb.notEqual(root.get(Message_.hasSenderRemovedMessage), true));

        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        criteria.orderBy(cb.desc(root.get(Message_.postedAt)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public long countAllSentMessages(User sender) {
        logger.infof("counting all messages sent by user with Id %d ...", sender.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Message> criteria = cb.createQuery(Message.class);
        Root<Message> root = criteria.from(Message.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Message_.sender), sender));
        predicates.add(cb.notEqual(root.get(Message_.hasSenderRemovedMessage), true));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }
        return em.createQuery(criteria).getResultList().size();
    }

    public List<Message> getAllReceivedMessages(User receiver, int first, int count) {
        logger.infof(
                "loading %d messages starting from %d receiver by user with Id %d and ordered by descending Creation date ...",
                count, first, receiver.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Message> query = cb.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        Join<Message, User> join = root.join(Message_.receivers);
        Join<Message, User> alreadyDeleted = root.join(Message_.isDeletedByReceiver);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(join.in(receiver));
        predicates.add(alreadyDeleted.in(receiver));
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        query.orderBy(cb.desc(root.get(Message_.postedAt)));
        return em.createQuery(query).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public long countAllReceivedMessages(User receiver) {
        logger.infof("counting all messages received by user with Id %d ...", receiver.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Message> query = cb.createQuery(Message.class);
        Root<Message> root = query.from(Message.class);
        Join<Message, User> join = root.join(Message_.receivers);
        Join<Message, User> alreadyDeleted = root.join(Message_.isDeletedByReceiver);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(join.in(receiver));
        predicates.add(alreadyDeleted.in(receiver));

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        return em.createQuery(query).getResultList().size();
    }

    public void deleteMessageForReceiver(Message message, User receiver) {
        logger.infof("deleting message with id %d by removing receiver from isDeletedByReceiver list ...", message.getId());
        // add user to removed users list of message
        if (!message.getIsDeletedByReceiver().isEmpty()) {
            message.getIsDeletedByReceiver().remove(receiver);
            update(message);

            // check further, if you need to delete message permanently from DB
            deleteMessage(message);
        }
    }

    public void deleteMessageForSender(Message message, User sender) {
        logger.infof("deleting message with id %d by setting sender's hasSenderRemovedMessage flag ...", message.getId());

        // add user to removed users list of message
        message.setHasSenderRemovedMessage(true);
        update(message);
        // check further, if you need to delete message permanently from DB
        deleteMessage(message);
    }

    /**
     * deletes message if and only if, isDeletedByReceiver list is empty and sender has removed message flag is set
     *
     * @param message
     */
    private void deleteMessage(Message message) {
        // check further, if there was only one user in the list
        if (message.getIsDeletedByReceiver().size() == 0 && message.isHasSenderRemovedMessage()) {
            delete(message);
        }
    }

    public int countAllUnreadMessages(User receiver) {
        logger.infof("counting all unread messages received by user with Id %d ...", receiver.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Message> criteria = cb.createQuery(Message.class);
        Root<Message> root = criteria.from(Message.class);
        Join<Message, User> join = root.join(Message_.receivers);
        Join<Message, User> alreadyRead = root.join(Message_.isRedByUser);
        Join<Message, User> alreadyDeleted = root.join(Message_.isDeletedByReceiver);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(join.in(receiver));
        predicates.add(alreadyRead.in(receiver));
        predicates.add(alreadyDeleted.in(receiver));    // if user deletes message before reading it. 

        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        return em.createQuery(criteria).getResultList().size();
    }
}
