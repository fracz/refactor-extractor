commit 53f0e6dcd2d19940c3ff414eb7aaf3dd4ecf36d3
Author: Michael Nedzelsky <Michael.Nedzelsky@jetbrains.com>
Date:   Tue Aug 12 22:52:41 2014 +0400

    JS backend: remove dangerous package, correct translation for call, assignments, binary operations, support try...catch as expression

     #KT-5576 Fixed
     #KT-5594 Fixed
     #KT-3166 Fixed
     #KT-5545 Fixed
     #KT-5594 Fixed
     #KT-5258 Fixed

    JS backend: fix KT-4879: extra side effect when use when in default arguments

     #KT-4879 Fixed

    JS backend: improve and fix WhenTranslator, fix order of evaluation for condtitions, fix KT-5263 (JS: extra tmp when initialize val in when by expression with if)

     #KT-5263 Fixed