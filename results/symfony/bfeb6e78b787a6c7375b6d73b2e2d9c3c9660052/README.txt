commit bfeb6e78b787a6c7375b6d73b2e2d9c3c9660052
Merge: c34f773 32dc31e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Nov 19 13:43:56 2012 +0100

    merged branch vicb/security-config (PR #6017)

    This PR was merged into the 2.0 branch.

    Commits
    -------

    32dc31e [SecurityBundle] Convert Http method to uppercase in the config

    Discussion
    ----------

    [SecurityBundle] Convert Http method to uppercase in the config

    This is not striclty required as method names would be converted to uppercase by the matcher after #5988.

    However I think it is better to always use uppercase for http method names.

    The config UT has also been improved as part of this PR.

    This is good to propagate to 2.1 & 2.2 also.