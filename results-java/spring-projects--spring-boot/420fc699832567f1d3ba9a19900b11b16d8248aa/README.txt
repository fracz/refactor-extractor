commit 420fc699832567f1d3ba9a19900b11b16d8248aa
Author: Dave Syer <dsyer@gopivotal.com>
Date:   Sat Jan 11 07:23:39 2014 +0000

    Only run InitCommand if there is code to process

    This appears to be a significant improvement in performance
    (checking for the existence of init.grooy is cheap, but compiling
    it is expensive).

    I'm going to say this fixes gh-212.