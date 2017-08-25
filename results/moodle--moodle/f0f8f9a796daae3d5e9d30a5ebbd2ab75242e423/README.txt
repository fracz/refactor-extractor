commit f0f8f9a796daae3d5e9d30a5ebbd2ab75242e423
Author: Petr Skoda <commits@skodak.org>
Date:   Fri Oct 21 16:35:19 2011 +0200

    MDL-29866 page setup improvements

    incorrect PAGE init - it should be done at the very end; redirect() should not use OUTPUT before PAGE init; SITEID should be deprecated in favour of $SITE->id (this is going to cause troubles in tenant switching in CLI, cron and tests); missing "global $SITE"'; minor coding style issues; PHPDocs; it also helps with merging/testing of multitenant patch