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
package edu.stanford.registry.server.config.tools;

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.shared.RegConfigCategory;
import edu.stanford.registry.shared.RegConfigProperty;
import edu.stanford.registry.shared.RegConfigUsage;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.shared.UserPrincipal;
import edu.stanford.registry.server.config.SiteDao;
import edu.stanford.registry.server.security.UserDao;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigDao.ConfigType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Flavor;
import com.github.susom.database.DatabaseProvider.Builder;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlSelect;

/**
 * Utility for populating or changing a database's app_config
 *
 * @author rstr
 */
public class LoadConfig {
  final static String DEV_ADMIN_SITES = "1 hand ir pac ped tj";  // admin gets all authority to these, except BUILDER just for the first
  final static Long UNK_SITE = Long.valueOf(-1);
  final static Long RETIRED_SITE = Long.valueOf(-2);
  final static Long GLOBAL_SITE = Long.valueOf(0);

  private Database db;
  private User user;
  protected String username;
  protected Sites sites;     // interface to existing sites
  protected Props props;        // current set of properties to load
  protected Emails emails;

  public static void main(String[] args) {
    launch(args);
  }


  protected LoadConfig(String username) {
    this.username = username;
  }

  // Developers: to add a command, copy one to create a new one, and put it in this list.
  Command[] createCommands() {
    return new Command[] {
        // new TmpCmd(),
        new LineSepCmd("Most commands are 2 letters- the dash is optional."),
        new LineSepCmd("Sites - the sites in the database"),
        new SiteSetCmd(sites), new SitesListCmd(sites), new SiteAddCmd(sites), new SiteDeleteCmd(sites),

        new LineSepCmd("Config - the site configuration parameters in the database"),
        new ConfigList(sites), new ConfigListGlobalCmd(sites), new ConfigListBatchCmd(sites),
        new ConfigUpdate(sites),  // show and set a config
        new ConfigHistory(sites), // show the app_config history for a site

        new LineSepCmd("Properties - read/alter properties from a file to update the database"),
        new PropsReadCmd(props), new PropsUpdateCmd(props), new PropsListCmd(props),
        new PropsDeleteCmd(props), new PropsSetScopeCmd(props),

        new LineSepCmd("Emails - read email templates from a folder to update the database"),
        new EmailsReadCmd(emails), new EmailsListCmd(sites), new EmailsUpdateCmd(emails), new EmailsDeleteCmd(emails),

        new LineSepCmd("Other - commands"),
        new PropDocCmd(), new QuitCmd(), new AbortCmd(),
        new IgnoreCmd(),
        new ProgramHelpCmd(usage)
    };
  }

  public LoadConfig setup(Supplier<Database> dbp) {
    if (dbp == null) { // running w/o a database, for testing
      db = null;
      user = null;
    } else {
      db = dbp.get();
      if (!doesAppConfigTableExist()) {
        err("appConfig table doesn't exist, aborting.");
        return this;
      }
      UserDao udao = new UserDao(db, null, null);
      UserPrincipal prince = udao.findUserPrincipal(username);
      user = new User(udao.findDefaultIdp().getIdpId(), username, prince.displayName, prince.userPrincipalId, true);
      if (user == null) {
        throw new AbortException("Query failed to find the '"+username+"' user, use arg: -u=<username>");
      }
    }

    sites = new Sites(db, user);
    props = new Props(sites);
    emails = new Emails(db, sites, user);
    return this;
  }

  public void run(Supplier<Database> dbp) {
    setup(dbp);
    Command commands[] = createCommands();

    Command quit = new QuitCmd();
    Command abort = new AbortCmd();
    HelpCmd help = new HelpCmd(commands);
    try {
      while (params.hasMoreInput("\nSite "+sites.curSite+" LoadConfig (?,q,ab)> ")) {
        String linebuf = params.nextLine();
        String words[] = split(linebuf);

        if (quit.matchesDo(words))
          break; // end while loop, leave program
        if (abort.matchesDo(words))
          break;

        boolean found = false;
        for (Command cmd: commands) {
          found = cmd.matchesDo(words);
          if (found) {
            break;
          }
        }
        if (!found) {
          help.unknownCmd(words[0]);
        }
      }
      System.out.println("quitting");
    } finally {
      System.out.flush();
      LoadConfig.sleep(80);
    }
  }

  // Special- split on ' ' or on a non-alpha char after the first
  String[] split(String line) {
    String[] one = new String[1];
    one[0] = line;
    if (line == null || line.length() < 2) {
      return one;
    }
    for (int i = 1;  i < line.length();  i++) {
      char c = (char) (line.charAt(i) | 0x20); // lowercase
      if ('a' <= c && c <= 'z') {
        continue;
      }
      if (c == ' ') {
        return line.split(" ", 2);
      }
      if (c == '-') { // don't split on '-' unless h follows it.
        if (!line.startsWith("-h", i)) {
          continue;
        }
      }
      one = new String[2];
      one[0] = line.substring(0, i);
      one[1] = line.substring(i);
      return one;
    }
    return one;
  }

  // Used in a special way by the code to end the program.
  static class QuitCmd extends Command {
    QuitCmd() {
      super("Quit", "        ", "commits the changes and ends the program");
    }
    @Override public void run(String line) { }
  }

  static class AbortCmd extends Command {
    AbortCmd() {
      super("ABort", "       ", "ends the program, rolling back the transaction");
    }
    @Override public void run(String line) {
      throw new AbortException("executing abort command");
    }
  }


  // Used in a special way by the code to end the program.
  static class HelpCmd extends Command {
    final Command[] commands;
    HelpCmd(Command[] cmds) {
      super("Help", "", "describes the commands (or ?)");
      commands = cmds;
    }
    void unknownCmd(String failed) {
      if (failed != null && !"?".equals(failed) && !failed.startsWith("-h") && !matches(failed))
        err(" -- Unrecognized command: "+failed);
      sayAll();
    }
    void sayAll() {
      p("");
      for (Command cmd: commands) {
        cmd.usage();
      }
    }
    @Override public void run(String line) { }
  }


  static class ProgramHelpCmd extends Command {
    final String usage[];
    ProgramHelpCmd(String usage[]) {
      super("??", "", "Gives program usage");
      this.usage = usage;
    }
    @Override public void usage() {
      p(" NOTE: Lowercase is fine. You only have to type the letters capitalized.\n"
          + "       Add -h after a command for its usage.");
    }
    @Override public void run(String line) {
      p("\nUSAGE:");
      for (String msg: usage) {
        p(msg);
      }
    }
  }

  static abstract class SiteCmd extends Command {
    Sites sites;
    SiteCmd(Sites sites, String cmd, String usage, String desc) {
      super(cmd, usage, desc);
      this.sites = sites;
    }
  }


  static class SiteSetCmd extends SiteCmd {
    SiteSetCmd(Sites sites) {
      super(sites, "S-Set", "<number>", "Sets the current site");
    }
    @Override public void run(String line) {
      if (line == null || line.isEmpty()) {
        err("Argument is missing- line number (or short word)");
      }
      if ("0".equals(line)) {
        sites.curSite = GLOBAL_SITE;
        return;
      }
      SiteInfo si = sites.get(line);
      if (si == null) {
        err("No site found matching: "+line);
        return;
      }
      sites.curSite = si.getSiteId();
    }
  }


