commit 2f6505014428405abf8f31c60a46ece6eb2ec4a2
Author: Marc Pujol <kilburn@la3.org>
Date:   Sat Feb 14 13:17:53 2015 +0100

    Improve sluggable:table command handling in L5

    This commit introduces several fixes to improve the sluggable:table command handling in L5:
    - Fix wrong path to the stubs folder
    - Customize the generated migration name to include the table and column being added
    - Avoid double-writing of the generated migration file

    This is accomplished through the following changes:
    - New SluggableMigrationCreator class that reuses L5's codebase (MigrationCreator) to generate the migration file
    - Simplified SluggableTableCommand that delegates stub handling logic to SluggableMigrationCreator
    - Edited SluggableServiceProvider to wire up the above classes