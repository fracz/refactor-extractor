commit 498f72ce9c18397cddaaa63a37f56bf376abe1b5
Merge: 722ad12 f8b5f35
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Jul 6 17:49:40 2011 +0200

    merged branch stof/monolog (PR #1556)

    Commits
    -------

    f8b5f35 [MonologBundle] Refactored the way to configure the email prototype for swiftmailer
    874fb95 [MonologBundle] Refactored the configuration of processors

    Discussion
    ----------

    Monolog

    This refactors the way processors and email prototype are configured in MonologBundle for consistency with the other bundles. The hack using ``@id`` to use a service in the semantic configuration is not used anywhere else in Symfony2.
    This removes the ability to use a static callback as processor (or a PHP function) but adds the support of adding a processor only for a given logging channel (for processors attached to the logger) which was not possible previously.

    ---------------------------------------------------------------------------

    by stof at 2011/07/06 07:33:52 -0700

    the PR for the doc and the standard edition are coming.