  /**
   * Lists the config params in sites.curSite (subclasses list global and batch-control.
   */
  static class ConfigList extends SiteCmd {
    protected String query = "SELECT config_name, config_value, enabled FROM app_config "
                           + "WHERE survey_site_id=? AND config_type=? AND enabled='Y'";
    protected AppConfigDao.ConfigType type = AppConfigDao.ConfigType.CONFIGPARAM;
    boolean global;  // default false to show curSite
    ConfigList(Sites sites) {
      super(sites, "C-List", "[site] ", "Show the site's database configuration properties and make\n"
          + "                     it current. If none is specified, show the current site's\n"
          + "                     If site == a (single letter a), show properties for all sites.\n"
          + "                     Or :string to list all that match in all sites. E.g. cl appoint%");
    }
    /**
     * @param c  g=global, b=batch, s=site
     */
    protected ConfigList(Sites sites, boolean global, String cmd, String usage, String desc) {
      super(sites, cmd, usage, desc);
      this.global = global;
    }
    protected void useBatchSet() {
      String vars = "('polling,registry.load.users','registry.load.config',"
          + "'registry.reload.users,registry.reload.config',"
          + "'registry.batch.interval.seconds','cache.reload.seconds','import.process.frequency')"
          .replaceAll(",", "','"); // add inner quotes
      query += " and config_name IN "+vars;
    }
    @Override
    public void run(String line) {
      if (line == null) {
        sites.listConfig(query, global, type);
      } else if ("a".equals(line)) {
        sites.listAllConfig(query, type);
      } else if (line.startsWith(":")) {
        sites.listConfigAllSites(AppConfigDao.ConfigType.CONFIGPARAM, line.substring(1));
      } else if (sites.set(line)) {
        sites.listConfig(query, global, type);
      } // else we failed to set the site and already gave the user a msg
    }
  }


  static class ConfigListGlobalCmd extends ConfigList {
    ConfigListGlobalCmd(Sites sites) {
      super(sites, true, "C-GlobalsList", "", "Show global database configuration properties");
    }
  }


  static class ConfigListBatchCmd extends ConfigList {
    ConfigListBatchCmd(Sites sites) {
      super(sites, true, "C-Batch      ", "", "Show batch/polling (global) database configuration properties");
      useBatchSet();
    }
  }


  static class ConfigUpdate extends SiteCmd {
    ConfigUpdate(Sites sites) {
      super(sites, "C-Update", "<name> <value>", "Update or insert a config param.\n"
          + "                     Note: -not-set- disables, -no-value- overrides global with no value");
    }
    @Override
    public void run(String line) {
      String words[] = (line == null) ? new String[0] : line.split(" ", 2);
      if (words.length != 2) {
        err("Need name, value, 2 args not "+words.length+" words");
        return;
      }
      if (!words[0].matches("[A-Za-z0-9\\._]+")) {
        err("Property name must contain only letters, numbers and period: "+words[0]);
        return;
      }
      boolean x = sites.setConfig(words[0], words[1], AppConfigDao.ConfigType.CONFIGPARAM);
      p(" addOrEnable returned: "+x);
    }
  }


  static class ConfigHistory extends SiteCmd {
    ConfigHistory(Sites sites) {
      super(sites, "C-Hist", "[site] ", "Show the configuration change history for a site.");
    }
    @Override
    public void run(String line) {
      if (line != null) {
        if (!sites.set(line)) {
          return;
        }
      }
      sites.listHistory("configparam");
    }
  }


  static class SitesListCmd extends SiteCmd {
    SitesListCmd(Sites sites) {
      super(sites, "S-List       ", "", "List the sites stored in the database");
    }
    @Override
    public void run(String line) {
      sites.tellSites();
    }
  }


  static class SiteAddCmd extends SiteCmd {
    SiteAddCmd(Sites sites) {
      super(sites, "S-Add", "<num> <shortname> <the title>", "Add a site to the database");
    }
    @Override
    public void run(String line) {
      String words[] = (line == null) ? new String[0] : line.split(" ", 3);
      if (words.length < 3) {
        err("Need 'number, shortName, Maxie Pain Clinic Title' - 3 or more args "+words.length+" words");
        return;
      }
      Long siteNum;
      try {
        siteNum = Long.valueOf(words[0]);
      } catch (NumberFormatException ne) {
        err("NumberFormatException for first word, must be a number: "+words[0]);
        return;
      }
      sites.add(siteNum, words[1], words[2]);
    }
  }


  static class SiteDeleteCmd extends SiteCmd {
    SiteDeleteCmd(Sites sites) {
      super(sites, "S-Delete     ", "", "Delete a site you just created. It can't be deleted once config are made."
          + "\n                     If you try & fail, you'll have to quit the program afterwards...");
    }
    @Override
    public void run(String line) {
      sites.delete();
    }
  }


  abstract static class PropsCmd extends Command {
    Props props;
    PropsCmd(Props props, String cmd, String usage, String desc) {
      super(cmd, usage, desc);
      this.props = props;
    }
  }

  static class PropsReadCmd extends PropsCmd {
    RegPropertyList regPropList = new RegPropertyList();
    PropsListCmd showCmd;
    PropsReadCmd(Props props) {
      super(props, "P-Read  ", "<file>", "Read a set of properties from a file");
      showCmd = new PropsListCmd(props);
    }
    @Override
    public void run(String line) {
      props.site = null;
      props.list.clear();
      if (line == null || line.isEmpty()) {
        err("No file was specified");
        return;
      }
      File propFile = fileIsReadable(line, false);
      if (propFile == null)
        return;
      Properties p = new Properties();
      try {
        p.load(new FileInputStream(propFile));
      } catch (IOException e) {
        err("Could not load properties file: "+propFile.getAbsolutePath()+"; "+e.getMessage());
        return;
      }
      props.list = putPropsIntoList(p);

      // Put all the properties into the site
      String siteVal = (props.site == null || props.site.intValue() < 0) ? "-1 (UNKNOWN)" : props.site.toString();
      p("Read in "+props.list.size()+" properties for site "+siteVal+" from file: "+propFile.getAbsolutePath());
      setPropSites(regPropList);
      props.list.sort(null);
      showCmd.run(null);
    }

    private ArrayList<Prop> putPropsIntoList(Properties properties) {
      ArrayList<Prop> list = new ArrayList<Prop>();
      for (String key: properties.stringPropertyNames()) {
        String value = properties.getProperty(key);
        if (!key.equals("siteId")) {
          list.add(new Prop(key, value));
        } else { // siteId
          props.site = longFrom(value);
          if (props.site == null) {
            err("Could not make a Long out of siteId = "+value+" you'll need to set the site first");
          } else {
            if (props.site.intValue() == 0 || props.sites.isOne(props.site)) {
              p("Got siteId="+props.site+" - setting curSite to it.");
              props.sites.curSite = props.site;
            } else {
              p("Got siteId="+props.site+" - for a non-existent site, won't be able to apply properties till you set curSite");
              props.sites.curSite = null;
            }
          }
        }
      }
      return list;
    }

    private void setPropSites(RegPropertyList regPropList) {
      for (Prop prop: props.list) {
        prop.rp = regPropList.fromName(prop.key);
        if (prop.rp == null) {
          prop.site = props.site; // custom, assume Site-Specific
          continue;
        }
        switch (prop.rp.getUsage()) {
        case Static:
        case StaticRec:
        case Global:
          prop.site = Long.valueOf(0L);
          continue;
        case Retired:
          prop.site = RETIRED_SITE;
          p("Note: "+prop.key+" is no longer used. Marking it as Retired (site=-2) (your setting was: "+prop.value+")");
          continue;
        case SiteSpecific:
        case SitePath:
          prop.site = props.site;
          continue;
        default:
        }
        err("ERROR - SETTING A SITE TO NULL for property: "+prop.key);
        prop.site = null; // should never happen
      }
    }
  }


  static class PropsDeleteCmd extends PropsCmd {
    PropsDeleteCmd(Props props) {
      super(props, "P-Delete", "<key> ", "Deletes a property from the set read in.");
    }
    @Override
    public void run(String line) {
      if (line == null || line.isEmpty()) {
        err("You must specify the name of the property to delete.");
        return;
      }
      props.delete(line);
    }
  }


  class PropsUpdateCmd extends PropsCmd {
    RegPropertyList regPropList = new RegPropertyList();
    SiteAddCmd addSiteCmd = null;
    PropsUpdateCmd(Props props) {
      super(props, "P-Update", "      ", "Update the database with the current property set");
      addSiteCmd = new SiteAddCmd(props.sites);
    }
    @Override
    public void run(String line) {
      if (props.list == null || props.list.isEmpty()) {
        err("No properties were defined");
        return;
      }
      if (props.site.intValue() != 0 && !sites.isOne(props.site)) {
        err("Site #"+props.site+" doesn't yet exist- define it first");
        p(addSiteCmd.usage);
        return;
      }
      props.writeToDb();
    }
  }


