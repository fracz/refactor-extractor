commit c767b53906990c426859ca4509237de1dc91ea20
Author: Michael Bodnarchuk <DavertMik@users.noreply.github.com>
Date:   Tue May 23 00:27:20 2017 +0300

    db dump populator (#4230)

    * Add ability to load the dump using a tool/command

    The db module now accepts a `dumptool` configuration item
    where the user can enter a command to execute his/her prefered
    tool or wrapper script.
    The dsn is parsed into variables and are made available, with
    the variables from the module configuration, to the command
    for interpolation, allowing the executing of commands like:

    `dumptool: 'mysql -u $user -p $password -h $host -D $dbname < $dump'`

    The sqlite module has been cleaned up to respect the reconnect
    settings and to aviod dependency on the order of the execution
    of its parent method. Mainly because there is no need to "reload"
    the connection when touching the sqlite db file...

    * Add ability to load the dump using a populator command

    The db module now accepts a populator configuration item
    where the user can enter a command to execute his/her prefered
    tool or wrapper script to load the dump.

    The dsn is parsed into variables and are made available, with
    the variables from the module configuration, to the command
    for interpolation, allowing the executing of commands like:

    dumptool: 'mysql -u $user -p $password -h $host -D $dbname < $dump'

    The Db module test has been structured in order to test individual
    drivers features.

    * Remove useless dbTest

    * Fix sqlite dump

    * Add cleaning and loading tests

    * small refactoring, disabled Sqlite populator test on wercker ci, try to fix sqlite on travis

    * chaned perissions for file

    * fixed tests, refactored populator, updated docs

    * made populated protected property back again

    * enabled db tests on Windows (Appveyor CI)

    * fixed test for PHP 5.4

    * * restored populate: true for Db tests
    * implemented code review suggestions
    * updated documenation

    * cleanup between db tests

    * fixed tests

    * fixed php 5.4 tests

    * minor updates