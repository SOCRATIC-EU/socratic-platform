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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * @author ATB
 */
public class Effects {

    /**
     * Prepends given {@code component} as HTML element given in
     * {@code componentHtmlTag} to the children of HTML element specified by
     * {@code containerMarkupId}.
     *
     * @param target
     * @param component
     * @param componentHtmlTag
     * @param containerMarkupId
     */
    public static void prependAndFadeIn(AjaxRequestTarget target,
                                        Component component, String componentHtmlTag,
                                        String containerMarkupId) {
        addAndFadeIn(target, component, JSTemplates.PREPEND_ELEM_TEMPLATE,
                componentHtmlTag, containerMarkupId);
    }

    /**
     * Appends given {@code component} as HTML element given in
     * {@code componentHtmlTag} to the children of HTML element specified by
     * {@code containerMarkupId}.
     *
     * @param target
     * @param component
     * @param componentHtmlTag
     * @param containerMarkupId
     */
    public static void appendAndFadeIn(AjaxRequestTarget target,
                                       Component component, String componentHtmlTag,
                                       String containerMarkupId) {
        addAndFadeIn(target, component, JSTemplates.APPEND_ELEM_TEMPLATE,
                componentHtmlTag, containerMarkupId);
    }

    /**
     * @param target
     * @param component
     * @param jsAddTemplate
     * @param componentHtmlTag
     * @param containerMarkupId
     */
    private static void addAndFadeIn(AjaxRequestTarget target,
                                     Component component, String jsAddTemplate, String componentHtmlTag,
                                     String containerMarkupId) {
        // add 'display:none' so that it is initially hidden
        component.add(new DisplayNoneBehavior());
        // add JS to append or prepend component to container html and hide it
        // initially
        target.prependJavaScript(String.format(jsAddTemplate, componentHtmlTag,
                component.getMarkupId(), containerMarkupId));
        target.add(component);// add JS to fade in the new idea
        target.appendJavaScript(String.format(
                JSTemplates.FADE_IN_ELEM_TEMPLATE, component.getMarkupId()));
    }

    /**
     * Replaces the given {@code component} in HTML using a fade out fade in
     * effect.
     *
     * @param target
     * @param component
     */
    public static void replaceWithFading(AjaxRequestTarget target,
                                         Component component) {
        component.add(new DisplayNoneBehavior());
        target.prependJavaScript(String.format(
                JSTemplates.FADE_OUT_ELEM_TEMPLATE, component.getMarkupId()));
        target.add(component);
        target.appendJavaScript(String.format(
                JSTemplates.FADE_IN_ELEM_TEMPLATE, component.getMarkupId()));
    }

    /**
     * Replaces the given {@code component} in HTML using a slide up slide down
     * effect.
     *
     * @param target
     * @param component
     */
    public static void replaceWithSliding(AjaxRequestTarget target,
                                          Component component) {
        component.add(new DisplayNoneBehavior());
        target.prependJavaScript(String.format(
                JSTemplates.SLIDE_UP_ELEM_TEMPLATE, component.getMarkupId()));
        target.add(component);
        target.appendJavaScript(String.format(
                JSTemplates.SLIDE_DOWN_ELEM_TEMPLATE, component.getMarkupId()));
    }

    /**
     * @param target
     * @param markupId
     */
    public static void fadeOutElem(AjaxRequestTarget target, String markupId) {
        target.appendJavaScript(String.format(JSTemplates.FADE_OUT_ELEM_TEMPLATE, markupId));
    }

    /**
     * @param target
     * @param markupId
     */
    public static void fadeOutAndRemove(AjaxRequestTarget target,
                                        String markupId) {
        target.appendJavaScript(String.format(
                JSTemplates.FADE_OUT_AND_REMOVE_ELEM_TEMPLATE, markupId));
    }

    /**
     * @param target
     * @param markupId
     */
    public static void showModal(AjaxRequestTarget target, String markupId) {
        target.appendJavaScript("$('#" + markupId + "').modal('show');");
    }

    /**
     * @param target
     * @param markupId
     */
    public static void hideModal(AjaxRequestTarget target, String markupId) {
        target.appendJavaScript("$('#" + markupId + "').modal('hide');");
    }

}
