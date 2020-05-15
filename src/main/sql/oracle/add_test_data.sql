--
-- Copyright 2013 The Board of Trustees of The Leland Stanford Junior University.
-- All Rights Reserved.
--
-- See the NOTICE and LICENSE files distributed with this work for information
-- regarding copyright ownership and licensing. You may not use this file except
-- in compliance with a written license agreement with Stanford University.
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
-- WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
-- License for the specific language governing permissions and limitations under
-- the License.
--
DECLARE
    provider_mcurie number(6);
    provider_jsalk number(6);
BEGIN
-- Users
insert into user_principal (user_principal_id, username, display_name, enabled, email_addr) values(user_principal_sequence.nextval, 'mcurie', 'Curie, Marie S', 'Y', 'mcurie@notreal.stanford.edu');
insert into user_principal (user_principal_id, username, display_name, enabled, email_addr) values(user_principal_sequence.nextval, 'jsalk', 'Salk, Jonas E', 'Y', 'jsalk@notreal.stanford.edu');

-- Authorities
insert into user_authority select user_principal_id, 'CLINIC_STAFF[1]' from user_principal where username = 'mcurie';
insert into user_authority select user_principal_id, 'REGISTRATION[1]' from user_principal where username = 'jsalk'; 

-- Providers
insert into provider (provider_id, provider_eid, user_principal_id, dt_created) select user_principal_sequence.nextval, 'S0010000', user_principal_id, sysdate from user_principal where username = 'mcurie';
insert into provider (provider_id, provider_eid, user_principal_id, dt_created) select user_principal_sequence.nextval, 'S0020000', user_principal_id, sysdate from user_principal where username = 'jsalk';


