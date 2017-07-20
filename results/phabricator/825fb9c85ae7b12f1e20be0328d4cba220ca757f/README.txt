commit 825fb9c85ae7b12f1e20be0328d4cba220ca757f
Author: epriestley <git@epriestley.com>
Date:   Tue Sep 3 17:27:38 2013 -0700

    Add JIRA doorkeeper and remarkup support

    Summary:
    Ref T3687. Adds a Doorkeeper bridge for JIRA issues, plus remarkup support. In particular:

      - The Asana and JIRA remarkup rules shared most of their implementation, so I refactored what I could into a base class.
      - Actual bridge implementation is straightforward and similar to Asana, although probably not similar enough to really justify refactoring.

    Test Plan:
      - When logged in as a JIRA-connected user, pasted a JIRA issue link and saw it enriched at rendering time.
      - Logged in and out with JIRA.
      - Tested an Asana link, too (seems I haven't broken anything).

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T3687

    Differential Revision: https://secure.phabricator.com/D6878