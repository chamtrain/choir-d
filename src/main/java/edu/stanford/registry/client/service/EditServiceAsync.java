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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EditServiceAsync {

  void getEmailTemplate(String processType, AsyncCallback<String> callback) throws IllegalArgumentException,
      ServiceUnavailableException;

  void getEmailTemplatesList(AsyncCallback<ArrayList<String>> callback);

  void updateEmailTemplate(String processType, String contents, AsyncCallback<String> callback)
      throws IllegalArgumentException, ServiceUnavailableException;

  void getEmailContentType(String templateName, AsyncCallback<EmailContentType> async) throws ServiceUnavailableException;

  void updateEmailContentType(String templateName, EmailContentType contentType, AsyncCallback<Boolean> async) throws ServiceUnavailableException;
}
