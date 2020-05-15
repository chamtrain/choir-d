set pagesize 5000
set linesize 350
set tab off

column status        format a10
column table_name    format a30
column fk_name       format a30
column fk_columns    format a30
column index_name    format a30
column index_columns format a30
column constraint_name format a30

SET VERIFY OFF

SELECT 
    a.table_name,
    a.constraint_name,
    (listagg(a.column_name, ',') WITHIN GROUP (ORDER BY a.position)) fk_columns
  FROM user_cons_columns a,
       user_constraints b
  WHERE a.constraint_name = b.constraint_name
    AND b.constraint_type = 'R'
    AND a.table_name = UPPER('&&TABLE')
    AND b.table_name = UPPER('&&TABLE')
  GROUP BY a.table_name, a.constraint_name
  ORDER BY fk_columns
;

SET VERIFY ON

prompt -- To drop an index:  DROP INDEX name;
prompt -- To create one:     CREATE INDEX name ON table (col1, col2...);   -- or use:  CREATE UNIQUE INDEX name...
prompt -- Always drop index A if an index B exists that starts with all the columns of A.

