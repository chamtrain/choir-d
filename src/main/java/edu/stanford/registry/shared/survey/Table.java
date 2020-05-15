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

package edu.stanford.registry.shared.survey;

import java.io.Serializable;
import java.util.ArrayList;

public class Table implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 2777922367211832020L;
  ArrayList<String> headings = new ArrayList<>();
  ArrayList<TableRow> rows = new ArrayList<>();
  int width = 0;
  int border = 0;

  public ArrayList<TableRow> getRows() {
    return rows;
  }

  public void setRows(ArrayList<TableRow> rows) {
    this.rows = rows;
  }

  public void addRow(TableRow row) {
    rows.add(row);
  }

  public ArrayList<String> getHeadings() {
    return headings;
  }

  public void setHeadings(ArrayList<String> headings) {
    this.headings = headings;
  }

  public void addHeading(String heading) {
    headings.add(heading);
  }

  public int getNumberRows() {
    return rows.size();
  }

  public int getNumberHeadings() {
    return headings.size();
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getBorder() {
    return border;
  }

  public void setBorder(int border) {
    this.border = border;
  }

}
