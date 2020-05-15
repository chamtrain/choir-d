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
package edu.stanford.registry.server.hl7;

import java.util.ArrayList;

public abstract class TerserLocations {
  public TerserLocations() {}
  public final static int SENDING_APPLICATION = 0;
  public final static int APPOINTMENT_ACTION = 1;
  public final static int APPOINTMENT_TYPE= 2;
  public final static int MESSAGE_CONTROL_ID = 3;
  public final static int MESSAGE_PROCESSING_ID = 4;
  public final static int HL7_VERSION_ID = 5;

  public final static int DEPARTMENT_ID = 6;
  public final static int DEPARTMENT_NAME = 7;

  public final static int PATIENT_ADDRESS_1 = 8;
  public final static int PATIENT_ADDRESS_2 = 9;
  public final static int PATIENT_CITY = 10;
  public final static int PATIENT_STATE = 11;
  public final static int PATIENT_ZIP_CODE = 12;
  public final static int PATIENT_COUNTRY = 13;
  public final static int PATIENT_BIRTH_DATE = 14;
  public final static int PATIENT_LAST_NAME = 15;
  public final static int PATIENT_FIRST_NAME = 16;
  public final static int PATIENT_MIDDLE_NAME = 17;
  public final static int PATIENT_NAME_SUFFIX = 18;
  public final static int PATIENT_GENDER = 19;
  public final static int PATIENT_RACE = 20;
  public final static int PATIENT_ETHNICITY = 21;
  public final static int PATIENT_LANGUAGE = 22;
  public final static int PATIENT_MARITAL_STATUS = 23;
  public final static int PATIENT_RELIGION = 24;
  public final static int PATIENT_MRN = 25;
  public final static int PATIENT_HOME_PHONE = 26;
  public final static int PATIENT_WORK_PHONE = 27;
  public final static int PATIENT_EMAIL = 28;
  public final static int PHYSICIAN_GROUP = 29;
  public final static int ATTENDING_PROVIDER_MSO = 30;
  public final static int ATTENDING_PROVIDER_LAST_NAME = 31;
  public final static int ATTENDING_PROVIDER_FIRST_NAME = 32;
  public final static int ATTENDING_PROVIDER_MIDDLE_NAME = 33;
  public final static int ATTENDING_PROVIDER_ID = 34;
  public final static int REFERRING_PROVIDER_MSO = 35;
  public final static int REFERRING_PROVIDER_LAST_NAME = 36;
  public final static int REFERRING_PROVIDER_FIRST_NAME = 37;
  public final static int REFERRING_PROVIDER_MIDDLE_NAME = 38;
  public final static int REFERRING_PROVIDER_ID = 39;
  public final static int CONSULTING_PROVIDER_MSO = 40;
  public final static int CONSULTING_PROVIDER_LAST_NAME = 41;
  public final static int CONSULTING_PROVIDER_FIRST_NAME = 42;
  public final static int CONSULTING_PROVIDER_MIDDLE_NAME = 43;
  public final static int CONSULTING_PROVIDER_ID = 44;

  public final static int ADMITTING_PROVIDER_MSO = 45;
  public final static int ADMITTING_PROVIDER_LAST_NAME = 46;
  public final static int ADMITTING_PROVIDER_FIRST_NAME = 47;
  public final static int ADMITTING_PROVIDER_MIDDLE_NAME = 48;
  public final static int ADMITTING_PROVIDER_ID = 49;
  public final static int PHYSICIAN_TITLE = 50;

  public final static int AIP_PROVIDER_MSO = 51;
  public final static int AIP_PROVIDER_LAST_NAME = 52;
  public final static int AIP_PROVIDER_FIRST_NAME = 53;
  public final static int AIP_PROVIDER_MIDDLE_NAME = 54;
  public final static int AIP_PROVIDER_ID = 55;
  public final static int ENCOUNTER_ID = 56;

