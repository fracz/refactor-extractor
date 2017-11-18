commit d3015b169b1884c03346d97c06ca6efb06e90b04
Author: Adam Murdoch <adam@gradle.com>
Date:   Mon Nov 14 12:08:54 2016 +1100

    Added more coverage for the various methods of `ResolvedConfiguration` and `LenientConfiguration`. Fixed a regression in `LenientConfiguration.files` introduced in some earlier refactoring.

    Also added some convenience overloads for the various filtered methods on these types.