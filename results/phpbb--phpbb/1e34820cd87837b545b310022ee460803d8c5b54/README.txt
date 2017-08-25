commit 1e34820cd87837b545b310022ee460803d8c5b54
Author: Nils Adermann <naderman@naderman.de>
Date:   Sat Dec 16 20:24:34 2006 +0000

    - Optimize acl_getf_global a bit
    - a little performance improvement of the IP regular expressions
    - convert post_text/subject collation to utf8_unicode_ci if a user wants to use mysql_fulltext to allow case insensitivity [Bug #6272]
    - mysql_fulltext should alter all necessary columns at once to speed up the process
    - validate URLs against RFC3986
    - fixed some weirdness in make_clickable
    I hope I didn't break any URLs with this commit, if I did then report it to the bugtracker please!


    git-svn-id: file:///svn/phpbb/trunk@6774 89ea8834-ac86-4346-8a33-228a782c2dd0