package de.atb.socratic.model.tour;

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

import java.io.Serializable;

import de.atb.socratic.model.User;
import de.atb.socratic.service.votes.ToursService;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;

public class TourHelper implements Serializable {

    private static final long serialVersionUID = -4731597463478059337L;

    public static final String TOUR_IMPLEMENTATION_NAME = "tour_implementation";
    public static final String TOUR_PRIORITISATION_NAME = "tour_prioritisation";
    public static final String TOUR_USERDASHBOARD_NAME = "tour_userdashboard";
    public static final String TOUR_INCEPTION_NAME = "tour_inception";
    public static final String TOUR_USERPROFILE_NAME = "tour_userprofile";


    public String template;

    public TourHelper(Component context) {
        template = "<div class='popover tour' style='width:320px;'>" +
                "<div class='arrow'></div>" +
                "<h3 class='popover-title'></h3>" +
                "<div class='popover-content'></div>" +
                "<div class='popover-navigation'>" +
                "<input type='button' class='btn btn-default' onclick='tour.prev();' value='" + new StringResourceModel("tour.prev", context, null).getString() + "' id='prev'/>" +
                "<span data-role='separator'>&nbsp;</span>" +
                "<input type='button' class='btn btn-default' onclick='tour.next();' value='" + new StringResourceModel("tour.next", context, null).getString() + "' id='next'/>" +
                "<input type='button' style='margin-left:40px;' class='btn btn-default' onclick='tour.end();' value='" + new StringResourceModel("tour.end", context, null).getString() + "'/>" +
                "</div></div>";
    }

    public AbstractDefaultAjaxBehavior getAjaxBehavior(final String name,
                                                       final ToursService toursService,
                                                       final User loggedInUser) {
        return new AbstractDefaultAjaxBehavior() {
            protected void respond(final AjaxRequestTarget target) {
                if (loggedInUser != null) {
                    String paramStep = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("step").toString();
                    String paramEnded = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("ended").toString();
                    toursService.updateTour(loggedInUser.getId(), name, paramStep, Boolean.parseBoolean(paramEnded));
                }
            }
        };
    }

    public String getAjaxCallbackJS(AbstractDefaultAjaxBehavior behave) {
        final CharSequence url = behave.getCallbackUrl();
        return "function callWicket(step, ended) {var wcall = Wicket.Ajax.ajax({'u':'" + url + "', 'ep':[ {'name':'step','value':step}, {'name':'ended','value':ended}]}); }";
    }

    public String createImplementationTour(long size, ToursService toursService, User loggedInUser, Component context) {
        if (toursService.isEnded(loggedInUser.getId(), TOUR_IMPLEMENTATION_NAME)) {
            return null;
        }
        String createTour = new String();

        if (size > 0) {

            createTour = "(function(){" +
                    "tour = new Tour({" +
                    "name: '" + TOUR_IMPLEMENTATION_NAME + "'," +
                    "keyboard: true," +
                    "storage: false," +
                    "debug: true," +
                    "backdrop: false," +
                    "redirect: false," +
                    "orphan: false," +
                    "duration: 20000," +
                    "template: \"" + template + "\"," +
                    "});\n";

            String step1 = "tour.addStep({" +
                    "element: '#project-list > tbody > tr > td:nth-child(2) > a:nth-child(1)',"
                    + "title: '" + new StringResourceModel("tour.1.title", context, null).getString() + "',"
                    + "placement: 'top',"
                    + "content: '" + new StringResourceModel("tour.1.message", context, null).getString() + "'"
                    + "});\n";
            createTour += step1;

            String step2 = "tour.addStep({" +
                    "element: '#project-list > tbody > tr > td:nth-child(6) > div > div',"
                    + "title: '" + new StringResourceModel("tour.2.title", context, null).getString() + "',"
                    + "placement: 'top',"
                    + "content: '" + new StringResourceModel("tour.2.message", context, null).getString() + "'"
                    + "});\n";
            createTour += step2;

        }
        createTour += "tour.setCurrentStep(" + toursService.getLastStep(loggedInUser.getId(), TOUR_IMPLEMENTATION_NAME) + ");\n tour.init();\n tour.start();\n}());";
        return createTour;
    }

    public String createUserdashboardTour(long size, ToursService toursService, User loggedInUser, Component context) {
        if (toursService.isEnded(loggedInUser.getId(), TOUR_USERDASHBOARD_NAME)) {
            return null;
        }

        String createTour = "(function(){" +
                "tour = new Tour({" +
                "name: '" + TOUR_USERDASHBOARD_NAME + "'," +
                "keyboard: true," +
                "storage: false," +
                "debug: true," +
                "backdrop: false," +
                "redirect: false," +
                "orphan: false," +
                "duration: 20000," +
                "template: \"" + template + "\"," +
                "});\n";

        if (size > 0) {
            String step1 = "tour.addStep({" +
                    "element: '#ideas-list > tbody > tr:nth-child(1) > td:nth-child(3) > span:nth-child(1)'," +
                    "title: '" + new StringResourceModel("tour.1.title", context, null).getString() + "'," +
                    "placement: 'top'," +
                    "content: '" + new StringResourceModel("tour.1.message", context, null).getString() + "'" +
                    "});\n";
            createTour += step1;
        }
        createTour += "tour.setCurrentStep(" + toursService.getLastStep(loggedInUser.getId(), TOUR_USERDASHBOARD_NAME) + ");\n tour.init();\n tour.start();\n}());";
        return createTour;
    }

