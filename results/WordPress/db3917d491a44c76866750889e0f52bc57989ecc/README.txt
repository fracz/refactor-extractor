commit db3917d491a44c76866750889e0f52bc57989ecc
Author: Gary Pendergast <gary@pento.net>
Date:   Mon May 23 06:32:29 2016 +0000

    Database: Use the `utf8mb4_unicode_520_ci` collation, when available.

    The `utf8mb4_unicode_520_ci` (Unicode Collation Algorithm 5.2.0, October 2010) collation is an improvement over `utf8mb4_unicode_ci` (UCA 4.0.0, November 2003).

    There is no word on when MySQL will support later UCAs.

    Fixes #32105.


    Built from https://develop.svn.wordpress.org/trunk@37523


    git-svn-id: http://core.svn.wordpress.org/trunk@37491 1a063a9b-81f0-0310-95a4-ce76da25c4cd