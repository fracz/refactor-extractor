commit 8779802fa1a27c81215f8c0c469611bad41893f4
Author: Muyuan Li <muyuanli@google.com>
Date:   Thu Apr 7 17:51:25 2016 -0700

    sysui: refactored for extension.

    Refactored StackScrollAglorithm and NotificationStackScrollLayout to
    implemment pinned top expandable view. For StackScrollAlgorithm, the
    logic of computing Y translation and Z translation are extracted from
    the for loop body.

    Bug: 27101250
    Change-Id: Id54af4d758e9ed692c01363de90e85dee3b2d026
    (cherry picked from commit 0f5a0d2ebdb7a979af48b42a00ff8d1f7b57a5b7)