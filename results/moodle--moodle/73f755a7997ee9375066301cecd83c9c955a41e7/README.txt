commit 73f755a7997ee9375066301cecd83c9c955a41e7
Author: Dan Poltawski <dan@moodle.com>
Date:   Tue Nov 11 19:23:51 2014 +0000

    MDL-47502 statslib tests: improve test

    While integrating I noticed some problems which already existed in this
    test:
    * The enrolment data generator was not used
    * The student roleid was hardocded
    * The role assignment was at site level..