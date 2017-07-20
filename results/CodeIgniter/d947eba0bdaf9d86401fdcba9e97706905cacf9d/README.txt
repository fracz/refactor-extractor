commit d947eba0bdaf9d86401fdcba9e97706905cacf9d
Author: Andrey Andreev <narf@bofh.bg>
Date:   Mon Apr 9 14:58:28 2012 +0300

    Multiple DB Forge improvements
     - Replaced driver methods _create_database(), _drop_database(), _drop_table() and _rename_table() with properties
     - Added defaults for the above mentioned platform-specific queries, so that not all drivers need to define them
     - Improved support for the SQLite, ODBC and PDO drivers