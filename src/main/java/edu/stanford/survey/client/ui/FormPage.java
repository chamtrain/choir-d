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

package edu.stanford.survey.client.ui;

import edu.stanford.survey.client.api.FormAnswer;
import edu.stanford.survey.client.api.FormField;
import edu.stanford.survey.client.api.FormFieldAnswer;
import edu.stanford.survey.client.api.FormFieldValue;
import edu.stanford.survey.client.api.FormQuestion;
import edu.stanford.survey.client.api.HasPlayerProgressHandlers;
import edu.stanford.survey.client.api.PlayerProgressEvent;
import edu.stanford.survey.client.api.PlayerProgressHandler;
import edu.stanford.survey.client.api.SurveyFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sksamuel.jqm4gwt.DataIcon;
import com.sksamuel.jqm4gwt.button.JQMButton;
import com.sksamuel.jqm4gwt.events.TapEvent;
import com.sksamuel.jqm4gwt.events.TapHandler;
import com.sksamuel.jqm4gwt.form.CustomJQMForm;
import com.sksamuel.jqm4gwt.form.JQMForm;
import com.sksamuel.jqm4gwt.form.JQMTextset;
import com.sksamuel.jqm4gwt.form.SubmissionHandler;
import com.sksamuel.jqm4gwt.form.elements.CustomJQMCheckset;
import com.sksamuel.jqm4gwt.form.elements.CustomJQMNumber;
import com.sksamuel.jqm4gwt.form.elements.CustomJQMRadiosetTable;
import com.sksamuel.jqm4gwt.form.elements.CustomJQMSlider;
import com.sksamuel.jqm4gwt.form.elements.CustomJQMText;
import com.sksamuel.jqm4gwt.form.elements.JQMCheckbox;
import com.sksamuel.jqm4gwt.form.elements.JQMFormWidget;
import com.sksamuel.jqm4gwt.form.elements.JQMRadioset;
import com.sksamuel.jqm4gwt.form.elements.JQMSelect;
import com.sksamuel.jqm4gwt.form.elements.JQMSelectFilterable;
import com.sksamuel.jqm4gwt.form.elements.JQMTextArea;
import com.sksamuel.jqm4gwt.form.validators.Validator;
import com.sksamuel.jqm4gwt.layout.JQMCollapsible;
import com.sksamuel.jqm4gwt.plugins.datebox.CustomJQMCalBox;

/**
 * This survey page presents two lines of instructions, a set of various fields,
 * and a continue button.
 */
class FormPage extends SurveyPage implements HasPlayerProgressHandlers {
  interface Submit {
    void submit(FormAnswer answer);
  }
  void serverValidationFailed(FormQuestion question, String validationMessage) {
    SafeHtml html;
    if (validationMessage == null) {
      html = new SafeHtmlBuilder().appendEscapedLines(question.getServerValidationMessage()).toSafeHtml();
    } else {
      html = new SafeHtmlBuilder().appendEscapedLines(validationMessage).toSafeHtml();
    }
    error.setHTML(html);
  }

  private static final String DROPDOWN_NOSELECTION_TEXT = "Select an option";
  private final ArrayList<WidgetAndField> widgetAndField = new ArrayList<>();
  private final ArrayList<MyJQMRadioset> radiosets = new ArrayList<>();
  private final HTML error;
  private int tabIndex=0;
  private int videoLinks=0;

  FormPage(final SurveyFactory factory, FormQuestion question, final FormAnswer answer, final Submit submit) {
    if (question.getTitle1() != null) {
      add(new HTML("<h3>" + Sanitizer.sanitizeHtml(question.getTitle1()).asString() + "</h3>"));
    }


    if (question.getTitle2() != null) {
      add(new HTML("<h3>" + Sanitizer.sanitizeHtml(question.getTitle2()).asString() + "</h3>"));
    }

    final CustomJQMForm form = new CustomJQMForm();
    if (question.getFields() != null) {
      for (final FormField field : question.getFields()) {
        addFieldToForm(form, field, new ArrayList<JQMFormWidget>());
      }
    }
    form.setSubmissionHandler(new SubmissionHandler<JQMForm>() {
      @Override
      public void onSubmit(JQMForm form) {
        ArrayList<FormFieldAnswer> fieldAnswers = new ArrayList<>();
        for (int i = 0; i < form.getWidgetCount(); i++) {
          Widget widget = form.getWidget(i);
          if (!widget.isVisible()) {
            // Don't count the ones that are hidden because of choices the user made
            continue;
          }
          for (WidgetAndField wf : widgetAndField) {
            if (wf.widget == widget) {
              switch (wf.field.getType()) {
              case heading:
              case collapsibleContentField:
                break;
              case number:
                //fieldAnswers.add(fieldAnswer(wf.field, ((JQMNumber) widget).getValue()));
                fieldAnswers.add(fieldAnswer(wf.field, ((CustomJQMNumber) widget).getValue()));
                break;
              case text:
                fieldAnswers.add(fieldAnswer(wf.field, ((CustomJQMText) widget).getValue()));
                break;
              case textArea:
                fieldAnswers.add(fieldAnswer(wf.field, ((MyJQMTextArea) widget).getValue()));
                break;
              case checkboxes:
                fieldAnswers.add(fieldAnswer(wf.field, ((MyJQMCheckset) widget).getValues()));
                break;
              case radios:
                fieldAnswers.add(fieldAnswer(wf.field, ((MyJQMRadioset) widget).getValue()));
                break;
              case radioSetGrid:
                fieldAnswers.add(fieldAnswer(wf.field, ((CustomJQMRadiosetTable) widget).getValues()));
                break;
              case dropdown:
                fieldAnswers.add(fieldAnswer(wf.field, ((JQMSelect) widget).getValue()));
                break;
              case videoLink:
                fieldAnswers.add(fieldAnswer(wf.field, ((VideoLink) widget).getValue()));
                break;
              case numericScale:
                fieldAnswers.add(fieldAnswer(wf.field, ((MyJQMRadioset) widget).getValue()));
                break;
              case textBoxSet:
                fieldAnswers.add(fieldAnswer(wf.field, ((JQMTextset) widget).getValues()));
                break;
              case datePicker:
                fieldAnswers.add(fieldAnswer(wf.field, ((CustomJQMCalBox) widget).getValue()));
                break;
              case numericSlider:
                String value = ((CustomJQMSlider) widget).getValue();
                GWT.log("handling slider answer " + value);
                fieldAnswers.add(fieldAnswer(wf.field, value));
                break;
              default:
                throw new RuntimeException("Unknown field type: " + wf.field.getType());
              }
              break;
            }
          }
        }
        answer.setFieldAnswers(fieldAnswers);
        submit.submit(answer);
      }

      FormFieldAnswer fieldAnswer(FormField field, String... values) {
        FormFieldAnswer fieldAnswer = factory.fieldAnswer().as();
        fieldAnswer.setFieldId(field.getFieldId());
        ArrayList<String> choices = new ArrayList<>();
        choices.addAll(Arrays.asList(values));
        fieldAnswer.setChoice(choices);
        return fieldAnswer;
      }
    });
    add(form);

    error = new HTML();
    error.addStyleName(SurveyBundle.INSTANCE.css().errorMessage());
    add(error);

    if (!question.isTerminal()) {
      final JQMButton button = new JQMButton(new Button("Continue")) {};
      button.addStyleName(SurveyBundle.INSTANCE.css().continueButton());
      button.getElement().setTabIndex(tabIndex);
      button.addTapHandler(new TapHandler() {
        @Override
        public void onTap(TapEvent event) {
          form.submit();
        }
      });
      add(button);
    }
  }

