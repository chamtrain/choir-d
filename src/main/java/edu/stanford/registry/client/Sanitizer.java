package edu.stanford.registry.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

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
public class Sanitizer {
  private static final Set<String> TAG_WHITELIST = new HashSet<>(
      Arrays.asList("b", "em", "i", "h1", "h2", "h3", "h4", "h5", "h6", "hr",
          "ul", "ol", "li", "u", "p"));

  /**
   * HTML-sanitizes a string.
   *
   * <p>
   * The input string is processed as described above. The result of sanitizing
   * the string is guaranteed to be safe to use (with respect to XSS
   * vulnerabilities) in HTML contexts, and is returned as an instance of the
   * @link com.google.gwt.safehtml.shared.SafeHtml type.
   *
   * @param html the input String
   * @return a sanitized SafeHtml instance
   */
  public static SafeHtml sanitizeHtml(String html) {
    return sanitizeHtml(html, TAG_WHITELIST);
  }

  public static SafeHtml sanitizeHtml(String html, Set<String> whiteList) {
    if (html == null || html.length() == 0) {
      return SafeHtmlUtils.EMPTY_SAFE_HTML;
    }
    return SafeHtmlUtils.fromTrustedString(simpleSanitize(html, whiteList));
  }

  /*
   * Sanitize a string containing simple HTML markup as defined above. The
   * approach is as follows: We split the string at each occurence of '<'. Each
   * segment thus obtained is inspected to determine if the leading '<' was
   * indeed the start of a whitelisted tag or not. If so, the tag is emitted
   * unescaped, and the remainder of the segment (which cannot contain any
   * additional tags) is emitted in escaped form. Otherwise, the entire segment
   * is emitted in escaped form.
   *
   * In either case, EscapeUtils.htmlEscapeAllowEntities is used to escape,
   * which escapes HTML but does not double escape existing syntactially valid
   * HTML entities.
   */
  private static String simpleSanitize(String text, Set<String> whiteList) {
    StringBuilder sanitized = new StringBuilder();

    boolean firstSegment = true;
    for (String segment : text.split("<", -1)) {
      if (firstSegment) {
        /*
         *  the first segment is never part of a valid tag; note that if the
         *  input string starts with a tag, we will get an empty segment at the
         *  beginning.
         */
        firstSegment = false;
        sanitized.append(SafeHtmlUtils.htmlEscapeAllowEntities(segment));
        continue;
      }

      /*
       *  determine if the current segment is the start of an attribute-free tag
       *  or end-tag in our whitelist
       */
      int tagStart = 0; // will be 1 if this turns out to be an end tag.
      int tagEnd = segment.indexOf('>');
      String tag = null;
      boolean isValidTag = false;
      if (tagEnd > 0) {
        if (segment.charAt(0) == '/') {
          tagStart = 1;
        }
        tag = segment.substring(tagStart, tagEnd);
        isValidTag = isTagValid(whiteList, tag);
      }

      if (isValidTag) {
        // append the tag, not escaping it
        if (tagStart == 0) {
          sanitized.append('<');
        } else {
          // we had seen an end-tag
          sanitized.append("</");
        }
        sanitized.append(tag).append('>');

        // append the rest of the segment, escaping it
        sanitized.append(SafeHtmlUtils.htmlEscapeAllowEntities(
            segment.substring(tagEnd + 1)));
      } else {
        // just escape the whole segment
        sanitized.append("&lt;").append(
            SafeHtmlUtils.htmlEscapeAllowEntities(segment));
      }
    }
    return sanitized.toString();
  }

  public static ArrayList<String> findInvalidTags(String text, Set<String> whiteList) {
    ArrayList<String> tags = new ArrayList<>();
    boolean firstSegment = true;
    for (String segment : text.split("<", -1)) {
      if (firstSegment) {
        /*
         *  the first segment is never part of a valid tag; note that if the
         *  input string starts with a tag, we will get an empty segment at the
         *  beginning.
         */
        firstSegment = false;
        continue;
      }

      /*
       *  determine if the current segment is the start of an attribute-free tag
       *  or end-tag in our whitelist
       */
      int tagStart = 0; // will be 1 if this turns out to be an end tag.
      int tagEnd = segment.indexOf('>');
      String tag;
      if (tagEnd > 0) {
        if (segment.charAt(0) == '/') {
          tagStart = 1;
        }
        tag = segment.substring(tagStart, tagEnd);
        if (!isTagValid(whiteList, tag)) {
          tags.add(tag);
        }
      }
    }
    return tags;
  }

  private static boolean isTagValid(Set<String> whiteList, String tag) {
    String complexTag = tag;
    if (tag.contains(" ")) {
      complexTag = tag.substring(0, tag.indexOf(" ")).trim();
    }
    if (whiteList.contains(tag) || whiteList.contains(complexTag)) {
      return true;
    }
    GWT.log("TAG ' " + tag + "', complex tag '" + complexTag + "' was not found in whitelist " );
    return false;
  }

}
