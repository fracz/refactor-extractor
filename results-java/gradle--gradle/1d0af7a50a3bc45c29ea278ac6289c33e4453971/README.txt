commit 1d0af7a50a3bc45c29ea278ac6289c33e4453971
Author: Gary Hale <ghhale@computer.org>
Date:   Tue Jan 13 17:33:29 2015 -0500

    Refactoring/cleanup of Play distribution story

    - Changed distribution to use new "playRun" configuration instead of binary.getClasspath()
    - Removed version from distribution zip and explicitly set archiveName
    - Wired stage and dist tasks more tightly
    - Split up and refactored createDistributionTasks method in plugin rules class

    +review REVIEW-5324