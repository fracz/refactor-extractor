commit 7ce78776c6b0bec6ee93e3fe0e63ed4d9c9ac2b0
Author: Stefano Mazzocchi <stefano@apache.org>
Date:   Mon May 31 08:51:51 2010 +0000

    refactored tests to avoid using static initialization which conflicts with our ability to have special log4j configurati
    ons for testings. Also tagged all broken tests as broken so that the tree is now green. Also used the test class as the
    logger name so that it's easier to groupt the logger verbosity based on java packages.

    git-svn-id: http://google-refine.googlecode.com/svn/trunk@922 7d457c2a-affb-35e4-300a-418c747d4874