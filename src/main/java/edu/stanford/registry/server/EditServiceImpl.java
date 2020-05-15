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

package edu.stanford.registry.server;

import edu.stanford.registry.client.service.EditService;
import edu.stanford.registry.server.service.EditorServices;
import edu.stanford.registry.shared.EmailContentType;
import edu.stanford.registry.shared.ServiceUnavailableException;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class EditServiceImpl extends RemoteServiceServlet implements EditService {
  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(EditServiceImpl.class);

  /**
   * Get the contents of an email template from the file.
   *
   * @param processType Name of the process type
   * @return The contents of the template file.
   */
  @Override
  public String getEmailTemplate(String processType) throws ServiceUnavailableException {
    try {
      return getService().getEmailTemplate(processType);
    } catch (Exception e) {
      logger.error("Error in getEmailTemplatesList - ", e);
      throw new ServiceUnavailableException(e.getMessage());
    }
  }

  /**
   * Get a list of all email templates.
   *
   * @return List of process type name strings.
   */
  @Override
  public ArrayList<String> getEmailTemplatesList() {
    try {
      return getService().getEmailTemplatesList();
    } catch (Exception e) {
      logger.error("Error in getEmailTemplatesList - ", e);
      throw new ServiceUnavailableException(e.getMessage());
    }
  }

  /**
   * Update the email template file. Creates new, or replaces the file if it exists with the string provided.
   *
   * @param contents template content string.
   */
  @Override
  public String updateEmailTemplate(String processType, String contents) throws IllegalArgumentException,
      ServiceUnavailableException {

    try {
      return getService().updateEmailTemplate(processType, contents);
    } catch (Exception e) {
      logger.error("Error in updateEmailTemplate:" + e.getMessage(), e);
      throw new ServiceUnavailableException("Error saving template for " + processType);
    }
  }

  @Override
  public EmailContentType getEmailContentType(String templateName) throws ServiceUnavailableException {
    try {
      return getService().getEmailContentType(templateName);
    } catch (Exception e) {
      logger.error("Error in getEmailContentType:" + e.getMessage(), e);
      throw new ServiceUnavailableException("Error getting content-type for " + templateName);
    }
  }

  @Override
  public Boolean updateEmailContentType(String templateName, EmailContentType contentType) throws ServiceUnavailableException {
    try {
      return getService().updateEmailContentType(templateName, contentType);
    } catch (Exception e) {
      logger.error("Error in updateEmailContentType:" + e.getMessage(), e);
      throw new ServiceUnavailableException("Error saving content-type for " + templateName);
    }
  }

  private EditorServices getService() throws Exception {

    RegistryServletRequest regRequest = (RegistryServletRequest) getThreadLocalRequest();
    return (EditorServices) regRequest.getService();
  }

}
