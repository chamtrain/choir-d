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

package edu.stanford.registry.client.survey;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.base.ValueBoxBase;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;

public class SurveyBuilderValidatorHelper {

  public static void addValueRequiredValidator(final ValueBoxBase<String> widget) {
    widget.addValidator(new Validator<String>() {
      @Override
      public int getPriority() {
        return 0;
      }

      @Override
      public List<EditorError> validate(Editor<String> editor, String value) {
        List<EditorError> result = new ArrayList<EditorError>();
        String valueStr = value == null ? "" : value.toString().trim();
        if (valueStr.length() < 1) {
          result.add(new BasicEditorError(widget, value, "missing"));
        }
        return result;
      }
    });
  }
}
