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

package edu.stanford.registry.client.xform;

import edu.stanford.registry.shared.survey.RegistryAnswer;
import edu.stanford.registry.shared.survey.SurveyAnswerIntf;

import java.util.ArrayList;
import java.util.Set;

import com.google.gwt.user.client.ui.Composite;

public class XMLWidget extends Composite implements SurveyAnswerIntf {
  String name, tag, label, reference, clientId;
  RegistryAnswer registryAnswer;

  public XMLWidget() {
    registryAnswer = new RegistryAnswer();
  }

  public void setName(String name) {
    registryAnswer = new RegistryAnswer();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getTag() {
    return tag;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getReference() {
    return reference;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public ArrayList<String> getText() {
    return registryAnswer.getText();
  }

  @Override
  public void addText(String answer) {
    registryAnswer.addText(answer);

  }

  @Override
  public int getType() {
    return registryAnswer.getType();
  }

  @Override
  public void setType(int type) {
    registryAnswer.setType(type);
  }

  @Override
  public Set<String> getAttributeKeys() {
    return registryAnswer.getAttributeKeys();
  }

  @Override
  public String getAttribute(String key) {
    return registryAnswer.getAttribute(key);
  }

  @Override
  public void setAttribute(String key, String value) {
    registryAnswer.setAttribute(key, value);
  }

  protected boolean hasNonEmptyAttribute(RegistryAnswer answer, String name) {
    if (answer.hasAttribute(name) && answer.getAttribute(name).trim().length() > 0) {
      return true;
    }
    return false;

  }

  @Override
  public ArrayList<String> getResponse() {
    return new ArrayList<>();
  }

  @Override
  public boolean getSelected() {
    return false;
  }

  @Override
  public String getClientId() {
    return clientId;
  }

  @Override
  public void setClientId(String id) {
    clientId = id;

  }

}
