commit 7f0fb63c445027cd5c9148bd9d8f7d4263b68802
Author: epriestley <git@epriestley.com>
Date:   Thu Jul 17 15:49:00 2014 -0700

    Modernize "owner" typeahead datasource

    Summary: Ref T4420. This one is users plus "upforgrabs". I renamed that to "none" and gave it a special visual style to make it more discoverable. Future diffs will improve this.

    Test Plan:
      - Used it in global search.
      - Used it in batch editor.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T4420

    Differential Revision: https://secure.phabricator.com/D9891