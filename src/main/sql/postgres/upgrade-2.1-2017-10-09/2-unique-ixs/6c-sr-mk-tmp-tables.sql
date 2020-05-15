-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '-- ======== Registry 2.1-2 PostGres SCRIPT 6c'
\echo '-- ======== Make tmp tables to fix site/token duplicates in SURVEY_REGISTRATION ========'
\echo

\echo '-- This script creates a small table with your duplicate survey_registration site/token rows'
\echo '-- and then shows you the multiple rows from one set of duplicates.'
\echo '-- '

\set SITE Choir


\echo '== First, we will drop 3 temporary tables if they exist, then make them'

DROP TABLE IF EXISTS tmp_sr_dups;
DROP TABLE IF EXISTS tmp_sr_rows;
DROP TABLE IF EXISTS tmp_sub_tokens;

\echo '== Create a small table of the duplicate site and token pairs, plus FYI their dates'

CREATE TABLE tmp_sr_dups AS
(SELECT site, token, num_dups, min_crdt, max_crdt FROM
   (SELECT count(*) num_dups, survey_site_id site, token, 
           min(dt_created) min_crdt, max(dt_created) max_crdt FROM survey_registration 
     GROUP BY survey_site_id, token
   ) x WHERE num_dups > 1);

\echo '== Here they are (crdt is the creation date):'

SELECT * FROM tmp_sr_dups;

\echo '== And FYI, here is more info about their rows:'

SELECT a.SURVEY_SITE_ID SITE, a.token, a.SURVEY_REG_ID, a.PATIENT_ID,
       to_Char(a.SURVEY_DT,'YY-MON-DD') survey_dt, a.SURVEY_TYPE, 
       to_char(a.DT_CREATED,'YY-MON-DD') DT_CREATED, to_char(a.DT_CHANGED,'YY-MON-DD') DT_CHANGED,
       a.ASSESSMENT_REG_ID as_reg_id, a.SURVEY_NAME, a.SURVEY_ORDER
FROM survey_registration a, tmp_sr_dups b
WHERE  a.survey_site_id = b.site AND a.token = b.token ORDER BY SITE, TOKEN, a.SURVEY_REG_ID;

\echo '-- Make a small table tmp_sr_rows with the rows of SURVEY_REGISTRATION that are dups.'
\echo '--   We add PATIENT_IDS and TOKEN_IDS for the rows where the SITE,TOKEN,PATIENT_ID are in patient_study'

\set SUBTOK0 10000

CREATE TABLE tmp_sr_rows AS (
  SELECT  row_number() OVER (ORDER BY sub_tok) AS rn, site, token, token_id, sreg_id, patient_id, sub_tok FROM
 (SELECT  t.site, t.token, 
          (SELECT survey_token_id FROM survey_token st
            WHERE t.token=st.survey_token AND t.site=st.survey_site_id AND
                  EXISTS (SELECT * FROM patient_study p 
                          WHERE sr.survey_reg_id=p.survey_reg_id AND sr.patient_id=p.patient_id)) token_id,
	  sr.survey_reg_id sreg_id, sr.patient_id, :SUBTOK0 sub_tok
    FROM  tmp_sr_dups t JOIN survey_registration sr
           ON t.site=sr.survey_site_id AND t.token=sr.token
 ) x
);

\echo '-- If none of a set of duplicates has a token_id, we need to change all but one of them, so we will'
\echo '--   keep the one with the smallest patient_id by setting its token_id to 1 (not null) in our tmp table'

UPDATE tmp_sr_rows SET token_id = 1 
 WHERE rn IN 
    (SELECT rn FROM tmp_sr_rows tmp WHERE
       rn NOT IN (SELECT rn
                    FROM tmp_sr_rows t, (SELECT site, token FROM tmp_sr_rows t WHERE t.token_id IS NOT NULL) q
                   WHERE t.site=q.site AND t.token=q.token)
       AND tmp.patient_id != (SELECT min(patient_id) FROM tmp_sr_rows t
                              WHERE tmp.site=t.site AND tmp.token=t.token));


\echo '-- Now we make a small table of new, substitute tokens at 10,000'

CREATE TABLE tmp_sub_tokens AS
  (SELECT generate_series(:SUBTOK0+1, :SUBTOK0+1000) AS n);

ALTER TABLE tmp_sub_tokens ADD COLUMN ordr int;

\echo '-- We delete any that happen to already be used by SURVEY_REGISTRATION'

DELETE FROM tmp_sub_tokens WHERE EXISTS (SELECT * FROM survey_registration WHERE token= cast(n as text));

\echo '-- And we number the rows of these substitute tokens table, creating, using and dropping a sequence'

CREATE SEQUENCE tmp_st START 1;
UPDATE tmp_sub_tokens SET ordr = nextval('tmp_st');
DROP SEQUENCE tmp_st;

-- prompt -- FYI: Here's the min, max and number of rows in the table
-- SELECT min(n) min_n, max(n) max_n, count(*) num_rows FROM tmp_sub_tokens;

\echo '-- Now we number the rows of tmp_sr_rows that need their token changed (TOKEN_ID is null)'

CREATE SEQUENCE tmp_st START 1;
UPDATE tmp_sr_rows SET rn = nextval('tmp_st') WHERE TOKEN_ID IS NULL;
DROP SEQUENCE tmp_st;

\echo '-- Put the first substitute tokens into the rows of tmp_sr_rows that have a NULL token_id'

UPDATE tmp_sr_rows SET sub_tok = (SELECT n FROM tmp_sub_tokens WHERE ordr=rn) 
WHERE token_id IS NULL;

\echo '-- And here are the rows to update (just showing the ones with NULL token_id)'

SELECT * FROM tmp_sr_rows WHERE token_id IS NULL ORDER BY rn;

\echo '-- If this looks good, fix the duplicate tokens with null TOKEN_IDs'
\echo '--    in the next script: \\i 6d-sr-fix-site-token-dups.sql'
\echo
