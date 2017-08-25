commit b8610c4b989fd1e4e9e310de776de38dfe4a09a2
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Fri Jun 21 18:04:11 2013 -0400

    [feature/auth-refactor] Refactor code to use services

    Refactors all loading of auth providers to use services instead of
    directly calling the class.

    PHPBB3-9734