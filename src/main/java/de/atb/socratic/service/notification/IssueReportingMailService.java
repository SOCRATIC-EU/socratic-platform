/**
 *
 */
package de.atb.socratic.service.notification;

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

import javax.enterprise.context.ApplicationScoped;

import de.atb.socratic.exception.NotificationException;

/**
 * @author ATB
 */
@ApplicationScoped
public class IssueReportingMailService extends MailNotificationService<Void> {

    /**
     *
     */
    private static final long serialVersionUID = 661430941882732423L;

    private static final String[] RECIPIENTS = {"socratic-platform@atb-bremen.de"};
    private static final String TEMPLATE_REPORT_ISSUE = "report_issue_template.html";
    private static final String FROM = "SOCRATIC Support";
    private static final String SUBJECT = "New SOCRATIC Issue Reported";

    /**
     *
     */
    public IssueReportingMailService() {
        super(Void.class);
        this.from = FROM;
        this.subject = SUBJECT;
    }

    /**
     * @param report
     * @param errorMessage
     * @param stackTrace
     * @param markup
     * @param reportedBy
     * @throws NotificationException
     */
    public void sendIssueReport(final String report, final String errorMessage, final String stackTrace,
                                final String markup, final String reportedBy) throws NotificationException {
        this.template = TEMPLATE_REPORT_ISSUE;

        setValue("REPORT", report);
        setValue("REPORTEDBY", reportedBy);
        setValue("ERRORMESSAGE", errorMessage);
        setValue("STACKTRACE", stackTrace);
        setValue("MARKUP", markup);

        sendMessage(RECIPIENTS);
    }

}
