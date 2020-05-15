/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

public class MenuDefIntfUtils {

  // Define the standard Schedule Widget action column menus
  private static final List<MenuDef> ENROLL_ACTIONS = Arrays.asList(new MenuDef[] {
      new MenuDef("Register this patient", Constants.ACTION_CMD_ENROLL_POPUP),
      new MenuDef("Decline this patient", Constants.ACTION_CMD_DECLINE_POPUP)
  });

  private static final List<MenuDef> ASSIGN_SURVEY_ACTIONS = Arrays.asList(new MenuDef[] {
      new MenuDef("Assign survey", Constants.ACTION_CMD_ASSIGN_SURVEY_POPUP)
  });

  private static final List<MenuDef> ASSESSMENT_ACTIONS = Arrays.asList(new MenuDef[] {
      new MenuDef("Start assessment", Constants.ACTION_CMD_START_SURVEY_POPUP),
      new MenuDef("Send email link", Constants.ACTION_CMD_EMAIL_POPUP),
      new MenuDef("Cancel this assessment", Constants.ACTION_CMD_CANCEL_POPUP),
      new MenuDef("Decline this patient", Constants.ACTION_CMD_DECLINE_POPUP)
  });

  private static final List<MenuDef> IN_PROGRESS_ACTIONS = Arrays.asList(new MenuDef[] {
      new MenuDef("Continue assessment", Constants.ACTION_CMD_START_SURVEY_POPUP),
      new MenuDef("Print partial result", Constants.ACTION_CMD_PRINT),
      new MenuDef("Send email link", Constants.ACTION_CMD_EMAIL_POPUP),
      new MenuDef("Cancel this assessment", Constants.ACTION_CMD_CANCEL_POPUP),
      new MenuDef("Decline this patient", Constants.ACTION_CMD_DECLINE_POPUP)
  });

  private static final List<MenuDef> PRINT_ACTIONS = Arrays.asList(new MenuDef[] {
      new MenuDef("Print results", Constants.ACTION_CMD_PRINT)
  });

  private static final List<MenuDef> DECLINED_ACTIONS = Arrays.asList(new MenuDef[] {
      new MenuDef("Register this patient", Constants.ACTION_CMD_ENROLL_POPUP)
  });

  private static final List<MenuDef> PRINTED_ACTIONS = Arrays.asList(new MenuDef[] {
      new MenuDef("Print results again", Constants.ACTION_CMD_PRINT)
  });

  private static final List<MenuDef> RECENTLY_COMPLETED_ACTIONS = Arrays.asList(new MenuDef[] {
      new MenuDef("Re-print recent result", Constants.ACTION_CMD_PRINT_RECENT),
      new MenuDef("Start this assessment anyway", Constants.ACTION_CMD_START_SURVEY_POPUP),
      new MenuDef("Cancel this assessment", Constants.ACTION_CMD_CANCEL_POPUP),
      new MenuDef("Decline this patient", Constants.ACTION_CMD_DECLINE_POPUP)
  });

  private static final List<MenuDef> INELIGIBLE_ACTIONS = Arrays.asList(new MenuDef[] {
      new MenuDef("Assign survey", Constants.ACTION_CMD_ASSIGN_SURVEY_POPUP)
  });

  public MenuDefIntfUtils() {
  }

  // Define the standard Schedule Widget actions
  public  final ApptAction getActionEnroll(MenuDefBeanFactory factory) {
    return new ApptAction(Constants.ACTION_TYPE_ENROLL, true, Constants.OPT_ENROLL, asStringArray(ENROLL_ACTIONS, factory));
  }

  public  final ApptAction getActionAssignSurvey(MenuDefBeanFactory factory) {
    return  new ApptAction(Constants.ACTION_TYPE_OTHER, true, Constants.OPT_ASSIGN_SURVEY, asStringArray(ASSIGN_SURVEY_ACTIONS, factory));
  }

  public  final ApptAction getActionAssessment(MenuDefBeanFactory factory) {
    return  new ApptAction(Constants.ACTION_TYPE_ASSESSMENT, true, Constants.OPT_ASSESSMENT, asStringArray(ASSESSMENT_ACTIONS, factory));
  }

