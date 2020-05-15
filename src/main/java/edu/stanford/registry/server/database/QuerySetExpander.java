package edu.stanford.registry.server.database;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.susom.database.SqlSelect;

/**
 * The QuerySetExpander is an iterator to expand an SQL string with a " IN (?)" substring
 * potentially multiple times if the set of things is large. JDBC seems to have a limit of
 * 1000 elements. Plus, this always makes the set one of two sizes, otherwise each different
 * size of set would be a different query in the database's query cache.
 * <p>
 * To use it, you'd do something like like the following.
 * <p>
 * In this example, you have a list of Long IDs in myListOfIDs.
 * <pre>class ThingCollector implements RowsHandler&lt;List&lt;Thing>> {
 *   List&lt;Thing> all = new List&lt;Thing>(256); // default maximum per query
 *   ThingCollector(List&lt;Thing> list) {   all = list;  }
 *   public Boolean process(Rows rs) {
 *       return all.add(DataTableObjectConverter.convertToObjects(rs, Thing.class));
 *    }
 * }
 * List&lt;Thing> thingList = new List&lt;Thing>(myListOfIDs.size());
 * ThingCollector thingCollector = new ThingCollector(thingList);
 * QueryExpander <b>expander = new QuerySetExpander</b>(myListOfIDs.size(), sqlStringToExpand);
 * for (String sql: <b>expander</b>) {  // each time, returns an sql with IN(?, ?...)
 *    SqlSelect sqlSelect = database.toSelect(sql).argString(a).argLong(siteId); // add first args
 *    sqlSelect = <b>expander</b>.args(sqlSelect, x -> return myListOfIDs.get(ix)); // add IN args
 *    sqlSelect.argLong(finalSiteId);  // add any args afterwards
 *    thingList = sqlSelect.query(thingCollector {...});
 * }</pre>
 * <p>
 * You can also use a set of argLongs.  More functional interfaces can easily be made.
 * <p>
 * The point is that you use the QuerySetExpander to generate the sql with the expanded set of question marks
 * and then use the QuerySetExpander against to fill in the arguments it created
 * <p>
 * This does NOT yet support two "IN (?)" in your query. To do that, we'd need to apply the same iterator
 * twice.  The best way to do that would be with a separate constructor that noted the number of sets.
 * <p>
 * This DOES support 2 different sets in the query- simply nest the creation of the second expander and its
 * for-loop in the for-loop of the first.
 * <p>
 * The QuerySetExpander will create a query with one of two numbers of question marks. You can set these sizes
 * yourself, or use the default, 32 and 256.  When the number of IDs is smaller, the extra places are filled in
 * with copies of the last ID.
 */
public class QuerySetExpander implements Iterable<String> {
  private static Logger logger = LoggerFactory.getLogger(PatientDao.class);

  // Okay, I should have done just one...
  private int bigNum = 256;   // big enough to save lots of queries
  private int smallNum = 32;  // small enough so doing this for just 1 isn't too much of a waste...

  private final String queryName;
  private final String sqlToExpand;
  private final int numberOfIds;
  private final int questionMarkIx;

  /**
   * The whole QuerySetExpander is an iterator,
   * but error-prone will complain if the same class is both an iterable and an iterator
   */
  QSExpanderIterator iterator;


  /**
   * Constructs the expander over which you can iterate to create multiple SQLs to handle the numberOfIDs,
   * and insert the IDs into the SqlSelect using argStrings(..) or argLongs(..). See the class name for usage.
   * @param queryName if not null or empty, writes a debug log line for which indexes are being output
   * @param numberOfIds  the total number of IDs to handle.
   * @param sql The SELECT statement containing the "IN (?)" to be expanded.
   */
  public QuerySetExpander(String queryName, int numberOfIds, String sql) {
    this.queryName = queryName;
    this.numberOfIds = numberOfIds;
    this.sqlToExpand = sql;

    questionMarkIx = sqlToExpand.indexOf("(?)");
    if (questionMarkIx < 0) {
        throw new RuntimeException("Bad SQL- did not find '(?)' in sql="+sql);
    }
    iterator = new QSExpanderIterator();
  }


