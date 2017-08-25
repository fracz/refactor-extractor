commit 823f2571f5443e3529327047f0d50d0955a4be45
Author: Andreas Fischer <bantu@phpbb.com>
Date:   Mon Apr 12 21:08:55 2010 +0200

    [ticket/9536] Small improvement for query against user/session tables when managing users from the ACP.

    There can be multiple entries in the session table for one user_id. We only need and also only fetch one. Using LIMIT 1 should therefore increase performance slightly. This is especially true when editing the anonymous user account because the session table can have many entries for the user_id ANONYMOUS.

    PHPBB3-9536