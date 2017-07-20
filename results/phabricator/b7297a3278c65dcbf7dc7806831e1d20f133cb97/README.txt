commit b7297a3278c65dcbf7dc7806831e1d20f133cb97
Author: epriestley <git@epriestley.com>
Date:   Sat Oct 5 12:04:31 2013 -0700

    Add an "Author's projects" Herald field to Differential

    Summary:
    Ref T1279. This allows installs to implement two different flavors of project review. They can either implement this rule:

      When:
        [ ... ] [ ... ]
      Take Action:
        [ Add blockign reviewers ] [ Security ]

    ...which means "every revision matching X needs to be signed off by someone else on the Security team, //even if the author is on that team//". The alternative is to implement this rule:

      When:
        [ Author's projects ] [ do not include ] [ Security ]
        [ ... ] [ ... ]
      Take Action:
        [ Add blocking reviewers ] [ Security ]

    ...which means that people on the Security team don't need a separate signoff from someone else on the team.

    I think this weaker version maps to some of what, e.g., Google does (you need to be reviewed by someone with "readability" in a language, but if you have it that's good enough), but I could imagine cases like "Security" wanting to prevent self-review from satisfying the requirement.

    @zeeg, not sure which of these use cases is relevant here, but either one should work after this.

    Test Plan: Created rules with this field, verified it populated properly in the transcript.

    Reviewers: btrahan

    Reviewed By: btrahan

    CC: zeeg, aran

    Maniphest Tasks: T1279

    Differential Revision: https://secure.phabricator.com/D7238