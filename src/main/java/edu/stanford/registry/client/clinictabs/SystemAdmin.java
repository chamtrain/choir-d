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

import edu.stanford.registry.client.ErrorDialogWidget;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.AdminService;
import edu.stanford.registry.client.service.AdminServiceAsync;
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.client.widgets.ColorPicker;
import edu.stanford.registry.client.widgets.ProcessXml;
import edu.stanford.registry.shared.ConfigurationOptions;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.ProcessInfo;
import edu.stanford.registry.shared.Study;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * General sys admin stuff
 *
 * @author tpacht
 */
public class SystemAdmin extends TabWidget implements ClickHandler {

  //private FlowPanel dockPanel = new FlowPanel();
  private FlowPanel centerPanel = new FlowPanel();
  private FlowPanel mainPanel = new FlowPanel();
  private DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
  private FlowPanel showTableDataPanel = new FlowPanel();
  private FlexTable dataFlexTable = new FlexTable();
  //private HorizontalPanel showTableNamePanel = new HorizontalPanel();
  private final PopupPanel processPopUp = new PopupPanel();
  private final FlexTable processTable = new FlexTable();
  private final ScrollPanel scrollPanel = new ScrollPanel();
  /**
   * Create a remote service proxy to talk to the server-side Table service.
   */
  private final AdminServiceAsync adminService = GWT.create(AdminService.class);
  private final ListBox tableNameListBox = new ListBox();
  private ListBox studyCodeList;
  private final FlowPanel subPanel = new FlowPanel();
  private final Label optionalLabel = new Label("(optional)");
  private final ListBox testNumberListBox = new ListBox();
  private final Label studyCodeLabel = new Label("Study code");
  private final Label patientIdLabel = new Label("Patient id");
  private final TextBox patientId = new TextBox();
  private final Label titleLabel = new Label("Chart title");
  private final TextBox title = new TextBox();
  private final Label colorComboLabel = new Label("Color option");
  private final ListBox colorComboList = new ListBox();
  private final Label bandLabel = new Label("Banding");
  private final ListBox bandList = new ListBox();
  private Label colorListLabel = new Label("Background");
  private final Label scaleLabel = new Label("Scale %");
  private final TextBox scale = new TextBox();
  private final Button reloadXmlButton = new Button("Reload xml definitions");
  private final Button reloadUsersButton = new Button("Reload users");
  private final Button sendTodaysEmails = new Button("Send todays emails");
  private final Button showProcessesButton = new Button("Show running processes");
  private final Button clearProcessesButton = new Button("Clear list of all running processes");
  private final Button sendEmailToListButton = new Button("Send email to a defined list of recipients");
  private final Button runTest1Button = new Button("Run test");
  Button tableButton = new Button("Get data");
  private final HorizontalPanel testButtonPanel = new HorizontalPanel();
  protected ErrorDialogWidget errorPopUp = new ErrorDialogWidget();

  private ClientUtils utils;
  private ProcessXml processXml = null;
  private ArrayList<Study> studies = null;
  private final Logger logger = Logger.getLogger(SystemAdmin.class.getName());
  private final Button closeTestButton = // new Button("Return to edit");
      new Button(new Image(RegistryResources.INSTANCE.close()).toString());
  ColorPicker colorPicker = new ColorPicker("Select color", ConfigurationOptions.COLOR_NAMES,
      ConfigurationOptions.COLORS);

  public SystemAdmin(ClinicUtils clinicUtils) {
    super(clinicUtils);
    utils = clinicUtils;
    initWidget(dockPanel);
  }

