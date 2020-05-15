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

package edu.stanford.registry.client.service;

import edu.stanford.registry.shared.EmailContentType;
import edu.stanford.registry.shared.ServiceUnavailableException;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;

public interface EditService extends RemoteService {

  String getEmailTemplate(String processType) throws IllegalArgumentException, ServiceUnavailableException;

  ArrayList<String> getEmailTemplatesList();

  String updateEmailTemplate(String processType, String contents) throws IllegalArgumentException,
      ServiceUnavailableException;

  EmailContentType getEmailContentType(String templateName) throws ServiceUnavailableException;

  Boolean updateEmailContentType(String templateName, EmailContentType contentType) throws ServiceUnavailableException;

}
