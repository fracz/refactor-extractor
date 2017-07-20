<?php
/**
 * @package     Joomla.UnitTest
 * @subpackage  Schema
 *
 * @copyright   Copyright (C) 2005 - 2016 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE
 */

/**
 * Test class for JSchemaChangeset covering the MySQL driver.
 */
class JSchemaChangesetTestMysql extends TestCaseDatabaseMysql
{
	/**
	 * Object under test
	 *
	 * @var  JSchemaChangeset
	 */
	protected $object;

	/**
	 * Sets up the fixture, for example, opens a network connection.
	 * This method is called before a test is executed.
	 *
	 * @return  void
	 */
	protected function setUp()
	{
		parent::setUp();

		// Register the object
		$this->object = new JSchemaChangeset(static::$driver, __DIR__ . '/stubs');
	}

	/**
	 * @testdox  The object is instantiated correctly
	 *
	 * @covers   JSchemaChangeset::__construct
	 * @covers   JSchemaChangeset::getUpdateFiles
	 * @covers   JSchemaChangeset::getUpdateQueries
	 * @medium
	 */
	public function testTheObjectIsInstantiatedCorrectly()
	{
		$this->assertAttributeInstanceOf('JDatabaseDriverMysql', 'db', $this->object, 'The database driver was not correctly injected');
		$this->assertAttributeContainsOnly('JSchemaChangeitemMysql', 'changeItems', $this->object, null, 'The list of change items was not correctly set');
	}

	/**
	 * @testdox  The schema's status is correctly validated
	 *
	 * @covers   JSchemaChangeset::getStatus
	 */
	public function testTheSchemaStatusIsCorrectlyValidated()
	{
		$status = $this->object->getStatus();

		$this->assertArrayHasKey('unchecked', $status, 'An array should be returned containing a list of unchecked items');
		$this->assertContainsOnlyInstancesOf('JSchemaChangeitemMysql', $status['unchecked'], 'The unchecked items array should only contain JSchemaChangeitem objects');
	}

	/**
	 * @testdox  The latest schema version is returned
	 *
	 * @covers   JSchemaChangeset::getStatus
	 * @covers   JSchemaChangeset::getUpdateFiles
	 */
	public function testTheLatestSchemaVersionIsReturned()
	{
		$this->assertSame('3.5.0-2016-03-01', $this->object->getSchema(), 'The latest schema version was not returned');
	}
}