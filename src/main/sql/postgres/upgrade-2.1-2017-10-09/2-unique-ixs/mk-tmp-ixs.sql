-- PostGres script
\echo '=== (re)Creating a list of ALL the tables and their index columns in table tmp_ixs'

drop table if exists tmp_ixs;

CREATE TABLE tmp_ixs AS 
(SELECT schema_name, table_name, index_name, uniq, 
        substring(columns from 2 for char_length(columns)-2) as index_columns
   FROM
(SELECT n.nspname  as schema_name,
       t.relname  as table_name,
       c.relname  as index_name,
       CASE WHEN pg_get_indexdef(indexrelid) like 'CREATE UNIQUE %' THEN 'UNIQUE' ELSE 'non-uq' END as uniq,
       substring(pg_get_indexdef(indexrelid) from '\(.+\)') as columns
FROM pg_catalog.pg_class c
    JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
    JOIN pg_catalog.pg_index i ON i.indexrelid = c.oid
    JOIN pg_catalog.pg_class t ON i.indrelid   = t.oid
WHERE c.relkind = 'i'
    AND n.nspname NOT IN ('pg_catalog', 'pg_toast')
    AND pg_catalog.pg_table_is_visible(c.oid)
) as x );
-- ORDER BY n.nspname, t.relname, c.relname);

-- we order by index_columns so you can easily see where 2 overlap

