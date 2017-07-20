commit 5dccc14bbf46b0cfaabf1d595c5f446c6e0302c1
Author: epriestley <git@epriestley.com>
Date:   Fri Sep 4 10:34:39 2015 -0700

    Modularize generation of supplemental login messages

    Summary:
    Ref T9346. This mostly allows us to give users additional advice based on which instance they are trying to log in to in the Phacility cluster.

    It's also slightly more flexible than `auth.login-message` was, and maybe we'll add some more hooks here eventually.

    This feels like it's a sidegrade in complexity rather than really an improvement, but not too terrible.

    Test Plan:
      - Wrote the custom handler in T9346 to replicate old config functionality.
      - Wrote a smart handler for Phacility that can provide context-sensitive messages based on which OAuth client you're trying to use.

    See new message box at top (implementation in next diff):

    {F780375}

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T9346

    Differential Revision: https://secure.phabricator.com/D14057