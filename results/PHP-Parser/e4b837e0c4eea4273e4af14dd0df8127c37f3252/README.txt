commit e4b837e0c4eea4273e4af14dd0df8127c37f3252
Author: Nikita Popov <nikita.ppv@googlemail.com>
Date:   Fri Oct 2 11:14:31 2015 +0200

    Split up pretty printer test in stmt/expr

    The list was getting unweildly.

    Also improve error message when parsing fails in pretty printer
    test and extend some tests.