/*
 * Copyright 2017 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.registry.server.service.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;

//import org.mockito.ArgumentMatchers;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.runners.MockitoJUnitRunner;

import edu.stanford.registry.server.utils.StanfordMrn;

/**
 * Creates Patient.csv and Appointment.csv files
 */
public class ImportFilesCreate {
  //false puts them in path/subdir/patient.csv instead of e.g. path/subdir/Patient/Patient.csv
  static boolean putThemInImportDirs = true; // true makes them immediately ready for import
  static boolean useSameNamesAboveImportDirs = false; // if true (-l), you can copy straight in

  static String pathFmt = "/var/tmp/%s/xchgin";
  static String siteList = "1,ped"; // these/patient.csv and these/appt.csv will be created if they're missing

  static int minNumAppts = 20;  // makes a random number between min and max
  static int maxNumAppts = 25;

  public static void main(String args[]) {
    processArgs(args);

    ImportFilesCreate im = new ImportFilesCreate();

    String[] sites = siteList.split(",");
    System.out.println();
    for (String site: sites) {
      String path = String.format(pathFmt, site);
      File dir = new File(path);
      if (!dir.exists()) {
        System.err.println("Base dir for import does not exist: "+path);
      } else {
        im.createRealImportFiles(new File(path), site);
      }
    }
    System.out.println();
  }

  int encounterEID = 0;
  static class PersonMaker {

    long siteId;
    StanfordMrn mrn = new StanfordMrn();
    int onezzz =   1000; // basis for 5 digit bogus MRN, starting with 1
    int highbit = 10000;
    Random numGen = new Random();
    SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat dateTimeFmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:SS");
    int encounterEID = (1+numGen.nextInt(10000))*100; // probably will be unique ranges for our DB

    PersonMaker(String site) {
      if (site.contentEquals("pedpain"))
        siteId = 6;
      else
        siteId = 1;
    }

    /* private String getSurvey() {
      if (siteId == 6)
        return "InitialPRePChild.0416";

      return "Initial.1116";
    } /* */

    private String makeMrn() {
      String num = "" + (highbit + numGen.nextInt(8*onezzz) + onezzz) + "-0";
      if (mrn.isValid(num))
        return num;

      String msg = mrn.getInvalidMessage();
      int ix = 1 + msg.lastIndexOf(' ');
      while (msg.charAt(ix) == '0')
        ix++;
      return msg.substring(ix);
    }

    private String nameGen() {  // first,last
      int len = numGen.nextInt(4) + 5;
      String s = "" + (char)('A'+numGen.nextInt(25));
      while (--len > 0)
        s += (char)('a'+numGen.nextInt(25));
      return s;
    }

    private String dobGen() {
      long fiveyears = 365 * 24 * 60 * 60000L;
      long now = System.currentTimeMillis() - fiveyears;
      long when = Math.floorMod(numGen.nextLong(), now);
      Date d = new Date(when);
      return dateFmt.format(d);
    }

    int whenAppt = 3;  // in 45 min
    private String apptDateGen() {
      long fifteenMin = 15 * 60000L;
      long now = System.currentTimeMillis();
      now -= Math.floorMod(now, fifteenMin);  // truncate to even time
      long when = now + (whenAppt++ * fifteenMin);
      Date d = new Date(when);
      return dateTimeFmt.format(d);
    }

    String encId() {
      return String.format("\"%03d,%03d,%03d,%03d\"",
          Integer.valueOf(randomInt(100, 999)),
          Integer.valueOf(randomInt(100, 999)),
          Integer.valueOf(randomInt(100, 999)),
          Integer.valueOf(randomInt(100, 999)));
    }

    String apptSt() {
      return "1"; /*
      return String.format("%02d.%02d",
          Integer.valueOf(randomInt(10, 99)),
          Integer.valueOf(randomInt(10, 99))); /* */
    }

    String genGen() {
      return numGen.nextBoolean() ? "Male" : "Female";
    }

    public int randomInt(int a, int b) {
      return numGen.nextInt(1+b-a) + a;
    }

    String pFirst, pLast, pDob, pId, pGen;
    String makePatientCsvLine(String siteUrlFirstName) {
      pFirst = siteUrlFirstName;
      pLast = nameGen();
      pDob = dobGen();
      pId = makeMrn();
      pGen = genGen();
      char comma = ',';
      StringBuilder sb = new StringBuilder(100);
      sb            .append(pId)       // ID, col 0
      .append(comma).append(pFirst)    // first name, col 1
      .append(comma).append(pLast)     // last name, col 2
      .append(comma).append(pDob)      // dob, col 3
      .append(comma).append(pGen)      // sex, col 4
      .append('\n');
      return sb.toString();
    }

    String mkEmail() {
      return pLast + "@gmail3434.com";
    }

