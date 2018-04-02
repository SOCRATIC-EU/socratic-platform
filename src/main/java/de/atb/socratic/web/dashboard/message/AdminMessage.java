package de.atb.socratic.web.dashboard.message;

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

import de.atb.socratic.model.Message;
import de.atb.socratic.service.inception.MessageService;
import de.atb.socratic.web.dashboard.Dashboard;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class AdminMessage extends Dashboard {
    private static final long serialVersionUID = -5559578453943490669L;

    protected Message theMessage;

    @EJB
    MessageService messageService;

    public AdminMessage(final PageParameters parameters) {
        super(parameters);

        add(new AjaxLink<Void>("inbox") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(AdminMessageInboxPage.class, new PageParameters());
            }
        });

        add(new AjaxLink<Void>("sent") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(AdminMessageSentPage.class, new PageParameters());
            }
        });
    }


}
