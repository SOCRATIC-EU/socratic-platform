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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;

/**
 * An almost complete copy of Wickets org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel.
 * Overriding the original lazy load panel is needed since the nested item which is to be replaced of type
 * <code>div</code> causes the new-line break; This implementation uses a <code>span</code> tag instead.<br/>
 *
 * @author ATB
 * @since 1.3
 */
public abstract class AjaxLazyLoadSpanPanel extends Panel {
    private static final long serialVersionUID = 1L;

    /**
     * The component id which will be used to load the lazily loaded component.
     */
    public static final String LAZY_LOAD_COMPONENT_ID = "content";

    // state,
    // 0:add loading component
    // 1:loading component added, waiting for ajax replace
    // 2:ajax replacement completed
    private byte state = 0;

    /**
     * Constructor
     *
     * @param id
     */
    public AjaxLazyLoadSpanPanel(final String id) {
        this(id, null);
    }

    /**
     * Constructor
     *
     * @param id
     * @param model
     */
    public AjaxLazyLoadSpanPanel(final String id, final IModel<?> model) {
        super(id, model);

        setOutputMarkupId(true);

        add(new AbstractDefaultAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void respond(final AjaxRequestTarget target) {
                if (state < 2) {
                    Component component = getLazyLoadComponent(LAZY_LOAD_COMPONENT_ID);
                    AjaxLazyLoadSpanPanel.this.replace(component);
                    setState((byte) 2);
                    AjaxLazyLoadSpanPanel.this.onComponentLoaded(component, target);
                }
                target.add(AjaxLazyLoadSpanPanel.this);

            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                AjaxLazyLoadSpanPanel.this.updateAjaxAttributes(attributes);
            }

            @Override
            public void renderHead(final Component component, final IHeaderResponse response) {
                super.renderHead(component, response);
                if (state < 2) {
                    CharSequence js = getCallbackScript(component);
                    handleCallbackScript(response, js, component);
                }
            }
        });
    }

    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
    }

    /**
     * Allows subclasses to change the callback script if needed.
     *
     * @param response       the current response that writes to the header
     * @param callbackScript the JavaScript to write in the header
     * @param component      the component which produced the callback script
     */
    protected void handleCallbackScript(final IHeaderResponse response,
                                        final CharSequence callbackScript, final Component component) {
        response.render(OnDomReadyHeaderItem.forScript(callbackScript));
    }

    /**
     * @see org.apache.wicket.Component#onBeforeRender()
     */
    @Override
    protected void onBeforeRender() {
        if (state == 0) {
            add(getLoadingComponent(LAZY_LOAD_COMPONENT_ID));
            setState((byte) 1);
        }
        super.onBeforeRender();
    }

    /**
     * @param state
     */
    private void setState(final byte state) {
        this.state = state;
        getPage().dirty();
    }

    /**
     * @param markupId The components markupid.
     * @return The component that must be lazy created. You may call setRenderBodyOnly(true) on this
     * component if you need the body only.
     */
    public abstract Component getLazyLoadComponent(String markupId);

    /**
     * Called when the placeholder component is replaced with the lazy loaded one.
     *
     * @param target    The Ajax request handler
     * @param component The lazy loaded component.
     */
    protected void onComponentLoaded(Component target, AjaxRequestTarget component) {
    }

    /**
     * @param markupId The components markupid.
     * @return The component to show while the real component is being created.
     */
    public Component getLoadingComponent(final String markupId) {
        IRequestHandler handler = new ResourceReferenceRequestHandler(
                AbstractDefaultAjaxBehavior.INDICATOR);
        return new Label(markupId, "<img alt=\"Loading...\" src=\"" +
                RequestCycle.get().urlFor(handler) + "\"/>").setEscapeModelStrings(false);
    }

}
