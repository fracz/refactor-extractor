commit 7e6008f0b9a84c16ba3cd6aeb8a6395e8fbfe88e
Author: Robert Muir <rmuir@apache.org>
Date:   Tue Nov 3 13:39:17 2015 -0500

    refactor GroovySecurityTests into a unit test.

    This was basically a resurrected form of the tests for the old sandbox.
    We use it to check that groovy scripts some degree of additional containment.

    The other scripting plugins (javascript, python) already have this as a unit test,
    its much easier to debug any problems that way.

    closes #14484