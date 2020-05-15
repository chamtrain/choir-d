/*
 * Copyright 2015 The Board of Trustees of The Leland Stanford Junior University.
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
package com.sksamuel.jqm4gwt.form;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Document;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sksamuel.jqm4gwt.HasMini;
import com.sksamuel.jqm4gwt.HasOrientation;
import com.sksamuel.jqm4gwt.HasText;
import com.sksamuel.jqm4gwt.IconPos;
import com.sksamuel.jqm4gwt.JQMCommon;
import com.sksamuel.jqm4gwt.JQMWidget;
import com.sksamuel.jqm4gwt.Orientation;
import com.sksamuel.jqm4gwt.events.HasTapHandlers;
import com.sksamuel.jqm4gwt.events.JQMComponentEvents;
import com.sksamuel.jqm4gwt.events.JQMHandlerRegistration;
import com.sksamuel.jqm4gwt.events.JQMHandlerRegistration.WidgetHandlerCounter;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.elements.JQMFormWidget;
import com.sksamuel.jqm4gwt.form.elements.JQMRadio;
import com.sksamuel.jqm4gwt.form.elements.JQMText;
import com.sksamuel.jqm4gwt.html.Legend;
import com.sksamuel.jqm4gwt.panel.JQMControlGroup;

/**
 * Created by tpacht on 6/18/2015.
 */
