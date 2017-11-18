commit 5f8e731f18c5cbfd345236c691db0b09aceb7c5d
Author: Yohei Yukawa <yukawa@google.com>
Date:   Thu Dec 10 00:58:55 2015 -0800

    Remove unnecessary parameter that is always true.

    This is a mechanical refactoring that removes an unnecessary parameter
    that is always specified to 'true'.

    No behavior change is intended.

    Bug: 22859862
    Change-Id: If3aef8209a355af1432ca2600bcc3a0027a6c24c