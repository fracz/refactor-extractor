commit 2c3786ced9eabf5f50850da4a354a755b9d55957
Author: Serghei Iakovlev <sergeyklay@users.noreply.github.com>
Date:   Sat Feb 25 11:46:11 2017 +0200

    Merge the 3.0.x branch (#12644)

    * Regenerated build (PHP7)

    * Regenerated build (PHP5)

    * Regenerated build (PHP5)

    * Regenerated build (PHP7)

    * Using latest Zephir [ci skip]

    * Bump dev version

    * Added a number (0) to be a label in a form field. Null, empty string or empty array will keep invalidating a label and using the value instead.

    * Update CONTRIBUTING.md [ci skip]

    * Include cipher aliases for \Phalcon\Crypt

    The older version of Phalcon used aliases for cipher and now they are not available. They are easy to use and migration will be easier if they persist.

    * Test Alias cipher for \Phalcon\Crypt

    * Update Changelog

    * Change test in changelog

    * Allow other except fields than unique fields

    * Added backers file

    * Update tests CHANGELOG.md [ci skip]

    Add TEST_MC_WEIGHT to export list

    * Fixed #12567 [ci skip]

    * Cleaned .gitignorei [ci skip]

    * Update Phalcon\Mvc\Model return ResultsetInterface (#12574)

    * Update Phalcon\Mvc\Model return ResultsetInterface

    ResultsetInterface is not imported and causes problems when generating Stubs for IDE. It is generating @return type of Phalcon\Mvc\ResultsetInterface and not Phalcon\Mvc\Model\ResultsetInterface

    I think it should be imported in order to easily find correct class

    * Update CHANGELOG.md

    * Updated backers file

    * Fixes #12573

    * Updated backers file

    * Update BACKERS.md with known links

    * Added mctekk and abits logos

    * Removing the images to ensure that they are refreshed with the next commit

    * Re-added the images for backers

    * Added pdffiller in the backers file

    * Added links to the BACKERS.md

    * Updated backers file

    * Removing the images to ensure that they are refreshed with the next commit

    * Re-added the images for backers

    * Added links to the BACKERS.md

    * Updated backers file

    * Updated backers file

    * Updated backers file

    * Updated backers file

    * Bump version

    * Tune up Travis build

    * Fixed ModelsManager tests

    * Tune up PHP_CodeSniffer

    * Tune up PHP_CodeSniffer

    * Tune up the tests

    * Updated CHANGELOG.md [ci skip]

    * Cleaned Gettext::query

    * Fixed Phalcon\Validation\Message\Group::offsetUnset

    * Allow role and resource object in isAllowed

    * Updated backers file

    * Updated backers file

    * Updated backers file

    * Removing the images to ensure that they are refreshed with the next commit

    * Re-added the images for backers

    * Added links to the BACKERS.md

    * Updated Copyright [ci skip]

    * Updated dependencies

    * Regenerated build (PHP7)

    * Regenerated build (PHP5)

    * Using latest Zephir

    * Updated CHANGELOG.md [ci skip]

    * Restarted Travis build

    * Regenerated build [ci skip]

    * Regenerating build for php5 [ci skip]

    * Cleaned Phalcon\Annotations\AdapterInterface

    * Cleaned the Appveyor Build

    * Added Phalcon\Validation for Phalcon\Mvc\Collection

    * install file refactoring, custom phpize & php-config paths allowed

    * install BINs fix

    * Rosolved conflicts [ci skip]

    * Removed unused variables

    * Amended Uniqueness Validator test

    * Port Uniquenss Validator changes from 3.0.x

    * Regenerated build (PHP7) [ci skip]

    * Regenerated build (PHP5) [ci skip]

    * Using latest Zephir [ci skip]