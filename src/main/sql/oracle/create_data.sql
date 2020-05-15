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

-- Get the survey system id's for Local and PROMIS and add the assessments
DECLARE
local_system_id number(6);
local_promis_system_id number(6);
cat2_system_id number(6);
promis_system_id number(6);
promis2_system_id number(6);
admin_user_id number(6);
BEGIN
SELECT survey_system_id INTO local_system_id FROM survey_system WHERE survey_system_name = 'Local';
SELECT survey_system_id INTO local_promis_system_id FROM survey_system WHERE survey_system_name = 'LocalPromis';
SELECT survey_system_id INTO cat2_system_id FROM survey_system WHERE survey_system_name = 'cat2';
SELECT survey_system_id INTO promis_system_id FROM survey_system WHERE survey_system_name = 'PROMIS';
SELECT survey_system_id INTO promis2_system_id FROM survey_system WHERE survey_system_name = 'PROMIS.2';
SELECT survey_system_id INTO medsOpioid4A_system_id FROM survey_system WHERE survey_system_name = 'edu.stanford.registry.server.survey.MedsOpioid4SurveyService';
SELECT survey_system_id INTO headache_system_id FROM survey_system WHERE survey_system_name = 'edu.stanford.registry.server.survey.HeadacheSurveyService';
SELECT survey_system_id INTO opioidSurveys_system_id FROM survey_system WHERE survey_system_name = 'edu.stanford.registry.server.survey.OpioidSurveysService';

INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id, STUDY_CODE_SEQ.NEXTVAL, 'names', 0, CURRENT_TIMESTAMP, null, 'names');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'painIntensity', 0, CURRENT_TIMESTAMP, null,'Pain Intensity');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'bodymap', 0, CURRENT_TIMESTAMP, null, 'Body Map');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'painExperience', 0, CURRENT_TIMESTAMP, null,'Pain Experience');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'stanfordFive', 0, CURRENT_TIMESTAMP, null,'Stanford Five and Treatment Expectations');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'faDay', 0, CURRENT_TIMESTAMP, null,'Functional Assessment');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'faWorking', 0, CURRENT_TIMESTAMP, null,null);
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'faDisability', 0, CURRENT_TIMESTAMP, null,null);
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'faLawsuit', 0, CURRENT_TIMESTAMP, null,null);
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'sleepImpair', 0, CURRENT_TIMESTAMP, null,'Sleep Impairment');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'psychHistory', 0, CURRENT_TIMESTAMP, null,'Psychology History');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'ptsd', 0, CURRENT_TIMESTAMP, null,'Post Traumatic Stress Disorder');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'healthUtil', 0, CURRENT_TIMESTAMP, null,'Healthcare Utilization');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'background', 0, CURRENT_TIMESTAMP, null,'Background');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'education', 0, CURRENT_TIMESTAMP, null,'Education');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'questions', 0, CURRENT_TIMESTAMP, null,'Questions');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'otherPainDocs', 0, CURRENT_TIMESTAMP, null,'Other Pain Physicians');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'smoking', 0, CURRENT_TIMESTAMP, null,'Smoking');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'alcohol', 0, CURRENT_TIMESTAMP, null,'Alcohol and drugs');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'painCatastrophizingScale', 0, CURRENT_TIMESTAMP, null,'Pain Catastrophizing Scale');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'globalHealth', 0, CURRENT_TIMESTAMP, null,'Global Health');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'research', 0, CURRENT_TIMESTAMP, null, null);

INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'parentGlobalHealth', 0, CURRENT_TIMESTAMP, null,'Parent Global Health');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'proxyPainIntensity', 0, CURRENT_TIMESTAMP, null,'Parent Proxy Pain Intensity');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'proxyBodymap', 0, CURRENT_TIMESTAMP, null, 'Parent Proxy Body Map');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'proxyPainCatastrophizingScale', 0, CURRENT_TIMESTAMP, null,'Parent Proxy Pain Catastrophizing Scale');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'pedPainCatastrophizingScale', 0, CURRENT_TIMESTAMP, null,'Pediatric Pain Catastrophizing Scale');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'primaryReason', 0, CURRENT_TIMESTAMP, null,'Primary Referral Reason');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'secondaryReason', 0, CURRENT_TIMESTAMP, null,'Secondary Referral Reason');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'otherDiagnoses', 0, CURRENT_TIMESTAMP, null,'Other Diagnoses');

INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsOpioid', 0, CURRENT_TIMESTAMP, null, 'Opioid Pain Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsNerve', 0, CURRENT_TIMESTAMP, null, 'Nerve Pain Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsHeadache', 0, CURRENT_TIMESTAMP, null, 'Headache Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsMuscle', 0, CURRENT_TIMESTAMP, null, 'Muscle Relaxant Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsMood', 0, CURRENT_TIMESTAMP, null, 'Mood Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsAnxiety', 0, CURRENT_TIMESTAMP, null, 'Anxiety Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsSleep', 0, CURRENT_TIMESTAMP, null, 'Sleep Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsNSAID', 0, CURRENT_TIMESTAMP, null, 'NSAID Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsADV', 0, CURRENT_TIMESTAMP, null, 'Pain Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsCAM', 0, CURRENT_TIMESTAMP, null, 'Complementary and Alternative Medications');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsOther', 0, CURRENT_TIMESTAMP, null, 'Other Pain Medications');

INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'treatmentsRehab', 0, CURRENT_TIMESTAMP, null, 'Rehabilitative Modalities');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'treatmentsPsych', 0, CURRENT_TIMESTAMP, null, 'Psychological Treatments');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'treatmentsCAM', 0, CURRENT_TIMESTAMP, null, 'Complementary and Alternative Medicine Treatments');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'treatmentsIntervention', 0, CURRENT_TIMESTAMP, null, 'Interventional Procedures');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'treatmentsADV', 0, CURRENT_TIMESTAMP, null, 'Specialized Pain Management Interventions or Surgeries');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'treatmentsOther', 0, CURRENT_TIMESTAMP, null, 'Other Interventions or Surgeries');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (local_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'opioidRisk', 0, CURRENT_TIMESTAMP, null, 'Opioid Risk Tool');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (medsOpioid4A_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'medsOpioid4A', 0, CURRENT_TIMESTAMP, null, '4 A`'s of Opioid Treatment');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (headache_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'headache', 0, CURRENT_TIMESTAMP, null, 'Headache');
INSERT INTO STUDY (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed, title) VALUES (opioidSurveys_system_id,  STUDY_CODE_SEQ.NEXTVAL, 'opioidPromisSurvey', 0, CURRENT_TIMESTAMP, null, 'PROMIS Opioid Pain Medications');

--
-- Promis v1
--
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Pain Intensity Bank',0, CURRENT_TIMESTAMP, null, 'PROMIS Pain Interference Bank');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED) VALUES (promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Pain Behavior Bank',0, CURRENT_TIMESTAMP, null);
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED) VALUES (promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Physical Function Bank',0, CURRENT_TIMESTAMP, null);
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED) VALUES (promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Fatigue Bank',0, CURRENT_TIMESTAMP, null);
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED) VALUES (promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Depression Bank',0, CURRENT_TIMESTAMP, null);
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED) VALUES (promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Anxiety Bank',0, CURRENT_TIMESTAMP, null);
update study set title = substr(study_description, 1, length(study_description)-5) where study_description like 'PROMIS%Bank' and title is null;
--
-- Promis v2
--
INSERT INTO STUDY (SELECT promis2_system_id, STUDY_CODE, STUDY_DESCRIPTION, META_VERSION, sysdate, null, TITLE, EXPLANATION from STUDY s WHERE s.STUDY_DESCRIPTION like 'PROMIS%');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (promis2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Bank v1.0 - Sleep Disturbance',0, CURRENT_TIMESTAMP, null, 'PROMIS Sleep Disturbance');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (promis2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Bank v1.0 - Sleep-Related Impairment',0, CURRENT_TIMESTAMP, null, 'PROMIS Sleep-Related Impairment');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (promis2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Bank v1.0 - Anger',0, CURRENT_TIMESTAMP, null, 'PROMIS Anger');

-- Local PROMIS CAT
insert into study (survey_system_id,study_code,study_description,meta_version,dt_created,dt_changed,title,explanation)
select local_promis_system_id,study_code,study_description,meta_version,sysdate,dt_changed,title,explanation
  from study where survey_system_id=promis2_system_id;

INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Ped Bank v1.0 - Pain Interference',0,CURRENT_TIMESTAMP,null,'PROMIS Pediatric Pain Interference');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Ped Bank v1.0 - Mobility',0,CURRENT_TIMESTAMP,null,'PROMIS Pediatric Mobility');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Ped Bank v1.0 - Fatigue',0,CURRENT_TIMESTAMP,null,'PROMIS Pediatric Fatigue');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Ped Bank v1.1 - Depressive Sx',0,CURRENT_TIMESTAMP,null,'PROMIS Pediatric Depression');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Ped Bank v1.1 - Anxiety',0,CURRENT_TIMESTAMP,null,'PROMIS Pediatric Anxiety');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Ped Bank v1.0 - Peer Rel',0,CURRENT_TIMESTAMP,null,'PROMIS Pediatric Peer Relations');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Parent Proxy Bank v1.0 - Pain Interference',0,CURRENT_TIMESTAMP,null,'PROMIS Parent Proxy Pain Interference');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Parent Proxy Bank v1.0 - Mobility',0,CURRENT_TIMESTAMP,null,'PROMIS Parent Proxy Mobility');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Parent Proxy Bank v1.0 - Fatigue',0,CURRENT_TIMESTAMP,null,'PROMIS Parent Proxy Fatigue');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Parent Proxy Bank v1.1 - Depressive Sx',0,CURRENT_TIMESTAMP,null,'PROMIS Parent Proxy Depression');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Parent Proxy Bank v1.1 - Anxiety',0,CURRENT_TIMESTAMP,null,'PROMIS Parent Proxy Anxiety');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED,TITLE) values (local_promis_system_id,STUDY_CODE_SEQ.NEXTVAL,'PROMIS Parent Proxy Bank v1.0 - Peer Relations',0,CURRENT_TIMESTAMP,null,'PROMIS Parent Proxy Peer Relations');

INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (cat2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'painIntensity',0, CURRENT_TIMESTAMP, null, 'PROMIS Pain Intensity');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (cat2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'painBehavior',0, CURRENT_TIMESTAMP, null, 'PROMIS Pain Behavior');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (cat2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'physicalFunction',0, CURRENT_TIMESTAMP, null, 'PROMIS Physical Function');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (cat2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'fatigue2',0, CURRENT_TIMESTAMP, null, 'PROMIS Fatigue');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (cat2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'depression2',0, CURRENT_TIMESTAMP, null, 'PROMIS Depression');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (cat2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'anxiety2',0, CURRENT_TIMESTAMP, null, 'PROMIS Anxiety');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (cat2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'sleepDisturbance',0, CURRENT_TIMESTAMP, null, 'PROMIS Sleep Disturbance');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (cat2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'sleepRelatedImpairment',0, CURRENT_TIMESTAMP, null, 'PROMIS Sleep-Related Impairment');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (cat2_system_id, STUDY_CODE_SEQ.NEXTVAL, 'anger',0, CURRENT_TIMESTAMP, null, 'PROMIS Anger');

INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Bank v2.0 - Emotional Support',0, CURRENT_TIMESTAMP, null, 'PROMIS Emotional Support');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Bank v2.0 - Instrumental Support',0, CURRENT_TIMESTAMP, null, 'PROMIS Instrumental Support');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Bank v2.0 - Satisfaction Roles Activities',0, CURRENT_TIMESTAMP, null, 'PROMIS Satisfaction Roles Activities');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Bank v2.0 - Social Isolation',0, CURRENT_TIMESTAMP, null, 'PROMIS Social Isolation');
INSERT INTO STUDY (SURVEY_SYSTEM_ID,STUDY_CODE,STUDY_DESCRIPTION,META_VERSION,DT_CREATED,DT_CHANGED, TITLE) VALUES (local_promis_system_id, STUDY_CODE_SEQ.NEXTVAL, 'PROMIS Bank v2.0 - Ability to Participate Social',0, CURRENT_TIMESTAMP, null, 'PROMIS Ability to Participate Social');

