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

package edu.stanford.survey.client.ui;

import com.google.gwt.resources.client.CssResource;

/**
 * Compiler checked CSS names.
 */
public interface SurveyCss extends CssResource {
  String errorMessage();

  String fullWidthUnlabeledFieldset();

  String unlabeledFieldset();

  String errorHighlight();

  String continueButton();

  String stopButton();

  String labeledNumericScale();

  String unlabeledNumericScale();

  @ClassName("ui-btn-left")
  String uiBtnLeft();

  @ClassName("ui-link-form")
  String uiLinkForm();

  @ClassName("ui-link-title")
  String uiLinkTitle();

  @ClassName("ui-choir-logo-right")
  String uiChoirLogoRight();

  @ClassName("ui-stanford-logo-left")
  String uiStanfordLogoLeft();

}
