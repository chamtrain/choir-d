/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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

import java.util.HashMap;
import java.util.Map;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.tool.RandomSetsCreate;

/**
 * A fake SiteInfo that's easy to add config params to.
 * Use the constructor(true) for DB tests.
 */
public class FakeSiteInfo extends SiteInfo {

  public FakeSiteInfo() {
    this(2L);
  }

  /**
   * Returns a fake SiteInfo, ID 6, urlParam "ped".
   * This uses a real (non-1) siteId, so DB tests work
   *
   * See addEmail() for how to add config params to it.
   */
  public FakeSiteInfo(long siteId) {
    super(siteId, "test", "Stanford Under Test", true);  // use 2, because 6/ped doesn't use default/xml/process.xml
    localParams = new HashMap<String,String>();
    globalParams = new HashMap<String,String>();
    emailTemplates = new HashMap<String,String>();

    HashMap<String,String>tsetJsons = new HashMap<String,String>();
    tsetJsons.put(RandomSetsCreate.TSET_KSort_BackPain, RandomSetsCreate.createBackPainRandomSet().toJsonString());
    tsetJsons.put(RandomSetsCreate.TSET_Pure_Migraine, RandomSetsCreate.createMigraineRandomSet().toJsonString());

    // add this early, to avoid a warning
    addGlobalProps("default.dateTimeFormat", "MM/dd/yyyy h:mm a");
    // skipping the main init for now
    initResources(null, emailTemplates, tsetJsons); // configure the site-specific registryCustomer, date, etc
  }


  /**
   * Add a standard set of email config parameters
   * @param isLocal Add to either the site-specific or the global parameters
   */
  public FakeSiteInfo addEmail(boolean isLocal) {
    String s[] = {
             "appointment.daysout.load", "1",
             "appointment.initialemail.daysout", "7",
             "appointment.noemail.withindays", "2",
             "appointment.lastsurvey.daysout", "11",
             "appointment.reminderemail.daysout", "4,1",
             "appointment.scheduledsurvey.daysout", "90",
             "appointment_template", "apptTemplate",
             "email.template.directory", "/var/tmp",
             "default.dateTimeFormat", "MM/dd/yyyy h:mm a",
             "xml_resource", "default/xml" // crucial
    };
    if (isLocal) {
      addLocalProps(s);
    } else {
      addGlobalProps(s);
    }
    String templates[] = {"FollowUp",  "FollowUp-reminder",
        "Initial", "Initial-reminder", "No-appointment",   "No-appointment-reminder" };
    for (String t: templates) {
        putEmailTemplate(t);
    }
    return this;
  }

  private void putEmailTemplate(String name) {
    emailTemplates.put(name, "Subject: Testing "+name+" template\nHello,\n"
        + "click on this link:\n[SURVEY_LINK] \n"
        + "and take the survey by [SURVEY_DATE] \n,Yours truly,\nUs");
  }

  public void addLocalProps(String...strings) {
    addProps(localParams, strings);
  }

  public void addGlobalProps(String...strings) {
    addProps(globalParams, strings);
  }

  private void addProps(Map<String,String> map, String...strings) {
    for (int i = 0;  i < strings.length;  i+=2) {
      map.put(strings[i], strings[i+1]);
    }
  }
}
