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
column uniq          format a6
column dup           format a3
prompt === Here are ALL the tables and their index columns

BREAK on table_name SKIP 1

SELECT x.table_name, x.index_name,
       DECODE(y.table_name, null, '   ', 'dup'),
       DECODE(x.uniqueness, 'UNIQUE', 'UNIQUE', '      ') uniq, x.index_columns
  FROM
(SELECT i.table_name, i.index_name, i.uniqueness,
       listagg(c.column_name, ', ') within group (order by c.column_position) index_columns
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name=c.table_name
   AND i.index_name=c.index_name
 GROUP by i.table_name, i.index_name, i.uniqueness) x LEFT OUTER JOIN
(SELECT i.table_name, i.index_name,
       listagg(c.column_name, ', ') within group (order by c.column_position) index_columns
  FROM user_indexes i, user_ind_columns c
 WHERE i.table_name=c.table_name
   AND i.index_name=c.index_name
 GROUP by i.table_name, i.index_name, i.uniqueness) y
       ON x.table_name=y.table_name 
       AND length(x.index_columns) < length(y.index_columns)
       AND 1 = instr(y.index_columns, x.index_columns)
ORDER BY table_name, index_columns;
-- we order by index_columns so you can easily see where 2 overlap

SET VERIFY ON
