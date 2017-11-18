commit b887171f073b2eed90b9871d0111b19256d4d7b9
Author: daz <daz@bigdaz.com>
Date:   Wed Feb 6 20:29:04 2013 -0700

    Started rolling improvements to maven publishing over into ivy publishing (REVIEW-1268)
    - Prevent publishing artifact that is directory
    - Better error messages for publication validation failures
    - Many integration test improvements for ivy publication
       - Test for non-ascii characters in artifact names
       - Test for org/module/artifact/version/etc values containing whitespace