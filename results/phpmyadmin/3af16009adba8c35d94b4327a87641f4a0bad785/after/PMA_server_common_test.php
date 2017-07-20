<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * tests for server_common.lib.php
 *
 * @package PhpMyAdmin-test
 */

/*
 * Include to test.
 */
require_once 'libraries/Util.class.php';
require_once 'libraries/php-gettext/gettext.inc';
require_once 'libraries/url_generating.lib.php';
require_once 'libraries/server_common.lib.php';
require_once 'libraries/Theme.class.php';

class PMA_ServerCommon_Test extends PHPUnit_Framework_TestCase
{
    /**
     * Prepares environment for the test.
     *
     * @return void
     */
    public function setUp()
    {
        $_SESSION['PMA_Theme'] = PMA_Theme::load('./themes/pmahomme');
    }

    /**
     * Test for PMA_getSubPageHeader
     *
     * @return void
     */
    public function testPMA_getSubPageHeader()
    {
    	//server_engines
    	$html = PMA_getSubPageHeader("engines");
        $this->assertContains(
            '<img src="themes/dot.gif" title="" alt="" class="icon ic_b_engine" />',
        	$html
        );
        $this->assertContains(
            'Storage Engines',
        	$html
        );

    	//server_databases
    	$html = PMA_getSubPageHeader("databases");
        $this->assertContains(
            '<img src="themes/dot.gif" title="" alt="" class="icon ic_s_db" />',
        	$html
        );
        $this->assertContains(
            'Databases',
        	$html
        );

    	//server_replication
    	$html = PMA_getSubPageHeader("replication");
        $this->assertContains(
            '<img src="themes/dot.gif" title="" alt="" class="icon ic_s_replication" />',
        	$html
        );
        $this->assertContains(
            'Replication',
        	$html
        );
    }

}