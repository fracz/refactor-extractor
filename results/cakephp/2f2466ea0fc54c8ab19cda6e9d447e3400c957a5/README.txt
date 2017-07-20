commit 2f2466ea0fc54c8ab19cda6e9d447e3400c957a5
Author: mark_story <mark@mark-story.com>
Date:   Fri Apr 27 23:11:51 2012 -0400

    Start refactoring requestContext.

    * Make requestContext less expensive to generate.
    * Remove garbage methods in Router.
    * Remove tests for garbage methods.
    * Add methods to RouteCollection to facilitate testing.
    * Update tests.