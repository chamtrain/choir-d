@column-formats

-- turn off echoing before/after variable substitution
SET VERIFY OFF

prompt 
prompt 
prompt ============= view the constraints and indexes on table &&TABLE ================

column TABLE_NAME format a20
column COLUMN_NAME format a20
column INDEX_NAME format a30
column CONSTRAINT_NAME format a28
column COVERING_INDEX format a30
column SEARCH_CONDITION format a50

prompt == Here are the foreign key constraints on &TABLE:

BREAK on constraint_name SKIP 1

SELECT '   ', con.constraint_name, col.column_name, con.index_name, col.position, con.r_constraint_name covering_index, con.search_condition
  FROM user_cons_columns col, user_constraints con
 WHERE col.table_name=UPPER('&TABLE') AND con.table_name=UPPER('&TABLE')
   AND col.constraint_name=con.constraint_name
--   AND con.constraint_type = 'R'
 ORDER by col.constraint_name, col.position;

prompt
prompt == Here are the indexes on &TABLE:

BREAK on index_name SKIP 1

SELECT '   ', i.table_name, i.index_name, col.column_name, col.column_position, i.uniqueness position
  FROM user_indexes i, user_ind_columns col
 WHERE i.table_name=UPPER('&TABLE') AND col.table_name=UPPER('&TABLE')
   AND i.index_name=col.index_name
 ORDER by i.index_name, col.column_position, col.column_name;

SET VERIFY ON
