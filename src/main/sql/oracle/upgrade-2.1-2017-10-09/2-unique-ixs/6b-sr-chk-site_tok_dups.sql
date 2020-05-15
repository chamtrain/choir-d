@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt -- ======== Registry 2.1 Oracle SCRIPT 6b
prompt -- ======== Checking for duplicate SITE+TOKEN rows in SURVEY_REGISTRATION ========
prompt
prompt -- We want the site+token to be unique.
prompt -- There is a 50% chance you will get a duplicate token after 77,000 survey assignments.
prompt -- This query tells if all the site+tokesn are unique, or if there are pairs, trios or more
prompt --   (If no rows are found, no surveys have been assigned, that's fine.)

SELECT DECODE(num_duplicates,1,'unique',2,'pairs',3,'trios','more') num_same, count(*) counts FROM
  (SELECT count(*) num_duplicates FROM survey_registration 
    GROUP BY survey_site_id, token, survey_name
  ) GROUP BY num_duplicates;

prompt
prompt -- You CAN NOT make the index unique if there are Pairs, Trios or More.
prompt

SET VERIFY ON

prompt -- If there are duplicates (pairs, trios or more), handle them with:   @ 6c-sr-mk-tmp-tables
prompt
prompt -- If there are no duplicates, or no rows at all, create the unique index with:  @ 6e-sr-fix-site-token-ix
prompt
