/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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
import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryEntryPoint;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.service.AdminService;
import edu.stanford.registry.client.service.AdminServiceAsync;
import edu.stanford.registry.client.survey.FormWidgets;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.ConfigParam;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.RegConfigProperty;
import edu.stanford.registry.shared.RegConfigUsage;

import java.util.ArrayList;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.Tooltip;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.Placement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class AppConfigEditorWidget extends TabWidget implements ClickHandler {

  // service to get/put config values
  private final AdminServiceAsync adminService = GWT.create(AdminService.class);

  // display components
  private final DockLayoutPanel dockPanel = new DockLayoutPanel(Unit.EM);
  private final FlowPanel mainPanel = new FlowPanel();
  private final FlowPanel detailPanel = new FlowPanel();
  private final HorizontalPanel categoryHeadingPanel = new HorizontalPanel();
  private final HorizontalPanel paramHeadingPanel = new HorizontalPanel();
  private final HorizontalPanel paramValuePanel = new HorizontalPanel();
  private final HorizontalPanel commandButtonBar = new HorizontalPanel();
  private final HorizontalPanel defaultValuePanel = new HorizontalPanel();
  private final HorizontalPanel cachedValuePanel = new HorizontalPanel();
  private final VerticalPanel parameterPanel = new VerticalPanel();


  private final FormLabel categoryLabel = FormWidgets.formLabelFor("Category", "categoryList");
  private final ListBox categoryList = new ListBox();
  private final ArrayList<String> categoryTitles = new ArrayList<>();
  private final ArrayList<ArrayList<RegConfigProperty>> categoryArrList = new ArrayList<>(); // by group
  private final Tooltip categoryToolTip = new Tooltip();
  private final FormLabel messageLabel = new FormLabel();

  private final FormLabel paramLabel = FormWidgets.formLabelFor("Parameter", "paramTextBox");
  private final ListBox paramList = new ListBox();
  private final Button updConfigButton = new Button("Update");
  private final Button disableConfigButton = new Button("Disable");
  private final Button enableconfigButton = new Button("Enable");
  private final FormLabel paramValueLabel = FormWidgets.formLabelFor("Site value", "paramTextBox");
  private final Tooltip paramTypeToolTip = new Tooltip();
  private final TextArea paramValueText = new TextArea();
  private final FormLabel defaultValueLabel = FormWidgets.formLabelFor("Default value", "defTextBox");
  private final TextArea defaultValueText = new TextArea();
  private final FormLabel cachedValueLabel = FormWidgets.formLabelFor("Cached value", "cacheTextBox");
  private final TextArea cacheValueText = new TextArea();
  private final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private final ScrollPanel paramValueScroll = new ScrollPanel();

  private final ErrorDialogWidget basicErrorPopUp = new ErrorDialogWidget();
  private ConfigParam saveParam = null;

  public AppConfigEditorWidget(ClinicUtils utils) {
    super(utils);
    new RegistryEntryPoint().setServiceEntryPoint(adminService, "admin", null);
    initWidget(dockPanel);
    load();
  }

  @Override
  public void load() {
    getRegConfigProperties();
    setEmptyMessage();

    updConfigButton.addClickHandler(this);
    updConfigButton.setEnabled(false);
    updConfigButton.setVisible(true);
    styleButton(updConfigButton);

    disableConfigButton.addClickHandler(this);
    disableConfigButton.setVisible(false);
    styleButton(disableConfigButton);

    enableconfigButton.addClickHandler(this);
    enableconfigButton.setVisible(false);
    styleButton(enableconfigButton);

    // Create the category selection panel
    styleRightLabel(categoryLabel);
    styleListBox(categoryList);
    styleSelectPanel(categoryHeadingPanel);
    styleLeftLabel(messageLabel);
    categoryHeadingPanel.add(categoryLabel);
    categoryToolTip.setWidget(categoryList);
    categoryHeadingPanel.add(categoryToolTip);
    categoryHeadingPanel.add(messageLabel);

    // Create the parameter selection panel
    styleRightLabel(paramLabel);
    styleListBox(paramList);
    styleSelectPanel(paramHeadingPanel);
    styleCommandBar(commandButtonBar);
    //paramLabel.setStylePrimaryName(RegistryResources.INSTANCE.css().clTabPgHeadingBarLabel());
    paramHeadingPanel.add(paramLabel);
    commandButtonBar.add(updConfigButton);
    commandButtonBar.add(disableConfigButton);
    commandButtonBar.add(enableconfigButton);
    paramTypeToolTip.setWidget(paramList);
    paramHeadingPanel.add(paramTypeToolTip);
    paramHeadingPanel.add(commandButtonBar);
    setParmListChangeHandler();

    // Create the scrollable panel for the parameter value
    styleValuePanel(paramValuePanel);
    styleRightLabel(paramValueLabel);
    styleTextBox(paramValueText);
    paramValueText.setId("paramTextBox");
    paramValueText.setVisibleLines(2);
    paramValueText.setCharacterWidth(500);
    paramValueText.addChangeHandler(getResizeandler(paramValueText));
    paramValuePanel.add(paramValueLabel);
    paramValuePanel.add(paramValueText);

    // Create the default panel
    styleValuePanel(defaultValuePanel);
    styleTextBox(defaultValueText);

    defaultValueText.setId("defTextBox");
    defaultValueText.setVisibleLines(2);
    defaultValueText.setEnabled(false);
    styleRightLabel(defaultValueLabel);
    defaultValuePanel.add(defaultValueLabel);
    defaultValuePanel.add(defaultValueText);
    defaultValuePanel.setVisible(false);

    // Create the cached value panel
    styleValuePanel(cachedValuePanel);
    styleTextBox(cacheValueText);
    cacheValueText.setId("cacheTextBox");
    cacheValueText.setVisibleLines(2);
    cacheValueText.setEnabled(false);
    cacheValueText.addChangeHandler(getResizeandler(cacheValueText));
    styleRightLabel(cachedValueLabel);
    cachedValuePanel.add(cachedValueLabel);
    cachedValuePanel.add(cacheValueText);
    cachedValuePanel.setVisible(false);

    parameterPanel.add(paramValuePanel);
    parameterPanel.add(defaultValuePanel);
    parameterPanel.add(categoryHeadingPanel);
    parameterPanel.add(cachedValuePanel);
    parameterPanel.setWidth("100%");
    paramValueScroll.add(parameterPanel);

    // Assemble the detail panel
    detailPanel.add(categoryHeadingPanel);
    detailPanel.add(paramHeadingPanel);
    detailPanel.add(paramValueScroll);

    // Assemble the main page
    dockPanel.setWidth("100%");
    dockPanel.addNorth(getMessageBar(), 3);
    mainPanel.setWidth("100%");

    mainPanel.add(detailPanel);
    dockPanel.add(mainPanel);
  }

  @Override
  public void onClick(ClickEvent event) {
    Widget sender = (Widget) event.getSource();
    if (sender == updConfigButton) {
      updateConfig();
    }
    if (sender == disableConfigButton) {
      disableConfig();
    }
  }
  private void updateConfig() {
    updateConfig(true);
  }
  private boolean updateConfig(boolean isEnabled) {

    String paramValue = paramValueText.getValue();
    if (paramValue.isEmpty()) {
      if (isEnabled) {
        setErrorMessage("Not updated. No parameter value was provided.");
      } else {
          setErrorMessage("Nothing to disable. Parameter has not been set");
      }

      return false;
    } else if (!isEnabled && saveParam == null) {
        setErrorMessage("No previous value to remove!");
        return false;
      }


    ConfigParam param = new ConfigParam();
    param.setConfigName(paramList.getSelectedValue());
    param.setConfigValue(paramValueText.getValue());
    param.setEnabled(isEnabled);
    ArrayList<RegConfigProperty> properties = categoryArrList.get(categoryList.getSelectedIndex());
    for (final RegConfigProperty rcp : properties) {
      if (rcp.getName().equalsIgnoreCase(paramList.getSelectedValue())) {
        param.setConfigType(rcp);
      }
    }
    if (saveParam != null) {
      param.setConfigId(saveParam.getConfigId());
    }


    adminService.updateConfig(param, new AsyncCallback<ConfigParam>() {
      @Override
      public void onFailure(Throwable caught) {
        basicErrorPopUp.setText("Encountered an error! ---- Your value has not been saved!" );
        basicErrorPopUp.setError(caught.getMessage());
        caught.printStackTrace();
      }

      @Override
      public void onSuccess(ConfigParam param) {
        setSuccessMessage("Database has been updated!");
      }
    });
    return true;
  }

  private void disableConfig() {

    if (updateConfig(false)) {
      disableConfigButton.setVisible(false);
      enableconfigButton.setVisible(true);
    }
  }

  private void getRegConfigProperties() {
    adminService.getRegConfigProperties(new AsyncCallback<ArrayList<RegConfigProperty>>() {
      @Override
      public void onFailure(Throwable caught) {
          basicErrorPopUp.setText("Encountered the following error getting the Registry Configuration Property List");
          basicErrorPopUp.setError(caught.getMessage());
          caught.printStackTrace();
      }

      @Override
      public void onSuccess(ArrayList<RegConfigProperty> result) {
      // create the categories list
        String catTitle = null;
        ArrayList<RegConfigProperty> properties = new ArrayList<>();

        for (RegConfigProperty rp: result) {
          if (!rp.getCategory().getTitle().equals(catTitle)) {
            if (catTitle != null) {
              categoryArrList.add(properties);
              GWT.log("adding " + String.valueOf(properties.size()) + " properites to index " + String.valueOf(categoryArrList.size()-1));
              properties = new ArrayList<>();
            }

            catTitle = rp.getCategory().getTitle();
            if (rp.getCategory().getDesc()!= null && !rp.getCategory().getDesc().isEmpty()) {
              categoryList.addItem( catTitle, catTitle );
              categoryTitles.add( rp.getCategory().getDesc());
            } else {
              categoryList.addItem(catTitle, catTitle);
              categoryTitles.add(catTitle);
            }

          }
          properties.add(rp);
        }
        categoryArrList.add(properties); // add the last one
        if (categoryList.getItemCount() > 0) {
            GWT.log("handling inital category" );
           fillParamList(0);

        }
        setCategoryTip(0);
        categoryList.addChangeHandler(new ChangeHandler() {
          @Override
          public void onChange(ChangeEvent event) {
            int catIndex = categoryList.getSelectedIndex();
             fillParamList(catIndex); // whenever the category changes build the parameter list
            setCategoryTip(catIndex);
          }
        });
      }
    });
  }

  @Override
  public String serviceName() {
    return Constants.ROLE_DEVELOPER;
  }

  private void fillParamList(int catIndex) {
    GWT.log("fillParmList called for " + categoryList.getSelectedValue() + "index=" + catIndex);
     paramList.clear();
    ArrayList<RegConfigProperty> properties = categoryArrList.get(catIndex);
    if (properties != null && properties.size() > 0) {
      GWT.log("category has " + properties.size() + " properties");
      for (RegConfigProperty rp: properties) {
        paramList.addItem(rp.getName() + " [" + rp.getUsageAbbrev() + "]", rp.getName());
      }
      if (paramList.getItemCount() > 0) {
        paramList.setSelectedIndex(0);
        paramList.fireEvent(new ChangeEvent() { // this is so we look up the parameter shown
        });
      }
    }
    styleListBox(paramList);
  }

  private void setParmListChangeHandler() {
    paramList.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updConfigButton.setEnabled(false);
        disableConfigButton.setVisible(false);
        enableconfigButton.setVisible(false);
        ArrayList<RegConfigProperty> properties = categoryArrList.get(categoryList.getSelectedIndex());
        for (final RegConfigProperty rcp : properties) {
          if (rcp.getName().equalsIgnoreCase(paramList.getSelectedValue())) {
            setEmptyMessage();
            adminService.getConfig(rcp, new AsyncCallback<ConfigParam>() {
              @Override
              public void onFailure(Throwable caught) {
                GWT.log("property not found for " + rcp.getName());
              }

              @Override
              public void onSuccess(ConfigParam result) {
                if (result != null) {
                  saveParam = result;
                  paramValueText.setText(result.getConfigValue());
                  GWT.log("setting value to " + result.getConfigValue());
                  updConfigButton.setEnabled(true);
                  if (result.getConfigValue() != null && result.isEnabled()) {
                    disableConfigButton.setVisible(true);
                  }
                  if (result.getConfigValue() != null && !result.isEnabled()) {
                    enableconfigButton.setVisible(true);
                  }
                  if (result.getConfigValue() != null) {
                    setVisibleLines(paramValueText);
                  }
                }
                if (rcp.getDefValue() != null  && !rcp.getDefValue().isEmpty()) {
                  defaultValueText.setText(rcp.getDefValue());
                  defaultValuePanel.setVisible(true);
                } else {
                  defaultValuePanel.setVisible(false);
                }
                if (result != null && result.getCachedValue() != null && !result.getCachedValue().isEmpty()) {
                  cacheValueText.setText(result.getCachedValue());
                  cachedValuePanel.setVisible(true);
                } else {
                  cachedValuePanel.setVisible(false);
                }
                if (rcp.getUsageAbbrev().equals("ST") || rcp.getUsageAbbrev().equals("GL")) {
                  paramValueText.setEnabled(false);
                } else {
                  paramValueText.setEnabled(true);
                }
                setUsageTip(rcp.getUsage());
                if (rcp.getUsageAbbrev().equalsIgnoreCase(RegConfigUsage.Static.abbrev)
                    || rcp.getUsageAbbrev().equalsIgnoreCase(RegConfigUsage.Global.abbrev)
                    || rcp.getUsageAbbrev().equalsIgnoreCase(RegConfigUsage.StaticRec.abbrev)
                    || rcp.getUsageAbbrev().equalsIgnoreCase(RegConfigUsage.Retired.abbrev)) {
                  messageLabel.setText("Parameter is view only");
                  updConfigButton.setEnabled(false);
                  disableConfigButton.setVisible(false);
                  enableconfigButton.setVisible(false);
                  paramValueText.setEnabled(false);
                  updConfigButton.getElement().setPropertyString("style", "cursor: no-drop");
                } else {
                  messageLabel.setText("");
                  updConfigButton.setEnabled(true);
                  paramValueText.setEnabled(true);
                  updConfigButton.getElement().setPropertyString("style", "cursor: pointer");
                }
              }
            });
          }
        }
      }
    });
  }
  private void styleListBox(ListBox listBox) {
    listBox.addStyleName(css.clTabPgHeadingSelectBar());
    listBox.addStyleName(css.clTabPgHeadingBarText());
    listBox.addStyleName(css.clTabPgHeadingBarList());
    listBox.addStyleName(css.leftLabel());
    listBox.setWidth("350px");
    listBox.setHeight("28px");
  }

  private void styleSelectPanel(Panel panel) {
    panel.addStyleName(css.clTabPgHeadingBar());
    panel.addStyleName(css.emailTemplateHeadingBar());
    panel.setWidth("760px");
  }

  private void styleCommandBar(Panel panel) {
    panel.addStyleName(css.clTabPgHeadingBar());
    panel.addStyleName(css.emailTemplateHeadingBar());
    panel.setWidth("180px");
  }

  private void styleValuePanel(Panel panel) {
    panel.addStyleName(css.emailTemplateHeadingBar());
    panel.setWidth("760px");
  }

  private void styleLabel(FormLabel label) {
    label.addStyleName(css.clTabPgHeadingBarLabel());
    label.addStyleName(css.clTabPgHeadingBarText());


  }
  private void styleLeftLabel(FormLabel label) {
    label.addStyleName(css.leftLabel());
     styleLabel(label);
    label.setWidth("180px");
  }
  private void styleRightLabel(FormLabel label) {
    label.addStyleName(css.alignRightLabel());
    styleLabel(label);
    label.setWidth("80px");
  }

  private void styleTextBox(TextArea box) {
    box.getElement().setAttribute("style", "margin-left: 20px; width: 655px;");
    box.addStyleName(css.leftButton());
    box.addStyleName(css.clTabPgHeadingBarList());
  }

  private void styleButton(Button button) {
    button.setStyleName(css.actionButton());
    button.setWidth("180px");
    button.setSize(ButtonSize.EXTRA_SMALL);
  }
  private void setUsageTip(RegConfigUsage usage) {
    paramTypeToolTip.setTitle("["+ usage.abbrev+ "] " + usage.desc);
    paramTypeToolTip.setPlacement(Placement.BOTTOM);
  }

  private void setCategoryTip(int index) {
    if (categoryTitles.size() -1 <index)
      return;
    categoryToolTip.setTitle(categoryTitles.get(index));
    categoryToolTip.setPlacement(Placement.RIGHT);
  }
  private ChangeHandler getResizeandler(final TextArea textArea) {
    return
    new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        setVisibleLines(textArea);
      }
    };
  }

  private void setVisibleLines(TextArea textArea) {
    if (textArea == null || textArea.getValue() == null) {
      return;
    }
    int numchars =textArea.getValue().length();
    int numlines = 2;
    if (numchars > 3600) {
      numlines = 30;
    } else {
      while (numchars > 0) {
        numlines++;
        numchars = numchars - 120;
      }
    }
    textArea.setVisibleLines(numlines);
  }
}
