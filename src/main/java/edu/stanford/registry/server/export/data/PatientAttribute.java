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

package edu.stanford.registry.server.export.data;

import edu.stanford.registry.server.export.CommonFormatter;
import edu.stanford.registry.server.export.ExportFormatterIntf;
import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.server.xchg.data.AttributeQualifier;

@SuppressWarnings("rawtypes")
public class PatientAttribute extends
    edu.stanford.registry.shared.PatientAttribute implements DataTableIntf {

  private static final long serialVersionUID = 9023179989722722737L;

  @Override
  public QualifierIntf getQualifier(String qualifyingString) {
    return new AttributeQualifier(qualifyingString);
  }

  @Override
  public ExportFormatterIntf getFormatter(String formatterString) {
    return new CommonFormatter();
  }

  @Override
  public String[] getChildrenDataSourceNames() {
    return null; // Patient Attributes have no children
  }

}
