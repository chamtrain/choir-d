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

import com.google.gwt.user.client.rpc.IsSerializable;

public class SurveySystem extends DataTableBase implements IsSerializable, DataTable {

  private static final long serialVersionUID = 1590231420782820901L;
  private Integer surveySystemId;
  private String surveySystemName;

  public static String[] HEADERS = { "Survey System Id", "Survey System Name", "MetaData Version", "Date Created",
  "Date Changed" };


  public int[] CHANGE_INDICATORS = { 0, 1, 1, 0, 0 };

  public SurveySystem() {
  }
  public Integer getSurveySystemId() {
    return surveySystemId;
  }

  public void setSurveySystemId(Integer id) {
    surveySystemId = id;
  }

  public String getSurveySystemName() {
    return surveySystemName;
  }

  public void setSurveySystemName(String name) {
    surveySystemName = name;
  }

  public void copyFrom(final SurveySystem source) {
    this.setSurveySystemId(source.getSurveySystemId());
    this.setSurveySystemName(source.getSurveySystemName());
    this.setDtChanged(source.getDtChanged());
    this.setDtCreated(source.getDtCreated());
    this.setMetaVersion(source.getMetaVersion());
  }

  @Override
  public String[] getAllHeaders() {
    return HEADERS;
  }

  @Override
  public int[] getChangeIndicators() {
    return CHANGE_INDICATORS;
  }

  @Override
  public String[] getData(DateUtilsIntf utils) {
    String data[] = new String[6];
    data[0] = getSurveySystemId().toString();
    data[1] = getSurveySystemName();
    data[2] = getMetaVersion().toString();
    data[3] = utils.getDateString(getDtCreated());
    if (getDtChanged() == null) {
      data[4] = "";
    } else {
      data[4] = utils.getDateString(getDtChanged());
    }

    return data;
  }

  /**
   * setData will set the local values for the elements that can be changed and
   * will set the dtChanged value to now.
   */
  @Override
  public void setData(String data[]) throws InvalidDataElementException {
    if (data == null || data.length != 5) {
      throw new InvalidDataElementException("Invalid number of data elements ");
    }
    if (data[0] == null || data[1] == null || data[2] == null) {
      throw new InvalidDataElementException("Invalid null data value");
    }
    try {
      setSurveySystemId(Integer.valueOf(data[0]));
      setSurveySystemName(data[1]);
      setMetaVersion(Integer.valueOf(data[3]));
      setDtChanged(getNow());
    } catch (Exception e) {
      throw new InvalidDataElementException(e.getMessage(), e);
    }
  }
}
