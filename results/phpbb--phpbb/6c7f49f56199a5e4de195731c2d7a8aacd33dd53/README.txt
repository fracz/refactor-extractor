commit 6c7f49f56199a5e4de195731c2d7a8aacd33dd53
Author: Igor Wiedler <igor@wiedler.ch>
Date:   Mon Feb 14 00:00:59 2011 +0100

    [task/refactor-db-testcase] Refactor phpbb_database_test_case

    Move most of the methods to a separate connection manager class. The
    test case creates a manager to handle database creation, schema loading
    and more. Most of the methods could be simplified because they can
    access shared pdo, config and dbms data.

    PHPBB3-10043