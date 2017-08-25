commit 57689948e252ef3240b2c20be95923d6a0635ca9
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Tue Jun 18 15:39:51 2013 -0400

    [feature/auth-refactor] Make Apache consistent with interface

    Makes the provider_apache consistent with the provider_interface
    by removing the pass-by-reference of $username and $password.

    PHPBB3-9734