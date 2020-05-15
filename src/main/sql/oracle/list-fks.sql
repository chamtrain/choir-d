set pagesize 5000
set linesize 350
set tab off

SET VERIFY OFF

column status        format a10
column table_name    format a29
column fk_name       format a30
column fk_columns    format a29
column index_name    format a30
column index_columns format a46

prompt === Here are ALL the tables preceded by indexed or UNINDEXED

select
  DECODE(b.table_name, null, 'UNINDEXED', 'indexed') as status,
  a.table_name      as table_name,
  a.constraint_name as fk_name,
  a.fk_columns      as fk_columns,
  b.index_name      as index_name,
  b.index_columns   as index_columns
from
( SELECT a.table_name, a.constraint_name,
         (listagg(a.column_name, ',') WITHIN GROUP (ORDER BY a.position)) fk_columns
  FROM user_cons_columns a,
       user_constraints b
  WHERE a.constraint_name = b.constraint_name
    AND b.constraint_type = 'R'
  GROUP BY a.table_name, a.constraint_name
) a
,
(SELECT table_name, index_name, 
         listagg(c.column_name, ',') within group (order by c.column_position) index_columns
  FROM user_ind_columns c
  GROUP BY table_name, index_name
) b
WHERE a.table_name = b.table_name(+)
  AND b.index_columns(+) LIKE a.fk_columns || '%'  -- a bit approximate, a,b matches a,box,c
  ORDER BY 2, 4;

SET VERIFY ON
