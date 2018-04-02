package de.atb.socratic.web.components.resource.header;

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

import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.CampaignType;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.web.components.InnovationStatusPanel;
import de.atb.socratic.web.components.facebook.share.FacebookSharePanel;
import de.atb.socratic.web.components.linkedin.share.LinkedInSharePanel;
import de.atb.socratic.web.components.resource.ChallengePictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.inception.campaign.CampaignActionsPanel;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 * @param <T>
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class CommonResourceHeaderPanel<T> extends GenericPanel<T> {

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // inject the EJB for managing ideas
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    @EJB
    IdeaService ideaService;

    private Campaign challenge;
    private NonCachingImage challengeProfilePicture;

    public CommonResourceHeaderPanel(final String id, final IModel<T> model) {
        super(id, model);
        this.setOutputMarkupId(true);

        this.challenge = (Campaign) model.getObject();

        // challenge picture
        add(newChallengeProfilePicturePreview(challenge));

        // challenge title
        add(new Label("title", new PropertyModel<String>(challenge, "name")));

        //status
        InnovationStatusPanel innovationStatusPanel = new InnovationStatusPanel("status", new Model<>(challenge));
        innovationStatusPanel.setVisible(challenge.getCampaignType() != CampaignType.FREE_FORM);
        add(innovationStatusPanel);

        // add notification modal for stopping campaign
        add(getStopNotificationModal());

        add(getCampaignActionsPanel());

        add(addFacebookSharePanel());

        add(addLinkedInSharePanel());

        // followers button
        final AjaxLink<Void> addFollowersLink = newAddFollowersLink();
        add(addFollowersLink);
    }

    protected NonCachingImage newChallengeProfilePicturePreview(Campaign challenge) {
        challengeProfilePicture = new NonCachingImage("profilePicturePreview", ChallengePictureResource.get(PictureType.PROFILE, challenge));
        challengeProfilePicture.setOutputMarkupId(true);
        return challengeProfilePicture;
    }

    public abstract AjaxLink<Void> newAddFollowersLink();

    public abstract CampaignActionsPanel getCampaignActionsPanel();

    public abstract FacebookSharePanel<?> addFacebookSharePanel();

    public abstract LinkedInSharePanel<?> addLinkedInSharePanel();

    public abstract Modal getStopNotificationModal();

}
