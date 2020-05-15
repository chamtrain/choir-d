/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * This version of the sanitizer is exactly the same as the one provided by
 * GWT, but allows a couple additional tags in the whitelist ({@code <u>} and {@code <p>}).
 * I had to copy the key bits because the relevant tag list and sanitizing code
 * are marked private.
 *
 * <p>-- Original docs:</p>
 *
 * A simple and relatively inexpensive HTML sanitizer.
 *
 * <p>
 * This sanitizer accepts the subset of HTML consisting of the following
 * attribute-free tags:
 *
 * <ul>
 * <li>{@code <b>}, {@code <em>}, {@code <i>}</li>
 * <li>{@code <h1>}, {@code <h2>}, {@code <h3>},
 *     {@code <h4>}, {@code <h5>}, {@code <h6>}</li>
 * <li>{@code <ul>}, {@code <ol>}. {@code <li>}</li>
 * <li>{@code <hr>}</li>
 * </ul>
 *
 * as well as numeric HTML entities and HTML entity references. Any HTML
 * metacharacters that do not appear as part of markup in this subset will be
 * HTML-escaped.
 */
public class EmailSanitizer extends Sanitizer {
  private static final Set<String> EMAIL_TAG_WHITELIST = new HashSet<>(
      Arrays.asList("b", "i", "u", "em", "h1", "h2", "h3", "h4", "h5", "h6", "hr",
          "ul", "ol", "li",  "p", "br", "img", "a", "span", "div"));

  private static final Map<String, String> tagsMap = new HashMap<>();
  static {

    tagsMap.put("<b>", "Bold Text");
    tagsMap.put("<i>", "Italicized Text");
    tagsMap.put("<u>", "Underlined Text");
    tagsMap.put("<em>", "Emphasized Text");
    tagsMap.put("<h1>", "Heading 1");
    tagsMap.put("<h2>", "Heading 2");
    tagsMap.put("<h3>", "Heading 3");
    tagsMap.put("<h4>", "Heading 4");
    tagsMap.put("<h5>", "Heading 5");
    tagsMap.put("<h6>", "Heading 6");
    tagsMap.put("<hr>", "Horizontal Rule");
    tagsMap.put("<ul>", "Ordered List");
    tagsMap.put("<ol>", "Unordered List");
    tagsMap.put("<li>", "List Item");
    tagsMap.put("<p>", "Paragraph");
    tagsMap.put("<br>", "Line Break");
    tagsMap.put("<img>", "Image");
    tagsMap.put("<a>", "Hyperlink");
    tagsMap.put("<span>", "Span");
    tagsMap.put("<div>", "Div");
  }

  public static SafeHtml sanitizeHtml(String html) {
    GWT.log("in sanitizeHTML calling with " + EMAIL_TAG_WHITELIST.size() + " tags");
    return sanitizeHtml(html, EMAIL_TAG_WHITELIST);
  }

  public static ArrayList<String> findInvalidTags(String text) {
    return findInvalidTags(text, EMAIL_TAG_WHITELIST);
  }

  public static Map<String, String> getSupportedTags() {
    return tagsMap;
  }
}
