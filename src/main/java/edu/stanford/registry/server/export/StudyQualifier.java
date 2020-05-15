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

package edu.stanford.registry.server.export;

import edu.stanford.registry.server.export.data.Study;
import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.shared.DataTable;

public class StudyQualifier implements QualifierIntf<Study> {

  @SuppressWarnings("unused")
  private static final String SURVEY_SYSTEM = "SurveySystem";

  private String qualifyingString;
  private String[] qualifiers;

  public StudyQualifier(String qualifyingString) {
    // Get the list of parameters passed in
    this.qualifyingString = qualifyingString;
  }

  @Override
  public String getQualifier() {
    return qualifyingString;
  }

  @Override
  public String[] getQualifiers() {
    return qualifiers;
  }

  @Override
  public boolean qualifies(DataTable dt) {
    // if (dt != null && ( (Study) dt).
    return false;
  }

  @Override
  public boolean qualifies(String string) {
    // TODO implement me!
    return false;
  }
}