  private void addValidator(JQMForm form, Validator validator, JQMFormWidget widget, ArrayList<JQMFormWidget> parents) {
//    parents.add(widget);
//    form.addValidator(validator, parents.toArray(new JQMFormWidget[parents.size()]));
//    parents.remove(parents.size() - 1);
    form.addValidator(validator, widget);

  }

  private void addValidator(JQMForm form, Validator validator, JQMFormWidget widget, boolean immediate) {
    form.addValidator(validator, immediate, widget);
  }
  interface HideShowController {
    void onVisibilityOrValueChange(Command command);
  }

  private JQMFormWidget addFieldToForm(final CustomJQMForm form, FormField field, ArrayList<JQMFormWidget> parents) {
    switch (field.getType()) {
    case heading:
      MyJQMHeading html = new MyJQMHeading("<h3>" + Sanitizer.sanitizeHtml(field.getLabel()).asString() + "</h3>");
      form.add(html);
      return html;
    case number:
      final CustomJQMNumber number;
      if (field.getMin() != null && field.getMax() != null) {
        number = new MyJQMNumber(Sanitizer.sanitizeHtml(field.getLabel()), Integer.parseInt(field.getMin()), Integer.parseInt(field.getMax()));
        if (field.getAttributes() != null && field.getAttributes().get("step") != null) {
          addValidator(form, new IntegerRangeValidator(number, field.getMin(), field.getMax(), field.getAttributes().get("step")), number, parents);
        } else {
          addValidator(form, new IntegerRangeValidator(number, field.getMin(), field.getMax()), number, parents);
        }
      } else {
        number = new MyJQMNumber(Sanitizer.sanitizeHtml(field.getLabel()));
        if (field.getMin() != null) {
          number.setMin(field.getMin());
        } else if (field.getMax() != null) {
          number.setMax(field.getMax());
        }
        addValidator(form, new NumberValidator(number), number, parents);
      }
      if (field.getAttributes() != null && field.getAttributes().get("step") != null) {
        number.setStep(field.getAttributes().get("step") );
      }
      form.add(number);
      if (field.isRequired()) {
        addValidator(form, new NumberRequiredValidator(number), number, parents);
      }
      widgetAndField.add(new WidgetAndField(number, field));
      return number;
    case text:
      final MyJQMText text = new MyJQMText(Sanitizer.sanitizeHtml(field.getLabel()));
      if (field.getAttributes() != null && field.getAttributes().get("StyleName") != null) {
        text.addStyleName(field.getAttributes().get("StyleName"));
      }
      form.add(text);
      if (field.isRequired()) {
        addValidator(form, new TextRequiredValidator(text), text, parents);
      }
      widgetAndField.add(new WidgetAndField(text, field));
      return text;
    case textArea:
      final MyJQMTextArea textArea = new MyJQMTextArea(Sanitizer.sanitizeHtml(field.getLabel()));
      if (field.getAttributes() != null && field.getAttributes().get("StyleName") != null) {
        textArea.addStyleName(field.getAttributes().get("StyleName"));
      }
      form.add(textArea);
      // TODO why does this cause form submit button to require two clicks?
      if (field.isRequired()) {
        addValidator(form, new TextAreaRequiredValidator(textArea), textArea, parents);
      }
      widgetAndField.add(new WidgetAndField(textArea, field));
      return textArea;
    case checkboxes:
      final MyJQMCheckset checkset = new MyJQMCheckset(field.getLabel());
      form.add(checkset);
      if (field.isRequired()) {
        addValidator(form, new ChecksetRequiredValidator(checkset), checkset, false);
      }

      for (final FormFieldValue value : field.getValues()) {
        String label = value.getLabel();
        final JQMCheckbox checkbox = new JQMCheckbox(value.getId(), label);
        if (label != null && label.contains("<")) {
          label = label.replace("<span class=\"fontnormal\">", "").replace("</span>", "");
          checkbox.getLabel().setHTML(Sanitizer.sanitizeHtml(label).asString());
        }
        checkset.addCheckbox(checkbox);
        if (value.getFields() != null) {
          for (final FormField childField : value.getFields()) {
            parents.add(checkset);
            final JQMFormWidget child = addFieldToForm(form, childField, parents);
            parents.remove(parents.size() - 1);
            child.asWidget().setVisible(false);
            GWT.log("Add checkset change handler: " + value.getId());
            checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
              @Override
              public void onValueChange(ValueChangeEvent<Boolean> event) {
                GWT.log("Checkbox change: " + checkbox.getInput().getName() + " (" + checkbox.isChecked() + ")");
                form.setVisible(child, isVisible(checkset.getElement()) && checkbox.isChecked());
              }
            });
          }
          ValueChangeEvent.fire(checkset, checkset.getValue());
        }
      }
      if (field.getAttributes() != null && "horizontal".equals(field.getAttributes().get("Align"))) {
          checkset.setHorizontal();
      }
      if (field.getAttributes() != null && field.getAttributes().get("StyleName") != null) {
        checkset.addStyleName(field.getAttributes().get("StyleName"));
      }
      widgetAndField.add(new WidgetAndField(checkset, field));
      return checkset;
    case radios:
      final MyJQMRadioset radioset = new MyJQMRadioset(Sanitizer.sanitizeHtml(field.getLabel()));
      form.add(radioset);
      // Check if the should be displayed horizontally
      if (field.getAttributes() != null) {
        if ("horizontal".equals(field.getAttributes().get("Align"))) {
          radioset.setHorizontal();
          if (field.getAttributes().get("leftLabel") != null) {
            radioset.setLeftLabel(field.getAttributes().get("leftLabel"));
          }
          if (field.getAttributes().get("rightLabel") != null) {
            radioset.setRightLabel(field.getAttributes().get("rightLabel"));
          }
          radiosets.add(radioset);
        }
        if (field.getAttributes() != null && field.getAttributes().get("StyleName") != null) {
          radioset.addStyleName(field.getAttributes().get("StyleName"));
        }
      }
      if (field.isRequired()) {
        addValidator(form, new RadiosetRequiredValidator(radioset), radioset, parents);
      }
      // Make sure we add all the radios before we attach change listeners below or else
      // we miss events for radios added after the first listener is attached
      for (final FormFieldValue value : field.getValues()) {
        radioset.addRadio(value.getId(), Sanitizer.sanitizeHtml(value.getLabel()));
      }
      for (final FormFieldValue value : field.getValues()) {
        if (value.getFields() != null) {
          for (FormField childField : value.getFields()) {
            parents.add(radioset);
            final JQMFormWidget child = addFieldToForm(form, childField, parents);
            parents.remove(parents.size() - 1);
            child.asWidget().setVisible(false);
            GWT.log("Adding radioset change handler: " + value.getId());
            radioset.onVisibilityOrValueChange(new Command() {
              @Override
              public void execute() {
                GWT.log("Radioset change handler: " + value.getId() + " =? " + radioset.getValue()
                    + " isVisible: " + isVisible(radioset.getElement()));
                form.setVisible(child, isVisible(radioset.getElement()) && value.getId().equals(radioset.getValue()));
              }
            });
          }
          ValueChangeEvent.fire(radioset, radioset.getValue());
        }
      }
      widgetAndField.add(new WidgetAndField(radioset, field));
      return radioset;
    case radioSetGrid:
      CustomJQMRadiosetTable grid = new CustomJQMRadiosetTable(field);
      form.add(grid);
      if (field.isRequired()) {
        addValidator(form, new RadiosetGridRequiredValidator(grid), grid, parents);
      }
      widgetAndField.add(new WidgetAndField(grid, field ));
      return grid;
    case dropdown:
      final JQMSelect dropdown;
      if ((field.getAttributes() != null) && (field.getAttributes().get("Filter") != null) &&
          field.getAttributes().get("Filter").equalsIgnoreCase("true")) {
        dropdown = new MyJQMSelectFilterable(Sanitizer.sanitizeHtml(field.getLabel())) {
          @Override
          public Boolean doFiltering(Element elt, Integer index, String searchValue) {
            // Wait until the first character has been typed before showing the filtered list.
            // This prevents the entire contents of a large list from showing if the user
            // has not typed anything yet. Instead only show the first 10.
            if ((searchValue == null) || searchValue.isEmpty()) {
              return index > 10;
            }
            return null;
          }
        };
      } else {
        dropdown = new MyJQMSelect(Sanitizer.sanitizeHtml(field.getLabel()));
      }
      form.add(dropdown);
      if (field.getAttributes() != null && field.getAttributes().get("StyleName") != null) {
        dropdown.addStyleName(field.getAttributes().get("StyleName"));
      }
      if (field.isRequired()) {
        addValidator(form, new SelectRequiredValidator(dropdown), dropdown, parents);
      }

