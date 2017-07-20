<?php
if(!defined("PIWIK_PATH_TEST_TO_ROOT"))
{
    define('PIWIK_PATH_TEST_TO_ROOT', realpath(dirname(__FILE__) . '/../..'));
}
if(!defined('PIWIK_USER_PATH'))
{
    define('PIWIK_USER_PATH', PIWIK_PATH_TEST_TO_ROOT);
}
if(!defined('PIWIK_INCLUDE_PATH'))
{
    define('PIWIK_INCLUDE_PATH', PIWIK_PATH_TEST_TO_ROOT);
}
if(!defined('PIWIK_INCLUDE_SEARCH_PATH'))
{
    define('PIWIK_INCLUDE_SEARCH_PATH', get_include_path()
        . PATH_SEPARATOR . PIWIK_INCLUDE_PATH . '/core'
        . PATH_SEPARATOR . PIWIK_INCLUDE_PATH . '/libs'
        . PATH_SEPARATOR . PIWIK_INCLUDE_PATH . '/plugins');
}
@ini_set('include_path', PIWIK_INCLUDE_SEARCH_PATH);
@set_include_path(PIWIK_INCLUDE_SEARCH_PATH);
@ini_set('memory_limit', -1);
error_reporting(E_ALL|E_NOTICE);
@date_default_timezone_set('UTC');

require_once PIWIK_INCLUDE_PATH .'/libs/upgradephp/upgrade.php';
require_once PIWIK_INCLUDE_PATH .'/core/testMinimumPhpVersion.php';
require_once PIWIK_INCLUDE_PATH .'/core/Loader.php';
require_once PIWIK_INCLUDE_PATH .'/core/FrontController.php';
require_once PIWIK_INCLUDE_PATH .'/tests/PHPUnit/DatabaseTestCase.php';
require_once PIWIK_INCLUDE_PATH .'/tests/PHPUnit/IntegrationTestCase.php';
require_once PIWIK_INCLUDE_PATH .'/tests/PHPUnit/FakeAccess.php';
require_once PIWIK_INCLUDE_PATH .'/tests/PHPUnit/MockPiwikOption.php';
require_once PIWIK_INCLUDE_PATH .'/tests/PHPUnit/MockEventDispatcher.php';

// required to build code coverage for uncovered files
require_once PIWIK_INCLUDE_PATH .'/plugins/SecurityInfo/PhpSecInfo/PhpSecInfo.php';


// General requirement checks & help: a webserver must be running for tests to work!
checkPiwikSetupForTests();

function checkPiwikSetupForTests()
{
	if(empty($_SERVER['REQUEST_URI'])
		|| $_SERVER['REQUEST_URI'] == '@REQUEST_URI@') {
		echo "WARNING: for tests to pass, you must first:
1) Install webserver on localhost, eg. apache
2) Make these Piwik files available on the webserver, at eg. http://localhost/dev/piwik/ - Piwik does need to be installed to run tests, but this URL must work.
3) Copy phpunit.xml.dist to phpunit.xml
4) Edit in phpunit.xml the @REQUEST_URI@ and replace with the webserver path to Piwik, eg. '/dev/piwik/'

Try again and now the tests should run!";
		exit(1);
	}

	// Now testing if the webserver is running
	$piwikServerUrl = IntegrationTestCase::getRootUrl();
	try {
		$fetched = Piwik_Http::sendHttpRequest($piwikServerUrl, $timeout = 3);
	} catch(Exception $e) {
		$fetched = "ERROR fetching: " . $e->getMessage();
	}
	$expectedString = 'plugins/CoreHome/templates/images/favicon.ico';

	if(strpos($fetched, $expectedString) === false) {
		echo "\nPiwik should be running at: " . $piwikServerUrl
			. "\nbut this URL returned an unexpected response: '"
			. substr($fetched,0,300) . "...'\n\n";
		exit;
	}
}