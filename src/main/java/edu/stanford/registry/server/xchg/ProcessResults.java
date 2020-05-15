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

package edu.stanford.registry.server.xchg;

import java.util.ArrayList;

public class ProcessResults {
  int succeeded = 0;
  int failed = 0;
  private ArrayList<ProcessError> problems = new ArrayList<>();

  public ProcessResults() {
  }

  public void incrementNumberSucceeded() {
    succeeded++;
  }

  public void incrementNumberFailed() {
    failed++;
  }

  public int getNumberSucceeded() {
    return succeeded;
  }

  public int getNumberFailed() {
    return failed;
  }

  public ArrayList<ProcessError> getErrors() {
    return problems;
  }

  public void addError(int lineNumber, String error) {
    problems.add(new ProcessError(lineNumber, error));
  }

  public void addError(ProcessError error) {
    problems.add(error);
  }
}