  @Override
  public void load() {
    if (isLoaded()) {
      logger.log(Level.INFO, "already loaded");
      return;
    }
    setServiceEntryPoint(adminService, "admin");

    processXml = getUtils().getProcessXml();
    scrollPanel.setSize("98%", "90%");
    mainPanel.setSize("100%", "100%");
    scrollPanel.add(mainPanel);
    dataFlexTable.setCellPadding(6);
    dataFlexTable.getRowFormatter().addStyleName(0, "tableDataHeader");
    dataFlexTable.addStyleName("dataList");
    dataFlexTable.setVisible(false);

    tableNameListBox.addItem("Select Table");
    tableNameListBox.addItem("Study");
    tableNameListBox.addItem("SurveySystem");
    tableNameListBox.setSelectedIndex(0);
    tableNameListBox.setFocus(true);
    tableButton.addStyleName("paddedButton");
    tableButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        sendNameToServer();
      }
     });
    //showTableNamePanel.add(tableNameListBox);
    //mainPanel.add(showTableNamePanel);
    mainPanel.add(tableNameListBox);
    mainPanel.add(tableButton);
    mainPanel.add(showTableDataPanel);
    mainPanel.add(new HTML("<hr>"));

    // Assemble the table to show various admin function options
    Grid buttonTable = new Grid(3,2);
    buttonTable.setWidget(0, 0, reloadXmlButton);
    buttonTable.setWidget(0, 1, reloadUsersButton);
    buttonTable.setWidget(1, 0, sendTodaysEmails);
    buttonTable.setWidget(1, 1, showProcessesButton);
    buttonTable.setWidget(2, 0, clearProcessesButton);
    buttonTable.setWidget(2, 1, sendEmailToListButton);
    for (int r=0; r<buttonTable.getRowCount(); r++) {
      for (int c=0; c<buttonTable.getColumnCount(); c++) {
        styleButtonGridWidget(buttonTable, r, c);
        ((Button)buttonTable.getWidget(r, c)).addClickHandler(this);
      }
    }
    mainPanel.add(buttonTable);
    mainPanel.add(new HTML("<hr>"));

    // Assemble the table to run tests
    testNumberListBox.addItem("Select test ");
    testNumberListBox.addItem("Create simple line chart");
    testNumberListBox.addItem("Create multiple study line chart");
    testNumberListBox.addItem("Create Image");
    testNumberListBox.addStyleName("listBoxInput");
    testNumberListBox.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int selection = testNumberListBox.getSelectedIndex();
        if (selection >= 1) {
          makeTestPanel(selection);
        }
      }
    });
    runTest1Button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        setEmptyMessage();
        int selInx = studyCodeList.getSelectedIndex();
        if (patientId.getValue() == null || patientId.getValue().trim().length() < 1) {
          setErrorMessage("Please enter a patient id");
        } else if (selInx == 0 && studyCodeList.getItemCount() > 1) {
          setErrorMessage("Please select a study");
        } else {
          String urlString = getUtils().getChartUrl("rpt", "test", 
              "tno", Integer.toString(testNumberListBox.getSelectedIndex()),
              "patient", patientId.getValue(),
              "ops", getTestOptions().toString(),
              "height=118&width=297&title", title.getValue(),
              "study", getSelectedStudies());
          logger.log(Level.INFO, "URL=" + urlString);
          Window.open(urlString, "TestResults", "");
        }
      }
    });
    closeTestButton.addStyleName("scoresCloseButton");
    closeTestButton.setTitle("Close");
    closeTestButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        testButtonPanel.clear();
        centerPanel.remove(subPanel);
        centerPanel.add(scrollPanel);
      }
    });
    subPanel.setStyleName("centerPanel");
    colorComboList.addItem("Colors");
    colorComboList.addItem("Gray");
    bandList.addItem("Yes");
    bandList.addItem("No");
    mainPanel.add(testNumberListBox);

    // Assemble Main panel.
    mainPanel.setStylePrimaryName("dataList"); //customizedFlowPanel");
    centerPanel.add(scrollPanel);

    messageBar.setStylePrimaryName("messageBar");
    dockPanel.addNorth(getMessageBar(), 2);
    dockPanel.add(centerPanel);
  }

  public void styleButtonGridWidget(Grid grid, int row, int column) {
      grid.getCellFormatter().setHeight(row, column, "40px");
      grid.getCellFormatter().setWidth(row, column, "300px");
      grid.getCellFormatter().setHorizontalAlignment(row, column, HasHorizontalAlignment.ALIGN_LEFT);
      if (grid.getWidget(row, column) != null) {
        grid.getWidget(row, column).setWidth("100%");
      }
  }

  @Override
  public void onClick(ClickEvent event) {

    setEmptyMessage();
    Widget sender = (Widget) event.getSource();
    if (sender == reloadXmlButton) {
      reloadXml();
    } else if (sender == reloadUsersButton) {
      reloadUsers();
    } else if (sender == sendTodaysEmails) {
      sendEmails();
    } else if (sender == showProcessesButton) {
      showProcesses();
    } else if (sender == clearProcessesButton) {
      clearProcesses();
    }
  }

  /**
   * Send the name from the nameTextBox to the server and wait for a response.
   */
  private void sendNameToServer() {
    getUtils().showLoadingPopUp();
    // First, we validate the input.
    if (tableNameListBox.getSelectedIndex() < 1) {
      tableButton.setText("Get data");
      tableNameListBox.setEnabled(true);
      dataFlexTable.removeAllRows();
      getUtils().hideLoadingPopUp();
      return;
    }
    String textToServer = tableNameListBox.getItemText(tableNameListBox.getSelectedIndex());
    logger.log(Level.INFO, "processing " + textToServer);
    // Then, we send the input to the server.

    adminService.getTableData(textToServer, new AsyncCallback<ArrayList<DataTable>>() {
      @Override
      public void onFailure(Throwable caught) {
        errorPopUp.setText("Checking table s");
        errorPopUp.setError(caught.getMessage());
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(ArrayList<DataTable> result) {
        tableButton.setText("Clear");
        tableNameListBox.setSelectedIndex(0);
        tableNameListBox.setEnabled(false);
        logger.log(Level.INFO, "success");

        if (result != null) {
          logger.log(Level.INFO, "success: Result had " + result.size() + " rows");
        }

        // first add the headers
        int rowCount = 1;
        String[] strgs;
        for (DataTable obj : result) {
          if (obj instanceof DataTable) {
            if (rowCount == 1) {
              strgs = obj.getAllHeaders();
              for (int i = 0; i < strgs.length; i++) {
                dataFlexTable.setText(rowCount, i, strgs[i]);
              }
              dataFlexTable.getRowFormatter().addStyleName(1, "tableDataHeader");
              dataFlexTable.addStyleName("dataList");
              rowCount++;
            }
            strgs = obj.getData(utils);
            for (int i = 0; i < strgs.length; i++) {
              if (strgs[i] != null) {
                dataFlexTable.setText(rowCount, i, strgs[i]);
              } else {
                dataFlexTable.setText(rowCount, i, "");
              }
            }
            rowCount++;
          }

          rowCount++;
        }
        dataFlexTable.setVisible(true);
        showTableDataPanel.setStylePrimaryName("centerPanel");
        showTableDataPanel.add(dataFlexTable);
        getUtils().hideLoadingPopUp();
      }
    });

  }

  public void reloadXml() {
    getUtils().showLoadingPopUp();
    adminService.reloadXml(new AsyncCallback<Boolean>() {
      @Override
      public void onFailure(Throwable caught) {
        setErrorMessage("Reload xml failed with : " + caught.getMessage());
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(Boolean result) {
        processXml.getProcessNames();
        setSuccessMessage("XML was reloaded");
        getUtils().hideLoadingPopUp();
      }
    });

  }

  public void reloadUsers() {
    getUtils().showLoadingPopUp();
    adminService.reloadUsers(new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        setErrorMessage("Reload users failed with : " + caught.getMessage());
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(Void result) {
        setSuccessMessage("Users have been reloaded");
        getUtils().hideLoadingPopUp();
      }
    });
  }

  private void sendEmails() {
    getUtils().showLoadingPopUp();
    adminService.doSurveyInvitations(new AsyncCallback<Integer>() {

      @Override
      public void onFailure(Throwable caught) {
        setErrorMessage(caught.getMessage());
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(Integer result) {
        setSuccessMessage(result + " emails sent");
        getUtils().hideLoadingPopUp();
      }
    });

  }

  private void clearProcesses() {
    getUtils().showLoadingPopUp();
    adminService.clearProcesses(new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        setErrorMessage(caught.getMessage());
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(Void result) {
        setSuccessMessage("Processes have been cleared");
        getUtils().hideLoadingPopUp();
      }
    });

  }

  private void showProcesses() {
    getUtils().showLoadingPopUp();
    adminService.getRunningProcesses(new AsyncCallback<ArrayList<ProcessInfo>>() {

      @Override
      public void onFailure(Throwable caught) {
        setErrorMessage(caught.getMessage());
        getUtils().hideLoadingPopUp();
      }

      @Override
      public void onSuccess(ArrayList<ProcessInfo> result) {
        setSuccessMessage(result.size() + " processes found");
        VerticalPanel processPanel = new VerticalPanel();
        processTable.clear();
        for (int i = 0; i < result.size(); i++) {
          processTable.setText(i, 0, result.get(i).getProcessName());
          processTable.setText(i, 0, result.get(i).getDateStarted().toString());
          processTable.setText(i, 0, result.get(i).getUserId());
        }
        processTable.setVisible(true);
        Button closeButton = new Button("close");
        closeButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            processPopUp.hide(false);
          }
        });
        processPanel.add(processTable);
        processPanel.add(closeButton);
        processPopUp.setWidget(processPanel);
        processPopUp.center();
        processPopUp.show();
        getUtils().hideLoadingPopUp();
      }
    });

  }

  public String getName() {
    return "View tables";
  }

  public String getDescription() {
    return "View data in tables";
  }

  public void onSelect() {
    // where we can do something when selected
  }

  @Override
  public String serviceName() {
    return Constants.ROLE_DEVELOPER;
  }

  private void makeTestPanel(final int testNumber) {
    subPanel.clear();
    colorListLabel = new Label("Background");
    if (testNumber == 3) {
      colorListLabel = new Label("Highlights");
    }
    if (testNumber == 1 || testNumber == 2 || testNumber == 3) {
      if (testNumber == 1) {
        studyCodeList = new ListBox();
        studyCodeList.addItem("-- Select study --");
      } else if (testNumber == 2) {
        studyCodeList = new ListBox();
        studyCodeList.setMultipleSelect(true);
        studyCodeList.addItem("-- Select studies --");
      } else if (testNumber == 3) {
        studyCodeList = new ListBox();
        studyCodeList.addItem("Body Map");
      }
      adminService.getStudies(true, new AsyncCallback<ArrayList<Study>>() {
        @Override
        public void onFailure(Throwable caught) {
          Window.alert("Failed " + caught.getMessage());
        }

        @Override
        public void onSuccess(ArrayList<Study> result) {
          if (result == null || result.size() < 1) {
            studyCodeList.addItem("No study codes were found");
          } else {
            studies = result;
            if (testNumber == 1 || testNumber == 2) {
              for (Study aResult : result) {
                if (aResult.getTitle() != null) {
                  studyCodeList.addItem(aResult.getTitle());
                } else {
                  studyCodeList.addItem(aResult.getStudyDescription());
                }
              }
            }
            studyCodeList.setSelectedIndex(0);
          }
        }
      });
      bandList.setSelectedIndex(0);
      Grid grid = new Grid(8, 3);
      grid.setWidget(0, 0, patientIdLabel);
      grid.setWidget(0, 1, patientId);
      grid.setWidget(1, 0, studyCodeLabel);
      grid.setWidget(1, 1, studyCodeList);
      grid.setWidget(2, 0, titleLabel);
      grid.setWidget(2, 1, title);
      grid.setWidget(2, 2, optionalLabel);
      grid.setWidget(3, 0, colorComboLabel);
      grid.setWidget(3, 1, colorComboList);
      grid.setWidget(4, 0, bandLabel);
      grid.setWidget(4, 1, bandList);
      grid.setWidget(5, 0, colorListLabel);
      Button colorPickerButton = new Button(" ");
      colorPickerButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          colorPicker.center();
          colorPicker.show();
        }
      });
      grid.setWidget(5, 1, colorPickerButton);
      grid.setWidget(6, 0, scaleLabel);
      grid.setWidget(6, 1, scale);
      grid.setWidget(grid.getRowCount() - 1, 0, runTest1Button);
      grid.setWidget(grid.getRowCount() - 1, 1, closeTestButton);
      grid.getColumnFormatter().setWidth(0, "300px");
      grid.getColumnFormatter().setWidth(1, "400px");
      for (int row = 0; row < grid.getRowCount(); row++) {
        grid.getCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT,
            HasVerticalAlignment.ALIGN_MIDDLE);
        grid.getCellFormatter().setAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT,
            HasVerticalAlignment.ALIGN_MIDDLE);
      }
      subPanel.add(grid);
      grid.setCellPadding(10);
      grid.setCellSpacing(10);
    } else {
      subPanel.add(closeTestButton);
    }
    centerPanel.remove(scrollPanel);
    centerPanel.add(subPanel);
  }

  private String getSelectedStudies() {
    if (studyCodeList.getItemCount() > 1) {
      StringBuilder selectList = new StringBuilder();
      for (int s = 0; s < studyCodeList.getItemCount(); s++) {
        if (studyCodeList.isItemSelected(s)) {
          if (selectList.length() > 0) {
            selectList.append(",");
          }
          selectList.append(studies.get(s - 1).getStudyCode());
        }
      }
      return selectList.toString();
    } else if (studyCodeList.getItemCount() == 1) {
      for (Study study : studies) {
        if (study.getStudyDescription() != null && "bodymap".equals(study.getStudyDescription())) {
          return study.getStudyCode().toString();
        }
      }
    }
    return "";
  }

  private ConfigurationOptions getTestOptions() {
    Boolean TRUE = true;
    ConfigurationOptions options = new ConfigurationOptions(ConfigurationOptions.CHART_TEST);
    if (!bandList.isItemSelected(1)) {
      options.setOption(ConfigurationOptions.OPTION_BANDING, TRUE);
    }
    if (colorComboList.isItemSelected(1)) {
      options.setOption(ConfigurationOptions.OPTION_GRAY, TRUE);
    }
    int background = colorPicker.getSelected();
    options.setOption(ConfigurationOptions.OPTION_BACKGROUND_COLOR, background);
    if (scale.getValue() != null && scale.getValue().length() > 0) {
      options.setOption(ConfigurationOptions.OPTION_SCALE_IMAGES, scale.getValue());
    }
    return options;
  }
}
