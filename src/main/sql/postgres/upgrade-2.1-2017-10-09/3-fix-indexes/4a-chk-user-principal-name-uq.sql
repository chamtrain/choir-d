-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo
\echo '============= 4a.'
\echo '============= need a constraint that USER_PRINCIPAL . USERNAME column is unique ================'
\echo
\echo '== Just FYI, we will see how many rows have non-unique username columns:'

SELECT min_id, max_id, num FROM
(SELECT min(user_principal_id) min_id, max(user_principal_id) max_id, count(*) num
  FROM user_principal GROUP BY username) x
WHERE num > 1;

\echo
\echo '== Hopefully, that was zero, so we can add the column constraint:'

ALTER TABLE user_principal ADD CONSTRAINT u_princ_username_uq UNIQUE (username);

\echo
\echo '== If that worked (there was no error),'
\echo '==   or there was an error that the contraint already existed,'
\echo '==   continue to:  \\i 5-add-3-foreign-keys.sql'
\echo '== '
\echo '== If that didnt work because there ARE duplicates, the next script will'
\echo '==   append a digit to every username there are more than one of.'
\echo '==   Simply run the next script repeatedly until it stops updating any rows.'
\echo '==   This is:  \\i 4b-mk-user-principal-name-uq.sql'
\echo
