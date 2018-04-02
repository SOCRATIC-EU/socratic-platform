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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Campaign_;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.Comment_;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.IdeaDiscardEvent;
import de.atb.socratic.model.Idea_;
import de.atb.socratic.model.InnovationObjective;
import de.atb.socratic.model.Reply;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.User_;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.validation.Prioritisation;
import de.atb.socratic.model.votes.PrioritisationDotSplitVotes;
import de.atb.socratic.model.votes.PrioritisationStatusEnum;
import de.atb.socratic.model.votes.VoteType;
import de.atb.socratic.model.votes.Votes;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import de.atb.socratic.web.inception.idea.IdeaDevelopmentPhase;
import de.atb.socratic.web.provider.UrlProvider;
import org.hibernate.search.query.dsl.PhraseContext;
import org.hibernate.search.query.dsl.PhraseMatchingContext;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.WildcardContext;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@Stateless
public class IdeaService extends AbstractService<Idea> {

    private static final long serialVersionUID = 5689160616648831544L;

    @Inject
    Event<Idea> ideaEventSrc;

    @Inject
    UserService userService;

    @Inject
    ScopeService scopeService;

    @Inject
    CommentService commentService;

    @Inject
    UrlProvider urlProvider;

    @Inject
    ParticipateNotificationService participateNotifier;

    @Inject
    Logger logger;

    private final PrioritisationStatusEnum RUNNING = PrioritisationStatusEnum.Running;
    private final PrioritisationStatusEnum SCHEDULED = PrioritisationStatusEnum.Scheduled;
    private final PrioritisationStatusEnum STOPPED = PrioritisationStatusEnum.Stopped;
    private final PrioritisationStatusEnum FINISHED = PrioritisationStatusEnum.Finished;

    public IdeaService() {
        super(Idea.class);
    }

    @Override
    protected TermMatchingContext setFullTextSearchFields(WildcardContext wc) {
        return wc.onField("shortText")
                .andField("description")
                .andField("elevatorPitch")
                .andField("beneficiaries")
                .andField("valueForBeneficiaries")
                .andField("impactStakeholders")
                .andField("resourcesForIdeaImplementation")
                .andField("implementationPlan")
                .andField("location")
                .andField("reasonForBringingIdeaForward")
                .andField("relatedInnovations")
                .andField("postedBy.firstName")
                .andField("postedBy.lastName")
                .andField("postedBy.nickName")
                .andField("postedBy.email")             // does not work
                .andField("keywords")
                .andField("skills")
                .andField("collaborators.firstName")    // not tested
                .andField("collaborators.lastName")     // not tested
                .andField("collaborators.nickName")     // not tested
                .andField("collaborators.email")        // not tested
                .andField("comments.commentText")
                .andField("comments.postedBy.firstName")
                .andField("comments.postedBy.lastName")
                .andField("comments.postedBy.nickName")
                .andField("comments.postedBy.email");   // does not work
    }

    @Override
    protected PhraseMatchingContext setFullTextSearchFields(PhraseContext wc) {
        return wc.onField("shortText")
                .andField("description")
                .andField("elevatorPitch")
                .andField("beneficiaries")
                .andField("valueForBeneficiaries")
                .andField("impactStakeholders")
                .andField("resourcesForIdeaImplementation")
                .andField("implementationPlan")
                .andField("location")
                .andField("reasonForBringingIdeaForward")
                .andField("relatedInnovations")
                .andField("postedBy.firstName")
                .andField("postedBy.lastName")
                .andField("postedBy.nickName")
                .andField("postedBy.email")             // does not work
                .andField("keywords")
                .andField("skills")
                .andField("collaborators.firstName")    // not tested
                .andField("collaborators.lastName")     // not tested
                .andField("collaborators.nickName")     // not tested
                .andField("collaborators.email")        // not tested
                .andField("comments.commentText")
                .andField("comments.postedBy.firstName")
                .andField("comments.postedBy.lastName")
                .andField("comments.postedBy.nickName")
                .andField("comments.postedBy.email");   // does not work
    }

    @Override
    protected List<Idea> filterFullTextSearchResults(List<Idea> results, final User loggedInUser) {

        if (loggedInUser == null) {
            return new ArrayList<Idea>(0);
        } else {
            filter(results.iterator(), loggedInUser);
            return results;
        }
    }

    public Iterator<Idea> filter(Iterator<Idea> itemsToBeFiltered, User user) {
        while (itemsToBeFiltered.hasNext()) {
            Idea i = itemsToBeFiltered.next();
            if (i.getDeleted()) {
                itemsToBeFiltered.remove();
            } else {
                if (i.getCampaign() == null) {
                    itemsToBeFiltered.remove();
                }
            }
        }
        return itemsToBeFiltered;
    }

