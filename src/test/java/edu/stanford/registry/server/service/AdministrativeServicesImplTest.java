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

import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.ServerContext;
import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.User;
import edu.stanford.registry.test.FakeSiteInfo;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.function.Supplier;

import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.SqlSelect;

@RunWith(MockitoJUnitRunner.class)
public class AdministrativeServicesImplTest {
  private User usr;

  @Mock
  private Supplier<Database> databaseProvider;
  @Mock
  private Database database;
  @Mock
  private SqlSelect sqlSelect;
  @Mock
  private DatabaseProvider.Builder builder;
  @Mock
  private Set<Service> restrictServices;
  @Mock
  private ServletContext servletContext;
  @Mock
  private Enumeration<String> propsEnum;

  private AdministrativeServicesImpl classToTest;
  private SiteInfo siteInfo;

  @Before
  public void setUp() throws Exception {
    long userId = 1337;
    Mockito.when(databaseProvider.get()).thenReturn(database);
    usr = new User(1L, "fakeUserName", "fakeDisplayName", userId, "fakeEmail", true);

    Mockito.when(servletContext.getInitParameterNames()).thenReturn(propsEnum);
    Mockito.when(propsEnum.hasMoreElements()).thenReturn(false);
    Mockito.when(servletContext.getContextPath()).thenReturn("");
    // Mockito.when(servletContext.getRealPath("/")).thenReturn("");

    ServerContext context = new ServerContext(servletContext, builder, true, true, restrictServices);
    siteInfo = new FakeSiteInfo();

    // Must ensure it's initialized
    classToTest = new AdministrativeServicesImpl(usr, databaseProvider, context, siteInfo);
}

  @After
  public void tearDown() throws Exception {
    usr = null;
    classToTest = null;
  }

  @Test
  public void getTableDataWhenStudyExpectStudyResults() throws Exception {
    DataTable dt1 = Mockito.mock(DataTable.class);
    Mockito.when(database.toSelect(Mockito.anyString())).thenReturn(sqlSelect);
    ArrayList<DataTable> returnedResult = new ArrayList<>();
    returnedResult.add(dt1);
    Mockito.when(sqlSelect.query(Mockito.any())).thenReturn(returnedResult);
    ArrayList<DataTable> result = classToTest.getTableData("Study");
    Assert.assertTrue((result.size() > 0));
    Mockito.verify(databaseProvider, Mockito.times(2)).get();
    Mockito.verify(database).toSelect(Mockito.anyString());
  }

  @Test
  public void getTableDataWhenStudyExpectSurveySystemResults() throws Exception {
    DataTable dt1 = Mockito.mock(DataTable.class);
    Mockito.when(database.toSelect(Mockito.anyString())).thenReturn(sqlSelect);
    ArrayList<DataTable> returnedResult = new ArrayList<>();
    returnedResult.add(dt1);
    Mockito.when(sqlSelect.query(Mockito.any())).thenReturn(returnedResult);
    ArrayList<DataTable> result = classToTest.getTableData("SurveySystem");
    Assert.assertTrue((result.size() > 0));
    Mockito.verify(databaseProvider, Mockito.times(2)).get();
    Mockito.verify(database).toSelect(Mockito.anyString());
    Mockito.verify(sqlSelect).query(Mockito.any());
  }

}