      dropdown.addOption(null, DROPDOWN_NOSELECTION_TEXT);
      for (final FormFieldValue value : field.getValues()) {
        dropdown.addOption(value.getId(), Sanitizer.sanitizeHtml(value.getLabel()).asString());
      }
      for (final FormFieldValue value : field.getValues()) {
        if (value.getFields() != null) {
          for (FormField childField : value.getFields()) {
            parents.add(dropdown);
            final JQMFormWidget child = addFieldToForm(form, childField, parents);
            parents.remove(parents.size() - 1);
            child.asWidget().setVisible(false);
            GWT.log("Adding radioset change handler: " + value.getId());
            ((HideShowController)dropdown).onVisibilityOrValueChange(new Command() {
              @Override
              public void execute() {
                GWT.log("Dropdown change handler: " + value.getId() + " =? " + dropdown.getValue()
                    + " isVisible: " + isVisible(dropdown.getElement()));
                form.setVisible(child, isVisible(dropdown.getElement()) && value.getId().equals(dropdown.getValue()));
              }
            });
          }
          ValueChangeEvent.fire(dropdown, dropdown.getValue());
        }
      }

      widgetAndField.add(new WidgetAndField(dropdown, field));
      return dropdown;
    case collapsibleContentField:
      MyJQMCollapsible collapsHeading = new MyJQMCollapsible(Sanitizer.sanitizeHtml(field.getLabel()));
      if (field.getAttributes() != null && field.getAttributes().get("collapsibleContent") != null) {
        collapsHeading.add(field.getAttributes().get("collapsibleContent"));
      }
      if (field.getAttributes() != null && "INFO".equals(field.getAttributes().get("icon"))) {
        collapsHeading.setCollapsedIcon(DataIcon.INFO);
      }
      form.add(collapsHeading);
      widgetAndField.add(new WidgetAndField(collapsHeading, field));
      return collapsHeading;
    case videoLink:
      videoLinks++;
      if (field.getAttributes() != null) {
        VideoLink videoLink = new VideoLink(
            videoLinks,
            field.getAttributes().get("partnerId"),
            field.getAttributes().get("targetId"),
            field.getAttributes().get("wid"),
            field.getAttributes().get("uiConfId"),
            field.getAttributes().get("entryId"),
            field.getAttributes().get("videoHeight"),
            field.getAttributes().get("videoWidth"),
            field.getAttributes().get("frameHeight"),
            field.getAttributes().get("frameWidth"), new Callback<PlayerProgressEvent, Throwable>() {
          @Override
          public void onFailure(Throwable reason) {
            throw new RuntimeException("Videolink failure: " + reason.getMessage());
          }

          @Override
          public void onSuccess(PlayerProgressEvent result) {
            firePlayerProgressEvent(result);

          }
        });
        form.add(videoLink);

        widgetAndField.add(new WidgetAndField(videoLink, field));
        return videoLink;

      } else {
        throw new RuntimeException("Field type videoLink is missing attributes");
      }

