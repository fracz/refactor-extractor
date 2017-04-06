commit 2d65e17d2b509e904190834341a16be4a3eeddbe
Merge: 1a7ba03 99079ba
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Mar 11 09:29:22 2012 +0100

    merged branch johnnypeck/patch-2 (PR #3536)

    Commits
    -------

    99079ba Very small semantic changes improving understanding and readability.

    Discussion
    ----------

    Very small semantic changes improving understanding and readability.

    The "may or may not" change may seem pedantic but it quantifies the use of the field; obviously a boolean is true or not but "may not be empty" made me wonder about it's intent so clarification seemed appropriate.

    Change "return" to "returns" as the rest of the code in the class uses this syntax.

    Change "contains" to "contain" in an exception message.