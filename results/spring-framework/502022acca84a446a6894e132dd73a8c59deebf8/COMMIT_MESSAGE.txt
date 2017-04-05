commit 502022acca84a446a6894e132dd73a8c59deebf8
Author: Sam Brannen <sam@sambrannen.com>
Date:   Wed Jun 25 21:25:00 2014 +0200

    Delete test annotations in spring-orm

    This commit deletes all test annotations from the spring-orm module in
    order to reduce unnecessary confusion between these "copies" and the
    real, current versions of such classes in the spring-test module.

    Furthermore, the legacy abstract JUnit 3.8 base classes and test cases
    have been refactored accordingly.