commit b457a4001ce2380e97f36b0a983b477c3e31de69
Author: Andrey Andreev <narf@bofh.bg>
Date:   Mon Apr 9 16:11:56 2012 +0300

    DB Utility improvements
     - Replaced driver methods _list_databases(), _optimize_table() and _repair_table() with properties
     - Added defaults for optimize_table() and repair_table() SQL strings (FALSE, as those are mostly MySQL-specific)
     - Added MSSQL/SQLSRV support for optimize_table() (actually rebuilds table indexes)
     - Switched public driver methods to protected
     - Improved CUBRID support for list_databases() as it used to only return the currently used database
     - Minor speed improvements