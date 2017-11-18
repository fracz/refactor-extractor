commit 1ce0161e30de2585f67e9c42ad98b33ab0333f46
Author: Fyodor Kupolov <fkupolov@google.com>
Date:   Fri Aug 26 11:39:07 2016 -0700

    Extracted persistence layer into a separate class

    Introduced AccountsDb class and moved all DB-accessing methods there.
    The methods are organized by the database:
    - DeDatabaseHelper - provides access to accounts_de data
    - CeDatabaseHelper - access to accounts_ce
    - PreNDatabaseHelper - migration logic from a pre-N single database to two databases in N
    - DebugDbHelper - debug table + helper methods

    Notable improvements:
    - logRecord methods no longer opens SQLiteDatabase (it was actually unused
      down in the call chain)
    - Clean separation between business and persistence logic - no more
      sql statements concatenation in the AMS code.

    Test: Refactoring CL.

    Bug: 30639520
    Change-Id: I41bd1abe47a23efbc735344413f32cbb68a5c8af