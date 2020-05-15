/*
 * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.tool;

import edu.stanford.survey.server.promis.PromisItemBank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility to convert the Assessment Center JSON representations of the form (item bank)
 * and calibrations into Java code. The forms are available at:
 *
 * http://www.assessmentcenter.net/ac_api/2012-01/Forms/{OID}.json
 *
 * and the calibrations are available at:
 *
 * http://www.assessmentcenter.net/ac_api/2012-01/Calibrations/{OID}.json
 */
public class GenerateItemBankFromPromis {
  private static final Map<String, String> officialNameToJavaName = new HashMap<>();
  private static final Map<String, String> addingOfficialNameToJavaName = new HashMap<>();
  private static final Set<String> javaNameFailed = new HashSet<>();

  public static void main(String[] args) throws IOException {
    // Current set for adult pain clinic
    // Be careful generating these because the current code in the GitHub master are
    // frozen and have in certain cases been hand adjusted, so they may not match the
    // output of this tool
//    gen("Anger D2FA612D-C290-4B88-957D-1C27F48EE58C.json", "PromisAnger");
//    gen("Anxiety FFCDF6E3-8B17-4673-AB38-C677FFF6DBAF.json", "PromisAnxiety");
//    gen("Depression 96FE494D-F176-4EFB-A473-2AB406610626.json", "PromisDepression");
//    gen("Fatigue B3B30AF1-7536-451D-AD39-62A097A5EA4D.json", "PromisFatigue");
//    gen("Pain Behavior C4ADCFAB-6B75-498E-9E94-AFD3BA211DC4.json", "PromisPainBehavior");
//    gen("Pain Intensity 8F048079-0A22-46DA-9711-07687D492259.json", "PromisPainInterference");
//    gen("Physical Function 98DB589F-04D5-4529-9E4D-0D77F022C2CC.json", "PromisPhysicalFunction");
//    gen("Sleep Disturbance F625E32A-B26D-4228-BBC2-DD6C2E4EEA6E.json", "PromisSleepDisturbance");
//    gen("Sleep-Related Impairment E038718E-F556-4D0D-9B00-BD178DE6A2C8.json", "PromisSleepRelatedImpairment");

    // New set for adult pain clinic
//    gen("Ability to Participate Social 5211C7AF-8F4A-4648-90A6-ECE5669AE0EF.json", "PromisAbilityToParticipateSocial");
//    gen("Emotional Support 29654543-C99B-4CE9-884B-3D401B4AD857.json", "PromisEmotionalSupport");
//    gen("Instrumental Support 48AA3E5F-CDE6-4FAE-819A-D65992D539C6.json", "PromisInstrumentalSupport");
//    gen("Satisfaction Roles Activities 612ADAF7-0C0E-4D16-9236-C8006F84759A.json", "PromisSatisfactionRolesActivities");
//    gen("Social Isolation 8D79D960-EC12-4242-AE66-03EF8EF4D61D.json", "PromisSocialIsolation");

    // New set for pediatric pain clinic
    // TBD parent proxy Anxiety, Depression, and Pain Interference (only have Excel files for these)
//    gen("Parent Proxy-Fatigue 73CDF7AE-E1C2-4ADF-B9B3-335885F5BAC9.json", "PromisParentProxyFatigue");
//    gen("Parent Proxy-Mobility 7ADF7E22-C6E5-4B2A-A441-3300EE5B5C8B.json", "PromisParentProxyMobility");
//    gen("Parent Proxy-Peer Relations 1DACF311-2A84-4CF0-BDCC-9166811E2266.json", "PromisParentProxyPeerRelations");
//    gen("Ped-Anxiety BEBC8474-D244-4A38-87DE-00A640DE03DE.json", "PromisPediatricAnxiety");
//    gen("Ped-Depressive Sx 896BB80C-0540-41C0-8A2B-69DCEDC7B6B9.json", "PromisPediatricDepressiveSx");
//    gen("Ped-Fatigue C0E737C9-EF7C-4842-8661-4B00F82C0FED.json", "PromisPediatricFatigue");
//    gen("Ped-Mobility 8A02EC35-DC30-455F-8783-B5F28206A4A7.json", "PromisPediatricMobility");
//    gen("Ped-Pain Interference AA295C4F-2E21-414D-AC90-8D6A3AE1F39F.json", "PromisPediatricPainInterference");
//    gen("Ped-Peer Relations 9DA933EC-4E3B-4409-8785-9D692010B7A2.json", "PromisPediatricPeerRelations");

    // To turn Bank.java into a set of (officialName,javaName) pairs:
    // echo '{"banks":[' > banks.json
    // grep 'return' Bank.java | grep -v officialNameToBank | sed 's/.*return \([^"][^.]*\).*/{"java":"\1","name":/' | sed 's/.*return \([^;]*\).*/\1},/' >> banks.json
    // sed -i '' '$ s/.$/]}/' banks.json
    String banksJson = FileUtils.readFileToString(new File("src/main/java/edu/stanford/survey/server/promis/banks.json"));
    JSONArray banks = new JSONObject(banksJson).getJSONArray("banks");
    for (int i = 0; i < banks.length(); i++) {
      JSONObject bank = banks.getJSONObject(i);
      officialNameToJavaName.put(bank.getString("name"), bank.getString("java"));
    }

    String formsJson = FileUtils.readFileToString(new File("src/test/java/edu/stanford/registry/tool/promis/forms.json"));
    JSONArray forms = new JSONObject(formsJson).getJSONArray("Form");
    for (int i = 0; i < forms.length(); i++) {
      JSONObject form = forms.getJSONObject(i);
      gen(form.getString("OID"), toJavaName(form.getString("Name")));
//      System.out.println(form.getString("OID") + ": " + form.getString("Name"));
//      System.out.println(form.getString("OID") + ": " + toJavaName(form.getString("Name")));
    }

    System.out.println("Add these into Bank.java:\n");
    for (String officialName : addingOfficialNameToJavaName.keySet()) {
      String javaName = addingOfficialNameToJavaName.get(officialName);
      if (javaNameFailed.contains(javaName)) {
        continue;
      }
      System.out.println(String.format("  %1$s {\n"
          + "    @Override\n"
          + "    public ItemBank bank() {\n"
          + "      return %2$s.bank();\n"
          + "    }\n"
          + "\n"
          + "    @Override\n"
          + "    public String officialName() {\n"
          + "      return \"%3$s\";\n"
          + "    }\n"
          + "  },", javaName.substring(0, 1).toLowerCase() + javaName.substring(1), javaName, officialName));
    }
//    gen("8F64582D-C19C-40E9-B470-4643D78AAE90", "PromisAngerNew");

    // Other item banks we are not using yet
//    gen("Alcohol Negative Consequences A3A571A6-1E62-4768-B8C1-AB99CDBF867F.json", "PromisAlcoholNegativeConsequences");
//    gen("Alcohol Negative Expectancies E9F0500B-6305-4924-A3B5-A7B3357AF15D.json", "PromisAlcoholNegativeExpectancies");
//    gen("Alcohol Positive Consequences F6C7CFF1-76B1-4CB6-8D11-8CAEF848B11A.json", "PromisAlcoholPositiveConsequences");
//    gen("Alcohol Positive Expectancies A841AF31-A0A1-4FB0-8DC6-B5AF77DE2877.json", "PromisAlcoholPositiveExpectancies");
//    gen("Alcohol Use C42EFA6F-48CE-4217-B9EF-4FE9493BE15F.json", "PromisAlcoholUse");
//    gen("Applied Cog Abilities 5FAF7608-6175-4B89-A410-1E88D157E903.json", "PromisAppliedCogAbilities");
//    gen("Applied Cog General Concerns 9D5DC1F6-9993-4EF3-87AB-73550836D263.json", "PromisAppliedCogGeneralConcerns");
//    gen("Informational Support 9A2F920A-F470-4F3A-B82E-EC9D19F38F8D.json", "PromisInformationalSupport");
//    gen("Parent Proxy-Upper Extremity 8C3AA645-DC1D-4762-BCD2-E2F8DAC0B0EB.json", "PromisParentProxyUpperExtremity");
//    gen("Ped-Asthma F67A03AA-3318-48DE-A61D-4A17D4AA2ABC.json", "PromisPediatricAsthma");
//    gen("Ped-Upper Extremity 3F2FCBBA-E8A1-4D0B-A9AE-9AEABF5BF60B.json", "PromisPediatricUpperExtremity");
//    gen("Social Sat DSA FEE4576A-D94F-4E85-8DAB-5A3181BB14CB.json", "PromisSocialSatDSA");
//    gen("Social Sat Role 36F00430-CC4C-4977-AE8A-0787B3C53AB8.json", "PromisSocialSatRole");
  }

