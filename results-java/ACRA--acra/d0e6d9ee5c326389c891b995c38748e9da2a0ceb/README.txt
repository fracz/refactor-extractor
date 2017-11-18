commit d0e6d9ee5c326389c891b995c38748e9da2a0ceb
Author: Jay Soffian <jaysoffian@gmail.com>
Date:   Thu Jan 15 10:54:19 2015 -0500

    checkReportsOnApplicationStart: only check for new version if configured to delete old reports

    Small optimization to improve startup time for folks who don't
    care about deleting old reports upon app upgrade.