update study set explanation = 'Pain Interference, also known as <I>pain impact</I>, refers to the degree to which pain limits or interferes with individuals'' physical, mental and social activities. This domain is increasingly recognized as important for both understanding patients'' experiences and as a key outcome in pain management.<P><U>Overall Mean:</U> <B>[VAR_X]</B> ([VAR_Y] SD)<P><U>Most recent:</U> <B>[VAR_W]</B> ([VAR_Z] SD)
<P>Amtmann <I>et al, Pain</I>. 2010 Jul;150(1):173-82.' where study_description='PROMIS Pain Intensity Bank';
update study set explanation = 'Pain Behaviors include verbal complaints, non-language sounds, facial expressions, posturing and gesturing, and limitations in activities. They are intended to communicate the experience of pain. It provides insights into a person''s attempts to cope with or manage pain. 
<P><P><U>Overall Mean:</U> <B>[VAR_X]</B> ([VAR_Y] SD)
<P><U>Most recent:</U> <B>[VAR_W]</B> ([VAR_Z] SD)
<P>Revicki <I>et al, Pain</I>. 2009 Nov; 146(1-2):158-69.' where study_description='PROMIS Pain Behavior Bank';
update study set explanation = 'Physical Function reflects self-reported capability rather than actual performance of physical activities. This includes dexterity, mobility, truncal control, and instrumental activities of daily living such as running errands.
<P><P><U>Overall Mean:</U> <B>[VAR_X]</B> ([VAR_Y] SD)
<P><U>Most recent:</U> <B>[VAR_W]</B> ([VAR_Z] SD)
<P>Fries <I>et al, Arthritis Research & Therapy</I>. 2011; 13(5): R147.' where study_description='PROMIS Physical Function Bank';
update study set explanation = 'Fatigue is an overwhelming, debilitating and sustained sense of exhaustion that decreases one''s ability to carry out daily activities, including the ability to work effectively and to function at one''s usual level in family or social roles. <P><P><U>Overall Mean:</U> <B>[VAR_X]</B> ([VAR_Y] SD)
<P><U>Most recent:</U> <B>[VAR_W]</B> ([VAR_Z] SD)
<P>Christodoulou <I>et al, Quality of Life Research</I>. 2008 December; 17(10): 1239-1246.' where study_description='PROMIS Fatigue Bank';
update study set explanation = 'Depression and pain exist in a mutually reinforcing relationship, mediated by the patients'' appraisals of the effects of the pain on their lives, and appraisals of their ability to exert any control over their pain and lives. <P><P><U>Overall Mean:</U> <B>[VAR_X]</B> ([VAR_Y] SD)
<P><U>Most recent:</U> <B>[VAR_W]</B> ([VAR_Z] SD)
<P>Pilkonis <I>et al, Assessment</I>. 2011 September; 18(3): 263-283.'  where study_description='PROMIS Depression Bank';
update study set explanation = 'Anxiety enhances the pain experience, and exacerbates fear-avoidance beliefs by promoting the negative interpretation of bodily sensations. It is associated with distress, analgesic use, and physical and social functioning in patients. It is a risk factor for the maintenance and exacerbation of chronic pain and disability.
<P><P><U>Overall Mean:</U> <B>[VAR_X]</B> ([VAR_Y] SD)
<P><U>Most recent:</U> <B>[VAR_W]</B> ([VAR_Z] SD)
<P>Pilkonis <I>et al, Assessment</I>. 2011 September; 18(3): 263-283.' where study_description='PROMIS Anxiety Bank';
update study set explanation = 'Sleep disturbance is reported in over 70% of patients with chronic pain. Most patients report that sleep is interrupted due to pain and many develop unhealthy sleep patterns. Quality of sleep the night prior is one of the most important predictors of next day pain intensity.
<P><P><U>Overall Mean:</U> <B>[VAR_X]</B> ([VAR_Y] SD)
<P><U>Most recent:</U> <B>[VAR_W]</B> ([VAR_Z] SD)' where study_description='PROMIS Bank v1.0 - Sleep Disturbance';
update study set explanation = 'Sleep-related impairment assesses perceptions of alertness, sleepiness, and tiredness during usual waking hours, and the perceived functional impairments during wakefulness associated with sleep problems or impaired alertness.
<P><P><U>Overall Mean:</U> <B>[VAR_X]</B> ([VAR_Y] SD)
<P><U>Most recent:</U> <B>[VAR_W]</B> ([VAR_Z] SD)' where study_description='PROMIS Bank v1.0 - Sleep-Related Impairment';
update study set explanation = 'Anger in chronic pain may be attributable to enduring personality dispositions associated with conscious or unconscious conflicts, or a reaction to the recalcitrant symptoms that are either unsubstantiated by objective medical findings and/or unrelieved by medical treatment.
<P><P><U>Overall Mean:</U> <B>[VAR_X]</B> ([VAR_Y] SD)
<P><U>Most recent:</U> <B>[VAR_W]</B> ([VAR_Z] SD)' where study_description='PROMIS Bank v1.0 - Anger';