  static class PropsSetScopeCmd extends PropsCmd {
    PropsSetScopeCmd(Props props) {
      super(props, "P-Setscope", "[g|s] <propertyName>", "set the property to be global or site-specific");
    }
    @Override
    public void run(String line) {
      if (props == null) {
        err("There are no properties loaded yet");
        return;
      }
      String args[] = (line == null) ? (new String[0]) : line.split(" ");
      if (args.length != 2) {
        err("Expect 2 arguments, g or s (global or site) and propertyName, not: "+args.length);
        return;
      }
      Prop prop = props.get(args[1]);
      if (prop == null) {
        err("No property was found matching: "+args[1]);
        return;
      }
      char c = args[0].charAt(0);
      if (c != 'g' && c != 's') {
        err("First word must be g for global or s for site-specific, not: "+args[0]);
        return;
      }
      props.setGlobal(c == 'g', prop.key);
    }
  }


  static class PropsListCmd extends PropsCmd {
    PropsListCmd(Props props) {
      super(props, "P-List        ", "", "Show them");
    }
    @Override
    public void run(String line) {
      if (props.list.size() == 0) {
        err("No properties have been read in");
        return;
      }
      for (int i = 0;  i < props.list.size();  i++) {
        Prop p = props.list.get(i);
        if (p.rp == null) {
          p(String.format(" %2d. %8s: %s = %s (Custom property)", i, type(p), p.key, p.value));
        } else {
          p(String.format(" %2d. %8s: %s = %s (%s, %s)", i, type(p), p.key, p.value, p.rp.getCategory(), p.rp.getUsage()));
        }
      }
      p("* Signifies can be site-specific or assigned to global as a default (use p-gl to make it global, p-si to make it site-specific)");
    }
    String type(Prop p) {
      int siteN = p.site.intValue();
      if (siteN == -2) {
        return "  Retired";
      } else if (siteN == -1) {
        return "    Site?";
      }
      String retValue;
      retValue = (siteN == 0) ? "Global" : ("Site#" + siteN);
      //I don't remember what this was supposed to be doing...
      //if (p.rp == null || p.rp.usage == RegConfigProperty.Usage.SiteSpecific)
      //  return retValue+"*";
      return retValue;
    }
  }


  abstract static class EmailCmd extends Command {
    Emails emails;
    EmailCmd(Emails emails, String cmd, String usage, String desc) {
      super(cmd, usage, desc);
      this.emails = emails;
    }
  }

  static class EmailsReadCmd extends EmailCmd {
    EmailsReadCmd(Emails emails) {
      super(emails, "E-Read", "<dir>", "Read templates from a directory");
    }
    @Override
    public void run(String line) {
      emails.clear();
      if (line == null || line.isEmpty()) {
        err("Directory path was missing (any old emails read in were cleared)");
        return;
      }
      emails.read(line);
    }
  }


  static class EmailsListCmd extends ConfigList {
    EmailsListCmd(Sites sites) {
      super(sites, false, "E-List", "[num]", "Show the email templates in the DB for the current site\n"
          + "                    or if a site is specified, for it and make it the current site");
      type = AppConfigDao.ConfigType.EMAILTEMPLATE;
    }
    @Override
    public void run(String line) {
      if (line != null) {
        if (!sites.set(line)) {
          return;
        }
      }
      if (sites.curSiteIsntSite("Specify the site on the command or use the S-S command"))
        return;
      super.run(line);
    }
  }


  static class EmailsUpdateCmd extends Command {
    Emails emails;
    EmailsUpdateCmd(Emails emails) {
      super("E-Update", "   ", "Updates the database with the email templates which were read in.\n"
          + "                   Any existing entry with the same name will be overwritten.");
      this.emails = emails;
    }
    @Override
    public void run(String line) {
      if (line != null && !line.isEmpty()) {
        err("Should have no arguments: "+line);
        return;
      }
      if (emails.curSite().intValue() < 1) {
        err("Use S-Set command to set the current site, it's currently: "+emails.curSite());
        return;
      }
      if (emails.isDbMissing()) {
        p("Pretending to store emails to site "+emails.curSite());
      } else {
        emails.write();
      }
      emails.clear();
    }
  }


  static class EmailsDeleteCmd extends Command {
    Emails emails;
    EmailsDeleteCmd(Emails emails) {
      super("E-DELete   ", "", "Emails - Deletes ALL email templates from the current site");
      this.emails = emails;
    }
    @Override
    public void run(String line) {
      if (line != null && !line.isEmpty()) {
        err("Should have no arguments: "+line);
        return;
      }
      if (emails.isDbMissing()) {
        p("Pretending to delete emails from site "+emails.curSite());
        return;
      }
      emails.delete();;
    }
  }


  static class PropDocCmd extends Command {
    int width = 79;
    int numToShow = 9;
    RegPropertyList regPropList = new RegPropertyList();
    PropDocCmd() {
      super("Doc", "[G|I|E|R|P|A|?|propertyName|-width|/th]", "Prints description of all properties (if no args),"
                    + "\n      category if one letter, or a property, if one matches."
                    + "\n      If the property name is misspelled, it'll make suggestions."
                    + "\n      -120, for instance sets the line-width to 120. Default is 80."
                    + "\n      /sp, for instance, shows all path properties. Also g, s, ?");
    }
    @Override
    public void run(String line) {
      RegConfigCategory oneCat = null;
      if (line == null) { // no args
        printPropList(oneCat);
      } else if (line.length() == 1) {  // just a category character

        oneCat = RegConfigCategory.valueOf(line.charAt(0), true);
        if (oneCat == null) {
          printCategories();     // print it
        } else {
          printPropList(oneCat); // or print the categories if it isn't one
        }
      } else if (line.charAt(0) == '-') { // change line width
        width = intFrom(line.substring(1));
        if (width < 80) {
          width = 80;
        }
      } else if (line.charAt(0) == '+') { // show more matching items (for debugging)
        numToShow = intFrom(line.substring(1));
        if (numToShow < 4) {
          numToShow = 4;
        }
      } else if (line.charAt(0) == '/') { // show more matching items (for debugging)
        printUsages(line.substring(1).trim());
      } else {  // 1 arg, a word, show doc for the one property
        printOneProp(line);
      }
    }

    private void printOneProp(String line) {
      RegConfigProperty p = regPropList.fromName(line); // matches the exact name
      if (p != null) { // found it
        p(String.format("  %s  (%s, %s)", p.getName(), p.getCategory().name(), p.getUsage().abbrev));
        printDesc(p.getDesc());
      } else {  // else print things that are close
        p("That name isn't a known property.  Maybe one of these?");
        RegConfigProperty[] list = regPropList.closeMatches(line, 8);
        int i = 0;
        for (RegConfigProperty rp: list) {
          p(String.format("  %2d. %s  (%s, %s)", ++i, rp.getName(), rp.getCategory().name(), rp.getUsage().abbrev));
        }
      }
    }

    private void printCategories() {
      p("\nAfter each property name is (Category Usage)");
      String msg = "";
      int i = 0;
      for (RegConfigCategory c: RegConfigCategory.values()) {
        msg += c.title() + ",  ";
        if (++i == 4)
          msg += "\n                  ";
      }
      i = msg.lastIndexOf(',');
      p("  Categories are: "+msg.substring(0, i));

      p("  Usages are:");
      for (RegConfigUsage u: RegConfigUsage.values()) {
        p(String.format("    %s %s - %s", u.abbrev, u.name(), u.desc));
      }
    }

