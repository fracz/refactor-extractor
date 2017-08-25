commit cecdbaa4c3074992c900e3f161d6f43582357811
Author: David M <davidmj@users.sourceforge.net>
Date:   Thu Feb 15 20:30:41 2007 +0000

    #7570
    #7232
    - pretty much rewrote backup/restore. whatever is not new code is refactored. Download now works properly with gzip on the fly, bzip2 is still very much not on the fly ( will to take suggestions on this ). Should *never* hit any memory issue. Loads of bug fixes, tested with every DB we support


    git-svn-id: file:///svn/phpbb/trunk@6988 89ea8834-ac86-4346-8a33-228a782c2dd0