-- Patients
INSERT into PATIENT (PATIENT_ID,FIRST_NAME,LAST_NAME,DT_BIRTH,CONSENT,META_VERSION,DT_CREATED,DT_CHANGED) values ('10001-6','George','Washington',to_date('01-MAR-93','DD-MON-RR'),'n',0,to_timestamp('05-JUN-13 09.40.40.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT (PATIENT_ID,FIRST_NAME,LAST_NAME,DT_BIRTH,CONSENT,META_VERSION,DT_CREATED,DT_CHANGED) values ('10002-4','John','Adams',to_date('19-JAN-80','DD-MON-RR'),'n',0,to_timestamp('05-JUN-13 09.42.30.000000000 PM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT (PATIENT_ID,FIRST_NAME,LAST_NAME,DT_BIRTH,CONSENT,META_VERSION,DT_CREATED,DT_CHANGED) values ('10003-2','Thomas','Jefferson',to_date('25-DEC-25','DD-MON-RR'),'n',0,to_timestamp('05-JUN-13 09.50.38.000000000 PM','DD-MON-RR HH.MI.SS.FF AM'),to_timestamp('17-DEC-13 03.49.06.676929000 PM','DD-MON-RR HH.MI.SS.FF AM'));
INSERT into PATIENT (PATIENT_ID,FIRST_NAME,LAST_NAME,DT_BIRTH,CONSENT,META_VERSION,DT_CREATED,DT_CHANGED) values ('10004-0','Abraham','Lincoln',to_date('16-FEB-73','DD-MON-RR'),'n',0,to_timestamp('26-FEB-13 09.41.02.908860000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT (PATIENT_ID,FIRST_NAME,LAST_NAME,DT_BIRTH,CONSENT,META_VERSION,DT_CREATED,DT_CHANGED) values ('10009-9','Eleanor','Roosevelt',to_date('01-JAN-62','DD-MON-RR'),'n',0,to_timestamp('15-SEP-13 11.27.35.297389000 PM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT (PATIENT_ID,FIRST_NAME,LAST_NAME,DT_BIRTH,CONSENT,META_VERSION,DT_CREATED,DT_CHANGED) values ('10010-7','Betty','Ford',to_date('04-SEP-78','DD-MON-RR'),'n',0,to_timestamp('24-FEB-13 08.56.13.931626000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT (PATIENT_ID,FIRST_NAME,LAST_NAME,DT_BIRTH,CONSENT,META_VERSION,DT_CREATED,DT_CHANGED) values ('10012-3','Nancy','Reagan',to_date('14-AUG-90','DD-MON-RR'),'n',0,to_timestamp('23-SEP-13 11.27.36.397389000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT (PATIENT_ID,FIRST_NAME,LAST_NAME,DT_BIRTH,CONSENT,META_VERSION,DT_CREATED,DT_CHANGED) values ('10017-2','Hilary','Clinton',to_date('22-OCT-57','DD-MON-RR'),'n',0,to_timestamp('29-AUG-13 12.16.34.577452000 PM','DD-MON-RR HH.MI.SS.FF AM'),to_timestamp('26-SEP-13 08.53.48.559503000 AM','DD-MON-RR HH.MI.SS.FF AM'));

-- Patient attributes
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10001-6','gender','Male','string',0,to_timestamp('05-JAN-13 09.40.40.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10001-6','participatesInSurveys','y','string',0,to_timestamp('05-JAN-13 09.40.46.252014000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10001-6','surveyEmailAddress','gwashington@notReal.stanford.edu','string',0,to_timestamp('05-JAN-13 09.41.40.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10002-4','gender','Male','string',0,to_timestamp('05-JUN-13 09.42.40.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10002-4','participatesInSurveys','y','string',0,to_timestamp('05-JUN-13 09.42.40.070229000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10002-4','surveyEmailAddress','jadams@notReal.stanford.edu','string',0,to_timestamp('05-JUN-13 09.42.42.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10003-2','gender','Male','string',0,to_timestamp('05-JAN-13 09.50.40.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10003-2','participatesInSurveys','y','string',0,to_timestamp('05-JAN-13 09.50.40.592905000 PM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10003-2','surveyEmailAddress','tjefferson@notReal.stanford.edu','string',0,to_timestamp('05-JAN-13 09.51.42.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10004-0','gender','Male','string',0,to_timestamp('16-FEB-13 09.41.05.908860000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10004-0','participatesInSurveys','n','string',0,to_timestamp('16-FEB-13 09.41.06.192905000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10004-0','declineReasonCode','english','string',0,to_timestamp('16-FEB-13 09.41.07.592905000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10009-9','gender','Female','string',0,to_timestamp('15-SEP-13 11.27.35.397389000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10009-9','participatesInSurveys','y','string',0,to_timestamp('15-SEP-13 11.27.39.397389000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10009-9','surveyEmailAddress','eroosevelt@notReal.stanford.edu','string',0,to_timestamp('15-SEP-13 11.27.39.497399000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10010-7','gender','Female','string',0,to_timestamp('24-FEB-13 08.56.14.931626000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10010-7','participatesInSurveys','n','string',0,to_timestamp('24-FEB-13 08.56.39.397389000 PM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10010-7','declineReasonCode','technology','string',0,to_timestamp('24-FEB-13 08.57.40.397399090 PM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10012-3','gender','Female','string',0,to_timestamp('23-SEP-13 11.27.36.397389000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10012-3','participatesInSurveys','y','string',0,to_timestamp('23-SEP-13 11.48.51.432174000 AM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10017-2','gender','Female','string',0,to_timestamp('29-AUG-13 12.16.34.577452000 PM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10017-2','participatesInSurveys','y','string',0,to_timestamp('29-AUG-13 12.16.37.577452000 PM','DD-MON-RR HH.MI.SS.FF AM'),null);
INSERT into PATIENT_ATTRIBUTE (patient_attribute_id,survey_site_id, PATIENT_ID,DATA_NAME,DATA_VALUE,DATA_TYPE,META_VERSION,DT_CREATED,DT_CHANGED) values (patient_seq.nextval,1,'10017-2','surveyEmailAddress','hclinton@notReal.stanford.edu','string',0,to_timestamp('29-AUG-13 12.16.39.577452000 PM','DD-MON-RR HH.MI.SS.FF AM'),null);

-- Survey registrations
SELECT provider_id INTO provider_mcurie FROM provider p, user_principal u WHERE p.user_principal_id = u.user_principal_id and u.username = 'mcurie';
SELECT provider_id INTO provider_jsalk FROM provider p, user_principal u WHERE p.user_principal_id = u.user_principal_id and u.username = 'jsalk';

--
INSERT into SURVEY_REGISTRATION (survey_reg_id, survey_site_id, PATIENT_ID, EMAIL_ADDR,TOKEN, SURVEY_DT, SURVEY_TYPE, REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION, DT_CREATED, DT_CHANGED, VISIT_TYPE, APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10001-6','smackey@pain.stanford.edu',1303156998,to_timestamp('01-JAN-14 11.00.00.001000000 AM','DD-MON-RR HH.MI.SS.FF AM'),'Initial.0605','s', null, provider_mcurie, 0,systimestamp,null,'NPV75',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10001-6','smackey@pain.stanford.edu',517997020,to_timestamp('01-JAN-14 08.30.00.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),'FollowUp','a', null, provider_mcurie, 0, systimestamp,null,'RPV30',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10001-6','smackey@pain.stanford.edu',1640169093,to_timestamp('01-JAN-14 08.30.00.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),'FollowUp','a', null, provider_mcurie, 0, systimestamp,null,'RPV30',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10001-6','smackey@pain.stanford.edu',2117362951,to_timestamp('01-JAN-14 08.30.00.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),'FollowUp.1013','a', null, provider_mcurie, 0, systimestamp,null,'RPV60',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10002-4','jadams@notReal.stanford.edu',808534351,to_timestamp('01-JAN-14 03.15.00.000000000 PM','DD-MON-RR HH.MI.SS.FF AM'),'Initial.0605','a', null, provider_jsalk, 0, systimestamp,null,'NPV60',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10003-2','tomjefferson@notReal.stanford.edu',2075827599,to_timestamp('01-JAN-14 03.30.00.000000000 PM','DD-MON-RR HH.MI.SS.FF AM'),'Initial.1013','a', null, provider_mcurie, 0, systimestamp,null,'NPV75',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10003-2','tjefferson@notReal.stanford.edu',2033451050,to_timestamp('01-JAN-14 10.30.00.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),'FollowUp.1013','a', null, provider_mcurie, 0, systimestamp,null,'RPV60',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10009-9','eroosevelt@notReal.stanford.edu',1879639929,to_timestamp('01-JAN-14 04.00.00.000000000 PM','DD-MON-RR HH.MI.SS.FF AM'),'Initial.1013','a', null, provider_jsalk, 0, systimestamp,null,'NPV60',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10009-9','eroosevelt@notReal.stanford.edu',1222136808,to_timestamp('01-JAN-14 01.00.00.000000000 PM','DD-MON-RR HH.MI.SS.FF AM'),'FollowUp.1013','a', null, provider_jsalk, 0, systimestamp,null,'RPV60',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10012-3', null, 1045556739,to_timestamp('01-JAN-14 09.00.00.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),'Initial.0605','a', null, provider_mcurie, 0, systimestamp, null,'NPV60',null);
INSERT into SURVEY_REGISTRATION (survey_reg_id,survey_site_id, PATIENT_ID,EMAIL_ADDR,TOKEN,SURVEY_DT,SURVEY_TYPE,REGISTRATION_TYPE, encounter_eid, provider_id, META_VERSION,DT_CREATED,DT_CHANGED,VISIT_TYPE,APPT_COMPLETE) 
values (patient_seq.nextval, 1, '10017-2', 'hclinton@notReal.stanford.edu', 571668718,to_timestamp('01-JAN-14 08.30.00.000000000 AM','DD-MON-RR HH.MI.SS.FF AM'),'FollowUp.1013','a', null, provider_jsalk, 0, systimestamp, null,'RPV30',null);


END;
/
commit
/

