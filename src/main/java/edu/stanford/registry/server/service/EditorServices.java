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

package edu.stanford.registry.server.service;

import edu.stanford.registry.shared.EmailContentType;

import java.io.IOException;
import java.util.ArrayList;

public interface EditorServices {
  String getEmailTemplate(String processType) throws IOException;

  ArrayList<String> getEmailTemplatesList();

  String updateEmailTemplate(String processType, String contents);

  Boolean updateEmailContentType (String templateName, EmailContentType contentType);

  EmailContentType getEmailContentType(String templateName);

}
