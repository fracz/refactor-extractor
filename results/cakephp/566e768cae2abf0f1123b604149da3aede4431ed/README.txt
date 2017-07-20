commit 566e768cae2abf0f1123b604149da3aede4431ed
Author: mark_story <mark@mark-story.com>
Date:   Thu Jul 3 11:05:05 2014 -0400

    Remove default routes.

    Remove the default routes file that generates routes based on config
    data. Routing in CakePHP 3.0 should be much more declarative and less
    magic based.  While the scopes result in slightly more routes being
    connected, the performance improvements are shown to offset any slowdown
    even for large route sets.