commit 0ad587aec3288ff1f9e29f71b7c27dc2f1d2ff8e
Merge: f64cf23 02818e4
Author: Tobias Schultze <webmaster@tubo-world.de>
Date:   Mon Sep 21 00:21:34 2015 +0200

    minor #15847 [DomCrawler] Optimize the regex used to find namespace prefixes (stof)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [DomCrawler] Optimize the regex used to find namespace prefixes

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | n/a
    | License       | MIT
    | Doc PR        | n/a

    I ran Blackfire on the [Mink BrowserKitDriver](https://github.com/minkphp/MinkBrowserKitDriver) testsuite (which is a realistic usage of the Mink API btw, as the driver testsuite is basically a functional test of a fixtures website, where failures are considered to be caused by the driver misbehaving).
    I discovered that 17% of the time was spent in ``preg_match_all`` to look for prefixes in the XPath namespace prefixes (Mink does not use any namespace prefix in its Xpath queries).

    This optimizes the regex to use a possessive quantifier. This avoids useless backtracking when matching (due to the way the regex is written, backtracking cannot allow finding more matches here). See https://blackfire.io/profiles/compare/21aaebf3-e38f-456a-8fc9-cf7d9e2a35eb/graph for the improvement.
    The optimization is applied in the 2.7 branch, because this regex does not exist in 2.3: the registration of namespaces was added in 2.4

    Commits
    -------

    02818e4 [DomCrawler] Optimize the regex used to find namespace prefixes