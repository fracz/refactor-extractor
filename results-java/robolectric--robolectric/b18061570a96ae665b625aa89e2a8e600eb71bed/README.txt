commit b18061570a96ae665b625aa89e2a8e600eb71bed
Author: Christian Williams <christianw@google.com>
Date:   Tue Apr 18 14:10:45 2017 -0700

    ShadowContentResolver improvements.

    Add #getStatements() including insert, bulkInsert, update, and delete.
    All insert, bulkInsert, update, and delete calls cause statements to be
            recorded for verification.
    ShadowContentResolver#setNextDatabaseIdForUpdates is deprecated; #update
            now returns 1 (row) by default.
    Better javadoc.