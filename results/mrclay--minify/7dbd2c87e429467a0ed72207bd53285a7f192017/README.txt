commit 7dbd2c87e429467a0ed72207bd53285a7f192017
Author: Steve Clay <steve@mrclay.org>
Date:   Fri Feb 26 11:16:16 2016 -0500

    Minify no longer tries to minify -min.js/.min.js files

    2.2 used empty string as a magical value meaning do not minify, but
    in the refactoring `Minify::combineMinify` forgot to interpret this
    value that way and instead inherited the default compressor for the type.

    This eliminates `""` as a magical value, but for BC rewrites it to
    `Minify::nullMinifier` in the sources.

    Fixes #499