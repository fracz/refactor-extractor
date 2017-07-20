commit 561018677fb04ef8c34d15003a82ae1b85812278
Author: Dominik Schilling <dominikschilling+git@gmail.com>
Date:   Fri Jul 8 11:19:29 2016 +0000

    Constants: Move constants for data sizes before constants for memory limits.

    This allows us to improve core's memory limit handling.

    See #32075.
    Built from https://develop.svn.wordpress.org/trunk@38011


    git-svn-id: http://core.svn.wordpress.org/trunk@37952 1a063a9b-81f0-0310-95a4-ce76da25c4cd