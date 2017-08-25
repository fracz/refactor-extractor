commit 6efe086b0fde453ca4a4b75acee369f2e78de703
Author: Mike van Riel <me@mikevanriel.com>
Date:   Sun Jun 1 09:48:23 2014 +0200

    Assembling a method crashes

    The ArgumentAssembler that is injected sometimes has no builder set.
    When this is the case the processing of a method will crash. This
    behaviour change was introduced during a recent refactoring where the
    construction of said assembler was moved to the container.

    To solve this we verify that a builder is set on the argument assembler
    and should it be missing we just set the current builder of the method.
    These should always be the same so we can do this without a problem.