commit ed6373300028deda9a0878b3975699d183c1f75c
Author: Igor Minar <igor@angularjs.org>
Date:   Tue Sep 9 02:52:36 2014 +0200

    fix(ngRepeat): preserve original position of elements that are being animated away

    During the recent refactoring a typo was made that broke code that detects if we are
    already removed from the DOM (animation has completed).

    Closes #8918
    Closes #8994