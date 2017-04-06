commit 2a0f6fbea3eb9140d40a2542a9e7178c7080aec2
Merge: 2539af6 af420c1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Oct 6 17:11:44 2015 +0200

    bug #15161 avoid duplicated path with addPrefix (remicollet)

    This PR was submitted for the 2.7 branch but it was merged into the 2.3 branch instead (closes #15161).

    Discussion
    ----------

    avoid duplicated path with addPrefix

     Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | [n/a
    | License       | MIT
    | Doc PR        | n/a

    Having UniversalClassLoader deprecated raise various issues... and ClassLoader doesn't provide the same features... :(

    This one avoid duplication of paths for a given prefix, so can improve perf is such case.

    Notice: I open this PR against 2.7, first version where UniversalClassLoader is deprecated, but feel free to apply only if 2.8 or 3.0, as you prefer.

    Context on how this class is used in various fedora packages, see
    http://blog.remirepo.net/post/2015/06/30/PHP-SIG-autoloader

    Commits
    -------

    af420c1 avoid duplicated path with addPrefix