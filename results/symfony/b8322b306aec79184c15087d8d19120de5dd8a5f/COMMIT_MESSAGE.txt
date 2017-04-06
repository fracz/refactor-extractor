commit b8322b306aec79184c15087d8d19120de5dd8a5f
Merge: 9f05d4a b3fd2fa
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Feb 14 23:50:27 2012 +0100

    merged branch willdurand/propel-stopwatch (PR #3352)

    Commits
    -------

    b3fd2fa [Propel] Added Propel to Stopwatch

    Discussion
    ----------

    [Propel] Added Propel to Stopwatch

    I've added the Stopwatch feature, everything is ready on the PropelBundle.
    The trick is to log `prepare` queries in Propel, that way we got first the prepared statement, and then the executed query. That's why there is a `$isPrepare` boolean.

    I kept BC if people don't update the PropelBundle too.

    William

    ---------------------------------------------------------------------------

    by stof at 2012-02-14T12:16:51Z

    @willdurand toggling a flag for each call seems a bit hackish to me. Is there no better way to do it ?

    ---------------------------------------------------------------------------

    by willdurand at 2012-02-14T12:21:38Z

    Unfortunately no... But it's quite safe as we cannot change logged methods.
    There is neighter start/stop methods, nor typed messages.

    Le 14 févr. 2012 à 13:16, Christophe Coevoet<reply@reply.github.com> a écrit :

    > @willdurand toggling a flag for each call seems a bit hackish to me. Is there no better way to do it ?
    >
    > ---
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/3352#issuecomment-3959592

    ---------------------------------------------------------------------------

    by stof at 2012-02-14T12:26:04Z

    @willdurand then let's use this for propel 1. But please improve the logging interface for Propel 2 :)

    ---------------------------------------------------------------------------

    by willdurand at 2012-02-14T12:34:28Z

    Sure! I've added that on my todolist…

    2012/2/14 Christophe Coevoet <
    reply@reply.github.com
    >

    > @willdurand then let's use this for propel 1. But please improve the
    > logging interface for Propel 2 :)
    >
    > ---
    > Reply to this email directly or view it on GitHub:
    > https://github.com/symfony/symfony/pull/3352#issuecomment-3959729
    >