    private void printUsages(String usageStr) {
      if ("g".equalsIgnoreCase(usageStr)) {
        for (RegConfigProperty p: regPropList) {
          if (p.getUsage().isGlobal()) {
            printOne(p);
          }
        }
        return;
      } else if ("s".equalsIgnoreCase(usageStr)) {
        for (RegConfigProperty p: regPropList) {
          if (p.getUsage().isSiteSpecific()) {
            printOne(p);
          }
        }
        return;
      }
      RegConfigUsage usag = null;
      for (RegConfigUsage u: RegConfigUsage.values()) {
        usag = (u.abbrev.equalsIgnoreCase(usageStr)) ? u : usag;  // set it if it matches
      }
      if (usag != null) {
        for (RegConfigProperty p: regPropList) {
          if (p.getUsage().equals(usag)) {
            printOne(p);
          }
        }
        return;
      }

      p("Configuration parameter usages:");
      p(" g- print all properties with a global usage.");
      p(" s- print all properties with a site-specific usage.");
      for (RegConfigUsage u: RegConfigUsage.values()) {
        p("  "+u.abbrev+" - "+u.desc);
      }
    }

    private void printOne(RegConfigProperty rp) {
      p(String.format("  %s  (%s)", rp.getName(), rp.getUsage().abbrev));
      printDesc(rp.getDesc() );
      if (!isEmpty(rp.getDefValue())) {
        p("    Default = "+rp.getDefValue());
      }
    }

    private void printDesc(String desc) {
      int ix = 0;
      String spaces = "    ";
      while ((desc.length() - ix) > (width-spaces.length())) {
        int sp = desc.lastIndexOf(' ', ix+width-spaces.length());
        p(spaces+desc.substring(ix, sp));
        spaces = "      ";
        ix = sp+1;
      }
      p(spaces+desc.substring(ix));
    }

    private void printPropList(RegConfigCategory oneCat) {
      RegConfigCategory cat = null;
      for (RegConfigProperty rp: regPropList) {
        if (oneCat == null || oneCat.equals(rp.getCategory())) {
          if (rp.getCategory() != cat) {
            cat = rp.getCategory();
            p("\n"+cat.title() + " " + cat.desc);
          }
          printOne(rp);
        }
      }
    }
  }


  static class IgnoreCmd extends Command {
    IgnoreCmd() {
      super("IGnore", "<any>*", "Its arguments are ignored (add comments to a script)");
    }
    @Override
    public void run(String line) {
    }
  }


  // =================== end of commands,  start of utilities ============

  static class ConfigRowPrinter implements RowsHandler<ArrayList<String>> {
    boolean forEmail;
    Long forSite;
    ConfigRowPrinter(boolean forEmail, Long forSite) {
      this.forEmail = forEmail;
      this.forSite = forSite;
    }
    @Override
    public ArrayList<String> process(Rows rs) throws Exception {
      p("Config values for site: "+forSite);
      ArrayList<String> list = new ArrayList<String>(30);
      while (rs.next()) {
        String enabled = rs.getStringOrEmpty("enabled").equalsIgnoreCase("y") ? "" : "DISABLED: ";
        String name = rs.getStringOrEmpty("config_name");
        String value = rs.getStringOrEmpty("config_value").replaceAll("\\s+", " ");
        if (forEmail && value.length() > 47) {
          value = value.substring(0, 45)+".."+value.length();
        }
        list.add(String.format("%s = %s%s", name, enabled, value));
      }
      list.sort(String.CASE_INSENSITIVE_ORDER);
      return list;
    }
  }

  static class Base {
    protected void p(String msg) {
      System.out.println(msg);
    }

    protected void err(String msg) {
      sleep(80);
      System.err.println(" -- ERROR - "+msg);
    }

    void sleep(long millis) {
      try {
        Thread.sleep(80);
      } catch (InterruptedException e) {
        // ignore
      }
    }

    /**
     * Returns File if a file or dir is readable, and if not, complains and return null
     */
    public File fileIsReadable(String fileName, boolean isADirectory) {
      if (fileName == null || fileName.isEmpty()) {
        p("No fileName was specified");
        return null;
      }
      File f = new File(fileName);
      String what = isADirectory ? "Directory" : "File";
      if (!f.exists()) {
        err(what+" does not exist: "+f.getAbsolutePath());
        return null;
      } else if (f.isDirectory() != isADirectory) {
        err(what+" is"+(isADirectory ? " not " : " ")+"a directory: "+f.getAbsolutePath());
        return null;
      } else if (!f.canRead()) {
        err(what+" is not readable: "+f.getAbsolutePath());
        return null;
      }
      return f;
    }

    static final int BAD_NUM = -999;

    protected Long longFrom(String s) {
      int n = intFrom(s);
      if (n == BAD_NUM) {
        return null;
      }
      return Long.valueOf(n);
    }

    protected int intFrom(String s) {
      try {
        return Integer.parseInt(s);
      } catch (NumberFormatException ne) {
        err("NumberFormatException, argument must be a number: "+s);
        return BAD_NUM;
      }
    }

    protected boolean isEmpty(String s) {
      return s == null || s.trim().isEmpty();
    }
  }


  static class Props extends Base {
    final Sites sites;
    public Long site = UNK_SITE;
    private ArrayList<Prop> list = new ArrayList<Prop>();
    public Props(Sites sites) {
      this.sites = sites;
    }

    public void writeToDb() {
      int count = 0;
      int failed = 0;
      int skipped = 0;
      int doSite = sites.curSite.intValue();
      for (Prop p: list) {
        if (p.site.intValue() == doSite) {
          boolean worked = sites.setConfig(p.key, p.value, AppConfigDao.ConfigType.CONFIGPARAM);
          if (worked) {
            count++;
          } else {
            p("Failed! Maybe already set?  Try: c-u "+p.key+"   "+p.value);
            failed++;
          }
        } else if (p.site.intValue() == -2) {
          p("Omitting, property was retired: "+p.key);
        } else if (p.site.intValue() == -1) {
          p("Omitting, property site isn't known: "+p.key);
        } else {
          skipped++;
        }
      }
      if (doSite == 0) {
        p(String.format("Saved %d global parameters and failed %d -- skipped %d site-specific",
            count, failed, skipped));
      } else {
        p(String.format("Saved %d site parameters and failed %d -- skipped %d globals",
            count, failed, skipped));
      }
    }

    public boolean noSite(boolean zeroOkay) {
      return site.intValue() < (zeroOkay ? 0 : 1);
    }

    // Not yet used- all property files must have siteId - P-Fixsite S
    public void setSite(Long siteId) {
      sites.curSite = site = siteId;
    }

    public void delete(String name) {
      Prop p = get(name);
      if (p != null) {
        list.remove(p);
        p("Deleted property from the set: "+name);
        return;
      }
      err("The specified property was not in the set: "+name);
    }

    public Prop get(String propertyName) {
      for (Prop p: list) {
        if (p.key.equals(propertyName)) {
          return p;
        }
      }
      return null;
    }

    public void setGlobal(boolean toGlobal, String key) {
      for (Prop p: list) {
        if (p.getKey().equals(key)) {
          if (toGlobal) {
            if (p.site.equals(GLOBAL_SITE)) {
              err("Property '"+key+"' is already set to be a global");
            } else {
              p.site = GLOBAL_SITE;
            }
          } else { // toLocal
            if (p.site.equals(site)) {
              err("Property '"+key+"' is already set to be a site-specific value");
            } else {
              p.site = site;
            }
          }
        }
      }
    }
  }


  static class Emails extends Base {
    private final Database db;
    private final Sites sites;
    private final User user;
    HashMap<String,String> hash = new HashMap<String,String>();