    public List<Idea> getAllListofIdeasGivenStatus(
            InnovationObjective innovationObjective, Campaign campaign, int offset, int count,
            PrioritisationStatusEnum status, final User loggedInUser) {

        if (status == null) {
            List<Idea> ret = getAllListofIdeas(innovationObjective, campaign, offset, count);
            return ret;
        }
        switch (status) {
            case New:
                return getNewIdeas(innovationObjective, campaign, offset, count);
            case Discarded:
                return getDiscardedIdeas(innovationObjective, campaign, offset, count, loggedInUser);
            case Launched:
                return getLaunchedIdeas(innovationObjective, campaign, offset, count, loggedInUser);
            default:
                StringBuilder jpql = new StringBuilder(
                        "SELECT i FROM Idea i LEFT JOIN i.dotVotingConfig dotVoting LEFT JOIN i.prioMatrixConfig prioMatrixConfig LEFT JOIN i.anonVotingConfig anonVote LEFT JOIN i.quickVoteConfig quickVote "
                                + "WHERE (dotVoting.status=:status OR prioMatrixConfig=:status OR anonVote.status=:status OR quickVote.status=:status)");

                if (null != campaign) {
                    jpql.append(" AND i.campaign=:campaign");
                }
                if (null != innovationObjective) {
                    jpql.append(" AND i.campaign.innovationObjective=:innovationObjective");
                }
                jpql.append(" AND i.campaign.company=:company");

                TypedQuery<Idea> q = em.createQuery(jpql.toString(), Idea.class);
                if (null != campaign) {
                    q.setParameter("campaign", campaign);
                }
                if (null != innovationObjective) {
                    q.setParameter("innovationObjective", innovationObjective);
                }
                q.setParameter("status", status);
                q.setParameter("company", loggedInUser.getCurrentCompany());

                return q.setFirstResult(offset).setMaxResults(count)
                        .getResultList();
        }
    }

    private List<Idea> getNewIdeas(InnovationObjective innovationObjective, Campaign campaign, int offset, int count) {
        StringBuilder jpql = new StringBuilder(
                "SELECT i FROM Project p RIGHT JOIN p.ideas i LEFT JOIN i.campaign campaign WHERE p IS NULL"
                        + " AND i.dotVotingConfig IS NULL AND i.prioMatrixConfig IS NULL AND i.anonVotingConfig IS NULL AND i.quickVoteConfig IS NULL AND i.discardEvent IS NULL");

        if (null != campaign) {
            jpql.append(" AND i.campaign=:campaign");
        }
        if (null != innovationObjective) {
            jpql.append(" AND i.campaign.innovationObjective=:innovationObjective");
        }

        TypedQuery<Idea> q = em.createQuery(jpql.toString(), Idea.class);
        if (null != campaign) {
            q.setParameter("campaign", campaign);
        }
        if (null != innovationObjective) {
            q.setParameter("innovationObjective", innovationObjective);
        }
        List<Idea> ret = q.setFirstResult(offset).setMaxResults(count)
                .getResultList();
        return ret;
    }

    private List<Idea> getLaunchedIdeas(
            InnovationObjective innovationObjective, Campaign campaign, int offset, int count, final User loggedInUser) {

        StringBuilder jpql = new StringBuilder(
                "SELECT i FROM Project p LEFT JOIN p.ideas i LEFT JOIN i.campaign campaign WHERE i.deleted=0 ");
        if (null != campaign) {
            jpql.append(" AND i.campaign=:campaign");
        }
        if (null != innovationObjective) {
            jpql.append(" AND i.campaign.innovationObjective=:innovationObjective");
        }
        jpql.append(" AND i.campaign.company=:company");

        TypedQuery<Idea> q = em.createQuery(jpql.toString(), Idea.class);
        if (null != campaign) {
            q.setParameter("campaign", campaign);
        }
        if (null != innovationObjective) {
            q.setParameter("innovationObjective", innovationObjective);
        }
        q.setParameter("company", loggedInUser.getCurrentCompany());
        return q.setFirstResult(offset).setMaxResults(count).getResultList();
    }

    // write with JPA typed queries
    public List<Idea> getIdeasUnderPrio(
            InnovationObjective innovationObjective, Campaign campaign,
            int offset, int count, final User loggedInUser) {

        StringBuilder jpql = new StringBuilder(
                "SELECT i FROM Idea i LEFT JOIN i.dotVotingConfig dotVoting LEFT JOIN i.prioMatrixConfig prioMatrixConfig LEFT JOIN i.quickVoteConfig quickVote LEFT JOIN i.anonVotingConfig anonVote WHERE i.campaign.lcPhase=:lcPhase AND i.discardEvent IS NULL AND i.deleted<>1 AND (dotVoting.status=:status OR dotVoting.status=:statusS OR prioMatrixConfig.status=:status OR prioMatrixConfig.status=:statusS OR quickVote.status=:status OR quickVote.status=:statusS OR anonVote.status=:status OR anonVote.status=:statusS) AND NOT EXISTS (SELECT project FROM Project project WHERE i MEMBER OF project.ideas)");

        if (null != campaign) {
            jpql.append(" AND i.campaign=:campaign");
        }
        if (null != innovationObjective) {
            jpql.append(" AND i.campaign.innovationObjective=:innovationObjective");
        }

        jpql.append(" AND i.campaign.company=:company");

        TypedQuery<Idea> q = em.createQuery(jpql.toString(), Idea.class);

        if (null != campaign) {
            q.setParameter("campaign", campaign);
        }
        if (null != innovationObjective) {
            q.setParameter("innovationObjective", innovationObjective);
        }
        q.setParameter("lcPhase", Prioritisation.class);
        q.setParameter("status", RUNNING);
        q.setParameter("statusS", SCHEDULED);
        q.setParameter("company", loggedInUser.getCurrentCompany());
        return q.setFirstResult(offset).setMaxResults(count).getResultList();
    }

