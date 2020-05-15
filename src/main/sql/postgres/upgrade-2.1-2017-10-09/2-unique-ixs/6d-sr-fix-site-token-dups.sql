-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 6d'
\echo '-- ======== Fix the site/token duplicates in SURVEY_REGISTRATION ========'
\echo '-- ======== and any corresponding rows in PATIENT_STURY and ACTIVITY'

\set SITE Choir


\echo '-- NOTE:  This happens in a transaction. '
\echo '--        Before leaving this script, you can type:  ROLLBACK;'
\echo
\echo '-- First update SURVEY_REGISTRATION'

-- sigh, I wonder if I should add site_id to this...
-- it's probably very rare that a patient is in choir in 2 clinics, at this stage

UPDATE survey_registration sr
SET token = (SELECT sub_tok FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id)
WHERE token IN (SELECT token FROM tmp_sr_dups) 
  AND EXISTS (SELECT *      FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id);

\echo '-- Then PATIENT_STUDY'

update PATIENT_STUDY sr
set token = (SELECT sub_tok FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id)
WHERE token IN (SELECT token FROM tmp_sr_dups) 
  AND EXISTS (SELECT *      FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id);

\echo '-- Then ACTIVITY, though this may not fix much...'

UPDATE activity sr
SET token = (SELECT sub_tok FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id)
WHERE token IN (SELECT token FROM tmp_sr_dups) 
  AND EXISTS (SELECT *      FROM tmp_sr_rows t WHERE t.token_id IS NULL AND sr.token=t.token AND sr.patient_id=t.patient_id);

\echo '-- Now we drop the temporary tables:'

DROP TABLE IF EXISTS tmp_sr_dups;
DROP TABLE IF EXISTS tmp_sr_rows;
DROP TABLE IF EXISTS tmp_sub_tokens;

\echo '-- There might still be SITE,SURVEY_TOKEN combinations that '
\echo '--    serve multiple patient_ids in ACTIVITY, but that is a different problem'
\echo '--    we will fix those in the script after next, in 7...'
\echo
\echo '-- Next, finish this task. Fix the site-token index:  \\i 6e-sr-fix-site-token-ix.sql'
\echo

