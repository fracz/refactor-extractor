commit 6679bf239adc6d2e0447f29c66f9081009a69dc2
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Tue Sep 6 11:32:51 2016 +0300

    provide isAmend method for Upsource plugin

    It needs to check if user selected the next commit to amend.
    Ability to access this data was removed during refactoring in 2feb9cd