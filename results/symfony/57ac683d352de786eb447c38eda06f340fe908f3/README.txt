commit 57ac683d352de786eb447c38eda06f340fe908f3
Merge: 16702fc 0a42501
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Dec 28 23:54:30 2012 +0100

    merged branch fabpot/exception-logging (PR #6503)

    This PR was merged into the master branch.

    Commits
    -------

    0a42501 [HttpKernel] tweaked logging in the exception listener
    1a6c9b3 [HttpKernel] refactored logging in the exception listener

    Discussion
    ----------

    [HttpKernel] refactored logging in the exception listener

    * avoid code duplication
    * allow easier overloading of the default behavior