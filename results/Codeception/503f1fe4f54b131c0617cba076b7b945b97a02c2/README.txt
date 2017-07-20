commit 503f1fe4f54b131c0617cba076b7b945b97a02c2
Author: Luis Montealegre <montealegreluis@gmail.com>
Date:   Tue Apr 25 11:51:12 2017 -0500

    Support for PHPUnit 6

    * Modify version constraints to allow the installation of PHPUnit 6.

    * Add aliases for PHPUnit 6.

    * Use an updated implementation of TestListener for PHPUnit 6

    * Add an empty implementation for the
      method addWarning, added to the
      TestListener interface in PHPUnit6
    * Load this implementation only if
      PHPUnit 6 is being used, otherwise use
      the current implementation

    * Add shim for method setExpectedException

    * For PHPUnit 5 forward the call to the parent class.
    * For PHPUnit 6 use expectException, expectExceptionMessage, expectExceptionCode.

    * Add shim file for loggers removed in PHPUnit 6

    * Both the JSON and TAP loggers were removed in PHPUnit 6.

    * Class name for exception is different for PHPUnit 6.

    * Add missing setExpectedException method.

    Added method to the following classes

    * MysqlTest, PostgresTest, MongoDbTest and RedisTest

    * Fix version constraint for the mock-objects package when using PHPUnit 4.8

    * Check PHP version in shim.php.

    When using PHP 5.6 class_exists returns true for 'PHPUnit\Framework\TestCase'.
    PHUnit 6 can only be installed on PHP 7, now it's part of the main condition.

    * Add phpunit5-loggers.php and the PHPUnit listener to Nitpick's ignore list.

    * Fix class names in WebDriverTest use underscores instead of namespaces for PHPUnit classes.

    * Replace namespaces with underscores.

    * Added missing replacements.

    * Use method setExpectedException instead of the annotations.

    * testFailedSeeInPopup was using annotations that were conflicting with the exception thrown when a test is skipped.

    * Add method addWarning to Listener instead of providing 2 different implementations

    * Remove the path to this file from nitpick.json

    * Change tests using setExpectedException  to inherit Codeception/Test/Unit.

    * Remove duplicated method from all tests

    * Use method_exists instead of class_exists

    * Check if the method expectException is present.
    * Use if statement instead of the && operator to call the other 2 methods.

    * Fix versions range for PHPUnit related packages.

    * Improve the readability of the condition to add the classes alias

    * Remove extra space in version constraint.

    * Add deprecation warning when using PHPUnit 6.

    * Invert if statement to better match the comment description.