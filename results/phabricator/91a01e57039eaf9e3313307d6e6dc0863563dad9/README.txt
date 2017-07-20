commit 91a01e57039eaf9e3313307d6e6dc0863563dad9
Author: epriestley <git@epriestley.com>
Date:   Wed Jan 6 06:41:38 2016 -0800

    Improve Diffusion browse performance for large files

    Summary:
    When looking at a large file in Diffusion:

      - disable highlighting if it's huge and show a note about why;
      - pick up a few other optimizations.

    Test Plan: Locally, this improves the main render of `__phutil_library_map__.php` from 3,200ms to 600ms for me, at the cost of syntax highlighting (we can eventually add view options and let users re-enable it).

    Reviewers: chad

    Reviewed By: chad

    Differential Revision: https://secure.phabricator.com/D14959