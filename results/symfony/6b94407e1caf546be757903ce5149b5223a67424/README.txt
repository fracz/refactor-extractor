commit 6b94407e1caf546be757903ce5149b5223a67424
Merge: 6f32078 22cb817
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Aug 3 10:43:22 2012 +0200

    merged branch dlsniper/php-engine-escape-cache (PR #5117)

    Commits
    -------

    22cb817 Caching variables for the PHP templating engine

    Discussion
    ----------

    [Templating] PHP templating engine speed-ups

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: [![Build Status](https://secure.travis-ci.org/dlsniper/symfony.png?branch=php-engine-escape-cache)](http://travis-ci.org/dlsniper/symfony)
    Fixes the following tickets: ~
    Todo: ~
    License of the code: MIT
    Documentation PR: ~

    This PR should improve the speed for rendering the form present here: https://github.com/dlsniper/symfony-standard . On my computer, Ubuntu 12.04 Apache 2.2.22 + mod_php 5.3.10 default packages from Ubuntu on a core i7 I get about 30-40ms improvement / request with the first commit and with the second one I get a further smaller boost and also a small memory usage decrease.

    ---------------------------------------------------------------------------

    by dlsniper at 2012-07-31T06:18:54Z

    ping @bschussek This should help a bit more on the effort for optimizing the example provided for the Forms component.

    If there's another example of complex form(s) let me know so that I can have a look on them as well. Thanks!

    ---------------------------------------------------------------------------

    by travisbot at 2012-07-31T19:55:03Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/2003907) (merged 240152b9 into a172a812).

    ---------------------------------------------------------------------------

    by dlsniper at 2012-08-02T07:41:03Z

    @fabpot what do you think about this? or anyone else for that matter?

    ---------------------------------------------------------------------------

    by travisbot at 2012-08-02T12:55:54Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/2018613) (merged 5e773e79 into a172a812).

    ---------------------------------------------------------------------------

    by fabpot at 2012-08-03T07:42:31Z

    Can you squash your commits?

    ---------------------------------------------------------------------------

    by dlsniper at 2012-08-03T08:32:05Z

    @fabpot Done

    ---------------------------------------------------------------------------

    by travisbot at 2012-08-03T08:40:46Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/2026559) (merged 22cb8173 into 6f32078b).