package edu.stanford.registry.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * For each database flavors, run: ant create.db.scripts"
 * <br>It runs CreateRegistrySchema -verbose > build/release/createdb-<driver>.log
 * <br>And then runs this to create the sql schema-creation and
 * initial data creation sql scripts, for a release.
 *
 * Note: Developers can do this themselves to produce sql scripts containing their custom data.
 *
 * Note: Your build.properties file must have the property set to match the database below:  registry.database.driver
 *
 * @author rstr
 */
public class MakeDBCreationScripts {

  // Update this for each new release!
  static public final String VERSION = "3.0.0";  // Part of the output-file name

  /**
   * Contains the database types plus generates the input and output file names, and stores the new sql script version
   */
  enum DBType {
    Oracle("oracle.jdbc.OracleDriver"),
    PostGres("org.postgresql.Driver");

    private final String driver;  // ant uses this to name the log for the database type
    DBType(String driver) {
      this.driver = driver;
    }

    public String logName() {
      return "createdb-" + driver + ".log";
    }
    public String outName(boolean tableElseData) {
      return "choir-" + VERSION + (tableElseData ? "-tables-" : "-data-") + name().toLowerCase() + ".sql";
    }
    boolean isOracle() {
      return this.equals(Oracle);
    }
    boolean isPostGres() {
      return this.equals(PostGres);
    }
  }


  static void usage(String msg) {
    p("");
    if (!msg.isEmpty()) {
      p("  ERROR: "+msg+"\n");
    }
    p("  USAGE: java ... releaseDir");
    p("    Cleans up the 'ant create.database' logs for oracle and postgres");
    p("    Looks in the dir for input files:");
    for (DBType type: DBType.values()) {
      p("       "+type.logName());
    }
    p("    Writes 2 files for each DB type ( the version, now "+VERSION+" is a static var ) into the same dir:");
    for (DBType type: DBType.values()) {
      p("       "+type.outName(true)+(type.isOracle()?"     ":"   ")+type.outName(false));
    }
    p("    If just one log file is found, writes just one pair of sql files.");
    p("");
    System.exit(1);
  }


  public static void main(String[] args) {
    // Gets the directory, finds the input file(s), makes the output streams
    ArgProcessor argProcessor = new ArgProcessor(args);

    MakeDBCreationScripts cleaner = new MakeDBCreationScripts();
    for (DBType type: DBType.values()) {
      try {
        if (!argProcessor.inputExists(type))
          continue;

        p("\n ===== Creating the scripts for: "+type.name()+"\n");
        cleaner.run(type, argProcessor.getScanner(type),
                    argProcessor.getTablesStream(type), argProcessor.getDataStream(type));
      } catch (Throwable e) {
        e.printStackTrace(System.err);
      } finally {
        argProcessor.close();
      }
    }
    p("MakeDBCreationScripts finished");
  }


  /**
   * IO functions for a single input-line.
   * In the first version, we manage to do this w/o remembering the previous line,
   * but it could do that, too, if needed...
   */
  static class LineIO {
    private final PrintStream tablesStream;
    private final PrintStream dataStream;
    // private final DBType dbType; // in case there are database-type-specific differences

    private String s;

    LineIO(DBType dbType, PrintStream tablesStream, PrintStream dataStream) {
      // this.dbType = dbType;
      this.tablesStream = tablesStream;
      this.dataStream = dataStream;
    }

    void set(String line) {
      final String java = "[java] ";
      int ix = line.indexOf(java);
      s = (ix < 0) ? line : line.substring(ix+java.length());
    }

    String getS() {
      return s;
    }


    boolean justnuke(String text) {
      int ix = s.indexOf(text);
      if (ix < 0)
        return false;

      s = s.substring(ix + text.length());
      return true;
    }

    /**
     * If line contains text, nuke to text and to ") "
     * @return true if line contains the text
     */
    boolean nuke(String text) {
      if (!justnuke(text))
        return false;

      // After the DDL: or Insert: or Update: is time(time,time,time)<tab>
      int ix = s.indexOf(")\t");
      if (ix > -1)
        s = s.substring(ix+2);
      return true;
    }


    void print(boolean isTables, String str) {
      if (isTables) {
        tablesStream.println(str);
      } else {
        dataStream.println(str);
      }
    }


    boolean ddlWas2LineAlterStmt;


