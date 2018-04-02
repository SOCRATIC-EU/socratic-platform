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

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.components.ProgressBar;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.IdeaActivity;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.rating.RatingPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.io.IClusterable;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class IdeaVotingPanel extends GenericPanel<Idea> {

    /**
     *
     */
    private static final long serialVersionUID = -522989596052194414L;

    @Inject
    Logger logger;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    ActivityService activityService;

    private ModalWindow dotModalWindow;

    public Label totalNoOfVotesLabel;
    public Label overAllRatingLabel;

    private VotingPanel votingPanel;

    private Idea idea;

    private NonCachingImage voteIconPicture;

    private ProgressBar relevancyStatusBar, feasibilityStatusBar;
    private Label relevancyStatusTextlabel, feasibilityStatusTextlabel;
    private RatingPanel ratingPanelForFinalVotes;
    private Form form;
    private RatingModel totalVotes;

    /**
     * Initializes the IdeaVotingPanel.
     *
     * @param id
     * @param model             define in which innovation phase this panel is used
     * @param votingLinkVisible
     */
    public IdeaVotingPanel(final String id, final IModel<Idea> model, final boolean votingLinkVisible) {
        super(id, model);

        setOutputMarkupId(true);
        this.idea = getModelObject();

        form = new Form<>("form");
        add(form);
        // add idea data
        form.add(newDotVoteLink("dot", model, votingLinkVisible));

        form.add(this.dotModalWindow = new ModalWindow("myDotModal") {

            private static final long serialVersionUID = -6118683848343086655L;

            @Override
            public void show(AjaxRequestTarget target) {
                super.show(target);
                setOutputMarkupId(true);
                target.appendJavaScript(""//
                        + "var thisWindow = Wicket.Window.get();\n"
                        + "if (thisWindow) {\n"
                        + "thisWindow.window.style.width = \"1200px\";\n"
                        + "thisWindow.content.style.height = \"1000px\";\n"
                        + "thisWindow.center();\n" + "}");
            }
        });
        dotModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {

            private static final long serialVersionUID = -9143847141081283640L;

            @Override
            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                return true;
            }
        });
        dotModalWindow.setAutoSize(false);
        dotModalWindow.setResizable(false);

        // count total no of votes
        // total votes would be avg of all 3 types of votes from all users....
        int voteSize = getTotalNoVotes(idea);
        if (voteSize <= 1) {
            this.totalNoOfVotesLabel = new Label("totalNoOfVotes", String.format("(" + voteSize + " "
                    + getString("totalNoOfVote.label") + ")"));
        } else {
            this.totalNoOfVotesLabel = new Label("totalNoOfVotes", String.format("(" + voteSize + " "
                    + getString("totalNoOfVotes.label") + ")"));
        }
        totalNoOfVotesLabel.setOutputMarkupId(true);
        form.add(totalNoOfVotesLabel);

        // Over all rating
        double rating = getOverAllVotes(idea);
        form.add(this.overAllRatingLabel = new Label("overAllRating", String.format(getString("overAllRating.label") + " " + rating)));
        form.add(this.ratingPanelForFinalVotes = newRatingPanel(idea, rating));

        // Add relevance status: Image & text
        form.add(newIdeaProfilePicturePreview("voteRelevancyIconPicture", "img/Relevancy_GrayBG.png"));

        // Add relevance status: Status bar
        Double ratingRelevance = idea.getPrioritisationDotRelevanceVoteAVG();
        relevancyStatusBar = newProgressBar("relevancyStatusBar", ratingRelevance);
        form.add(relevancyStatusBar);

        // Add relevance status: Status Text
        this.relevancyStatusTextlabel = new Label("relevancyStatusTextlabel", String.format(String.valueOf(ratingRelevance)));
        relevancyStatusTextlabel.setOutputMarkupId(true);
        form.add(relevancyStatusTextlabel);

        // Add feasibility status: Status bar
        Double ratingFeasibility = idea.getPrioritisationDotFeasibilityVoteAVG();
        feasibilityStatusBar = newProgressBar("feasibilityStatusBar", ratingFeasibility);
        form.add(feasibilityStatusBar);

        // Add feasibility status: Status Text
        this.feasibilityStatusTextlabel = new Label("feasibilityStatusTextlabel", String.format(String
                .valueOf(ratingFeasibility)));
        feasibilityStatusTextlabel.setOutputMarkupId(true);
        form.add(feasibilityStatusTextlabel);

        form.add(newIdeaProfilePicturePreview("voteFeasibilityIconPicture", "img/Feasibility_GrayBG.png"));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        dotModalWindow.setTitle(
                new StringResourceModel("vote.idea.title", this, null).getString()
                        + (loggedInUser != null ? " " + loggedInUser.getNickName() : ""));
    }

    private ProgressBar newProgressBar(String wicketId, Double rating) {
        rating = Idea.round(rating * 100, 5);
        ProgressBar statusBar = new ProgressBar(wicketId, Model.of(rating.intValue()));
        statusBar.setOutputMarkupId(true);
        statusBar.value(Model.of(rating.intValue()));
        return statusBar;
    }

    protected NonCachingImage newIdeaProfilePicturePreview(String imageWicketId, String imagePath) {
        voteIconPicture = new NonCachingImage(imageWicketId, new PackageResourceReference(VotingPanel.class, imagePath));
        voteIconPicture.setOutputMarkupId(true);
        return voteIconPicture;
    }

    private int getTotalNoVotes(Idea idea) {
        return idea.getPrioritisationDotVoteUserList().size();
    }

    private double getOverAllVotes(Idea idea) {
        return idea.getPrioritisationDotVoteAVG();
    }

    public class RatingModel implements IClusterable {

        private static final long serialVersionUID = -7181296497895432958L;

        private double rating;

        public RatingModel(double votes) {
            this.rating = votes;
        }

        public boolean isActive(double star) {
            return star < ((rating));
        }

        public void addRating(int nrOfStars) {
            rating = (double) nrOfStars;
        }

        public Double getRating() {
            if (Double.isNaN(rating)) {
                return 0.0;
            } else {
                return rating;
            }
        }
    }

    private RatingPanel newRatingPanel(final Idea idea, final double total) {
        totalVotes = new RatingModel(total);
        RatingPanel p = new RatingPanel("votingPanel", new PropertyModel<Double>(totalVotes, "rating"), 5, null, false) {

            private static final long serialVersionUID = -4520654999230191343L;

            @Override
            protected boolean onIsStarActive(int star) {
                return totalVotes.isActive(star);
            }

            @Override
            protected void onRated(int rating, AjaxRequestTarget target) {
                totalVotes.addRating(rating);

            }
        };
        p.setRatingLabelVisible(false);
        p.setEnabled(false);
        p.setOutputMarkupId(true);
        return p;
    }

    private AjaxLink<Idea> newDotVoteLink(final String id, final IModel<Idea> model, final boolean visible) {
        return new AjaxLink<Idea>(id, model) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                boolean ifSelectionPhaseIsOver = !model.getObject().getCampaign().getInnovationStatus().equals(InnovationStatus.IMPLEMENTATION);
                boolean shouldVoteButtonDisplayed = loggedInUser != null && ifSelectionPhaseIsOver && visible;
                setVisible(shouldVoteButtonDisplayed);
                setEnabled(shouldVoteButtonDisplayed);
            }

            private static final long serialVersionUID = 1447978241999154618L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(dotModalWindow);
                votingPanel = new VotingPanel(dotModalWindow.getContentId(), getModel(), dotModalWindow) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onVotingButtonClicked(AjaxRequestTarget target) {
                        updateComponents(target);
                        onVotingPerformed(target);
                    }
                };

                dotModalWindow.setContent(votingPanel.setOutputMarkupId(true));
                dotModalWindow.show(target);
            }
        };
    }

    private void updateComponents(AjaxRequestTarget target) {
        int voteSize = getTotalNoVotes(idea);
        if (voteSize <= 1) {
            totalNoOfVotesLabel.setDefaultModelObject(String.format("(" + voteSize + " " + getString("totalNoOfVote.label")
                    + ")"));
        } else {
            totalNoOfVotesLabel.setDefaultModelObject(String.format("(" + voteSize + " " + getString("totalNoOfVotes.label")
                    + ")"));
        }
        target.add(totalNoOfVotesLabel);

        overAllRatingLabel.setDefaultModelObject(String.format(getString("overAllRating.label") + " " + getOverAllVotes(idea)));

        // update relevance status bar
        Double ratingRelevance = idea.getPrioritisationDotRelevanceVoteAVG();
        Double ratingRelevanceUpdated = (ratingRelevance * 100) / 5;
        relevancyStatusBar.value(Model.of(ratingRelevanceUpdated.intValue()));
        target.add(relevancyStatusBar);
        target.add(relevancyStatusTextlabel.setDefaultModelObject(String.format(String.valueOf(ratingRelevance))));

        // update feasibility status bar
        Double ratingFeasibility = idea.getPrioritisationDotFeasibilityVoteAVG();
        Double ratingFeasibilityUpdated = (ratingFeasibility * 100) / 5;
        feasibilityStatusBar.value(Model.of(ratingFeasibilityUpdated.intValue()));
        target.add(feasibilityStatusBar);
        target.add(feasibilityStatusTextlabel.setDefaultModelObject(String.format(String.valueOf(ratingFeasibility))));

        // update ratingPanelForFinalVotes' rating text
        final double total = idea.getPrioritisationDotVoteList() / voteSize;
        totalVotes = new RatingModel(total);
        ratingPanelForFinalVotes.setDefaultModel(new PropertyModel<Double>(totalVotes, "rating"));
        target.add(ratingPanelForFinalVotes);

        // create an activity once user has voted on an idea.
        activityService.create(IdeaActivity.ofVoted(idea, loggedInUser));

        target.add(form);
    }

    protected abstract void onVotingPerformed(AjaxRequestTarget target);
}
