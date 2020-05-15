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

--
-- creates the database triggers 
--

--set define off
--/
CREATE OR REPLACE TRIGGER PATIENT_STUDY_BIU_TRIGGER
BEFORE INSERT OR UPDATE ON PATIENT_STUDY
FOR EACH ROW WHEN (new.XML_CLOB IS NOT NULL)
DECLARE
BEGIN
  :new.XML_RESULTS := xmlType( :new.XML_CLOB , null, 1, 1);
END;
/

CREATE OR REPLACE TRIGGER PATIENT_DOCUMENT_BIU_TRIGGER
BEFORE INSERT OR UPDATE ON PATIENT_DOCUMENT
FOR EACH ROW WHEN (new.XML_CLOB IS NOT NULL)
DECLARE
BEGIN
  :new.XML_DOCUMENT := xmlType( :new.XML_CLOB , null, 1, 1);
END;
/

create or replace
TRIGGER PATIENT_EMAIL_AIU_TRIGGER
AFTER INSERT OR UPDATE ON PATIENT_ATTRIBUTE
FOR EACH ROW WHEN (new.data_name = 'surveyEmailAddress' and new.data_value is not null)
DECLARE
BEGIN
  update assessment_registration ar
  set email_addr = :new.data_value, dt_changed = sysdate
  where ar.patient_id = :new.patient_id 
  and ar.assessment_dt > sysdate;  
END;
/

commit
/

CREATE OR REPLACE FUNCTION isValidXML(xmlString CLOB)
RETURN NUMBER
AS
  xmldata XMLTYPE;
BEGIN
  xmldata := XMLTYPE(xmlString);
  return 1;
EXCEPTION
  when others then
    return 0;
END;
/
