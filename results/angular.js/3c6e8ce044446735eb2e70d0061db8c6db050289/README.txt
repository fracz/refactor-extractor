commit 3c6e8ce044446735eb2e70d0061db8c6db050289
Author: Raphael Jamet <rjamet@google.com>
Date:   Tue Jun 30 10:36:26 2015 +0200

    refactor($templateRequest): move $sce checks and trust the cache

    Move all the calls to $sce.getTrustedUrl inside $templateRequest, and
    also trust the contents of the cache. This allows prefetching templates
    and to bypass the checks on things where they make no sense, like
    templates specified in script tags.

    Closes #12219
    Closes #12220
    Closes #12240