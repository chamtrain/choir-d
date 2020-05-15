/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.survey.server;

import java.util.function.Supplier;

import com.github.susom.database.Database;

/**
 * Implement this interface to execute logic once and only once when a survey is
 * completed.
 *
 * The implementation will be passed a SiteInfo when it's created, for the survey's site.
 */
public interface SurveyCompleteHandler {

  boolean surveyCompleted(SurveyComplete survey, Supplier<Database> database);
}
