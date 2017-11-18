commit ff48a88b9110f1322d274e0e7f0a36086d44707c
Author: Stephane Nicoll <snicoll@pivotal.io>
Date:   Wed Aug 31 12:10:35 2016 +0200

    Enable fork more when devtools is present

    This commit improves the run goal to automatically fork the process when
    devtools is present and log a warning when fork has been disabled via
    configuration since devtools will not work on a non-forked process.

    We don't want devtools to kick in for integration tests so the logic has
    been placed in `RunMojo` requiring a couple of protected methods to
    override.

    Closes gh-5137