    case numericScale:
      final MyJQMRadioset radioScale = new MyJQMRadioset(Sanitizer.sanitizeHtml(field.getLabel()) );
      if (field.getLabel() == null || field.getLabel().trim().length() == 0) {
        radioScale.addStyleName(SurveyBundle.INSTANCE.css().unlabeledFieldset());
      }
      radioScale.setHorizontal();
      boolean hasLabels = false;
      if (field.getAttributes() != null && field.getAttributes().get("leftLabel") != null) {
        radioScale.setLeftLabel(field.getAttributes().get("leftLabel"));
        hasLabels = true;
      }
      if (field.getAttributes() != null && field.getAttributes().get("rightLabel") != null) {
        radioScale.setRightLabel(field.getAttributes().get("rightLabel"));
        hasLabels = true;
      }
      if (hasLabels) {
        radioScale.addStyleName(SurveyBundle.INSTANCE.css().labeledNumericScale());
      } else {
        radioScale.addStyleName(SurveyBundle.INSTANCE.css().unlabeledNumericScale());
      }
      radiosets.add(radioScale);
      int lowerBound = Integer.parseInt(field.getMin());
      int upperBound = Integer.parseInt(field.getMax());
      for (int choice = lowerBound; choice <= upperBound; choice++) {
        radioScale.addRadio(Integer.toString(choice));
      }
      form.add(radioScale);
      if (field.isRequired()) {
        addValidator(form, new NumericScaleRequiredValidator(radioScale), radioScale, parents);
      }
      widgetAndField.add(new WidgetAndField(radioScale, field));
      return radioScale;
    case datePicker:
      final CustomJQMCalBox calBox = new CustomJQMCalBox(Sanitizer.sanitizeHtml(field.getLabel()));
      if (field.getAttributes() != null) {
        if (field.getAttributes().get("inlineBlind") != null && "true".equals(field.getAttributes().get("inlineBlind"))) {
          calBox.setUseInlineBlind(true);
        }
        if (field.getAttributes().get("useFocus") != null && "true".equals(field.getAttributes().get("useFocus"))) {
          //  explicitly assign the useFocus option to the calbox to have calendar open when input is clicked
          calBox.setUseFocus(true);
        }
        if (field.getAttributes().get("lockInput") != null && "false".equals(field.getAttributes().get("lockInput"))) {
          calBox.setLockInput(false);
        }
        if (field.getAttributes().get("styleName") != null) {
          calBox.setTheme(field.getAttributes().get("styleName"));
        }
      }
      calBox.setThemeDateToday("dtpicktoday");
      form.add(calBox);
      if (field.isRequired()) {
        addValidator(form, new DateRequiredValidator(calBox), calBox, false);
      }

      widgetAndField.add(new WidgetAndField(calBox, field));
      return calBox;
    case numericSlider:
      String sliderLabel = field.getLabel();
      int sliderLowerBound = 1;
      int sliderUpperBound = 10;
      if (field.getAttributes().get("lowerBound") != null && !field.getAttributes().get("lowerBound").isEmpty()) {
        try {
          sliderLowerBound = Integer.parseInt(field.getAttributes().get("lowerBound"));
        } catch (Exception ignored) {
        }
      }
      if (field.getAttributes().get("upperBound") != null && !field.getAttributes().get("upperBound").isEmpty()) {
        try {
          sliderUpperBound = Integer.parseInt(field.getAttributes().get("upperBound"));
        } catch (Exception ignored) {
        }
      }
      final CustomJQMSlider slider = new CustomJQMSlider(sliderLabel, sliderLowerBound, sliderUpperBound);
      if (field.getAttributes() != null && field.getAttributes().get("StyleName") != null) {
        slider.addStyleName(field.getAttributes().get("StyleName"));
      }
      form.add(slider);
      widgetAndField.add(new WidgetAndField(slider, field));
      return slider;
    case textBoxSet:
      boolean isHorizontal =  (field.getAttributes() != null && "horizontal".equals(field.getAttributes().get("Align")));
      String setLabel = field.getLabel();
      final JQMTextset textset = new JQMTextset(Sanitizer.sanitizeHtml(setLabel), field.getValues().size(), isHorizontal);
      if (isHorizontal) {
        textset.setHorizontal();
      }
      if (setLabel != null && setLabel.contains("<")) {
        setLabel = setLabel.replace("<span class=\"fontnormal\">", "").replace("</span>", "");
        textset.setHTML(Sanitizer.sanitizeHtml(setLabel));
      }
      if (field.getAttributes() != null && field.getAttributes().get("StyleName") != null) {
        textset.addStyleName(field.getAttributes().get("StyleName"));
      }
      form.add(textset);
      if (field.isRequired()) {
        addValidator(form, new TextsetRequiredValidator(textset), textset, parents);
      }

