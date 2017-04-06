commit 587e8e2ba5feb275f14d259afb72ae6b77dfef18
Author: Stefan hr Berder <stefan.berder@ledapei.com>
Date:   Fri Sep 6 17:00:26 2013 +0800

    refactor(select): simplify the ngOptions regular expression

    \w matches [a-zA-Z0-9_] and \d matches [0-9], using both in a character set is
    simply redundant.

    Closes #3903