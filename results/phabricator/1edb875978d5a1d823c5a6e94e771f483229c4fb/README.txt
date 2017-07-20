commit 1edb875978d5a1d823c5a6e94e771f483229c4fb
Author: Neal Poole <neal@fb.com>
Date:   Fri Oct 4 06:37:32 2013 -0700

    Adding support for 'adds' and 'removes' in diff content.

    Summary:
    Does what it says on the label. We already had 'Any changed file content', now we have 'Any added file content' and 'Any removed file content'.
    - There is a bit of copied/pasted code here: I'm open to suggestions on how to refactor it so it's less redundant.
    - The wording seems a little awkward, and as @epriestley mentioned in T3829, moved code will be detected less than ideally.

    Test Plan: Created Herald Rules, verified via dry run that they were triggered in appropriate situations.

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: Korvin, aran

    Maniphest Tasks: T3829

    Differential Revision: https://secure.phabricator.com/D7214