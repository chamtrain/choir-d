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

package edu.stanford.registry.server.config;

import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.test.PrivateAccessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Rows;
import com.github.susom.database.SqlSelect;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class AppConfigTest {
  @InjectMocks
  private AppConfig classToTest;
  @Mock
  private Supplier<Database> databaseProvider;
  @Mock
  private Database database;
  @Mock
  private SqlSelect sqlSelect;
  @Mock
  private DatabaseProvider.Builder builder;
  @Mock
  private Rows rows, rows2;

  @Before
  public void setUp() throws Exception {
    Mockito.when(databaseProvider.get()).thenReturn(database);
  }

  @Test
  public void loadNoChangesExpectNoTimeUpdate() throws Exception {
    AppConfig realAppConfig = new AppConfig(null) {
      @Override
      protected Long refreshChangedSitesFromDb(final Supplier<Database> database) {
        return 0L; // largest revision number seen
      }
    };
    realAppConfig.refresh(databaseProvider);
    assertTrue(classToTest.getLastRevision() == 0);
  }

  @Test
  public void loadWithChangesExpectTimeUpdate() throws Exception {
    AppConfig realAppConfig = new AppConfig(null) {
      @Override
      protected Long refreshChangedSitesFromDb(final Supplier<Database> database) {
        return 2L; // largest revision number seen
      }
    };
    realAppConfig.refresh(databaseProvider);
    assertTrue(realAppConfig.getLastRevision() > 0);
  }

  /*
   * To test this, we'll need to mock calling the parameter
   * So like
   *     Mockito.when(sqlSelect.query(theRealRowHandler)).thenReturn(theRealRowHandler.process(rows1));
   * 
   * Or, put the RowsHandler into a class and just unit test it...
   * 
   * Hint- for the RowIterator, return(true,false) to return different values for the iterations
   *
  @Test
  public void refreshAtTimeZeroEnabledExpectFine() throws Exception {
    DataTable dt1 = Mockito.mock(DataTable.class);
    Mockito.when(database.toSelect(Mockito.anyString())).thenReturn(sqlSelect);
    ArrayList<DataTable> returnedResult = new ArrayList<>();
    returnedResult.add(dt1);

    // not sure what these are for
    //Long siteCacheCount = 123L;
    //
    //Mockito.when(sqlSelect.argLong(siteId)).thenReturn(sqlSelect);
    //Mockito.when(sqlSelect.queryLongOrNull()).thenReturn(siteCacheCount);

    AppConfig realAppConfig = new AppConfig(null);

    Mockito.when(databaseProvider.get()).thenReturn(database);
    Mockito.when(database.toSelect(Mockito.anyString())).thenReturn(sqlSelect);
    Mockito.when(sqlSelect.argDate(Mockito.any())).thenReturn(sqlSelect);
    Mockito.when(sqlSelect.argLong(Mockito.any())).thenReturn(sqlSelect);

    Mockito.when(sqlSelect.query(Mockito.any())).thenAnswer(rpw);
    Mockito.when(rows.next()).thenReturn(Boolean.TRUE, Boolean.FALSE);
    Mockito.when(rows.getLongOrZero()).thenReturn(1L);

    Mockito.when(realAppConfig.refreshChangedSitesFromDb(databaseProvider)).thenReturn(false);
    Mockito.when(sqlSelect.query(Mockito.any())).thenReturn(true);

    Mockito.verify(databaseProvider, Mockito.times(1)).get();
    Mockito.verify(database, Mockito.times(1)).toSelect(Mockito.anyString());
    Mockito.verify(sqlSelect, Mockito.times(0)).argLong(Mockito.anyLong());
    Mockito.verify(sqlSelect, Mockito.times(1)).query(Mockito.any());
    Mockito.verify(sqlSelect, Mockito.never()).argDate(Mockito.any());
    assertTrue(classToTest.lastDbRefresh > 0);
  } /* */

  @Test
  public void loadSiteAtTimeZeroExpectLoad() throws Exception {
    loadSiteAtTimeN(0);
    Mockito.verify(sqlSelect, Mockito.never()).argDate(Mockito.any());
  }

  @Test
  public void loadSiteAtTimeLaterExpectLoad() throws Exception {
    loadSiteAtTimeN(System.currentTimeMillis());
    Mockito.verify(sqlSelect, Mockito.never()).argDate(Mockito.any());
  }

  void loadSiteAtTimeN(long time) {
    Long siteId = 2L;
    DataTable dt1 = Mockito.mock(DataTable.class);
    Mockito.when(database.toSelect(Mockito.anyString())).thenReturn(sqlSelect);
    ArrayList<DataTable> returnedResult = new ArrayList<>();
    returnedResult.add(dt1);
    Mockito.when(sqlSelect.argLong(siteId)).thenReturn(sqlSelect);
    classToTest = new AppConfig(null);
    PrivateAccessor<AppConfig> accessor = new PrivateAccessor<AppConfig>(classToTest, AppConfig.class);
    accessor.setLongField("lastRevision", 10L);

    Method method = accessor.getMethod("loadSite", Supplier.class, Long.class, Long.class);
    accessor.callMethod(method, databaseProvider, siteId, Long.valueOf(0L));
    //classToTest.loadSite(databaseProvider, siteId, 0L);
    Mockito.verify(databaseProvider, Mockito.times(1)).get();
    Mockito.verify(database, Mockito.times(1)).toSelect(Mockito.anyString());
    Mockito.verify(sqlSelect, Mockito.times(1)).query(Mockito.any());
    Mockito.verify(sqlSelect, Mockito.times(1)).argLong(Mockito.any());
    Mockito.verify(sqlSelect, Mockito.never()).argDate(Mockito.any());
    Mockito.verify(sqlSelect, Mockito.never()).queryLongOrNull();
  }
}