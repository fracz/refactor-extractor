commit 5f7831158439f92f33c987d5d29dc9546bfe7c79
Author: Chris Craik <ccraik@google.com>
Date:   Mon Apr 8 14:52:51 2013 -0700

    Add input/output JSON data for baseline comparison

    CanvasCompare will output a JSON file with test results, and can take
    such files as input for baseline comparison. The new logcat output
    breaks down results into per-test and per-modifier improvement and
    regressions.

    Change-Id: I4da0251db0637841173ac95e9f431a7ff52c8b61