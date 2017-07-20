commit fe500f4268a2898289d013384277856c983747a8
Author: epriestley <git@epriestley.com>
Date:   Tue Feb 26 14:57:41 2013 -0800

    Pre-prepare for hovercards

    Summary:
    D5120 and followups refactor and generalize object references in Remarkup -- notably, they move remarkup rules from a central location to the implementing applications.

    Preserve blame by doing moves/renames only first. This change moves application remarkup rules into those applications, and renames the ones D5120 modifies.

    Test Plan: Typed some preview text into a textarea, got a valid Remarkup render.

    Reviewers: vrana, chad

    Reviewed By: vrana

    CC: aran

    Differential Revision: https://secure.phabricator.com/D5123