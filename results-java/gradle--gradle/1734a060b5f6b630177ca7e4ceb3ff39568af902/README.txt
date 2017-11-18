commit 1734a060b5f6b630177ca7e4ceb3ff39568af902
Author: adammurdoch <a@rubygrapefruit.net>
Date:   Wed Apr 28 13:36:07 2010 +1000

    GRADLE-907
    - Replaced usage of Ant's JunitTestRunner with our own, to improve error handling and get better access to stdout capturing.
    - Refactored JUnitTestResult test fixtures
    - Fixed test class/name detection for early 4.x versions