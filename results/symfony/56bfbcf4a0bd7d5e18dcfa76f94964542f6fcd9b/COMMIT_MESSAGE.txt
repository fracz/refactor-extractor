commit 56bfbcf4a0bd7d5e18dcfa76f94964542f6fcd9b
Merge: ba21854 5950571
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Sep 15 20:07:32 2014 +0200

    minor #11910 [HttpFoundation] Request - URI - comment improvements (1emming)

    This PR was submitted for the master branch but it was merged into the 2.3 branch instead (closes #11910).

    Discussion
    ----------

    [HttpFoundation] Request - URI - comment improvements

    Hi all,

    I was wondering why there is a difference between the URI's given by the `Request` object.

    The method `getUri` will give a URL so  including scheme,http host and base URL, for example:
    `http://dev.decorrespondent.nl/verleng?a=1`

    While the method `getRequestUri` will give:
    `/verleng?a=1`

    While both correct it can get confusing, that is why I propose these copy changes in the comments.

    Commits
    -------

    5950571 [HttpFoundation] Request - URI - comment improvements