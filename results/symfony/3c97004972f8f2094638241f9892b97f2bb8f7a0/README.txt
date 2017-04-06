commit 3c97004972f8f2094638241f9892b97f2bb8f7a0
Author: Andrew Hilobok <hilobok@gmail.com>
Date:   Tue Apr 23 19:01:41 2013 +0300

    Reset all catalogues when adding resource to fallback locale (#7715, #7819)

    Slightly refactored testAddResourceAfterTrans()
    and replaced costly part with reset all catalogues

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #7715, #7819
    | License       | MIT
    | Doc PR        |