commit 9f9716f81fce69b944883948b5dc7d825812828c
Author: epriestley <git@epriestley.com>
Date:   Sat May 5 11:28:30 2012 -0700

    Fix Firefox upload fatal

    Summary:
    Several problems:

      - With fpm-warmup, 'PhabricatorAccessLog' is always loaded, even if it hasn't actually initialized. Use a global instead (barf). I'll fix this when I refactor index.php, hopefully soon.
      - The 'POST' check isn't sufficient in Firefox for HTML5 uploads -- not 100% sure why, maybe it encodes post bodies differently? I added an additional '__file__' requirement, and will add this param to GET on all file uploads in a future diff.

    See discussion in D2381.

    Test Plan: Uploaded files with Firefox via drag-and-drop without various mysterious errors.

    Reviewers: vrana, btrahan, jungejason

    Reviewed By: vrana

    CC: aran

    Differential Revision: https://secure.phabricator.com/D2405