    public Emails(Database db, Sites sites, User user) {
      this.db = db;
      this.sites = sites;
      this.user = user;
    }
    public boolean isDbMissing() {
      return db == null;
    }
    public Object size() {
      return hash.size();
    }
    public void put(String name, String content) {
      hash.put(name,content);
    }
    public void clear() {
      hash.clear();
    }
    void write() {
      if (hash.isEmpty()) {
        err("No emails have been read in yet");
        return;
      }
      if (sites.curSiteIsntSite(null)) {
        return;
      }
      String pretend = (db != null) ? "" : "(would have) ";
      int i = 0;
      AppConfigDao appDao = (db == null) ? null : new AppConfigDao(db, user); // ServerUtils.getAdminUser - ServerUtils isn't initted yet
      for (Entry<String, String> e: hash.entrySet()) {
        boolean x = true;
        if (db != null) {
          x = appDao.addOrEnableAppConfigEntry(
            sites.curSite, AppConfigDao.ConfigType.EMAILTEMPLATE, e.getKey(), e.getValue());
        }
        LoadConfig.p(String.format("%2d. %sSaved email: %s = %s", ++i, pretend, e.getKey(), Boolean.valueOf(x).toString()));
      }
    }
    void read(String line) {
      File dir = fileIsReadable(line, true);
      if (dir == null) {
        return;
      }
      int ix = 0;
      for (String name: dir.list()) {
        File f = new File(dir, name);
        String content = getTemplateFromCustomFile(f);
        if (content != null) {
          hash.put(name, content);
          String cont = content.replaceAll("\\s+", " ");
          cont = (cont.length() > 53) ? (cont.substring(0, 47)+"..."+content.length()) : cont;
          p(++ix + ". Loaded template: "+name+" = "+cont);
        }
      }
      p(String.format("Loaded %d email templates from: %s", hash.size(), dir.getAbsolutePath()));
    }

    String getTemplateFromCustomFile(File templateFile) {
      try {
        return FileUtils.readFileToString(templateFile);
      } catch (IOException e) {
        err("Problem reading custom template " + templateFile.getName() + " from file ");
        e.printStackTrace();
        return null;
      }
    }

    Long curSite() {
      return sites.curSite;
    }

    void delete() {
      sites.deleteConfig(null, false, ConfigType.EMAILTEMPLATE);
    }
  }


  static class Sites extends Base {
    private Database db;
    private ArrayList<SiteInfo> sites;
    Long curSite = GLOBAL_SITE;
    final private SiteDao siteDao;
    final private AppConfigDao appConfigDao;

    Sites(Database db, User user) {
      this.db = db;
      if (db != null) {
        siteDao = new SiteDao(db);
        sites = siteDao.getSurveySites();
        appConfigDao = new AppConfigDao(db, user);
      } else {
        siteDao = null;
        appConfigDao = null;
        sites = new ArrayList<SiteInfo>();
      }
    }

    public void listHistory(String type) {
      String q = "SELECT changed_at_time, change_type, config_name, config_value "
          + "FROM app_config_change_history WHERE survey_site_id = ? AND config_type = ? ORDER BY changed_at_time";
      db.toSelect(q).argLong(curSite).argString(type).query(new RowsHandler<Integer>() {
        @Override
        public Integer process(Rows rs) throws Exception {
          SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss.SSS");
          p("Changes for "+type+" for site: "+curSite);
          int num = 0;
          while (rs.next()) {
            num++;
            Date date = rs.getDateOrNull(1);
            String day = dateTimeFormat.format(date);
            String chgType = rs.getStringOrEmpty(2);
            if ("A".equals(chgType)) {
              chgType = "Add";
            } else if ("M".equals(chgType)) {
              chgType = "Mod";
            } else if ("D".equals(chgType)) {
              chgType = "Del";
            }
            String name = rs.getStringOrEmpty(3);
            String value = rs.getStringOrEmpty(4).replaceAll("\\s+", " ");;
            if (value.length() > 47) {
              value = value.substring(0, 45)+".."+value.length();
            }
            p(String.format("  %s %3s %s = %s", day, chgType, name, value));
          }
          return Integer.valueOf(num);
        }
      });
    }

    public boolean set(String s) {
      Long n = longFrom(s);
      if (n == null) {
        err("Bad site value: "+s);
        return false;
      }
      if (db == null) {
        curSite = n; // sure, fake it
        return true;
      }
      if (n.intValue() == 0) {
        curSite = GLOBAL_SITE;
        return true;
      }
      SiteInfo si = get(n);
      if (si != null) {
        curSite = si.getSiteId();
        return true;
      }
      err("There's no site with value: "+s);
      return false;
    }

    public void tellSites() {
      if (db == null) {
        for (SiteInfo site: sites) {
          p(String.format("  %s  %s", site.getIdString(), site.getDisplayName()));
        }
        return;
      }

      String query = "SELECT (sum(c))c, (sum(e))e, survey_site_id, max(url_param), max(display_name) "
          + "FROM "
          +  "(SELECT 1 c, 0 e, a.survey_site_id, url_param, display_name FROM survey_site s JOIN app_config a "
          +   "ON s.survey_site_id=a.survey_site_id AND a.config_type='configparam' AND a.enabled='Y' "
          +  "UNION ALL"
          +  " SELECT 0 c, 1 e, a.survey_site_id, url_param, display_name FROM survey_site s JOIN app_config a "
          +   "ON s.survey_site_id=a.survey_site_id AND a.config_type='emailtemplate' AND a.enabled='Y' "
          +  "UNION ALL"
          +  " SELECT 1 c, 0 e, a.survey_site_id, 'GLOBAL' url_param, '' display_name FROM app_config a "
          +   "WHERE a.config_type='configparam' AND a.survey_site_id=0 AND enabled='Y' "
          +  "UNION ALL"
          +  " SELECT 0 c, 0 e, survey_site_id, url_param, display_name FROM survey_site s "
          + " ) u "
          + "GROUP BY survey_site_id ORDER BY survey_site_id";
      db.toSelect(query).query(new RowsHandler<Boolean>() {
        @Override
        public Boolean process(Rows rs) throws Exception {
          while (rs.next()) {
            Long siteId = rs.getLongOrNull(3);
            if (siteId.intValue() == 0) {
              p(String.format("\nSite %2d  GLOBAL, (%2d params)", siteId, rs.getIntegerOrNull(1)));
              continue;
            }
            p(String.format("Site %2d  %6s, (%2d params, %2d emailTemplates) %s",
                siteId, rs.getStringOrEmpty(4),
                rs.getIntegerOrNull(1), rs.getIntegerOrNull(2), rs.getStringOrEmpty(5)));
          }
          return Boolean.TRUE;
        }
      });
    }

    public void delete() {
      if (curSite.intValue() == 0) {
        err("Can not delete site 0 (global) - use s-s to set the site first");
        return;
      }
      if (!exists(curSite)) {
        err("Site "+curSite+" does not exist, so can't delete it");
        return;
      }
      try {
        siteDao.deleteSite(curSite);
        tellSites();
        curSite = GLOBAL_SITE;
      } catch (Exception e) {
        err("Could not delete site "+get(curSite)+". Once config is created, it can't be deleted.\n"
            + "You'll have to quit now- the transaction we're running in is broken...");
        sleep(100);
      }
    }

    public boolean curSiteIsntSite(String msg) {
      if (msg == null) {
        msg = "The current site isn't set, use: S-Set <number>";
      }
      boolean isErr = (curSite.intValue() == 0) || !exists(curSite);
      if (isErr) {
        err(msg);
      }
      return isErr;
    }

    public void add(Long siteNum, String url, String title) {
      if (siteNum < 1) {
        err("Current site is "+siteNum+", must be > 0 and not yet exist");
        return;
      }
      if (conflicts(siteNum, url, title)) {
        return;
      }
      if (db == null) {
        sites.add(new SiteInfo(siteNum, url, title, true));
      } else {
        SiteDao siteDao = new SiteDao(db);
        siteDao.addSite(Long.valueOf(siteNum), url, title);
        sites = siteDao.getSurveySites();
        if (isOne(siteNum)) {
          curSite = siteNum;
        } else {
          err(siteNum+" isn't a site after adding it!!!  :?(");
        }
      }
      p("Added: " + get(siteNum));
    }

    private boolean isOne(Long siteNum) {
      return get(siteNum) != null;
    }

