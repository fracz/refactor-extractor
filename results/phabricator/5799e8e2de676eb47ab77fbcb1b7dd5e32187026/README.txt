commit 5799e8e2de676eb47ab77fbcb1b7dd5e32187026
Author: epriestley <git@epriestley.com>
Date:   Fri Sep 27 08:43:50 2013 -0700

    Provide better strings in policy errors and exceptions

    Summary:
    Ref T603. This could probably use a little more polish, but improve the quality of policy error messages.

      - Provide as much detail as possible.
      - Fix all the strings for i18n.
      - Explain special rules to the user.
      - Allow indirect policy filters to raise policy exceptions instead of 404s.

    Test Plan: See screenshots.

    Reviewers: btrahan, chad

    Reviewed By: chad

    CC: aran

    Maniphest Tasks: T603

    Differential Revision: https://secure.phabricator.com/D7151