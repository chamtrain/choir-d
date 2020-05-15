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

package edu.stanford.registry.server;

import edu.stanford.registry.shared.Log;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class Log4jLog extends Logger implements Log {
  private static final LogFactory logFactory = new LogFactory();

  public Log4jLog(String name) {
    super(name);
  }

  public static Log get(Class<?> category) {
    return (Log) org.apache.log4j.Logger.getLogger(category.getName(), logFactory);
  }

  private static final class LogFactory implements LoggerFactory {
    @Override
    public Logger makeNewLoggerInstance(String clazz) {
      return new Log4jLog(clazz);
    }
  }
}
