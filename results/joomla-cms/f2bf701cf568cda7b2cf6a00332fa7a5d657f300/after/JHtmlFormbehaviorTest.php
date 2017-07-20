<?php
/**
 * @package     Joomla.UnitTest
 * @subpackage  HTML
 *
 * @copyright   Copyright (C) 2005 - 2016 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE
 */

require_once __DIR__ . '/stubs/JHtmlJqueryInspector.php';

/**
 * Test class for JHtmlFormbehavior.
 * Generated by PHPUnit on 2012-08-16 at 17:39:35.
 */
class JHtmlFormbehaviorTest extends TestCase
{
	/**
	 * Backup of the SERVER superglobal
	 *
	 * @var    array
	 * @since  3.1
	 */
	protected $backupServer;

	/**
	 * Sets up the fixture, for example, opens a network connection.
	 * This method is called before a test is executed.
	 *
	 * @return  void
	 *
	 * @since   3.1
	 */
	protected function setUp()
	{
		$this->saveFactoryState();

		parent::setUp();

		JFactory::$application = $this->getMockCmsApp();
		JFactory::$document = $this->getMockDocument();

		$this->backupServer = $_SERVER;

		$_SERVER['HTTP_HOST'] = 'example.com';
		$_SERVER['SCRIPT_NAME'] = '';
	}

	/**
	 * Tears down the fixture, for example, closes a network connection.
	 * This method is called after a test is executed.
	 *
	 * @return  void
	 *
	 * @since   3.1
	 */
	protected function tearDown()
	{
		$_SERVER = $this->backupServer;
		unset($this->backupServer);
		$this->restoreFactoryState();

		parent::tearDown();

		JHtmlJqueryInspector::resetLoaded();
	}

	/**
	 * Tests the chosen method.
	 *
	 * @return  void
	 *
	 * @since   3.1
	 */
	public function testChosen()
	{
		// Initialise the chosen script
		JHtmlFormbehavior::chosen('testSelect');

		// Get the document instance
		$document = JFactory::getDocument();

		$this->assertArrayHasKey(
			'/media/jui/js/jquery.min.js',
			$document->_scripts,
			'Verify that the chosen method initialises jQuery as well'
		);

		$this->assertArrayHasKey(
			'/media/jui/js/chosen.jquery.min.js',
			$document->_scripts,
			'Verify that the Chosen JS is loaded'
		);

		$this->assertArrayHasKey(
			'/media/jui/css/chosen.css',
			$document->_styleSheets,
			'Verify that the Chosen CSS is loaded'
		);

		$this->assertContains(
			"jQuery('testSelect').chosen",
			$document->_script['text/javascript'],
			'Verify that the Chosen JS is initialised with the supplied selector'
		);
	}
}