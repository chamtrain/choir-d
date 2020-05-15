@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 6d
prompt -- ======== Fix the site/token duplicates in SURVEY_REGISTRATION ========
prompt -- ======== and any corresponding rows in PATIENT_STURY and ACTIVITY

set pagesize 99
set linesize 200
column SURVEY_REG_ID format 99999999999999999999
column SITE          format 9999 
column PATIENT_ID    format a10
column SURVEY_TYPE   format a20
column TOKEN         format a20
column as_reg_id     format 99999999999999999999
column sreg_id       format 99999999999999999999
column SURVEY_NAME   format a30
column SURVEY_ORDER  format 999
column SURVEY_DT     format a28
column DT_CREATED    format a28
column MIN_CRDT      format a28
column MAX_CRDT      format a28
column DT_CHANGED    format a28
column min_dt_created format a28
column token_id      format 99999999999999999999
column sub_tok       format 99999999999999999999
define SITE=Choir


prompt -- NOTE:  This happens in a transaction. 
prompt --        Before leaving this script, you can type:  ROLLBACK;
prompt
prompt -- First update SURVEY_REGISTRATION

-- sigh, I wonder if I should add site_id to this...

UPDATE survey_registration sr
SET token = (SELECT sub_tok FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id)
WHERE token IN (SELECT token FROM tmp_sr_dups) 
  AND EXISTS (SELECT *      FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id);

prompt -- Then PATIENT_STUDY

update PATIENT_STUDY sr
set TOKEN = (SELECT sub_tok FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id)
WHERE TOKEN IN (SELECT token FROM tmp_sr_dups) 
  AND EXISTS (SELECT *      FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id);

prompt -- Then ACTIVITY, though this may not fix much...

update ACTIVITY sr
set TOKEN = (SELECT sub_tok FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id)
WHERE TOKEN IN (SELECT token FROM tmp_sr_dups) 
  AND EXISTS (SELECT *      FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id);

prompt -- Now we drop the temporary tables:

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE tmp_sr_dups';
   EXECUTE IMMEDIATE 'DROP TABLE tmp_sr_rows';
   EXECUTE IMMEDIATE 'DROP TABLE tmp_sub_tokens';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN RAISE; END IF;
END;
/

prompt -- There might still be SITE,SURVEY_TOKEN combinations that 
prompt --    serve multiple patient_ids in ACTIVITY, but that's a different problem
prompt --    we'll fix those in the script after next, in 7...
prompt
prompt -- Next, finish this task. Fix the site-token index:  @ 6e-sr-fix-site-token-ix
prompt

