/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.client.api;

import java.util.Map;

public interface SurveyBuilderFormCondition {

  enum Method {
    exists {
      @Override
      public String value() {
        return String.valueOf(exists);
      }

      @Override
      public String label() {
        return "Exists";
      }
    }, notexists {
      @Override
      public String value() {
        return String.valueOf(notexists);
      }

      @Override
      public String label() {
        return "Does not exist";
      }
    }, equal {
      @Override
      public String value() {
        return String.valueOf(equal);
      }
      @Override
      public String label() {
        return "Equals";
      }
    }, notequal {
      @Override
      public String value() {
        return String.valueOf(notequal);
      }

      @Override
      public String label() {
        return "Does not equal";
      }
    }, lessthan {
      @Override
      public String value() {
        return String.valueOf(lessthan);
      }

      @Override
      public String label() {
        return "Is less than";
      }
    }, lessequal {
      @Override
      public String value() {
        return String.valueOf(lessequal);
      }

      @Override
      public String label() {
        return "Is less than or equal";
      }
    }, greaterthan {
      @Override
      public String value() {
        return String.valueOf(greaterthan);
      }

      @Override
      public String label() {
        return "Is greater than";
      }
    }, greaterequal {
      @Override
      public String value() {
        return String.valueOf(greaterequal);
      }

      @Override
      public String label() {
        return "Is greater than or equal";
      }
    }, contains {
      @Override
      public String value() {
        return String.valueOf(contains);
      }

      @Override
      public String label() {
        return "Contains";
      }
    };

    public abstract String value();

    public abstract String label();

  }

  enum Type { patientAttribute {
    @Override
    public String value() {
      return "patientAttribute";
    }
    @Override
    public String label() {
      return "Patient Attribute";
    }
  }, item {
    @Override
    public String value() {
      return "item";
    }

    @Override
    public String label() {
      return "Previous answer by Item reference";
    }
  }, response {
    @Override
    public String value() {
      return "response";
    }
    @Override
    public String label() {
      return "Previous answer by Response reference";
    }
  } ;
    public abstract String value();
    public abstract String label();
  }
  Method getMethod();

  void setMethod(Method method);

  String getDataValue();

  void setDataValue(String value);

  Type getType();

  void setType(Type typ);

  Map<String, String> getAttributes();

  void setAttributes( Map<String, String> attributes);

  SurveyBuilderFormFieldValue getValue();

  void setValue(SurveyBuilderFormFieldValue value);

}
