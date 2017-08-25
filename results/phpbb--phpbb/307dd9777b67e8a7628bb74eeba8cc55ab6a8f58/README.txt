commit 307dd9777b67e8a7628bb74eeba8cc55ab6a8f58
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Tue Jun 25 14:12:31 2013 -0400

    [feature/auth-refactor] Test login() for provider_apache

    Provides a test for the login() method for provider_apache.
    Appears to be failing due to an issue with the mock phpBB request
    class.

    PHPBB3-9734