    private boolean conflicts(Long siteNum, String shortName, String title) {
      for (SiteInfo si: sites) {
        if (si.getSiteId().equals(siteNum) || si.getUrlParam().equals(shortName)) {
          err("  Your site conflicts with existing "+si.getIdString().toLowerCase());
          return true;
        } else if (si.getDisplayName().equalsIgnoreCase(title)) {
          err("  Your site conflicts with existing "+si.getIdString().toLowerCase()+"; title="+si.getDisplayName());
          return true;
        }
      }
      return false;
    }

    SiteInfo get(String word) {
      for (SiteInfo si: sites) {
        if (si.getSiteId().toString().equals(word) || si.getUrlParam().equals(word))
          return si;
      }
      return null;
    }
    /**
     * Returns the siteInfo for a site number, or null if there's none (including for 0L)
     */
    SiteInfo get(Long siteNum) {
      for (SiteInfo si: sites) {
        if (si.getSiteId().longValue() == siteNum.longValue())
          return si;
      }
      return null;
    }
    Long siteNumFromString(boolean mustExist, String word) {
      if (word == null || word.isEmpty()) {
        err("No site was specified");
        return null;
      }
      Long siteNum = longFrom(word);
      if (siteNum == null) {
        return null;
      }

      if (mustExist) {
        if (!siteNum.equals(GLOBAL_SITE) || !exists(siteNum)) {
          err("The specified site doesn't exist. Add it with S-Add...");
          return null;
        }
      }
      return siteNum;
    }

    boolean exists(Long siteNum) {
      return get(siteNum) != null;
    }

    ArrayList<String> getCurrentConfigs(String query, boolean global, String type, String name) {
      if (name != null) {
        query += " AND name = ?";
      }
      Long siteId = global ? GLOBAL_SITE : curSite;
      SqlSelect sel = db.toSelect(query)
          .argLong(siteId).argString(type.toString());
      if (name != null) {
        sel = sel.argString(name);
      }
      return sel.query(new ConfigRowPrinter(type.contains("email"), siteId));
    }

    void listConfig(String query, boolean global, AppConfigDao.ConfigType type) {
      if (db == null) {
        return;
      }
      ArrayList<String>list = getCurrentConfigs(query, global, type.toString(), null);
      Long siteId = global ? GLOBAL_SITE : curSite;
      for (int i = 0;  i < list.size();  i++) {
        p(String.format("  %2d. %s", i+1, list.get(i)));
      }
      if (list.size() == 0) {
        p("  No "+type+" configs for site "+siteId+" were found.");
      }
    }

    public void listConfigAllSites(ConfigType type, String name) {
      if (db == null) {
        return;
      }
      String q = "SELECT survey_site_id, config_name, config_value FROM app_config "
               + "WHERE config_type=? AND config_name LIKE ? ORDER BY survey_site_id";
      db.toSelect(q)
          .argString(type.toString()).argString(name)
          .query(new RowsHandler<Integer>() {
            @Override public Integer process(Rows rp) throws Exception {
              int i = 0;
              while (rp.next()) {
                i++;
                int id = rp.getIntegerOrNull(1);
                String name = rp.getStringOrEmpty(2);
                String value = rp.getStringOrEmpty(3);
                p(String.format("  Site%3d: %s = %s", id, name, value));
              }
              if (i == 0) {
                p("  Found no "+type.toString()+" config entries named: "+name);
              }
              return Integer.valueOf(i);
            }
          });
    }
    public void listAllConfig(String query, ConfigType type) {
      p("");
      curSite = GLOBAL_SITE;
      listConfig(query, true, type);
      for (SiteInfo site: sites) {
        p("");
        curSite = site.getSiteId();
        listConfig(query, false, type);
      }
      curSite = GLOBAL_SITE;
    }

    void deleteConfig(String nameOrNullIsAll, boolean global, AppConfigDao.ConfigType type) {
      if (db == null) {
        return;
      }
      String q = "SELECT config_name, config_value, enabled FROM app_config "
               + "WHERE survey_site_id=? AND config_type=? AND enabled='Y'";
      ArrayList<String>list = getCurrentConfigs(q, global, type.toString(), nameOrNullIsAll);
      Long siteId = global ? GLOBAL_SITE : curSite;
      for (String config: list) {
        int ix = config.indexOf(' ');
        String key = config.substring(0, ix);
        // Can't really delete- just disable:
        appConfigDao.addOrEnableAppConfigEntry(siteId, type, key, AppConfigDao.SIGNIFY_NOT_SET);
      }
      listConfig(q, global, type);
    }

    boolean setConfig(String key, String value, AppConfigDao.ConfigType type) {
      if (db == null)
        return false;
      boolean x = appConfigDao.addOrEnableAppConfigEntry(curSite, type, key, value);
      return x;
    }
  }

  abstract static class Command extends Base {
    String lcaseCmd;
    String lcaseDashless;
    String cmd;
    int minAmt;
    int minAmtDashless;
    String usage;
    String desc;

    /**
     * Use this constructor to insert a command as a line-separator
     */
    Command() {
      //
    }

    int getMinLength(String x) {
      for (int i = 0;  i < x.length();  i++) {
        char c = x.charAt(i);
        if (c > 'Z')
          return i;
      }
      return x.length();
    }

    Command(String cmd, String usage, String desc) {
      this.cmd = cmd;
      lcaseDashless = cmd.replaceAll("-", "").toLowerCase();
      lcaseCmd = cmd.toLowerCase();

      int minAmt = getMinLength(cmd);
      if (minAmt == 0 || minAmt > cmd.length())
        minAmt = cmd.length(); // use the whole cmd
      this.minAmt = minAmt;

      String dashless = cmd.replaceAll("-", "");
      minAmt = getMinLength(dashless);
      if (minAmt == 0 || minAmt > dashless.length())
        minAmt = cmd.length(); // use the whole cmd
      this.minAmtDashless = minAmt;

      this.desc = desc;

      this.usage = cmd + ' ' + usage;
      if (cmd.length() < minAmt)
        this.usage = cmd.substring(0, minAmt)+"/"+this.usage;
    }

    protected boolean matches(String word) {
      word = word.toLowerCase();
      if (word.length() >= minAmt && lcaseCmd.startsWith(word)) {
        return true;
      }
      return (word.length() >= minAmtDashless && lcaseDashless.startsWith(word));
    }

    /**
     * If it matches, and isn't -h for help, do the command
     */
    protected boolean matchesDo(String word[]) {
      boolean result = matches(word[0]);
      if (result) {
        if (word.length > 1) {
          if (word[1].startsWith("-h")) {
            p("USAGE: "+usage+" - "+desc);
            return true;
          }
          run(word[1]);
          p("");
        } else {
          run(null);
        }
      }
      return result;
    }

    /**
     * @param arg contains the rest of the line, with the commands
     */
    abstract public void run(String arg);

    public void usage() {
      if (usage != null) {
        p("    "+usage+" - "+desc);
      } else {
        p(""); // a blank line
      }
    }

    public void help() {
      if (usage != null) {
        p("USAGE:  "+usage+" - "+desc);
      }
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName()+"/"+cmd;
    }
  }


  static class LineSepCmd extends Command {
    String title;
    LineSepCmd(String title) {
      this.title = title;
    }

    @Override public void usage() {
      p(title+" - - - ");
    }

    @Override protected boolean matches(String word) {
      return false;
    }

    @Override
    public void run(String arg) {
    }
  }


  /**
   * A property and whether to assign as a global site or to a local site.
   */
  static class Prop extends Base implements Comparable<Prop> {
    Long site; // a site or 0=global
    String key, value;
    RegConfigProperty rp; // documentation about it
    Prop(String k, String v) {
      key = k;
      value = v;
    }
    public String getKey() {
      return key;
    }
    public String getValue() {
      return value;
    }
    public void setValue(String newvalue) {
      value = newvalue;
    }

