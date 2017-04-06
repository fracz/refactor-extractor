commit 1ac1856dcb3cdec0686e9c05765ed838e8ce20f0
Author: Oliver Gierke <ogierke@pivotal.io>
Date:   Wed Apr 1 13:10:07 2015 +0200

    DATAJPA-698 - Upgraded to EclipseLink 2.6.0.

    Slightly refactored general infrastructure integration tests to make sure they're executed during the build. Added neccesary ignores to prevent the test cases from running into spec violations.