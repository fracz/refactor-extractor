commit 212235d3e23bf06decde2f10b96b57fbad46e593
Author: tjhunt <tjhunt>
Date:   Mon Dec 15 06:22:18 2008 +0000

    accesslib: MDL-17647, MDL-17648 and MDL-17649 Bug fix, improvement and unit test.

    MDL-17647 was referring to moodle/site:candoanything insstead of moodle/site:doanything

    MDL-17648 Let get_users_by_capability take an array of capabilities, like has_any_capability

    MDL-17649 get_users_by_capability must have unit tests (HEAD only).

    The unit tests were briefly working (apart from the system context, which I had to set up by hand in the test contexts table). Then I made the mistake of trying to upgrade the test tables, and it all went horribly wrong (MDL-17644).