    void printDDL() {
      if (s.startsWith("create table")) {
        tablesStream.println();  // add a blank line before a new table

      } else if (s.startsWith("alter table")) { // always a 2-line command
        ddlWas2LineAlterStmt = true;

      } else if (s.startsWith("create index") || s.startsWith("create sequence")
              || s.startsWith("create unique index")) { // 1-line commands
        tablesStream.print(s);
        tablesStream.println(";");
        return;
      }

      tablesStream.println(s);
    }

    public boolean printInsertOrUpdateIsFinished(boolean isTables) {
      s = s.replace("<java.io.StringReader>", ""); // Weird bug- can't get a fixed version of the database.jar...
      int ix = s.indexOf("|");
      if (ix >= 0) {
        s = s.substring(ix+1);
      }
      return printDataTellFinished(numberQuotesIsEven()); // finished if has an even number of quotes, else includes a newline
    }


    private boolean printDataTellFinished(boolean finished) {
      if (finished) {
        dataStream.print(s);
        dataStream.println(";");
      } else {
        dataStream.println(s);
      }
      return finished;
    }


    private boolean numberQuotesIsEven() {
      int n = 0;
      for (int ix = s.indexOf('\'');  0 <= ix;  ix = s.indexOf('\'', ix+1)) {
        n++;
      }
      return 0 == (n & 1);
    }


    public boolean printMoreDataIsFinished() {
      return printDataTellFinished(!numberQuotesIsEven()); // finished if has an odd number of quotes, else includes a newline
    }


    public void printMoreDDL() {
      char c = s.charAt(s.length() - 1);
      boolean lastCreateLine = s.startsWith(")")  ?  (s.length() == 1 /* postgres */ || /* oracle */ s.contains(" store as "))  :  false;
      boolean addSemi = ddlWas2LineAlterStmt || lastCreateLine || (c == '\'');
      if (addSemi) {
        tablesStream.print(s);
        tablesStream.println(";");
        ddlWas2LineAlterStmt = false;
      } else {
        tablesStream.println(s);
      }
    }
  }


  private void addScriptHeader(DBType dbtype, String scriptName, String title, String tblOrData, PrintStream stream) {
    stream.println("-- "+title+" script for CHOIR v"+VERSION+" for database: "+dbtype.name());

    if (dbtype.isOracle()) {
      stream.println("-- run with:  sqlplus user/password@host:port/SID @"+dbtype.logName()+" @"+scriptName);
      stream.println();
      stream.println("-- The following allows blank lines in strings (e.g. email templates)");
      stream.println("SET SQLBLANKLINES ON");
      stream.println("-- The following echoes the inserts to the console");
      stream.println("SET ECHO ON");
      stream.println("select concat('Running script "+scriptName+" on ', sysdate) from dual;");
    } else if (dbtype.isPostGres()) {
      stream.println("-- run with:  psql -q -b --log-file="+tblOrData+".log --file="+scriptName+" DBNAME user \n");
      stream.println("--   -q suppresses logging many lines of 'INSERT 0 1' to the console");
      stream.println("--   -b logs errors to the console");
      stream.println("--   --log-file=file writes the insert statements to a log");
    }

    // The first DDL command is a create-table command, so a blank line will be added before it
  }


  private void run(DBType dbtype, Scanner scanner, PrintStream tableStream, PrintStream dataStream) {
    addScriptHeader(dbtype, dbtype.outName(true), "Schema-creation", "tables", tableStream);
    addScriptHeader(dbtype, dbtype.outName(false), "Initial-data", "data", dataStream);

    boolean tooEarly = true;
    boolean isTables = true;
    final String ddl = "DDL:";
    final String query = "Query:";
    final String insert = "Insert:";
    final String update = "Update:";
    final String params = "ParamSql:";
    LineIO line = new LineIO(dbtype, tableStream, dataStream);
    int numMissingParams = 0;
    int numDataLines = 0;
    boolean updateIsFinished = false;

    while (scanner.hasNextLine()) {
      line.set(scanner.nextLine());
      if (tooEarly) {
        if (!line.s.contains(ddl)) {
          continue;  // havent seen the first ddl line yet
        }
        tooEarly = false;
      }

      if (line.nuke(ddl)) {
        isTables = true;
        line.printDDL();

      } else if (line.nuke(query)) {
        isTables = false; // now in data mode

      } else if (line.nuke(insert) || line.nuke(update)) {
        isTables = false; // now in data mode
        numMissingParams += (line.justnuke(params)) ? 0 : 1;
        updateIsFinished = line.printInsertOrUpdateIsFinished(isTables);
        numDataLines++;

      } else if (isTables) {  // queries are all on one line
        line.printMoreDDL();
      } else if (!updateIsFinished) {
        updateIsFinished = line.printMoreDataIsFinished();
      } else {
        if (!line.s.contains("adding service urlPathToService") &&
            !line.s.contains("Standard app_config types are")) { // these are expected
          p("Ignoring: "+line.s);
        }
      }
    } while (scanner.hasNextLine());

    if (dbtype.isOracle()) {
      tableStream.println("\nquit;");
      dataStream.println("\nquit;");
    }
    tableStream.println("\n-- END of schema-creation script for CHOIR v"+VERSION+" for database: "+dbtype.name()+"\n");
    dataStream.println( "\n-- END of initial-data script for CHOIR v"+VERSION+" for database: "+dbtype.name()+"\n");
    if (numMissingParams > 0) {
      err("Parameter settings were missing from "+numMissingParams+" of "+numDataLines+" update/insert statements");
      err("  ENSURE -verbose is set for CreateRegistrySchema");
    }
  }

