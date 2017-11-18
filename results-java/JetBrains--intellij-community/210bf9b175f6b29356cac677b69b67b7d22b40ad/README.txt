commit 210bf9b175f6b29356cac677b69b67b7d22b40ad
Author: Dmitry Trofimov <dmitry.trofimov@jetbrains.com>
Date:   Mon Oct 9 20:30:43 2017 +0200

    Unify stubs for all Python language levels (PY-26392)

    The cause of the problem was the difference in parsing for different
    language levels.

    This commit improves error recovery and adds support for stubs for
    syntax elements unavailable in older Python versions.
    Cases that are covered are necessary for stubs generation
    for Python 3.6 standard library. They are discovered by automatic
    validation executed by stubs generator in StubsGenerator.kt.

    Those are the cases that were fixed:
    * A stub for single star argument in Py2 was absent
    * Type annotations in Py2 were absent
    * Print with end argument in Py2 broke parser
    * Exec function as argument in Py2 broke parser
    * Async keyword after decorator broke parser

    All the cases are listed as test cases in PyUnifiedStubsTest

    P.S. It is possible though that more cases will be revealed in future.