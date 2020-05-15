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

package edu.stanford.registry.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Study extends DataTableBase implements IsSerializable, DataTable {
  private static final long serialVersionUID = 1L;

  private Integer surveySystemId;
  private Integer studyCode;
  private String studyDescription;
  private String title;
  private String explanation;
  private Integer replacedByCode;

  public static final String[] HEADERS = { "Survey System Id", "Study Code", "Description", "MetaData Version",
      "Date Created", "Date Changed", "Title", "Explanation" };
  public static final int[] CHANGE_INDICATORS = { 1, 0, 1, 1, 0, 0, 1, 1 };

  public Study() {

  }

  public Study(Integer surveySystemId, Integer studyCode, String description, Integer version) {
    setSurveySystemId(surveySystemId);
    setStudyCode(studyCode);
    setStudyDescription(description);
    setMetaVersion(version);
    setTitle("");
    setExplanation("");
  }

  public Study(Integer surveySystemId, Integer studyCode, String description, Integer version, Date dtCreated,
               Date dtChanged) {
    this(surveySystemId, studyCode, description, version);
    setDtCreated(dtCreated);
    setDtChanged(dtChanged);
  }

  public Integer getSurveySystemId() {
    return surveySystemId;
  }

  public void setSurveySystemId(Integer id) {
    surveySystemId = id;
  }

  public Integer getStudyCode() {
    return studyCode;
  }

  public void setStudyCode(Integer code) {
    studyCode = code;
  }

  public String getStudyDescription() {
    return studyDescription;
  }

  public void setStudyDescription(String sdesc) {
    studyDescription = sdesc;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String stitle) {
    title = stitle;
  }

  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explain) {
    explanation = explain;
  }

  public Integer getReplacedByCode() { return replacedByCode; }

  public void setReplacedByCode(Integer studyCode) {
    replacedByCode = studyCode;
  }
  /**
   * Get the display column headers.
   */
  @Override
  public String[] getAllHeaders() {
    return HEADERS;
  }

  /**
   * Returns an int array indicating which data elements can be modified. 0 = no, 1 = yes.
   *
   * @return int array
   */
  @Override
  public int[] getChangeIndicators() {
    return CHANGE_INDICATORS;
  }

  @Override
  public String[] getData(DateUtilsIntf utils) {
    String data[] = new String[9];
    data[0] = getSurveySystemId().toString();
    data[1] = getStudyCode().toString();
    data[2] = getStudyDescription();
    data[3] = getMetaVersion().toString();
    if (getDtCreated() == null) {
      data[4] = "";
      // this has no logger, else would warn...
    } else {
      data[4] = utils.getDateString(getDtCreated());
    }
    if (getDtChanged() == null) {
      data[5] = "";
    } else {
      data[5] = utils.getDateString(getDtChanged());
    }
    if (getTitle() == null) {
      data[6] = "";
    } else {
      data[6] = getTitle();
    }
    if (getExplanation() == null) {
      data[7] = "";
    } else {
      data[7] = getExplanation();
    }
    if (getReplacedByCode() == null) {
      data[8] = "";
    } else {
      data[8] = getReplacedByCode().toString();
    }
    return data;
  }

  /**
   * setData will set the local values for the elements that can be changed and will set the dtChanged value to now.
   */
  @Override
  public void setData(String data[]) throws InvalidDataElementException {
    if (data == null || data.length != 9) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }
    if (data[0] == null || data[1] == null || data[2] == null || data[3] == null) {
      throw new InvalidDataElementException("Invalid null data value");
    }
    try {
      setSurveySystemId(Integer.valueOf(data[0]));
      setStudyCode(Integer.valueOf(data[1]));
      setStudyDescription(data[2]);
      setMetaVersion(Integer.valueOf(data[3]));

      setDtChanged(getNow());
      setTitle(data[6]);
      setExplanation(data[7]);
      if (!data[8].isEmpty()) {
        setReplacedByCode(Integer.valueOf(data[8]));
      }
    } catch (Exception e) {
      throw new InvalidDataElementException(e.getMessage(), e);
    }
  }

}
