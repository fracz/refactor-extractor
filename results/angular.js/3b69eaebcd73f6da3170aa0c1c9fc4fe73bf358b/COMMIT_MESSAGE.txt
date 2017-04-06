commit 3b69eaebcd73f6da3170aa0c1c9fc4fe73bf358b
Author: Chris M <chris@moultrie.org>
Date:   Sat May 18 12:23:06 2013 -0300

    docs(ngMock::$log): improve the `$log.*.logs` descriptions

    Because ngDoc generation only takes the last segment of a property name,
    each $log.[error|warn|log...].logs property has the same name and is
    confusing in the docs.
    This commit helps this by adding a link to the $log.* method and also an
    appropriate usage example.