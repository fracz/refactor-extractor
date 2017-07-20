<?php
if (! defined('SIMPLE_TEST')) {
	define('SIMPLE_TEST', '../simpletest/');
}
require_once(SIMPLE_TEST.'autorun.php');
SimpleTest :: prefer(new HtmlReporter());

class Test_Piwik_Blank extends UnitTestCase
{
    function __construct()
    {
        parent::__construct('Log class test');
    }

    /**
     *
     */
    function test_ToAdd()
    {

    }


    /**
     * -> exception
     */
    public function _test_()
    {
    	try {
    		test();
        	$this->fail("Exception not raised.");
    	}
    	catch (Exception $expected) {
    		$this->assertPattern("()", $expected->getMessage());
            return;
        }
    }
}
?>