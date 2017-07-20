commit fbc00b31ef77dd8dac0a9571e6d35148f11eec1e
Author: Ryan McCue <me@ryanmccue.info>
Date:   Tue Dec 13 01:49:39 2016 +0000

    General: Remove most uses of create_function()

    create_function() is equivalent to eval(), and most of our uses can be refactored. This is simpler, more secure, and slightly more performant.

    Props sgolemon.
    Fixes #37082.

    Built from https://develop.svn.wordpress.org/trunk@39591


    git-svn-id: http://core.svn.wordpress.org/trunk@39531 1a063a9b-81f0-0310-95a4-ce76da25c4cd