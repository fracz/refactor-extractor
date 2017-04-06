commit d1b775f92018e2c1e5b34e46c8253eb6f5c408d8
Merge: adf07f1 3ad01c0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed May 30 07:06:02 2012 +0200

    merged branch guilhermeblanco/patch-4 (PR #4451)

    Commits
    -------

    3ad01c0 Update src/Symfony/Bundle/FrameworkBundle/EventListener/TestSessionListener.php

    Discussion
    ----------

    Fix broken web test cases with session already started

    This PR fixes the issues highlighted in PR #4445 and #3741 by not overriding the session id if the session is already started.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-29T19:06:33Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1468970) (merged 3ad01c05 into adf07f1e).

    ---------------------------------------------------------------------------

    by stloyd at 2012-05-29T19:07:54Z

    Look at #2040. It's quite old, but there is at least some "reason" =)

    ---------------------------------------------------------------------------

    by guilhermeblanco at 2012-05-29T20:57:27Z

    @stloyd it seems to me that doing a session_id() (and now as $session->setId()) is still wrong.
    The right should be a regeneration of the id then, which would still make my code pass. The problem is that setId method checks for a possible regeneration/double attribution, which prevents an already started session (from a WebTestCase) to live peacefully with the TestSessionListener, because it will always try to set an id.
    So either we allow to regenerate ids or this code is not only useless for 2.1, but actually broken.

    ---------------------------------------------------------------------------

    by drak at 2012-05-30T02:42:47Z

    @guilhermeblanco - When I was doing the session refactoring I wanted to make exactly the patch this PR now but I didnt because I wast sure if there were reasons I was unaware of. @fabpot - I would merge this PR if it fixes the related tickets.