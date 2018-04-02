package de.atb.socratic.web.selection;

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

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.votes.VoteType;
import de.atb.socratic.model.votes.Votes;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.inception.ScopeService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.rating.RatingPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.io.IClusterable;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class VotingPanel extends GenericPanel<Idea> {

    private static final long serialVersionUID = 8120483250443605537L;

    @EJB
    IdeaService ideaService;

    @EJB
    UserService userService;

    @Inject
    ParticipateNotificationService participateNotifier;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    ScopeService scopeService;

    // how many ideas do we show per page
    private static final int itemsPerPage = 10;
    private NonCachingImage voteIconPicture;
    private Idea idea;
    int relevanceRating;
    int feasibilityRating;

    public VotingPanel(String id, IModel<Idea> model, final ModalWindow window) {
        this(id, null, model, window);
    }

    public VotingPanel(String id, final StyledFeedbackPanel feedbackPanel,
                       IModel<Idea> model, final ModalWindow window) {
        super(id, model);

        this.idea = model.getObject();
        List<Idea> ideas = new LinkedList<Idea>();
        ideas.add(idea);
        DataView<Idea> ideaGroups = new DataView<Idea>("ideas", new ListDataProvider<>(ideas),
                itemsPerPage) {

            private static final long serialVersionUID = 9045211351724312375L;

            @Override
            protected void populateItem(Item<Idea> item) {
                final Idea idea = item.getModelObject();

                // for relevance
                //int relevanceRating = 0, feasibilityRating = 0;
                if (idea.getPrioritisationDotVote(loggedInUser) != null) {
                    relevanceRating = idea.getPrioritisationDotVote(loggedInUser).getRelevanceVote();
                    feasibilityRating = idea.getPrioritisationDotVote(loggedInUser).getFeasibilityVote();
                }

                RatingModel relevanceRatingModel = new RatingModel(relevanceRating, VoteType.relevance.toString());
                Label relevanceLabel = new Label("votingStarNum1",
                        new PropertyModel<Double>(relevanceRatingModel, "getRelevanceRating"));
                relevanceLabel.setOutputMarkupId(true);
                relevanceLabel.setVisible(false);
                item.add(newVotingFragment(idea, relevanceRatingModel, "relevance",
                        "showRelevanceVotingFragment", "relevanceVotingPanel"));
                item.add(relevanceLabel);
                item.add(newIdeaProfilePicturePreview("voteRelevancyIconPicture", "img/Relevancy.png"));

                // For feasibility  
                RatingModel feasibilityRatingModel = new RatingModel(feasibilityRating, VoteType.feasibility.toString());
                Label feasibilityLabel = new Label("votingStarNum2",
                        new PropertyModel<Double>(feasibilityRatingModel, "getFeasibilityRating"));
                feasibilityLabel.setOutputMarkupId(true);
                feasibilityLabel.setVisible(false);
                item.add(newVotingFragment(idea, feasibilityRatingModel, "feasibility",
                        "showFeasibilityVotingFragment", "feasibilityVotingPanel"));
                item.add(feasibilityLabel);
                item.add(newIdeaProfilePicturePreview("voteFeasibilityIconPicture", "img/Feasibility.png"));
            }

        };

        add(newSelectionVotingButton(window));
        add(newSelectionVotingCancelButton(window));
        add(ideaGroups);
    }

    protected abstract void onVotingButtonClicked(AjaxRequestTarget target);

    protected NonCachingImage newIdeaProfilePicturePreview(String imageWicketId, String imagePath) {
        voteIconPicture = new NonCachingImage(imageWicketId, new PackageResourceReference(VotingPanel.class, imagePath));
        voteIconPicture.setOutputMarkupId(true);
        return voteIconPicture;
    }

    /**
     * @param window
     * @return
     */
    private AjaxLink<String> newSelectionVotingButton(final ModalWindow window) {

        AjaxLink<String> editLink = new AjaxLink<String>("saveVoteButton") {
            private static final long serialVersionUID = 647877101758774046L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                Votes vote = new Votes(relevanceRating, feasibilityRating);
                ideaService
                        .addorUpdateDotVote(idea, loggedInUser, vote, "");

                // once user has voted, add user to followers list
                if (!userService.isUserFollowsGivenIdea(idea, loggedInUser.getId())) {
                    // Add idea to list of ideas followed for loggedInUser
                    loggedInUser = userService.addIdeaToFollowedIdeasList(idea, loggedInUser.getId());

                    // send him/her notification that they now becomes follower of this idea.
                    participateNotifier.addParticipationNotification(idea, loggedInUser, NotificationType.IDEA_FOLLOWED);
                }

                onVotingButtonClicked(target);
                window.close(target);
            }
        };

        return editLink;
    }

    /**
     * @param window
     * @return
     */
    private AjaxLink<String> newSelectionVotingCancelButton(final ModalWindow window) {

        AjaxLink<String> editLink = new AjaxLink<String>("cancelButton") {
            private static final long serialVersionUID = 647877101758774046L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                onVotingButtonClicked(target);
                window.close(target);
            }
        };

        return editLink;
    }

    private Fragment newVotingFragment(final Idea idea,
                                       final RatingModel ratingModel, String fragmentId, String fragmentMarkupId, String ratingPanelId) {

        final Fragment showVotingFragment = new Fragment(fragmentId,
                fragmentMarkupId, this);
        showVotingFragment.setOutputMarkupId(true);
        RatingPanel p = new RatingPanel(ratingPanelId,
                new PropertyModel<Integer>(ratingModel, "rating"), 5, null, false) {

            private static final long serialVersionUID = -4520654999230191343L;

            @Override
            protected boolean onIsStarActive(int star) {
                return ratingModel.isActive(star);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(isButtonEnabled());
            }

            @Override
            protected void onRated(int rating, AjaxRequestTarget target) {
                ratingModel.addRating(rating);
                target.add(showVotingFragment);
            }
        };
        p.setRatingLabelVisible(true);
        showVotingFragment.add(p);

        return showVotingFragment;
    }

    /**
     * Rating model for storing the ratings, typically this comes from a
     * database.
     */
    public class RatingModel implements IClusterable {

        private static final long serialVersionUID = -7181296497895432958L;
        private double rating;
        private String tag;

        public RatingModel(int votes, String tag) {
            this.rating = votes;
            this.tag = tag;
        }

        public RatingModel(double votes, String tag) {
            this.rating = votes;
            this.tag = tag;
        }

        public boolean isActive(int star) {
            return star < ((int) (rating + 0.5));
        }

        public void addRating(int nrOfStars) {
            rating = (double) nrOfStars;

            if (tag.compareToIgnoreCase(VoteType.relevance.toString()) == 0) {
                relevanceRating = nrOfStars;
            } else if (tag.compareToIgnoreCase(VoteType.feasibility.toString()) == 0) {
                feasibilityRating = nrOfStars;
            }
        }

        public Double getRating() {
            if (Double.isNaN(rating)) {
                return 0.0;
            } else {
                return rating;
            }
        }
    }

    private boolean isButtonEnabled() {
        return true;
    }

}
