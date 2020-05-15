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

package edu.stanford.registry.client;

import java.util.Date;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

public class TimePicker extends Composite implements ChangeHandler, ClickHandler, HasChangeHandlers, HasClickHandlers {

  public static final long MS_IN_A_MINUTE = 60 * 1000;
  public static final long MS_IN_AN_HOUR = 60 * MS_IN_A_MINUTE;
  public static final long MS_IN_A_DAY = 24 * MS_IN_AN_HOUR;
  public static final int AM = 0;
  public static final int PM = 1;

  public static DateTimeFormat fmtDisplayTime = DateTimeFormat.getFormat("hh:mm a");
  public static DateTimeFormat fmtDate = DateTimeFormat.getFormat("MM/dd/yyyy");
  public static DateTimeFormat fmtTime = DateTimeFormat.getFormat("MM/dd/yyyy HH:mm");
  public static DateTimeFormat fmtHr = DateTimeFormat.getFormat("HH");
  public static DateTimeFormat fmtMi = DateTimeFormat.getFormat("mm");

  int startHour = 8;
  int endHour = 19;
  long interval = 15 * MS_IN_A_MINUTE;

  private ListBox timeBox = new ListBox();

  public TimePicker(int startHr, int endHr, int intervalMinutes) {
    startHour = startHr;
    endHour = endHr;
    interval = intervalMinutes * MS_IN_A_MINUTE;
  }

  public TimePicker() {

    /**
     * Add the appointment time options as every [interval] minutes from the
     * start hour until the end hour
     **/

    // Add time intervals
    Date date = new Date();
    date = setTimeToZero(date);
    long itemTime = date.getTime() + (startHour * MS_IN_AN_HOUR);
    long endTime = date.getTime() + (endHour * MS_IN_AN_HOUR);
    while (itemTime < endTime) {
      timeBox.addItem(fmtDisplayTime.format(new Date(itemTime)), itemTime + "");
      itemTime = itemTime + interval;
    }
    timeBox.setSelectedIndex(0);

    initWidget(timeBox);
  }

  @Override
  public void onClick(ClickEvent event) {
    // Widget sender = (Widget) event.getSource();
    // checkAmPm(sender);
  }

  @Override
  public void onChange(ChangeEvent event) {
    // Widget sender = (Widget) event.getSource();
    // checkAmPm(sender);
  }

  /**
   * public void checkAmPm(Widget sender) { if (sender == hours) { // make sure
   * the am/pm indicator is correct and then notify anyone
   * <p/>
   * if (hours.getSelectedIndex() < AFTERNOON) { amPm.setSelectedIndex(AM); }
   * else { amPm.setSelectedIndex(PM); } } }
   *
  private void addArrayItems(ListBox box, String[] items) {
    for (String item : items) {
      box.addItem(item);
    }
  }*/

  @Override
  public HandlerRegistration addChangeHandler(ChangeHandler handler) {
    return addDomHandler(handler, ChangeEvent.getType());
  }

  /**
   * allows for observing click events on the entire widget
   */
  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }

  public Date getSelectedTime() {
    long time = Long.parseLong(timeBox.getValue(timeBox.getSelectedIndex()));
    return new Date(time);
  }

  public int getSelectedHour() {
    // return Integer.parseInt(hours.getValue(hours.getSelectedIndex()));
    return Integer.parseInt(fmtHr.format(getSelectedTime()));
  }

  public int getSelectedMinutes() {
    // return Integer.parseInt(minutes.getValue(minutes.getSelectedIndex()));
    return Integer.parseInt(fmtMi.format(getSelectedTime()));
  }

  /*
   * public int getAmPm() { return amPm.getSelectedIndex(); }
   *
   * public void setHours(int hr) { for (int h = 0; h < HOURS.length; h++) { if
   * (Integer.parseInt(HOURS[h]) == hr) { hours.setSelectedIndex(h); } } if
   * (hours.getSelectedIndex() < AFTERNOON) { amPm.setSelectedIndex(AM); } else
   * { amPm.setSelectedIndex(PM); } }
   *
   * public void setMinutes(int min) { for (int m = 0; m < MINUTES.length; m++)
   * { if (Integer.parseInt(MINUTES[m]) == min) { minutes.setSelectedIndex(m); }
   * }
   *
   * }
   *
   * public void setTime(Date time) { String hrs = fmtHours.format(time);
   * String min = fmtMinutes.format(time); String amp = fmtAmPm.format(time);
   *
   * for (int inx = 0; inx < HOURS.length; inx++) { if (HOURS[inx].equals(hrs))
   * { hours.setSelectedIndex(inx); } }
   *
   * for (int inx = 0; inx < MINUTES.length; inx++) { if
   * (MINUTES[inx].equals(min)) { minutes.setSelectedIndex(inx); } }
   *
   * for (int inx = 0; inx < AMPM.length; inx++) { if (AMPM[inx].equals(amp)) {
   * amPm.setSelectedIndex(inx); } } }
   *
   * public Date getTime(Date dt) { String stringDateTime = fmtDate.format(dt) +
   * " " + hours.getValue(hours.getSelectedIndex()) + ":" +
   * minutes.getValue(minutes.getSelectedIndex()) + " " +
   * amPm.getValue(amPm.getSelectedIndex()); // Log.debug("time is: " +
   * stringDateTime); return fmtTime.parse(stringDateTime);
   *
   * }
   *
   * public String getSelectedHours() { return
   * hours.getValue(hours.getSelectedIndex()); }
   *
   * public String getSelecteMinutes() { return
   * minutes.getValue(minutes.getSelectedIndex()); }
   */
  public Date setTimeToZero(Date dateIn) {
    String dtNoTimeString = fmtDate.format(dateIn) + " 00:00";
    return fmtTime.parse(dtNoTimeString);
  }

  public void setTime(Date timeIn) {
    for (int inx = 0; inx < timeBox.getItemCount(); inx++) {
      long itemTime = Long.parseLong(timeBox.getValue(inx));
      if (fmtDisplayTime.format(timeIn).equals(fmtDisplayTime.format(new Date(itemTime)))) {
        timeBox.setSelectedIndex(inx);
      }
    }
  }

  public Date getTime(Date date) {

    String stringDateTime = fmtDate.format(date) + " " + getSelectedHour() + ":" + getSelectedMinutes();
    return fmtTime.parse(stringDateTime);
  }

}
