commit e64abea999f68b248cfe41ab22ac60abc9e2951f
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Tue Jun 18 15:17:14 2013 -0400

    [feature/auth-refactor] Document the provider interface

    Provides basic documentation of the auth_provideR_interface.
    Changes the login method to login($username, $password) for
    consistency with the providers.
    acp() is not fully documented. It appears that it is meant to
    return an array of some sort and take in a variable by reference.

    PHPBB3-9734