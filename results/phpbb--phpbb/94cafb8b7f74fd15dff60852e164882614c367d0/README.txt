commit 94cafb8b7f74fd15dff60852e164882614c367d0
Author: David M <davidmj@users.sourceforge.net>
Date:   Wed Feb 8 04:18:40 2006 +0000

    - A bug fix ( never noticed that the unset() touched the wrong var ) and a few minor (super minor) speed improvements
    + When from post inc to pre inc because pre is SLIGHTLY faster
    + The [] operations are very slow, it is easy enough to feed the array an index to insert at


    git-svn-id: file:///svn/phpbb/trunk@5536 89ea8834-ac86-4346-8a33-228a782c2dd0