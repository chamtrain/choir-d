package com.sksamuel.jqm4gwt.form.elements;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.sksamuel.jqm4gwt.HasMini;
import com.sksamuel.jqm4gwt.JQMCommon;
import com.sksamuel.jqm4gwt.JQMWidget;
import com.sksamuel.jqm4gwt.form.JQMFieldContainer;
import com.sksamuel.jqm4gwt.html.FormLabel;

/**
 * This is slightly modified version of JQMSlider to fix a couple bugs.
 * Comments from the original class:
 *
 * @author Stephen K Samuel samspade79@gmail.com 10 May 2011 00:24:06
 *         <p/>
 *         An implementation of a jquery mobile "slider" widget.
 *         <p/>
 *         For further info see
 * @link http://jquerymobile.com/demos/1.0b1/#/demos/1.0b1/docs/forms/forms-slider.html
 */
public class CustomJQMSlider extends JQMFieldContainer implements HasMini<JQMWidget>, JQMFormWidget {

    private final FormLabel label = new FormLabel();

    /**
     * The input to use as the base element
     */
    private final TextBox input = new TextBox();

    private Double internVal;
    /**
     * Create a new {@link CustomJQMSlider} with no label and default values
     * for the min and max
     */
    public CustomJQMSlider() {
        String id = Document.get().createUniqueId();
        label.setFor(id);
        input.getElement().setId(id);
        input.getElement().setAttribute("type", "range");
        input.getElement().setAttribute("value", "0");
        input.getElement().setAttribute("min", "0");
        input.getElement().setAttribute("max", "100");
        add(label);
        add(input);
        internVal = 0D;
    }

    /**
     * Create a new {@link CustomJQMSlider} with the given label and default values
     * for the min and max
     *
     * @param text the label text
     */
    public CustomJQMSlider(String text) {
        this();
        label.setText(text);
    }

    /**
     * Create a new {@link CustomJQMSlider} with the given label and min and max
     * values
     *
     * @param text the label text
     * @param min  the minimum value of the slider
     * @param max  the maximum value of the slider
     */
    public CustomJQMSlider(String text, int min, int max) {
        this(text);
        setMax(max);
        setMin(min);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return input.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                ValueChangeEvent.fire(CustomJQMSlider.this, getValue());
            }
        });
    }

    public void disable() {
        disable(input.getElement().getId());
    }

    private native void disable(String id)/*-{
        $wnd.$("#" + id).slider('disable');
    }-*/;

    public void enable() {
        enable(input.getElement().getId());
    }

    private native void enable(String id) /*-{
        $wnd.$("#" + id).slider('enable');
    }-*/;

    /**
     * Returns the text of the label
     *
     * @return
     */
    public String getLabelText() {
        return label.getText();
    }

    /**
     * Returns the max value of the slider
     */
    public int getMax() {
        return Integer.parseInt(input.getElement().getAttribute("max"));
    }

    /**
     * Returns the min value of the slider
     */
    public int getMin() {
        return Integer.parseInt(input.getElement().getAttribute("min"));
    }

    @Override
    public String getTheme() {
        return input.getElement().getAttribute("data-theme");
    }

    public String getTrackTheme() {
        return input.getElement().getAttribute("data-tracktheme");
    }

    @Override
    public String getValue() {
        Double rslt = getUiValue();
        if (rslt == null) 
          return internVal.toString();

        if (internVal == null) {
            int min = getMin();
            if (rslt.doubleValue() == min)
              return null;
        }
        internVal = rslt;
        return rslt.toString();
    }

    /**
     * returns the current value of the slider
     * returns null if slider is not created/initialized/attached yet and therefore has no UI value.
     */
    private Double getUiValue() {
        String v = JQMCommon.getVal(input.getElement().getId());
        if (v == null || v.isEmpty()) return null;
        return Double.valueOf(v);
    }

    @Override
    public void setValue(String value) {
        input.getElement().setAttribute("value", value);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        input.getElement().setAttribute("value", value);
    }

    private native String getValue(String id) /*-{
        return $wnd.$("#" + id).attr("value");
    }-*/;

    public boolean isHighlight() {
        return "true".equals(getAttribute("data-highligh"));
    }

    @Override
    public boolean isMini() {
        return "true".equals(getAttribute("data-mini"));
    }

    private native void refresh(String id, int value) /*-{
        $wnd.$("#" + id).val(value).slider("refresh");
    }-*/;


    public void setHighlight(boolean highlight) {
        setAttribute("data-highlight", String.valueOf(highlight));
    }

    /**
     * Sets the text of the label
     *
     * @param text is the new text to display
     */
    public void setLabelText(String text) {
        label.setText(text);
    }

    /**
     * Sets the new max range for the slider.
     *
     * @param max the new max range
     */
    public void setMax(int max) {
        input.getElement().setAttribute("max", String.valueOf(max));
    }

    /**
     * Sets the new min range for the slider
     *
     * @param min the new min range
     */
    public void setMin(int min) {
        input.getElement().setAttribute("min", String.valueOf(min));
    }

    /**
     * If set to true then renders a smaller version of the standard-sized element.
     */
    @Override
    public void setMini(boolean mini) {
        setAttribute("data-mini", String.valueOf(mini));
    }

    /**
     * If set to true then renders a smaller version of the standard-sized element.
     */
    @Override
    public CustomJQMSlider withMini(boolean mini) {
        setMini(mini);
        return this;
    }

    /**
     * Sets the theme swatch for the slider
     */
    public void setTrackTheme(String theme) {
        input.getElement().setAttribute("data-track-theme", theme);
    }

    /**
     * Sets the value of the slider to the given value
     *
     * @param value the new value of the slider, must be in the range of the
     *              slider
     */

    public void setIntValue(Integer value) {
        setValue(String.valueOf(value), false);
    }

    /**
     * Sets the value of the slider to the given value
     *
     * @param value the new value of the slider, must be in the range of the
     *              slider
     *
    */
    public void setIntValue(Integer value, boolean ignored) {
        getValue(input.getElement().getId());
        input.getElement().setAttribute("value", String.valueOf(value));
        refresh(getId(), value);
    }

    @Override
    public Label addErrorLabel() {
        return null;
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return input.addBlurHandler(handler);
    }
  }
