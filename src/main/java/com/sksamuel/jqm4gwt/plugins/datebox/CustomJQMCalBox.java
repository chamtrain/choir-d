package com.sksamuel.jqm4gwt.plugins.datebox;

import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Created by tpacht on 3/3/2016.
 */
public class CustomJQMCalBox extends JQMCalBox {
  protected static final String USE_FOCUS       = "\"useFocus\":"; // Show the calendar when the input box is clicked

  private Boolean useFocus = null;
  public CustomJQMCalBox() {
    this((String)null);
  }

  public CustomJQMCalBox(String text) {
    super(text);
  }

  public CustomJQMCalBox(SafeHtml html) {
    label.setHTML(html.asString());
  }

  @Override
  protected String constructDataOptions() {
    String dataOptions = super.constructDataOptions();
    StringBuilder sb = new StringBuilder();
    sb.append(dataOptions.substring(0, dataOptions.indexOf("}")));

    if (useFocus != null) {
      sb.append(',').append(USE_FOCUS).append(bool2Str(useFocus));
    }
    sb.append("}");
    input.getElement().setAttribute("tabindex", "1");
    return sb.toString();
  }

  public Boolean getUseFocus() {
    return useFocus;
  }

  /** This makes the calendar popUp when the input field is clicked */
  public void setUseFocus(Boolean useFocus) {
    this.useFocus = useFocus;
    refreshDataOptions();
  }
}