    @Override
    public int compareTo(Prop o) {
      long siteDiff = 0;
      if (site != null || o.site != null) {
        if (site == null) {
          return -1;
        } else if (o.site == null) {
          return 1;
        }
        siteDiff = site.longValue() - o.site.longValue();
      }
      if (siteDiff != 0) {
        return siteDiff < 0 ? -1 : 1;
      }
      return key.compareTo(o.key);
    }
  }

  protected static void p(String s) {
    System.out.println(s);
  }
  protected static void err(String s) {
    try {
      Thread.sleep(80);
    } catch (InterruptedException e) {
      // ignore
    }
    System.err.println(s);
  }

  final String tableNames[] = {
      "activity",
      "app_config", "app_config_change_history", "appt_registration",
      "assessment_registration",
      "notification",
      "patient", "patient_attribute", "patient_attribute_history",
      "patient_res_to_survey_reg", "patient_result", "patient_result_type", "patient_study",
      "patient_test_only",
      "provider",
      "service_audit", "study",
      "survey_advance", "survey_advance_push", "survey_advance_status", "survey_complete",
      "survey_complete_push", "survey_json", "survey_player_progress", "survey_progress",
      "survey_progress_dup", "survey_reg_attr", "survey_reg_attr_hist", "survey_registration",
      "survey_session", "survey_site", "survey_system", "survey_token", "survey_user_agent"
  };

  String inTables() {
    return "('" + String.join("','", tableNames) + "')";
  }
  String tablesTable() {
    return (db.flavor().equals(Flavor.postgresql)) ? "information_schema.tables" : "user_tables";
  }

  /**
   * This tells what expected tables aren't in the schema and return true if app_config exists
   */
  private boolean doesAppConfigTableExist() {
    String found[];
    boolean foundAppConfig = false;
    try {
      String tbls = inTables();
      String findTablesQuery = "SELECT table_name FROM user_tables WHERE lower(table_name) IN "; // oracle
      if (db.flavor().equals(Flavor.postgresql))
        findTablesQuery = "SELECT table_name FROM information_schema.tables WHERE lower(table_name) IN ";
      findTablesQuery += tbls + " ORDER BY table_name";

      found = db
          .toSelect(findTablesQuery).query(
              new RowsHandler<String[]>() {
                @Override
                public String[] process(Rows rs) throws Exception {
                  ArrayList<String> list = new ArrayList<>(tableNames.length);
                  while (rs.next()) {
                    list.add(rs.getStringOrEmpty().toLowerCase());
                  }
                  list.sort(null);
                  return list.toArray(new String[list.size()]);
                }
              });

      for (String tbl: found) {
        if (foundAppConfig = "app_config".equals(tbl))
          break;
      }
    } catch (Throwable t) {
      err("Something bad happened- aborting...");
      t.printStackTrace(System.out);
      err("Something bad happened- aborting...");
      return false;
    }

    int added = 0;
    int missing = 0;
    // probably need to specify the schema name - could get lots of duplicates on Oracle...
    for (int i=0, j=0;  i < found.length && j < tableNames.length;  ) {
      if (i == found.length) {  // no more found tables
        err(" Missing table: "+tableNames[j++]);
        missing++;
      } else if (j == tableNames.length) { // no more expected tables
        err(" Extra++ table: "+found[i++]);
        added++;
      } else if (found[i].equals(tableNames[j])) {
        i++; j++;
      } else if (found[i].compareTo(tableNames[j]) < 0) { // found < expected
        err(" Extra++ table: "+found[i++]);
        added++;
      } else {
        err(" Missing table: "+tableNames[j++]);
        missing++;
      }
    }
    if (added + missing == 0) {
      p(" -- All tables are present in the schema");
    } else {
      err(" -- In the schema, "+missing+" tables are missing and the number of extra tables is: "+added);
    }
    return foundAppConfig;
  }

  public void tellRowsInTables(Database db, String tableName) {
    String selectFrom = "SELECT count(*), lower(table_name) FROM ";
    String whereTablesIn = "WHERE upper(table_name) IN :tblname";
    String findTableQuery =  selectFrom + tablesTable() + whereTablesIn + inTables();

    db.toSelect(findTableQuery)
    .query(
      new RowsHandler<Integer>() {
        @Override
        public Integer process(Rows rs) throws Exception {
          if (rs.next()) {
            int count = rs.getIntegerOrZero(1);
            String name = rs.getStringOrEmpty(2);
            p(String.format("%6d rows in: %s", count, name));
          }
          return 0;
        }
      });
  }

  /**
   * A LineReader that can read from a string of comma-separated strings,
   * and change _ to spaces. Then, if there's no quit command, takes input from stdin.
   */
  static class LineReader {
    ArrayList<String> strings;  // null by default, null if no strings, or when they're done
    int ix;
    Scanner scanner;
    public void setStrings(String in) {
      String array[] = convertSpecialChars(in);
      strings = new ArrayList<String>();
      for (String s: array) {
        s = s.trim();
        if (!s.isEmpty()) {
          strings.add(s);
        }
      }
      if (strings.isEmpty()) {
        strings = null;
      }
    }
    public boolean hasMore(String prompt) {
      if (strings != null) { // process comma1nd-line strings first
        p('\n' + prompt + strings.get(ix));
        return true;
      }
      System.out.println('\n' + prompt);
      if (scanner == null) { // make this when strings are done- last command might be 'q'
        scanner = new Scanner(System.in);
      }
      if (scanner.hasNextLine()) {
        return true;
      }
      scanner.close();
      return false;
    }
    // Get a non-empty line
    public String nextLine() {
      String s;
      if (strings != null) {
        s = strings.get(ix);
        if (++ix == strings.size()) {
          strings = null;
        }
        return s;
      }
      do {
        s = scanner.nextLine().trim().replaceAll("\\s+"," ");
      } while (s.isEmpty() && scanner.hasNextLine());
      return s;
    }

    private String[] convertSpecialChars(String s) {
      String a = changeNumSignToSpaces('_', ' ',  s);
      String b = changeNumSignToSpaces(',', '\n', a);
      return b.split("\n");
    }
    private String changeNumSignToSpaces(char a, char b, String s) {
      int ix = s.indexOf(a);
      if (ix < 0) {
        return s;
      }
      StringBuilder sb = new StringBuilder(s.length());
      int last = 0;
      do {
        if (ix - last > 0)
          sb.append(s.substring(last, ix));
        last = ix+1;
        if (s.charAt(ix+1) != a) { // just one of them
          sb.append(b);
        } else { // there are two of them
          sb.append(a);
          last = ix+2;
        }
        ix = s.indexOf(a, ix+1);
      } while (ix >= 0);
      if (last < s.length()) {
        sb.append(s.substring(last));
      }
      return sb.toString();
    }
  }

  static class Params extends Base {
    boolean verbose;
    String dbDrvr;
    String dbUrl;
    String dbUser;
    String dbPass;
    String username = "admin";

    LineReader reader = new LineReader();

    boolean needsDb = true;

    boolean abort = false;
    String usage[];
    Builder builder;

    String theRest; // temporary var to hold 2nd output of argStartsWith()

    Params(String[] usage, String[] args) {
      this.usage = usage;
      init(args);
    }

    static class ArgMatcher implements Iterator<String>{
      String args[];
      int ix = -1;
      String theRest;
      public ArgMatcher(String args[]) {
        this.args = args;
      }
      @Override public boolean hasNext() {
        return ++ix < args.length;
      }
      @Override public String next() {
        return args[ix];
      }
      public boolean equalTo(String s) {
        return args[ix].equals(s);
      }
      public boolean startsWith(String prefix) {
        if (!args[ix].startsWith(prefix)) {
          return false;
        }
        theRest = args[ix].substring(prefix.length());
        return true;
      }
      public String theRest() {
        return theRest;
      }
    }

