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

public class TableRow implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 6517625498594581341L;
  private static int columnGap = 0;

  public TableRow() {

  }

  public TableRow(int width) {
    setWidth(width);
  }

  int width = 0;
  ArrayList<TableColumn> columns = new ArrayList<>();

  public ArrayList<TableColumn> getColumns() {
    return columns;
  }

  public int size() {

    return columns.size();
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void addColumn(TableColumn column) {
    columns.add(column);

  }
  
  public int getColumnGap() {
    return columnGap;
  }
  
  public void setColumnGap(int spaceBetweenColumns) {
    columnGap = spaceBetweenColumns;
  }
}
