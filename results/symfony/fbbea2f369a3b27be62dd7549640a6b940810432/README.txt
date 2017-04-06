commit fbbea2f369a3b27be62dd7549640a6b940810432
Merge: c819d84 e37783f
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jan 23 13:45:25 2012 +0100

    merged branch stof/doctrine_collector (PR #3173)

    Commits
    -------

    e37783f [DoctrineBridge] Refactored the query sanitization in the collector
    3b260d2 Refactored the collector to separate the loggers per connection

    Discussion
    ----------

    Doctrine collector

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: yes (for the end user, it will require deleting old profiler data)
    Symfony2 tests pass: yes ![Build Status](https://secure.travis-ci.org/stof/symfony.png?branch=doctrine_collector)

    This refactors the Doctrine collector to allow implementing doctrine/DoctrineBundle#7
    The first commit splits the logging of queries per connection to be able to know which connection was used instead of using a shared stack.

    The second commit refactors the sanitation of the parameters to apply the DBAL conversion and then keep the param whenever possible (i.e. when we are sure it is serializable). Such queries will then be explainable in the profiler as we will be able to use the parameters again. Due to the way PDO works, the only cases where we would get an unexplainable queries due to the parameters are queries using a LOB parameter (as it is a resource) or broken queries (passing an object to PDO for instance). And this second case does not make sense to explain the query of course.

    ---------------------------------------------------------------------------

    by stof at 2012-01-23T12:32:16Z

    Merging this PR should be synchronized with the DoctrineBundle PR due to the BC break in the collector