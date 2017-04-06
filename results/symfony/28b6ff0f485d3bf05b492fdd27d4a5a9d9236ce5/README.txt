commit 28b6ff0f485d3bf05b492fdd27d4a5a9d9236ce5
Merge: 168b895 b804b94
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jul 2 10:42:21 2012 +0200

    merged branch nomack84/wdt_documentation_link_color_fix (PR #4260)

    Commits
    -------

    b804b94 Fixed style for the abbr tag
    147cab7 [WDT] Fix the color of Documentation link to keep concistence.

    Discussion
    ----------

    [WDT] Fix the color of Documentation link to keep concistence.

    This pull request is to make the Documentation link black as the other links of the WDT

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-11T13:33:24Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1304777) (merged 5a87a098 into 554e0738).

    ---------------------------------------------------------------------------

    by Tobion at 2012-05-11T16:36:39Z

    should be done via selector in the css file that is used for the WDT (also refactor the profiler token link like this)

    ---------------------------------------------------------------------------

    by nomack84 at 2012-05-11T17:46:15Z

    Done.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-11T17:48:24Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1307502) (merged eee437c9 into 554e0738).

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-11T18:27:55Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1307838) (merged 3604f131 into dd0da03c).

    ---------------------------------------------------------------------------

    by mvrhov at 2012-05-11T18:40:05Z

    While you are at it, the controller text color is also wrong.

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-11T18:43:00Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1308018) (merged 147cab74 into dd0da03c).

    ---------------------------------------------------------------------------

    by nomack84 at 2012-05-11T18:49:47Z

    @mvrhov I don't see the difference.

    ---------------------------------------------------------------------------

    by mvrhov at 2012-05-11T19:45:45Z

    Set the color for abbr tag on your website to red or something like that. By default abbr color is set to black. My website has is set to #55555 so the controller name its barely visible.

    ---------------------------------------------------------------------------

    by nomack84 at 2012-05-14T12:42:30Z

    @mvrhov Done!

    ---------------------------------------------------------------------------

    by travisbot at 2012-05-14T12:43:48Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1326494) (merged b804b942 into dd0da03c).

    ---------------------------------------------------------------------------

    by nomack84 at 2012-05-15T13:09:59Z

    Hi @fabpot,
    Can you merge this? The only thing it does is add a missed style to the Documentation link and also to the abbr tag, as suggested by @mvrhov.
    Greetings!