commit 31eeb5e84a81fef5880e78de16450837d1c12de6
Author: Mikhail Golubev <mikhail.golubev@jetbrains.com>
Date:   Tue Apr 7 22:49:19 2015 +0300

    Fix test data in several tests that failed after PyUtil#deleteElementSafely was introduces

    * `pass` is consistently inserted on the new line now
    * excess spaces left after element was deleted are removed

    Use PyUtil#deleteElementSafely in pull, push and extract superclass
    refactoring implementations.

    It turned out that all problems with whitespaces left after we moved
    function are rooted in awkward implementation of PyFunctionImpl#delete()
    that delegated to plain AST manipulation and thus ignored
    usual additional reformatting step. I removed it and it's allowed to
    clean a lot of test files and remove the code that was added previously
    exactly for this purpose in PyMoveSymbolProcessor.