    public List<Idea> getListOfAllIdeas(
            InnovationObjective innovationObjective, Campaign campaign, int offset, int count) {
        return getAllListofIdeas(innovationObjective, campaign, offset, count);
    }

    private List<Idea> getAllListofIdeas(
            InnovationObjective innovationObjective, Campaign campaign, int offset, int count) {
        StringBuilder jpql = new StringBuilder(
                "SELECT i FROM Idea i LEFT JOIN i.dotVotingConfig dotVoting LEFT JOIN i.prioMatrixConfig prioMatrixConfig LEFT JOIN i.quickVoteConfig quickVote LEFT JOIN i.anonVotingConfig anonVote "
                        + "WHERE i.deleted=0 ");
        if (null != campaign) {
            jpql.append(" AND i.campaign=:campaign");
        }
        if (null != innovationObjective) {
            jpql.append(" AND i.campaign.innovationObjective=:innovationObjective");
        }

        TypedQuery<Idea> q = em.createQuery(jpql.toString(), Idea.class);
        if (null != campaign) {
            q.setParameter("campaign", campaign);
        }
        if (null != innovationObjective) {
            q.setParameter("innovationObjective", innovationObjective);
        }
        return q.setFirstResult(offset).setMaxResults(count).getResultList();
    }

    private Long getCountAllListofIdeas(
            InnovationObjective innovationObjective, Campaign campaign) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(DISTINCT i) FROM Idea i LEFT JOIN i.dotVotingConfig dotVoting LEFT JOIN i.prioMatrixConfig prioMatrixConfig LEFT JOIN i.quickVoteConfig quickVote LEFT JOIN i.anonVotingConfig anonVote "
                        + "WHERE i.deleted=0 ");

        if (null != campaign) {
            jpql.append(" AND i.campaign=:campaign");
        }
        if (null != innovationObjective) {
            jpql.append(" AND i.campaign.innovationObjective=:innovationObjective");
        }

        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
        if (null != campaign) {
            q.setParameter("campaign", campaign);
        }
        if (null != innovationObjective) {
            q.setParameter("innovationObjective", innovationObjective);
        }
        return q.getSingleResult();
    }

    private Long getCountAllListofIdeas(
            InnovationObjective innovationObjective, Campaign campaign,
            PrioritisationStatusEnum status, final User loggedInUser) {

        if (status == null) {
            Long ret = countIdeasForListOfIdeasTab(innovationObjective, campaign);
            return ret;
        }
        switch (status) {
            case New:
                return countNewIdeas(innovationObjective, campaign);
            case Discarded:
                return countDiscardedIdeas(innovationObjective, campaign, loggedInUser);
            case Launched:
                return countLaunchedIdeas(innovationObjective, campaign);
            default:
                StringBuilder jpql = new StringBuilder(
                        "SELECT COUNT(DISTINCT i) FROM Idea i LEFT JOIN i.dotVotingConfig dotVoting LEFT JOIN i.prioMatrixConfig prioMatrixConfig LEFT JOIN i.anonVotingConfig anonVote LEFT JOIN i.quickVoteConfig quickVote "
                                + "WHERE (dotVoting.status=:status OR prioMatrixConfig=:status OR anonVote.status=:status OR quickVote.status=:status)");
                if (null != campaign) {
                    jpql.append(" AND i.campaign=:campaign");
                }
                if (null != innovationObjective) {
                    jpql.append(" AND i.campaign.innovationObjective=:innovationObjective");
                }

                TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
                if (null != campaign) {
                    q.setParameter("campaign", campaign);
                }
                if (null != innovationObjective) {
                    q.setParameter("innovationObjective", innovationObjective);
                }
                q.setParameter("status", status);

                Long ret = q.getSingleResult();
                return ret;
        }
    }

    private Long countNewIdeas(InnovationObjective innovationObjective, Campaign campaign) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(DISTINCT i) FROM Project p RIGHT JOIN p.ideas i LEFT JOIN i.campaign campaign WHERE p IS NULL"
                        + " AND i.dotVotingConfig IS NULL AND i.prioMatrixConfig IS NULL AND i.anonVotingConfig IS NULL AND i.quickVoteConfig IS NULL AND i.discardEvent IS NULL");

        if (null != campaign) {
            jpql.append(" AND i.campaign=:campaign");
        }
        if (null != innovationObjective) {
            jpql.append(" AND i.campaign.innovationObjective=:innovationObjective");
        }

        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
        if (null != campaign) {
            q.setParameter("campaign", campaign);
        }
        if (null != innovationObjective) {
            q.setParameter("innovationObjective", innovationObjective);
        }
        Long ret = q.getSingleResult();
        return ret;
    }

    private Long countLaunchedIdeas(InnovationObjective innovationObjective, Campaign campaign) {

        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(DISTINCT i) FROM Project p LEFT JOIN p.ideas i LEFT JOIN i.campaign campaign WHERE i.deleted=0 ");
        if (null != campaign) {
            jpql.append("AND i.campaign=:campaign");
        }
        if (null != innovationObjective) {
            jpql.append("AND i.campaign.innovationObjective=:innovationObjective");
        }

        TypedQuery<Long> q = em.createQuery(jpql.toString(), Long.class);
        if (null != campaign) {
            q.setParameter("campaign", campaign);
        }
        if (null != innovationObjective) {
            q.setParameter("innovationObjective", innovationObjective);
        }
        return q.getSingleResult();
    }

    private List<Idea> genericIdeaSearch(
            InnovationObjective innovationObjective, Campaign campaign,
            boolean discarded, int offset, int count, final User loggedInUser) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> query = cb.createQuery(Idea.class);

        Root<Idea> root = query.from(Idea.class);

        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(cb.not(root.get(Idea_.deleted)));
        predicates.add(discarded ? cb.isNotNull(root.get(Idea_.discardEvent))
                : cb.isNull(root.get(Idea_.discardEvent)));

        if (campaign != null) {
            predicates.add(cb.equal(root.get(Idea_.campaign), campaign));
        }

        // Requirement by ATB: 2013/10/07: Show the company's
        // campagins only
        // ATB - 2013/10/11: only restrict visibility if user is NOT
        // ADMIN
        // take actual SCOPE into account when restricting visibility !!!
        if (!loggedInUser.hasAnyRoles(UserRole.SUPER_ADMIN)) {
            predicates.add(cb.equal(
                    root.get(Idea_.campaign).get(Campaign_.company),
                    loggedInUser.getCurrentCompany()));
        }

        if (null != innovationObjective) {
            predicates
                    .add(cb.equal(
                            root.get(Idea_.campaign).get(
                                    Campaign_.innovationObjective),
                            innovationObjective));
        }

        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        return em.createQuery(query).setFirstResult(offset)
                .setMaxResults(count).getResultList();
    }

    public List<Idea> getDiscardedIdeas(
            InnovationObjective innovationObjective, Campaign campaign, int offset, int count, final User loggedInUser) {
        return this.genericIdeaSearch(innovationObjective, campaign, true, offset, count, loggedInUser);
    }

    public Long countDiscardedIdeas(InnovationObjective innovationObjective, Campaign campaign, final User loggedInUser) {
        return this.genericIdeaCount(innovationObjective, campaign, true, loggedInUser);
    }

    public Long countIdeasForListOfIdeasTab(
            InnovationObjective innovationObjective, Campaign campaign) {
        return getCountAllListofIdeas(innovationObjective, campaign);
    }

    public Long countIdeasForListOfIdeasTab(
            InnovationObjective innovationObjective, Campaign campaign, PrioritisationStatusEnum status, final User loggedInUser) {
        return getCountAllListofIdeas(innovationObjective, campaign, status, loggedInUser);
    }

    private Long genericIdeaCount(InnovationObjective innovationObjective, Campaign campaign, boolean discarded, final User loggedInUser) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<Idea> root = query.from(Idea.class);
        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(cb.not(root.get(Idea_.deleted)));
        predicates.add(discarded ? cb.isNotNull(root.get(Idea_.discardEvent))
                : cb.isNull(root.get(Idea_.discardEvent)));

        if (campaign != null) {
            predicates.add(cb.equal(root.get(Idea_.campaign), campaign));
        }

        if (null != innovationObjective) {
            predicates
                    .add(cb.equal(
                            root.get(Idea_.campaign).get(
                                    Campaign_.innovationObjective),
                            innovationObjective));
        }

        // Requirement by ATB: 2013/10/07: Show the company's
        // campagins only
        // ATB - 2013/10/11: only restrict visibility if user is NOT
        // ADMIN
        // take actual SCOPE into account when restricting visibility !!!
        if (!loggedInUser.hasAnyRoles(UserRole.SUPER_ADMIN)) {
            predicates.add(cb.equal(
                    root.get(Idea_.campaign).get(Campaign_.company),
                    loggedInUser.getCurrentCompany()));
        }

        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        query.select(cb.countDistinct(root));
        return em.createQuery(query).getSingleResult();
    }

    /**
     * @param ideaId
     * @return
     */
    @Override
    public Idea getById(Long ideaId) {
        Idea idea = super.getById(ideaId);
        if ((idea != null) && idea.getDeleted()) {
            throw new EntityNotFoundException(String.format(
                    "Idea with id %d does not exist", ideaId));
        }
        return idea;
    }

    /**
     * Mark idea as deleted but do not really remove from DB.
     *
     * @param ideaId
     */
    public void softDelete(Long ideaId) {
        Idea idea = getById(ideaId);
        idea.setDeleted(true);
        update(idea);
    }

    /**
     * Mark idea as deleted but do not really remove from DB.
     *
     * @param idea
     */
    public void softDelete(Idea idea) {
        idea.setDeleted(true);

        // once idea is deleted decrease NoOfIdeasLeads
        userService.decreaseNoOfIdeasLeads(idea.getPostedBy());

        update(idea);
    }

    /**
     * Mark idea discarded
     */
    private void discard(Idea idea, User user) {
        IdeaDiscardEvent discardEvent = new IdeaDiscardEvent();
        discardEvent.setDiscardedBy(user);
        discardEvent.setDiscarded(new Date());
        idea.setDiscardEvent(discardEvent);

        update(idea);
    }

    /**
     * Mark ideas discarded
     *
     * @param user
     */
    public void discard(Collection<Idea> ideas, User user) {
        for (Idea idea : ideas) {
            this.discard(idea, user);
        }
    }

    /**
     * Mark idea undiscarded
     */
    private void undiscard(Idea idea) {
        idea.setDiscardEvent(null);
        update(idea);
    }

    /**
     * Mark ideas undiscarded
     */
    public void undiscard(Collection<Idea> ideas) {
        for (Idea idea : ideas) {
            this.undiscard(idea);
        }
    }

    /**
     * @param ideas
     */
    public void softDelete(Collection<Idea> ideas) {
        for (Idea idea : ideas) {
            softDelete(idea);
        }
    }

    /**
     * @param idea
     * @param userId
     * @return
     */
    public Idea voteUp(Idea idea, Long userId) {
        // remove user id from up votes if present
        // if not present (= remove returns false), add to up votes
        if (!idea.getUpVotes().remove(userId)) {
            idea.getUpVotes().add(userId);
        }
        // remove from down votes
        idea.getDownVotes().remove(userId);
        return update(idea);
    }

    /**
     * @param idea
     * @param userId
     * @return
     */
    public Idea voteDown(Idea idea, Long userId) {
        // remove user id from down votes if present
        // if not present (= remove returns false), add to down votes
        if (!idea.getDownVotes().remove(userId)) {
            idea.getDownVotes().add(userId);
        }
        // remove from up votes
        idea.getUpVotes().remove(userId);
        return update(idea);
    }

    /**
     * @param idea
     * @param comment
     * @return
     */
    public Comment addComment(Idea idea, Comment comment) {
        idea = em.merge(idea);
        comment = addCommentToIdea(idea, comment);
        logger.infof("added comment %s to idea %s ...", comment, idea);
        return comment;
    }

    /**
     *
     * @param idea
     * @param comment
     * @return
     */
    private Comment addCommentToIdea(Idea idea, Comment comment) {
        comment = commentService.create(comment);
        idea.addComment(comment);
        em.merge(idea);
        return comment;
    }

    /**
     * @param ideaId
     * @param comment
     */
    public void removeComment(Long ideaId, Comment comment) {
        Idea idea = getById(ideaId);
        removeCommentFromIdea(idea, comment);
        logger.infof("removed comment %s from idea with ID %d ...", comment,
                ideaId);
    }

    /**
     * @param idea
     * @param comment
     */
    public void removeComment(Idea idea, Comment comment) {
        idea = em.merge(idea);
        removeCommentFromIdea(idea, comment);
        logger.infof("removed comment %s from idea %s ...", comment, idea);
    }

    /**
     * @param idea
     * @param comment
     */
    private void removeCommentFromIdea(Idea idea, Comment comment) {
        comment = em.merge(comment);
        idea.removeComment(comment);

        // once comment is removed/deleted, reduce NoOfCommentsPosted for given user
        userService.decreaseNoOfCommentsPosted(comment.getPostedBy());
        em.merge(idea);
    }

    /**
     * @param replyId
     * @return
     */
    public Reply getReplyById(Long replyId) {
        logger.infof("loading reply with ID %d ...", replyId);
        Reply reply = em.find(Reply.class, replyId);
        if (reply == null) {
            throw new EntityNotFoundException(String.format(
                    "Reply with id %d does not exist", replyId));
        }
        return reply;
    }

    /**
     * @param reply
     * @return
     */
    public Reply detachReply(Reply reply) {
        em.detach(reply);
        return reply;
    }

    /**
     * @param commentId
     * @param reply
     * @return
     */
    public Reply addReply(Long commentId, Reply reply) {
        Comment comment = commentService.getCommentById(commentId);
        reply = addReplyToComment(comment, reply);
        logger.infof("added reply %s to comment with ID %d ...", reply, commentId);
        return reply;
    }

    /**
     * @param comment
     * @param reply
     * @return
     */
    public Reply addReply(Comment comment, Reply reply) {
        comment = em.merge(comment);
        reply = addReplyToComment(comment, reply);
        logger.infof("added reply %s to comment %s ...", reply, comment);
        return reply;
    }

    /**
     * @param comment
     * @param reply
     * @return
     */
    private Reply addReplyToComment(Comment comment, Reply reply) {
        reply = em.merge(reply);
        comment.addReply(reply);
        em.merge(comment);
        return reply;
    }

    /**
     *
     * @param reply
     * @return
     */
    public Reply updateReply(Reply reply) {
        reply = em.merge(reply);
        logger.infof("updated reply %s", reply);
        return reply;
    }

    /**
     * @param commentId
     * @param reply
     */
    public void removeReply(Long commentId, Reply reply) {
        Comment comment = commentService.getCommentById(commentId);
        removeReplyFromComment(comment, reply);
        logger.infof("removed reply %s from comment with ID %d ...", reply,
                commentId);
    }

    /**
     * @param comment
     * @param reply
     */
    public void removeComment(Comment comment, Reply reply) {
        comment = em.merge(comment);
        removeReplyFromComment(comment, reply);
        logger.infof("removed reply %s from comment %s ...", reply, comment);
    }

    /**
     * @param comment
     * @param reply
     */
    private void removeReplyFromComment(Comment comment, Reply reply) {
        reply = em.merge(reply);
        comment.removeReply(reply);
        em.merge(comment);
    }

    //
    // @author Zahid & Mitat (C4FF)
    //
    //
    public Idea addorUpdateDotVote(Idea idea, User user, int votesint, String comment, String tag) {
        Votes vote = new Votes();
        if (idea.getPrioritisationDotVote(user) != null) {
            vote = idea.getPrioritisationDotVote(user);
        }
        if (tag.compareToIgnoreCase(VoteType.relevance.toString()) == 0) {
            vote.setRelevanceVote(votesint);
        } else if (tag.compareToIgnoreCase(VoteType.feasibility.toString()) == 0) {
            vote.setFeasibilityVote(votesint);
        }
        if (idea.getPrioritisationDotVoteObject(user) == null) {
            idea.getPrioritisationDotVotes().add(new PrioritisationDotSplitVotes(user, vote, comment));
        } else // update existing vote
        {
            if (vote.getRelevanceVote() == -1 || vote.getFeasibilityVote() == -1) {
                vote = idea.getPrioritisationDotVote(user);
            }
            idea.setPrioritisationDotVote(user, vote, comment);
        }
        return update(idea);
    }

    public Idea addorUpdateDotVote(Idea idea, User user, Votes vote, String comment) {
        if (idea.getPrioritisationDotVoteObject(user) == null) {
            idea.getPrioritisationDotVotes().add(new PrioritisationDotSplitVotes(user, vote, comment));
        } else // update existing vote
        {
            if (vote.getRelevanceVote() == -1 || vote.getFeasibilityVote() == -1) {
                vote = idea.getPrioritisationDotVote(user);
            }
            idea.setPrioritisationDotVote(user, vote, comment);
        }
        return update(idea);
    }

    @Override
    public Idea create(Idea idea) {
        idea = createOrUpdate(idea);
        logger.infof("persisted idea '%s' ...", idea);

        // update counter noOfIdeasLeads for idea leader
        userService.increaseNoOfIdeasLeads(idea.getPostedBy());

        return idea;
    }

    @Override
    public Idea update(Idea idea) {
        idea = createOrUpdate(idea);
        logger.infof("updated idea '%s' ...", idea);
        return idea;
    }

    public Idea createOrUpdate(Idea idea) {
        idea = em.merge(idea);
        return idea;
    }

    public boolean isDiscarded(Idea idea) {
        if (null == idea) {
            throw new IllegalArgumentException("Idea argument must not be null");
        }
        return null != idea.getDiscardEvent();
    }

    public Idea getIdeaFromComment(Comment comment) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> query = cb.createQuery(Idea.class);
        Root<Idea> root = query.from(Idea.class);
        Join<Idea, Comment> join = root.join(Idea_.comments);
        query.where(join.in(comment));
        List<Idea> resultList = em.createQuery(query).setMaxResults(1).getResultList();
        if (resultList.size() > 0) {
            return resultList.get(0);
        }
        return null;
    }

    public List<Idea> getForPIPNNotifications(int offset, int count, User user) {
        StringBuilder jpql = new StringBuilder("SELECT i FROM Notification n LEFT JOIN n.idea i WHERE n.user=:user AND creationDate > :lastWeek");
        TypedQuery<Idea> q = em.createQuery(jpql.toString(), Idea.class);
        q.setParameter("user", user);
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        q.setParameter("lastWeek", cal.getTime());
        List<Idea> ret = q.setFirstResult(offset).setMaxResults(count).getResultList();
        return ret;
    }

    //
    public List<Idea> getForUINNotifications(int offset, int count, User user) {
        StringBuilder jpql = new StringBuilder("SELECT i FROM Notification n LEFT JOIN n.idea i WHERE n.user=:user AND creationDate > :lastWeek");
        TypedQuery<Idea> q = em.createQuery(jpql.toString(), Idea.class);
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        q.setParameter("user", user);
        q.setParameter("lastWeek", cal.getTime());
//        q.setParameter("dType","UIN");
        List<Idea> ret = q.setFirstResult(offset).setMaxResults(count).getResultList();
        return ret;
    }

    public void notifyIdeaFollowersAboutIdeaUpdates(Idea theIdea) {
        List<User> ideaFollowers = userService.getAllUsersByGivenFollowedIdea(theIdea, Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (User user : ideaFollowers) {
            participateNotifier.addParticipationNotification(theIdea, user, NotificationType.IDEA_UPDATE);
        }
    }

    public List<Idea> getAllIdeasByDescendingCreationDateAndPostedBy(int first, int count, User postedBy, EntitiySortingCriteria sortingCriteria) {
        logger.infof("loading %d ideas starting from %d ordered by ascending Creation date and postedBy %d...", count, first,
                postedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        // criteria.where(root.get(Campaign_.id).in(ids));
        criteria.where(cb.equal(root.get(Idea_.postedBy), postedBy));

        if (sortingCriteria.equals(EntitiySortingCriteria.created)) {
            criteria.orderBy(cb.desc(root.get(Idea_.postedAt)));
        } else {
            criteria.orderBy(cb.desc(root.get(Idea_.lastModified)));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public List<Idea> getAllSelectedIdeasByDescendingCreationDateAndPostedBy(int first, int count, User postedBy) {
        logger.infof("loading %d selected ideas starting from %d ordered by ascending Creation date and postedBy %d...", count, first,
                postedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Idea_.deleted)));
        if (postedBy != null) {
            predicates.add(cb.equal(root.get(Idea_.postedBy), postedBy));
        }
        predicates.add(cb.equal(root.get(Idea_.ideaPhase), IdeaDevelopmentPhase.Selected));
        predicates.add(cb.equal(root.get(Idea_.isActionCreated), false));

        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        // criteria.orderBy(cb.asc(root.get(Campaign_.name)));
        //criteria.select(root.get(Idea_.id));
        criteria.orderBy(cb.desc(root.get(Idea_.postedAt)));

        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public long countIdeasForUser(User postedBy) {
        logger.infof("counting all ideas for user %d ...", postedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Idea> root = criteria.from(Idea.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Idea_.deleted)));
        if (postedBy != null) {
            predicates.add(cb.equal(root.get(Idea_.postedBy), postedBy));
        }

        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        // criteria.orderBy(cb.asc(root.get(Campaign_.name)));
        criteria.select(root.get(Idea_.id));
        return em.createQuery(criteria).getResultList().size();
    }

    public long countSelectedIdeasForUser(User postedBy) {
        logger.infof("counting all selected ideas for user %d ...", postedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Idea> root = criteria.from(Idea.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Idea_.deleted)));
        if (postedBy != null) {
            predicates.add(cb.equal(root.get(Idea_.postedBy), postedBy));
        }
        predicates.add(cb.equal(root.get(Idea_.ideaPhase), IdeaDevelopmentPhase.Selected));
        predicates.add(cb.equal(root.get(Idea_.isActionCreated), false));

        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        // criteria.orderBy(cb.asc(root.get(Campaign_.name)));
        criteria.select(root.get(Idea_.id));
        return em.createQuery(criteria).getResultList().size();
    }

    public List<Idea> getAllIdeasByDescendingCreationDateAndCommentedBy(int first, int count, User commentedBy) {
        logger.infof("loading %d ideas starting from %d ordered by ascending Creation date and commentedBy %d...", count,
                first, commentedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> queryCommentedBy = cb.createQuery(Idea.class);
        Root<Idea> root = queryCommentedBy.from(Idea.class);
        Join<Idea, Comment> comments = root.join(Idea_.comments);
        Join<Comment, User> postedBy = comments.join(Comment_.postedBy);
        queryCommentedBy.where(cb.equal(postedBy, commentedBy));
        queryCommentedBy.distinct(true);
        return em.createQuery(queryCommentedBy).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public long getCountForAllIdeasByDescendingCreationDateAndCommentedBy(User commentedBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> queryCommentedBy = cb.createQuery(Idea.class);
        Root<Idea> root = queryCommentedBy.from(Idea.class);
        Join<Idea, Comment> comments = root.join(Idea_.comments);
        Join<Comment, User> postedBy = comments.join(Comment_.postedBy);
        queryCommentedBy.where(cb.equal(postedBy, commentedBy));
        queryCommentedBy.distinct(true);
        return em.createQuery(queryCommentedBy).getResultList().size();
    }

    /**
     * @param idea
     * @param user
     * @return
     */
    public long countCommentsForIdeaByUser(Idea idea, User user) {
        logger.infof("counting all comments for idea %s postedBy user %s...", idea, user);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Idea> root = criteria.from(Idea.class);
        Join<Idea, Comment> join = root.join(Idea_.comments);

        Join<Comment, User> postedBy = join.join(Comment_.postedBy);

        criteria.select(cb.countDistinct(join))
                .where(cb.and(cb.equal(postedBy, user),
                        cb.equal(root.get(Idea_.id), idea.getId())));

        return em.createQuery(criteria).getSingleResult();
    }

    public long countTotalLikesForIdeaCreatedByUser(User user) {
        logger.infof("counting all likes for idea postedBy user %d ...",
                user);
        long returnVal = 0;
        for (Idea idea : getAllIdeasCreatedByUser(user)) {
            returnVal += idea.getNoOfUpVotes();
        }

        return returnVal;
    }

    /**
     * This method counts likes given by user for all ideas.
     * @param user
     * @return
     */
    public long countTotalLikesGivenByUserForAllIdeas(User user) {
        logger.infof("counting all likes by given user %s for all ideas  ...", user);

        long returnVal = 0;
        for (Idea idea : getAll()) {
            if (idea.getUpVotes().contains(user.getId())) {
                returnVal += 1;
            }
        }

        return returnVal;
    }

    /**
     * This method counts likes for all ideas.
     * @param
     * @return
     */
    public long countTotalLikesForAllIdeas() {
        logger.infof("counting all likes for all ideas...");

        long returnVal = 0;
        for (Idea idea : getAll()) {
            returnVal += idea.getNoOfUpVotes();
        }

        return returnVal;
    }

    /**
     * This method counts votes for all ideas.
     * @param
     * @return
     */
    public long countTotalVotesForAllIdeas() {
        logger.infof("counting all votes for all ideas...");

        long returnVal = 0;
        for (Idea idea : getAll()) {
            returnVal += idea.getPrioritisationDotVotes().size();
        }

        return returnVal;
    }

    public List<Idea> getAllIdeasCreatedByUser(User postedBy) {
        logger.infof("getting all ideas postedBy user %d ...", postedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Idea_.deleted)));
        if (postedBy != null) {
            predicates.add(cb.equal(root.get(Idea_.postedBy), postedBy));
        }
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * This will return all leaders from all ideas
     * @return
     */
    public List<User> getAllIdeaLeadersByAscNickName() {
        logger.infof("loading leaders for all ideas ordered by ascending users' nick name...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Idea> root = criteria.from(Idea.class);
        Join<Idea, User> join = root.join(Idea_.postedBy);
        criteria.select(join);
        criteria.orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * This will return all leaders from all ideas
     * @return
     */
    public List<User> getIdeaLeadersForGivenIdeasByAscNickName(Collection<Idea> ideas) {
        logger.infof("loading leaders for given ideas ordered by ascending users' nick name...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Idea> root = criteria.from(Idea.class);
        Join<Idea, User> join = root.join(Idea_.postedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();

        if (ideas != null && !ideas.isEmpty()) {
            predicates.add(root.in(ideas));
        }

        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        criteria.select(join).orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    public List<Idea> getAllActiveIdeas(int first, int count) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> idea = criteria.from(Idea.class);

        criteria.where(cb.not(idea.get(Idea_.deleted)));
        criteria.orderBy(cb.desc(idea.get(Idea_.postedAt)));
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public int countAllActiveIdeas() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> idea = criteria.from(Idea.class);

        criteria.where(cb.not(idea.get(Idea_.deleted)));
        criteria.orderBy(cb.desc(idea.get(Idea_.postedAt)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    public void notifyIdeaFollowersAboutIdeaCommentsAndLikes(Idea theIdea) {
        // at first check if total comments and likes exceeds certain threshold, ie in multiple of 10.
        int totalComments = 0, totalLikes = 0;
        int threshold;
        if (!theIdea.getComments().isEmpty() && theIdea.getComments() != null) {
            totalComments = theIdea.getComments().size();
        }
        if (!theIdea.getUpVotes().isEmpty() && theIdea.getUpVotes() != null) {
            totalLikes = theIdea.getNoOfUpVotes();
        }

        threshold = (totalComments + totalLikes) % 10;
        if (totalComments != 0 || totalLikes != 0) {
            if (threshold == 0) {
                List<User> campaignFollowers = userService.getAllUsersByGivenFollowedIdea(theIdea, Integer.MAX_VALUE, Integer.MAX_VALUE);
                for (User user : campaignFollowers) {
                    participateNotifier.addParticipationNotification(theIdea, user, NotificationType.IDEA_COMMENTS_LIKES);
                }
            }
        }
    }
}
