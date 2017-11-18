commit 0f820afa86bf4104a3da02706b3afe9fb1b14e44
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Thu Mar 3 14:31:00 2016 +0100

    Auto-configure GitInfo

    This commit moves `GitInfo` to a general "project info" area that will be
    further improved with others project related information.

    Deprecate `spring.git.properties` in favour of `spring.info.git.location`

    Closes gh-2484