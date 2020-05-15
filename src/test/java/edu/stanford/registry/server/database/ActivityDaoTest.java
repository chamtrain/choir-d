package edu.stanford.registry.server.database;

import edu.stanford.registry.server.utils.DateUtils;
import edu.stanford.registry.shared.Activity;
import edu.stanford.registry.shared.AssessmentId;
import edu.stanford.registry.shared.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Supplier;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.SqlArgs;
import com.github.susom.database.SqlInsert;
import com.github.susom.database.SqlSelect;
import com.github.susom.database.SqlUpdate;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActivityDaoTest  {

  private static Logger logger = Logger.getLogger(ActivityDaoTest.class);


  @InjectMocks
  private ActivityDao classToTest;

  @Mock
  private Supplier<Database> databaseProvider;
  @Mock
  private Database database;
  @Mock
  private SqlSelect sqlSelect;
  @Mock
  private DatabaseProvider.Builder builder;

  private Long siteId= 2L;
  private Date fromDate;
  private Date toDate;


  @Before
  public void setUp() throws Exception {
    fromDate = new Date();
    toDate = new Date(DateUtils.getDaysOutDate(2).getTime());
  }

  @Test
  public void getActivityTrue() throws Exception {

    runGetActivity(true);
  }
  @Test
  public void getActivityFalse() throws Exception {

    runGetActivity(false);
  }


  @Test
  public void getPatientsActivityTrue() throws Exception {
    runGetPatientsActivity(true);
  }

  @Test
  public void getPatientsActivityFalse() throws Exception {
    runGetPatientsActivity(false);
  }

  @Test
  public void getActivityByToken() throws Exception {
    runGetActivityByToken(null);
  }

  @Test
  public void getActivityByTokenAndType() throws Exception {
    runGetActivityByToken(Constants.ACTIVITY_SURVEY_TYPE_CHANGED);
  }

  @Test
  public void getActivityByAssessmentIdAndType() throws Exception {
    SqlSelect toSelect = Mockito.mock(SqlSelect.class);
    ArrayList<Activity> activities = new ArrayList<>();

    when(toSelect.apply(Mockito.any(SqlArgs.class))).thenReturn(toSelect);
    when(toSelect.query(ArgumentMatchers.<RowsHandler<ArrayList<Activity>>>any())).thenReturn(activities);
    when(database.toSelect(Mockito.anyString())).thenReturn(toSelect);

    classToTest = new ActivityDao(database, siteId);
    ArrayList<Activity> result = classToTest.getActivityByAssessmentId(1000L, Constants.ACTIVITY_SURVEY_TYPE_CHANGED);
    Assert.assertNotNull(result);
    Mockito.verify(database).toSelect(Mockito.anyString());
  }

  @Test
  public void createActivity() throws Exception {
    SqlInsert toInsert = Mockito.mock(SqlInsert.class);
    when(database.toInsert(Mockito.anyString())).thenReturn(toInsert);
    when(toInsert.argPkSeq(Mockito.anyString(),Mockito.anyString())).thenReturn(toInsert);
    when(toInsert.argLong(Mockito.anyString(), Mockito.anyLong())).thenReturn(toInsert);
    when(toInsert.argLong(Mockito.anyLong())).thenReturn(toInsert);
    when(toInsert.argDate(Mockito.anyString(), Mockito.any())).thenReturn(toInsert);
    when(toInsert.argString(Mockito.anyString())).thenReturn(toInsert);
    when(toInsert.argInteger(Mockito.anyInt())).thenReturn(toInsert);
    //when(toInsert.insertReturningPkSeq(Mockito.anyString())).thenReturn(Mockito.anyLong());
    Activity activity = new Activity("888888-8", Constants.ACTIVITY_AGREED, new AssessmentId(10000L), "22222", null);
    classToTest = new ActivityDao(database, siteId);
    classToTest.createActivity(activity);
    Mockito.verify(database).toInsert(Mockito.anyString());
  }

  @Test
  public void deleteActivity() throws Exception {
    SqlUpdate toUpdate = Mockito.mock(SqlUpdate.class);
    when(database.toDelete(Mockito.anyString())).thenReturn(toUpdate);
    when(toUpdate.argLong(Mockito.anyLong())).thenReturn(toUpdate);
    Activity activity = Mockito.mock(Activity.class);
    when(activity.getActivityId()).thenReturn(Mockito.anyLong());
    classToTest = new ActivityDao(database, siteId);
    classToTest.deleteActivity(activity);
    Mockito.verify(database).toDelete(Mockito.anyString());
  }

  private void runGetActivity ( boolean includeCompleted ){
    logger.info("Testing getActivity");
    SqlSelect toSelect = Mockito.mock(SqlSelect.class);
    ArrayList<Activity> activities = new ArrayList<>();

    when(toSelect.argLong(Mockito.anyLong())).thenReturn(toSelect);
    when(toSelect.argDate(Mockito.any(Date.class))).thenReturn(toSelect);
    when(toSelect.query(ArgumentMatchers.<RowsHandler<ArrayList<Activity>>>any())).thenReturn(activities);
    when(database.toSelect(Mockito.anyString())).thenReturn(toSelect);

    classToTest = new ActivityDao(database, siteId);
    ArrayList<Activity> result = classToTest.getActivity(fromDate, toDate, includeCompleted);

    //expect
    Assert.assertNotNull(result);
    Mockito.verify(database).toSelect(Mockito.anyString());
  }

  private void runGetPatientsActivity(boolean includeCompleted) {
    logger.info("Testing runGetPatientsActivity(" + includeCompleted + ")");
    SqlSelect toSelect = Mockito.mock(SqlSelect.class);
    ArrayList<Activity> activities = new ArrayList<>();

    when(toSelect.argLong(Mockito.anyLong())).thenReturn(toSelect);
    when(toSelect.argString(Mockito.anyString())).thenReturn(toSelect);
    when(toSelect.query(ArgumentMatchers.<RowsHandler<ArrayList<Activity>>>any())).thenReturn(activities);
    when(database.toSelect(Mockito.anyString())).thenReturn(toSelect);

    classToTest = new ActivityDao(database, siteId);
    ArrayList<Activity> result = classToTest.getPatientsActivity("10000", includeCompleted);
    Assert.assertNotNull(result);
    Mockito.verify(database).toSelect(Mockito.anyString());
  }

  private void runGetActivityByToken(String activityType) {
    logger.info("Testing runGetActivityByToken(" + activityType + ")");
    SqlSelect toSelect = Mockito.mock(SqlSelect.class);
    ArrayList<Activity> activities = new ArrayList<>();

    when(toSelect.apply(Mockito.any(SqlArgs.class))).thenReturn(toSelect);
    when(toSelect.query(ArgumentMatchers.<RowsHandler<ArrayList<Activity>>>any())).thenReturn(activities);
    when(database.toSelect(Mockito.anyString())).thenReturn(toSelect);

    classToTest = new ActivityDao(database, siteId);
    ArrayList<Activity> result;
    if (activityType == null) {
      result = classToTest.getActivityByToken("10000");
    } else {
      result = classToTest.getActivityByToken("10000", activityType);
    }
    Assert.assertNotNull(result);
    Mockito.verify(database).toSelect(Mockito.anyString());
  }




}
