commit daee13f55ade478e83a30326b56cfa8e377195bb
Author: Guy Carmeli <guyc@wix.com>
Date:   Tue Jun 14 13:50:02 2016 +0300

    Fix buttons not getting set in Modals

    * Due to race condition when showing modal, buttons were added to
    previous Toolbar.

    * Minor refactor, moved `updateStyles` from BaseReactActivity to
    StyleHelper class since it's now also used by RnnModal