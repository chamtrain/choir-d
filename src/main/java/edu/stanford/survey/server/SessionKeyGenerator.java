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

package edu.stanford.survey.server;

import java.security.SecureRandom;

import org.apache.log4j.Logger;

import com.github.susom.database.Metric;


/**
 * Generate session keys using the secure random number generator.
 */
public class SessionKeyGenerator {
  private static final Logger log = Logger.getLogger(SessionKeyGenerator.class);
  private final SecureRandom secureRandom;

  public SessionKeyGenerator() {
    secureRandom = new SecureRandom();
  }


  /**
   * Returns a random token of 80-92 base36 characters
   */
  public String create() {
    return createToken(80, false);
  }


  /**
   * Returns a random token of exactly tokenLength base36 characters.
   */
  public String createLength(int tokenLength) {
    return createToken(tokenLength, true);
  }


  /**
   * Returns a random base36 string of the passed length,
   * or up to 12 characters longer if exact=false
   */
  private String createToken(int length, boolean exact) {
    Metric metric = new Metric(log.isDebugEnabled());
    StringBuilder key = new StringBuilder(length);

    while (key.length() < length) {
      key.append(geta64bitRandomBase36String());
    }

    if (log.isDebugEnabled() && metric.elapsedMillis() > 50) {
      log.debug(length + "char Session key generation: " + metric.getMessage());
    }

    return exact ? key.substring(0, length-1) : key.toString();
  }


  /**
   * Outputs a 64 bit number in up to 11 chars as base-36
   */
  private String geta64bitRandomBase36String() {
    return Long.toString(Math.abs(secureRandom.nextLong()), Character.MAX_RADIX);
  }
}
