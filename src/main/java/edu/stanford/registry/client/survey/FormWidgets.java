/*
 * Copyright 2016 The Board of Trustees of The Leland Stanford Junior University.
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
package edu.stanford.registry.client.survey;

import edu.stanford.survey.client.api.FieldType;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.helper.StyleHelper;
import org.gwtbootstrap3.client.ui.constants.Alignment;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.dom.client.Style;

public class FormWidgets {
  static Column headingColumn(String heading) {
    return headingColumn(heading, ColumnSize.MD_2);
  }

  static Column headingColumn(String heading, ColumnSize size) {
    Column lblCol = new Column(size);
    lblCol.add(headingParagraph(heading));
    return lblCol;
  }

  private static Paragraph headingParagraph(String heading) {
    Paragraph lbl = new Paragraph();
    lbl.add(new Heading(HeadingSize.H4, heading));
    StyleHelper.addEnumStyleName(lbl, Alignment.RIGHT);
    return lbl;
  }

  static Column buttonColumn(Button button, ButtonType buttonType) {
    return buttonColumn(button, buttonType, ColumnSize.MD_2);
  }

  static Column buttonColumn(Button button, ButtonType buttonType, ColumnSize columnSize) {
    Column buttonCol = new Column(columnSize);
    button.setType(buttonType);
    buttonCol.add(button);
    return buttonCol;
  }

  static Column valueColumn(String value) {
    return valueColumn(value, ColumnSize.MD_4);
  }

  static Column valueColumn(String value, ColumnSize size) {
    Column valCol = new Column(size);
    TextBox valueBox = new TextBox();
    valueBox.setText(value);
    valueBox.setEnabled(false);
    valCol.add(valueBox);
    return valCol;
  }

  static Column textBoxColumn(TextBox textBox, String value) {
    return textBoxColumn(textBox, value, ColumnSize.MD_9);
  }

  static Column textBoxColumn(TextBox textBox, String value, ColumnSize columnSize) {
    textBox.setText(value);
    Column line1Col = new Column(columnSize);
    line1Col.add(textBox);
    return line1Col;
  }

  static Column iconColumn(Icon icon, String text, ColumnSize columnSize) {
    final Column iconCol = new Column(columnSize);
    final Heading heading = new Heading(HeadingSize.H4);
    heading.add(new Text(text + " "));
    heading.add(icon);
    iconCol.add(heading);
    return iconCol;
  }

  static Column checkBoxColumn(CheckBox checkBox, boolean value) {
    checkBox.setValue(value);
    Column line1Col = new Column(ColumnSize.MD_9);
    line1Col.add(checkBox);
    return line1Col;
  }

  static Column formLabelColumn(FormLabel formLabel, ColumnSize size) {
    Column line1Col = new Column(size);
    line1Col.add(formLabel);
    return line1Col;
  }

  public static FormLabel formLabelFor(String labelText, String labelFor, Style.HasCssName styleName) {
    FormLabel formLabel = formLabelFor(labelText, labelFor);
    StyleHelper.addEnumStyleName(formLabel, styleName);
    return formLabel;
  }

  public static FormLabel formLabelFor(String labelText, String labelFor) {
    FormLabel formLabel = new FormLabel();
    formLabel.setText(labelText);
    formLabel.setFor(labelFor);
    return formLabel;
  }

  static TextBox forTextBox(String label, String id) {
    TextBox textBox = new TextBox();
    textBox.setText(label);
    textBox.setId(id);
    return textBox;
  }

// --Commented out by Inspection START (5/19/17, 8:06 AM):
//  public static InlineHelpBlock inlineHelpWithIcon(IconType iconType) {
//    InlineHelpBlock helpBlock = new InlineHelpBlock();
//    helpBlock.setIconType(iconType);
//    return helpBlock;
//  }
// --Commented out by Inspection STOP (5/19/17, 8:06 AM)

  public static String getFieldTypeHeading(FieldType fieldType) {
    return fieldType.toString().substring(0,1).toUpperCase()
        + fieldType.toString().substring(1);
  }



}
