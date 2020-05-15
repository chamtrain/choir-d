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
package edu.stanford.registry.client.api;

import edu.stanford.survey.client.api.BodyMapAnswer;
import edu.stanford.survey.client.api.BodyMapQuestion;
import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.NumericAnswer;
import edu.stanford.survey.client.api.RadiosetAnswer;
import edu.stanford.survey.client.api.RadiosetQuestion;
import edu.stanford.survey.client.api.SliderQuestion;
import edu.stanford.survey.client.api.TextInputAnswer;
import edu.stanford.survey.client.api.TextInputQuestion;


/**
 * API Survey Step definition
 *
 * @author tpacht
 */
public interface SurveyStudyStepObj {

  FormQuestion getQuestion();
  void setQuestion(FormQuestion question);

  FormAnswer getAnswer();
  void setAnswer(FormAnswer answer);

  BodyMapQuestion getBodyMapQuestion();
  void setBodyMapQuestion(BodyMapQuestion question);

  BodyMapAnswer getBodyMapAnswer();
  void setBodyMapAnswer(BodyMapAnswer answer);

  RadiosetQuestion getRadiosetQuestion();
  void setRadiosetQuestion(RadiosetQuestion question);

  RadiosetAnswer getRadiosetAnswer();
  void setRadioSetAnswer(RadiosetAnswer answer);

  SliderQuestion getSliderQuestion();
  void setSliderQuestion(SliderQuestion question);

  NumericAnswer getNumericAnswer();
  void setNumericAnswer(NumericAnswer answer);

  TextInputQuestion getTextInputQuestion();
  void setTextInputQuestion(TextInputQuestion TextInputQuestion);

  TextInputAnswer getTextInputAnswer();
  void setTextInputAnswer(TextInputAnswer answer);
}
