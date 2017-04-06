commit 76f3d6e501722bd822cfde7ab17f051293fa2f2e
Author: Sam Brannen <sam@sambrannen.com>
Date:   Wed Feb 5 15:41:47 2014 +0100

    Improve test coverage for AbstractMethodMockingControl

    This commit improves the test coverage for AbstractMethodMockingControl
    by introducing tests that verify expected behavior for:

     - reentrant method invocations via public methods
     - reentrant method invocations via private methods
     - test methods that do not set expectations or invoke playback()
     - test methods that throw exceptions

    For a more complete discussion of "after" vs. "after returning" advice
    within AbstractMethodMockingControl, see the Javadoc in the tests.