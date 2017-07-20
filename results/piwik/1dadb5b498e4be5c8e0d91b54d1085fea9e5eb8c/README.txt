commit 1dadb5b498e4be5c8e0d91b54d1085fea9e5eb8c
Author: diosmosis <benaka@piwik.pro>
Date:   Mon Aug 31 17:45:22 2015 -0700

    Refs #8317, fixing regression caused by refactor, post event Tracker.newVisitorInformation using reference to array, not by value.