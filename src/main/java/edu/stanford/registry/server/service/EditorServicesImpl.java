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

import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigDao.ConfigType;
import edu.stanford.registry.server.utils.EmailTemplateUtils;
import edu.stanford.registry.shared.EmailContentType;
import edu.stanford.registry.shared.User;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class EditorServicesImpl implements EditorServices {
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(EditorServicesImpl.class);

  
  private final Supplier<Database> dbp;
  private final User user;
  private final ServerContext context;
  protected SiteInfo siteInfo;

  public EditorServicesImpl(User usr, Supplier<Database> databaseProvider, ServerContext svrContext, SiteInfo siteInfo) {
    dbp = databaseProvider;
    user = usr;
    context = svrContext;
    this.siteInfo = siteInfo;
  }

  @Override
  public String getEmailTemplate(String processType) {
    return new EmailTemplateUtils().getTemplate(siteInfo, processType);
  }

  @Override
  public ArrayList<String> getEmailTemplatesList() {
    return new EmailTemplateUtils().getAllTemplateNames(siteInfo);
  }

  @Override
  public String updateEmailTemplate(String templateName, String contents) {
    String result = new EmailTemplateUtils().updateTemplate(dbp.get(), siteInfo, user, templateName, contents);
    context.reload(false/*users*/, true/*config*/); // tell the cache to sense the change in the database
    return result;
  }

  @Override
  public EmailContentType getEmailContentType(String templateName) {
    return new EmailTemplateUtils().getEmailContentType(dbp.get(), siteInfo, templateName);
  }

  @Override
  public Boolean updateEmailContentType(String templateName, EmailContentType contentType) {
    AppConfigDao appConfigDao = new AppConfigDao(dbp.get(), user);
    Boolean result = appConfigDao.addOrEnableAppConfigEntry(siteInfo.getSiteId(), ConfigType.EMAILCONTENTTYPE, templateName, contentType.toString());
    context.reload(false, true);
    return result;
  }
}
