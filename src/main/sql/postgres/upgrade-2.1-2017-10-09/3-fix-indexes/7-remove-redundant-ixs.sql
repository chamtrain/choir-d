-- PostGres script
\set IGNOREEOF 3
\echo
\echo
\echo
\echo
\echo '============= 7'
\echo '============= Remove any redundant indexes'

\echo '== The database layer created indexes for foreign keys if no exact match existed.'
\echo '==   This resulted in redundant indexes. '
\echo '==   An index A is redundant if there is an index B on the same table with the'
\echo '==      same columns plus more.  So A(col2,col4) is redundant with B(col2,col4,col5)'
\echo '==   Redundant indexes should be removed'
\echo
\echo '== This lists all indexes that are redundant, and thus should be dropped.'

\i list-dup-ixs.sql

-- The "group by" gets rid of redundant statements (occurs if A is a dup of both B and C)

\echo '== If there are rows above, copy and paste them to drop redundant indexes'
\echo
\echo '== To inspect all the indexes, with duplicates marked, see:    \\i list-ixs.sql'
\echo '== You can run this again afterwards to verify they are gone:  \\i 7-remove-redundant-ixs.sql'
\echo '== You can identify unindexed foreign keys with:               \\i list-fks-noix.sql'
\echo
\echo '== Before you leave, drop any temporary tables. Below, we find them with:  \\dt tmp_*'
\dt tmp_*

\echo '== When you are finished, leave with:  \\q'
\echo '== If you are running "script" to capture the output, at the shell prompt, type:  exit'
\echo
