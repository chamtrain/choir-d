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
import edu.stanford.registry.client.utils.ClientUtils;
import edu.stanford.registry.shared.Activity;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

public class ActivityTreeItem extends Composite {

  private static final RegistryCssResource css = RegistryResources.INSTANCE.css();
  private static final DateTimeFormat dtTm = DateTimeFormat.getFormat("MM/dd/yyyy hh:mm a");

  private final Label lbl1 = new Label();
  private final Label lbl2 = new Label();
  private final Label lbl3 = new Label();
  private final Grid tbl = new Grid(1, 3);

  /**
   * This is a formatted object for displaying an activity on the page.
   *
   * @param act Activity to list
   * @param style String style name to use
   */
  public ActivityTreeItem(Activity act, String style) {
    createTable(ClientUtils.getDateString(dtTm, act.getActivityDt()), act.getActivityType(), act.getDisplayName(), style);

    tbl.getCellFormatter().addStyleName(0, 0, css.dataListFirst());
    tbl.getCellFormatter().addStyleName(0, 1, css.dataList());
    tbl.getCellFormatter().addStyleName(0, 2, css.dataListLast());
    tbl.setBorderWidth(2);

    initWidget(tbl);
  }

  public ActivityTreeItem(String label1, String label2, String label3, String style) {
    createTable(label1, label2, label3, style);

    initWidget(tbl);
  }

  private void createTable(String label1, String label2, String label3, String style) {

    lbl1.setText(label1);
    lbl2.setText(label2);
    lbl3.setText(label3);

    lbl1.setWidth("150px");
    lbl2.setWidth("170px");
    lbl3.setWidth("120px");

    tbl.setWidget(0, 0, lbl1);
    tbl.setWidget(0, 1, lbl2);
    tbl.setWidget(0, 2, lbl3);
    tbl.setStylePrimaryName(css.dataList());

    if (style != null) {
      tbl.getRowFormatter().addStyleName(0, style);
    }

    tbl.getCellFormatter().setStylePrimaryName(0, 0, css.treeDataList());
    tbl.getCellFormatter().setStylePrimaryName(0, 1, css.treeDataList());
    tbl.getCellFormatter().setStylePrimaryName(0, 2, css.treeDataList());
  }
}
