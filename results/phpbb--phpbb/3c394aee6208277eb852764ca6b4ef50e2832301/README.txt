commit 3c394aee6208277eb852764ca6b4ef50e2832301
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Tue Jun 25 22:21:38 2013 -0400

    [feature/auth-refactor] Refactor auth in acp_board

    Changes the acp_board code to directly call the auth providers out
    of the $auth_providers variable that is populated by the
    phpbb_container.

    PHPBB3-9734