  private static String toJavaName(String name) {
    if (officialNameToJavaName.containsKey(name)) {
      return officialNameToJavaName.get(name);
    }

    StringBuilder result = new StringBuilder();

    for (String part : name.split(" ")) {
      switch (part) {
      case "PROMIS": part = "Promis"; break;
      case "PROMIS-Ca": part = "PromisCa"; break;
      case "Neuro-QOL": part = "NeuroQol"; break;
      case "-": part = ""; break;
      case "SF": part = "ShortForm"; break;
      case "v1.0": part = "OneZero"; break;
      case "V10": part = "OneZero"; break;
      case "v1.1": part = "OneOne"; break;
      case "v1.2": part = "OneTwo"; break;
      case "v2.0": part = "TwoZero"; break;
      }
      if (part.length() > 0 && Character.isLowerCase(part.charAt(0))) {
        if (part.length() > 1) {
          part = Character.toString(Character.toUpperCase(part.charAt(0))) + part.substring(1);
        } else {
          part = Character.toString(Character.toUpperCase(part.charAt(0)));
        }
      }
      result.append(part.replaceAll("[^\\p{Alnum}]", ""));
    }

    String javaName = result.toString();

    addingOfficialNameToJavaName.put(name, javaName);

    return javaName;
  }

  private static void gen(String oid, String javaFile) {
    try {
      System.out.println("Trying to create " + javaFile + ".java");
      String directory = "src/test/java/edu/stanford/registry/tool/promis/";
      String javaDirectory = "src/main/java/edu/stanford/survey/server/promis/";

      String bankFile = "forms/" + oid + ".json";
      String calibrationFile = "calibrations/" + oid + ".json";
      String calibrationJson = FileUtils.readFileToString(new File(directory, calibrationFile));
      String bankJson = FileUtils.readFileToString(new File(directory, bankFile));

      PromisItemBank bank = new PromisItemBank(bankJson, calibrationJson);

      writeSource(javaDirectory, javaFile, bank, oid);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Failed to create " + javaFile + ".java");
      javaNameFailed.add(javaFile);
    }
  }

//  private static void gen(String bankFile, String javaFile) {
//    try {
//      System.out.println("Trying to create " + javaFile + ".java");
//      String directory = "test/edu/stanford/registry/tool/promis/";
//      String javaDirectory = "src/edu/stanford/survey/server/promis/";
//
//      String calibrationFile = bankFile.substring(0, bankFile.length() - 42) + " Calibration"
//          + bankFile.substring(bankFile.length() - 42);
//      String calibrationJson = FileUtils.readFileToString(new File(directory, calibrationFile));
//
//      String bankJson = FileUtils.readFileToString(new File(directory, bankFile));
//
//      PromisItemBank bank = new PromisItemBank(bankJson, calibrationJson);
//
//      writeSource(javaDirectory, javaFile, bank, bankFile);
//    } catch (Exception e) {
//      e.printStackTrace();
//      System.err.println("Failed to create " + javaFile + ".java");
//    }
//  }

