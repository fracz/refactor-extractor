commit 2c22c57e582a0263586f43275b1f0cff1c6d9ee2
Author: Igor Minar <igor@angularjs.org>
Date:   Mon Sep 14 09:10:49 2015 -0700

    refactor($sanitize): remove <script> from valid block elements

    the script and style tag are explicitly blacklisted, so this doesn't change any functionality.
    the change is done to improve code clarity.

    Closes #12524