  /**
   * Set the small and big sizes.  It must be that: 1 < small <= big <= 1000 (JDBC seems to assert the 1000 max)
   * @param small 2 <= small
   * @param big   small <= big <= 1000, a limit the JDBC seems to impose
   * @return this, for convenience
   */
  public QuerySetExpander setSizes(int small, int big) {
    if (iterator.offset != 0) {
      throw new RuntimeException("Must call QuerySetExpander.setSizes(small,big) BEFORE iterating!");
    }
    if (!((1 < small) && (small <= big) && (big <= 1000))) {
      throw new RuntimeException(String.format(
          "QuerySetExpander.setSizes(small,big) Not true: (1 < %d=small) && (small=%d <= %d=big) && (big=%d <= 1000) ",
          small, small, big, big));
    }
    smallNum = small;
    bigNum = big;
    return this;
  }


  /**
   * Use one of these to fetch the index'th a String ID from a collection.
   */
  @FunctionalInterface public static interface FetchStringFromSet {
    String fetch(int index);
  }

  /**
   * Use one of these to fetch the index'th a Long ID from a collection.
   */
  @FunctionalInterface public static interface FetchLongFromSet {
    Long fetch(int index);
  }

  /**
   * Use one of these to fetch the index'th a Integer ID from a collection.
   */
  @FunctionalInterface public static interface FetchIntegerFromSet {
    Integer fetch(int index);
  }


  /**
   * Returns the single iterator for the expander.
   * Multiple calls will return the same expander.
   */
  @Override
  public Iterator<String> iterator() {
    return iterator;
  }


  /**
   * Fills in the proper number of string IDs
   * @param sqlSelect
   * @param fetcher  Something like ix -> return myListOfIDs.get(ix)
   * <br>Or ix -> return myListOfThings.get(ix).getId()
   */
  public SqlSelect argStrings(SqlSelect sqlSelect, FetchStringFromSet fetcher) {
    return iterator.argStrings(sqlSelect, fetcher);
  }


  /**
   * Fills in the proper number of string IDs
   * @param sqlSelect
   * @param fetcher  Something like ix -> return myListOfIDs.get(ix)
   * <br>Or ix -> return myListOfThings.get(ix).getId()
   */
  public SqlSelect argLongs(SqlSelect sqlSelect, FetchLongFromSet fetcher) {
    return iterator.argLongs(sqlSelect, fetcher);
  }


  /**
   * Fills in the proper number of string IDs
   * @param sqlSelect
   * @param fetcher  Something like ix -> return myListOfIDs.get(ix)
   * <br>Or ix -> return myListOfThings.get(ix).getId()
   */
  public SqlSelect argIntegers(SqlSelect sqlSelect, FetchIntegerFromSet fetcher) {
    return iterator.argIntegers(sqlSelect, fetcher);
  }


  class QSExpanderIterator implements Iterator<String> {
    // iteration variables - these first two are updated as the IDs are being added to the SqlSelect.
    private int offset;       // The index of the next ID to add to the IN(?, ?, ?...) statement
    private int numLeft;      // The number left for the next run- when this is zero, there are no more left

    // iteration variables - these are computed during hasNext()
    private int numQsThisTime;  // The total number of question marks, that is, the number of IDs to add
    private int numUniqueArgsThisTime;      // The number of unique IDs to add. Equals numQsThisTime except possibly the last time.

    QSExpanderIterator() {
      offset = 0;
      numUniqueArgsThisTime = 0;
      numLeft = numberOfIds;
    }


