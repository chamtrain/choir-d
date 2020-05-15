@column-formats
prompt 
prompt 
prompt 
prompt 
prompt 
prompt ============= 4a.
prompt ============= need a constraint that USER_PRINCIPAL . USERNAME column is unique ================

column TABLE_NAME format a13
column COLUMN_NAME format a21

prompt == Just FYI, we will see how many rows have non-unique username columns:

SELECT min_id, max_id, num FROM
(SELECT min(user_principal_id) min_id, max(user_principal_id) max_id, count(*) num
  FROM user_principal GROUP BY username) x
WHERE num > 1;

prompt == Hopefully, that was zero, so we can add the column constraint:

ALTER TABLE user_principal 
ADD CONSTRAINT u_princ_username_uq UNIQUE (username);

prompt == If that worked (there was no error), or the contraint already existed,
prompt ==   continue to:  @ 5-add-3-foreign-keys
prompt == 
prompt == If that didnt work because there ARE duplicates, you should
prompt ==   append an N (2, 3...) to every username there are more than one of.
prompt ==   Simply run the next query repeatedly until it stops updating any rows...
prompt ==   If you want, you can change any user22 -> user3... (if there are any)
prompt ==   You can do this by repeatedly running:  @ 4b-mk-user-principal-name-uq
prompt
