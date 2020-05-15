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

package edu.stanford.registry.server.utils;

import java.text.Normalizer;

import org.apache.commons.lang3.StringEscapeUtils;

public class StringUtils {

  public static String replace(String text, String variable, String value) {
    int inx = text.indexOf(variable);

    while (inx > -1) {
      StringBuilder sbuf = new StringBuilder();
      sbuf.append(text.subSequence(0, inx));
      sbuf.append(value);
      if (text.length() > inx + value.length()) {
        sbuf.append(text.substring(inx + variable.length()));
      }
      text = sbuf.toString();
      inx = text.indexOf(variable);
    }
    return text;
  }

  public static String cleanString(String inputString) {

    inputString = StringEscapeUtils.escapeHtml3(inputString);
    if (!Normalizer.isNormalized(inputString, Normalizer.Form.NFD)) {
      Normalizer.normalize(inputString, Normalizer.Form.NFD);
    }
    inputString = inputString.replaceAll("eval\\((.*)\\)", "");
    inputString = inputString.replaceAll("[\"'][\\s]*((?i)javascript):(.*)[\"']", "\"\"");
    inputString = inputString.replaceAll("((?i)script)", "");
    return inputString;
  }

  @SuppressWarnings("deprecation")
  public static String cleanXmlString(String inputString) {
    if (inputString == null) {
      return null;
    }
    String[] stokens = inputString.split("\"");
    StringBuilder strBuf = new StringBuilder(inputString.length() + 100);
    boolean text = false;
    for (String stoken : stokens) {
      if (text) { // escape string inside quotes
        text = false;
        strBuf.append("\"");
        strBuf.append(StringEscapeUtils.escapeXml(stoken));
        strBuf.append("\"");
      } else { // keep strings outside quotes as is
        text = true;
        strBuf.append(stoken);
      }
    }
    return strBuf.toString();
  }

  public static boolean notEmpty(String str) {
    return !isEmpty(str);
  }

  public static boolean isEmpty(String str) {
    return str == null || str.trim().length() == 0;
  }

  public static String stripOutMarkup(String str)
  {
      if (str == null || str.length() < 1) {
        return str;
      }

      if (!str.contains("<")) {
        return str;
      }

      StringBuilder cleanStr = new StringBuilder();
      int ptr=0;
      int open=str.indexOf("<");
      while (open >= 0 && ptr < str.length()) {
        cleanStr.append(str.substring(ptr, open));
        ptr = str.indexOf( ">", open) + 1;
        open=str.indexOf("<", ptr);
      }

      if (ptr < str.length()) {
        cleanStr.append(str.substring(ptr));
      }
      return cleanStr.toString();
  }
}
