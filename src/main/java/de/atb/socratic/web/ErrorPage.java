/**
 *
 */
package de.atb.socratic.web;

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

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.User;
import de.atb.socratic.service.notification.IssueReportingMailService;
import de.atb.socratic.web.components.TinyMCETextArea;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Generics;

/**
 * @author ATB
 * <p>
 */
public class ErrorPage extends BasePage {

    /**
     *
     */
    private static final long serialVersionUID = -3174223286742145007L;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    @Inject
    IssueReportingMailService issueReportingMailService;

    private final Throwable throwable;

    private String report;

    /**
     *
     */
    public ErrorPage() {
        this(null);
    }

    /**
     * @param throwable
     */
    public ErrorPage(final Throwable throwable) {
        super(new PageParameters());

        this.throwable = throwable;

        // add home page link
        add(new BookmarkablePageLink<Page>("homeLink", getApplication().getHomePage()));

        if (throwable == null) {
            // we are submitting a bug
            add(newLabel("header", "bug.report.header"));
            add(newLabel("message", "bug.report.message"));
        } else {
            // actual error page
            add(newLabel("header", "error.header"));
            add(newLabel("message", "error.message"));
        }

        // add bug report form
        add(newBugReportForm());
    }

    private Label newLabel(final String id, final String property) {
        return new Label(id, new StringResourceModel(property, this, null));
    }

    private Form<Void> newBugReportForm() {
        final Form<Void> form = new Form<Void>("form") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                try {
                    reportIssue(report);

                    // set thank you message and return to home page
                    setResponsePage(
                            getApplication().getHomePage(),
                            new PageParameters()
                                    .set(MESSAGE_PARAM, getString("success.message"))
                                    .set(LEVEL_PARAM, FeedbackMessage.SUCCESS));
                } catch (NotificationException e) {
                    String message = getString("fail.message");
                    logger.error(message, e);
                    getPage().error(message);
                }
            }
        };

        form.add(new TinyMCETextArea("report", new PropertyModel<String>(ErrorPage.this, "report")));

        return form;
    }

    private void reportIssue(final String report) throws NotificationException {
        String errorMessage = getErrorMessage(throwable);
        String stackTrace = getStackTrace(throwable);
        String markup = getMarkUp(throwable);
        String reportedBy = loggedInUser != null
                ? loggedInUser.getNickName() + " (" + loggedInUser.getEmail() + ")"
                : null;
        sendReport(report, errorMessage, stackTrace, markup, reportedBy);
    }

    private void sendReport(final String report, final String errorMessage, final String stackTrace,
                            final String markup, final String reportedBy) throws NotificationException {
        logger.info(report);
        logger.info(reportedBy);
        logger.info(errorMessage);
        logger.info(stackTrace);
        logger.info(markup);

        issueReportingMailService.sendIssueReport(report, errorMessage, stackTrace, markup, reportedBy);
    }

    private String getMarkUp(final Throwable throwable) {
        String resource = "";
        String markup = "";
        MarkupStream markupStream = null;
        if (throwable instanceof MarkupException) {
            markupStream = ((MarkupException) throwable).getMarkupStream();

            if (markupStream != null) {
                markup = markupStream.toHtmlDebugString();
                resource = markupStream.getResource().toString();
            }
        }
        String markupStr = "Resource: " + (resource.isEmpty() ? "[No Resource]" : resource);
        markupStr += "\nMarkup: " + (markup.isEmpty() ? "[No Markup]" : markup);
        return markupStr;
    }

    /**
     * @param throwable <p>
     * @return
     */
    private String getErrorMessage(final Throwable throwable) {
        if (throwable != null) {
            StringBuilder sb = new StringBuilder(256);

            // first print the last cause
            List<Throwable> al = convertToList(throwable);
            int length = al.size() - 1;
            Throwable cause = al.get(length);
            sb.append("Last cause: ").append(cause.getMessage()).append('\n');
            if (throwable instanceof WicketRuntimeException) {
                String msg = throwable.getMessage();
                if ((msg != null) && (msg.equals(cause.getMessage()) == false)) {
                    if (throwable instanceof MarkupException) {
                        MarkupStream stream = ((MarkupException) throwable).getMarkupStream();
                        if (stream != null) {
                            String text = "\n" + stream.toString();
                            if (msg.endsWith(text)) {
                                msg = msg.substring(0, msg.length() - text.length());
                            }
                        }
                    }

                    sb.append("WicketMessage: ");
                    sb.append(msg);
                    sb.append("\n\n");
                }
            }
            return sb.toString();
        } else {
            return "[Unknown]";
        }
    }

    /**
     * Converts a Throwable to a string.
     * <p>
     *
     * @param throwable The throwable
     *                  <p>
     * @return The string
     */
    private String getStackTrace(final Throwable throwable) {
        if (throwable != null) {
            List<Throwable> al = convertToList(throwable);

            StringBuilder sb = new StringBuilder(256);

            // first print the last cause
            int length = al.size() - 1;
            Throwable cause = al.get(length);

            sb.append("Root cause:\n\n");
            outputThrowable(cause, sb, false);

            if (length > 0) {
                sb.append("\n\nComplete stack:\n\n");
                for (int i = 0; i < length; i++) {
                    outputThrowable(al.get(i), sb, true);
                    sb.append("\n");
                }
            }
            return sb.toString();
        } else {
            return "[No Throwable]";
        }
    }

    /**
     * Outputs the throwable and its stacktrace to the stringbuffer. If
     * stopAtWicketSerlvet is true then the output will stop when the
     * org.apache.wicket servlet is reached. sun.reflect. packages are filtered
     * out.
     * <p>
     *
     * @param cause
     * @param sb
     * @param stopAtWicketServlet
     */
    private void outputThrowable(Throwable cause, StringBuilder sb, boolean stopAtWicketServlet) {
        sb.append(cause);
        sb.append("\n");
        StackTraceElement[] trace = cause.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            String traceString = trace[i].toString();
            if (!(traceString.startsWith("sun.reflect.") && i > 1)) {
                sb.append("     at ");
                sb.append(traceString);
                sb.append("\n");
                if (stopAtWicketServlet
                        && (traceString.startsWith("org.apache.wicket.protocol.http.WicketServlet") || traceString
                        .startsWith("org.apache.wicket.protocol.http.WicketFilter"))) {
                    return;
                }
            }
        }
    }

    /**
     * @param throwable <p>
     * @return
     */
    private List<Throwable> convertToList(final Throwable throwable) {
        List<Throwable> al = Generics.newArrayList();
        Throwable cause = throwable;
        al.add(cause);
        while ((cause.getCause() != null) && (cause != cause.getCause())) {
            cause = cause.getCause();
            al.add(cause);
        }
        return al;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.socratic.web.BasePage#getPageTitleModel()
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("bug.report.title", this, null);
    }

    @Override
    protected void setHeaders(final WebResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

}
