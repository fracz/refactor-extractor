commit 22fc6e17165e47e5347772aaeba4d07f20eb264d
Author: Ilya.Kazakevich <Ilya.Kazakevich@jetbrains.com>
Date:   Mon Jun 19 23:38:43 2017 +0300

    PY-24770: CleanUp and unify test class detection

    PythonUnitTestUtil been refactored, see its doc.
    Code that is not test-case specific moved to PyClassExt.kt

    New logic is the following:
    * UnitTest believes any TestCase inheritor is test case, so its method
    is test, its parent file is test and so on.
    * Other runners think that any "test_" function is test when it is located
    on toplevel or in test class (see PythonUnitTestUtil)