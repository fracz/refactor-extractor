<?php
/**
 * @package     Joomla.UnitTest
 * @subpackage  com_finder
 *
 * @copyright   Copyright (C) 2005 - 2016 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE
 */

require_once JPATH_ADMINISTRATOR . '/components/com_finder/helpers/indexer/parser/rtf.php';

/**
 * Test class for FinderIndexerParserRtf.
 * Generated by PHPUnit on 2012-06-10 at 14:44:57.
 */
class FinderIndexerParserRtfTest extends PHPUnit_Framework_TestCase
{
	/**
	 * @var FinderIndexerParserRtf
	 */
	protected $object;

	/**
	 * Sets up the fixture, for example, opens a network connection.
	 * This method is called before a test is executed.
	 */
	protected function setUp()
	{
		$this->object = new FinderIndexerParserRtf;
	}

	/**
	 * Tears down the fixture, for example, closes a network connection.
	 * This method is called after a test is executed.
	 *
	 * @return void
	 *
	 * @see     PHPUnit_Framework_TestCase::tearDown()
	 * @since   3.6
	 */
	protected function tearDown()
	{
		unset($this->object);
	}

	/**
	 * Method to test the parse and process methods.
	 */
	public function testParse()
	{
		$testResult = 'massa elementum. Mauris consequat';

		$input = file_get_contents(dirname(__DIR__) . '/data/parseHtml.txt');

		$this->assertContains(
			$testResult,
			$this->object->parse($input)
		);
	}
}