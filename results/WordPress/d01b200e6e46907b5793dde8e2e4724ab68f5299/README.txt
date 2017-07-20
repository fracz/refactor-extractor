commit d01b200e6e46907b5793dde8e2e4724ab68f5299
Author: Andrea Fercia <a.fercia@gmail.com>
Date:   Fri Jun 10 21:43:29 2016 +0000

    Accessibility: Help text improvements.

    Avoid references to "visual" positions in favour of positions in the document
    structure. The help text shouldn't assume users can see.

    Also, in the Posts screen don't mention specific types of posts and use a more
    generic text instead.

    Props odysseygate, pansotdev, zakb8.
    Fixes #34761.
    Built from https://develop.svn.wordpress.org/trunk@37680


    git-svn-id: http://core.svn.wordpress.org/trunk@37646 1a063a9b-81f0-0310-95a4-ce76da25c4cd