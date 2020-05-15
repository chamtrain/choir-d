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
package com.sksamuel.jqm4gwt.form.elements;

import com.google.gwt.safehtml.shared.SafeHtml;
/**
 *  Created 03/2018 by tpacht. To add support for HTML tags in the label
 */
public class CustomJQMText extends JQMText {

  public CustomJQMText() {
    this(null);
  }

  public CustomJQMText(SafeHtml html) {
    super();
    if (html != null) {
      label.setHTML(html.asString());
    }
  }
}
