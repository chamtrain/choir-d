-- This is a utility script to show the indexes on a table
-- the table is defined in the caller with:
--    define TABLE=TABLE_NAME

@column-formats

SET VERIFY OFF
prompt -- ======== Here are indexes on table &&TABLE ========

BREAK ON i.index_name SKIP 1
column index_columns format a50

SELECT '    ' "----" , i.index_name, i.uniqueness,
       listagg(c.column_name, ', ') within group (order by c.column_position) index_columns
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name=UPPER('&&TABLE') AND c.table_name=UPPER('&&TABLE')
   AND i.index_name=c.index_name
 GROUP by i.index_name, i.uniqueness
 ORDER by i.index_name;

prompt
SET VERIFY ON
