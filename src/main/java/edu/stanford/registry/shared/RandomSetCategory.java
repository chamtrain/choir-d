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
package edu.stanford.registry.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Defines a category for a RandomSet for stratification. Effectively, having a category with N values
 * turns the RandomSet into N of them, with the randomization algorithm running independently in each
 * stratum. Adding another RandomSet with M values makes there be M*N strata (randomization sets within
 * the same RandomSet (of type TreatmentSet).
 *
 * <p>For instance, say a baldness clinic is doing a study on the effectiveness of a drug v.s. a placebo.
 * 40% of hair-loss sufferers are female, and the clinic expects 90% of the participants to be women.
 * To avoid the 10% who are male from accidentally being mostly in one group, they make a Category: Sex,
 * with two values, Male and Female. When assigning a patient to the TreatmentSet, the doctor must enter
 * in Male or Female, and the patient is randomly assigned to drug vs placebo for that stratum (sub-group).
 *
 * <p>Let's say most patients will be age 40 or above, but a few will be under 40. So they also add an
 * Age category with 2 sub-groups, 40+ and under-40.  So the doctor must enter both sex and age when
 * assigning patients to a group, and there are 4 strata: 40+ males, 40+ females, under-40 males and under-40 females.
 *
 * <p>The block-size for the randomization algorithm
 */
public class RandomSetCategory implements Serializable, IsSerializable {
  private static final long serialVersionUID = 1L;
  public  static final String NoStratumName = "all";  // If has no strata, participants will be assigned to "all"

  String name;
  String title;
  String question;
  Value[] values;

  public RandomSetCategory() { }

  public RandomSetCategory(String name, String title, String question) {
    this.name = name;
    this.title = title;
    this.question = question;
  }

  public void setValues(Value[] values) {
    this.values = values;
  }

  public Value[] getValues() {
    return values;
  }

  public String getName() {
    return name;
  }
  public String getTitle() {
    return title.isEmpty() ? name : title;
  }

  /**
   * Returns the text for the set of radio buttons in the UI
   */
  public String getQuestion() {
    return question.isEmpty() ? (getTitle()+"?") : question;
  }

  /**
   * Assumes this is the category in the name=value param. It finds the value and returns a description.
   * @return preDelimiter + thisCategory.getTitle() + between + value.getTitle()
   */
  public String getDescription(String preDelimiter, String nameEqValue, String between) {
    int ix = nameEqValue.indexOf('=');
    String valStr = nameEqValue.substring(ix+1);
    for (int i = 0;  i < values.length;  i++) {
      if (values[i].getName().equals(valStr)) {
        return preDelimiter + getTitle() + between + values[i].getTitle();
      }
    }
    throw new RuntimeException("No value for cat=value: "+nameEqValue);
  }


  public StringBuilder addValueToNameID(StringBuilder sb, Value val) {
    sb.append(sb.length() == 0 ? "" : ",");
    return sb.append(getName()).append('=').append(val.getName());
  }


  // =====================================================================================

  public static class Value implements Serializable, IsSerializable {
    private static final long serialVersionUID = 1L;

    private String name;  // short value, used stratum name, must not be null
    private String title;  // value to show in the UI, null=>value
    private String answer; // text for the UI's radio button, to set the category, null=>title

    public Value() { }

    public Value(String name, String title, String answer) {
      this.name = name;
      this.title = title;
      this.answer = answer;
    }

    public String getName() {
      return name;
    }

    public String getTitle() {
      return title.isEmpty() ? name : title;
    }

    public String getAnswer() {
      return answer.isEmpty() ? getTitle() : answer;
    }
  }
}