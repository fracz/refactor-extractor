commit 412fc3455730408d8b868bf30bafda20b803b8cc
Author: epriestley <git@epriestley.com>
Date:   Fri May 6 11:46:46 2016 -0700

    Improve inline mail snippet rendering, possibly fixing Airmail?

    Summary:
    Ref T10694. General improvements:

      - Remove leading empty lines from context snippets.
      - Remove trailing empty lines from context snippets.
      - If we removed everything, render a note.
      - Try using `style` instead of `<pre>`? My thinking is that maybe Airmail has weird default rules for `<pre>`, since that's the biggest / most obvious thing that's different about this element to me.

    Test Plan: Viewed normal comments locally, faked a comment on an empty line in the middle of a lot of other empty lines.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10694

    Differential Revision: https://secure.phabricator.com/D15864