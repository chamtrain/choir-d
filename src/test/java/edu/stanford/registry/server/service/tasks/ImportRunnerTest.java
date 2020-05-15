package edu.stanford.registry.server.service.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
import org.junit.Before;

//import org.mockito.ArgumentMatchers;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.runners.MockitoJUnitRunner;

import static edu.stanford.registry.server.xchg.data.Constants.IMPORT_FILES_PENDING;
import static edu.stanford.registry.server.xchg.data.Constants.IMPORT_FILES_PROCESSED;
import edu.stanford.registry.server.config.PropertyMapFromStrings;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.SitesInfo;

/**
 * Tests the static getBool(), and default getMap and getBoolean
 *
 * To make Patient.csv and Appointment.csv files for manual testing, see ImportFilesCreate.java
 */
public class ImportRunnerTest {
  PropertyMapFromStrings h;
  static String DIR_IN_KEY = IMPORT_FILES_PENDING;
  static String DIR_OUT_KEY = IMPORT_FILES_PROCESSED;
  static String URLKEY = ImportRunner.IMPORT_URL_KEY;

  static String URL1 = "http://localhost:8080/page1";
  static String URL2 = "http://localhost:8080/page2";

  static boolean isDir(String s) {
    File f = new File(s);
    if (f.exists() && f.isDirectory())
      return true;

    if (f.mkdir()) {
      return true;
    }
    return false;
  }

  static String dir1, dir2, dir3, dir4 = initDirs();
  static String initDirs() {
    if (isDir("build")) {
      if (isDir(dir1 = "build"+File.separator+"dir1") &&
          isDir(dir2 = "build"+File.separator+"dir2") &&
          isDir(dir3 = "build"+File.separator+"dir3") &&
          isDir(dir4 = "build"+File.separator+"dir4"))
        return dir4;
    }
    return "couldntMakeADir";
  }

  @Before
  public void runBefore() {
    Assert.assertTrue("Making this dir works on my build: "+dir1, isDir(dir1));
  }
  static class MySitesInfo extends SitesInfo {

    MySitesInfo() {
      //  public SitesInfo(String contextPath, boolean loadSurveySites, Map<String,String> globals, AppConfig appConfig) {
      super("path", false, new HashMap<>(0), null);
    }

    MySitesInfo(String...globals) {
      super("path", false, new PropertyMapFromStrings(globals).getMap(), null);
    }

    MySitesInfo addSite(long id, String...strings) {
      PropertyMapFromStrings h = new PropertyMapFromStrings(strings);
      addTestProperties(Long.valueOf(id), h.getMap(), null);
      return this;
    }
  }

  // ============= test the conflicts method


  @Test
  public void noSitesNoConflicts() {
    ImportRunner runner = new ImportRunner(new MySitesInfo(), "whatever");

    ArrayList<SiteInfo> list = runner.getUniqueEnabledSites(false);
    Assert.assertNotNull(list);
  }

  @Test
  public void blankSitesNoConflicts() {
    ImportRunner runner = new ImportRunner(new MySitesInfo(), "whatever");

    ArrayList<SiteInfo> list = runner.getUniqueEnabledSites(false);
    Assert.assertNotNull(list);
  }

  @Test
  public void twoSitesNoConflicts() {
    MySitesInfo info = new MySitesInfo();
    info.addSite(1L, DIR_IN_KEY, "local1");
    info.addSite(2L, DIR_IN_KEY, "local2");
    ImportRunner runner = new ImportRunner(info, "whatever");

    ArrayList<SiteInfo> list = runner.getUniqueEnabledSites(false);
    Assert.assertNotNull(list);
  }

  @Test
  public void twoSitesWithInDirConflicts() {
    MySitesInfo info = new MySitesInfo(ImportRunner.IMPORT_URL_KEY, "http://localhost/foo");
    info.addSite(1L, DIR_IN_KEY, dir1, DIR_OUT_KEY, dir3);
    info.addSite(2L, DIR_IN_KEY, dir1, DIR_OUT_KEY, dir4);

    ImportRunner runner = new ImportRunner(info, "whatever");

    ArrayList<SiteInfo> list = runner.getUniqueEnabledSites(false);
    Assert.assertNull(list);
  }

  @Test
  public void twoSitesWithOurDirConflicts() {
    MySitesInfo info = new MySitesInfo(ImportRunner.IMPORT_URL_KEY, "http://localhost/foo");
    info.addSite(1L, DIR_IN_KEY, dir1, DIR_OUT_KEY, dir3);
    info.addSite(2L, DIR_IN_KEY, dir2, DIR_OUT_KEY, dir3);

    ImportRunner runner = new ImportRunner(info, "whatever");

    ArrayList<SiteInfo> list = runner.getUniqueEnabledSites(false);
    Assert.assertNull(list);
  }

  @Test
  public void twoSitesWithDiffInOutDirConflicts() {
    String SAME = dir1;
    MySitesInfo info = new MySitesInfo(ImportRunner.IMPORT_URL_KEY, "http://localhost/foo");
    info.addSite(1L, DIR_IN_KEY, SAME, DIR_OUT_KEY, dir3);
    info.addSite(2L, DIR_IN_KEY, dir2, DIR_OUT_KEY, SAME);

    ImportRunner runner = new ImportRunner(info, "whatever");

    ArrayList<SiteInfo> list = runner.getUniqueEnabledSites(false);
    Assert.assertNull(list);
  }

  @Test
  public void twoSitesWithSameInOutDirConflicts() {
    String SAME = dir1;
    MySitesInfo info = new MySitesInfo(ImportRunner.IMPORT_URL_KEY, "http://localhost/foo");
    info.addSite(1L, DIR_IN_KEY, SAME, DIR_OUT_KEY, SAME);
    info.addSite(2L, DIR_IN_KEY, dir2, DIR_OUT_KEY, dir3);

    ImportRunner runner = new ImportRunner(info, "whatever");

    ArrayList<SiteInfo> list = runner.getUniqueEnabledSites(false);
    Assert.assertNull(list);
  }

  @Test
  public void theFolderOverridesGlobal() {
    MySitesInfo info = new MySitesInfo(DIR_IN_KEY, dir1, URLKEY, URL2);
    info.addSite(1L, DIR_IN_KEY, dir3, DIR_OUT_KEY, dir4, URLKEY, URL1);
    File f = new File(".");
    for (String s: f.list()) {
      System.out.println(" File exists: "+s);
    }

    ImportRunner runner = new ImportRunner(info, "whatever");

    ArrayList<SiteInfo> list = runner.getUniqueEnabledSites(false);
    Assert.assertNotNull(list);
    Assert.assertEquals("Should have 1 item in list", 1, list.size());
    Assert.assertEquals(dir3, info.getProperty(1L, DIR_IN_KEY));
  }

  @Test
  public void oneBlankFolderOverridesGlobal() {
    MySitesInfo info = new MySitesInfo(DIR_IN_KEY, "globalFolder", URLKEY, "globalUrl");
    info.addSite(1L, URLKEY, "localUrl");
    ImportRunner runner = new ImportRunner(info, "whatever");

    ArrayList<SiteInfo> list = runner.getUniqueEnabledSites(false);
    Assert.assertNotNull(list);
    // The lack of folder name should cause no enabled site to be in the list, despite the global value
    Assert.assertEquals("Should have no items in list", 0, list.size());
  }
}
