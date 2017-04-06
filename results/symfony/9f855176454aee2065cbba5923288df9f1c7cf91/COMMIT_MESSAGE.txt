commit 9f855176454aee2065cbba5923288df9f1c7cf91
Merge: bd01a29 9af0ff2
Author: Tobias Schultze <webmaster@tubo-world.de>
Date:   Tue Jan 13 13:45:06 2015 +0100

    feature #13361 [Routing] apply deprecation triggers and fix tests (Tobion)

    This PR was merged into the 2.7 branch.

    Discussion
    ----------

    [Routing] apply deprecation triggers and fix tests

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | [#12641, #12640, #12719]
    | License       | MIT
    | Doc PR        |

    Correctly add deprecation for _scheme and _method requirements and fix code to not trigger deprecations itself. And fix test so that they either explicitly test deprecated functionality or alternatively refactor tests to use new style.

    It also fixes the deprecation triggers for "pattern" which was not correctly done for YAML/XML loading.

    Commits
    -------

    9af0ff2 [FrameworkBundle] fix routing container param resolving to not access deprecated requirements while maintaining BC
    bd91867 [FrameworkBundle] remove superfluous test that is already covered in routing
    bc1c5c8 [Routing] apply deprecation triggers and fix tests