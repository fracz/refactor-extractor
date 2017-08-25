commit 257e3f4c61a0e9b2da11aeb85e2ac8ee13fd2b73
Author: defacer <defacer>
Date:   Tue Apr 12 01:09:31 2005 +0000

    Utilize $CFG->calendar_adminseesall and part of Penny's patches (for bug 2804)
    to improve performance: if the admin wants to see all events, there's no need
    to make even one query for course groupmode.