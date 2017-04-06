commit 8dee4be6b7843ec6cd6b7fcbdea0712be326a23b
Merge: 0e8b2a3 17757d8
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Tue Oct 18 17:34:22 2016 +0200

    bug #20235 [DomCrawler] Allow pipe (|) character in link tags when using Xpath expressions (klausi, nicolas-grekas)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [DomCrawler] Allow pipe (|) character in link tags when using Xpath expressions

    | Q             | A
    | ------------- | ---
    | Branch?       | master
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #20229
    | License       | MIT
    | Doc PR        | -

    @klausi could you please validate this patch? Is it an improvement over yours? (sorry I don't have the proper use case to test.)

    Commits
    -------

    17757d8 [DomCrawler] Optimize DomCrawler::relativize()
    5b26e33 [DomCrawler] Allow pipe (|) character in link tags when using Xpath expressions