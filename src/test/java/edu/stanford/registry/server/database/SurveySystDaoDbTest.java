package edu.stanford.registry.server.database;

import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.DateUtilsIntf;
import edu.stanford.registry.shared.Study;
import edu.stanford.registry.shared.SurveySystem;
import edu.stanford.registry.shared.survey.Constants;
import edu.stanford.registry.test.DatabaseTestCase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import com.github.susom.database.Database;

public class SurveySystDaoDbTest extends DatabaseTestCase implements DateUtilsIntf {

  private final String LOCAL = Constants.REGISTRY_SURVEY_SYSTEM_NAME;
  
  public void testGetSurveySystem() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    SurveySystem surveySystem1 = dao.getSurveySystem(1000);
    assertNotNull("There should be a survey system with id 1000", surveySystem1);
    SurveySystem surveySystem2 = dao.getSurveySystem(LOCAL);
    assertNotNull("There should be a "+LOCAL+" survey system...", surveySystem2);
  }

  public void testGetSurveySystems() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    ArrayList<SurveySystem> list = dao.getSurveySystems();
    assertNotNull("I'm expecting a list of surveys, not null...", list);
    assertTrue("Expected a list.size of 13 or so, not: "+list.size(), list.size() > 5);
  }
  
  public void testGetSurveySystemsAsDataTablesTest() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    ArrayList<DataTable> list = dao.getSurveySystemsAsDataTables();
    assertNotNull("I'm expecting a list of surveys, not null...", list);
    assertTrue("Expected a list.size of 13 or so, not: "+list.size(), list.size() > 5);
  }


  public void testGetLocalSurveyByName() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    SurveySystem s = dao.getSurveySystem(LOCAL);
    // Randy: I'm new here, and just judging by what I see
    assertNotNull("There should be a "+LOCAL+" survey...", s);
    assertEquals("Fetched 'Local' survey, so the name should be "+LOCAL, LOCAL, s.getSurveySystemName());
    assertEquals("Local should be the first, at ID=1000", 1000L, (long)s.getSurveySystemId());
  }

  public void testGetLocalSurveyById() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    SurveySystem s = dao.getSurveySystem(1000);
    assertNotNull("There should be 'Local' survey with id=1000", s);
    assertEquals("Survey wi id=1000 should be named "+LOCAL, LOCAL, s.getSurveySystemName());
  }

  public void testGetPromisSurveyByName() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    SurveySystem s = dao.getSurveySystem("PROMIS");
    assertNotNull("I'm expecting a 'PROMIS' survey...", s);
    assertEquals("Fetched 'PROMIS' survey, so should have this name", "PROMIS", s.getSurveySystemName());
  }

  public void testInsert() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    String name = "HelloTest";
    SurveySystem inserted = dao.getSurveySystem(name);
    assertNull(inserted);
    inserted = dao.insertSurveySystem(name);

    SurveySystem got = dao.getSurveySystem(name);
    cleanUpCreatedSurvey(dao, name);
    assertNotNull(got);
    
    assertEquals(got.getSurveySystemName(), name);
    assertEquals(got.getSurveySystemId(), inserted.getSurveySystemId());
    int mv = got.getMetaVersion();
    Date chgd = got.getDtChanged();
    Date crtd = got.getDtCreated();

    assertNotNull(crtd);
    assertNull(chgd);  // it hasn't changed yet
    assertTrue(mv == 0);
  }

  private boolean isInList(String name, ArrayList<SurveySystem> list) {
    for (SurveySystem ss: list) {
      if (ss.getSurveySystemName().equals(name))
        return true;
    }
    return false;
  }

  public void testGetOrCreateUsingGet() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    ArrayList<SurveySystem> preTestList = dao.getSurveySystems();
    
    String name = "Local";
    SurveySystem got = dao.getOrCreateSurveySystem(name, null);
    assertNotNull(got);
    assertTrue(isInList(name, preTestList));
  }

  public void testGetOrCreateUsingCreate() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    ArrayList<SurveySystem> preTestList = dao.getSurveySystems();

    Random r = new Random(System.currentTimeMillis());
    String name = "Foreign_" + r.nextInt();
    SurveySystem got = dao.getOrCreateSurveySystem(name, null);
    cleanUpCreatedSurvey(dao, name);
    
    assertNotNull(got);
    assertFalse(isInList(name, preTestList));
  }

  private void cleanUpCreatedSurvey(SurveySystDao dao, String name) {
    Database db = getDatabaseProvider().get();
    int numRows = db.toDelete("DELETE FROM SURVEY_SYSTEM WHERE SURVEY_SYSTEM_NAME = ?").argString(name).update();
    SurveySystem got = dao.getSurveySystem(name);
    assertNull("Failed to delete survey named: "+name, got);
    assertEquals("Wrong number of surveys deleted during cleanup", 1, numRows);
  }

  // This implements the DateUtilsIntf interface, to pass to SurveySystem.getData(this)
  @Override
  public String getDateString(Date dt) {
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM-dd-yyyy");
    return dateTimeFormat.format(dt);
  }

  public void testGetStudyByCode() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    SurveySystem ss = dao.getSurveySystem(LOCAL);
    assertNotNull("There should be a "+LOCAL+" survey...", ss);

    Study study = dao.getStudy(ss.getSurveySystemId(), 1000);
    assertNotNull("Should find study 1000", study);
    assertEquals("Study 1000 should be the 'names' study", "names", study.getStudyDescription());
  }

  public void testGetStudyByName() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    SurveySystem ss = dao.getSurveySystem(LOCAL);
    assertNotNull("There should be a "+LOCAL+" survey...", ss);

    Study study = dao.getStudy(ss.getSurveySystemId(), "names");
    assertNotNull("Expecting to find the names study", study);
    assertEquals("Expect the study id to be 1000", 1000, study.getStudyCode().intValue());
  }

  public void testGetStudies() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    SurveySystem ssLocal = dao.getSurveySystem(LOCAL);
    assertNotNull("There should be a "+LOCAL+" survey...", ssLocal);
    ArrayList<Study> localStudies = dao.getStudies(ssLocal.getSurveySystemId());
    assertNotNull("Should find local studies", localStudies);
    assertTrue("Should find several local studies", localStudies.size() > 1);
    SurveySystem ssPromis = dao.getSurveySystem("LocalPromis");
    ArrayList<Study> promisStudies = dao.getStudies(ssPromis.getSurveySystemId());
    assertNotNull("Should find PROMIS studies", promisStudies);
    assertTrue("Should find several PROMIS studies", localStudies.size() > 1);
  }

  public void testInsertStudy() {
    SurveySystDao dao = new SurveySystDao(getDatabaseProvider());
    Study newStudy = new Study();
    newStudy.setSurveySystemId(1000);
    newStudy.setStudyDescription("myTestStudy");
    newStudy.setTitle("My Test Study");
    Study studyReturned = dao.insertStudy(newStudy);
    assertNotNull("Expect study to be returned from insert", studyReturned);
    assertNotNull("Study code should be set", studyReturned.getStudyCode());
    assertEquals("Study description should be what we sent ", newStudy.getStudyDescription(), studyReturned.getStudyDescription());
    assertEquals("Study title should be what we sent ", newStudy.getTitle(), studyReturned.getTitle());
  }
}