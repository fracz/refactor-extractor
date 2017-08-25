commit 047f2617f164f8f95d1c576994008719af68fecb
Author: Joey3000 <Joey3000@users.noreply.github.com>
Date:   Sun Feb 14 13:40:58 2016 +0100

    Circular reference handling improvement

    Refs https://github.com/phpseclib/phpseclib/pull/934/files#r52838650

    This does the following:

    * Addresses the comments to https://github.com/terrafrost/phpseclib/commit/102d53bd275f5c08e7042eaac5fb74f73eded62a

    * Fixes an "Allowed memory size of ... bytes exhausted" issue and simplifies the implementation, bringing it closer to the example in https://stackoverflow.com/questions/9042142/detecting-infinite-array-recursion-in-php/9293146#9293146