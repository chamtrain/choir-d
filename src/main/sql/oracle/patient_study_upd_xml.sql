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


--set define off
--/

-- '.... updating patient_study.xml_results ....' 


declare myClob clob; myClobVar varchar2(32767) :='<Form/>';
local_system_id number(6);
local_promis_system_id number(6);
promis_system_id number(6);
promis2_system_id number(6);
pr1p_pin_study number(6);
pr1p_beh_study number(6);
pr1p_phf_study number(6);
pr1p_dep_study number(6);
pr1p_fat_study number(6);
pr1p_anx_study number(6);
local_names number(6);  local_bodymap number(6); local_painint number(6); local_painexp number(6);    local_stanford5 number(6);
local_day number(6);    local_work number(6);    local_disabil number(6); local_lawsuit number(6);    local_sleep number(6); 
local_psych number(6);  local_ptsd number(6);    local_health  number(6); local_background number(6); local_education number(6); 
local_quest number(6);  local_other number(6);   local_smoking number(6); local_alcohol number(6); 
begin 
SELECT survey_system_id INTO local_system_id FROM survey_system WHERE survey_system_name = 'Local';
SELECT survey_system_id INTO local_promis_system_id FROM survey_system WHERE survey_system_name = 'LocalPromis';
SELECT survey_system_id INTO promis_system_id FROM survey_system WHERE survey_system_name = 'PROMIS';
SELECT survey_system_id INTO promis2_system_id FROM survey_system WHERE survey_system_name = 'PROMIS.2';	
SELECT study_code INTO pr1p_pin_study FROM study where study_description='PROMIS Pain Intensity Bank' and survey_system_id = promis_system_id ;
SELECT study_code INTO pr1p_beh_study FROM study where study_description='PROMIS Pain Behavior Bank' and survey_system_id = promis_system_id ;
SELECT study_code INTO pr1p_phf_study FROM study where study_description='PROMIS Physical Function Bank' and survey_system_id = promis_system_id ;
SELECT study_code INTO pr1p_dep_study FROM study where study_description='PROMIS Depression Bank' and survey_system_id = promis_system_id ;
SELECT study_code INTO pr1p_fat_study FROM study where study_description='PROMIS Fatigue Bank' and survey_system_id = promis_system_id ;
SELECT study_code INTO pr1p_anx_study FROM study where study_description='PROMIS Anxiety Bank' and survey_system_id = promis_system_id ;
SELECT study_code INTO local_names FROM study where study_description='names' and survey_system_id = local_system_id;
SELECT study_code INTO local_bodymap FROM study where study_description='bodymap' and survey_system_id = local_system_id;
SELECT study_code INTO local_painint FROM study where study_description='painIntensity' and survey_system_id = local_system_id;
SELECT study_code INTO local_painexp FROM study where study_description='painExperience' and survey_system_id = local_system_id;
SELECT study_code INTO local_stanford5 FROM study where study_description='stanfordFive' and survey_system_id = local_system_id;
SELECT study_code INTO local_day FROM study where study_description='faDay' and survey_system_id = local_system_id;
SELECT study_code INTO local_work FROM study where study_description='faWorking' and survey_system_id = local_system_id;
SELECT study_code INTO local_disabil FROM study where study_description='faDisability' and survey_system_id = local_system_id;
SELECT study_code INTO local_lawsuit FROM study where study_description='faLawsuit' and survey_system_id = local_system_id;
SELECT study_code INTO local_sleep FROM study where study_description='sleepImpair' and survey_system_id = local_system_id;
SELECT study_code INTO local_psych FROM study where study_description='psychHistory' and survey_system_id = local_system_id;
SELECT study_code INTO local_ptsd FROM study where study_description='ptsd' and survey_system_id = local_system_id;
SELECT study_code INTO local_health FROM study where study_description='healthUtil' and survey_system_id = local_system_id;
SELECT study_code INTO local_background FROM study where study_description='background' and survey_system_id = local_system_id;
SELECT study_code INTO local_education FROM study where study_description='education' and survey_system_id = local_system_id;
SELECT study_code INTO local_quest FROM study where study_description='questions' and survey_system_id = local_system_id;
SELECT study_code INTO local_other FROM study where study_description='otherPainDocs' and survey_system_id = local_system_id;
SELECT study_code INTO local_smoking FROM study where study_description='smoking' and survey_system_id = local_system_id;
SELECT study_code INTO local_alcohol FROM study where study_description='alcohol' and survey_system_id = local_system_id;
-- Patient study without the xml results
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES  ('10003-2',local_system_id,local_names,787404111,'1e3fcca8-3158-4889-ab8e-c94c64195918',3,0,to_timestamp('2011-01-09 13:24:40.639', 'YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 13:53:45.534','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_bodymap,787404111,'f4709411-060c-453f-9c20-42efe3ff4e8b',4,0,to_timestamp('2011-01-09 13:24:40.786','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 13:53:56.072','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_painint,787404111,'335199b4-49a5-47e8-88bc-8ea208b183b8',5,0,to_timestamp('2011-01-09 13:24:41.121','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:16:14.793','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_painexp,787404111,'b7e7eea3-24e2-4f2f-a4b3-6af8b31a7b6e',7,0,to_timestamp('2011-01-09 13:24:41.578','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:16:51.541','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_stanford5,787404111,'0fe0815b-e034-4a16-99d2-b2d4df1e31ca',6,0,to_timestamp('2011-01-09 13:24:41.36','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:16:37.669','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_day,787404111,'4cfc87f0-91c7-400f-9fdf-f81b3f1c0acc',9,0,to_timestamp('2011-01-09 13:24:41.703','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:18:42.246','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES  ('10003-2',local_system_id,local_work,787404111,'1e3fcca8-3158-4889-ab8e-c94c64195918',3,0,to_timestamp('2011-01-09 13:24:40.639', 'YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 13:53:45.534','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_disabil,787404111,'f4709411-060c-453f-9c20-42efe3ff4e8b',4,0,to_timestamp('2011-01-09 13:24:40.786','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 13:53:56.072','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_lawsuit,787404111,'335199b4-49a5-47e8-88bc-8ea208b183b8',5,0,to_timestamp('2011-01-09 13:24:41.121','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:16:14.793','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_sleep,787404111,'b7e7eea3-24e2-4f2f-a4b3-6af8b31a7b6e',7,0,to_timestamp('2011-01-09 13:24:41.578','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:16:51.541','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_psych,787404111,'0fe0815b-e034-4a16-99d2-b2d4df1e31ca',6,0,to_timestamp('2011-01-09 13:24:41.36','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:16:37.669','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_ptsd,787404111,'4cfc87f0-91c7-400f-9fdf-f81b3f1c0acc',9,0,to_timestamp('2011-01-09 13:24:41.703','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:18:42.246','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES  ('10003-2',local_system_id,local_health,787404111,'1e3fcca8-3158-4889-ab8e-c94c64195918',3,0,to_timestamp('2011-01-09 13:24:40.639', 'YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 13:53:45.534','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_background,787404111,'f4709411-060c-453f-9c20-42efe3ff4e8b',4,0,to_timestamp('2011-01-09 13:24:40.786','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 13:53:56.072','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_education,787404111,'335199b4-49a5-47e8-88bc-8ea208b183b8',5,0,to_timestamp('2011-01-09 13:24:41.121','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:16:14.793','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_quest,787404111,'b7e7eea3-24e2-4f2f-a4b3-6af8b31a7b6e',7,0,to_timestamp('2011-01-09 13:24:41.578','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:16:51.541','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_other,787404111,'0fe0815b-e034-4a16-99d2-b2d4df1e31ca',6,0,to_timestamp('2011-01-09 13:24:41.36','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:16:37.669','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_smoking,787404111,'4cfc87f0-91c7-400f-9fdf-f81b3f1c0acc',9,0,to_timestamp('2011-01-09 13:24:41.703','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:18:42.246','YYYY-MM-DD HH24:MI:SS.FF3'), null);
INSERT INTO PATIENT_STUDY (PATIENT_ID, SURVEY_SYSTEM_ID, STUDY_CODE, TOKEN, EXTERNAL_REFERENCE_ID, ORDER_NUMBER, META_VERSION, DT_CREATED, DT_CHANGED, XML_RESULTS) VALUES ('10003-2',local_system_id,local_alcohol,787404111,'4cfc87f0-91c7-400f-9fdf-f81b3f1c0acc',9,0,to_timestamp('2011-01-09 13:24:41.703','YYYY-MM-DD HH24:MI:SS.FF3'), to_timestamp('2012-01-09 14:18:42.246','YYYY-MM-DD HH24:MI:SS.FF3'), null);

-- Now add the xml_results
myClobVar :='<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Description="Names" questionsPerPage="3">
<Items>
<Item ItemResponse="1" ItemScore="0" Order="1" TimeFinished="1385071582890">
<Description>What is the patient''s name?</Description>
<Responses><Response Order="1" Type="input" required="true">
<ref>PATIENTNAME</ref>
<format><datatype>text</datatype><lines>1</lines><charwidth>80</charwidth></format>
<hint>Enter your name if you are filling out this survey for yourself,or the patient''s name if you are assisting someone else.</hint>
<value>Teresatest</value>
</Response>
</Responses>
</Item>
<Item ItemResponse="1" ItemScore="0" Order="2" TimeFinished="1385071582893">
<Description>Is someone helping complete this questionnaire?</Description>
<Responses>
<Response Order="1" Type="select1" required="true">
<ref>ASSISTED</ref>
<item selected="false">
<label>No</label>
<value>1</value>
</item>
<item selected="true">
<label>Yes</label>
<value>2</value>
<onselect Type="Item" Value="3" Where="Order">
<Set Name="RequiredMin" Type="attribute" Value="1"/>
<Set Name="visible" Type="state" Value="true"/>
</onselect>
<ondeselect Type="Item" Value="3" Where="Order">
<Set Name="RequiredMin" Type="attribute" Value="0"/>
<Set Name="visible" Type="state" Value="false"/>
</ondeselect>
</item>
</Response>
</Responses>
</Item>
<Item ItemResponse="1" ItemScore="0" Order="3" RequiredMin="0" TimeFinished="1385071582902" Visible="false">
<Description>Who is helping?</Description>
<Responses>
<Response Order="1" Type="select1" required="true">
<ref>HELPER</ref>
<item selected="false">
<label>Spouse</label>
<value>1</value>
</item>
<item selected="false">
<label>Parent</label>
<value>2</value>
</item>
<item selected="false">
<label>Child</label>
<value>3</value>
</item>
<item selected="false">
<label>Other Family</label>
<value>4</value>
</item>
<item selected="false">
<label>Translator</label>
<value>5</value>
</item>
<item selected="true">
<label>Clinic Staff</label>
<value>6</value>
</item>
<item selected="false">
<label>Someone Else</label>
<value>7</value>
</item>
</Response>
</Responses>
</Item>
</Items>
</Form>'; 
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_names and token = 787404111  ;
myClobVar :='<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<Form DateFinished="" DateStarted="" Description="bodymap">
<Items>
<Item Align="Image" Class="surveyQuestionBodymap" ItemResponse="123,223,222" ItemScore="3" Order="2" RequiredMax="100" RequiredMin="1" TimeFinished="1383859409659" TimeStarted="">
<PatientAttribute condition="notequal" data_name="gender" data_type="string" data_value="Male"/>
<Description><![CDATA[Select the areas where you are experiencing pain]]></Description>
<img alt="Female front image" border="0" height="580" id="female_front" name="female_front" src="images/maps/female_front.071813.cache.png" usemap="#m_female_front" width="280"/>
<map Order="1" class="imgmap" id="m_female_front" name="m_female_front">
<area alt="136" coords="149,536,148,542,150,557,147,564,150,568,155,568,158,566,171,566,174,564,177,564,178,562,172,556,168,548,168,541,167,536,163,538,160,538,149,536" href="#ff36" id="136" shape="poly"/>
<area alt="135" coords="113,537,111,541,110,549,106,557,101,562,101,563,106,565,109,567,122,566,129,568,132,563,129,555,129,544,131,536,125,538,119,538,113,537" href="#ff35" id="135" shape="poly"/>
<area alt="134" coords="149,535,152,526,151,504,156,502,167,502,173,506,167,533,167,536,163,538,155,538,149,535" href="#ff34" id="134" shape="poly"/>
<area alt="133" coords="105,506,111,502,124,503,127,504,127,526,130,533,129,536,123,538,115,538,113,536,108,515,105,506" href="#ff33" id="133" shape="poly"/>
<area alt="132" coords="151,503,150,486,150,449,151,434,156,436,165,437,172,434,178,430,181,436,182,441,182,462,180,474,177,490,173,505,166,502,161,502,153,503,151,503" href="#ff32" id="132" shape="poly"/>
<area alt="131" coords="100,430,97,448,97,462,100,481,106,506,112,502,117,502,124,502,127,504,129,486,129,449,129,446,128,434,123,436,114,437,106,434,100,430" href="#ff31" id="131" shape="poly"/>
<area alt="130" coords="151,434,147,412,146,401,158,397,166,396,168,395,176,397,179,399,178,421,179,429,171,435,164,436,157,436,151,434" href="#ff30" id="130" shape="poly"/>
<area alt="129" coords="100,429,100,403,99,398,104,396,111,396,114,395,121,397,128,399,133,401,132,412,130,423,129,431,128,435,121,437,114,436,106,433,100,429" href="#ff29" id="129" shape="poly"/>
<area alt="127" coords="146,401,143,380,143,343,141,319,140,312,140,305,144,304,148,302,154,296,159,289,165,284,171,281,175,280,180,289,185,297,190,304,194,307,194,318,192,336,187,360,183,382,180,391,179,398,174,396,168,396,161,397,154,397,146,401" href="#ff27" id="127" shape="poly"/>
<area alt="126" coords="99,397,88,345,84,317,85,308,99,289,104,279,115,286,121,291,130,301,135,304,139,305,139,312,135,340,135,380,133,400,124,398,112,395,103,397,99,397" href="#ff26" id="126" shape="poly"/>
<area alt="128" coords="218,297,218,315,219,320,220,338,221,343,223,343,224,327,231,347,234,347,235,346,237,347,239,346,238,326,241,339,242,343,245,345,245,339,243,327,242,311,251,319,257,318,258,315,250,309,245,303,235,293,230,298,227,300,222,299,218,297" href="#ff28" id="128" shape="poly"/>
<area alt="125" coords="22,318,22,315,28,310,31,305,44,293,51,300,57,300,61,298,61,314,60,321,60,337,58,343,57,344,56,343,55,327,49,347,46,347,45,346,42,347,41,347,41,327,38,340,35,345,34,345,34,339,36,328,37,311,28,319,22,318" href="#ff25" id="125" shape="poly"/>
<area alt="119" coords="44,293,46,287,50,273,60,273,65,275,68,278,63,291,61,297,57,299,52,300,44,293" href="#ff19" id="119" shape="poly"/>
<area alt="124" coords="211,278,218,296,223,299,227,299,235,293,229,273,220,273,214,275,211,278" href="#ff24" id="124" shape="poly"/>
<area alt="123" coords="183,245,178,251,175,257,173,260,173,269,175,279,181,290,187,299,194,307,194,281,189,259,183,245" href="#ff23" id="123" shape="poly"/>
<area alt="122" coords="175,276,165,276,159,272,154,268,150,262,145,259,142,258,140,258,140,304,143,304,148,302,153,297,156,292,161,287,167,283,172,280,175,279,175,276" href="#ff22" id="122" shape="poly"/>
<area alt="121" coords="140,258,133,259,128,263,124,268,120,273,114,275,105,275,104,279,114,284,119,288,127,297,133,303,135,304,139,305,140,258" href="#ff21" id="121" shape="poly"/>
<area alt="120" coords="96,245,87,268,84,284,85,307,97,292,102,282,105,274,106,260,102,251,96,245" href="#ff20" id="120" shape="poly"/>
<area alt="118" coords="228,272,220,235,218,224,215,218,207,223,202,224,197,224,191,223,188,222,191,232,195,245,202,261,211,278,214,275,220,273,228,272" href="#ff18" id="118" shape="poly"/>
<area alt="116" coords="215,218,213,213,210,210,211,204,203,200,194,200,187,205,187,217,188,221,192,223,198,223,202,223,209,222,215,218" href="#ff16" id="116" shape="poly"/>
<area alt="114" coords="205,164,210,202,202,200,194,199,186,205,181,176,181,160,183,155,184,154,187,154,193,158,203,163,205,164" href="#ff14" id="114" shape="poly"/>
<area alt="113" coords="140,182,152,183,158,188,166,193,178,193,181,191,177,210,175,219,176,226,183,246,177,250,173,258,172,262,173,270,174,275,165,275,158,272,150,262,143,258,140,257,140,182" href="#ff13" id="113" shape="poly"/>
<area alt="112" coords="96,244,103,226,103,217,98,191,103,193,110,193,116,192,124,186,130,183,133,182,139,182,140,258,133,258,129,262,125,268,119,272,113,275,105,275,106,260,102,251,96,244" href="#ff12" id="112" shape="poly"/>
<area alt="117" coords="50,272,56,245,62,223,64,218,71,222,88,223,91,221,90,228,88,236,83,246,78,258,73,270,69,277,63,274,58,273,50,272" href="#ff17" id="117" shape="poly"/>
<area alt="115" coords="64,218,67,213,69,210,69,203,78,200,85,200,92,205,91,220,87,222,72,222,64,218" href="#ff15" id="115" shape="poly"/>
<area alt="111" coords="69,201,71,186,73,171,74,164,83,160,90,156,93,153,96,156,98,160,97,188,94,203,91,204,87,200,81,199,75,200,70,202,69,201" href="#ff11" id="111" shape="poly"/>
<area alt="110" coords="205,163,195,159,188,154,179,147,172,137,164,127,159,116,155,108,151,100,156,98,160,102,167,107,173,111,179,113,186,114,192,117,196,120,200,125,202,132,204,141,205,148,205,163" href="#ff10" id="110" shape="poly"/>
<area alt="109" coords="151,101,146,103,140,103,140,180,147,182,153,184,157,186,163,191,167,193,178,193,181,191,182,186,181,178,181,165,181,161,182,156,183,154,186,153,180,147,175,142,169,134,160,121,156,111,151,101" href="#ff9" id="109" shape="poly"/>
<area alt="108" coords="139,103,139,180,132,182,127,183,122,186,118,189,114,192,104,192,99,191,97,188,98,171,97,158,95,154,94,153,103,143,111,132,117,123,122,113,127,104,128,101,134,103,139,103" href="#ff8" id="108" shape="poly"/>
<area alt="107" coords="122,97,117,103,110,108,101,111,93,113,87,116,83,119,80,122,78,128,75,138,74,150,74,164,85,158,93,153,99,147,105,140,110,133,115,125,120,117,126,105,128,101,122,97" href="#ff7" id="107" shape="poly"/>
<area alt="106" coords="140,79,140,102,145,102,151,100,155,98,155,77,147,79,140,79" href="#ff6" id="106" shape="poly"/>
<area alt="105" coords="123,77,132,78,139,79,139,102,134,102,128,100,123,97,123,77" href="#ff5" id="105" shape="poly"/>
<area alt="102" coords="140,13,150,15,156,17,160,22,163,27,149,31,140,31,140,13" href="#ff2" id="102" shape="poly"/>
<area alt="101" coords="116,27,118,23,121,20,125,17,129,15,134,14,139,13,139,31,132,31,124,30,116,27" href="#ff1" id="101" shape="poly"/>
<area alt="104" coords="163,28,154,30,146,32,140,32,140,78,147,78,155,76,158,72,160,68,161,64,162,62,168,53,169,49,169,45,168,43,164,44,164,31,163,28" href="#ff4" id="104" shape="poly"/>
<area alt="103" coords="139,78,126,77,122,76,118,66,117,61,111,55,110,47,110,45,112,43,114,43,115,44,115,30,116,28,123,30,125,30,133,31,139,32,139,78" href="#ff3" id="103" shape="poly"/>
</map>
<img alt="Female back image" border="0" height="568" id="female_back" name="female_back" src="images/maps/female_back.071813.cache.png" usemap="#m_female_back" width="263"/>
<map Order="2" id="m_female_back" name="m_female_back">
<area alt="238" coords="139,521,139,529,140,537,138,542,137,545,137,549,140,551,144,552,147,549,159,550,162,547,166,547,167,545,163,543,160,539,156,529,156,524,155,523,155,521,152,522,145,522,139,521" href="#fb38" id="238" shape="poly"/>
<area alt="237" coords="104,520,102,522,102,526,101,532,99,537,96,542,93,545,93,548,97,548,99,550,112,550,115,551,118,552,121,549,122,547,121,543,119,539,119,528,121,526,120,520,114,522,107,523,104,520" href="#fb37" id="237" shape="poly"/>
<area alt="236" coords="138,520,140,513,140,511,141,489,145,488,145,487,154,487,159,490,162,491,159,502,157,513,155,516,155,519,151,522,147,523,138,520" href="#fb36" id="236" shape="poly"/>
<area alt="235" coords="103,520,102,517,100,504,97,491,101,489,103,487,113,487,118,490,117,509,119,515,120,519,116,521,112,523,108,522,103,520" href="#fb35" id="235" shape="poly"/>
<area alt="234" coords="140,488,139,469,138,440,139,437,140,423,147,424,154,424,161,420,167,417,170,428,170,435,170,450,167,467,163,483,162,491,157,489,153,488,146,488,140,488" href="#fb34" id="234" shape="poly"/>
<area alt="233" coords="97,491,91,468,89,454,88,430,90,422,91,418,94,419,102,423,115,424,118,422,119,432,120,446,120,448,120,463,119,476,118,489,113,488,104,487,97,491" href="#fb33" id="233" shape="poly"/>
<area alt="232" coords="140,423,138,410,136,398,135,389,143,388,149,386,163,386,167,388,167,393,166,415,168,418,159,422,151,424,147,424,140,423" href="#fb32" id="232" shape="poly"/>
<area alt="231" coords="92,418,93,409,93,396,91,387,96,385,104,386,113,386,123,390,121,400,120,411,118,422,112,424,106,424,97,420,92,418" href="#fb31" id="231" shape="poly"/>
<area alt="230" coords="204,290,203,301,206,316,207,332,208,334,210,334,211,319,214,329,217,338,219,338,221,337,222,338,224,338,223,319,226,329,230,335,231,332,228,317,228,304,233,307,237,310,241,310,243,308,240,306,235,301,231,294,221,285,218,289,214,291,207,292,204,290" href="#fb30" id="230" shape="poly"/>
<area alt="229" coords="135,389,134,379,133,339,132,318,131,306,131,305,138,308,150,309,163,306,171,303,179,300,182,300,182,315,179,332,176,346,173,361,171,373,168,387,161,386,155,385,144,387,135,389" href="#fb29" id="229" shape="poly"/>
<area alt="228" coords="123,390,112,386,95,386,91,387,86,362,82,344,79,326,77,308,77,298,90,304,101,307,105,308,124,308,128,305,127,326,125,342,126,371,123,390" href="#fb28" id="228" shape="poly"/>
<area alt="227" coords="27,296,38,286,44,291,52,291,54,290,54,306,52,311,53,329,51,331,51,334,50,335,49,333,48,319,46,326,45,331,42,337,41,339,41,339,40,339,38,337,36,338,34,336,35,324,33,328,31,333,30,335,27,336,28,331,31,318,31,310,31,303,29,305,25,309,22,310,17,310,17,307,19,305,23,303,24,299,27,296" href="#fb27" id="227" shape="poly"/>
<area alt="226" coords="205,290,197,272,201,269,204,267,207,266,216,266,220,283,220,285,219,288,216,290,215,292,208,291,205,290" href="#fb26" id="226" shape="poly"/>
<area alt="224" coords="163,250,162,254,161,268,165,275,170,284,174,291,182,299,167,304,154,307,148,309,138,308,131,306,130,304,130,256,136,255,149,253,158,251,161,250,163,250" href="#fb24" id="224" shape="poly"/>
<area alt="223" coords="78,299,91,303,105,308,124,307,128,305,129,303,130,256,118,254,111,253,102,252,99,251,96,251,97,256,97,265,95,271,92,279,88,285,84,291,78,299" href="#fb23" id="223" shape="poly"/>
<area alt="225" coords="170,240,175,250,178,260,181,271,182,282,182,299,178,295,173,289,168,280,164,272,162,267,162,253,164,249,166,244,170,240" href="#fb25" id="225" shape="poly"/>
<area alt="222" coords="77,298,77,275,78,265,82,255,86,245,88,240,94,245,96,251,97,256,98,263,96,269,93,277,89,283,85,289,81,294,77,298" href="#fb22" id="222" shape="poly"/>
<area alt="220" coords="175,216,178,224,184,241,191,258,197,271,202,267,207,266,215,265,210,241,205,222,202,215,202,214,194,217,181,217,175,216" href="#fb20" id="220" shape="poly"/>
<area alt="221" coords="37,285,41,272,43,266,53,266,57,267,62,272,56,282,54,290,51,292,44,291,40,288,37,285" href="#fb21" id="221" shape="poly"/>
<area alt="219" coords="171,239,166,245,163,251,144,254,135,255,130,255,130,217,149,217,158,216,159,214,164,215,165,223,171,239" href="#fb19" id="219" shape="poly"/>
<area alt="218" coords="88,240,95,221,95,215,109,217,129,217,129,255,111,254,102,252,97,251,93,245,88,240" href="#fb18" id="218" shape="poly"/>
<area alt="217" coords="44,265,50,233,54,221,56,214,63,217,78,217,83,216,82,222,78,232,74,244,68,257,62,271,55,266,48,265,44,265" href="#fb17" id="217" shape="poly"/>
<area alt="216" coords="201,213,202,211,197,207,197,199,192,197,189,196,182,196,174,200,174,213,176,216,179,217,194,218,198,215,201,213" href="#fb16" id="216" shape="poly"/>
<area alt="214" coords="196,198,196,190,193,168,192,161,189,160,182,156,174,150,171,153,169,160,169,173,170,186,174,200,179,197,182,196,190,196,193,198,196,198" href="#fb14" id="214" shape="poly"/>
<area alt="213" coords="171,153,156,155,130,155,130,216,147,216,157,215,164,214,166,201,169,191,170,186,169,175,169,172,169,159,171,153" href="#fb13" id="213" shape="poly"/>
<area alt="212" coords="129,216,129,154,106,154,96,153,88,153,90,159,89,179,88,185,93,204,95,214,110,216,129,216" href="#fb12" id="212" shape="poly"/>
<area alt="215" coords="57,213,58,210,60,207,61,206,62,198,64,198,65,197,68,197,69,195,77,196,80,198,83,200,83,216,78,217,65,217,57,213" href="#fb15" id="215" shape="poly"/>
<area alt="211" coords="61,198,63,186,65,173,67,165,67,161,73,159,76,157,80,155,83,152,85,151,88,154,90,161,89,177,88,184,87,190,85,196,84,200,80,197,76,196,69,196,64,197,61,198" href="#fb11" id="211" shape="poly"/>
<area alt="210" coords="192,161,192,145,190,130,187,123,184,118,179,115,172,112,164,110,157,107,150,101,146,97,140,100,145,110,148,115,150,120,154,127,159,133,163,139,166,142,170,147,175,151,181,155,186,159,190,161,192,161" href="#fb10" id="210" shape="poly"/>
<area alt="209" coords="173,150,170,152,152,155,130,154,130,101,134,101,138,100,140,100,142,105,145,112,150,121,156,129,160,136,163,139,166,143,173,150" href="#fb9" id="209" shape="poly"/>
<area alt="208" coords="129,102,129,154,110,155,97,153,88,153,85,150,92,144,96,138,100,134,103,128,107,124,109,119,111,116,113,112,115,108,117,103,118,103,117,99,123,101,129,102" href="#fb8" id="208" shape="poly"/>
<area alt="207" coords="114,97,108,102,100,107,91,111,81,114,74,118,71,122,69,129,67,140,67,161,75,157,80,153,86,149,92,143,97,137,102,131,106,124,110,117,113,111,116,105,117,102,117,99,114,97" href="#fb7" id="207" shape="poly"/>
<area alt="206" coords="145,97,145,77,136,78,130,79,130,101,136,101,142,99,145,97" href="#fb6" id="206" shape="poly"/>
<area alt="205" coords="114,97,119,99,124,101,129,101,129,79,126,78,118,77,114,77,114,97" href="#fb5" id="205" shape="poly"/>
<area alt="204" coords="130,78,138,77,145,76,149,70,151,65,151,62,152,61,156,55,158,52,158,45,153,45,153,29,146,31,136,32,130,33,130,78" href="#fb4" id="204" shape="poly"/>
<area alt="203" coords="129,78,129,33,119,33,111,31,107,29,105,31,105,45,104,44,102,45,101,46,100,51,103,56,106,60,109,63,109,68,110,73,114,76,124,78,129,78" href="#fb3" id="203" shape="poly"/>
<area alt="202" coords="151,29,149,25,145,20,140,17,133,15,129,15,130,32,139,32,147,30,151,29" href="#fb2" id="202" shape="poly"/>
<area alt="201" coords="108,25,107,28,115,31,120,32,129,33,129,15,120,15,114,19,110,23,108,25" href="fb1" id="201" shape="poly"/>
</map>
<Responses>
<Response Description="23" Order="123" Type="map" id="23" value="0"><Scores><Score value="1"/></Scores></Response>
<Response Description="22" Order="222" Type="map" id="22" value="0"><Scores><Score value="1"/></Scores></Response>
<Response Description="23" Order="223" Type="map" id="23" value="0"><Scores><Score value="1"/></Scores></Response>
</Responses>
<Heading center="" class="imageHeading" left="Front" right="Back"/>
</Item>
</Items>
</Form>'; 
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_bodymap and token = 787404111;
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<Form DateFinished="" DateStarted="" Description="painIntensity">
<Items>     
<Item Align="Horizontal" Class="surveyQuestionHorizontal" ItemResponse="4" ItemScore="4" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383859413968" TimeStarted="">     
<Description><![CDATA[In the past 7 days...<BR>How intense was your pain at its<U>worst</U>?]]></Description>        
<Responses><!--default: DescriptionPosition="below" --> 
<Response Class="surveyAnswerHorizontal" Description="0" Description2="No Pain" Order="0" Type="radio"><Scores><Score value="0"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="1" Order="1" Type="radio"><Scores><Score value="1"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="2" Order="2" Type="radio"><Scores><Score value="2"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="3" Order="3" Type="radio"><Scores><Score value="3"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="4" Order="4" Type="radio"><Scores><Score value="4"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="5" Order="5" Type="radio"><Scores><Score value="5"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="6" Order="6" Type="radio"><Scores><Score value="6"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="7" Order="7" Type="radio"><Scores><Score value="7"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="8" Order="8" Type="radio"><Scores><Score value="8"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="9" Order="9" Type="radio"><Scores><Score value="9"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="10" Description2="Pain as bad as you can imagine" Order="10" Type="radio"><Scores><Score value="10"/></Scores></Response>        
</Responses>     
</Item>
<Item Align="Horizontal" Class="surveyQuestionHorizontal" ItemResponse="2" ItemScore="2" Order="2" RequiredMax="1" RequiredMin="1" TimeFinished="1383859418613" TimeStarted="">      
<Description><![CDATA[In the past 7 days...<BR>How intense was your<U>average</U> pain?]]></Description>        
<Responses> 
<Response Class="surveyAnswerHorizontal" Description="0" Description2="No Pain" Order="0" Type="radio"><Scores><Score value="0"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="1" Order="1" Type="radio"><Scores><Score value="1"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="2" Order="2" Type="radio"><Scores><Score value="2"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="3" Order="3" Type="radio"><Scores><Score value="3"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="4" Order="4" Type="radio"><Scores><Score value="4"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="5" Order="5" Type="radio"><Scores><Score value="5"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="6" Order="6" Type="radio"><Scores><Score value="6"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="7" Order="7" Type="radio"><Scores><Score value="7"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="8" Order="8" Type="radio"><Scores><Score value="8"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="9" Order="9" Type="radio"><Scores><Score value="9"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="10" Description2="Pain as bad as you can imagine" Order="10" Type="radio"><Scores><Score value="10"/></Scores></Response>       
</Responses>        
<Footing center="" left="" right=""/>Footing center="" left="" right=""/>    
</Item>    
<Item Align="Horizontal" Class="surveyQuestionHorizontal" ItemResponse="2" ItemScore="2" Order="3" RequiredMax="1" RequiredMin="1" TimeFinished="1383859425994" TimeStarted="">       
<Description><![CDATA[What is your level of pain<U>right now</U>?<BR>]]></Description>       
<Responses> 
<Response Class="surveyAnswerHorizontal" Description="0" Description2="No Pain" Order="0" Type="radio"><Scores><Score value="0"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="1" Order="1" Type="radio"><Scores><Score value="1"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="2" Order="2" Type="radio"><Scores><Score value="2"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="3" Order="3" Type="radio"><Scores><Score value="3"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="4" Order="4" Type="radio"><Scores><Score value="4"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="5" Order="5" Type="radio"><Scores><Score value="5"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="6" Order="6" Type="radio"><Scores><Score value="6"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="7" Order="7" Type="radio"><Scores><Score value="7"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="8" Order="8" Type="radio"><Scores><Score value="8"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="9" Order="9" Type="radio"><Scores><Score value="9"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="10" Description2="Pain as bad as you can imagine" Order="10" Type="radio"><Scores><Score value="10"/></Scores></Response>        
</Responses>      
</Item>
<Item Align="Horizontal" Class="surveyQuestionHorizontal" ItemResponse="1" ItemScore="1" Order="4" RequiredMax="1" RequiredMin="1" TimeFinished="1383859430706" TimeStarted="">        
<Description><![CDATA[In the past 7 days...<BR>How intense was your pain at its<U>least</U>?]]></Description>         
<Responses> 
<Response Class="surveyAnswerHorizontal" Description="0" Description2="No" Description3="Pain" Order="0" Type="radio"><Scores><Score value="0"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="1" Order="1" Type="radio"><Scores><Score value="1"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="2" Order="2" Type="radio"><Scores><Score value="2"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="3" Order="3" Type="radio"><Scores><Score value="3"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="4" Order="4" Type="radio"><Scores><Score value="4"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="5" Order="5" Type="radio"><Scores><Score value="5"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="6" Order="6" Type="radio"><Scores><Score value="6"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="7" Order="7" Type="radio"><Scores><Score value="7"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="8" Order="8" Type="radio"><Scores><Score value="8"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="9" Order="9" Type="radio"><Scores><Score value="9"/></Scores></Response> 
<Response Class="surveyAnswerHorizontal" Description="10" Description2="Pain as bad as" Description3="you can imagine" Order="10" Type="radio"><Scores><Score value="10"/></Scores></Response>        
</Responses>    
</Item>
</Items>
</Form>';
myclob := myClobVar;
update patient_study set xml_results = xmltype(myclob) WHERE study_code = local_painint and token = 787404111;
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Pain Experience" questionsPerPage="1"> 
<Items class="surveyInline">       
 <Item Align="Horizontal" Class="registrySurvey" ItemResponse="3,2,1" ItemScore="0" Order="1" RequiredMax="3" RequiredMin="1" TimeFinished="1383860237408"><Description><![CDATA[How long have you had your pain problem?]]></Description><Alert>Please enter a valid integer.</Alert><Responses>
 <Response Class="registrySurveyAnswer" Order="1" Type="input" required="false"><ref>PAINDURATION</ref><label>Years</label> <location>Right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Enter an integer value</hint><Scores/></Response>
 <Response Class="registrySurveyAnswer" Order="2" Type="input" required="false"><ref>PAINDURATION</ref><label>Months</label><location>Right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Enter an integer value</hint><Scores/><value>11</value></Response>
 <Response Class="registrySurveyAnswer" Order="3" Type="input" required="false"><ref>PAINDURATION</ref><label>Days</label><location>Right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Enter an integer value</hint><Scores/></Response></Responses></Item> 
<Item Align="Horizontal" Class="registrySurvey" DescriptionPosition="above" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="100" RequiredMin="1" TimeFinished="1383860255100"><Description><![CDATA[Briefly describe how your pain started]]></Description><Responses> <Response Align="Vertical" Class="surveyAugText" Order="1" Type="input" required="true"><Scores/><label/><location>above</location><format><datatype>text</datatype><lines>5</lines><charwidth>80</charwidth></format> <value>Fall on slippery ice</value></Response></Responses></Item>  
<Item Align="Horizontal" Class="registrySurvey" DescriptionPosition="above" ItemResponse="2,1" ItemScore="0" Order="3" RequiredMax="100" RequiredMin="1" TimeFinished="1383860271298"><Description><![CDATA[Describe your current pain]]></Description> <Responses><Response Appearance="full" Class="registrySurveyCBInline" Order="1" Type="select" align="horizontal"><ref>PAINQUALITY</ref><width>295px</width>
<item selected="true"><label>Throbbing</label><value>1</value></item><item selected="false"><label>Shooting</label><value>2</value></item>  
<item selected="false"><label>Stabbing</label><value>3</value></item><item selected="false"><label>Sharp</label><value>4</value></item> 
<item selected="false"><label>Cramping</label><value>5</value></item><item selected="false"><label>Gnawing</label><value>6</value></item>    
<item selected="false"><label>Hot</label><value>7</value></item><item selected="false"><label>Burning</label><value>8</value></item>
<item selected="true"><label>Aching</label><value>9</value></item> </Response><Response Appearance="full" Class="registrySurveyCBInline" Order="2" Type="select" align="horizontal"><ref>PAINQUALITY</ref>          
<item selected="false"><label>Heavy</label><value>10</value></item><item selected="false"><label>Tender</label><value>11</value></item>
<item selected="false"><label>Splitting</label><value>12</value></item><item selected="true"><label>Tiring</label><value>13</value></item>
<item selected="true"><label>Exhausting</label><value>14</value></item><item selected="false"><label>Sickening</label><value>15</value></item>    
<item selected="false"><label>Fearful</label><value>16</value></item><item selected="false"><label>Punishing</label><value>17</value></item>
<item selected="true"><label>Cruel</label><value>18</value></item> </Response></Responses></Item>  
<Item Align="Horizontal" Class="registrySurvey" DescriptionPosition="above" ItemResponse="2,1" ItemScore="0" Order="4" RequiredMax="100" RequiredMin="1" TimeFinished="1383860275960" required="true"><Description><![CDATA[Please describe the timing of your pain]]></Description><Responses><Response Appearance="full" Class="registrySurveyCBInline" Order="1" Type="select" align="vertical"><ref>PAINTIME</ref><width>295px</width>       
<item selected="false"><label>Brief</label><value>1</value></item><item selected="false"><label>Constant</label><value>2</value></item>
<item selected="false"><label>Comes and goes</label><value>3</value></item><item selected="false"><label>Continuous</label><value>4</value></item>         
<item selected="true"><label>Always there</label><value>5</value></item><item selected="false"><label>Appears and disappears</label><value>6</value></item>         
<item selected="false"><label>Intermittent</label><value>7</value></item></Response><Response Align="Vertical" Class="registrySurvey" Order="2" Type="input" required="false"><Scores/><ref>PAINTIME</ref><ref>text</ref><label>and/or</label><location>above</location><format><datatype>text</datatype> <lines>5</lines><charwidth>80</charwidth></format><hint>Optional: you may enter additional descriptions</hint></Response></Responses></Item>  
<Item Align="Horizontal" Class="registrySurvey" DescriptionPosition="above" ItemResponse="2,1" ItemScore="0" Order="5" RequiredMax="100" RequiredMin="1" TimeFinished="1383860292622" required="true"><Description><![CDATA[What do you do to ease or relieve your pain?]]></Description><Responses><Response Appearance="full" Class="registrySurveyCBInline" Order="1" Type="select" align="vertical"><ref>PAINRELIEF</ref><width>295px</width> 
<item selected="false"><label>Nothing</label><value>1</value></item>  
<item selected="false"><label>Acupuncture</label><value>2</value></item>  
<item selected="false"><label>Bedrest</label><value>3</value></item>  
<item selected="false"><label>Chiropractic treatments</label><value>4</value></item>  
<item selected="false"><label>Dark room</label><value>5</value></item>  
<item selected="false"><label>Exercise</label><value>6</value></item>  
<item selected="false"><label>Massage</label><value>7</value></item> 
<item selected="false"><label>Medications</label><value>8</value></item> 
<item selected="false"><label>Heat</label><value>9</value></item> 
<item selected="true"><label>Ice pack</label><value>10</value></item> 
<item selected="false"><label>Movement</label><value>11</value></item> 
<item selected="false"><label>Physical therapy</label><value>12</value></item> 
<item selected="false"><label>Quiet room</label><value>13</value></item> 
<item selected="true"><label>Sitting</label><value>14</value></item> 
<item selected="false"><label>Standing</label><value>15</value></item> 
<item selected="false"><label>Walking</label><value>16</value></item></Response>
<Response Align="Vertical" Class="surveyAugText" Order="2" Type="input" required="false"><Scores/><ref>PAINRELIEF</ref><label>and/or</label><location>above</location><format><datatype>text</datatype><lines>5</lines><charwidth>80</charwidth></format>
<hint>Optional: you may enter additional descriptions</hint></Response></Responses></Item>  
<Item Align="Horizontal" Class="registrySurvey" DescriptionPosition="above" ItemResponse="2,1" ItemScore="0" Order="6" RequiredMax="100" RequiredMin="1" TimeFinished="1383860307459" required="true">&gt;<Description><![CDATA[What makes your pain worse?]]></Description><Responses><Response Appearance="full" Class="registrySurveyCBInline" Order="1" Type="select" align="vertical"><ref>PAINWORSE</ref><width>295px</width><!--  px --><item selected="false"><label>Nothing</label><value>1</value></item><item selected="false"><label>Acupuncture</label><value>2</value></item><item selected="false"><label>Bedrest</label><value>3</value></item><item selected="false"><label>Chiropractic treatments</label><value>4</value></item><item selected="false"><label>Dark room</label><value>5</value></item><item selected="true"><label>Exercise</label><value>6</value></item><item selected="false"><label>Massage</label><value>7</value></item><item selected="false"><label>Medications</label><value>8</value></item><item selected="false"><label>Heat</label><value>9</value></item><item selected="false"><label>Ice pack</label><value>10</value></item><item selected="false"><label>Movement</label><value>11</value></item><item selected="false"><label>Physical therapy</label><value>12</value></item><item selected="false"><label>Quiet room</label><value>13</value></item><item selected="false"><label>Sitting</label><value>14</value></item><item selected="true"><label>Standing</label><value>15</value></item><item selected="true"><label>Walking</label><value>16</value></item><item selected="false"><label>Stress</label><value>17</value></item></Response><Response Align="Vertical" Class="surveyAugText" Order="2" Type="input" required="false"><Scores/><ref>PAINWORSE</ref><label>and/or</label><location>above</location><format><datatype>text</datatype><lines>5</lines><charwidth>80</charwidth></format><hint>Optional: you may enter additional descriptions</hint></Response></Responses></Item> 
</Items>
</Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_painexp and token = 787404111;
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Stanford Five" questionsPerPage="1"> <Items class="surveyInline"><Item Align="Horizontal" Class="painexpQuestion" DescriptionPosition="above" ItemResponse="2,1" ItemScore="0" Order="1" RequiredMax="999" RequiredMin="1" TimeFinished="1383860312500"><Description><![CDATA[Explain what you believe is the cause of your pain?]]></Description><Responses><Response Appearance="full" Class="registrySurveyCBInline" Order="1" Type="select" align="horizontal"><ref>S5CAUSE</ref> <width>280</width> <item selected="true"><label>Accident</label><value>1</value></item><item selected="false"><label>Injury</label><value>2</value></item><item selected="false"><label>Undiagnosed medical problem</label><value>3</value></item><item selected="false"><label>Muscle</label><value>4</value></item><item selected="false"><label>Nerve</label><value>5</value></item><item selected="false"><label>Disc</label><value>6</value></item><item selected="false"><label>Bone</label><value>7</value></item><item selected="false"><label>Cancer</label><value>8</value></item><item selected="false"><label>Infection</label><value>9</value></item><item selected="false"><label>Unknown</label><value>10</value></item></Response><Response Align="Vertical" Class="surveyAugText" Order="2" Type="input" required="false"><Scores/> <ref>S5CAUSE</ref> <label>and/or</label> <location>above</location> <alert/> <format> <datatype>text</datatype><charwidth>80</charwidth><!-- textarea width is chars --> <lines>5</lines> </format> <hint>Optional: you may enter additional descriptions</hint></Response></Responses></Item><Item Align="Horizontal" Class="painexpQuestion" DescriptionPosition="above" ItemResponse="2,1" ItemScore="0" Order="2" RequiredMax="999" RequiredMin="1" TimeFinished="1383860316963"><Description><![CDATA[How has your pain affected your life?]]></Description><Responses><Response Appearance="full" Class="registrySurveyCBInline" Order="1" Type="select" align="horizontal"><ref>S5IMPACT</ref> <width>280</width><item selected="false"><label>Laying in bed all day</label><value>1</value></item><item selected="true"><label>Cannot do my job</label><value>2</value></item><item selected="false"><label>Feeling isolated</label><value>3</value></item> <item selected="false"><label>Reduced social activities</label><value>4</value></item><item selected="false"><label>Reduced recreational activities</label><value>5</value></item></Response><Response Align="Vertical" Class="surveyAugText" Order="2" Type="input" required="false"><Scores/><ref>S5IMPACT</ref> <label>and/or</label> <location>above</location> <alert/> <format><datatype>text</datatype><charwidth>80</charwidth><!-- textarea width is chars --> <lines>5</lines> </format> <hint>Optional: you may enter additional descriptions</hint></Response></Responses></Item><Item Align="Horizontal" Class="painexpQuestion" Code="STANFORD5" DescriptionPosition="above" ItemResponse="2,1" ItemScore="0" Order="3" RequiredMax="999" RequiredMin="1" TimeFinished="1383860325869"><Description><![CDATA[If your pain were 50% less tomorrow, what would you be doing differently?]]></Description><Responses><Response Appearance="full" Class="registrySurveyCBInline" Order="1" Type="select" align="vertical"><ref>S5DIFFERENT</ref><width>280</width><item selected="false"><label>More physical activities</label><value>1</value></item>           
<item selected="true"><label>Return to work</label><value>2</value></item><item selected="false"><label>Do more housework</label><value>3</value></item></Response><Response Align="Vertical" Class="surveyAugText" Order="2" Type="input" required="false"><Scores/><ref>S5DIFFERENT</ref> <label>and/or</label> <location>above</location> <alert/> <format> <datatype>text</datatype> <charwidth>80</charwidth><!-- textarea width is chars --> <lines>5</lines> </format> <hint>Optional: you may enter additional descriptions</hint></Response></Responses></Item><Item Align="Horizontal" Class="painexpQuestion" DescriptionPosition="above" ItemResponse="2,1" ItemScore="0" Order="4" RequiredMax="999" RequiredMin="1" TimeFinished="1383860341347"><Description><![CDATA[What do you believe is the appropriate treatment?]]></Description><Responses><Response Appearance="full" Class="registrySurveyCBInline" Order="1" Type="select" align="vertical"> <ref>S5TREATMENT</ref><width>280</width><item selected="false"><label>Medications</label><value>1</value></item><item selected="true"><label>Surgery</label><value>2</value></item><item selected="true"><label>Physical therapy</label><value>3</value></item><item selected="false"><label>Natural therapies</label><value>4</value></item><item selected="false"><label>Complementary therapies</label><value>5</value></item><item selected="false"><label>Psychological therapies</label><value>6</value></item><item selected="false"><label>Mind-body therapies</label><value>7</value></item><item selected="false"><label>Finding a diagnosis</label><value>8</value></item><item selected="false"><label>Unknown</label><value>9</value></item></Response><Response Align="Vertical" Class="surveyAugText" Order="2" Type="input" required="false"><Scores/><ref>S5TREATMENT</ref>            
<label>and/or</label><location>above</location><alert/><format><datatype>text</datatype> <charwidth>80</charwidth> <lines>5</lines></format>            ]
<hint>Optional: you may enter additional descriptions</hint></Response></Responses></Item> </Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_stanford5 and token = 787404111;
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Functional Assessment" questionsPerPage="1"><Items class="surveyInline"> <Item Align="Horizontal" Class="painexpQuestion" DescriptionPosition="above" ItemResponse="2,1" ItemScore="0" Order="1" RequiredMax="999" RequiredMin="1" TimeFinished="1383860345362"><Description><![CDATA[Please describe your activities in an average day]]></Description><Responses>Response Appearance="full" Class="registrySurveyCBInline" Order="1" Type="select" align="vertical"><width>280</width><item selected="true"><label>Laying in bed all day</label><value>1</value></item><item selected="false"><label>Going to school</label><value>2</value></item><item selected="false"><label>Going to work</label><value>3</value></item><item selected="false"><label>Taking care of family</label><value>4</value></item>/Response>Response Align="Vertical" Class="surveyAugText" Order="2" Type="input" required="false"><Scores/><ref>text</ref><label>and/or</label><location>above</location><alert/><format><datatype>text</datatype><charwidth>80</charwidth><lines>5</lines></format><hint>Optional: you may enter additional descriptions</hint>/Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_day and token = 787404111;
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Functional Assessment" questionsPerPage="3">
  <Items class="surveyInline">
    <Item Align="Horizontal" Class="registrySurvey" ItemResponse="2" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860362349">
      <Description><![CDATA[Current or former occupation]]></Description>
      <Responses><Response Align="Vertical" Class="registrySurvey" Order="2" Type="input">
        <Scores/><ref>FAJOB</ref><label/><location>left</location><alert/><format><datatype>text</datatype><charwidth>25</charwidth><lines>2</lines></format>
        <hint>Optional: enter n/a if not applicable</hint><value>Tour guide</value>
        </Response>
      </Responses>
    </Item>
    <Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="1" RequiredMin="1" TimeFinished="1383860362350">
      <Description><![CDATA[Are you working now?]]></Description>
      <Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1"><ref>FAWORKING</ref><width>100%</width><label/><location>left</location>
        <item selected="false"><label>Yes</label><value>1</value></item>
        <item selected="true"><label>No</label><value>2</value>
            <onselect Type="Item" Value="3" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="1"/><Set Name="visible" Type="state" Value="true"/></onselect>
<ondeselect Type="Item" Value="3" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="0"/><Set Name="visible" Type="state" Value="false"/></ondeselect>
        </item>
        <format><datatype>text</datatype><charwidth>5</charwidth><lines>2</lines></format><Scores/>
       </Response></Responses></Item>
    <!--  This is conditional: only shows if the answer to the previous question is no=false -->
    <Item Align="Horizontal" Class="registrySurvey" ItemResponse="3,2,1" ItemScore="0" Order="3" RequiredMax="2" RequiredMin="0" TimeFinished="1383860362350" Visible="false">
      <Description><![CDATA[When was the last time you worked?]]></Description><Alert>Please answer all questions</Alert>
      <Responses><Response Class="registrySurvey" Order="1" Type="input">
        <ref>FAWORKINGLAST</ref><label>Years</label><location>right</location><format><datatype>integer</datatype><lines>1</lines> <charwidth>2</charwidth></format><hint>Enter the number of years it has been since you last worked</hint><Scores/></Response>
        <Response Class="registrySurvey" Order="2" Type="input" required="false"><ref>duration</ref><label>Months</label><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth>        </format>        <hint>Enter the number of months it has been since you last worked</hint>        <Scores/><value>11</value></Response>
       <Response Class="registrySurvey" Order="3" Type="input" required="false">
        <ref>duration</ref>        <label>Days</label>        <location>right</location>        <format><datatype>integer</datatype>
<lines>1</lines>
<charwidth>2</charwidth>
        </format>
        <hint>Enter the number of days it has been since you last worked</hint>
        <Scores/>
        </Response>
      </Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_work and token = 787404111;
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Functional Assessment - Working" questionsPerPage="3"><Items class="surveyInline"><Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860366609" required="true"><Description><![CDATA[Are you receiving any kind of disability?]]></Description><Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="true"><label>Yes</label><selected>false</selected><value>1</value><onselect Type="Item" Value="2" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="1"/><Set Name="visible" Type="state" Value="true"/></onselect><ondeselect Type="Item" Value="2" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="0"/><Set Name="visible" Type="state" Value="false"/></ondeselect></item><item selected="false"><label>No</label><selected>false</selected><value>2</value></item><format><datatype>text</datatype><charwidth>5</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item><!--  conditional, only show item 10 if the answer to 9 is true --><Item Align="Vertical" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="2" RequiredMin="0" TimeFinished="1383860366610" Visible="false"><Description><![CDATA[What kind of disability?]]></Description><Responses><Response Appearance="full" Class="registrySurvey" Order="1" Type="select1" align="Vertical"><item selected="true"><label>Worker''s Compensation</label><value>1</value></item><item selected="false"><label>Social Security Disability Insurance (SSDI)</label><value>2</value></item><item selected="false"><label>Other</label><value>3</value><onselect Type="Item" Value="3" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="1"/><Set Name="visible" Type="state" Value="true"/></onselect><ondeselect Type="Item" Value="3" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="0"/><Set Name="visible" Type="state" Value="false"/></ondeselect></item><format><datatype>text</datatype><charwidth>40</charwidth><lines>3</lines></format></Response></Responses></Item><Item Align="Vertical" Class="nospace" ItemResponse="" ItemScore="" Order="3" RequiredMax="2" RequiredMin="0" Visible="false"><Description/><Responses><Response Align="Vertical" Class="functAssessA" Order="1" Type="input" required="false"><Scores/><ref>text</ref><label/><location>above</location><alert/><format><datatype>text</datatype><charwidth>50</charwidth><lines>2</lines></format><hint>Optional: you may enter another source</hint></Response></Responses></Item></Items>
</Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_disabil and token = 787404111;
--
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Functional Assessment - Lawsuit" questionsPerPage="1"><Items class="surveyInline">
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860369868" required="true"><Description><![CDATA[Are you involved in a legal action related to your pain problem?]]></Description><Responses> <Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="true"><label>Yes</label><value>1</value></item><item selected="false"><label>No</label><value>2</value></item> <format><datatype>text</datatype><charwidth>5</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="1" RequiredMin="1" TimeFinished="1383860371904" required="true"><Description><![CDATA[Are there any other legal problems?]]></Description><Responses> <Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item> <format><datatype>text</datatype><charwidth>5</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_lawsuit and token = 787404111;
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Functional Assessment - Working" questionsPerPage="1"><Items class="surveyInline">
<Item Align="Vertical" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860374515" required="true"><Description><![CDATA[Have you been told you snore loudly or gasp for breath at night?]]></Description><Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item><format><datatype>text</datatype><charwidth>5</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Vertical" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="1" RequiredMin="1" TimeFinished="1383860380081"><Description><![CDATA[Can you estimate the average number of hours you sleep per night?]]></Description><Alert>Please provide an integer estimate for the hours you sleep each night</Alert><Responses><Response Class="registrySurveyAnswer" Order="1" Type="input"><ref>duration</ref><label>Hours</label><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Enter the approximate hours you sleep nights</hint><Scores/><value>6</value></Response></Responses></Item>
<Item Align="Vertical" Class="registrySurvey" DescriptionPosition="above" ItemResponse="1" ItemScore="0" Order="3" RequiredMax="999" RequiredMin="0" TimeFinished="1383860384168"><Description><![CDATA[If you have difficulty sleeping is it related more to:]]></Description><Responses><Response Appearance="full" Class="registrySurveyAnswer" Order="1" Type="select" align="vertical"><width>480</width><item selected="true"><label>Getting to sleep initially</label><value>1</value></item><item selected="true"><label>Maintaining sleep throughout the night</label><value>2</value></item></Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_sleep and token = 787404111;
--
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Psychology History" questionsPerPage="1"><Items class="surveyInline">
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860387382" required="true"><Description><![CDATA[Prior to the age of 17, did you experience any major upheaval that you think may have shaped your life or personality significantly?]]></Description><Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><!--item><label>...</label><value /><selected>true</selected></item--><item selected="false"><label>Yes</label><value>1</value></item><item selected="false"><label>No</label><value>2</value></item><item selected="true"><label>Choose not to answer</label><value>3</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="1" RequiredMin="1" TimeFinished="1383860389356" required="true"><Description><![CDATA[After the age of 17, did you experience any other major upheaval that you think may have shaped your life or personality significantly?]]></Description><Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="false"><label>No</label><value>2</value></item><item selected="true"><label>Choose not to answer</label><value>3</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="3" RequiredMax="1" RequiredMin="1" TimeFinished="1383860391014" required="true"><Description><![CDATA[Do you feel you were neglected as a child?]]></Description><Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="false"><label>No</label><value>2</value></item><item selected="true"><label>Choose not to answer</label><value>3</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="4" RequiredMax="1" RequiredMin="1" TimeFinished="1383860392858" required="true"><Description><![CDATA[Did you experience chronic pain as a child?]]></Description><Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="false"><label>No</label><value>2</value></item><item selected="true">
<label>Choose not to answer</label><value>3</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="5" RequiredMax="1" RequiredMin="1" TimeFinished="1383860395663" required="true"><Description><![CDATA[Do you currently feel threatened in your environment?]]></Description><Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item><item selected="false"><label>Choose not to answer</label><value>3</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="6" RequiredMax="1" RequiredMin="1" TimeFinished="1383860397477" required="true"><Description><![CDATA[Have you ever been psychiatrically hospitalized?]]></Description><Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item><item selected="false"><label>Choose not to answer</label><value>3</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_psych and token = 787404111;
--
myClobVar := '<?xml version="1.0" encoding="utf-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Post Traumatic Stress Disorder" questionsPerPage="1"><Items class="surveyInline">
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860400301" required="true"><Description><![CDATA[Is your pain from a traumatic event?]]></Description><Responses Align="Horizontal" Class="surveyAnswerInline"><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="true"><label>Yes</label><value>1</value></item><item selected="false"><label>No</label><value>2</value></item><item selected="false"><label>Choose not to answer</label><value>3</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="1" RequiredMin="1" TimeFinished="1383860406313" required="true"><Description><![CDATA[<STRONG>In your life, have you ever had any experience that was so frightening, horrible, or upsetting that, in the past month, you:</STRONG><P>Have had nightmares about it or thought about it when you did not want to?</P>]]></Description><Responses Align="Horizontal" Class="surveyAnswerInline"><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="3" RequiredMax="1" RequiredMin="1" TimeFinished="1383860408391" required="true"><Description><![CDATA[<STRONG>In your life, have you ever had any experience that was so frightening, horrible, or upsetting that, in the past month, you:</STRONG><P>Have tried hard not to think about it or went out of your way to avoid situations that reminded you of it?</P>]]></Description><Responses Align="Horizontal" Class="surveyAnswerInline"><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="4" RequiredMax="1" RequiredMin="1" TimeFinished="1383860410174" required="true"><Description><![CDATA[<STRONG>In your life, have you ever had any experience that was so frightening, horrible, or upsetting that, in the past month, you:</STRONG><P>Were constantly on guard, watchful, or easily startled?</P>]]></Description><Responses Align="Horizontal" Class="surveyAnswerInline"><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="5" RequiredMax="1" RequiredMin="1" TimeFinished="1383860412174" required="true"><Description><![CDATA[<STRONG>In your life, have you ever had any experience that was so frightening, horrible, or upsetting that, in the past month, you:</STRONG><P>Felt numb or detached from others, activities, or your surroundings?</P>]]></Description><Responses Align="Horizontal" Class="surveyAnswerInline"><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1" required="true"><label/><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item><format><datatype>text</datatype><charwidth>13</charwidth><lines>2</lines></format><Scores/></Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_ptsd  and token = 787404111;
myClobVar := '<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Healthcare Utilization" questionsPerPage="1"><Items class="surveyInline">
<Item Align="Vertical" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860418940" required="true"><Description><![CDATA[In the past 6 months, how many times did you visit a physician?<P>Do not include visits while in the hospital or to a hospital emergency room.</P>]]></Description><Alert>Please provide an integer response</Alert> <Responses><Response Class="registrySurveyAnswer" Order="1" Type="input"><ref>HURITTER1</ref><label>Physician visits</label><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Number of Doctor visits in the last 6 months.</hint><Scores/><value>9</value></Response></Responses></Item>
<Item Align="Vertical" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="1" RequiredMin="1" TimeFinished="1383860424831"><Description><![CDATA[In the past 6 months, how many times did you go to a hospital emergency room?]]></Description><Alert>Please provide an integer response</Alert><Responses><Response Class="registrySurveyAnswer" Order="1" Type="input"><ref>HURITTER2</ref><label>Emergency room visits</label><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Number of emergency room visits in the last 6 months.</hint><Scores/><value>0</value></Response></Responses></Item>
<Item Align="Vertical" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="3" RequiredMax="1" RequiredMin="1" TimeFinished="1383860432969"><Description><![CDATA[How many different times did you stay in a hospital overnight or longer in the past 6 months?]]></Description><Alert>Please provide an integer response</Alert><Responses><Response Class="registrySurveyAnswer" Order="1" Type="input"><ref>HURITTER3</ref><label>Overnight hospital stays</label><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Number of hospital stays in the last 6 months.</hint><Scores/><value>2</value></Response></Responses></Item>
<Item Align="Vertical" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="4" RequiredMax="1" RequiredMin="1" TimeFinished="1383860437163"><Description><![CDATA[How many total nights did you stay in the hospital in the past 6 months?]]></Description><Alert>Please provide an integer response</Alert><Responses><Response Class="registrySurveyAnswer" Order="1" Type="input"><ref>HURITTER4</ref><label>Total nights</label><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Number of nights in the hospital over the last 6 months.</hint><Scores/><value>5</value></Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_health and token = 787404111; 
--
myClobVar := '<?xml version="1.0" encoding="utf-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Background Information" questionsPerPage="1"><Items class="surveyInline"><Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860442002" required="true"><Description><![CDATA[How many miles do you live from our Clinic in Redwood City?]]></Description><Responses Align="Horizontal" Class="surveyAnswerInline"><Response Class="registrySurvey" Order="1" Type="input" appearance="compact" required="true"><label>Miles</label><ref>BGMILES</ref><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>4</charwidth></format><hint>Enter the number of miles.</hint><Scores/><value>5</value></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="2,1" ItemScore="0" Order="2" RequiredMax="1" RequiredMin="1" TimeFinished="1383860447322" required="true"><Description><![CDATA[How long does it take you to get to our Clinic in Redwood City?]]></Description><Responses Align="Horizontal" Class="surveyAnswerInline"><Response Class="registrySurvey" Order="1" Type="input" appearance="compact"><label>Hours</label><ref>BGTIME</ref><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>3</charwidth></format><hint>Enter the time it takes.</hint><Scores/></Response><Response Class="registrySurvey" Order="2" Type="input" appearance="compact"><label>Minutes</label><ref>BGTIME</ref><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Enter the time it takes.</hint><Scores/><value>15</value></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="3" RequiredMax="1" RequiredMin="1" TimeFinished="1383860465447" required="true"><Description><![CDATA[Who is your Primary Care Physician?]]></Description><Responses Align="Horizontal" Class="surveyAnswerInline"><Response Class="registrySurvey" Order="1" Type="input" appearance="compact" required="true"><label/><ref>BGPCP</ref><location>left</location><format><datatype>text</datatype><lines>1</lines><charwidth>20</charwidth></format><hint>Enter the name of your primary care physician.</hint><Scores/><value>Dr Aaye</value></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="4" RequiredMax="1" RequiredMin="1" TimeFinished="1383860470969" required="true"><Description><![CDATA[Who referred you to our clinic?]]></Description><Responses Align="Horizontal" Class="surveyAnswerInline"><Response Class="registrySurvey" Order="1" Type="input" appearance="compact" required="true"><label/><ref>BGREFER</ref><location>left</location><format><datatype>text</datatype><lines>1</lines><charwidth>20</charwidth></format><hint>Enter the person who referred you to the clinic</hint><Scores/><value>Dr Bee</value></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" DescriptionPosition="above" ItemResponse="1" ItemScore="0" Order="5" RequiredMax="100" RequiredMin="1" TimeFinished="1383860475457" required="true">
<Description><![CDATA[In terms of marital status, are you:]]></Description><Responses><Response Appearance="full" Class="registrySurveyAnswer" Order="1" Type="select1" align="vertical"><ref>BGMARITAL</ref><width>295px</width><item selected="false"><label>Married</label><value>1</value></item><item selected="true"><label>Divorced</label><value>6</value></item><item selected="false"><label>Separated</label><value>2</value></item><item selected="false"><label>Widowed</label><value>3</value></item><item selected="false"><label>Never Married</label><value>4</value></item><item selected="false"><label>Living together</label><value>5</value></item></Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_background and token = 787404111;
myClobVar :='<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Education" questionsPerPage="1"><Items class="surveyInline"><Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860482860" required="true"><Description><![CDATA[How far did you go in school? (Select the highest attained)]]></Description><Responses><Response Appearance="full" Class="registrySurveyAnswer" Order="1" Type="select1" align="vertical"><ref>SHEDUCATION</ref><width>295px</width><item selected="false"><label> Never attended/Kindergarten only</label><value>1</value></item><item selected="false"><label> 1st Grade</label><value>2</value></item><item selected="false"><label> 2nd Grade</label><value>3</value></item><item selected="false"><label> 3rd Grade</label><value>4</value></item><item selected="false"><label> 4th Grade</label><value>5</value></item><item selected="false"><label> 5th Grade</label><value>6</value></item><item selected="false"><label> 6th Grade</label><value>7</value></item><item selected="false"><label> 7th Grade</label><value>8</value></item><item selected="false"><label> 8th Grade</label><value>9</value></item><item selected="false"><label> 9th Grade</label><value>10</value></item><item selected="false"><label> 10th Grade</label><value>11</value></item><item selected="false"><label> 11th Grade</label><value>12</value></item><item selected="false"><label> 12th Grade, no diploma</label><value>13</value></item><item selected="false"><label> High school equivalent</label><value>14</value></item><item selected="false"><label> GED or equivalent</label><value>15</value></item><item selected="true"><label> Some college, no degree</label><value>16</value></item><item selected="false"><label> Associate degree: occupational/technical/vocational program</label><value>17</value></item><item selected="false"><label> Associate degree: academic program</label><value>18</value></item><item selected="false"><label> Bachelor''s degree (e.g., BA, AB, BS, BBA)</label><value>19</value></item><item selected="false"><label> Master''s degree (e.g., MA, MS, MEng, Med, MBA)</label><value>20</value></item><item selected="false"><label> Professional school degree (e.g. MD, DDS, DVM, JD)</label><value>21</value></item><item selected="false"><label> Doctoral degree (e.g., PhD, EdD)</label><value>22</value></item><item selected="false"><label> Unknown</label><value>23</value></item> </Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_education and token = 787404111; 
myClobVar :='<?xml version="1.0" encoding="utf-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Questions" questionsPerPage="1"> <Items class="surveyInline">  <Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860499250" required="true">      <Description>        <![CDATA[What are the specific question(s) that you or your doctor wants answered today?]]>      </Description>      <Responses Align="Horizontal" Class="surveyAnswerInline">        <Response Class="registrySurvey" Order="1" Type="input" appearance="compact" required="true">          <label/>          <ref>QUESTIONS</ref>          <location>left</location>          <format>          <datatype>text</datatype>          <lines>10</lines>           <charwidth>80</charwidth>          </format>          <hint>Enter questions you have.</hint>          <Scores/>        <value>What do we do next</value></Response>      </Responses>    </Item>  </Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_quest and token = 787404111; 
myclob :='<?xml version="1.0" encoding="utf-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Other Pain Physicians" questionsPerPage="1"> <Items class="surveyInline">  <Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860508392" required="true"><Description><![CDATA[Which other pain doctor or pain clinic have you seen for your problem, in the past 5 years?]]>      </Description>      <Responses Align="Horizontal" Class="surveyAnswerInline">        <Response Class="registrySurvey" Order="1" Type="input" appearance="compact" required="true">          <label/>            <ref>OTHERPAINDOCS</ref>          <location>left</location>          <format>            <datatype>text</datatype>           <lines>5</lines>     <charwidth>80</charwidth>          </format>          <hint>Enter names of pain Doctors and Clinics you''ve seen in the past 5 years.</hint><Scores/><value>Dr Sea</value></Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_other and token = 787404111;
myclob :='<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Smoking" questionsPerPage="2"><Items class="surveyInline"><Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860513517"><Description><![CDATA[Do you smoke?]]></Description><Responses><Response Align="vertical" Appearance="full" Class="registrySurvey" Order="1" Type="select1"><ref>SMOKENOW</ref><width>100%</width><label/><location>left</location><item selected="true"><label>Yes</label><value>1</value><onselect Type="Item" Value="2" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="1"/><Set Name="visible" Type="state" Value="true"/></onselect><ondeselect Type="Item" Value="2" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="0"/><Set Name="visible" Type="state" Value="false"/></ondeselect></item><item selected="false"><label>No</label><value>2</value></item><Scores/></Response></Responses></Item><!--  This is conditional: only shows if the answer to the previous question is yes --><Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="1" RequiredMin="0" TimeFinished="1383860513517" Visible="false"><Description><![CDATA[How many packs per day?]]></Description><Alert>Please answer all questions</Alert><Responses><Response Class="registrySurvey" Order="1" Type="input"><ref>SMOKEPPD</ref><label>Packs</label><location>right</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Enter the number of packs you smoke per day</hint><Scores/><value>1</value></Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_smoking and token = 787404111;
myclob :='<?xml version="1.0" encoding="UTF-8" standalone="no"?><Form Class="surveyInlineBordered" DateFinished="" DateStarted="" Description="Functional Assessment - Working" questionsPerPage="8"><Items class="surveyInline">
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="1" RequiredMax="1" RequiredMin="1" TimeFinished="1383860534657" required="true"><Description><![CDATA[Do you drink alcohol?]]></Description><Responses><Response Align="horizontal" Appearance="full" Class="registrySurveyAnswer" Order="1" Type="select1" required="true"><ref>ALCOHOLNOW</ref><item selected="true"><label>Yes</label><selected>false</selected><value>1</value><onselect Type="Item" Value="2" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="1"/><Set Name="visible" Type="state" Value="true"/></onselect><onselect Type="Item" Value="3" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="1"/><Set Name="visible" Type="state" Value="true"/></onselect><onselect Type="Item" Value="4" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="1"/><Set Name="visible" Type="state" Value="true"/></onselect><onselect Type="Item" Value="5" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="1"/><Set Name="visible" Type="state" Value="true"/></onselect><ondeselect Type="Item" Value="2" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="0"/><Set Name="visible" Type="state" Value="false"/></ondeselect><ondeselect Type="Item" Value="3" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="0"/><Set Name="visible" Type="state" Value="false"/></ondeselect><ondeselect Type="Item" Value="4" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="0"/><Set Name="visible" Type="state" Value="false"/></ondeselect><ondeselect Type="Item" Value="5" Where="Order"><Set Name="RequiredMin" Type="attribute" Value="0"/><Set Name="visible" Type="state" Value="false"/></ondeselect></item><item selected="false"><label>No</label><selected>false</selected><value>2</value></item><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="2" RequiredMax="1" RequiredMin="0" TimeFinished="1383860534657" Visible="false" align="horizontal"><Description><![CDATA[How many drinks per day?]]></Description><Responses><Response Align="horizontal" Class="registrySurveyAnswer" Order="1" Type="input"><ref>ALCOHOLPERDAY</ref><location>left</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><hint>Enter an integer value</hint><Scores/><value>2</value></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="3" RequiredMax="1" RequiredMin="0" TimeFinished="1383860534658" Visible="false" align="horizontal"><Description><![CDATA[How many drinks per week?]]></Description><Responses><Response Align="horizontal" Class="registrySurveyAnswer" Order="1" Type="input"><ref>ALCOHOLPERWEEK</ref><location>left</location><format><datatype>integer</datatype><lines>1</lines><charwidth>2</charwidth></format><value>14</value></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="4" RequiredMax="1" RequiredMin="0" TimeFinished="1383860534660" Visible="false"><Description><![CDATA[Do you drink to intoxication or binge drink?]]></Description><Responses><Response Align="horizontal" Appearance="full" Class="registrySurveyAnswer" Order="1" Type="select1"><ref>ALCOHOLBINGE</ref><location>left</location><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="5" RequiredMax="1" RequiredMin="0" TimeFinished="1383860534661" Visible="false"><Description><![CDATA[Do you drink to decrease your pain?]]></Description><Responses><Response Align="horizontal" Appearance="full" Class="registrySurveyAnswer" Order="1" Type="select1"><ref>ALCOHOLFORPAIN</ref><width>100%</width><location>left</location><item selected="false"><label>Yes</label><value>1</value></item><item selected="true"><label>No</label><value>2</value></item><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="6" RequiredMax="1" RequiredMin="1" TimeFinished="1383860534661" Visible="true" align="horizontal"><Description><![CDATA[In the past 10 years have you ever tried street drugs?]]></Description><Responses><Response Align="horizontal" Appearance="full" Class="registrySurveyAnswer" Order="1" Type="select1" required="true"><ref>DRUGS10YR</ref><item selected="false"><label>Yes</label><selected>false</selected><value>1</value></item><item selected="true"><label>No</label><selected>false</selected><value>2</value></item><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="7" RequiredMax="1" RequiredMin="1" TimeFinished="1383860534662" Visible="true" align="horizontal"><Description><![CDATA[Have you or anyone around you ever felt you had a problem with alcohol or drugs?]]></Description><Responses><Response Align="horizontal" Appearance="full" Class="registrySurveyAnswer" Order="1" Type="select1" required="true"><ref>DRUGSCUT</ref><item selected="true"><label>Yes</label><selected>false</selected><value>1</value></item><item selected="false"><label>No</label><selected>false</selected><value>2</value></item><Scores/></Response></Responses></Item>
<Item Align="Horizontal" Class="registrySurvey" ItemResponse="1" ItemScore="0" Order="8" RequiredMax="1" RequiredMin="1" TimeFinished="1383860534662" Visible="true" align="horizontal"><Description><![CDATA[Have you ever received alcohol or drug treatment?]]></Description><Responses><Response Align="horizontal" Appearance="full" Class="registrySurveyAnswer" Order="1" Type="select1" required="true"><ref>DRUGSTX</ref><item selected="true"><label>Yes</label><selected>false</selected><value>1</value></item><item selected="false"><label>No</label><selected>false</selected><value>2</value></item><Scores/></Response></Responses></Item></Items></Form>';
myclob := myClobVar; 
update patient_study set xml_results = xmltype(myclob)  WHERE study_code = local_alcohol and token = 787404111; 

end;
/

commit
/
