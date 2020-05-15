package edu.stanford.registry.server.utils;


import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.SiteInfo;
import edu.stanford.registry.server.config.AppConfigDao;
import edu.stanford.registry.server.config.AppConfigEntry;
import edu.stanford.registry.shared.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.github.susom.database.Database;
import com.github.susom.database.DatabaseProvider;
import com.github.susom.database.Flavor;
import com.github.susom.database.Rows;
import com.github.susom.database.RowsHandler;
import com.github.susom.database.Schema;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;

/**
 * Created by tpacht on 5/12/2016.
 */
public class SquareUtils {

  public static final ParameterFactory factory = AutoBeanFactorySource.create(ParameterFactory.class);
  public static final String CONFIGTYPE = AppConfigDao.ConfigType.SQUARETABLE.toString();

  public static boolean findTable(DatabaseProvider dbp, String tableName) {
    return findTable(dbp.get(), tableName);
  }

  public static boolean findTable(Database db, String tableName) {
    String findTableQuery = "SELECT count(*) FROM user_tables WHERE upper(table_name) = :tblname"; // oracle
    if (db.flavor().equals(Flavor.postgresql))
      findTableQuery = "SELECT count(*) FROM information_schema.tables WHERE upper(table_name) = :tblname";

    int found  = db
        .toSelect(findTableQuery)
        .argString(":tblname", tableName.toUpperCase())
        .query(
            new RowsHandler<Integer>() {
              @Override
              public Integer process(Rows rs) throws Exception {

                int count = 0;
                if (rs.next()) {
                  count = rs.getIntegerOrZero(1);
                }
                return count;
              }
            });
    if (found > 0) {
      return true;
    }
    return false;
  }

  public static void makeTable(DatabaseProvider dbp, String tableName) {
    //dbp.transact((dbs, t) -> {
    makeTable(dbp.get(), tableName);
    //});
  }

  public static void makeTable(Database db, String tableName) {
    String pkName = tableName;
    if (db.flavor().equals(Flavor.postgresql))
      pkName += "_pk"; // postGres disallows indexes to have the same name as a table- both are "relations"

    Schema schema = new Schema().addTable(tableName)
        .addColumn("survey_site_id").asLong().table()
        .addColumn("survey_token_id").asLong().table()
        .addColumn("patient_id").asString(50).table()
        // Note: PostGres won't allow the index to have the same name as the table
        .addPrimaryKey(pkName, "survey_site_id", "survey_token_id").table()
        .schema();
    schema.execute(db);
  }

  public static void makeColumn(Database db, String tableName, String ref, String type) {
    boolean useParens = db.flavor().equals(Flavor.oracle); // postGres needs no parens around the column name and type
    String openParen = useParens ? "(" : "";
    String closeParen = useParens ? ")" : "";
    String query = "alter table " + tableName + " add " + openParen + ref.trim() + " " + getDatabaseColumnType(db, type) + closeParen;
    db.ddl(query).execute();
  }

  /**
   * @param tableName
   * @return a hashmap of column_name -> data_type for the columns in the table
   */
  public static Map<String, String> getTableColumns(Database dbp, String tableName) {
    String columnQuery = "SELECT column_name, data_type FROM user_tab_columns WHERE upper(table_name) = :tblname"; // oracle
    if (dbp.flavor().equals(Flavor.postgresql))
      columnQuery = "SELECT upper(column_name), data_type FROM information_schema.columns WHERE upper(table_name) = upper(:tblname)";

    return
        dbp.get().toSelect(columnQuery)
            .argString(":tblname", tableName).query(new RowsHandler<HashMap<String, String>>() {
          @Override
          public HashMap<String, String> process(Rows rs) throws Exception {
            HashMap<String, String> cols = new HashMap<String, String>();
            while (rs.next()) {
              cols.put(rs.getStringOrEmpty(1), rs.getStringOrEmpty(2));
            }
            return cols;
          }
        });
  }

  public static String getDatabaseColumnType(Database database, String type) {
    if ("select1".equals(type.toLowerCase()) || "radio".equals(type.toLowerCase())
        || "dropdown".equals(type.toLowerCase()) || "radiosetgrid".equals(type.toLowerCase())) {
      return database.flavor().typeInteger();
    } else if ("select".equals(type.toLowerCase())) {
      return database.flavor().typeStringFixed(1);
    } else if ("promis".equals(type.toLowerCase())) {
      return database.flavor().typeBigDecimal(10, 5);
    }
    return database.flavor().typeStringVar(4000);
  }

  public static String getResponse(String question, boolean required) {
    return getResponse(question, required, null);
  }
  public static String getResponse(String question, boolean required, String[] validResponses) {
    String answer = "";
    boolean done = false;

    @SuppressWarnings("resource") // no need to close this- closing an InputStream does nothing
    Scanner scan = new Scanner(System.in);
    while (!done) {
      System.out.println(question);
      answer = scan.next();  // Should really be nextLine(), no?
      if (answer.isEmpty()) {
        if (required) {
          System.out.println("you must provide an answer");
        } else {
          done = true;
        }
      }

      else if (validResponses == null) {
        done = true;
      } else {

        for (String v: validResponses)
          if (answer.equals(v)) {
            done = true;
            break;
          }
        if (!done)
            System.out.println("Valid answers are: " + String.join(", ", validResponses));
      }
    }
    return answer;
  }

  public static ParameterFactory getFactory() {

    return factory;
  }

  public static AppConfigEntry addConfig(Database db, SiteInfo siteInfo, String tableName, String studyName, String prefix) {
    User adminUser = ServerUtils.getAdminUser(db);
    AutoBean<SquareTableParameters> paramsBean = factory.squareTableParameters();
    SquareTableParameters params = paramsBean.as();
    params.setPrefix(prefix);
    params.setTableName(tableName);

    AppConfigDao appConfigDao = new AppConfigDao(db, adminUser);
    appConfigDao.addOrEnableAppConfigEntry(siteInfo.getSiteId(), CONFIGTYPE, studyName, AutoBeanCodex.encode(paramsBean).getPayload() );
    return appConfigDao.findAppConfigEntry(siteInfo.getSiteId(), CONFIGTYPE, studyName);
  }

  public static AppConfigEntry getConfig(Database db, SiteInfo siteInfo, String studyName) {
    AppConfigDao appConfigDao = new AppConfigDao(db, ServerUtils.getAdminUser(db));
    AppConfigEntry entry = appConfigDao.findAppConfigEntry(siteInfo.getSiteId(), CONFIGTYPE, studyName);
    if (entry != null && entry.isEnabled()) {
      return entry;
    }
    return null;
  }

  public static AppConfigEntry getConfig(Database db, SiteInfo siteInfo, Long configId) {
    AppConfigDao appConfigDao = new AppConfigDao(db, ServerUtils.getAdminUser(db));
    AppConfigEntry entry = appConfigDao.findAppConfigEntry(configId);
    if (entry.isEnabled()) {
      return entry;
    }
    return null;
  }
  public interface SquareTableParameters {

    String getPrefix();

    void setPrefix(String prefix);

    String getTableName();

    void setTableName(String tableName);

    Long getContentId();

    void setContentId(Long contentId);
  }

  public interface ParameterFactory extends AutoBeanFactory {
    /**
     * Bump this when making changes client and server need to agree on.
     */
    long compatibilityLevel = 1;

    AutoBean<SquareTableParameters> squareTableParameters();
  }
}