    public String createUserProfileTour(ToursService toursService, User loggedInUser, Component context) {
        if (toursService.isEnded(loggedInUser.getId(), TOUR_USERPROFILE_NAME)) {
            return null;
        }

        String createTour = "(function(){" +
                "tour = new Tour({" +
                "name: '" + TOUR_USERPROFILE_NAME + "'," +
                "keyboard: true," +
                "storage: false," +
                "debug: true," +
                "backdrop: false," +
                "redirect: false," +
                "orphan: false," +
                "duration: 20000," +
                "template: \"" + template + "\"," +
                "});\n";

        String step1 = "tour.addStep({" +
                "element: '#user-tour'," +
                "title: '" + new StringResourceModel("tour.1.title", context, null).getString() + "'," +
                "placement: 'top'," +
                "content: '" + new StringResourceModel("tour.1.message", context, null).getString() + "'" +
                "});\n";
        createTour += step1;

        String step2 = "tour.addStep({" +
                "element: '#user-tour-buttons'," +
                "title: '" + new StringResourceModel("tour.2.title", context, null).getString() + "'," +
                "placement: 'right'," +
                "content: '" + new StringResourceModel("tour.2.message", context, null).getString() + "'" +
                "});\n";
        createTour += step2;

        String step3 = "tour.addStep({" +
                "element: '#user-tour-skills'," +
                "title: '" + new StringResourceModel("tour.3.title", context, null).getString() + "'," +
                "placement: 'top'," +
                "content: '" + new StringResourceModel("tour.3.message", context, null).getString() + "'" +
                "});\n";
        createTour += step3;

        String step4 = "tour.addStep({" +
                "title: '" + new StringResourceModel("tour.4.title", context, null).getString() + "'," +
                "element: '#user-tour-buttons'," +
                "placement: 'right'," +
                "content: '" + new StringResourceModel("tour.4.message", context, null).getString() + "'" +
                "});\n";
        createTour += step4;

        String step5 = "tour.addStep({" +
                "title: '" + new StringResourceModel("tour.5.title", context, null).getString() + "'," +
                "element: '#user-tour-password'," +
                "placement: 'top'," +
                "content: '" + new StringResourceModel("tour.5.message", context, null).getString() + "'" +
                "});\n";
        createTour += step5;

        createTour += "tour.setCurrentStep(" + toursService.getLastStep(loggedInUser.getId(), TOUR_USERPROFILE_NAME) + ");\n tour.init();\n tour.start();\n}());";
        return createTour;
    }

    public String createInceptionTour(long size, ToursService toursService, User loggedInUser, Component context) {
        if (loggedInUser == null || toursService.isEnded(loggedInUser.getId(), TOUR_INCEPTION_NAME)) {
            return null;
        }
        String createTour = "(function(){" +
                "tour = new Tour({" +
                "name: '" + TOUR_INCEPTION_NAME + "'," +
                "keyboard: true," +
                "storage: false," +
                "debug: true," +
                "backdrop: false," +
                "redirect: false," +
                "orphan: false," +
                "duration: 20000," +
                "template: \"" + template + "\"," +
                "});\n";

        String step1 = "tour.addStep({" +
                "element: 'ul.pull-right > li:nth-child(3)'," +
                "title: '" + new StringResourceModel("tour.1.title", context, null).getString() + "'," +
                "placement: 'bottom'," +
                "content: '" + new StringResourceModel("tour.1.message", context, null).getString() + "'" +
                "});\n";
        createTour += step1;

        String step2 = "tour.addStep({" +
                "element: 'a#add-wizard'," +
                "title: '" + new StringResourceModel("tour.2.title", context, null).getString() + "'," +
                "placement: 'right'," +
                "content: '" + new StringResourceModel("tour.2.message", context, null).getString() + "'" +
                "});\n";
        createTour += step2;

        if (size > 0) {
            String step3 = "tour.addStep({" +
                    "element: '#campaign-list > tbody > tr:nth-child(1) > td:nth-child(2) > a'," +
                    "title: '" + new StringResourceModel("tour.3.title", context, null).getString() + "'," +
                    "placement: 'top'," +
                    "content: '" + new StringResourceModel("tour.3.message", context, null).getString() + "'" +
                    "});\n";
            createTour += step3;

            String step4 = "tour.addStep({" +
                    "element: '#campaign-list > tbody > tr:nth-child(1) > td:nth-child(3) > div'," +
                    "title: '" + new StringResourceModel("tour.4.title", context, null).getString() + "'," +
                    "placement: 'top'," +
                    "content: '" + new StringResourceModel("tour.4.message", context, null).getString() + "'" +
                    "});\n";
            createTour += step4;

            String step5 = "tour.addStep({" +
                    "element: '#campaign-list > tbody > tr:nth-child(1) > td.actions > div > a:nth-child(1)'," +
                    "title: '" + new StringResourceModel("tour.5.title", context, null).getString() + "'," +
                    "placement: 'top'," +
                    "content: '" + new StringResourceModel("tour.5.message", context, null).getString() + "'" +
                    "});\n";
            createTour += step5;
        }
        createTour += "tour.setCurrentStep(" + toursService.getLastStep(loggedInUser.getId(), TOUR_INCEPTION_NAME) + ");\n tour.init();\n tour.start();\n}());";
        return createTour;
    }
}
