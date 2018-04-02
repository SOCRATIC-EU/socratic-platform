/**
 *
 */
package de.atb.socratic.web.learningcenter;

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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class LearningCenter extends BasePage {
    private static final long serialVersionUID = -5559578453943490669L;

    public LearningCenter(PageParameters parameters) {
        super(parameters);

        add(new AjaxLink<Void>("presentation") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(LearningCenterPresentation.class);
            }
        });

        add(new AjaxLink<Void>("defineChallenge") {
            private static final long serialVersionUID = -6633994061963892062L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(LearningCenterDefiningChallengePage.class);
            }
        });

        add(new AjaxLink<Void>("developIdeas") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(LearningCenterDevelopingIdeasPage.class);
            }
        });

        add(new AjaxLink<Void>("ideasToAction") {
            private static final long serialVersionUID = -3486178522694616314L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(LearningCenterIdeasToProjectsPage.class);
            }
        });

        add(new AjaxLink<Void>("creatingImpact") {
            private static final long serialVersionUID = -3486178522694616314L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(LearningCenterCreatingImpactPage.class);
            }
        });
    }
}
