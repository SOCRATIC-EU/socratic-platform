/**
 *
 */
package de.atb.socratic.web.inception;

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

import java.util.Iterator;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class SimilarCampaignsPanel extends Panel {

    /**
     *
     */
    private static final long serialVersionUID = -1249664628206962888L;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    /**
     * @param id
     */
    public SimilarCampaignsPanel(final String id) {
        super(id);

        add(new DataView<Campaign>("similarCampaigns",
                new SimilarCampaignProvider()) {
            private static final long serialVersionUID = 327518374058476100L;

            @Override
            protected void populateItem(final Item<Campaign> item) {
                item.add(new SimilarCampaignPanel("similarCampaign", Model
                        .of(item.getModelObject()), item.getIndex() + 1));
            }
        });
    }

    /**
     * @author ATB
     */
    private final class SimilarCampaignProvider extends
            EntityProvider<Campaign> {
        private static final long serialVersionUID = -134223904700379066L;

        // inject the EJB for managing campaigns
        // use @EJB annotation to inject EJBs to have proper proxying that works
        // with Wicket page store
        @EJB
        CampaignService campaignService;

        @Override
        public Iterator<? extends Campaign> iterator(long first, long count) {
            return campaignService.getSimilarCampaigns(loggedInUser).iterator();
        }

        @Override
        public long size() {
            return campaignService.countSimilar(loggedInUser);
        }

    }

}
