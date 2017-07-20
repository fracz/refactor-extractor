commit 567a65c1b0cfa94180d8672e032f3b9bd2381a1a
Author: mark_story <mark@mark-story.com>
Date:   Sat Jan 11 22:52:23 2014 -0500

    Expand logic to detect checked.

    Since value is a primitive attribute, re-using it for checked/selected
    state is a bit of a bad idea. This will probably lead to refactoring in
    selectbox and radio as well.