  void err(String s) {
    try {
      System.out.flush();
      Thread.sleep(20);  // ensure stderr and stdout are sequential
      System.err.println(s);
      System.err.flush();
      Thread.sleep(20);
    } catch (InterruptedException e) {
      // ignore
    }
  }
  static void p(String s) {
    System.out.println(s);
  }

  static class ArgProcessor {
    private final File dir;
    private final File inDir; // the subdir containing the input logs

    // These are created and given to caller, and kept for the close()
    private Scanner scanner;
    private PrintStream tablesStream;
    private PrintStream dataStream;

    public ArgProcessor(String[] args) {
      dir = processArgs(args);
      inDir = new File(dir, "in");
      ensureAnInputFileExists();
    }

    private File processArgs(String[] args) {
      File d = null;
      for (String arg: args) {
        if (arg.startsWith("-")) {
          handleDashArg(arg);
          continue;
        }
        if (d == null) {  // no args yet, first must be inputFile
          d = getDir(arg);
        } else {
          usage("Only need 1 arg, not: "+arg);
        }
      }
      if (d == null) {
        p("No directory is specified, will try ./release");
        d = getDir("build/release");
      }
      return d;
    }

    private void ensureAnInputFileExists() {
      boolean gotOne = false;
      for (DBType type: DBType.values()) {
        boolean got = inputExists(type);
        p(" -- input file "+(got ? "found  : " : "missing: ")+type.logName());
        gotOne |= got;
      }
      if (!gotOne) {
        usage("None of the input log files exist in: "+dir.getAbsolutePath());
      }
    }

    public boolean inputExists(DBType type) {
      return new File(inDir, type.logName()).exists();
    }

    Scanner getScanner(DBType type) throws FileNotFoundException {
      scanner = new Scanner(new File(inDir, type.logName()));
      return scanner;
    }

    PrintStream getTablesStream(DBType type) {
      return tablesStream = makeOutputStream(type, true);
    }

    PrintStream getDataStream(DBType type) {
      return dataStream = makeOutputStream(type, false);
    }

    void close() {
      if (scanner != null) {
        scanner.close();
      }
      if (dataStream != null) {
        try {
          dataStream.close();
        } catch (Exception e) {
          // ignore
        }
      }
      if (tablesStream != null) {
        try {
          tablesStream.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }

    void handleDashArg(String arg) {
      switch (arg) {
      case "-h":
      case "--help":
      case "-help":
        usage("");
        break;
      default:
        usage("Unknown command: "+arg);
      }
    }

    private PrintStream makeOutputStream(DBType type, boolean tableElseData) {
      File outFile = new File(dir, type.outName(tableElseData));
      if (!outFile.exists()) {
        File parent = outFile.getParentFile();
        if (!parent.exists()) {
          usage("Parent folder of outputFile does not exist: "+parent.getAbsolutePath());
        } else if (!parent.canWrite()) {
          usage("Can not write to outputFile's folder: "+parent.getAbsolutePath());
        }
      } else if (!outFile.canWrite()) {
        usage("Can not overwrite outputFile: "+outFile.getAbsolutePath());
      }
      try {
        return new PrintStream(outFile); // made on one platform, used on another...
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
        return null;
      }
    }

    private File getDir(String arg) {
      File d = new File(arg);
      if (!d.exists()) {
        usage("Directory does not exist: "+d.getAbsolutePath());
      } else if (!d.canRead()) {
        usage("Directory isn't readable: "+d.getAbsolutePath());
      } else if (!d.canWrite()) {
        usage("Directory isn't writable: "+d.getAbsolutePath());
      }
      p(" -- found directory: "+d.getAbsolutePath());
      return d;
    }
  }
}
