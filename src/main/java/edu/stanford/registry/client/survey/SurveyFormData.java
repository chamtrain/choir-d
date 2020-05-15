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

package edu.stanford.registry.client.survey;

import java.util.HashMap;

public class SurveyFormData extends edu.stanford.registry.client.FormData {
  HashMap<String, Object> attributes = new HashMap<>();

  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  public boolean hasAttribute(String key) {
    return attributes.containsKey(key);
  }
}
