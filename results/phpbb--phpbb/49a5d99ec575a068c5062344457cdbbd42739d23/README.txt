commit 49a5d99ec575a068c5062344457cdbbd42739d23
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Mon Jun 17 16:35:06 2013 -0400

    [feature/auth-refactor] Auth Apache Provider Skeleton

    Creates a skeleton for Apache based authentication using the
    phpbb_auth_provider_interface named phpbb_auth_provider_apache.
    This brings over all code in auth_apache.php verbatim complete with
    all global variables currently in use.

    PHPBB3-9734