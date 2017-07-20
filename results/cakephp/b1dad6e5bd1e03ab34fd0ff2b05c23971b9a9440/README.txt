commit b1dad6e5bd1e03ab34fd0ff2b05c23971b9a9440
Author: mark_story <mark@mark-story.com>
Date:   Sat Sep 24 22:35:21 2011 -0400

    Adding session renewal upon login/logout.
    This helps improve session security, as it reduces the opportunity
    of replaying a session id successfully.
    Fixes #836