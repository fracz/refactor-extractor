commit 44f6ca9e11a8c4e8540cbde03d8e63aaa8bff15d
Author: mudrd8mz <mudrd8mz>
Date:   Tue Sep 22 21:18:29 2009 +0000

    MDL-20334 re-fix - small performance improvement

    As Petr suggested, is_array() is faster than empty() so it makes sense
    to check it first.