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

public class TableColumn implements Serializable {
  private static final long serialVersionUID = -6471666379160245552L;
  private int width = 0;
  private String value = null;


  public TableColumn() {
    // Need no-arg constructor for serialization
  }

  public TableColumn(String text, int widthPct) {
    setValue(text);
    setWidth(widthPct);
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void set(String text, int widthPct) {
    setValue(text);
    setWidth(widthPct);
  }
}
