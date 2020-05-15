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

package edu.stanford.registry.server.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;

public class Spreadsheet {
  // private static Logger logger = Logger.getLogger(Spreadsheet.class);

  private HashMap<String, Integer> headerMap;
  private ArrayList<Cell> header = null;
  private ArrayList<ArrayList<Cell>> rows = new ArrayList<>();

  public Spreadsheet(ArrayList<Cell> headerRow) {
    header = headerRow;
    headerMap = new HashMap<>();

    if (header != null && header.size() > 0) {
      for (int i = 0; i < headerRow.size(); i++) {
        String colValue = header.get(i).toString().trim();
        headerMap.put(colValue, i);
      }
    }
  }

  public void addRow(ArrayList<Cell> nextRow) {
    boolean addRow = false;
    // make sure there's something in the row or don't add it
    if (nextRow != null && nextRow.size() > 0) {
      for (Cell aNextRow : nextRow) {
        if (aNextRow != null && aNextRow.toString() != null) {
          addRow = true;
        }
      }
    }
    if (addRow) {
      rows.add(nextRow);
    }
  }

  public ArrayList<Cell> getRow(int i) {
    return rows.get(i);
  }

  public Cell getColumnName(int c) {
    return header.get(c);
  }

  public int getColumnIndex(String columnName) {
    if (header != null && headerMap.get(columnName) != null) {
      return headerMap.get(columnName);
    }
    return -1;
  }

  public int size() {
    return rows.size();
  }

  public Object getValue(int row, int column) {
    return getRow(row).get(column);
  }
}
