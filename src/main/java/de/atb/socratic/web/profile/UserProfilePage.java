/**
 *
 */
package de.atb.socratic.web.profile;

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
import javax.inject.Inject;

import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.tour.TourHelper;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.service.votes.ToursService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class UserProfilePage extends BasePage {

    private static final long serialVersionUID = 8687903175692268021L;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    UserService userService;

    @Inject
    ToursService toursService;

    private Long userId;
    private Boolean edit;
    private Label profileLabel;
    private Model<String> model;
    private User profileUser;
    private TourHelper tourHelper = new TourHelper(this);
    private AbstractDefaultAjaxBehavior behave;

    /**
     * @param parameters
     */
    public UserProfilePage(final PageParameters parameters) {
        super(parameters);
        userId = parameters.get("id").toOptionalLong();
        edit = parameters.get("edit").toBoolean();

        if (userId != null && loggedInUser.getId().compareTo(userId) != 0) {
            profileUser = userService.getById(userId);
            model = Model.of(new StringResourceModel(
                    "page.user.header",
                    this,
                    null,
                    new Object[]{profileUser.getNickName()}).getObject());
            profileLabel = new Label("profileHeader", model) {
                @Override
                protected void onConfigure() {
                    model.setObject(new StringResourceModel(
                            "page.user.header",
                            this,
                            null,
                            new Object[]{profileUser.getNickName()}).getObject());
                    super.onConfigure();
                }
            };

            profileLabel.setOutputMarkupId(true);
            add(profileLabel);
        } else {
            profileUser = loggedInUser;
            model = Model.of(new StringResourceModel("page.your.header", this, null).getObject());
            profileLabel = new Label("profileHeader", model) {
                @Override
                protected void onConfigure() {
                    model.setObject(new StringResourceModel("page.your.header", this, null).getObject());
                    super.onConfigure();
                }
            };
            profileLabel.setOutputMarkupId(true);
            add(profileLabel);

            behave = tourHelper.getAjaxBehavior(TourHelper.TOUR_USERPROFILE_NAME, toursService, loggedInUser);
            add(behave);
        }

        // add panel to edit basic user properties
        add(new EditUserPanel("editUserPanel", feedbackPanel, userId, edit) {
            @Override
            protected void onAfterSubmit(AjaxRequestTarget target) {
                target.add(profileLabel);
                // user name may have changed --> update logout menu fragment
                updateNavbar();
            }

            @Override
            protected void onAfterCancel(AjaxRequestTarget target) {
                // user name may have changed --> update logout menu fragment
                updateNavbar();
            }
        });

        // add panel to reset password
        ChangePasswordPanel changePasswordPanel = new ChangePasswordPanel("resetPasswordPanel", feedbackPanel) {
            @Override
            protected void onConfigure() {
                if (loggedInUser.getId().compareTo(userId) == 0) {
                    if ((loggedInUser.authenticatesThroughLDAP() && !loggedInUser.isRegisteredThroughEFF())
                            || loggedInUser.authenticatesThroughLinkedIn() || loggedInUser.authenticatesThroughFacebook()) {
                        setVisible(false);
                    } else {
                        setVisible(true);
                    }
                } else {
                    setVisible(false);
                }
                super.onConfigure();
            }
        };
        add(changePasswordPanel);

        AdminChangePasswordPanel adminChangePasswordPanel =
                new AdminChangePasswordPanel("adminResetPasswordPanel", feedbackPanel, profileUser) {
                    @Override
                    protected void onConfigure() {
                        if (loggedInUser.hasAnyRoles(UserRole.ADMIN, UserRole.SUPER_ADMIN)
                                && loggedInUser.getId().compareTo(userId) != 0) {
                            if ((loggedInUser.authenticatesThroughLDAP() && !loggedInUser.isRegisteredThroughEFF())
                                    || loggedInUser.authenticatesThroughLinkedIn()) {
                                setVisible(false);
                            } else {
                                setVisible(true);
                            }
                        } else {
                            setVisible(false);
                        }
                        super.onConfigure();
                    }
                };
        add(adminChangePasswordPanel);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.socratic.web.BasePage#getPageTitleModel()
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        String create = tourHelper.createUserProfileTour(toursService, loggedInUser, this);
        if (create == null) {
            return;
        }
        response.render(JavaScriptHeaderItem.forScript(tourHelper.getAjaxCallbackJS(behave), ""));
        response.render(JavaScriptHeaderItem.forUrl(JSTemplates.BOOTSTRAP_TOUR, JSTemplates.BOOTSTRAP_TOUR_REF_ID));
        response.render(OnDomReadyHeaderItem.forScript(create));
    }

}
