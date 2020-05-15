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

public interface Log {
  boolean isInfoEnabled();

  boolean isDebugEnabled();

  boolean isTraceEnabled();

  void fatal(Object message, Throwable t);

  void fatal(Object message);

  void error(Object message, Throwable t);

  void error(Object message);

  void warn(Object message, Throwable t);

  void warn(Object message);

  void info(Object message, Throwable t);

  void info(Object message);

  void debug(Object message, Throwable t);

  void debug(Object message);

  void trace(Object message, Throwable t);

  void trace(Object message);
}
