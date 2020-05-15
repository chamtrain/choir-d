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
package edu.stanford.registry.tool;

import edu.stanford.registry.server.randomset.RandomSetKSort;
import edu.stanford.registry.server.randomset.RandomSetPure;
import edu.stanford.registry.server.randomset.RandomSetTenSet;
import edu.stanford.registry.server.randomset.RandomSetter;
import edu.stanford.registry.shared.RandomSet;
import edu.stanford.registry.shared.RandomSetCategory;
import edu.stanford.registry.shared.RandomSetGroup;

import java.util.ArrayList;

/**
 * For testing.
 * This creates some RandomSets,
 * and prints out the java that you can add to your CreateRegData file.
 */
public class RandomSetsCreate {

  public static void main(String args[]) {
    printOne(createBackPainRandomSet());
    printOne(createMigraineRandomSet());
    printOne(createDDRandomSet());
    printOne(createHF10RandomSet());
  }

  private static void printOne(RandomSetter r) {
    System.out.println("\nJSon for "+r.getName());
    printJSon(r.toJsonString());
  }

  public static final String TSET_KSort_DD = "DDTreatmentSet";
  public static final String TSET_KSort_BackPain = "BackPainTreatmentSet";
  public static final String TSET_Pure_Migraine = "MigraineTreatmentSet";

  public static final Long site0 = 0L;
  public static final Long site1 = 1L;


  // =====================================================================================
  /**
   * First implemented real treatment set, for Stanford pain clinic.
   */
  public static RandomSetter createDDRandomSet() {
    RandomSetter toFill = new RandomSetKSort(site1, TSET_KSort_DD);
    RandomSet rset = toFill.getRandomSet();

    rset.init("DD", RandomSet.RSType.TreatmentSet,
              "Duloxetine/Desipramine for Pain", "Dr. Salmasi", 1000, null, 182);
    ArrayList<RandomSetGroup> groups = new ArrayList<RandomSetGroup>(2);
    groups.add(new RandomSetGroup(0, "Duloxetine",   DDDulDesc, 2, false, 0, false));
    groups.add(new RandomSetGroup(1, "Desipramine",  DDDesDesc, 2, false, 0, false));
    rset.initGroups(groups);

    makeCategories(rset, 1);
    setCategory(rset, 0, 2, "Fibromyalgia", "", "Does the patient meet the criteria for Fibromyalgia?");
    setCatValue(rset, 0, 0, "Yes", "Has-fibromyalgia", "Yes");
    setCatValue(rset, 0, 1, "No", "Fibromyalgia-free", "No");
    return toFill;
  }

  final static String DDDulDesc =
        "DRUG:  Duloxetine (Cymbalta) 20mg\n"
      + "*also available in 30 and 60 mg tablets\n"
      + " \n"
      + "WEEK 1 20mg by mouth each morning\n"
      + "WEEK 2 40mg by mouth each morning\n"
      + "WEEK 3 60mg by mouth each morning";

  final static String DDDesDesc =
        "DRUG:  Desipramine (Norpramin) 25mg\n"
      + "*also available in 10, 50, 75, 100 and 150 mg tablets\n"
      + "\n"
      + "WEEK 1 25mg by mouth each morning\n"
      + "WEEK 2 50mg by mouth each morning\n"
      + "WEEK 3 75mg by mouth each morning\n";


  // =====================================================================================
  /**
   * Used for tests to create a KSort treatment set. With default/xml/backpain-tset.xml
   */
  public static RandomSetter createBackPainRandomSet() {
    return createBackPainRandomSet((char)0);
  }
  /**
   * Add the character to the end of the name, and uses it to set the state.
   * @param state e=Enrolling, n=NotEnrolling, r=Researching, c=Closed
   */
  public static RandomSetter createBackPainRandomSet(char state) {
    RandomSetter toFill = new RandomSetKSort(site0, TSET_KSort_BackPain);
    RandomSet rset = toFill.getRandomSet();

    rset.init(stateName(state, "Chronic Back Pain"), RandomSet.RSType.TreatmentSet,
              "Treatments for Chronic Back Pain outpatients", "Dr Zhivago", 100, null, 365);
    ArrayList<RandomSetGroup> groups = new ArrayList<RandomSetGroup>(5);
    int i = 0;
    groups.add(new RandomSetGroup(i++, "control/stretching",   null, 2, true, 4, true));
    groups.add(new RandomSetGroup(i++, "placebo+exercise",     null, 1, true, 2, true));
    groups.add(new RandomSetGroup(i++, "acupuncture+exercise", null, 1, true, 1, true));
    groups.add(new RandomSetGroup(i++, "inversion+exercise",   null, 1, true, 1, true));
    groups.add(new RandomSetGroup(i++, "exercise",             null, 2, true, 0, true));
    rset.initGroups(groups);
    return toFill;
  }
  public static RandomSetter createHF10RandomSet() {
    RandomSetter toFill = new RandomSetTenSet(site1, "HF10vBurstDR");
    RandomSet rset = toFill.getRandomSet();

    rset.init("HF10/BurstDR", RandomSet.RSType.TreatmentSet,
        "HF10 vs. BurstDR Spinal Cord Stimulation", "Dr. Salmasi", 1000, null, 182);
    ArrayList<RandomSetGroup> groups = new ArrayList<RandomSetGroup>(2);
    groups.add(new RandomSetGroup(0, "HF10",   "HF10 Spinal Cord Stimulation using Nevro system", 2, false, 0, false));
    groups.add(new RandomSetGroup(1, "BurstDR",  "BurstDR Spinal Cord Stimulation using Abbott system", 2, false, 0, false));
    rset.initGroups(groups);
    return toFill;
  }


