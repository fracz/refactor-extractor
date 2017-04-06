commit 20cacdd878990d31420302521be7d336a417accf
Merge: 2901aeb a395873
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Feb 14 23:39:18 2012 +0100

    merged branch yethee/session_auto_start (PR #3345)

    Commits
    -------

    a395873 [FrameworkBundle][Session] Add auto_start pass to the storage options

    Discussion
    ----------

    [FrameworkBundle][Session] Add auto_start pass to the storage options

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    I think that is bugfix.
    In currently value of auto_start in config has no effect, at least when using the session in WebTestCase context.

    ---------------------------------------------------------------------------

    by stof at 2012-02-13T14:59:17Z

    The ``auto_start`` setting is not an option passed to the session storage. It is about configuring the SessionListener. So this seems wrong

    ---------------------------------------------------------------------------

    by drak at 2012-02-13T15:02:26Z

    That said, the storage does need to know if it should respect autostart - that might be quite independent of anything else.  The moment something is output a session will start if `ini_set('auto_start', 1)`.

    ---------------------------------------------------------------------------

    by drak at 2012-02-13T15:05:52Z

    I guess in the context of FrameworkBundle you probably want the storage driver auto_start off (php's autostart that is) so that sessions are only explicitly started by the session listener.

    ---------------------------------------------------------------------------

    by dr-fozzy at 2012-02-13T15:22:02Z

    Just tested out master branch. With session.auto_start = 0 in php.ini and auto_start: false at framework -> session section of config.yml
    Session is <b>started</b>(cookie's are set) anyway...
    (PHP 5.3.9, simple blank page)
    This bug indirectly affect Varnish caching-proxy, as it's default behaviour to not cache anything if "Cookie" or "Set-Cookie" header is set.

    ---------------------------------------------------------------------------

    by yethee at 2012-02-13T17:55:14Z

    @drak, `ini_set('session.auto_start', 1)` will not work because it will be overriden [here](https://github.com/symfony/symfony/blob/137b0026b76b302409dddd46d70dfeb05b11ae1d/src/Symfony/Component/HttpFoundation/Session/Storage/AbstractSessionStorage.php#L222), if the `auto_start` option is not passed in the `$options`. Or have I missed something?

    I have trouble with session in functional tests (based on WebTestCase). I put some data, authentication token, into session before send request but lost them when session is [starting](https://github.com/symfony/symfony/blob/7e4f4dcdf9eb6706e55f5f6728fa5a7b6c5dfe8b/src/Symfony/Bundle/FrameworkBundle/EventListener/SessionListener.php#L58)

    ---------------------------------------------------------------------------

    by stof at 2012-02-13T18:04:19Z

    @drak seems like your refactored storage now need to be aware of the auto_start setting :)

    ---------------------------------------------------------------------------

    by drak at 2012-02-14T06:40:26Z

    > @drak, ini_set('session.auto_start', 1) will not work because it will be overriden here, if the auto_start option is not passed in the $options. Or have I missed something?

    This code simply sets a default value of off if there was no explicit setting.  I believe this is correct: if not set, then set to off, otherwise, leave as defined.  The issue in question is if FrameworkBundle passes the cofiguration on - it should and should have been since 2.0.

    @stof The storage drivers do indeed need to be aware of the autostart settings and afaik they are already - whether FrameworkBundle passes this on to the storage driver is a different matter though.

    @yethee - are you sure you are using the latest master from symfony/symfony (and not the split trees)?  I ask because your second link points to something that's either in the 2.0 branch or well before the new code was merged.

    ---------------------------------------------------------------------------

    by yethee at 2012-02-14T06:56:40Z

    Yep, I use latest version of master branch. [Here](https://github.com/symfony/symfony/blob/master/src/Symfony/Bundle/FrameworkBundle/EventListener/SessionListener.php) current version of SessionListener, there is no difference of code to the previous link, now. I specifically has specified the link to the commit, and not a branch.

    ---------------------------------------------------------------------------

    by drak at 2012-02-14T06:58:48Z

    Does your PR solve the problem for you?  I'm going to write some tests for this also.

    ---------------------------------------------------------------------------

    by yethee at 2012-02-14T07:09:49Z

    > This code simply sets a default value of off if there was no explicit setting. I believe this is correct: if not set, then set to off, otherwise, leave as defined. The issue in question is if FrameworkBundle passes the cofiguration on - it should and should have been since 2.0.

    How can I pass `auto_start` option in the `setOptions` method? Now this option is not pass, and is always set the default value.
    Difference between current implementation and 2.0 that in the previous version of sessions is automatically started when put any data into session. https://github.com/symfony/symfony/blob/2.0/src/Symfony/Component/HttpFoundation/Session.php#L120

    ---------------------------------------------------------------------------

    by yethee at 2012-02-14T07:17:18Z

    @drak, yes, it makes the behavior of the session as in 2.0 branch

    https://github.com/symfony/symfony/blob/master/src/Symfony/Component/HttpFoundation/Session/Storage/AbstractSessionStorage.php#L186

    ---------------------------------------------------------------------------

    by drak at 2012-02-14T14:41:29Z

    That means it was as I suspected, that the auto_start value in the config was not communicated to the session storage driver in `FrameworkBundle`, which your patch now fixes.  @fabpot I guess this is ok for merge now.