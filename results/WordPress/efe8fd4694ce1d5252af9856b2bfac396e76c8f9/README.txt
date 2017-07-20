commit efe8fd4694ce1d5252af9856b2bfac396e76c8f9
Author: Andrew Ozz <admin@laptoptips.ca>
Date:   Sat Dec 6 23:53:22 2014 +0000

    TinyMCE, improve accessibility:
    - Return focus to the editor on pressing Escape while the image toolbar is focused.
    - Add a Close button to the Help modal and close it on Escape.
    - Override the title on the editor iframe (read by screen reader apps), replace with the Alt+Shift+H shortcut.
    - Add focus shortcuts descriptions to the Help modal.
    Fixes #27642.
    Built from https://develop.svn.wordpress.org/trunk@30757


    git-svn-id: http://core.svn.wordpress.org/trunk@30747 1a063a9b-81f0-0310-95a4-ce76da25c4cd