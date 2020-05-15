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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.google.gwt.resources.client.ImageResource;

/**
 * GWT resources for the widgets package.
 */
public interface RegistryResources extends ClientBundle {
  RegistryResources INSTANCE = GWT.create(RegistryResources.class);

  @Source("Registry.css")
  RegistryCssResource css();

  @Source("clean.css")
  CleanCssResource cssGwt();

  @Source("images/hborder.png")
  DataResource hborder();

  @Source("images/vborder.png")
  DataResource vborder();

  @Source("images/circles.png")
  DataResource circles();

  @Source("images/corner.png")
  DataResource corner();

  @Source("images/thumb_vertical.png")
  DataResource thumbVertical();

  @Source("images/thumb_horz.png")
  DataResource thumbHorizontal();

  @Source("images/loader.gif")
  ImageResource loadingImage();

  //@Source("images/logo_header_sm2.gif")
  //ImageResource logo();

  @Source("images/ChoirLogo20150529.svg")
  @MimeType("image/svg+xml")
  DataResource choirLogo();

  @Source("images/cancel.png")
  ImageResource cancel();

  @Source("images/close.png")
  ImageResource close();

  @Source("images/accept.png")
  ImageResource accept();

  @Source("images/exclamation.png")
  ImageResource decline();

  @Source("images/application_view_detail.png")
  ImageResource detail();

  @Source("images/vcard_edit.png")
  ImageResource edit();

  @Source("images/application_form_edit.png")
  ImageResource editForm();

  @Source("images/application_form_add.png")
  ImageResource add();

  @Source("images/disk.png")
  ImageResource save();

  @Source("images/delete.png")
  ImageResource delete();

  @Source("images/bullet_toggle_plus.png")
  ImageResource plus();

  @Source("images/calendar.png")
  ImageResource calendar();

  @Source("images/report.png")
  ImageResource report();

  @Source("images/email_go.png")
  ImageResource email_go();

  @Source("images/calendar_edit.png")
  ImageResource calendarEdit();

  @Source("images/page.png")
  ImageResource page();

  @Source("images/page_edit.png")
  ImageResource pageEdit();

  @Source("images/printer.png")
  ImageResource printer();

  @Source("images/tick.gif")
  ImageResource tick();
}
