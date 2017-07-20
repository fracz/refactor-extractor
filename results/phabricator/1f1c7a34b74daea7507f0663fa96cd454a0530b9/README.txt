commit 1f1c7a34b74daea7507f0663fa96cd454a0530b9
Author: vrana <jakubv@fb.com>
Date:   Fri Apr 13 11:30:01 2012 -0700

    Improve image macros

    Summary:
    Couple of small improvements:

    - Delete `randomon` macro.
    - Make name unique (deleting current conflicts randomly).
    - Image macro must be alone on the line.
    - Filter by name.

    Test Plan:
    Run SQL.
    /file/macro/
    /file/macro/?name=imagemacro
    Try to create conflicting name.
    Write this comment:

      Test imagemacro.
      imagemacro

    Reviewers: aran, epriestley

    Reviewed By: epriestley

    CC: epriestley, Koolvin

    Differential Revision: https://secure.phabricator.com/D2230