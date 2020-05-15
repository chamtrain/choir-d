-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '============= 4b.'
\echo '============= Get rid of duplicate USERNAME columns in USER_PRINCIPAL, then make the column unique'
\echo
\echo '== updating more with the same username to username+number:'

update user_principal up set username = username || (select count(*) from user_principal uu where uu.username=up.username)
 where user_principal_id IN 
 (SELECT max_id FROM
  (SELECT max(user_principal_id) max_id, count(*) num
   FROM user_principal GROUP BY username) x
  WHERE num > 1);

\echo
\echo '== (If some were updated) Are there any duplicates left:'

SELECT min_id, max_id, num FROM
(SELECT min(user_principal_id) min_id, max(user_principal_id) max_id, count(*) num
  FROM user_principal GROUP BY username) x
WHERE num > 1;

\echo '== Hopefully, that was "no rows", so we can add the column constraint finally:'

ALTER TABLE user_principal
ADD CONSTRAINT u_princ_username_uq UNIQUE (username);

\echo
\echo '== If that failed (and there were duplicates), run this again:  \\i 4b-mk-user-principal-name-uq.sql'
\echo
\echo '== Otherwise, proceed to:  \\i 5-add-3-foreign-keys.sql'
\echo
