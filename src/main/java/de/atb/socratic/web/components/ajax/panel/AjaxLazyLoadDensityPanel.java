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

import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public abstract class AjaxLazyLoadDensityPanel<N extends Number> extends AjaxLazyLoadSpanPanel {

    private static final long serialVersionUID = 2160119194853620483L;

    private DensityPanel<N> densityPanel;
    private IconType iconType;
    private IModel<String> toolTip;
    private IModel<String> cssClass;

    public AjaxLazyLoadDensityPanel(String id, IModel<String> toolTip) {
        this(id, toolTip, "bar-chart");
    }

    public AjaxLazyLoadDensityPanel(String id, IModel<String> toolTip, IconType iconType) {
        super(id);
        this.iconType = iconType;
        this.toolTip = toolTip;
    }

    public AjaxLazyLoadDensityPanel(String id, IModel<String> toolTip, String iconType) {
        this(id, toolTip, new IconType(iconType));
    }

    public AjaxLazyLoadDensityPanel<N> setIcon(IconType iconType) {
        this.iconType = iconType;
        return this;
    }

    public AjaxLazyLoadDensityPanel<N> setIcon(String iconName) {
        this.iconType = new IconType(iconName);
        return this;
    }

    /**
     * @param cssClass the cssClass to add
     */
    public AjaxLazyLoadDensityPanel<N> addCssClass(IModel<String> cssClass) {
        this.cssClass = cssClass;
        return this;
    }

    @Override
    public Component getLazyLoadComponent(String markupId) {
        N number = getDensity();
        densityPanel = new DensityPanel<N>(markupId, Model.of(number)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                AjaxLazyLoadDensityPanel.this.onClick(target);
            }

        };
        densityPanel.setToolTip(toolTip);
        densityPanel.setIcon(this.iconType);
        densityPanel.add(new AttributeAppender("class", cssClass));
        return densityPanel;
    }

    public abstract N getDensity();

    public abstract void onClick(AjaxRequestTarget target);

}
