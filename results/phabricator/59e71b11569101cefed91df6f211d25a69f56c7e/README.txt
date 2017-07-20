commit 59e71b11569101cefed91df6f211d25a69f56c7e
Author: jungejason <jungejason@fb.com>
Date:   Mon Mar 7 11:05:05 2011 -0800

    Improve error message about missing certificate.

    Summary: improve the error message by adding reference to documentation.

    Test Plan: remove the certificate in .arcrc and run arc diff to see if
    the improved error message shows up.

    Reviewers: epriestley

    CC:

    Differential Revision: 53