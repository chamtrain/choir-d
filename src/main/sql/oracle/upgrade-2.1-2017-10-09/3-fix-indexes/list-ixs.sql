set pagesize 5000
set linesize 350
set tab off

SET VERIFY OFF

column status        format a10
column table_name    format a30
column fk_name       format a30
column fk_columns    format a30
column index_name    format a30
column index_columns format a55

SELECT '    ' "----" , i.index_name, i.uniqueness,
       listagg(c.column_name, ', ') within group (order by c.column_position) index_columns
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name=UPPER('&&TABLE') AND c.table_name=UPPER('&&TABLE')
   AND i.index_name=c.index_name
 GROUP by i.index_name, i.uniqueness
 ORDER by i.index_name;

SET VERIFY ON
