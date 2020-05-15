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

import java.security.SecureRandom;

public class Token {

  private static SecureRandom randomGenerator = new SecureRandom();

  private String tokenValue = null;

  /**
   * Creates a new token with a new randomly-generated positive 7+digit number.
   */
  public Token() {
    int tokenInt = 0;
    do { // ensure we don't give too short of a token
      tokenInt = randomGenerator.nextInt();
      if (tokenInt < 0) {
        tokenInt = -tokenInt;
      }
    } while (tokenInt < 1000000);

    tokenValue = Integer.toString(tokenInt);
  }


  /**
   * Creates a token string based on the character and the passed String.
   * <br>For '-' (minus), it concatenates the two, removing spaces.
   *
   * The purpose of this is to ensure Activity tokens have a value when they don't
   * have an assessment_reg_id, even if they don't have a real survey_registration.token.
   * To avoid duplicates with real tokens, we generate one based on the type, but make ti
   * easy to identify (or avoid in SQL) by prefixing it with a non alpha-numeric character.
   */
  public static String generateToken(char c, String tokenString) {
    if (c == '-') {
      return tokenString.replace(" ", "");
    }
    throw new RuntimeException("Dont know how to create a token string for char: "+c);
  }


  /**
   * Creates a token with the passed string as the token value.
   */
  public Token(String token) {
    tokenValue = token;
  }


  /**
   * Returns the token value
   */
  public String getToken() {
    return tokenValue;
  }


  /**
   * An extra main routine to show the probability of creating lots of random numbers with no duplicates.
   */
  static public void main(String args[]) {
    double chanceOfPairMatch = 1.0 / 0x100000000L;
    double chanceOfNoMatchForNumTokens = 1.0;
    double chanceOfNoMatches = 1.0;
    double chanceOfNoMoreMatches = 1.0;
    int numTokens;
    int num50 = 0;
    int lastTime = 0;
    //boolean wasGreaterThanHalf = true;
    // one token is unique
    // chance of a token #2 matching is 1 - 1/2^32
    // chance of token #3 matching is 1 - 2/2^32, etc
    for (numTokens = 2;  (numTokens < 1500000);  numTokens++) {
      chanceOfNoMatchForNumTokens -= chanceOfPairMatch;
      chanceOfNoMatches = chanceOfNoMatches * chanceOfNoMatchForNumTokens;
      chanceOfNoMoreMatches *= chanceOfNoMatchForNumTokens;
      boolean justPassedHalfway = /*wasGreaterThanHalf &&*/ chanceOfNoMoreMatches < 0.5;
      if (justPassedHalfway) {
        //wasGreaterThanHalf = false;
        System.out.printf("Chance of no matches after %d more is %3.3f, for none in %d tokens is %3.3f\n",
                          (numTokens - lastTime), chanceOfNoMoreMatches, numTokens, chanceOfNoMatches);
        chanceOfNoMoreMatches = 1.0; // start over
        lastTime = numTokens;
        if (++num50 == 10)
          break;
      }
    }
    //System.out.printf("Chance of no matches after %d, for %d tokens is %3.3f\n",
    //                  (numTokens - lastTime), numTokens, chanceOfNoMatches);
    System.out.println("done.");
  }
}
