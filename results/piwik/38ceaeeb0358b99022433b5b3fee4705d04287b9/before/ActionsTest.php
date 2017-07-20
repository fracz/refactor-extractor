<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id$
 */
require_once 'Actions/Actions.php';

class ActionsTests extends PHPUnit_Framework_TestCase
{
    function setUp()
    {
        Piwik_Translate::getInstance()->loadEnglishTranslation();
    }

    function tearDown()
    {
        Piwik_Translate::getInstance()->unloadEnglishTranslation();
    }

    public function getActionNameTestData()
    {
        return array(
            array(
                'params'    => array( 'name' => 'http://example.org/', 'type' => Piwik_Tracker_Action::TYPE_ACTION_URL),
                'expected'  => array('/index' ),
            ),
            array(
                'params'    => array( 'name' => 'http://example.org/path/', 'type' => Piwik_Tracker_Action::TYPE_ACTION_URL),
                'expected'  => array( 'path', '/index' ),
            ),
            array(
                'params'    => array( 'name' => 'http://example.org/test/path', 'type' => Piwik_Tracker_Action::TYPE_ACTION_URL),
                'expected'  => array( 'test', '/path' ),
            ),
            array(
                'params'    => array( 'name' => 'Test / Path', 'type' => Piwik_Tracker_Action::TYPE_ACTION_URL),
                'expected'  => array( 'Test', '/Path' ),
            ),
            array(
                'params'    => array( 'name' => '    Test trim   ', 'type' => Piwik_Tracker_Action::TYPE_ACTION_URL),
                'expected'  => array( '/Test trim' ),
            ),
            array(
                'params'    => array( 'name' => 'Category / Subcategory', 'type' => Piwik_Tracker_Action::TYPE_ACTION_NAME),
                'expected'  => array( 'Category', ' Subcategory' ),
            ),
            array(
                'params'    => array( 'name' => '/path/index.php?var=test', 'type' => Piwik_Tracker_Action::TYPE_ACTION_NAME),
                'expected'  => array( 'path', ' index.php?var=test' ),
            ),
            array(
                'params'    => array( 'name' => 'http://example.org/path/Default.aspx#anchor', 'type' => Piwik_Tracker_Action::TYPE_ACTION_NAME),
                'expected'  => array( 'path', ' Default.aspx' ),
            ),
            array(
                'params'    => array( 'name' => '', 'type' => Piwik_Tracker_Action::TYPE_ACTION_NAME),
                'expected'  => array( 'Page Name not defined' ),
            ),
            array(
                'params'    => array( 'name' => '', 'type' => Piwik_Tracker_Action::TYPE_ACTION_URL),
                'expected'  => array( 'Page URL not defined' ),
            ),
            array(
                'params'    => array( 'name' => 'http://example.org/download.zip', 'type' => Piwik_Tracker_Action::TYPE_DOWNLOAD),
                'expected'  => array( 'example.org', '/download.zip' ),
            ),
            array(
                'params'    => array( 'name' => 'http://example.org/download/1/', 'type' => Piwik_Tracker_Action::TYPE_DOWNLOAD),
                'expected'  => array( 'example.org', '/download/1/' ),
            ),
            array(
                'params'    => array( 'name' => 'http://example.org/link', 'type' => Piwik_Tracker_Action::TYPE_OUTLINK),
                'expected'  => array( 'example.org', '/link' ),
            ),
            array(
                'params'    => array( 'name' => 'http://example.org/some/path/', 'type' => Piwik_Tracker_Action::TYPE_OUTLINK),
                'expected'  => array( 'example.org', '/some/path/' ),
            ),
        );
    }

    /**
     * @dataProvider getActionNameTestData
     * @group Plugins
     * @group Actions
     */
    public function testGetActionExplodedNames($params, $expected)
    {
        $action = new Test_Piwik_Actions_getActionExplodedNames();

        $processed = $action->public_getActionExplodedNames($params['name'],$params['type']);
        $this->assertEquals($expected, $processed);
    }
}

class Test_Piwik_Actions_getActionExplodedNames extends Piwik_Actions {
    public function public_getActionExplodedNames($name, $type)
    {
        return self::getActionExplodedNames($name, $type);
    }
}