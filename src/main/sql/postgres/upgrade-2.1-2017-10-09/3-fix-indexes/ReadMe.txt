================================================================
upgrade-2.1-2017-09-12/3-fix-indexes/ReadMe.txt

This is the final set of scripts, to

Remove a column that was never needed or used
Fix 2 Primary Keys which had the first column name be the PK name
Add a constraint that USER_PRINCIPAL.USERNAME should be unique
Add some foreign keys
Fix indexes

In addition, some utility scripts are included to list foreign keys and indexes

You need not read further.  You can start with:
   mys @ 1a-chk-pat-study-email-col.sql


==== Check and remove a column that was never needed or used
1a-chk-pat-study-email-col.sql
1b-rm-pat-study-email-col.sql

==== Fix two Primary Keys which were each missing their first column and had the column name as the PK name

2a-chk-pk-for-user-pref.sql
2b-fix-pk-for-user-pref.sql

3a-chk-pk-for-survey_reg_attr.sql
3b-fix-pk-for-survey_reg_attr.sql

==== Add a constraint that USER_PRINCIPAL.USERNAME should be	unique

4a-chk-user-principal-name-uq.sql
4b-mk-user-principal-name-uq.sql

==== Add some foreign keys that were missing

5-add-3-foreign-keys.sql

==== Fix indexes

These change a bunch of standard indexes
6-fix-indexes.sql
7-remove-redundant-ixs.sql - identifies all of your redundant indexes
                             and prompts you with the commands to remove them.

UTILITIES

list-fks.sql      - Lists all the foreign keys and tells whether each is covered by an index
list-fks-noix.sql - Lists foreign keys that are not covered by indexes
list-ixs.sql      - Lists all indexes and marks those that are duplicates
list-dup-ixs.sql  - Lists duplicate indexes as DROP statements
list-table-pk.sql - Lists the primary key for a table defined in a variable "table"

This is the end of database upgrades for 2.1
###
