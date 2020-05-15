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

package edu.stanford.registry.client.widgets;

import edu.stanford.registry.client.RegistryCssResource;
import edu.stanford.registry.client.RegistryResources;
import edu.stanford.registry.client.utils.ClinicUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.Patient;
import edu.stanford.registry.shared.PatientActivity;
import edu.stanford.registry.shared.SurveyRegistration;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class AssessmentActivityTree extends Composite implements SelectionHandler<TreeItem>, OpenHandler<TreeItem>,
    CloseHandler<TreeItem> {

  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private Tree tree;
  private TreeItem root = new TreeItem(SafeHtmlUtils.fromSafeConstant("Root"));
  private ApptRegistration registration;
  private ArrayList<Activity> actList;
  private Patient patient;
  private ClinicUtils utils;
  public String style;

  public AssessmentActivityTree(PatientActivity act, ClinicUtils utils, String style) {
    this.registration = act.getRegistration();
    this.actList = act.getActivities();
    this.patient = act.getPatient();
    this.utils = utils;
    this.style = style;
    initTree();
    HorizontalPanel panel = new HorizontalPanel();
    panel.add(tree);
    initWidget(panel);
  }

  private void initTree() {
    tree = new Tree();
    tree.setStylePrimaryName(css.activityTree());
    root = new TreeItem(SafeHtmlUtils.fromSafeConstant("Root"));
    root.setState(true, true);
    tree.addItem(root);

    ActivityTreeItem header = new ActivityTreeItem("Activity Date / Time", "Action", "By", "tableDataHeader");
    root.addItem(header);
    if (actList != null && actList.size() > 0) {
      for (Activity anActList : actList) {
        ActivityTreeItem item = new ActivityTreeItem(anActList, style);
        root.addItem(item);
      }
    }

    Label dateLbl;

    if (registration.isAppointment()) {
      // get value including time
      dateLbl = new Label(utils.getDateString(registration.getSurveyDt()));
    } else {
      // strip off the time
      dateLbl = new Label(utils.getDateString(new Date(registration.getSurveyDt().getTime())));
    }
    Label surveyTypeLbl = new Label(registration.getSurveyType());
    Label tokenLbl = new Label();
    String token = "";

    @SuppressWarnings("deprecation")
    SurveyRegistration surveyReg = registration.getSurveyReg();
    if (surveyReg != null)
      token = surveyReg.getToken();

    if (patient != null && patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
        && "y".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())) {
      tokenLbl.setText(token);
    } else {
      tokenLbl.setText("xxxxxxxxx");
      if (patient.hasAttribute(Constants.ATTRIBUTE_PARTICIPATES)
          && "n".equals(patient.getAttribute(Constants.ATTRIBUTE_PARTICIPATES).getDataValue())) {
        tokenLbl.setTitle(token + "-");
        tokenLbl.addStyleName(css.serverResponseLabelError());
      } else {
        tokenLbl.setTitle("?" + token + "?");
      }
    }
    dateLbl.setWidth("150px");
    surveyTypeLbl.setWidth("175px");
    tokenLbl.setWidth("100px");

    // Set the registration details on the line
    HorizontalPanel panel = new HorizontalPanel();
    panel.setStylePrimaryName(css.treeDataList());
    panel.add(dateLbl);
    panel.add(surveyTypeLbl);

    if (utils.getProcessXml().isSurveyProcess(registration.getSurveyType())
        || Constants.ACTIVITY_DELETED.equals(registration.getSurveyType())) {
      panel.add(tokenLbl);
    }
    root.setWidget(panel);

    showCollapsed();

    tree.addSelectionHandler(this);
    tree.addOpenHandler(this);
    tree.addCloseHandler(this);
  }

  public void showCollapsed() {

  }

  public void showOpen() {

  }

  @Override
  public void onOpen(OpenEvent<TreeItem> event) {
    showOpen();
  }

  @Override
  public void onSelection(SelectionEvent<TreeItem> event) {

  }

  @Override
  public void onClose(CloseEvent<TreeItem> event) {
    showCollapsed();
  }

}