  public final static int SCHEDULE_CSN = 57;
  public final static int SCHEDULE_APPOINTMENT_CREATE_DATETIME = 58;
  public final static int SCHEDULE_APPOINTMENT_DURATION = 59;
  public final static int SCHEDULE_APPOINTMENT_START_DATETIME = 60;
  public final static int SCHEDULE_APPOINTMENT_STATUS = 61;
  public final static int VISIT_ID = 62;
  public final static int VISIT_DESCRIPTION = 62;

  static final ArrayList<String> LOCATION_ARRAY = new ArrayList<>();
  static {
    LOCATION_ARRAY.add("/.MSH-3"); // SENDING_APPLICATION
    LOCATION_ARRAY.add("/.MSH-4"); // APPOINTMENT_ACTION
    LOCATION_ARRAY.add("/.MSH-9-2"); // APPOINTMENT_TYPE
    LOCATION_ARRAY.add("/.MSH-10"); // MESSAGE_CONTROL_ID
    LOCATION_ARRAY.add("/.MSH-11"); // MESSAGE_PROCESSING_ID
    LOCATION_ARRAY.add("/.MSH-12"); // HL7_VERSION_ID

    /***********************************************************************************************/
    /*                      Department HL7 Message Terser Locations                                */
    /***********************************************************************************************/
    LOCATION_ARRAY.add("/.RGS-3-1"); //DEPARTMENT_ID
    LOCATION_ARRAY.add("/.RGS-3-2"); // DEPARTMENT_NAME

    /***********************************************************************************************/
    /*                      Patient HL7 Message Terser Locations                                   */
    /***********************************************************************************************/
    LOCATION_ARRAY.add("/.PID-11-1"); // PATIENT_ADDRESS_1
    LOCATION_ARRAY.add("/.PID-11-2"); // PATIENT_ADDRESS_2
    LOCATION_ARRAY.add("/.PID-11-3"); // PATIENT_CITY
    LOCATION_ARRAY.add("/.PID-11-4"); // PATIENT_STATE
    LOCATION_ARRAY.add("/.PID-11-5"); // PATIENT_ZIP_CODE
    LOCATION_ARRAY.add("/.PID-11-6"); // PATIENT_COUNTRY
    LOCATION_ARRAY.add("/.PID-7-1"); // PATIENT_BIRTH_DATE
    LOCATION_ARRAY.add("/.PID-5-1"); // PATIENT_LAST_NAME
    LOCATION_ARRAY.add("/.PID-5-2"); // PATIENT_FIRST_NAME
    LOCATION_ARRAY.add("/.PID-5-3"); // PATIENT_MIDDLE_NAME
    LOCATION_ARRAY.add("/.PID-5-4"); // PATIENT_NAME_SUFFIX
    LOCATION_ARRAY.add("/.PID-8"); // PATIENT_GENDER
    LOCATION_ARRAY.add("/.PID-10-1"); // PATIENT_RACE
    LOCATION_ARRAY.add("/.PID-22"); // PATIENT_ETHNICITY
    LOCATION_ARRAY.add("/.PID-15"); // PATIENT_LANGUAGE
    LOCATION_ARRAY.add("/.PID-16"); // PATIENT_MARITAL_STATUS
    LOCATION_ARRAY.add("/.PID-17"); // PATIENT_RELIGION
    LOCATION_ARRAY.add("/.PID-3-1"); // PATIENT_MRN
    LOCATION_ARRAY.add("/.PID-13-1"); // PATIENT_HOME_PHONE
    LOCATION_ARRAY.add("/.PID-14-1"); // PATIENT_WORK_PHONE
    LOCATION_ARRAY.add("/.PID-13(1)-4"); // PATIENT_EMAIL (/.PID(0)-13(1)-4)

    /***********************************************************************************************/
    /*                      Physician HL7 Message Terser Locations                                 */
    /***********************************************************************************************/

    LOCATION_ARRAY.add("/.AIG-3-2"); // PHYSICIAN_GROUP
    LOCATION_ARRAY.add("/.PV1-7-1"); // ATTENDING_PROVIDER_MSO
    LOCATION_ARRAY.add("/.PV1-7-2"); // ATTENDING_PROVIDER_LAST_NAME
    LOCATION_ARRAY.add("/.PV1-7-3"); // ATTENDING_PROVIDER_FIRST_NAME
    LOCATION_ARRAY.add("/.PV1-7-4"); // ATTENDING_PROVIDER_MIDDLE_NAME
    LOCATION_ARRAY.add("/.PV1-7(1)-1"); // ATTENDING_PROVIDER_ID

    LOCATION_ARRAY.add("/.PV1-8-1"); // REFERRING_PROVIDER_MSO =
    LOCATION_ARRAY.add("/.PV1-8-2"); // REFERRING_PROVIDER_LAST_NAME
    LOCATION_ARRAY.add("/.PV1-8-3"); // REFERRING_PROVIDER_FIRST_NAME
    LOCATION_ARRAY.add("/.PV1-8-4"); // REFERRING_PROVIDER_MIDDLE_NAME
    LOCATION_ARRAY.add("/.PV1-8(1)-1"); // REFERRING_PROVIDER_ID

    LOCATION_ARRAY.add("/.PV1-9-1"); // CONSULTING_PROVIDER_MSO
    LOCATION_ARRAY.add("/.PV1-9-2"); // CONSULTING_PROVIDER_LAST_NAME
    LOCATION_ARRAY.add("/.PV1-9-3"); // CONSULTING_PROVIDER_FIRST_NAME
    LOCATION_ARRAY.add("/.PV1-9-4"); // CONSULTING_PROVIDER_MIDDLE_NAME
    LOCATION_ARRAY.add("/.PV1-9(1)-1"); // CONSULTING_PROVIDER_ID

    LOCATION_ARRAY.add("/.PV1-17-1"); // ADMITTING_PROVIDER_MSO
    LOCATION_ARRAY.add("/.PV1-17-2"); // ADMITTING_PROVIDER_LAST_NAME
    LOCATION_ARRAY.add("/.PV1-17-3"); // ADMITTING_PROVIDER_FIRST_NAME
    LOCATION_ARRAY.add("/.PV1-17-4"); // ADMITTING_PROVIDER_MIDDLE_NAME
    LOCATION_ARRAY.add("/.PV1-17(1)-1"); // ADMITTING_PROVIDER_ID

    LOCATION_ARRAY.add("/.AIP-4-2"); // PHYSICIAN_TITLE

    LOCATION_ARRAY.add("/.AIP-3-1"); // AIP_PROVIDER_MSO
    LOCATION_ARRAY.add("/.AIP-3-2"); // AIP_PROVIDER_LAST_NAME
    LOCATION_ARRAY.add("/.AIP-3-3"); // AIP_PROVIDER_FIRST_NAME
    LOCATION_ARRAY.add("/.AIP-3-4"); // AIP_PROVIDER_MIDDLE_NAME
    LOCATION_ARRAY.add("/.AIP-3(1)-1"); // AIP_PROVIDER_ID

    LOCATION_ARRAY.add("/.PV1-19"); // ENCOUNTER_ID

  /***********************************************************************************************/
  /*                      Schedule HL7 Message Terser Locations                                  */
  /***********************************************************************************************/
    LOCATION_ARRAY.add("/SCH-2"); // SCHEDULE_CSN
    LOCATION_ARRAY.add("/MSH-7"); // SCHEDULE_APPOINTMENT_CREATE_DATETIME
    LOCATION_ARRAY.add("/SCH-11-3"); // SCHEDULE_APPOINTMENT_DURATION
    LOCATION_ARRAY.add("/SCH-11-4"); // SCHEDULE_APPOINTMENT_START_DATETIME
    LOCATION_ARRAY.add("/.SCH-25"); // SCHEDULE_APPOINTMENT_STATUS
    LOCATION_ARRAY.add("/.SCH-7-1"); // VISIT_ID
    LOCATION_ARRAY.add("/.SCH-7-2"); // VISIT_DESCRIPTION
  }


}
