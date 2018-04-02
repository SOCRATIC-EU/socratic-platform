/**
 *
 */
package de.atb.socratic.web.components;

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

import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonType;
import de.agilecoders.wicket.markup.html.bootstrap.button.TypedAjaxLink;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * @author ATB
 */
public class ExpandableBlockquotePanel<T> extends Panel {

    private static final long serialVersionUID = -2018737360460490313L;

    private static final String CONTAINER_ID = "container";

    private static final String LABEL_ID = "label";

    private static final String LINK_ID = "link";

    private final WebMarkupContainer container;

    private final IModel<?> containerCSS;

    private final TypedAjaxLink<Void> toggleLink;

    private final IModel<T> shortModel;

    private final IModel<T> longModel;

    private final Label label;

    private final TooltipBehavior expandTooltip = new TooltipBehavior(new StringResourceModel("expand.tooltip.text", this, null));

    private final TooltipBehavior collapseTooltip = new TooltipBehavior(new StringResourceModel("collapse.tooltip.text", this, null));

    private boolean showAll = false;

    public ExpandableBlockquotePanel(final String id, final IModel<T> shortModel, final IModel<T> longModel, final IModel<?> containerCSS) {
        this(id, shortModel, longModel, containerCSS, false);
    }

    /**
     * @param id
     * @param shortModel
     * @param longModel
     * @param containerCSS
     */
    public ExpandableBlockquotePanel(final String id, final IModel<T> shortModel, final IModel<T> longModel, final IModel<?> containerCSS, final boolean escapeHTML) {
        super(id);

        this.setOutputMarkupId(true);

        this.shortModel = shortModel;
        this.longModel = longModel;
        this.containerCSS = containerCSS;
        label = newLabel();
        add(container = newContainer());
        container.add(label.setEscapeModelStrings(escapeHTML));
        add(toggleLink = newToggleLink());
    }

    /*
     *
     */
    private WebMarkupContainer newContainer() {
        final WebMarkupContainer c = new WebMarkupContainer(CONTAINER_ID);
        c.add(new AttributeModifier("class", containerCSS));
        return c;
    }

    /**
     * @return
     */
    private TypedAjaxLink<Void> newToggleLink() {
        TypedAjaxLink<Void> link = new TypedAjaxLink<Void>(LINK_ID, ButtonType.Link) {
            private static final long serialVersionUID = -480060834199110090L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                toggle(target);
            }
        }.setIconType(IconType.plus).setInverted(false);
        link.add(expandTooltip);
        return link;
    }

    /**
     * @return
     */
    private Label newLabel() {
        if (showAll) {
            return new Label(LABEL_ID, longModel);
        } else {
            return new Label(LABEL_ID, shortModel);
        }
    }

    /**
     * @param target
     */
    private void toggle(AjaxRequestTarget target) {
        showAll = !showAll;
        if (showAll) {
            toggleLink.remove(expandTooltip);
            toggleLink.add(collapseTooltip);
            toggleLink.setIconType(IconType.minus);
            label.setDefaultModel(longModel);
        } else {
            toggleLink.remove(collapseTooltip);
            toggleLink.add(expandTooltip);
            toggleLink.setIconType(IconType.plus);
            label.setDefaultModel(shortModel);
        }
        Effects.replaceWithSliding(target, this);
    }

}
