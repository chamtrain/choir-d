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

package edu.stanford.registry.server.xchg.data;

import edu.stanford.registry.server.ServerUtils;
import edu.stanford.registry.server.xchg.QualifierIntf;
import edu.stanford.registry.shared.DataTable;
import edu.stanford.registry.shared.PatientAttribute;

public class AttributeQualifier implements QualifierIntf<PatientAttribute> {

  private static final String NAME = "AttributeName";
  private static final String VALUE = "AttributeValue";
  private String qualifyingString;
  private String[] qualifiers;

  private boolean qualifyByName = false;
  private boolean qualifyByValue = false;

  private String attributeName;
  private String attributeValue;

  public AttributeQualifier(String qualifyingString) {
    // Get the list of parameters passed in
    this.qualifyingString = qualifyingString;
    if (this.qualifyingString == null || this.qualifyingString.trim().length() < 1) {
      return;
    }

    this.qualifiers = ServerUtils.getTokens(qualifyingString, ",");
    for (String qualifier : qualifiers) {
      String identifier = null, qualifyingValue = null;

      if (qualifier.contains("=")) {
        String[] parsedQualifier = ServerUtils.getTokens(qualifier, "=");
        if (parsedQualifier != null && parsedQualifier.length > 0) {
          identifier = parsedQualifier[0];
        }
        if (parsedQualifier.length > 1) {
          qualifyingValue = parsedQualifier[1];
        }
      }
      if (NAME.equals(identifier)) {
        qualifyByName = true;
        attributeName = qualifyingValue;
      }
      if (VALUE.equals(identifier)) {
        qualifyByValue = true;
        attributeValue = qualifyingValue;
      }

    }
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
  public boolean qualifies(DataTable dataTableObject) {
    PatientAttribute patAttribute = (PatientAttribute) dataTableObject;
    boolean qualifies = true;

    // If matching by name
    if (qualifyByName && attributeName != null) {
      if (!attributeName.equals(patAttribute.getDataName())) { // must match
        qualifies = false;
      }
    }

    // If matching by value
    if (qualifyByValue) {
      if (attributeValue == null) {
        if (patAttribute.getDataValue() != null) {
          qualifies = false;
        }
      }
    } else {
      if (attributeValue.equals("*")) { // Only if not null
        if (patAttribute.getDataValue() == null) {
          qualifies = false;
        }
      } else { // Only if matches the value provided
        if (!attributeValue.equals(patAttribute.getDataValue())) {
          qualifies = false;
        }
      }
    }
    return qualifies;
  }

  @Override
  public boolean qualifies(String string) {
    // TODO implement me!
    return false;
  }

}
