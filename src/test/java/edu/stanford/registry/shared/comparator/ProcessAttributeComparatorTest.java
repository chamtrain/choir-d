package edu.stanford.registry.shared.comparator;

import edu.stanford.registry.shared.ProcessAttribute;
import org.junit.Assert;
import org.junit.Test;

import static edu.stanford.registry.shared.comparator.ProcessAttributeComparator.SORT_BY_VALUE;
import static edu.stanford.registry.shared.comparator.ProcessAttributeComparator.SORT_BY_NAME;
import static edu.stanford.registry.shared.comparator.ProcessAttributeComparator.SORT_BY_INTEGER;
import static edu.stanford.registry.shared.comparator.ProcessAttributeComparator.SORT_BY_VALUE_EXPIRATION;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProcessAttributeComparatorTest {
  static SimpleDateFormat fmt = new SimpleDateFormat("DD/mm/yyyy");
  static ProcessAttribute.FmtDate fmtd = new ProcessAttribute.FmtDate() {
    @Override public String format(Date d) {
      return d == null ? "null" : fmt.format(d);
    }
  };
  class PA extends ProcessAttribute {
    
    PA(String name, Integer value) {
      super(name, value);
    }
    PA(String name, String value) {
      super(name, value);
    }
    PA(String name, Integer value, String date1, String date2) {
      super(name, value);
        try {
          setStartDate((date1==null || date1.isEmpty()) ? null : fmt.parse(date1));
          setEndDate((date2==null || date2.isEmpty()) ? null : fmt.parse(date2));
        } catch (ParseException e) {
          e.printStackTrace();
          Assert.fail();
        }
    }
    @Override
    public String toString() {
      if (sortBy == SORT_BY_VALUE || sortBy == SORT_BY_INTEGER)
        return "(" + "typ="+this.getType()+",val="+this.getValue()+",int="+this.getInteger() + ")";

      if (sortBy == SORT_BY_NAME)
        return "(" + "nam="+this.getName() + ")";

      // (sortBy == SORT_BY_VALUE_EXPIRATION)
      return super.toString(fmtd);
    }
  }

  final String na = "n/a";
  int sortBy;
  String sortByWhat;
  Integer NULL_INT = null;
  String NULL_STR = null;
  
  ProcessAttributeComparator mkComp(int byX) {
    if (byX == SORT_BY_VALUE)
      sortByWhat = "sorting by Value: ";
    else if (byX == SORT_BY_NAME)
      sortByWhat = "sorting by Name: ";
    else if (byX == SORT_BY_INTEGER)
      sortByWhat = "sorting by Int: ";
    else if (byX == SORT_BY_VALUE_EXPIRATION)
      sortByWhat = "sorting by Value+Exp: ";
    sortBy = byX;
    return new ProcessAttributeComparator(byX);
  }

  @Test
  public void testToEnsureIsSmallerIsCorrect() {
    if (isSmaller(new PA("two", 2), new PA("one", 1), new ProcessAttributeComparator(SORT_BY_INTEGER)))
      Assert.fail("Oh oh!  The sense of isSmaller() is wrong :?(  ");
    
    if (!isSmaller(new PA("one", 1), new PA("two", 2), new ProcessAttributeComparator(SORT_BY_INTEGER)))
      Assert.fail("Huh??? This can't fail if the one above passes!!!");
 
    Assert.assertEquals("", " < ", rel(Integer.valueOf(1).compareTo(Integer.valueOf(2))));
  }

  @Test
  public void testByNameWi1Null() {
    test3(new PA("Able", "a"), new PA("Baker", "b"), new PA(null, "x"), mkComp(SORT_BY_NAME));
  }

  @Test
  public void testByNameWi2Nulls() {
    test3(new PA("Able", "a"), new PA(null, "b"), new PA(null, "x"), mkComp(SORT_BY_NAME));
  }



  @Test
  public void testByInteger123() {
    test3(new PA("Able", 1), new PA("Baker", 2), new PA(null, 3), mkComp(SORT_BY_INTEGER));
  }

  @Test
  public void testByValExpNumSmaller112() {
    assertSmaller(new PA("Zombic", 11), new PA("Able", 2), mkComp(SORT_BY_INTEGER));
  }

  @Test
  public void testByValExpNumSmaller112SwitchNames() {
    assertSmaller(new PA("Able", 11), new PA("Zombic", 2), mkComp(SORT_BY_INTEGER));
  }

  @Test
  public void testByValExpEqValExpSmaller32Date() {
    PA a = new PA("Zombic", 11, "1/1/1990", "1/1/1990");
    PA b = new PA("Able", 2, "1/1/1980", "1/1/1990");
    assertSmaller(a, b, mkComp(SORT_BY_VALUE_EXPIRATION));
  }

  @Test
  public void testByValExpEqValExpSmaller33nullDate() {
    PA a = new PA("Zombic", 3, "1/1/1980", null);
    PA b = new PA("Able", 3, "1/1/1980", "1/1/1990");
    assertSmaller(a, b, mkComp(SORT_BY_VALUE_EXPIRATION));
  }

  @Test
  public void testByValExpEqValExpSmaller36nullDate() {
    PA a = new PA("Zombic", 3, "1/1/1980", null);
    PA b = new PA("Able", 6, "1/1/1980", "1/1/1990");
    assertSmaller(a, b, mkComp(SORT_BY_VALUE_EXPIRATION));
  }

  @Test
  public void testByValExpNullSmaller() {
    assertSmaller(null, new PA("Able", 6, "1/1/1980", "1/1/1990"), mkComp(SORT_BY_VALUE_EXPIRATION));
  }

  @Test
  public void testByValExpEquals() {
    PA a = new PA("Zombic", 3, "1/1/1970", "1/1/1980");
    PA b = new PA("Able", 3, "1/1/1980", "1/1/1980");
    assertEquals(a, b, mkComp(SORT_BY_VALUE_EXPIRATION));
  }

  @Test
  public void testByValExpEqualsWiNullDates() {
    PA a = new PA("Zombic", 3, "1/1/1980", null);
    PA b = new PA("Able", 3, "1/1/1980", null);
    assertEquals(a, b, mkComp(SORT_BY_VALUE_EXPIRATION));
  }

  @Test
  public void testByIntegerNull23() {
    test3(new PA("Able", NULL_INT), new PA("Baker", 2), new PA(null, 3), mkComp(SORT_BY_INTEGER));
  }

  @Test
  public void testByIntegerNull2NULL() {
    test3(new PA("Able", NULL_INT), new PA("Baker", 2), new PA(null, NULL_INT), mkComp(SORT_BY_INTEGER));
  }



  @Test
  public void testByIntegerStringAndTwoInts() {
    test3(new PA("Able", "a"), new PA("Baker", 3), new PA(null, 6), mkComp(SORT_BY_INTEGER));
  }

  @Test
  public void testByIntegerStringEqualOneOfTwoInts() {
    test3(new PA("Able", "3"), new PA("Baker", 3), new PA(null, 6), mkComp(SORT_BY_INTEGER));
  }

  @Test
  public void testByIntegerStringBetweenTwoInts() {
    test3(new PA("Able", "4"), new PA("Baker", 3), new PA(null, 6), mkComp(SORT_BY_INTEGER));
  }

  @Test
  public void testByIntegerStringCleverlyBetweenTwoInts() {
    test3(new PA("Able", "35"), new PA("Baker", 4), new PA(null, 306), mkComp(SORT_BY_INTEGER));
  }



  @Test
  public void testByStringStringAndTwoInts() {
    test3(new PA("Able", "a"), new PA("Baker", 3), new PA(null, 6), mkComp(SORT_BY_VALUE));
  }

  @Test
  public void testByStringStringEqualOneOfTwoInts() {
    test3(new PA("Able", "3"), new PA("Baker", 3), new PA(null, 6), mkComp(SORT_BY_VALUE));
  }

  @Test
  public void testByStringStringBetweenTwoInts() {
    test3(new PA("Able", "4"), new PA("Baker", 3), new PA(null, 6), mkComp(SORT_BY_VALUE));
  }

  @Test
  public void testByStringStringCleverlyBetweenTwoInts() {
    test3(new PA("Able", "35"), new PA("Baker", 4), new PA(null, 306), mkComp(SORT_BY_VALUE));
  }

  @Test
  public void testByStringStringAlsoCleverlyBetweenTwoInts() {
    test3(new PA("Able", "5"), new PA("Baker", 40), new PA(null, 306), mkComp(SORT_BY_VALUE));
  }



  void test3(PA a, PA b, PA c, ProcessAttributeComparator comp) {
    confirmOpposites(a, b, comp);
    confirmOpposites(b, c, comp);
    confirmOpposites(a, c, comp);

    // handle any equality
    if (sameAndConfirmed(a, b, c, comp))
      return;
    else if (sameAndConfirmed(b, c, a, comp))
      return;
    else if (sameAndConfirmed(c, a, b, comp))
      return;
    
    // no 2 are equal, let's order a < b
    if (isSmaller(b, a, comp)) {
      PA tmp = a;  a = b;  b = tmp;
    }

    // a < b  -- we only have a problem if c < a and b < c
    if (isSmaller(c, a, comp) && isSmaller(b, c, comp))
      Assert.fail(sortByWhat+"C"+c+" < A"+a+" < B"+b+" < C");
  }

  void assertSmaller(PA a, PA b, ProcessAttributeComparator comp) {
    Assert.assertTrue("assert "+a+" < "+b, isSmaller(a, b, comp));
  }

  void assertEquals(PA a, PA b, ProcessAttributeComparator comp) {
    Assert.assertTrue("assert "+a+" = "+b, 0 == comp.compare(a, b));
  }

  void confirmOpposites(PA a, PA b, ProcessAttributeComparator comp) {
    int ab = comp.compare(a, b);
    int ba = comp.compare(b, a);
    if (ab != - ba)
      Assert.fail(sortByWhat+"A"+a+rel(ab)+"B"+b+" But B"+rel(ba)+"A - not reversible");
  }

  public boolean isSmaller(PA a, PA b, ProcessAttributeComparator comp) {
    return comp.compare(a, b) < 0;
  }

  /**
   * If (a == b), this confirms (b == a) AND a-to-c is the same as b-to-c,  and then returns true
   * if (a != b) returns false
   * @return true if they're equal and pass tests, false if they're unequal
   */
  boolean sameAndConfirmed(ProcessAttribute a, ProcessAttribute b, ProcessAttribute c, ProcessAttributeComparator comp) {
    int ab = comp.compare(a, b);
    if (0 != ab)
      return false;

    // They're the same, a==b and we already tested opposites
    int x = comp.compare(a, c);
    int y = comp.compare(b, c);
    if (x != y)
      Assert.fail(sortByWhat+"A"+a+" == B"+b+" but A"+rel(x)+"C and B"+rel(y)+" C"+c);

    return true;  // a?c is the same as b?c, so all's good
  }

  /**
   * Say the value of a relationship
   */
  String rel(int x) {
    return (x == 0) ? " == " : ((x < 0) ? " < " : " > ");
  }
}
