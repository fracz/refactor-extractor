commit a0f534b87c1b1a2fed27198e7f5407b7315901f3
Author: epriestley <git@epriestley.com>
Date:   Mon Mar 10 17:59:13 2014 -0700

    Minor improvements to Diviner layout

    Summary:
      - Render functions as `func()` for consistency/clarity.
      - Sort articles first.
      - Sort case insensitively.
      - Label the "no group" symbols.

    Test Plan: Regenerated and examined docs.

    Reviewers: btrahan, chad

    Reviewed By: chad

    CC: aran

    Differential Revision: https://secure.phabricator.com/D8480