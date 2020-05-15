/*
 * Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.stanford.registry.client.clinictabs;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.ValidEmailAddress;
import edu.stanford.registry.client.event.InvalidEmailEvent;
import edu.stanford.registry.client.event.InvalidEmailHandler;
import edu.stanford.registry.client.service.SecurityService;
import edu.stanford.registry.client.service.SecurityServiceAsync;
import edu.stanford.registry.client.survey.FormWidgets;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.widgets.Menu;
import edu.stanford.registry.client.widgets.PopdownButton;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Provider;
import edu.stanford.registry.shared.UserDetail;
import edu.stanford.registry.shared.UserIdp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.FormType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

public class UserAdminWidget extends TabWidget implements RegistryTabIntf, InvalidEmailHandler {

  private final SecurityServiceAsync securityService = GWT.create(SecurityService.class);
  // containers
  private final DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.EM);
  private final DockLayoutPanel centerPanel = new DockLayoutPanel(Unit.EM);
  private final DockLayoutPanel subPanel = new DockLayoutPanel(Unit.EM);
  private final HorizontalPanel headerPanel = new HorizontalPanel();
  private final FlowPanel titlePanel = new FlowPanel();
  private final HorizontalPanel searchPanel = new HorizontalPanel();
  private final Grid displayTable = new Grid(1, 2);

  // page components
  private final Label titleLabel = new Label("User Administration");
  private myPopdownButton searchOptionsButton;
  private final SingleSelectionModel<UserDetail> selectionModel = new SingleSelectionModel<>();
  //private Label effectiveHeading = new Label("The application server is currently honoring the following");

  private final TextBox searchText = new TextBox();
  private String priorSearchText = null;
  private final Button searchButton = new Button("Search");
  private Map<String, String> roles = null; // (ROLE_NAME, ROLE_DISPLAY_NAME)
  private ArrayList<UserIdp> identityProviders = null;
  private ValidEmailAddress emailAddr = null;
  private long nextIdp = 0L;
  private ArrayList<UserCred> userCredArr = new ArrayList<>();
  private Select providerList;
  private Option removeProviderOption;
  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();

  public UserAdminWidget(ClientUtils clientUtils) {
    super(clientUtils);
    setServiceEntryPoint(securityService, "securityService");
    initWidget(mainPanel);
  }

  @Override
  public void load() {
    setEmptyMessage();
    mainPanel.addNorth(getMessageBar(), 2);

    securityService.getRoles(getClientUtils().getClientConfig().getSiteName(),
        new edu.stanford.registry.client.service.Callback<Map<String, String>>() {
      @Override
      public void handleSuccess(Map<String, String> result) {
        roles = result;
      }
    });

    securityService.findIdentityProviders(
        new edu.stanford.registry.client.service.Callback<ArrayList<UserIdp>>() {
      @Override
      public void handleSuccess(ArrayList<UserIdp> result) {
        identityProviders = result;
      }
    });
    headerPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
    headerPanel.setStylePrimaryName(css.clTabPgHeadingBar());
    headerPanel.setSpacing(10);
    headerPanel.setWidth("95%");
    titleLabel.setStylePrimaryName(css.titleText());
    titleLabel.setHeight("40px");
    titlePanel.add(titleLabel);
    titlePanel.setStylePrimaryName(css.clTabPgHeadingBar());
    headerPanel.add(titlePanel);

    searchPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
    searchPanel.setStylePrimaryName(css.clTabPgHeadingBar());
    searchOptionsButton = new myPopdownButton();
    searchPanel.add(searchOptionsButton);
    searchText.setStylePrimaryName(css.clTabPgHeadingBarText());
    searchText.getElement().setAttribute("size", "50");
    searchText.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
          doSearch();
        }
      }
    });
    searchPanel.add(searchText);
    searchButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSearch();
      }
    });
    searchPanel.add(searchButton);
    headerPanel.add(searchPanel);
    headerPanel.setCellHorizontalAlignment(searchPanel, HorizontalPanel.ALIGN_RIGHT);

    subPanel.addNorth(headerPanel, 5.2);
    centerPanel.setStylePrimaryName(css.centerPanel());
    subPanel.add(centerPanel);
    mainPanel.add(subPanel);
  }

  private void doSearch() {
    setEmptyMessage();
    switch (searchOptionsButton.getOption()) {
    case 0:
      findUser(null);
      break;
    case 1:
      findUsersWithDisplayName();
      break;
    default:
      findAllUsers();
      break;
    }
  }

  private void findUser(final DataGrid<UserDetail> table) {
    if (getClientUtils() == null) {
      setErrorMessage("Utilities are currently unavailable");
      return;
    }
    if (getClientUtils().getUser() == null) {
      setErrorMessage("Current user information is unavailable");
      return;
    }
    if (!getClientUtils().getUser().hasRole(serviceName(), getClientUtils().getClientConfig().getSiteName())) {
      setErrorMessage("You do not have the authority to use this service");
      return;
    }
    securityService.findAllUsers(searchText.getText(),
        new edu.stanford.registry.client.service.Callback<ArrayList<UserDetail>>() {
          @Override
          public void handleSuccess(ArrayList<UserDetail> result) {
            showUserDetail(result, table);
          }
        });
  }

  private void showUserDetail(ArrayList<UserDetail> userDetailArray, final DataGrid<UserDetail> table) {
    centerPanel.clear();
    centerPanel.setWidth("95%");
    FlowPanel innerPanel = new FlowPanel();
    innerPanel.addStyleName(css.centerPanel());
    innerPanel.setWidth("98%");

    UserDetail userDetail = null;
    for (UserDetail ud : userDetailArray) {
      if (ud.getUsername().equals(searchText.getText().trim())) {
        userDetail = ud; // find the one that was searched for first
      }
    }
    if (userDetail == null) {
      setErrorMessage("Did not find the user. Fill out this form to create a new one.");
      userDetail = new UserDetail(null, searchText.getText(), null);
      userDetail.setProviderEid("");
    }
    searchText.setText("");
    Panel cPanel = new Panel(PanelType.DEFAULT);
    StyleHelper.addEnumStyleName(cPanel, ColumnSize.LG_11);

    PanelBody cPanelBody = new PanelBody();
    StyleHelper.addEnumStyleName(cPanel, ColumnSize.LG_11);
    userCredArr = new ArrayList<>();
    HashMap<Long, String> usedIdpMap = new HashMap<>();
    for (UserDetail ud : userDetailArray) {
      usedIdpMap.put(ud.getIdpId(), ud.getUsername());
    }
    cPanelBody.add(makeCredentialForm(userDetail, usedIdpMap));
    if (userDetailArray.size() > 1) {
      for (UserDetail ud : userDetailArray) {
        if (!ud.getUsername().equals(userDetail.getUsername())) {
          cPanelBody.add(makeCredentialForm(ud, usedIdpMap));
        }
      }
    }
    if (usedIdpMap.size() < identityProviders.size()) {
      cPanelBody.add(getAddButton(usedIdpMap, cPanelBody, userDetail));
    }
    cPanel.add(cPanelBody);
    innerPanel.add(cPanel);
    Form userForm = new Form(FormType.HORIZONTAL);
    final TextBox displayName = getTextBox(userDetail.getDisplayName(), "dname", true);
    userForm.add(getDisplayNameFormGroup(displayName));

    emailAddr = getClientUtils().makeEmailField(userDetail.getEmailAddr(), this);
    emailAddr.getElement().setId("email");
    StyleHelper.addEnumStyleName(emailAddr, ColumnSize.LG_6);
    userForm.add(getEmailFormGroup(emailAddr));
    providerList = getProviderList(userDetail.getProviderEid(), userDetail.getProviderId());
    userForm.add(getProvIdFormGroup(providerList));

// Comment out the "Allow this user to login" checkbox as this
// affects the user's ability to login across all sites not just
// to this specific site.
//    CheckBox enabled = new CheckBox("Allow this user to login");
//    enabled.setValue(userDetail.isEnabled());
//
//    enabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
//      @Override
//      public void onValueChange(ValueChangeEvent<Boolean> event) {
//        userDetailFinal.setEnabled(event.getValue());
//      }
//    });
//    FlowPanel enabledPanel = new FlowPanel();
//    enabledPanel.add(enabled);
//    addRow(2, enabledPanel);
    FormGroup grantedFormGroup = new FormGroup();

    FormLabel grantedLabel = FormWidgets.formLabelFor("Permissions for this user:", "granted", ColumnSize.LG_2);
    grantedFormGroup.add(grantedLabel);
    grantedFormGroup.add(getPermissions(roles, userDetail, "granted", false));
    userForm.add(grantedFormGroup);

    Map<String, String> views = getClientConfig().getCustomViews();
    if (views.size() > 0) {
      FormGroup customFormGroup = new FormGroup();
      FormLabel customLabel = FormWidgets.formLabelFor("Views for this user", "custom", ColumnSize.LG_2);
      customFormGroup.add(customLabel);
      customFormGroup.add(getPermissions(views, userDetail, "custom", false));
      userForm.add(customFormGroup);
    }
    Panel uPanel = new Panel(PanelType.DEFAULT);
    StyleHelper.addEnumStyleName(uPanel, ColumnSize.LG_11);

    PanelBody uPanelBody = new PanelBody();
    StyleHelper.addEnumStyleName(uPanel, ColumnSize.LG_11);
    uPanelBody.add(userForm);
    uPanel.add(uPanelBody);
    innerPanel.add(uPanel);
    displayTable.setVisible(true);
    FormGroup buttonGroup = new FormGroup();
    FormLabel buttonLabel = FormWidgets.formLabelFor("","buttons", ColumnSize.LG_2);
    buttonGroup.add(buttonLabel);
    buttonGroup.add(displayTable);
    uPanel.add(buttonGroup);

    final Button saveButton = new Button("Save");
    final UserDetail userDetailFinal = userDetail;
    saveButton.addStyleName(css.leftButton());
    saveButton.addStyleName(css.paddedButton());
    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // check that the same idp isn't used twice
        HashMap<Long, String> map = new HashMap<>();
        for (UserCred userCred : userCredArr) {
          if (map.containsKey(userCred.userIdp.getIdpId())) {
            setErrorMessage("Identity Provider must be unique ");
            searchText.setValue(userDetailFinal.getUsername());
            return;
          } else if (map.containsValue(userCred.username.getText())) {
            setErrorMessage("Username must be unique ");
            searchText.setValue(userDetailFinal.getUsername());
            return;
          } else {
            map.put(userCred.userIdp.getIdpId(), userCred.username.getText());
          }
        }

        saveButton.setText("Saving...");
        saveButton.setEnabled(false);
        ArrayList<UserDetail> saveUsers= new ArrayList<>();
        for (UserCred userCred : userCredArr) {
          if (userCred.username != null && userCred.username.getText().trim().length() > 0) {
            final UserDetail userDetailsave = new UserDetail(userCred.userIdp.getIdpId(),
             userCred.username.getText().trim(),
             emailAddr.getText().trim());
            userDetailsave.setDisplayName(displayName.getText());
            userDetailsave.setUserPrincipalId(userDetailFinal.getUserPrincipalId());
            Option selected = providerList.getSelectedItem();
            if (selected != null
                && !selected.getText().trim().isEmpty()) {
              userDetailsave.setProviderId((Long.valueOf(selected.getValue())));
              userDetailsave.setProviderEid(selected.getText());
              userDetailFinal.setProviderId(userDetailsave.getProviderId());
              userDetailFinal.setProviderEid(userDetailsave.getProviderEid());
              if (Long.valueOf(selected.getValue()) ==0) {
                providerList.remove(removeProviderOption);
              }
            }
            userDetailsave.setSurveySites(userDetailFinal.getSurveySites());
            userDetailsave.setRoles(userDetailFinal.getRoles());
            userDetailsave.setGrantedRoles(userDetailFinal.getGrantedRoles());
            saveUsers.add(userDetailsave);
          }
        }
        securityService.saveUserDetail(saveUsers, new edu.stanford.registry.client.service.Callback<Void>() {
          @Override
          public void handleSuccess(Void result) {
          saveButton.setEnabled(true);
            searchText.setValue(userDetailFinal.getUsername());
            setSuccessMessage("Saved");
            if (table != null) {
              UserDetail detail = selectionModel.getSelectedObject();
              detail.setDisplayName(userDetailFinal.getDisplayName());
              detail.setEmailAddr(userDetailFinal.getEmailAddr());
              detail.setProviderEid(userDetailFinal.getProviderEid());
              detail.setProviderId(userDetailFinal.getProviderId());
              table.redraw();
            }
            findUser(table);
          }
        });
      }
    });
    displayTable.setWidget(0, 0, saveButton);

    // Show the users cached permissions
    DisclosurePanel disclosurePanel = new DisclosurePanel("Currently cached permissions");
    StyleHelper.addEnumStyleName(disclosurePanel, ColumnSize.LG_6);
    disclosurePanel.addStyleName(css.searchPopUp());
    FormGroup effectiveGroup = new FormGroup();
    StyleHelper.addEnumStyleName(effectiveGroup, FormType.HORIZONTAL);
    FormLabel effectiveLabel = FormWidgets.formLabelFor(" ", "effective", ColumnSize.LG_2);
    effectiveGroup.add(effectiveLabel);
    FlexTable userServices = addServices(userDetail, true);
    userServices.setStylePrimaryName(css.leftLabel());
    disclosurePanel.setContent(userServices);
    effectiveGroup.add(disclosurePanel);
    innerPanel.add(effectiveGroup);
    final ScrollPanel scroller = new ScrollPanel();
    scroller.setSize("100%", "100%");
    scroller.add(innerPanel);
    centerPanel.add(scroller);
    if (table != null) {
      Button returnButton = new Button("Return to list");
      returnButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (priorSearchText != null && priorSearchText.length() > 0) {
            searchOptionsButton.setOption(1);
            searchText.setText(priorSearchText);
          } else {
            searchOptionsButton.setOption(2);
            searchText.setText("");
          }
          findAllUsers();
        }
      });
      returnButton.addStyleName(css.leftButton());
      displayTable.setWidget(0, 1, returnButton);
    }
  }

  private FormGroup getPermissions(Map<String, String> userAuthorities, final UserDetail user, String id, boolean readOnly) {
    FormGroup formGroup = new FormGroup();

    StyleHelper.addEnumStyleName(formGroup, ColumnSize.LG_10);
    // This shouldn't happen! Initialization should get these.
    if (userAuthorities == null) {
      return formGroup;
    }
    String DEVELOPERROLE = Constants.ROLE_DEVELOPER + "[" + getClientUtils().getClientConfig().getSiteName() +"]";
    for (final String authority : userAuthorities.keySet()) {
      FlowPanel rolePanel = new org.gwtbootstrap3.client.ui.gwt.FlowPanel();
      StyleHelper.addEnumStyleName(rolePanel, ColumnSize.LG_10);
      CheckBox roleBox = new CheckBox(userAuthorities.get(authority));
      roleBox.getElement().setAttribute("style", "text-align: left; font-size: large;");
      boolean isDeveloper = getClientUtils().getUser().hasRole(DEVELOPERROLE);
      boolean hasRole = getClientUtils().getUser().hasRole(authority);
      if (readOnly || (!hasRole && !isDeveloper)) {
        roleBox.setValue(user.hasRole(authority));
        roleBox.setEnabled(false);
        roleBox.setTitle("You do not have permission to grant access to this");
      } else {
        roleBox.setValue(user.hasGrantedRole(authority));
        roleBox.setTitle("Check box to grant the user access to this");
        roleBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            if (event.getValue()) {
              user.addGrantedRole(authority);
            } else {
              user.removeGrantedRole(authority);
            }
          }
        });

      }
      rolePanel.add(roleBox);
      formGroup.getElement().setId(id);
      formGroup.add(rolePanel);
    }
    return formGroup;

  }
  private FlexTable addServices(final UserDetail user, boolean readOnly) {

    FlexTable rolesTable = new FlexTable();
    rolesTable.setWidth("100%");
    // This shouldn't happen! Initialization should get these.
    if (roles == null) {
      return rolesTable;
    }
    int row = 0;
    for (final String authority : roles.keySet()) {
      CheckBox roleBox = new CheckBox(roles.get(authority));
      if (readOnly || !getClientUtils().getUser().hasRole(authority)) {
        roleBox.setValue(user.hasRole(authority));
        roleBox.setEnabled(false);
        roleBox.setTitle("You do not have permission to grant access to this");

      } else {
        roleBox.setValue(user.hasGrantedRole(authority));
        roleBox.setTitle("Check box to grant the user access to this");
        roleBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
          @Override
          public void onValueChange(ValueChangeEvent<Boolean> event) {
            if (event.getValue()) {
              user.addGrantedRole(authority);
            } else {
              user.removeGrantedRole(authority);
            }
          }
        });

      }
      roleBox.setWidth("100%");
      rolesTable.setWidget(row, 0, roleBox);
      rolesTable.getCellFormatter().setHeight(row, 0, "20px");
      row++;
    }
    rolesTable.getElement().setId("granted");
    return rolesTable;
  }

  private void findUsersWithDisplayName() {
    getClientUtils().showLoadingPopUp();
    priorSearchText = searchText.getText();
    securityService.findUsersWithDisplayName(searchText.getText(),
        new edu.stanford.registry.client.service.Callback<ArrayList<UserDetail>>() {
      @Override
      public void handleSuccess(ArrayList<UserDetail> result) {
        showUsersTable(createUsersTable(result));
      }
    });
  }

  private void findAllUsers() {
    priorSearchText = null;
    getClientUtils().showLoadingPopUp();
    securityService.findAllUsers(
        new edu.stanford.registry.client.service.Callback<ArrayList<UserDetail>>() {
      @Override
      public void handleSuccess(ArrayList<UserDetail> result) {
        showUsersTable(createUsersTable(result));
      }
    });
  }

  private void showUsersTable(Widget table) {
    selectionModel.clear();
    centerPanel.clear();
    centerPanel.addStyleName(css.centerPanel());
    centerPanel.setWidth("95%");
    centerPanel.setHeight("100%");
    centerPanel.add(table);
    setEmptyMessage();
    getClientUtils().hideLoadingPopUp();
  }

  @Override
  public String serviceName() {
    return "SECURITY";
  }

  private Widget createUsersTable(final List<UserDetail> usersList) {
    final DataGrid<UserDetail> usersTable = new DataGrid<>();

    usersTable.setRowStyles(new RowStyles<UserDetail>() {
      @Override
      public String getStyleNames(UserDetail row, int rowIndex) {
        return "textBoxInput";
      }
    });

    Column<UserDetail, String> sunetCol = new Column<UserDetail, String>(new ClickableTextCell()) {
      @Override
      public String getValue(UserDetail userDetail) {
        return userDetail.getUsername();
      }
    };
    sunetCol.setFieldUpdater(new FieldUpdater<UserDetail, String>() {
      @Override
      public void update(int index, UserDetail userDetail, String value) {
        priorSearchText = searchText.getText();
        searchText.setText(userDetail.getUsername());
        searchOptionsButton.setOption(0);
        findUser(usersTable);
      }
    });
    sunetCol.setSortable(true);
    usersTable.addColumn(sunetCol, "Username");

    Column<UserDetail, String> idpColumn =new Column<UserDetail, String>(new ClickableTextCell()) {
      @Override
      public String getValue(UserDetail userDetail) {
        for (UserIdp idp: identityProviders) {
          if (userDetail.getIdpId().equals(idp.getIdpId())) {
            return idp.getAbbrName();
          }
        }
        return "";
      }
    };
    idpColumn.setFieldUpdater(new FieldUpdater<UserDetail, String>() {
      @Override
      public void update(int index, UserDetail userDetail, String value) {
        priorSearchText = searchText.getText();
        searchText.setText(userDetail.getUsername());
        searchOptionsButton.setOption(0);
        findUser(usersTable);
      }
    });
    usersTable.addColumn(idpColumn, "IDP");

    Column<UserDetail, String> displayNameCol = new Column<UserDetail, String>(new ClickableTextCell()) {
      @Override
      public String getValue(UserDetail userDetail) {
        return userDetail.getDisplayName();
      }
    };
    displayNameCol.setFieldUpdater(new FieldUpdater<UserDetail, String>() {
      @Override
      public void update(int index, UserDetail userDetail, String value) {
        priorSearchText = searchText.getText();
        searchText.setText(userDetail.getUsername());
        searchOptionsButton.setOption(0);
        findUser(usersTable);
      }
    });
    displayNameCol.setSortable(true);
    usersTable.addColumn(displayNameCol, "Display Name");

    TextColumn<UserDetail> emailCol = new TextColumn<UserDetail>(){
      @Override
      public String getValue(UserDetail userDetail) {
        return userDetail.getEmailAddr();
      }
    };
    usersTable.addColumn(emailCol, "Email");

    TextColumn<UserDetail> providerCol = new TextColumn<UserDetail>(){
      @Override
      public String getValue(UserDetail userDetail) {
        if (userDetail.getProviderId() == 0) {
          return null;
        }
        return userDetail.getProviderEid();
      }
    };
    usersTable.addColumn(providerCol, "Provider Id");

    Column<UserDetail, String> isAdminCol = new Column<UserDetail, String>(new TextCell() {
      @Override
      public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
        // Override the default behavior to avoid empty buttons being visible and clickable
        if ("y".equals(data.asString())) {
          sb.appendHtmlConstant("<i class=\"fa fa-check GPBYFDEKI fa-1x\"></i>" );
        }
      }
    }) {
      @Override
      public String getValue(UserDetail userDetail) {
        if (userDetail.hasRole(Constants.ROLE_SECURTY, getClientUtils().getClientConfig().getSiteName())) {
          return "y";
        }
        return "";
      }
    };
    usersTable.addColumn(isAdminCol, "User Admin  ");

    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        searchText.setFocus(true);
      }
    });

    usersTable.setColumnWidth(sunetCol, 220, Unit.PX);
    usersTable.setColumnWidth(idpColumn, 65, Unit.PX);
    usersTable.setColumnWidth(displayNameCol, 250, Unit.PX);
    usersTable.setColumnWidth(emailCol, 300, Unit.PX);
    usersTable.setColumnWidth(providerCol, 100, Unit.PX);
    usersTable.setColumnWidth(isAdminCol, 65, Unit.PX );

    usersTable.setStylePrimaryName(css.fixedList());
    usersTable.addStyleName(css.dataList());
    usersTable.addStyleName(css.scheduleList());

    usersTable.setEmptyTableWidget(new Label("None found. "));
    ListDataProvider<UserDetail> dataProvider = new ListDataProvider<>();
    dataProvider.addDataDisplay(usersTable);
    List<UserDetail> users = dataProvider.getList();

    users.addAll(usersList);

    ListHandler<UserDetail> columnSortHandler = new ListHandler<>(users);
    columnSortHandler.setComparator(sunetCol,
        new Comparator<UserDetail>() {
      @Override
      public int compare(UserDetail o1, UserDetail o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getUsername().compareTo(o2.getUsername()) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(displayNameCol,
        new Comparator<UserDetail>() {
      @Override
      public int compare(UserDetail o1, UserDetail o2) {
        if (o1 == o2) {
          return 0;
        }

        if (o1 != null) {
          return (o2 != null) ? o1.getDisplayName().compareTo(o2.getDisplayName()) : 1;
        }
        return -1;
      }
    });

    usersTable.setSelectionModel(selectionModel);
    usersTable.addColumnSortHandler(columnSortHandler);
    usersTable.getColumnSortList().push(sunetCol);
    usersTable.setRowCount(users.size());
    usersTable.setPageSize(users.size());
    return usersTable;
  }

  private Select getProviderList(final String providerEid, final Long providerId) {
    final Select providerListBox = new Select();
    removeProviderOption = getOption("[No Provider]", 0L);
    if (providerEid != null) {
      providerListBox.add(getOption(providerEid, providerId));
      providerListBox.setValue(providerEid,true);
    }
    securityService.findProviders(true, new edu.stanford.registry.client.service.Callback<ArrayList<Provider>>() {
      @Override
      public void handleSuccess(ArrayList<Provider> result) {

        for (Provider provider : result) {
          providerListBox.add(getOption(provider.getProviderEid(), provider.getProviderId()));
        }
        if (providerEid != null && providerId != 0) {
          providerListBox.add(removeProviderOption);
        }
        providerListBox.refresh();
      }
    });
    return providerListBox;
  }

  private class myPopdownButton extends PopdownButton {

    final String[] options = {"Search by username", "Search by display name", "Show all users"};
    int choosen = 0;
    myPopdownButton() {
      withText(options[0]).withMenu(new PopdownButton.Customizer() {
        @Override
        public void customizePopup(final PopdownButton button, Menu menu) {
          menu.addItem(options[0], new Command() {
            @Override
            public void execute() {
              button.setText(options[0]);
              setEmptyMessage();
              choosen = 0;
            }
          });
          menu.addItem(options[1], new Command() {
            @Override
            public void execute() {
              searchOptionsButton.setText(options[1]);
              setEmptyMessage();
              choosen = 1;
            }
          });
          menu.addItem(options[2], new Command() {
            @Override
            public void execute() {
              searchOptionsButton.setText(options[2]);
              setEmptyMessage();
              choosen = 2;
              findAllUsers();
            }
          });

        }
      });
    }
    int getOption() {
      return choosen;
    }

    void setOption(int i) {
      choosen = i;
      searchOptionsButton.setText(options[choosen]);
    }
  }
  @Override
  public void onFailedValidation(InvalidEmailEvent event) {

    setErrorMessage(event.getMessage() + "!");
    emailAddr.setFocus(true);
    emailAddr.addStyleName(css.dataListTextError());
    emailAddr.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        setEmptyMessage();
        if (emailAddr.isValid()) {
          emailAddr.removeStyleName(css.dataListTextError());
        }
      }
    });
  }

  private TextBox getTextBox(String textBoxValue, String id, boolean enabled) {
    TextBox textBox = new TextBox();
    textBox.setValue(textBoxValue);
    textBox.setId(id);
    textBox.setEnabled(enabled);
    return textBox;
  }
  private FormGroup getDisplayNameFormGroup( IsWidget widget) {
    return getFormGroup("Display Name:", ColumnSize.LG_2, "dname", widget,  ColumnSize.LG_6);
  }
  private FormGroup getFormGroup(String labelText, ColumnSize col1,  String id, IsWidget widget, ColumnSize col2) {
    FormGroup formGroup = new FormGroup();
    FormLabel label = FormWidgets.formLabelFor(labelText, id);
    StyleHelper.addEnumStyleName(label, col1);

    formGroup.add(label);
    FlowPanel panel = new org.gwtbootstrap3.client.ui.gwt.FlowPanel();
    StyleHelper.addEnumStyleName(panel,col2);
    panel.addStyleName(HasHorizontalAlignment.ALIGN_LEFT.getTextAlignString());

    panel.add(widget);
    formGroup.add(panel);
    return formGroup;
  }
  private FormGroup getEmailFormGroup(ValidEmailAddress textBox) {
    textBox.addStyleName("form-control");
    return getFormGroup("Email:", ColumnSize.LG_2, "email", textBox, ColumnSize.LG_6);
  }

  private FormGroup getProvIdFormGroup(Select listBox) {
    FormGroup formGroup = new FormGroup();
    FormLabel label = FormWidgets.formLabelFor("Provider ID:", "provider");
    StyleHelper.addEnumStyleName(label, ColumnSize.LG_2);

    formGroup.add(label);
    FlowPanel panel = new org.gwtbootstrap3.client.ui.gwt.FlowPanel();
    StyleHelper.addEnumStyleName(panel, ColumnSize.LG_6);

    panel.add(listBox);
    formGroup.add(panel);
    return formGroup;
  }

  private Option getOption(String value, Long id) {
    Option opt = new Option();
    opt.setText(value);
    opt.setValue(id.toString());
    return opt;
  }

  private IsWidget getUserIdp(Long idpId, UserCred userCred, HashMap<Long, String> usedIdpMap) {
    final UserIdp currUserIdp = new UserIdp();

    final String widgetName;
    if (idpId == null) {
      widgetName = "idp0";
    } else {
      widgetName = "idp" + idpId.toString();
    }
    if (identityProviders.size() > 0) {
      currUserIdp.setIdpId(identityProviders.get(0).getIdpId());// Default to first
      currUserIdp.setAbbrName(identityProviders.get(0).getAbbrName());
      currUserIdp.setDisplayName(identityProviders.get(0).getDisplayName());
    }

    final DropDownMenu idpMenu = new DropDownMenu();
    final Button selectButton = new Button("Select Identity Provider");

    idpMenu.setId(widgetName);
    for (final UserIdp userIdp : identityProviders) {
      AnchorListItem listItem = new AnchorListItem(userIdp.getDisplayName());
      listItem.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          currUserIdp.setIdpId(userIdp.getIdpId());
          currUserIdp.setAbbrName(userIdp.getAbbrName());
          currUserIdp.setDisplayName(userIdp.getDisplayName());
          selectButton.setText(currUserIdp.getDisplayName() + " ");
        }
      });
      idpMenu.add(listItem);
      if (userIdp.getIdpId().equals(idpId)) {
        currUserIdp.setIdpId(userIdp.getIdpId());
        currUserIdp.setAbbrName(userIdp.getAbbrName());
        currUserIdp.setDisplayName(userIdp.getDisplayName());
        selectButton.setText(currUserIdp.getDisplayName() + " ");
      }
    }
    userCred.userIdp = currUserIdp;
    // count the number of identityProviders not in use
    nextIdp=-1;
    for (UserIdp idp : identityProviders) {
      if (!usedIdpMap.containsKey(idp.getIdpId())) {
        nextIdp = idp.getIdpId();
      }
    }
    if (nextIdp < 0) {
      final Button idpButton = new Button();
      StyleHelper.addEnumStyleName(idpButton, ColumnSize.LG_12);
      idpButton.setText(currUserIdp.getDisplayName());
      idpButton.addStyleName("form-control");
      idpButton.setId(widgetName);
      idpButton.setEnabled(false);
      return idpButton;
    }
    return createButtonGroup(selectButton, idpMenu);
  }

  private Form makeCredentialForm(UserDetail userDetail, HashMap<Long, String> usedIdpMap) {
    UserCred userCred = new UserCred();
    userCredArr.add(userCred);
    Form credentialForm = new Form(FormType.HORIZONTAL);
    FormGroup identityFormGroup = getFormGroup("Identity Provider", ColumnSize.LG_4, "idp" + userDetail.getIdpId(),
        getUserIdp(userDetail.getIdpId(), userCred, usedIdpMap), ColumnSize.LG_8);
    StyleHelper.addEnumStyleName(identityFormGroup, ColumnSize.LG_5);
    credentialForm.add(identityFormGroup);
    userCred.username = getTextBox(userDetail.getUsername(),"uname", userDetail.getUsername().equals(""));
    FormGroup usernameFormGroup = getFormGroup("Username:",  ColumnSize.LG_3, "uname", userCred.username, ColumnSize.LG_8);
    StyleHelper.addEnumStyleName(usernameFormGroup, ColumnSize.LG_5);
    credentialForm.add(usernameFormGroup);
    return credentialForm;
  }

  private ButtonGroup createButtonGroup(Button selectButton, DropDownMenu menu) {
    ButtonGroup buttonGroup = new ButtonGroup();
    StyleHelper.addEnumStyleName(buttonGroup, ColumnSize.LG_12);
    selectButton.setDataToggle(Toggle.DROPDOWN);
    buttonGroup.add(selectButton);
    buttonGroup.add(menu);
    menu.setVisible(true);
    return buttonGroup;
  }

  Button getAddButton(final HashMap<Long, String> usedIdpMap, final PanelBody panelBody, final UserDetail origUser) {
    final Button addButton = new Button();
    addButton.setIcon(IconType.PLUS);
    addButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        UserDetail newUserDetail = new UserDetail(nextIdp, "", "");
        newUserDetail.setUserPrincipalId(origUser.getUserPrincipalId());
        panelBody.add(makeCredentialForm(newUserDetail, usedIdpMap));
        addButton.setVisible(false);
      }
    });
    return addButton;
  }

  private static class UserCred {
    TextBox username;
    UserIdp userIdp;
  }
}
