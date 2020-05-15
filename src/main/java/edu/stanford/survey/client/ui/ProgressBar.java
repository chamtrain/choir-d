/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HTML;

public class ProgressBar extends HTML
{
  public ProgressBar()
  {
    super("<progress class='Progress-main'></progress>");
  }

  /**
   * Set the progress indicator the the specified values
   * @param value Current progress value
   * @param max Target/complete value
   */
  @SuppressWarnings("WeakerAccess")
  public void setProgress(int value, int max)
  {
    Element progressElement = getElement().getFirstChildElement();
    progressElement.setAttribute("value", String.valueOf(value));
    progressElement.setAttribute("max", String.valueOf(max));
    progressElement.setAttribute("id", "progress");
    progressElement.setAttribute("value", String.valueOf(value)); // Need to set twice to get IE11 to work correctly
  }

  public void setPercent(double percent) {
    Double percentDbl = percent;
    setProgress(percentDbl.intValue(), 100);
  }


//  /**
//   * Remove the progress indicator values.  On firefox, this causes the
//   * progress bar to sweep back and forth.
//   */
//  public void clearProgress()
//  {
//    Element progressElement = getElement().getFirstChildElement();
//    progressElement.removeAttribute("value");
//    progressElement.removeAttribute("max");
//  }
// --Not used, leaving in-case wanted in future
}
