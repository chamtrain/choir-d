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

import com.google.gwt.user.client.ui.RadioButton;

public class PROMISQuestionButton extends RadioButton {

  private String itemResponseOID;
  private String response;
  private int index = -1;

  public PROMISQuestionButton(String group, String label) {
    super(group, label);
  }

  public PROMISQuestionButton(String label) {
    super(label);
  }

  public String getItemResponseOID() {
    return itemResponseOID;
  }

  public void setItemResponseOID(String oid) {
    itemResponseOID = oid;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String resp) {
    response = resp;
  }

  public void setAnswerIndex(int indx) {
    index = indx;
  }

  public int getAnswerIndex() {
    return index;
  }
}
