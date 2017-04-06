commit 68143dedf21279227e87d321cbf3ebf42126a72f
Author: Robert Muir <rmuir@apache.org>
Date:   Tue Jul 14 23:37:04 2015 -0400

    Refactor integration tests

    1. tests don't have a bogus test dependency on zips anymore,
       instead we handle this in pre-integration-test. This reduces
       lots of confusion for e.g. mvn clean test.
    2. refactor integ logic so that core/ and plugin/ share it.
       previously they were duplicates but the above change simplifies life.
       it also makes it easier for doing more interesting stuff