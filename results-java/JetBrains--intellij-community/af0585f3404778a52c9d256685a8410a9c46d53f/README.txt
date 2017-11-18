commit af0585f3404778a52c9d256685a8410a9c46d53f
Author: Kirill Likhodedov <kirill.likhodedov@jetbrains.com>
Date:   Wed Apr 13 11:51:30 2011 +0400

    HgPushAction refactoring

    Transform to HgAction, get rid of HgBuilderCommands.
    HgPusher to show dialog and perform push, to be able to call it without action.