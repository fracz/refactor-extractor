commit ed4a155c9168c0d69b536594f4fa4a70f510beea
Author: epriestley <git@epriestley.com>
Date:   Tue Aug 7 11:54:06 2012 -0700

    Rename "IDPaged" to "CursorPaged", "executeWithPager" to "executeWith[Cursor|Offset]Pager"

    Summary:
    I'm trying to make progress on the policy/visibility stuff since it's a blocker for Wikimedia.

    First, I want to improve Projects so they can serve as policy groups (e.g., an object can have a visibility policy like "Visible to: members of project 'security'"). However, doing this without breaking anything or snowballing into a bigger change is a bit awkward because Projects are name-ordered and we have a Conduit API which does offset paging. Rather than breaking or rewriting this stuff, I want to just continue offset paging them for now.

    So I'm going to make PhabricatorPolicyQuery extend PhabricatorOffsetPagedQuery, but can't currently since the `executeWithPager` methods would clash. These methods do different things anyway and are probably better with different names.

    This also generally improves the names of these classes, since cursors are not necessarily IDs (in the feed case, they're "chronlogicalKeys", for example). I did leave some of the interals as "ID" since calling them "Cursor"s (e.g., `setAfterCursor()`) seemed a little wrong -- it should maybe be `setAfterCursorPosition()`. These APIs have very limited use and can easily be made more consistent later.

    Test Plan: Browsed around various affected tools; any issues here should throw/fail in a loud/obvious way.

    Reviewers: vrana, btrahan

    Reviewed By: vrana

    CC: aran

    Maniphest Tasks: T603

    Differential Revision: https://secure.phabricator.com/D3177