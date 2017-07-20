commit bf3b51adaf89c2850ce4b4c44e8b0a21cef32e00
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Wed Jun 1 17:51:27 2016 +0000

    Accessibility: improve the color contrast in the Edit Comment "Status" box.

    The current orange and red used for the radio button labels in the Edit Comment
    "Status" box don't have a sufficient color contrast ratio with the background.
    Removing the colors improves accessibility and consistency.

    See #35659, #35622.
    Fixes #36967.
    Built from https://develop.svn.wordpress.org/trunk@37611


    git-svn-id: http://core.svn.wordpress.org/trunk@37579 1a063a9b-81f0-0310-95a4-ce76da25c4cd