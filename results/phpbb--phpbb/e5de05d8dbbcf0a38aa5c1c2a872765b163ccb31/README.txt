commit e5de05d8dbbcf0a38aa5c1c2a872765b163ccb31
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Tue Jun 25 14:05:40 2013 -0400

    [feature/auth-refactor] Test for init on provider_apache

    Provides a test for the init() method of provider_apache.
    Appears to be failing due to an error with the mock request class.

    PHPBB3-9734