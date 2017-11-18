commit 5bb4e29aba21bf52873bc242102cdfabd9ec4aaa
Author: Fabrice Di Meglio <fdimeglio@google.com>
Date:   Tue Oct 2 15:53:00 2012 -0700

    Fix bug #7274075 Non-functional CheckedTextView on Nexus 7 Jelly Bean 4.1.1

    This regression has been introduced by this Change: Ia846de16bbc54f0729608259aa4b530da9404245

    - in CHOICE_MODE_SINGLE you need to clear the checked states before doing anything
    - rename variables for better readability too

    Change-Id: I89b4390e5ebb192ca280fea2c06f991b537a2870