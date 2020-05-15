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

package edu.stanford.registry.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

/**
 * GWT resources for the widgets package.
 */
public interface WidgetResources extends ClientBundle {
  WidgetResources INSTANCE = GWT.create(WidgetResources.class);

  @Source("widgets.css")
  WidgetsCssResource css();

  //@Source("icons/130.png")
  //ImageResource minusIcon();

  //@Source("icons/129.png")
  //ImageResource plusIcon();


  // @Source("images/loading.gif")
  // ImageResource loading();

  //@Source("images/logo.gif")
  //ImageResource logo();

  //@Source("images/page.png")
  //ImageResource page();


}
