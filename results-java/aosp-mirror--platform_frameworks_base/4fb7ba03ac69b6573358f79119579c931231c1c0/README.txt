commit 4fb7ba03ac69b6573358f79119579c931231c1c0
Author: Guang Zhu <guangzhu@google.com>
Date:   Thu May 7 10:51:29 2015 -0700

    improve app crash detection in compatibility test

    In addition to ensuring that process exists, we also need to
    check that it's the right state. Because crashed foreground
    activity can still get started as background service.

    Bug: 20899208
    Change-Id: I101e556ce757af1afb66686827c5851dd6fda620