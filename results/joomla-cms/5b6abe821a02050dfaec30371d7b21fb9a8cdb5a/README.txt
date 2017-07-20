commit 5b6abe821a02050dfaec30371d7b21fb9a8cdb5a
Author: Yves Hoppe <yves@compojoom.com>
Date:   Sun Jun 18 13:43:38 2017 +0200

    [Testing] Add basic setup and sample tests for Codeception System Tests (#16743)

    * System tests with codeception 2.2

    * System tests with codeception 2.2

    * System tests with codeception 2.2

    * System tests with codeception 2.2

    * Instructions for testing

    * Instructions for testing

    * Remove useless header

    * [tests] new convention for locating elements

    Adds Gherkin Pages and Step Objects as sample of useage

    * Update Codeception to 2.2.1

    * Writing new composer.lock file for update

    * Adding users.feature scenarios using gherkin

    * Adding content.feature scenarios using gherkin

    * Adding users.feature file

    * Adding scenarios for access level and user group

    * Adding Assert module for assertion

    * Implementing Pageobject

    * Passing $title and $content as a argument

    * Implement step and page object in users.feature file

    * Add missing pages

    * New approach to manage suite configuration

    * Fix the issue_#29 and issue_#31

    * Fix the issue_#30

    * Fix the issue #37

    * Remove the commented code

    * Every page object have the $url property

    * Update Codeception to version 2.2.2

    * Create a constant for waiting times

    * Adding project information in README.md

    * update readme

    * Update README.md for TIMEOUT

    * Checking if TIMEOUT is not already set

    Pull Request for Issue #45 .

    Update a constant for waiting times

    It contains fix related to #47

    * Using contants for timeouts

    * Improvement in frontend feature

    * improve the scenario

    * correct typo and improvement

    * Implement user frontend scenarios

    * Updated travis to only run system tests and no unit tests

    * add how to create snippets

    * category Scenario for issue #35

    * Fix missing call to Step object

    * Move administrator login to the current page object pattern

    * Move administrator login to the current page object pattern
    * Remove Given Joomla CMS is installed
    * Update composer
    * Fix LoginPage
    * Fix missing call to step object
    * Use genering url name for page variable

    * Simplify users manager table xpath

    * Fix typo error

    * Allow robo to run individual .feature tests (#58)

    * Allow robo tu run individual .feature tests

    * Improve running individuals tests documentation

    * Implement the categories scenario

    * slove xpath issue and implement frontend scenario

    * Handle waiting for iframe and menu edit view to save menu item

    * Updating selenium to 2.53.1

    * Updated travis integration for running tests (#60)

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Updated travis integration for running tests

    * Working on Travis

    * Working on Travis

    * Working on Travis

    * Working on Travis

    * Adding docblocks and code style improvements

    * Adding phpdoc header and code style improvement in acceptance tester
    * [dockblock] Adding in Admin page object

    * [dockblock] Adding dockblock in content manager page object

    * [code style] Improve name of variable

    * [dockblock] Adding dockblock in control panel page object
    * [dockblock] Using AcceptanceTester.Page as a subpackage
    * [dockblock] Adding dockblock in login page object
    * [dockblock] Adding docblock in user acl page object
    * [dockblock] Adding dockblock in user group page object
    * [dockblock] Adding dockblock in user manager page object
    * [code style] Adding code style improvement in acceptance tester class
    * [docblock] Adding docblock in acceptance helper class
    * [docblock] Adding docblock in functional and unit testing class
    * [docblock] Adding docblocks and code style improvements in site page objects
    * [code style] Adding docblock and code style improvements
    * [code style] Adding code style improvement in acceptance tester
    * [docblock] Adding docblock in content step object
    * [docblock] Adding docblock and code style improvement in category step object
    * Adding category feature execution using Robo
    * [docblock] Adding docblock and code style improvement for category and menu page objects
    * Rearrange tab based on latest joomla
    * [docblock] Adding docblock and code style improvement in login and user step objects
    * [docblock] Adding docblock and code style improvement in login and user step objects
    * [docblock] Adding docblock and code style improvement in user frontend step objects
    * Remove extra blank lines
    * Remove comments from robo

    * Using __DEPLOY_VERSION__ instead of static 3.7

    * Forcing 127.0.0.1 for /etc/hosts in travis

    * [code optimize] code optimization and remove duplicate codes (#65)

    * [code optimize] Combine save method for category and content

    * Using page object dynamically

    * Revert "[code optimize] Combine save method for category and content"

    This reverts commit 82682c5d57a14707c191736a57f22011c2fd54be.

    * Usign dynamic wait for test string

    * [duplicate] Handle duplicate code for user form

    * Using common annotation for create new user

    * [duplicate] Using common annotation for login in frontend scenarios

    * [duplicate] Using page object method to have user with username

    * [duplicate] Using page object method to have category with given title

    * [duplicate] Using page object method to have article with given title

    * [optimize] Reusing code by using oop concept

    * Resolve error in creating instance

    * Reuse fill article form code

    * Create Admin step

    * Using common method to search and select

    * Using common method for see system message

    * Using common method for search

    * Remove duplicate code

    * Remove duplicate code for delete and empty

    * Using property from parent class

    * Using Admin step as a parent class

    * Cleanup acceptance tester

    * Using generic page object properties

    * Updating travis and Robo to the final structure

    * Updating and moving codeception-build script

    * Updating and moving codeception-build script

    * Updating and moving codeception-build script

    * Updating and moving codeception-build script

    * Updating and moving codeception-build script

    * Updating and moving codeception-build script

    * Updating and moving codeception-build script

    * change the tests with generic name (#68)

    * change the tests with generic name

    * Rename test user1 to User One

    * Rename the email and login name

    * Remove acceptance.suite.yml file from git list (#70)

    * Remove acceptance.suite.yml file from git list

    * Remove unnecessory _generated folder

    * Fix issue #57 Update frontend_user tests  (#61)

    * Fix issue_#57 Update frontend_user tests as per commented in PR #51

    * [codestyle] Adding docblock and code style improvement in Robo and user frontend

    * Verify in backend that user is created

    * Fix scenario in user_frontend.feature to proper English

    * Using alternative of system message container for assurance (#72)

    * Using alternative of system message container for assurance in article feature

    * Improve code and convert into page object

    * Convert system message checks in category feature

    * Convert system message container in user feature

    * Repair user scenarios

    * CodeStyle for the Tests (#74)

    * codestyle

    * Small tweeks to the RoboFile (#75)

    * robo

    * robo

    * Changed all references to /tests as the root directory to /tests/codeception. (#78)

    * Update README.md

    Changed all references to /tests as the root directory to /tests/codeception.

    * Update README.md

    Corrected errors in the first PR

    * Changed a AND into a WHEN in the file tests/codeception/acceptance/users_frontend.feature

    * Update README.md (#84)

    * Update README.md

    Corrected two typos and inserted a warning for Firefox version above 47

    * Update README.md

    Corrected version numbers.
    In composer.json i found Selenium WebDriver 2.53.1
    Before the current firefox version 49 there was version 45.
    With version 45.0.2 I have no problems. With version 49.0.1 I had problems.

    * Update README.md

    Corrections

    * correction language category feature - UK to en-GB (#85)

    * Update to latest selenium server standalone version (#87)

    * Add a scroll to a element out of fold (#88)

    * Updating the Login Step File removing hardcoded username

    * Updating the feature files background step

    * Updating the background step

    * This should take care of the failure

    * Updating the Login Step

    * Trying to add a wait

    * Opening the Account Details Tab

    * Updating Manager Page

    * Adding an Extension Manager Class

    * Adding reference

    * Adding Extension Step File

    * Adding Extension Feature file

    * Fixing another failure

    * Work out of the box on Windows... (#90)

    * Adding execution in Travis

    * This update was a must

    * Updating Robo File

    * Updating Robo

    * Adidng Banner Step

    * Adding Feature

    * Adding both the Tests

    * Removing Accept Popup, not sure why this stuff is needed

    * back to pavilion

    * Codestyle (#95)

    * Updating branch

    * I have to remove it now

    * Removing uninstall Extension

    * Tests now run in chrome, firefox, edge and IE (#96)

    * Now works on chrome, firefox, edge and IE

    * update branch

    * update branch

    * fix mac detection and codestyle

    * updating dependencies

    * Update composer lock (#100)

    * Remove curl check (#101)

    We don't need it.

    * Better wrapper method to display search tools (#102)

    * Feature/add database module (#99)

    * Add Database module

    # Conflicts:
    #       tests/codeception/acceptance.suite.dist.yml

    * Add documentation

    * Remove dump configuration

    * Update db configuration

    * Create the test database before the tests

    * Revert old dependencies composer.lock (#104)

    * Fix typo (#109)

    * Fix typo

    * Code style

    * Add new line before try catch

    * Add a Joomla Database Helper (#108)

    * Add JoomlaDb Helper

    * Fix configuration

    * Update to latest joomlabrowser

    * Move AcceptanceHelper to the  codeception default location for helpers (#111)

    * Use default path for the AcceptanceHelper

    * Use codeception defaults for helper files

    * Order enabled modules alphabetical

    * Remove old tests (#115)

    * Remove the UserLoginCest

    The UserLoginCest does not define any scenarios and is not used anywhere.

    * Remove UserCest

    It is not used anymore

    * Composer update (#118)

    Update selenium-server-standalone to version 3.0.1.2 for working on linux with chrome

    * update version number in composer.json for JoomlaBrowser (#121)

    * update version number in composer.json for JoomlaBrowser

    * composer.lock

    * Fix in the RoboFile in the env parameter

    * Update composer

    * Update to latest selenium server version in composer.json

    Updating dependencies in composer file

    * Working on clean test branch

    * Working on sample tests

    * bit cs

    * bit cs

    * Removed old steps

    * revert readme (#132)

    * revert readme

    * readme

    * Further cleanup

    * Cleanup

    * Cleanup

    * Thank you @brianteeman

    * Thank you @brianteeman