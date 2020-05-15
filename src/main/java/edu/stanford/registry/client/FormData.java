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

package edu.stanford.registry.client;

import java.util.ArrayList;

public class FormData {

  private boolean required = false;
  private int occurance = 1;
  private ArrayList<FormData> associationsList = new ArrayList<>();
  private int dataType = 0;

  public FormData() {

  }

  public FormData(boolean isRequired) {
    setRequired(isRequired);
  }

  public void setRequired(boolean isRequired) {
    required = isRequired;
  }

  public boolean isRequired() {
    return required;
  }

  public void setOccurrance(int occurs) {
    occurance = occurs;
  }

  public int getOccurrance() {
    return occurance;
  }

  public void addAssociation(FormData fd) {
    associationsList.add(fd);
  }

  public ArrayList<FormData> getAssociations() {
    return associationsList;
  }

  public void setDataType(int dtype) {
    dataType = dtype;
  }

  public int getDataType() {
    return dataType;
  }
}
