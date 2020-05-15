================================================================
oracle/upgrade-2.1-2017-09-12/ReadMe.txt

Release 2.1 includes some database changes.

You must NOT run the new server code until the first database change is made.
It's best if the server is not used while the changes are being made.
I know of no way that having the server running will harm anything, but in step 2,
it's remotely possible you'll have to repeat a step if someone is using the server.


THE REASONS FOR THE CHANGES

We found some bugs in the database layer and in the code and add added some improvements.
Since the database needs an update, we also added an Enabled column to the small Survey_Site table.

1. Database Bug: When our schema-creation script specified a unique index, the uniqueness was being lost.
We fixed the database layer, and scripts in this update will add uniqueness to the tree indexes that need it.

2. Code Bug: Our UI code was sometimes creating a patient attribute twice quickly.  Possibly,
redundant attributes were created in the database to a table where uniqueness was missing on an index.
Before adding uniqueness, you'll apply scripts to find and eliminate any redundant attributes.
If redundant attribute creation occurs after uniqueness is added to the index, the second try would
be stopped by a database exception due to the uniqueness constraint.
We have also fixed the UI so it won't try to create the attribute twice.

3. Index creation bug:  The method call to create a Primary Key takes a name for the PK followed by column names.
Two calls to create PKs had multiple columns in the PK but lacked the name,
so these were using the first column name instead, and that first column was omitted from the Primary Key.
Scripts will fix the primary keys for these two tables and the database layer now prohibits the PK name
from being a column name to prevent this in the future.

4. Database Bug: The database layer automatically created an index for a foreign key if none existed.
But this is not needed if the foreign key column(s) is the first column(s) in an existing index.
The database layer was fixed to look for these, and scripts will remove these redundant indexes.

5. An extra field was found. The PATIENT_STUDY table mistakenly has an unused, unwanted EMAIL_ADDR column.
A script will remove this.

6. Missing foreign keys:  There were a few foreign key relations that were missing.
Scripts add them and indexes, if needed.

7. In reviewing the indexes, a couple more seemed needed and some could have columns added to them
to help with queries.  In general, if an index specifies more columns, it is more useful and there's
no speed cost. But having more indexes makes the database a little slower.  So we will add some indexes
with more columns and remove some others.  (Note that one can't add or alter index columns- one must
drop the old one and add the replacement.)

At the end there will be a query that tells you all the foreign keys not covered by an index.
It's not imperative that there are none, but it's a good practice.  There can be a performance
penalty if such an index is missing if a row is deleted or a foreign key column is updated.


FYI:  OTHER 2.1 CHANGES

There are 2 other interesting database-related changes in the 2.1 software

The scheduler page is now populated by three queries.
Previously, to populate it with N appointments it was making 1+2N queries.

The user cache was being refreshed every 30 seconds or so by reading in the user table.
Now, a quick query is done to see if any changes have been made, and it's only being refreshed 
if one or more users have been added or changed.



USE SQLPLUS

All the scripts are interactive sqlplus scripts,
so you'll use sqlplus to apply the changes to your database.
If you do not have sqlplus, search on the web for:  download sqlplus

I use an alias "mys" to access sqlplus, so you'll see this in the script.
You can define an alias if you're running the bash shell on Linux or a mac.
It's best to put this in your $HOME/.profile file.
This is my alias (replace <text> with the appropriate information):

    alias mys='sqlplus <user>/<password>@<host>:1521/<SID>'

The user is the registry.database.user that has access only to its schema,
not the registry.database.system.user which is used to create the schema.

Note that when you run sqlplus, you can give it an '@' and a file name to pull
commands from a script.  Once running sqlplus, you can also type @filename
to run a script.  The scripts check or change something, show you the results
and then prompt you with the next script so you can just copy/paste.

All the scripts are safe to run more than once (for instance, if you forget
to copy the next @script command before you paste, so accidentally paste the
last one again.)  But running a script twice will often display database errors, such
as adding a foreign key or index that is already present, or removing one that
no longer exists.  And all the scripts have a header so you can scroll back through
the output easily and see where the last script started.


FYI: CAPTURING YOUR SESSION

On a production system, if you're on Mac or Linux, I would run the whole thing
under the script command.  Just type "script" at the command line and it'll capture
your whole terminal session into a file until you hit control-d or type 'exit'.
(Note you shouldn't run an editor, like vi, when recording.)
You can give it a file name.  I use:

    % script "choir2.1-update-"`date +%y%m%d-%H-%M`.log

It echoes this filename when it starts and ends:
    Script started, output file is choir2.1-update-170907-13-20.log

Note that it starts a new shell, so you will need to define your mys alias again.
If you wrote this into your $HOME/.profile script, you can load it with:  source $HOME/.profile


MAKE THE CHANGES, IN 3 GROUPS

The changes are in 3 groups, so you'll have to run mys 3 times.
Running all these changes takes about 5 minutes, PLUS time to read the ReadMe files.
When finished with a section, the script prompts you to start the next.

Here are the 3 groups

1. Add the new ENABLED column to the SURVEY_SITE table with a default value of 'Y'.
Also adds some new columns to two square-tables, if you have them.
Do this now:

% mys @ 1-add-site-enable-col.sql

The other two groups of changes have multiple steps and multiple scripts,
so cd to into the directory, read the readme.txt file and run sqlplus there.


2.
% cd 2-unique-ixs
% more ReadMe.txt
% mys @ 1-*
then follow the prompts

and when finished: cd ..


3.
% cd 3-fix-indexes
% more ReadMe.txt
% mys @ 1-*
then follow the prompts


Please rest assured these scripts are much, much easier to use than they were to write.
-Randy
###
