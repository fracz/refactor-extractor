commit 82d6fcfe512f6096cfae0bc251e43ec9da4fd1d6
Author: Mark Story <mark@mark-story.com>
Date:   Sun Apr 3 20:41:55 2016 -0400

    Add MiddlewareStack.

    This is the first of many features needed to build out the PSR7 support
    in CakePHP. The MiddlewareStack provides an interface for managing
    a stack of middleware objects that will be applied to incoming
    request/response objects.

    I've not included support for conditionally applied or path specific
    middleware. I see this as an improvement to be done once the basics are
    in place.