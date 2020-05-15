package edu.stanford.registry.server.database;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.github.susom.database.SqlSelect;

import edu.stanford.registry.server.database.QuerySetExpander.FetchIntegerFromSet;
import edu.stanford.registry.server.database.QuerySetExpander.FetchLongFromSet;
import edu.stanford.registry.server.database.QuerySetExpander.FetchStringFromSet;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class QuerySetExpanderTest {

  @Mock SqlSelect sqlSelect;
  @Mock FetchStringFromSet strFetch;
  @Mock FetchLongFromSet longFetch;
  @Mock FetchIntegerFromSet intFetch;

  @Before
  public void before() {
    Mockito.when(sqlSelect.argString(Mockito.anyString())).thenReturn(sqlSelect);
    Mockito.when(sqlSelect.argInteger(Mockito.any(Integer.class))).thenReturn(sqlSelect);
    Mockito.when(sqlSelect.argLong(Mockito.anyLong())).thenReturn(sqlSelect);

    Mockito.when(strFetch.fetch(Mockito.anyInt())).thenReturn(IdStr);
    Mockito.when(longFetch.fetch(Mockito.anyInt())).thenReturn(IdLong);
    Mockito.when(intFetch.fetch(Mockito.anyInt())).thenReturn(IdInt);
  }

  static Integer IdInt = Integer.valueOf(0);
  static Long    IdLong = Long.valueOf(0);
  static String  IdStr = "ID";

  static String SELECT1 = "SELECT 1 FROM foo WHERE bub in (?)";
  static String SELECT1to2 = "SELECT 1 FROM foo WHERE bub in (?,?)";
  static String SELECT1to4 = "SELECT 1 FROM foo WHERE bub in (?,?,?,?)";

  static char I = 'I';
  static char L = 'L';
  static char S = 'S';




  @Test
  public void simpleExpand1() throws Exception {
    QuerySetExpander qse = new QuerySetExpander("qe1", 1, SELECT1).setSizes(2, 4);
    verifyExpand(qse, SELECT1to2, I, 2, 1, true);
  }


  @Test
  public void simpleExpand4for4atEnd() throws Exception {
    QuerySetExpander qse = new QuerySetExpander("qe1", 4, SELECT1).setSizes(2, 4);
    verifyExpand(qse, SELECT1to4, I, 4, 4, true);
  }

  @Test
  public void simpleExpand4for3atEnd() throws Exception {
    QuerySetExpander qse = new QuerySetExpander("qe1", 3, SELECT1).setSizes(2, 4);
    verifyExpand(qse, SELECT1to4, I, 4, 3, true);
  }

  @Test
  public void simpleExpand4notAtEnd() throws Exception {
    String append = " AND imgreat";
    String sql = SELECT1 + append;
    QuerySetExpander qse = new QuerySetExpander("qe1", 1, sql).setSizes(4, 6);
    verifyExpand(qse, SELECT1to4+append, I, 4, 1, true);
  }

  @Test
  public void simpleExpand2Rounds6() throws Exception {
    QuerySetExpander qse = new QuerySetExpander("qe1", 6, SELECT1).setSizes(2, 4);
    verifyExpand(qse, SELECT1to4, I, 4, 4, false);
    verifyExpand(qse, SELECT1to2, I, 6, 6, true);
  }


  @Test
  public void simpleExpand2Rounds7() throws Exception {
    QuerySetExpander qse = new QuerySetExpander("qe1", 7, SELECT1).setSizes(2, 4);
    verifyExpand(qse, SELECT1to4, I, 4, 4, false);
    verifyExpand(qse, SELECT1to4, I, 8, 7, true);
  }


  @Test
  public void simpleExpand2Rounds7Long() throws Exception {
    QuerySetExpander qse = new QuerySetExpander("qe1", 7, SELECT1).setSizes(2, 4);
    verifyExpand(qse, SELECT1to4, L, 4, 4, false);
    verifyExpand(qse, SELECT1to4, L, 8, 7, true);
  }

  @Test
  public void simpleExpand2Rounds7String() throws Exception {
    QuerySetExpander qse = new QuerySetExpander("qe1", 7, SELECT1).setSizes(2, 4);
    verifyExpand(qse, SELECT1to4, S, 4, 4, false);
    verifyExpand(qse, SELECT1to4, S, 8, 7, true);
  }


  @Test
  public void simpleExpand2Rounds7StringSmallEqBig() throws Exception {
    QuerySetExpander qse = new QuerySetExpander("qe1", 7, SELECT1).setSizes(4, 4);
    verifyExpand(qse, SELECT1to4, S, 4, 4, false);
    verifyExpand(qse, SELECT1to4, S, 8, 7, true);
  }


  @Test
  public void noSetInQuery() throws Exception {
    String sql = "SELECT 1 FROM foo WHERE bub in ? AND imgreat";
    try {
      new QuerySetExpander("noSetInQuery", 7, sql).setSizes(2, 4);
      Assert.fail("Should have thrown an exception due to no '(?)' in query");
    } catch (RuntimeException e) {
      // good!
    }
  }


  @Test
  public void smallIsTooSmallFails() throws Exception {
    try {
      new QuerySetExpander("noSetInQuery", 7, SELECT1).setSizes(1, 4);
      Assert.fail();
    } catch (RuntimeException e) {
      // good!
    }
  }

  @Test
  public void bigIsTooBigFails() throws Exception {
    try {
      new QuerySetExpander("noSetInQuery", 7, SELECT1).setSizes(2, 1001);
      Assert.fail();
    } catch (RuntimeException e) {
      // good!
    }
  }

  @Test
  public void bigLessThanSmallFails() throws Exception {
    try {
      new QuerySetExpander("noSetInQuery", 7, SELECT1).setSizes(5, 4);
      Assert.fail();
    } catch (RuntimeException e) {
      // good!
    }
  }

  @Test
  public void bigEqualsSmallisOk() throws Exception {
    try {
      new QuerySetExpander("noSetInQuery", 7, SELECT1).setSizes(5, 5);
    } catch (RuntimeException e) {
      Assert.fail(e + " " + e.getMessage());
    }
  }



  void verifyExpand(QuerySetExpander expander, String expandedString, char type, int num, int numFetch, boolean end) {
    Assert.assertTrue(expander.iterator().hasNext());
    String got = expander.iterator().next();
    Assert.assertEquals("Error in expanded string", expandedString, got);
    if (type == 'I') {
      sqlSelect = expander.argIntegers(sqlSelect, intFetch);
      Mockito.verify(sqlSelect, Mockito.times(num)).argInteger(IdInt);
      Mockito.verify(intFetch, Mockito.times(numFetch)).fetch(Mockito.anyInt());
    } else if (type == 'L') {
      sqlSelect = expander.argLongs(sqlSelect, longFetch);
      Mockito.verify(sqlSelect, Mockito.times(num)).argLong(IdLong);
      Mockito.verify(longFetch, Mockito.times(numFetch)).fetch(Mockito.anyInt());
    } else if (type == 'S') {
      sqlSelect = expander.argStrings(sqlSelect, strFetch);
      Mockito.verify(sqlSelect, Mockito.times(num)).argString(IdStr);
      Mockito.verify(strFetch, Mockito.times(numFetch)).fetch(Mockito.anyInt());
    } else {
      Assert.fail("type should have been one of I,L,S, not: "+type);
    }
    if (end) {
      Assert.assertFalse(expander.iterator().hasNext());
    }
  }
}
