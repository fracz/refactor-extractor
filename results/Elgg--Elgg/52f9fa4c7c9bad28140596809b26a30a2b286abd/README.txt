commit 52f9fa4c7c9bad28140596809b26a30a2b286abd
Author: Steve Clay <steve@mrclay.org>
Date:   Tue Jan 14 19:58:53 2014 -0500

    perf(upgrade): speeds up migrating remember me codes

    The Remember me cookie implementation has been improved by moving codes
    to a separate table. This performs that migration in one query.

    Fixes #6204