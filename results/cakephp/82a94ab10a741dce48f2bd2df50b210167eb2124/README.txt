commit 82a94ab10a741dce48f2bd2df50b210167eb2124
Author: mark_story <mark@mark-story.com>
Date:   Mon Aug 25 12:23:44 2014 +0200

    Use the `pass` option when matching routes.

    Routes are much easier to refactor if the `pass` option is honoured
    when matching routes. When the pass option is defined, positional
    arguments will be rekeyed to their matching route element should the
    route element be undefined.

    Fixes #4359