/**
 *
 */
package de.atb.socratic.web.search;

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

import de.atb.socratic.web.BasePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class SearchResultsPage extends BasePage {

    /**
     *
     */
    private static final long serialVersionUID = -5345591283885239073L;

    public static final String QUERY_PARAM = "query";

    // panel for displaying list of existing campaigns
    private final ChallengeSearchResultsPanel challengeSearchResultsPanel;

    // shows search results for ideas
    private final IdeaSearchResultsPanel ideaSearchResultsPanel;

    // shows search results for people
    private final UserSearchResultsPanel userSearchResultsPanel;

    // shows search results for action
    private final ActionSearchResultsPanel actionSearchResultsPanel;

    public SearchResultsPage(PageParameters parameters) {
        super(parameters);

        // retrieve the search query from page parameters
        String query = parameters.get(QUERY_PARAM).toString(null);
        add(new Label("queryText", query));

        // add panel with list of campaigns
        challengeSearchResultsPanel = new ChallengeSearchResultsPanel("challengeSearchResultsPanel");
        // filter campaign list by search query
        challengeSearchResultsPanel.setSearchQuery(query);
        add(challengeSearchResultsPanel);

        // add panel with search results for ideas
        ideaSearchResultsPanel = new IdeaSearchResultsPanel("ideaSearchResultsPanel");
        ideaSearchResultsPanel.setSearchQuery(query);
        add(ideaSearchResultsPanel);

        // add panel with search results for users
        userSearchResultsPanel = new UserSearchResultsPanel("userSearchResultsPanel");
        userSearchResultsPanel.setSearchQuery(query);
        add(userSearchResultsPanel);

        // add panel with search results for actions
        actionSearchResultsPanel = new ActionSearchResultsPanel("actionSearchResultsPanel");
        actionSearchResultsPanel.setSearchQuery(query);
        add(actionSearchResultsPanel);
    }

}
