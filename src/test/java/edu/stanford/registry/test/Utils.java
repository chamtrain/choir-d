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

package edu.stanford.registry.test;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;
import edu.stanford.registry.server.database.AssessDao;
import edu.stanford.registry.server.security.UserInfo;
import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.server.utils.SurveyRegUtils;
import edu.stanford.registry.shared.ApptRegistration;
import edu.stanford.registry.shared.Constants;
import edu.stanford.registry.shared.User;

import java.util.Date;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.github.susom.database.Database;

public class Utils {
  private static Logger logger = Logger.getLogger(Utils.class);
  
  final protected Database database;
  final protected SurveyRegUtils regUtils;
  final protected SiteInfo siteInfo;

  public Utils(Database db, SiteInfo siteInfo) {
    database = db;
    this.siteInfo = siteInfo;
    regUtils = new SurveyRegUtils(siteInfo);
  }

  public ApptRegistration addInitialRegistration(Database database, String mrn, Date apptTime, String email,
                                                   String visitCode) {
    ApptRegistration registration = getInitialRegistration(mrn, apptTime, email, visitCode);
    AssessDao assessDao = new AssessDao(database, siteInfo);
    return regUtils.createRegistration(assessDao, registration);
  }

  public ApptRegistration addFollowUpRegistration(Database database, String mrn, Date apptTime, String email,
                                                    String visitCode) {
    ApptRegistration registration = getFollowUpRegistration(mrn, apptTime, email, visitCode);
    AssessDao assessDao = new AssessDao(database, siteInfo);
    return regUtils.createRegistration(assessDao, registration);
  }

  public ApptRegistration getFollowUpRegistration(String mrn, Date apptTime, String email, String visitCode) {
    //PatientAttribute pattribute = patientDao.getAttribute(mrn, Constants.ATTRIBUTE_PARTICIPATES);
    //return getRegistration(mrn, apptTime, email, appt.getVisitType(1, pattribute), visitCode);
    return getRegistration(mrn, apptTime, email, regUtils.getVisitType(1, new Date(apptTime.getTime())), visitCode);
  }

  public ApptRegistration getInitialRegistration(String mrn, Date apptTime, String email, String visitCode) {
    //PatientAttribute pattribute = patientDao.getAttribute(database, mrn, Constants.ATTRIBUTE_PARTICIPATES);
    //return getRegistration(mrn, apptTime, email, appt.getVisitType(0, pattribute), visitCode);
    return getRegistration(mrn, apptTime, email, regUtils.getVisitType(0, new Date(apptTime.getTime())), visitCode);
  }

  public ApptRegistration getRegistration(String mrn, Date apptTime, String email, String surveyType,
                                            String visitCode) {
    ApptRegistration registration = new ApptRegistration(siteInfo.getSiteId(), mrn, apptTime, email,
                                             surveyType, Constants.REGISTRATION_TYPE_ACTIVE_APPOINTMENT, visitCode);
    registration.setSendEmail(true);
    return registration;
  }

  public Date[] getReminderDates(int daysout) {
    String daysString = siteInfo.getProperty("appointment.reminderemail.daysout");
    if (daysString == null || daysString.trim().length() < 1) {
      logger
          .error("Missing value for the 'appointment.reminderemail.daysout' parameter, no notifications will be created.");
      return new Date[0];
    }
    String[] dayStrings = daysString.split(",");
    Date[] dates = new Date[dayStrings.length];
    for (int d = 0; d < dayStrings.length; d++) {
      int days = Integer.parseInt(dayStrings[d]);
      dates[d] = DateUtils.getDaysOutDate(days);
    }
    return dates;
  }

  public User getUser(Supplier<Database> databaseProvider, SitesInfo sitesInfo, String name) {
    UserInfo userInfo = new UserInfo(sitesInfo);
    userInfo.load(databaseProvider);
    return userInfo.forName(name);
  }
}