    /**
     * hasNext() and next() are needed to make this object be an Iterator<String>
     */
    @Override
    public boolean hasNext() {
      // numberOfIds is a constant (final, set in the constructor)
      // offset is already set up, and incremented when the IDs are added to the SQL statement
      // We need to set numLeft, figure out the totalNumQsThisTime to use this time and numIDsThisTime
      numLeft = numberOfIds - offset;
      if (numLeft <= 0) {  // this is less than zero if we added duplicates last time
        return false;
      }

      numQsThisTime = (numLeft > smallNum) ? bigNum : smallNum;
      numUniqueArgsThisTime = (numLeft < numQsThisTime) ? numLeft : numQsThisTime;
      if (queryName != null && !queryName.isEmpty() && logger.isDebugEnabled()) {
        logger.debug("{}: querying for {} IDs starting with #{} of {} total, using a set size of {}",
            queryName, numQsThisTime, offset, numberOfIds, numQsThisTime);
      }
      // The (?) will expand to be numQsThisTime
      // We'll put numUniqueArgsThisTime args in and then (numQsThisTime - numUniqueArgsThisTime) copies of the last ID
      return true;
    }


    /**
     * hasNext() and next() are needed to make this object be an Iterator<String>
     * <p>
     * Returns a string where "(?)" in the sql (after offset) to be numQsThisTime instead of just 1, like (?,?,?...)
     * <p>
     * Note since smallNum > 1, "(?)" will always expand, so a nested expander will find a second (?).
     *
     * @param sql The query to change
     * @param offset Finds the first occurrence of (?) after offset
     * @param numQsThisTime The total number of items in the set (must be greater than 0)
     */
    @Override
    public String next() {
      // hasNext() has already been called, and it determined numQsThisTime and numUniqueArgsThisTime.
      int sqlToExpandLen = sqlToExpand.length();

      int ix = questionMarkIx + 2;  // point to the close-paren, so 0..ix is "SELECT ... IN (?"

      StringBuilder sb = new StringBuilder(sqlToExpandLen + 2*numQsThisTime).append(sqlToExpand, 0, ix);
      for (int i = 1;  i < numQsThisTime;  i++) {
        sb.append(',').append('?');
      }
      sb.append(sqlToExpand, ix, sqlToExpandLen);
      return sb.toString();
    }


    private SqlSelect argStrings(SqlSelect sqlSelect, FetchStringFromSet fetcher) {
      String lastId = null;
      int endIx = offset + numUniqueArgsThisTime;
      for ( ;  offset < endIx;  offset++) {
        lastId = fetcher.fetch(offset);
        sqlSelect = sqlSelect.argString(lastId);
      }
      endIx += numQsThisTime - numUniqueArgsThisTime;  // the last time, this endIx might be bigger
      for ( ;  offset < endIx;  offset++) {
        sqlSelect = sqlSelect.argString(lastId); // add the last string again to
      }
      return sqlSelect;
    }


    private SqlSelect argLongs(SqlSelect sqlSelect, FetchLongFromSet fetcher) {
      Long lastId = null;
      int endIx = offset + numUniqueArgsThisTime;
      for ( ;  offset < endIx;  offset++) {
        lastId = fetcher.fetch(offset);
        sqlSelect = sqlSelect.argLong(lastId);
      }
      endIx += numQsThisTime - numUniqueArgsThisTime;  // the last time, this endIx might be bigger
      for ( ;  offset < endIx;  offset++) {
        sqlSelect = sqlSelect.argLong(lastId); // add the last string again to
      }
      return sqlSelect;
    }


    private SqlSelect argIntegers(SqlSelect sqlSelect, FetchIntegerFromSet fetcher) {
      Integer lastId = null;
      int endIx = offset + numUniqueArgsThisTime;
      for ( ;  offset < endIx;  offset++) {
        lastId = fetcher.fetch(offset);
        sqlSelect = sqlSelect.argInteger(lastId);
      }
      endIx += numQsThisTime - numUniqueArgsThisTime;  // the last time, this endIx might be bigger
      for ( ;  offset < endIx;  offset++) {
        sqlSelect = sqlSelect.argInteger(lastId); // add the last string again to
      }
      return sqlSelect;
    }
  }
}