update study set explanation = 'Pain Intensity<P> The most recent response to "...pain right now" is <B>[LAST_3]</B> and the average is <B>[AVG_3]</B>. <P>The most recent response to "...average pain over the last 7 days" is <B>[LAST_2]</B> and the average is <B>[AVG_2]</B>.'
where study_description='painIntensity';
update study set explanation = 'There were [LAST] areas selected on the most recent body map' where study_description='bodymap';


insert into user_principal (user_principal_id, username, display_name, enabled) values(user_principal_sequence.nextval, 'admin', 'Admin Test-User', 'Y');
select user_principal_id into admin_user_id from user_principal where username = 'admin';

insert into user_authority values (admin_user_id, 'CLINIC_STAFF');
insert into user_authority values (admin_user_id, 'DATA_EXCHANGE');
insert into user_authority values (admin_user_id, 'DEVELOPER');
insert into user_authority values (admin_user_id, 'EDITOR');
insert into user_authority values (admin_user_id, 'REGISTRATION');
insert into user_authority values (admin_user_id, 'SECURITY');


insert into survey_site (survey_site_id, url_param, display_name) values (1, '1', 'Stanford Pain Management Center');
insert into survey_site (survey_site_id, url_param, display_name) values (2, 'test', 'Test Survey Client');
insert into survey_site (survey_site_id, url_param, display_name) values (3, 'stub', 'Test Stubbed Questions');
insert into survey_site (survey_site_id, url_param, display_name) values (4, 'sat', 'Patient Satisfaction');
insert into survey_site (survey_site_id, url_param, display_name) values (5, 'cat', 'Test Stanford CAT');
insert into survey_site (survey_site_id, url_param, display_name) values (6, 'ped', 'Pediatric Pain Management Clinic');

insert into patient_result_type (patient_res_typ_id, survey_site_id, result_name, result_title) values (1,1,'PARPT','Stanford Pain Management Center Outcomes');
insert into patient_result_type (patient_res_typ_id, survey_site_id, result_name, result_title) values (2,6,'PARPT','Pediatric Pain Management Clinic Outcomes');
insert into patient_result_type (patient_res_typ_id, survey_site_id, result_name, result_title) values (3,1,'PARPTJSON','Stanford Pain Management Center Outcomes');
insert into patient_result_type (patient_res_typ_id, survey_site_id, result_name, result_title) values (4,6,'PARPTJSON','Pediatric Pain Management Clinic Outcomes');
insert into patient_result_type (patient_res_typ_id, survey_site_id, result_name, result_title) values (5,1,'PARPTTEXT','Stanford Pain Management Center Outcomes');
insert into patient_result_type (patient_res_typ_id, survey_site_id, result_name, result_title) values (6,6,'PARPTTEXT','Pediatric Pain Management Clinic Outcomes');
END;
/

insert into survey_complete_push (survey_recipient_id, recipient_name, recipient_display_name, pushed_survey_sequence, failed_count, is_enabled)
values (1, 'pdf', 'Create PDF report', null, 0, 'Y')
/

commit
/
