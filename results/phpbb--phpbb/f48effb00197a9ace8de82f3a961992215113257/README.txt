commit f48effb00197a9ace8de82f3a961992215113257
Author: Joseph Warner <hardolaf@hardolaf.com>
Date:   Mon Jul 1 22:37:55 2013 -0400

    [feature/auth-refactor] Fix the actual cause of test failures

    Enables super globals before the new container is instantiated in
    the final step of installation to prevent issues caused by trying
    to create a phpbb_request object when super globals are disabled.

    PHPBB3-9734