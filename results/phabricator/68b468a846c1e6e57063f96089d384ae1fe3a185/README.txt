commit 68b468a846c1e6e57063f96089d384ae1fe3a185
Author: epriestley <git@epriestley.com>
Date:   Thu Mar 10 16:33:44 2016 -0800

    Partially improve threading UI for adjacent inline comments

    Summary:
    Ref T10563. This isn't a complete fix, but should make viewing complex inline threads a little more manageable.

    This just tries to put stuff in thread order instead of in pure chronological order. We can likely improve the display treatment -- this is a pretty minimal approach, but should improve clarity.

    Test Plan:
    T10563 has a "before" shot. Here's the "after":

    {F1169018}

    This makes it a bit easier to follow the conversations.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10563

    Differential Revision: https://secure.phabricator.com/D15459