  public  final ApptAction getActionInProgress(MenuDefBeanFactory factory) {
    return  new ApptAction(Constants.ACTION_TYPE_IN_PROGRESS, true, Constants.OPT_IN_PROGRESS, asStringArray(IN_PROGRESS_ACTIONS, factory));
  }

  public  final ApptAction getActionPrint(MenuDefBeanFactory factory) {
    return  new ApptAction(Constants.ACTION_TYPE_PRINT, true, Constants.OPT_PRINT, asStringArray(PRINT_ACTIONS, factory));
  }

  public  final ApptAction getActionNothingDeclined(MenuDefBeanFactory factory) {
    return  new ApptAction(Constants.ACTION_TYPE_OTHER, false, Constants.OPT_NOTHING_DECLINED, asStringArray(DECLINED_ACTIONS, factory));
  }

  public  final ApptAction getActionNothingPrinted(MenuDefBeanFactory factory) {
    return  new ApptAction(Constants.ACTION_TYPE_PRINTED, false, Constants.OPT_NOTHING_PRINTED, asStringArray(PRINTED_ACTIONS, factory));
  }

  public  final ApptAction getActionNothingRecentlyCompleted(MenuDefBeanFactory factory) {
    return  new ApptAction(Constants.ACTION_TYPE_OTHER, false, Constants.OPT_NOTHING_RECENTLY_COMPLETED, asStringArray(RECENTLY_COMPLETED_ACTIONS, factory));
  }

  public final ApptAction getActionNothingIneligible(MenuDefBeanFactory factory) {
    return new ApptAction(Constants.ACTION_TYPE_OTHER, false, Constants.OPT_NOTHING_INELIGIBLE, asStringArray(INELIGIBLE_ACTIONS, factory));
  }

  public String[] asStringArray(List<MenuDef> menuDefs, MenuDefBeanFactory factory) {
    if (menuDefs == null) {
      return new String[0];
    }
    String[] menuDefJson = new String[menuDefs.size()];
    for (int inx = 0; inx < menuDefs.size(); inx++) {
      menuDefJson[inx] = getJson(menuDefs.get(inx), factory);
    }
    return menuDefJson;
  }

  private String getJson(MenuDef menuDef, MenuDefBeanFactory factory) {
    if (factory == null) {
      return("No Factory");
    }
    if (menuDef == null) {

      return "";
    }
    AutoBean<MenuDefIntf> menuDefIntfAutoBean = factory.MenuDefIntf();
    MenuDefIntf menuDefIntf = menuDefIntfAutoBean.as();
    menuDefIntf.setAction(menuDef.getAction());
    menuDefIntf.setCommandName(menuDef.getCommandName());
    menuDefIntf.setConfirmMsg(menuDef.getConfirmMsg());
    menuDefIntf.setMenuLabel(menuDef.getMenuLabel());
    menuDefIntf.setParameters(menuDef.getParameters());
    return AutoBeanCodex.encode(menuDefIntfAutoBean).getPayload();
  }

  public ArrayList<MenuDef> getMenuDefs(String[] menuDefJson, MenuDefBeanFactory factory) {
    if (menuDefJson == null) {
      return new ArrayList<>();
    }
    ArrayList<MenuDef> menuDefs = new ArrayList<>();
    for (int inx = 0; inx < menuDefJson.length; inx++) {
        MenuDefIntf intf = AutoBeanCodex.decode(factory, MenuDefIntf.class, menuDefJson[inx]).as();
        MenuDef menuDef = new MenuDef(intf.getMenuLabel(), intf.getCommandName());
        menuDef.setParameters(intf.getParameters());
        menuDef.setAction(intf.getAction());
        menuDef.setConfirmMsg(intf.getConfirmMsg());
        menuDefs.add(menuDef);
    }
    return menuDefs;
  }

  public interface MenuDefBeanFactory extends AutoBeanFactory {
    /**
     * Bump this when making changes client and server need to agree on.
     */
    long compatibilityLevel = 1;

    AutoBean<MenuDefIntf> MenuDefIntf();
  }
}