      for (final FormFieldValue value : field.getValues()) {
        String label = value.getLabel();

        final CustomJQMText textbox = new CustomJQMText(Sanitizer.sanitizeHtml(label));
        textset.addTextBox(textbox);
        if (value.getFields() != null) {
          for (final FormField childField : value.getFields()) {
            parents.add(textbox);
            final JQMFormWidget child = addFieldToForm(form, childField, parents);
            parents.remove(parents.size() - 1);
            child.asWidget().setVisible(false);
            GWT.log("Add checkset change handler: " + value.getId());
            textbox.addValueChangeHandler(new ValueChangeHandler<String>() {
              @Override
              public void onValueChange(ValueChangeEvent<String> event) {
                if (textbox.getValue() != null && textbox.getValue().length() > 0) {
                  form.setVisible(child, isVisible(textbox.getElement()));
                }
              }
            });
          }
          ValueChangeEvent.fire(textbox, textset.getValue());
        }
        /*
          For now textboxset is only used for physician surveys and there's no other type of input on the page so to make <ENTER> jump to
          the next one we set tabIndex on the individual text boxes and when there's no greater tabIndex input found submit the form
         */
        setTabIndex(textbox);
        textbox.addKeyDownHandler(new KeyDownHandler() {
          @Override
          public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
              if (!moveToNextField((textbox.getTabIndex() + 1) + "")) {
                form.submit();
              }
            }
          }
        });

        if (field.getAttributes() != null) {
          if (field.getAttributes().get("datatype") != null && "integer".equals(field.getAttributes().get("datatype"))) {
            int min = 0;
            int max = 999999999;
            if (field.getAttributes().get("min") != null) {
              try {
                min = Integer.parseInt(field.getAttributes().get("min"));
              } catch (Exception ignored) {
              }
            }
            if (field.getAttributes().get("max") != null) {
              try {
                max = Integer.parseInt(field.getAttributes().get("max"));
              } catch (Exception ignored) {
              }
            }
            if (field.getAttributes() != null && field.getAttributes().get("step") != null) {
              addValidator(form, new IntegerRangeValidator(new CustomJQMNumber(label), Integer.toString(min), Integer.toString(max), field.getAttributes().get("step")), textset, parents);
            } else {
              addValidator(form, new IntegerRangeValidator(new CustomJQMNumber(label), Integer.toString(min), Integer.toString(max)), textset, parents);
            }
          }
        }
      }
      widgetAndField.add(new WidgetAndField(textset, field));
      return textset;
    default:
      throw new RuntimeException("Unknown field type: " + field.getType());
    }
  }

  @Override
  protected void onPageBeforeShow() {
    //Window.alert("radioset id:"+radiosetId+" leftLabel:" + leftLabel + " rightLabel:"+rightLabel);
    for (MyJQMRadioset radioset : radiosets) {
      if (radioset.isHorizontal() && (
          (radioset.getLeftLabel() != null && radioset.getLeftLabel().asString().length() > 0)
          || (radioset.getRightLabel() != null && radioset.getRightLabel().asString().length() > 0))) {
        hackLabels(radioset.getId(), radioset.getLeftLabel().asString(), radioset.getRightLabel().asString());
      }
    }
  }

  private native void hackLabels(String radiosetId, String lowerBoundLabel, String upperBoundLabel) /*-{
  //$wnd.$("#" + radiosetId).css({paddingBottom: "50px", paddingTop: "20px"});
  $wnd.$("#" + radiosetId
                  + " .ui-radio:first-child").prepend('<span class="s-scale-left-label" style="position: absolute; left:0; right:80px; top:50px;">'
                  + lowerBoundLabel + '</span>');
  $wnd.$("#" + radiosetId
                  + " .ui-radio:last-child").append('<span class="s-scale-right-label" style="position: absolute; left:-40px; right:0; top:50px; text-align:right;">'
                  + upperBoundLabel + '</span>');
  }-*/;

  private static class WidgetAndField {
    final Widget widget;
    final FormField field;

    WidgetAndField(Widget widget, FormField field) {
      this.widget = widget;
      this.field = field;
    }
  }

  private static class TextAreaRequiredValidator implements Validator {
    private final JQMTextArea widget;

    public TextAreaRequiredValidator(JQMTextArea widget) {
      this.widget = widget;
    }

    @Override
    public String validate() {
      String value = widget.getValue();
      if (widget.isVisible() && (value == null || value.trim().length() == 0)) {
        widget.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
        return "Please provide an answer";
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
      return null;
    }
  }

  private static class TextRequiredValidator implements Validator {
    private final CustomJQMText widget;

    public TextRequiredValidator(MyJQMText widget) {
      this.widget = widget;
    }

    @Override
    public String validate() {
      if (widget.isVisible() && (widget.getValue() == null || widget.getValue().trim().length() == 0)) {
        widget.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
        return "Please provide an answer";
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
      return null;
    }
  }

  private static class NumberRequiredValidator implements Validator {
    private final CustomJQMNumber widget;

    public NumberRequiredValidator(CustomJQMNumber widget) {
      this.widget = widget;
    }

    @Override
    public String validate() {

      if (widget.isVisible() && (widget.getValue() == null || widget.getValue().trim().length() == 0)) {
        widget.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
        return "Please provide a number";
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());

      return null;
    }
  }

  private static class ChecksetRequiredValidator implements Validator {
    private final MyJQMCheckset widget;

    public ChecksetRequiredValidator(MyJQMCheckset widget) {
      this.widget = widget;
    }

    @Override
    public String validate() {
      if (widget.isVisible() && widget.getValue() == null) {
        widget.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
        return "Please choose at least one option";
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
      return null;
    }
  }

  private static class RadiosetRequiredValidator implements Validator {
    private final MyJQMRadioset widget;

    RadiosetRequiredValidator(MyJQMRadioset widget) {
      this.widget = widget;
    }

    @Override
    public String validate() {
      if (widget.isVisible() && widget.getValue() == null) {
        widget.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
        return "Please choose one option";
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
      return null;
    }
  }

  private static class RadiosetGridRequiredValidator implements Validator {
    private final CustomJQMRadiosetTable widget;

    RadiosetGridRequiredValidator(CustomJQMRadiosetTable widget) {
      this.widget = widget;
    }

    @Override
    public String validate() {
      GWT.log("Grid has " + widget.getQuestions() + " questions with " + widget.getChoices() + " possible answers and "
           + widget.getValues().length+ " responses");
      if (widget.isRanking()) {
        if (widget.isVisible() && widget.getValues().length != widget.getChoices()) {
          return "Please make a selection for each one";
        }
      } else {
        if (widget.getValues().length < widget.getQuestions()) {
          return "Please select a response for each question";
        }
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
      return null;
    }
  }

  private static class NumericScaleRequiredValidator implements Validator {
    private final MyJQMRadioset widget;

    NumericScaleRequiredValidator(MyJQMRadioset widget) {
      this.widget = widget;
    }

    @Override
    public String validate() {
      if (widget.isVisible() && (widget.getValue() == null || widget.getValue().isEmpty())) {
        widget.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
        return "Please choose an option";
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
      return null;
    }
  }

  private static class SelectRequiredValidator implements Validator {
    private final JQMSelect widget;

    public SelectRequiredValidator(JQMSelect widget) {
      this.widget = widget;
    }

    @Override
    public String validate() {
      if (widget.isVisible()) {
        if ((widget.getValue() == null) ||
            widget.getValue().isEmpty() ||
            widget.getValue().equals(DROPDOWN_NOSELECTION_TEXT)) {
          widget.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
          return "Please choose an option";
        }
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
      return null;
    }
  }

  private static class NumberValidator implements Validator {
    private final CustomJQMNumber number;
    private final String message = "Enter a number";
    private NumberValidator(final CustomJQMNumber number) {
      this.number = number;
      this.number.getInput().addBlurHandler( new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
          if (event.getSource() instanceof CustomJQMNumber) {
            CustomJQMNumber textBox = (CustomJQMNumber) event.getSource();
            try {
              int rangeChkResult;
              if (textBox.getStep() == null || textBox.getStep().equals("") || textBox.getStep().equals("1")) {
                 rangeChkResult = textBox.checkRange(Integer.parseInt(textBox.getValue()));
              } else {
                Float flt = Float.parseFloat(textBox.getValue());
                rangeChkResult = textBox.checkRange( flt );
              }
              if (rangeChkResult > 0) {
                textBox.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
                CustomJQMNumber.refresh(textBox.getId(), textBox.MESSAGES[rangeChkResult]);
              }
            } catch (NumberFormatException nfe) {
              textBox.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
              CustomJQMNumber.refresh(textBox.getId(), "Numbers only");
            }

          }
        }
        });
    }
    @Override
    public String validate() {
      if (number.getValue() != null && number.getValue().length() > 0) {
        try {
          int value = Integer.parseInt(number.getValue());
          number.setValue(String.valueOf(value), true);
          number.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
        } catch (NumberFormatException e) {
          number.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
          number.setValue("", true);
          number.getInput().setValue("");
          return message;
        }
        return null;
      } else {
        number.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
        number.setValue("", true);
      }
      return null;
    }
  }

  private static class IntegerRangeValidator implements Validator {
    private final CustomJQMNumber number;
    private final String message;
    private final String min;
    private int minInt;
    private final String max;
    private int maxInt;
    private final String step;

    private IntegerRangeValidator(final CustomJQMNumber number, String min, String max) {
      this.number = number;

      this.number.getInput().addBlurHandler(new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
          if (event.getSource() instanceof CustomJQMNumber) {
            CustomJQMNumber textBox = (CustomJQMNumber) event.getSource();
            try {
              int rangeChkResult = textBox.checkRange(
              Integer.parseInt(textBox.getValue()));
              if (rangeChkResult > 0) {
                textBox.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
                textBox.setValue("", true);
                CustomJQMNumber.refresh(textBox.getId(), textBox.MESSAGES[rangeChkResult]);
              }
            } catch (NumberFormatException nfe) {
              textBox.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
              textBox.setValue("", true);
              CustomJQMNumber.refresh(textBox.getId(), "Numbers only");
              number.setValue("");
            }
          }
        }
      });
      this.min = min;
      this.max = max;
      this.step = "1";
      if (min != null && max != null) {
        message = "Enter a number from " + min + " through " + max;
        minInt = Integer.parseInt(min);
        maxInt = Integer.parseInt(max);
      } else if (min != null) {
        message = "Enter a number at least " + min;
        minInt = Integer.parseInt(min);
      } else if (max != null) {
        message = "Enter a number at most " + max;
        maxInt = Integer.parseInt(max);
      } else {
        message = "Enter a number";
      }
    }
    private IntegerRangeValidator(final CustomJQMNumber number, String min, String max, final String step) {
      this.min = min;
      this.max = max;
      this.step = step;
      this.number = number;
      final int places = step.contains(".") ? step.length() - (step.indexOf(".") +  1): 0;
      this.number.getInput().addBlurHandler(new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
          if (event.getSource() instanceof TextBox) {
            TextBox textBox = (TextBox) event.getSource();
            try {
              int rangeChkResult;
              Float flt = Float.parseFloat(textBox.getValue());
              rangeChkResult = number.checkRange( flt );
              if (rangeChkResult > 0) {
                textBox.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
                textBox.setValue("", true);
                CustomJQMNumber.refresh(number.getId(), number.MESSAGES[rangeChkResult]);
              }
              int valuePlaces = flt.toString().contains(".") ? flt.toString().length() - (flt.toString().indexOf(".") + 1) : 0;
              if (valuePlaces  > places) {
                number.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
                number.setValue("", true);
                CustomJQMNumber.refresh(number.getId(), "Up to " + places + " decimal places only");
              }
            } catch (NumberFormatException nfe) {
              textBox.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
              textBox.setValue("", true);
              CustomJQMNumber.refresh(number.getId(), "Numbers only");
              number.setValue("");
            }
          }
        }
      });

      if (min != null && max != null) {
        message = "Enter a number from " + min + " through " + max + " with up to " + places + " decimal places";
        minInt = Integer.parseInt(min);
        maxInt = Integer.parseInt(max);
      } else if (min != null) {
        message = "Enter a number at least " + min + " with up to " + places + " decimal places";
        minInt = Integer.parseInt(min);
      } else if (max != null) {
        message = "Enter a number at most " + max + " with up to " + places + " decimal places";
        maxInt = Integer.parseInt(max);
      } else {
        message = "Enter a number with up to " + places + " decimal places";
      }
    }

    @Override
    public String validate() {
      if (number.getValue() != null && number.getValue().length() > 0) {
        try {
          int value;
          if (step == null || step.equals("") || step.equals("1")) {
            value = Integer.parseInt(number.getValue());
          } else {
            value = ((Float) Float.parseFloat(number.getValue())).intValue();
          }
          if (min != null && value < minInt) {
            return message;
          }
          if (max != null && value > maxInt) {
            return message;
          }
        } catch (NumberFormatException e) {
          return message;
        }
      }
      return null;
    }
  }

  private static class TextsetRequiredValidator implements Validator {
    private final JQMTextset widget;

    public TextsetRequiredValidator(JQMTextset widget) {
      this.widget = widget;
    }

    @Override
    public String validate() {
      if (widget.isVisible() && (widget.getValue() == null || widget.getValue().isEmpty())) {
        widget.addStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
        return "Please enter a value";
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
      return null;
    }
  }

  private static class DateRequiredValidator implements Validator {
    private final CustomJQMCalBox widget;
    public DateRequiredValidator(CustomJQMCalBox widget) {
      this.widget = widget;
    }

    @Override
    public String validate() { if (widget.isVisible() && widget.getValue() == null) {
        return "Please select a date";
      }
      widget.removeStyleName(SurveyBundle.INSTANCE.css().errorHighlight());
      return null;
    }
  }


  private static class MyJQMNumber extends CustomJQMNumber {
    MyJQMNumber(SafeHtml html) {
      super();
      label.getElement().setInnerSafeHtml(html);
    }
    MyJQMNumber(SafeHtml html, int min, int max){
      super("",min, max);
      label.getElement().setInnerSafeHtml(html);
    }
  }

  private static class MyJQMHeading extends HTML implements JQMFormWidget {
    public MyJQMHeading(String html) {
      super(html);
    }

    @Override
    public Label addErrorLabel() {
      return null;
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
      return null;
    }

    @Override
    public String getValue() {
      return null;
    }

    @Override
    public void setValue(String value) {
      // nothing to do
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
      // nothing to do
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
      return null;
    }
  }

  /**
   * Override to make sure it implements JQMFormWidget so we can use the Validators with it.
   */
  private static class MyJQMTextArea extends JQMTextArea implements JQMFormWidget {
    public MyJQMTextArea(SafeHtml html) {
      super(  );
      super.setHTML(html.asString());
    }

    @Override
    public Label addErrorLabel() {
      return null;
    }
  }

  private static class MyJQMText extends CustomJQMText {
    MyJQMText(SafeHtml safeHtml) {
        super.label.setHTML(safeHtml.asString());
    }
  }
  /**
   * Can you believe JQMCheckset doesn't have a way to ask for all selected values rather
   * rather than the first value selected.
   */
  private static class MyJQMCheckset extends CustomJQMCheckset implements HideShowController{
    // hack because this is private in super
    private final List<JQMCheckbox> checks = new ArrayList<>();
    private ArrayList<Command> onChange;
    private ScheduledCommand cmd;

    MyJQMCheckset(String text) {
      super(Sanitizer.sanitizeHtml(text));
    }

    @Override
    public void onVisibilityOrValueChange(Command command) {
        if (onChange == null) {
          onChange = new ArrayList<>();
          cmd = new ScheduledCommand() {
            @Override
            public void execute() {
              for (Command c : onChange) {
                try {
                  c.execute();
                } catch (Throwable t) {
                  t.printStackTrace();
                }
              }
            }
          };
          addDomHandler(new ChangeHandler() {
            @Override
            public void onChange(final ChangeEvent event) {
              GWT.log("Scheduling cmd for checkset ChangeHandler");
              Scheduler.get().scheduleDeferred(cmd);
            }
          }, ChangeEvent.getType());
          addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
              GWT.log("Scheduling cmd for checkset ValueChangeHandler");
              Scheduler.get().scheduleDeferred(cmd);
            }
          });
        }
        onChange.add(command);
      }


    @Override
    public void addCheckbox(JQMCheckbox checkbox) {
      super.addCheckbox(checkbox);
      checks.add(checkbox);
    }

    /**
     * @return the set of selected checkbox ids, or a zero length array if none are selected.
     */
    String[] getValues() {
      ArrayList<String> ids = new ArrayList<>();
      for (JQMCheckbox box : checks) {
        if (box.isChecked()) {
          ids.add(box.getInput().getName());
        }
      }
      return ids.toArray(new String[ids.size()]);
    }

    @Override
    public void setVisible(boolean visible) {
      if (isVisible(getElement()) != visible) {
        super.setVisible(visible);
        if (cmd != null) {
          GWT.log("Executing cmd for checkset (setVisible)");
          cmd.execute();
        } else {
          GWT.log("Skipping cmd for checkset (setVisible)");
        }
      }
    }
  }

  /**
   * Custom version of radioset so we can capture relevant value/visibility changes.
   */
  private static class MyJQMRadioset extends JQMRadioset implements HideShowController {
    private ArrayList<Command> onChange;
    private ScheduledCommand cmd;
    SafeHtml leftLabel;
    SafeHtml rightLabel;

    public MyJQMRadioset(SafeHtml html) {
      super(html);
    }

    @Override
    public void onVisibilityOrValueChange(Command command) {
      if (onChange == null) {
        onChange = new ArrayList<>();
        cmd = new ScheduledCommand() {
          @Override
          public void execute() {
            for (Command c : onChange) {
              try {
                c.execute();
              } catch (Throwable t) {
                t.printStackTrace();
              }
            }
          }
        };
        addValueChangeHandler(new ValueChangeHandler<String>() {
          @Override
          public void onValueChange(ValueChangeEvent<String> event) {
            GWT.log("Scheduling cmd for radioset ValueChangeHandler");
            Scheduler.get().scheduleDeferred(cmd);
          }
        });
      }
      onChange.add(command);
    }

    @Override
    public void setVisible(boolean visible) {
      if (isVisible(getElement()) != visible) {
        super.setVisible(visible);
        if (cmd != null) {
          GWT.log("Executing cmd for radioset (setVisible)");
          cmd.execute();
        } else {
          GWT.log("Skipping cmd for radioset (setVisible)");
        }
      }
    }

    public SafeHtml getLeftLabel() {
      return leftLabel;
    }

    public void setLeftLabel(String label) {
      leftLabel = Sanitizer.sanitizeHtml(label);
    }

    public SafeHtml getRightLabel() {
      return rightLabel;
    }

    public void setRightLabel(String label) {
      rightLabel = Sanitizer.sanitizeHtml(label);
    }
  }

  /**
   * Custom version of select so we can capture relevant value/visibility changes.
   */
  private static class MyJQMSelect extends JQMSelect implements HideShowController {
    private ArrayList<Command> onChange;
    private ScheduledCommand cmd;

    public MyJQMSelect(SafeHtml safeHtml) {
      super.label.setHTML(safeHtml.asString());
    }

    @Override
    public void onVisibilityOrValueChange(Command command) {
      if (onChange == null) {
        onChange = new ArrayList<>();
        cmd = new ScheduledCommand() {
          @Override
          public void execute() {
            for (Command c : onChange) {
              try {
                c.execute();
              } catch (Throwable t) {
                t.printStackTrace();
              }
            }
          }
        };
        addValueChangeHandler(new ValueChangeHandler<String>() {
          @Override
          public void onValueChange(ValueChangeEvent<String> event) {
            GWT.log("Scheduling cmd for radioset ValueChangeHandler");
            Scheduler.get().scheduleDeferred(cmd);
          }
        });
      }
      onChange.add(command);
    }

    @Override
    public void setVisible(boolean visible) {
      if (isVisible(getElement()) != visible) {
        super.setVisible(visible);
        if (cmd != null) {
          GWT.log("Executing cmd for radioset (setVisible)");
          cmd.execute();
        } else {
          GWT.log("Skipping cmd for radioset (setVisible)");
        }
      }
    }
  }

  /**
   * Custom version of select filterable so we can capture relevant value/visibility changes.
   */
  private static class MyJQMSelectFilterable extends JQMSelectFilterable implements HideShowController {
    private ArrayList<Command> onChange;
    private ScheduledCommand cmd;

    public MyJQMSelectFilterable(SafeHtml safeHtml) {
      super.label.setHTML(safeHtml.asString());
    }

    @Override
    public void onVisibilityOrValueChange(Command command) {
      if (onChange == null) {
        onChange = new ArrayList<>();
        cmd = new ScheduledCommand() {
          @Override
          public void execute() {
            for (Command c : onChange) {
              try {
                c.execute();
              } catch (Throwable t) {
                t.printStackTrace();
              }
            }
          }
        };
        addValueChangeHandler(new ValueChangeHandler<String>() {
          @Override
          public void onValueChange(ValueChangeEvent<String> event) {
            GWT.log("Scheduling cmd for radioset ValueChangeHandler");
            Scheduler.get().scheduleDeferred(cmd);
          }
        });
      }
      onChange.add(command);
    }

    @Override
    public void setVisible(boolean visible) {
      if (isVisible(getElement()) != visible) {
        super.setVisible(visible);
        if (cmd != null) {
          GWT.log("Executing cmd for radioset (setVisible)");
          cmd.execute();
        } else {
          GWT.log("Skipping cmd for radioset (setVisible)");
        }
      }
    }
  }

  private static class MyJQMCollapsible extends JQMCollapsible implements JQMFormWidget {

    public MyJQMCollapsible(SafeHtml html) {
      super("", true);
      super.setHTML(html);
    }
    public void add(String collapsibleContent) {
      super.add(new HTML(Sanitizer.sanitizeHtml(collapsibleContent)));
    }
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
      return null;
    }

    @Override
    public Label addErrorLabel() {
      return null;
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
      return null;
    }

    @Override
    public String getValue() {
      return null;
    }

    @Override
    public void setValue(String value) {
      // nothing to do
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
      // nothing to do
    }
  }

  @Override
  public HandlerRegistration addPlayerProgressHandler(PlayerProgressHandler handler) {
    return super.addHandler( handler, PlayerProgressEvent.getType());
  }

  private void firePlayerProgressEvent(PlayerProgressEvent event) {
    event.fire(this);
  }

  native boolean moveToNextField(String nextIndex) /*-{

    var node_list = $doc.getElementsByTagName('input');
    for (var i = 0; i < node_list.length; i++) {
      var node = node_list[i];
      if (node.hasAttribute("tabindex")) {
        if (node.getAttribute("tabindex") == nextIndex) {
          node.focus();
          return true;
        }
      }
    }
    return false;
  }-*/;

  private void setTabIndex(Focusable focusable) {
    focusable.setTabIndex(tabIndex);
    tabIndex++;
  }


}
