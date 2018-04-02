package de.atb.socratic.web.components.ajax.panel;

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

import de.agilecoders.wicket.markup.html.bootstrap.image.Icon;
import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public abstract class DensityPanel<N extends Number> extends Panel {

    private static final long serialVersionUID = -8775508915202771590L;

    private Icon icon = new Icon("icon", new IconType("bar-chart"));
    private Label density;
    private AjaxLink<Void> link;

    public DensityPanel(String id, IModel<N> model) {
        super(id, model);
        link = new AjaxLink<Void>("link") {
            private static final long serialVersionUID = 5283484081925713839L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                DensityPanel.this.onClick(target);
            }
        };
        density = new Label("density", String.format("%.2f", model.getObject()));
        link.add(density);
        link.add(icon);
        add(link);
    }

    public void setToolTip(IModel<String> toolTip) {
        add(new AttributeModifier("title", toolTip));
    }

    public void setToolTip(String toolTip) {
        add(new AttributeModifier("title", toolTip));
    }

    public DensityPanel<N> setIcon(IconType iconType) {
        icon.setType(iconType);
        return this;
    }

    public DensityPanel<N> setIcon(String iconName) {
        icon.setType(new IconType(iconName));
        return this;
    }

    public abstract void onClick(AjaxRequestTarget target);

}
