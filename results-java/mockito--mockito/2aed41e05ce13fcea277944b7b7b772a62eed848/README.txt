commit 2aed41e05ce13fcea277944b7b7b772a62eed848
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Mon Apr 7 13:05:29 2008 +0000

    massive checkin (worked in the plane :DDDD)
    -added javdoc for examples how to configure mockito
    -annotation style of creating mocks has additional benefit now: it uses the name of the field to print verification errors.
    -refactored the rest of matchers to assertors

    --HG--
    rename : src/org/mockito/internal/creation/MockNamer.java => src/org/mockito/internal/creation/ClassNameFinder.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40531