    private void init(String[] args) {
      ArgMatcher arg = new ArgMatcher(args);
      while (arg.hasNext()) {
        if (arg.equalTo("v")) {
          verbose = true;
        } else if (arg.equalTo("nodb")) {
          needsDb = false;
        } else if (arg.startsWith("drvr=")) {
          dbDrvr = setDriver(arg.theRest);
        } else if (arg.startsWith("db=")) {
          extractDbVars(arg.theRest);
        } else if (arg.startsWith("u=")) {
          username = arg.theRest;

        } else if (arg.startsWith("in=")) {
          reader.setStrings(arg.theRest);
        } else if (arg.startsWith("ig=")) {
          // ignore
        } else if (arg.equalTo("-h") || arg.equalTo("--help")) {
          sayUsage(null);
          abort = true;
          return;
        } else {
          sayUsage("Unknown arg: "+arg.next());
        }
      }
      if (dbDrvr == null)
        dbDrvr = System.getProperty("database.driver");
      if (dbUrl == null)
        dbUrl = System.getProperty("database.url");
      if (dbUser == null)
        dbUser = System.getProperty("database.user");
      if (dbPass == null)
        dbPass = System.getProperty("database.password");
    }
    /**
     * Side-effect: sets theRestOfArg
     * @return true if arg.startsWith(prefixEquals)
     */
    boolean argStartsWith(String arg, String prefixEquals) {
      if (arg.startsWith("-")) { // allow a common mistake
        arg = arg.substring(1);
      }
      if (!arg.startsWith(prefixEquals)) {
        return false;
      }
      theRest = arg.substring(prefixEquals.length());
      return true;
    }

    private String setDriver(String s) {
      if (s.equals("pg")) {
        return "org.postgresql.Driver";
      } else if (s.equals("or")) {
        return "oracle.jdbc.OracleDriver";
      } else {
        return s;
      }
    }

    private void extractDbVars(String s) {
      int ix = s.indexOf('@');
      if (ix < 0) {
        sayUsage("db=user/pass@url -- lacks @:  db="+s);
        return;
      }
      dbUrl = s.substring(ix + 1);
      s = s.substring(0, ix);

      ix = s.indexOf('/');
      if (ix < 0) {
        sayUsage("db=user/pass@url -- lacks / before @:  db="+s);
        return;
      }
      dbUser = s.substring(0, ix);
      dbPass = s.substring(ix + 1);
    }

    public boolean hasMoreInput(String prompt) {
      return reader.hasMore(prompt);
    }

    public String nextLine() {
      return reader.nextLine();
    }

    public void sayUsage(String s) {
      if (s != null) {
        err(s);
      }
      System.err.println("  USAGE: "+usage[0]);
      for (int i = 1;  i < usage.length;  i++) {
        System.err.println("    "+usage[i]);
      }
      System.err.println("");
      abort = true;
    }

    public Builder getBuilder() {
      if (abort || !needsDb) {
        return null;
      }

      if (dbUrl == null || dbUrl.isEmpty()) {
        sayUsage("You must set -Ddatabase.url=...");
        abort = true;
        return null;
      }

      if (dbDrvr != null) {
        try {
          Class.forName(dbDrvr).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
          throw new RuntimeException("Failed to load dbDriver class: "+dbDrvr, e);
        }
      }

      // Put all Derby related files inside ./build to keep our working copy clean
      File directory = new File("build").getAbsoluteFile();
      if (directory.exists() || directory.mkdirs()) {
        System.setProperty("derby.stream.error.file", new File(directory, "derby.log").getAbsolutePath());
      }

      p(String.format("Connecting with driver=%s, %s /*** @%s",dbDrvr,dbUser,dbUrl));
      builder = DatabaseProvider.fromDriverManager(dbUrl, dbUser, dbPass);
      if (params.verbose) {
        builder = builder.withSqlParameterLogging();
      }
      return builder;
    }
  }

  static final String[] usage = {
      "LoadConfig [ nodb | drvr=<dbDriver> | db=usr/pass@url | u=name | del=c | in=[<...>][;<...>]*[;[ab|q] | ig=...",
      "   Command-line and/or interactive program to inspect and configure app_config values.",
      "   Reads configuration properties files and load them into the database.",
      "   Can also read and display DB config parameters and lets you add, delete and set them.",
      "   Can also read email files from a directory into a database, and can create a new survey_site row.",
      "   processes input from the command line if given, and from stdin (if 'q' doesn't end command line input.",
      "",
   // "v     - verbose ",
      " nodb  - run without a database (to ensure a script works), database commands are parsed, but ignored",
      " drvr=<dbDriver>  - specify the driver, abbrevs: pg -> org.postgresql.Driver, or -> oracle.jdbc.OracleDriver",
      "                    Can also be specified with -Ddatabase.driver=<dbDriver> java arg (system property)",
      " db=usr/pass@url  - specify the database user, password and url",
      "                    Can also be specified as java VM arguments (system properties) with",
      "                        -Ddatabase.url=<>, -Ddatabase.user=<>, -Ddatabase.password=<>",
      " u=name           - Specify a real registry user name (default: admin) to store with config changes",
      " in=lines         - input lines, separated by the delimiter character, ',' (or newlines \\n)",
      "                    To make it easier to put commands in a single string, comma (,) separates commands, and _ -> space",
      "                       Two commas become one, and two __ become one",
      "                    If a 'q' command is given, the program will exit when it's processed. If an 'ab' command",
      "                       is given, the program will abort, rolling back the database.",
      "                       (In a shell script, # is a comment only when preceded by a space (or first in the line))",
      " ig=xxx           - Ignores the following arg",
      "",
      " Examples:  nodb 'in=?#doc_fact#q' -- prints the commands and configuration parameter docs",
      "            ",
      ""
  };


  static Params params;
  /**
   * The main program
   * @param args  No args are looked at...
   */
  public static void launch(String[] args) {
    try {
      params = new Params(usage, args);
      if (params.abort) {
        return;
      }
      initLog4J();  // we don't use logging, but init it for registry

      if (!params.needsDb) {  // database is null
        new LoadConfig(params.username).run((Supplier<Database>)null);
      } else {
        Builder b = params.getBuilder();
        if (b != null) {
          LoadConfig conf = new LoadConfig(params.username);
          b.transact(dbp -> conf.run(dbp)); // an exception causes this to rollback
        }
      }
    } catch (Throwable e) {
      if (!(e instanceof AbortException)) {
        e.printStackTrace();
      }
    }
  }

  static class AbortException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public AbortException(String s) {
      super(s);
      p(s);
      p("...throwing an AbortException (you'll see a stack trace)...");
      sleep(80);
    }
  }

  static void sleep(long millis) {
    try {
      Thread.sleep(80);
    } catch (InterruptedException e) {
      // ignore
    }
  }

  private static Logger initLog4J() {
    Properties p = new Properties();
    p.setProperty("log4j.appender.A", "org.apache.log4j.ConsoleAppender");
    p.setProperty("log4j.appender.A.Target", "System.err");
    p.setProperty("log4j.appender.A.Threshold", "WARN"); // set to WARN
    p.setProperty("log4j.appender.A.layout", "org.apache.log4j.PatternLayout");
    p.setProperty("log4j.appender.A.layout.ConversionPattern", "%l %m%n");
    p.setProperty("log4j.rootLogger", "WARN, A"); // set to WARN
    p.setProperty("log4j.additivity.SECURITY", "false");
    p.setProperty("log4j.additivity.SECURITY.access", "WARN");
    p.setProperty("log4j.logger.class.of.the.day", "INHERIT");
    // <category name=\"edu.stanford\"><priority value=\"WARN\"/></category>");
    // <category name=\"com.github.susom\"><priority value=\"WARN\"/></category>");
    //String log4jConfig = new File(params.verbose ? "log4j.xml" : "log4j-nodb.xml").getAbsolutePath();
    PropertyConfigurator.configure(p);
    Logger logger = Logger.getLogger(LoadConfig.class);
    return logger;

    // old file-based logging
    // String log4jConfig = new File(params.verbose ? "log4j.xml" : "log4j-nodb.xml").getAbsolutePath();
    // DOMConfigurator.configure(log4jConfig);
    // log.info("Initialized log4j using file: " + log4jConfig);
  }
}
