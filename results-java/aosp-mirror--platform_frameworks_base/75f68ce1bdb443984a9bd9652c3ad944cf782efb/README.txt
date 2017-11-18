commit 75f68ce1bdb443984a9bd9652c3ad944cf782efb
Author: Xiaohui Chen <xiaohuic@google.com>
Date:   Fri Aug 14 09:28:07 2015 -0700

    Clean up USER_OWNER in AccountManager

    Refactor copyAccountUser to take an explicit user handle instead of
    hardcode to owner.  This requires refactoring one app that uses this
    Api.

    Bug: 19913735
    Change-Id: Ib9b11d8155bea2a58974d09ec2d70bc756d46313