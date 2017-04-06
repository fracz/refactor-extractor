commit 34c2bf75e03ae42967382abd34e2733ae8ed9cb8
Merge: 8754c0c 0d0a968
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jul 17 22:45:12 2012 +0200

    merged branch bschussek/phpengine_cache_escape (PR #4942)

    Commits
    -------

    0d0a968 [Templating] Cached the result of escape() in order to improve performance (+470ms)

    Discussion
    ----------

    [Templating] Cached the result of escape() in order to improve performance

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -

    This improvement gains **400ms** of rendering speed on [this particular example page](http://advancedform.gpserver.dk/app_dev.php/taxclasses/1).

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-07-16T17:36:50Z

    i guess we don't have to be concerned with increased memory usage here .. if at all we could offer a clear cache method in case someone is f.e. using this to generate tons of messages in a cron job.

    ---------------------------------------------------------------------------

    by henrikbjorn at 2012-07-17T06:39:52Z

    The example form is broken.

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-17T07:21:26Z

    The source code for the form can be found [here](https://github.com/stof/symfony-standard/blob/twig_forms/src/AdvancedForm/CoreBundle/Form/TaxClassType.php).

    ---------------------------------------------------------------------------

    by henrikbjorn at 2012-07-17T07:28:11Z

    But i am guessing this is only for php not twig :P

    ---------------------------------------------------------------------------

    by bschussek at 2012-07-17T07:41:07Z

    Obviously..