  // =====================================================================================

  /**
   * Used for tests to create a Pure treatment set.
   */
  public static RandomSetter createMigraineRandomSet() {
    return createMigraineRandomSet((char)0, true);
  }

  /**
   * Used for tests to create a Pure treatment set.
   */
  public static RandomSetter createMigraineRandomSetKSort() {
    return createMigraineRandomSet((char)0, false);
  }

  public static RandomSetter createMigraineRandomSet(char state, boolean isPure) {
    RandomSetter toFill =
        isPure ? new RandomSetPure(site0, TSET_Pure_Migraine) : new RandomSetKSort(site0, TSET_Pure_Migraine);
    RandomSet rset = toFill.getRandomSet();
    rset.init(stateName(state, "MigraineTreatmentSet"), RandomSet.RSType.TreatmentSet,
              "description", "Dr Who", 10, null, 100);
    ArrayList<RandomSetGroup> groups = new ArrayList<RandomSetGroup>(3);
    int i = 0;
    groups.add(new RandomSetGroup(i++, "placebo",    null, 1, true, 2, true));
    groups.add(new RandomSetGroup(i++, "exercise",   null, 3, true, 1, true));
    groups.add(new RandomSetGroup(i++, "meditation", null, 3, true, 0, true));
    rset.initGroups(groups);

    makeCategories(rset, 3);
    int cati = -1;
    setCategory(rset, ++cati, 2, "FavColor", "", "Which color does the patient prefer");
    setCatValue(rset, cati, 0, "Green",    "", "Prefers green");
    setCatValue(rset, cati, 1, "NotGreen", "", "Prefers some other color");

    setCategory(rset, ++cati, 3, "SexId", "Sexual Identity", "");
    setCatValue(rset, cati, 0, "Male",   "", "");
    setCatValue(rset, cati, 1, "Female", "", "");
    setCatValue(rset, cati, 2, "Other",  "Neither or both", "");

    setCategory(rset, ++cati, 2, "Age", "", "How mature is the patient?");
    setCatValue(rset, cati, 0, "Childish",   "", "Patient seems to act age 15 or younger");
    setCatValue(rset, cati, 1, "Adolt",      "Adult", "");

    return toFill;
  }


  // ========= Utilities =================================================================

  static private String stateName(char c, String name) {
    return (c == '0' || c == 0) ? name : (name+c);
  }


  /**
   * Makes an array for the NC categories, plus all the arrays for the NV1, NV2... values in each.
   * @param rset The randomset
   * @param numCategories The number of categories this random set has
   */
  static void makeCategories(RandomSet rset, int numCategories) {
    if (numCategories < 1) {
      throw new RuntimeException("If no categories, don't call this, "
          + "else give the number of values for each category, at least 1 value, not "+numCategories);
    }
    rset.setCategories(new RandomSetCategory[numCategories]);
  }

  static void setCategory(RandomSet rset, int catIndex, int numValues, String catName, String catTitle, String catQuestion) {
    ensureNoBadChars(rset.getName(), "Category", catName);
    rset.getCategories()[catIndex] = new RandomSetCategory(catName, catTitle, catQuestion);
    rset.getCategories()[catIndex].setValues(new RandomSetCategory.Value[numValues]);
  }

  static void setCatValue(RandomSet rset, int catIndex, int valIndex, String vname, String vtitle, String vanswer) {
    ensureNoBadChars(rset.getName(), "Category.Value", vname);
    RandomSetCategory cat = rset.getCategories()[catIndex];
    cat.getValues()[valIndex] = new RandomSetCategory.Value(vname, vtitle, vanswer);
  }

  private static void ensureNoBadChars(String rsetName, String which, String name) {
    final String badChars = "=,<";
    for (char c: badChars.toCharArray()) {
      if (name.indexOf(c) > -1) {
        throw new RuntimeException("Creating RandomCategory "+rsetName+", no chars in '"+badChars+
                                   "' are allowed in "+which+" name: "+name);
      }
    }
  }

  private static void printJSon(String json) {
    System.out.println("First, to paste into java ("+json.length()+" chars) :");
    String s = json.replaceAll("\"", "|"); // so can easily output as a string
    int lastx = 0;
    int ix = s.indexOf('{', 1);
    String quote = "\"";
    String plus = "(" + quote;
    System.out.println("  String json = ");
    while (ix >= 0) {
      System.out.println("    " + plus + s.substring(lastx, ix) + quote);
      plus = (lastx == 0) ? "+ \"" : plus;
      lastx = ix;
      ix = s.indexOf('{', lastx+1);
    }
    String repAll = ").replaceAll('\\\\|','\\'');".replaceAll("'", "\"");
    System.out.println("    " + plus + s.substring(lastx) + quote + repAll);
    System.out.println("Next, to paste into sql:\n'"+json+"'");
  }
}
