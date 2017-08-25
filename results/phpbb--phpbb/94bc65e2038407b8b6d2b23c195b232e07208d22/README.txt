commit 94bc65e2038407b8b6d2b23c195b232e07208d22
Author: Joas Schilling <nickvergessen@gmx.de>
Date:   Fri Mar 26 16:39:37 2010 +0100

    [feature/dbal-tests] Added database test & refactored test framework

    There is now a phpbb_database_test_case which can be used as a base class for tests that require database access. You have to set up a test_config.php file in your tests/ directory containing host, user, pass etc.

    Extra test functionality has been moved to phpbb_test_case_helpers to provide the same functionality in database tests and regular tests without duplicating the code. This is achieved through delegation of method calls.