  private static void writeSource(String javaDirectory, String javaFile, PromisItemBank bank, String oid) {
    try {
      File srcFile = new File(javaDirectory, javaFile + ".java");
      if (!srcFile.exists()) {
        if (!srcFile.createNewFile()) {
          throw new Exception("Unable to create file: " + srcFile.getCanonicalPath());
        }
      }

      String separator = System.getProperty("line.separator");

      Writer output = new BufferedWriter(new FileWriter(srcFile));
      output.write("/*\n"
          + " * Copyright 2018 The Board of Trustees of The Leland Stanford Junior University.\n"
          + " * All Rights Reserved.\n"
          + " *\n"
          + " * See the NOTICE and LICENSE files distributed with this work for information\n"
          + " * regarding copyright ownership and licensing. You may not use this file except\n"
          + " * in compliance with a written license agreement with Stanford University.\n"
          + " *\n"
          + " * Unless required by applicable law or agreed to in writing, software\n"
          + " * distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT\n"
          + " * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your\n"
          + " * License for the specific language governing permissions and limitations under\n"
          + " * the License.\n"
          + " */\n"
          + "\npackage edu.stanford.survey.server.promis;");
      output.write(separator);
      output.write(separator +"import edu.stanford.survey.server.CatAlgorithm.ItemBank;");
      output.write(separator);
      output.write(separator +"import static edu.stanford.survey.server.ItemBanks.*;");
      output.write(separator);
      output.write(separator +"/**");
      output.write(separator +" * Item bank for PROMIS assessment. Generated from OID " + oid + ".");
      output.write(separator +" */");
      output.write(separator +"public class " + javaFile + " {");
      output.write(separator +"  private static final ItemBank bank = ");
      output.write(bank.toJavaString());
      output.write(separator + "  public static ItemBank bank() {");
      output.write(separator + "    return bank;");
      output.write(separator + "  }");
      output.write(separator + "}");
      output.write(separator);
      output.close();
      System.out.println(srcFile.getCanonicalPath() + " SUCCESSFULLY CREATED.");
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    } 
  }
}
