commit 0ab53dcb31e7ae52cc265c8020df538df90ed2d7
Author: Felipe Leme <felipeal@google.com>
Date:   Thu Feb 23 08:33:18 2017 -0800

    Auto-fill logging improvements:

    - Shorten history line by removing less relevant items.
    - Decrease number of history items (from 100 to 20).
    - Removed redundant logs from service (and kept them on manager)
    - Use {} on all log statements.
    - Remove DEBUG guard on some important messages.
    - Remove DEBUG guard from service-side toString() implementations.
    - Don't log FLAG_FOCUS_LOST actions.

    BUG: 33197203
    Test: manual verification
    Change-Id: I30466ab3c0d12cfa2ad68b2b2a107d5890256845