public class JQMTextset extends JQMFieldContainer implements HasText<JQMTextset>, HasValue<String>,
      HasSelectionHandlers<String>, HasOrientation<JQMTextset>, HasMini<JQMTextset>,
      JQMFormWidget, HasClickHandlers, HasTapHandlers {

    private boolean valueChangeHandlerInitialized;
    /**
     * The panel that is used for the controlgroup container
     */
    private MyJQMFieldset fieldset;


    private Legend legend;

    /**
     * The input's that are used for the text boxes
     */
    private final List<JQMText> boxes = new ArrayList<>();

  /**
   * The size of the grid containing the textboxes
   */
  private int gridSize = 0;
  private String labelText=null;
  private boolean horizontalLayout = false;


  /**
   * The class names used to make this display as a 2,3,4, or 5 item grid
   */
  private String[] gridStyle = {"ui-grid-a", "ui-grid-b", "ui-grid-c", "ui-grid-d"};

  private String[] blockStyle = {"ui-block-a", "ui-block-b", "ui-block-c", "ui-block-d", "ui-block-e"};

    /**
     * Creates a new {@link JQMTextset} with no label
     *
     *
    public JQMTextset() {
      this(null, 0);
    }
*/
    /**
     * Creates a new {@link JQMTextset} with the label text set to the given
     * value
     *
     * @param text
     *            the text for the label
     */
    public JQMTextset(String text, int size, boolean isHorizontal) {
      gridSize = size;
      if (text != null && text.trim().length() > 0)
        labelText = text;
      horizontalLayout = isHorizontal;
      setupFieldset();
    }

    public JQMTextset(final SafeHtml html, int size, boolean isHorizontal) {
      gridSize = size;
      horizontalLayout = isHorizontal;
      labelText = ""; // if we don't do this setupFieldset does't create the legend
      setupFieldset();

      legend.addAttachHandler(new Handler() {
        @Override
        public void onAttachOrDetach(AttachEvent event) {
          if (event.isAttached()) {
            setHTML(html);
          }
        }
      });
    }

    private void setupFieldset() {

      if (fieldset != null) remove(fieldset);
      // the fieldset is the inner container and is contained inside the flow
      if (horizontalLayout && gridSize > 1 && gridSize < 6)
        fieldset = new MyJQMFieldset(gridStyle[gridSize - 1]);
      else
        fieldset = new MyJQMFieldset("jqm4gwt-fieldset");

      // the legend must be added to the fieldset
      if (labelText != null) {
        legend = new Legend();
        legend.setText(labelText);
        legend.addStyleName(blockStyle[0]);
        legend.getElement().addClassName(blockStyle[0]);
        fieldset.add(legend);
        if (horizontalLayout) {
          setHorizontal();
          gridSize++;
          setMini(true);
        }

      }


      fieldset.getElement().setId(Document.get().createUniqueId());
      add(fieldset);

    }

    BlurHandler blurHandler;
    ArrayList<HandlerRegistration> blurHandlers = new ArrayList<HandlerRegistration>();

    private void addTextBoxesBlurHandler(final BlurHandler handler) {
      for (JQMText box : boxes) {
        blurHandlers.add(box.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            handler.onBlur(null);
          }
        }));
      }
    }

    private void clearBlurHandlers() {
      for (HandlerRegistration blurHandler : blurHandlers) blurHandler.removeHandler();
      blurHandlers.clear();
    }

    @Override
    protected void onLoad() {
      if (blurHandler != null && blurHandlers.size() == 0) addTextBoxesBlurHandler(blurHandler);
    }

    @Override
    protected void onUnload() {
      clearBlurHandlers();
    }

    /**
     * no-op implementation required for {@link JQMFormWidget}
     */
    @Override
    public HandlerRegistration addBlurHandler(final BlurHandler handler) {
      this.blurHandler = handler;
      clearBlurHandlers();
      addTextBoxesBlurHandler(handler);
      return null;
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
      return addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    public HandlerRegistration addTapHandler(TapHandler handler) {
      // this is not a native browser event so we will have to manage it via JS
      return JQMHandlerRegistration.registerJQueryHandler(new WidgetHandlerCounter() {
        @Override
        public int getHandlerCountForWidget(Type<?> type) {
          return getHandlerCount(type);
        }
      }, this, handler, JQMComponentEvents.TAP_EVENT, TapEvent.getType());
    }

    @Override
    public Label addErrorLabel() {
      return null;
    }



    /**
     * Adds a new text box to this set using the given text.
     * Returns a JQMText instance which can be used to change the value and
     * label of the radio button.
     *
     * @param text
     *            the value to associate with this radio option. This will be
     *            the value returned by methods that query the selected value.
     *
     * @param text
     *            the label to show for this radio option.
     *
     * @return a JQMRadio instance to adjust the added radio button
     */
    public JQMText addTextBox(String text) {
      final JQMText jqmText = new JQMText(text);
      addTextBox(jqmText);

      jqmText.addKeyDownHandler(new KeyDownHandler() {
        @Override
        public void onKeyDown(KeyDownEvent event) {
          if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            if (jqmText.getTabIndex() < (boxes.size() - 1)) {
              //boxes.get(jqmText.getTabIndex() + 1).setFocus(true);
            } else {

            }
          }
        }
      });
      return jqmText;
    }

    /**
     * UiBinder call method to add a radio button
     *
     * @param jqmText
     */
    @UiChild(tagname = "radio")
    public void addTextBox(JQMText jqmText) {

      if (horizontalLayout && boxes.size() < 5) {
        int index = boxes.size();
        if (labelText != null) {
          index++;
        }
        MyJQMFieldset horizontalJqmText = new MyJQMFieldset(blockStyle[index]);
        horizontalJqmText.add(jqmText);
        fieldset.add(horizontalJqmText);
      } else {
        fieldset.add(jqmText);
      }
      boxes.add(jqmText);
      jqmText.setTabIndex(boxes.size() -1);
    }

    public void clear() {
      boxes.clear();
      setupFieldset();
    }

    @Override
    public void setTheme(String themeName) {
      for (JQMText textBox : boxes) JQMCommon.applyTheme(textBox, themeName);
    }

    @Override
    public JQMWidget withTheme(String themeName) {
      setTheme(themeName);
      return this;
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<String> handler) {
      return addHandler(handler, SelectionEvent.getType());
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
      // Initialization code
      if (!valueChangeHandlerInitialized) {
        valueChangeHandlerInitialized = true;
        for (JQMText box : boxes) {
          box.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              SelectionEvent.fire(JQMTextset.this, getValue());
              ValueChangeEvent.fire(JQMTextset.this, getValue());
            }
          });
        }
      }
      return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Returns the text used for the main label
     */
    @Override
    public String getText() {
      return legend.getText();
    }

  @Override
  public void setText(String text) {
    labelText = text;
    setupFieldset();
  }
  public void setHTML(SafeHtml html) {
    legend.getElement().setInnerSafeHtml(html);
  }

  /**
     *
     * Note: this method will return null until after the jquery init phase has
     * completed. That means if you call getValue() on an element in the intial
     * construction of a page then it will return null.
     *
     * @return the value of the currently selected radio button or null if no
     *         button is currently selected.
     *
     *
     */
    @Override
    public String getValue() {
      StringBuilder response = new StringBuilder();
      for (JQMText box : boxes) {
         response.append(box.getValue());
      }
      return response.toString();
    }

  public String[] getValues() {
    ArrayList<String> ids = new ArrayList<>();
    for (JQMText text : boxes) {
      if (text != null && text.getValue().trim().length() > 0) {
        ids.add(text.getValue());
      }
    }
    return ids.toArray(new String[ids.size()]);
  }

  @Override
  public void setValue(String value) {

  }

  @Override
  public void setValue(String value, boolean fireEvents) {

  }

  /**
     * Returns the value of the radio option at the given index.
     *
     * @return the value of the k'th radio option
     */
    public String getValue(int k) {
      return boxes.get(k).getElement().getAttribute("value");
    }

    /**
     * Returns the value of the button that has the given id
     *
     * @return the value of the button with the given id
     */
    private String getValueForId(String id) {
      for (int k = 0; k < fieldset.getWidgetCount(); k++) {
        Widget widget = fieldset.getWidget(k);
        if (id.equals(widget.getElement().getAttribute("id")))
          return widget.getElement().getAttribute("value");
      }
      return null;
    }

    @Override
    public boolean isHorizontal() {
      return fieldset.isHorizontal();
    }

    @Override
    public boolean isVertical() {
      return fieldset.isVertical();
    }

    /**
     * Removes the given {@link JQMRadio} from this radioset
     *
     * @param jqmText
     *            the radio to remove
     */
    public void removeTextBox(JQMText jqmText) {
      if (jqmText == null) return;
      boxes.remove(jqmText);
      fieldset.remove(jqmText);
    }

    @Override
    public void setHorizontal() {
      fieldset.withHorizontal();
      if (isHorizontal() && gridSize > 1 && gridSize < 6) {
        fieldset.getElement().setClassName(gridStyle[gridSize - 1]);
        //fieldset.addStyleName(gridStyle[gridSize - 2]);
        //NodeList nodes =  fieldset.getElement().getChildNodes();

      }
    }

    @Override
    public JQMTextset withHorizontal() {
      setHorizontal();

      return this;
    }


    protected static native void refreshAll() /*-{
      $wnd.$("input[type='text']").each(function() {
        var w = $wnd.$(this);
        if (w.data('mobile-text') !== undefined) {
          w.text('refresh');
        }
      });
    }-*/;

    @Override
    public void setVertical() {
      fieldset.withVertical();
    }

    @Override
    public JQMTextset withVertical() {
      setVertical();
      return this;
    }

    /**
     * Returns the number of radio options set on this radioset
     *
     * @return the integer number of options
     */
    public int size() {
      return boxes.size();
    }

    @Override
    public JQMTextset withText(String text) {
      setText(text);
      return this;
    }

    public void setOrientation(Orientation value) {
      HasOrientation.Support.setOrientation(this, value);
    }

    public Orientation getOrientation() {
      return HasOrientation.Support.getOrientation(this);
    }

    public IconPos getIconPos() {
      String string = fieldset.getElement().getAttribute("data-iconpos");
      return string == null ? null : IconPos.valueOf(string);
    }

    /**
     * Sets the position of the icon.
     */
    public void setIconPos(IconPos pos) {
      if (pos == null)
        fieldset.getElement().removeAttribute("data-iconpos");
      else
        fieldset.getElement().setAttribute("data-iconpos", pos.getJqmValue());
    }

    @Override
    public boolean isMini() {
      return "true".equals(fieldset.getElement().getAttribute("data-mini"));
    }

    /**
     * If set to true then renders a smaller version of the standard-sized element.
     */
    @Override
    public void setMini(boolean mini) {
      fieldset.getElement().setAttribute("data-mini", String.valueOf(mini));
    }

    /**
     * If set to true then renders a smaller version of the standard-sized element.
     */
    @Override
    public JQMTextset withMini(boolean mini) {
      setMini(mini);
      return this;
    }

  static private class MyJQMFieldset extends JQMControlGroup {
    public MyJQMFieldset(String gridStyle) {
      super(Document.get().createFieldSetElement(), gridStyle);
    }
  }




}
