package edu.stanford.registry.server.utils;

import java.util.ArrayList;

/**
 * Created by tpacht on 4/20/2016.
 */
public class SquareDocumentationBuilder {

private static String quote = "\"";
private static String blankfield = "\"\"";
private static String separator = ",";

  public static String option(String column, String description, String value) {
    return blankfield + separator +column  + separator + blankfield
        + separator + quoted(description) + separator + quoted(value);

  }

  public static String option(String description, String value) {
    return
    blankfield + separator +blankfield+ separator+ blankfield
        + separator + quoted(description) + separator + quoted(value);
  }

  public static String option(String column, String fieldType, String description, String value) {
    return blankfield + separator +column  + separator + fieldType + separator + quoted(description) + separator + quoted(value);
  }
  public static ArrayList<String> question(ArrayList<String> questionText, String columnName, String fieldType, String value) {
    ArrayList<String> lines = new ArrayList<>();
    if (questionText != null && questionText.size() > 0) {
      for (int i = 0; i < questionText.size(); i++) {
        if (i == 0)
          lines.add(
              quoted(questionText.get(i)) + separator + columnName + separator + fieldType + separator + quoted(value));
        else
          lines.add(quoted(questionText.get(i)));
      }
    } else {
      lines.add(blankfield + separator + columnName + separator + fieldType + separator + value);
    }
    return lines;
  }

    private static    String quoted(String str) {
    return quote + stripTags(str) + quote;
    }

  private static String stripTags(String inputString) {
    String outputString = "";
    if (inputString == null) {
      return outputString;
    }
    boolean insideTag = false;
    for (int i=0; i < inputString.length(); ++i)
    {
      if (!insideTag && inputString.charAt(i) == '<')
      {
        insideTag = true;
        continue;
      }
      if (insideTag && inputString.charAt(i) == '>')
      {
        insideTag = false;
        continue;
      }
      if (!insideTag)
      {
        outputString = outputString + inputString.charAt(i);
      }
    }
    return outputString;
  }
}
