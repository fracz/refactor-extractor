commit 398eca609f3bf458e1b175e5de712c46cf672996
Author: Sergey Malenkov <sergey.malenkov@jetbrains.com>
Date:   Wed Feb 17 16:22:24 2016 +0300

    IDEA-151753 Scroll bar doesn't work in 'Pull Members Up/Down' refactoring dialogs
    call setComponentZOrder after layoutComponent
    to prevent missing some addNotify calls