    void makeAppt(Writer awriter) throws IOException {
      char comma = ',';
      if (randomInt(0,2) == 0) // 1/3 of the time, no appt
        return;

      String apptDate = apptDateGen();
      int ix = apptDate.indexOf(' ');
      if (ix < 1) {
        System.out.println("date is "+apptDate);
      }
      String apptTime = apptDate.substring(ix+1);
      apptDate = apptDate.substring(0, ix);

      StringBuilder sb = new StringBuilder(100);
      sb.append(pId)                   // col 0/1
      .append(comma).append(pFirst)    // 2 first name
      .append(comma).append(pLast)     // 3 last name
      .append(comma).append(pId)       // 4 patient ID / MRN
      .append(comma).append("Addr1")        // 5
      .append(comma).append("Addr2")        // 6
      .append(comma).append("PaloAlto")     // 7
      .append(comma).append("94036")        // 8
      .append(comma).append("800-111-2222") // 9
      .append(comma).append("800-111-2222") // 10
      .append(comma).append(mkEmail())  // 11 email
      .append(comma).append(this.pDob)  // 12 date of birth

      .append(comma).append(apptDate)   // 13 appt date
      .append(comma).append(apptTime)   // 14 appt time
      .append(comma).append("")         // 15 Lang1
      .append(comma).append("")         // 16 Lang2
      .append(comma).append("")         // 17 ApptType - ignored in appt2, max of 20 chars
      .append(comma).append("NPV60")             // 18 VisitTypeId
      .append(comma).append(""+(encounterEID++)) // 19 EncounterId
      .append(comma).append("")         // 20 ENC_PROV_NAME
      .append(comma).append("")         // 21 ProviderId
      .append(comma).append("")         // 22 Dept
      .append(comma).append("1")        // 23 Dept ID
      .append(comma).append(pGen)       // 24 Gender
      .append(comma).append("")         // 25 Ethnicity
      .append(comma).append("")         // 26 Race
      .append(comma).append(encId())    // 27 Enc Type C
      .append(comma).append(apptSt())   // 28 Appt Status
      .append(comma).append("")         // 29 Appt Cancel Date
      .append(comma).append("")         // 30 APPT_SERIAL_NO
      .append(comma).append("")         // 31 APPT_BLOCK_C
      .append(comma).append("")         // 32 CANCEL_REASON_CMT
      .append('\n');
      awriter.write(sb.toString());
    }

    public void create(String siteUrl, Writer pwriter, Writer awriter) throws IOException {
      pwriter.write(makePatientCsvLine(siteUrl));
      makeAppt(awriter);
    }
  }

  File mkSubDir(File dir, String subdir) {
    File subDir = new File(dir, subdir);
    if (subDir.exists())
      return subDir;

    subDir.mkdirs();
    Assert.assertTrue("Could not make sub dir "+subdir+" in "+subDir.getAbsolutePath(), subDir.exists());
    return subDir;
  }

  public void createRealImportFiles(File siteDir, String site) {
    mkSubDir(siteDir, "Patient"); // file(s) are read from here
    mkSubDir(siteDir, "Appointment"); // file(s) are read from here

    File patCsv, apptCsv;
    if (putThemInImportDirs) {
      File subDir = new File(siteDir, "Patient");
      patCsv  = new File(subDir, "Patient.csv");
      subDir = new File(siteDir, "Appointment");
      apptCsv = new File(subDir, "Appointment.csv");
    } else { // put them above import dir, ready to be copied in
      String name = useSameNamesAboveImportDirs ? "Patient.csv" : "pat.csv";
      patCsv  = new File(siteDir, name);
      name = useSameNamesAboveImportDirs ? "Appointment.csv" : "appt.csv";
      apptCsv = new File(siteDir, name);
    }
    PersonMaker personMaker = new PersonMaker(site);
    try {
      Writer pwriter = new OutputStreamWriter(new FileOutputStream(patCsv), StandardCharsets.UTF_8);
      Writer awriter = new OutputStreamWriter(new FileOutputStream(apptCsv), StandardCharsets.UTF_8);
      int num = personMaker.randomInt(minNumAppts, maxNumAppts);
      String siteFirstName = site.equals("1") ? "One" : (site.substring(0, 1).toUpperCase() + site.substring(1));
      for (int i = 0; i <= num;  i++)
        personMaker.create(siteFirstName, pwriter, awriter);
      System.out.println("  Wrote: "+num+" people + appts for site "+siteFirstName);
      pwriter.close();
      awriter.close();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.assertTrue("ERROR " + e.getMessage(), false);
    }
    Assert.assertTrue("should exist: "+patCsv.getAbsolutePath(), patCsv.exists());
    System.out.println("  Wrote: "+patCsv.getAbsolutePath());
    System.out.println("  Wrote: "+apptCsv.getAbsolutePath());
  }

  // ========= statics

  static void p(String s) {
    System.err.println(s);
  }

  static void usage(String msg) {
    p("");
    if (msg != null)
      p("  ERROR: "+msg+"\n");
    p("  USAGE: java ... [ -r ] [ -dPath ] [ subdirs ]");
    p("    -r: puts them in the import dirs, so they're ready to import");
    p("        otherwise they're put 1 dir higher, in path/subdir, instead of e.g. path/subdir/Patient");
    p("    -dPath: sets the path string, instead of /var/tmp");
    p("    subdirs: default is 1/pedpain");
    p("");
    System.exit(1);
  }

  static void processArgs(String args[]) {
    for (int i = 0;  i < args.length;  i++) {
      String s = args[i];
      if ("-h".equals(s)) {
        usage(null);
      }
      if ("-r".equals(s)) {
        putThemInImportDirs = true;
        continue;
      } else if (s.startsWith("-l")) {
        useSameNamesAboveImportDirs = true;
      } else if (s.startsWith("-d")) {
        pathFmt = s.substring(2);
        continue;
      } else if (s.startsWith("-")) {
        usage("Unknown arg: "+s);
      } else if (i < args.length - 1) {
        usage("Path must be last, not: '"+s+"' '"+args[i+1]+" ...");
      } else
        siteList = s;
    }
  }
}
