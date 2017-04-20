commit 93fc188afb016b44f0b4e464621bfca721979cc5
Author: CommitSyncScript <jeffmo@fb.com>
Date:   Tue Jun 4 12:15:13 2013 -0700

    style prop improvements

    Some improvements to how style={{x:y}} is handled in React:
    * ignores null styles, rather than setting them.

    Codez:

        var highlighted = false;
        <div style={{color: highlighted ? 'red' : null}} />

    Before:

        <div style="color:;"></div>

    After:

        <div></div>

    Respects that 0 has no units.