<?php

/*
  +------------------------------------------------------------------------+
  | Phalcon Framework                                                      |
  +------------------------------------------------------------------------+
  | Copyright (c) 2011-2012 Phalcon Team (http://www.phalconphp.com)       |
  +------------------------------------------------------------------------+
  | This source file is subject to the New BSD License that is bundled     |
  | with this package in the file docs/LICENSE.txt.                        |
  |                                                                        |
  | If you did not receive a copy of the license and are unable to         |
  | obtain it through the world-wide-web, please send an email             |
  | to license@phalconphp.com so we can send you a copy immediately.       |
  +------------------------------------------------------------------------+
  | Authors: Andres Gutierrez <andres@phalconphp.com>                      |
  |          Eduar Carvajal <eduar@phalconphp.com>                         |
  +------------------------------------------------------------------------+
*/

class ModelsResultsetTest extends PHPUnit_Framework_TestCase
{

	public function __construct()
	{
		spl_autoload_register(array($this, 'modelsAutoloader'));
	}

	public function __destruct()
	{
		spl_autoload_unregister(array($this, 'modelsAutoloader'));
	}

	public function modelsAutoloader($className)
	{
		if (file_exists('unit-tests/models/'.$className.'.php')) {
			require 'unit-tests/models/'.$className.'.php';
		}
	}

	protected function _getDI()
	{

		Phalcon\DI::reset();

		$di = new Phalcon\DI();

		$di->set('modelsManager', function(){
			return new Phalcon\Mvc\Model\Manager();
		});

		$di->set('modelsMetadata', function(){
			return new Phalcon\Mvc\Model\Metadata\Memory();
		});

		return $di;
	}

	protected function _prepareTestMysql()
	{
		$di = $this->_getDI();

		$di->set('db', function(){
			require 'unit-tests/config.db.php';
			return new Phalcon\Db\Adapter\Pdo\Mysql($configMysql);
		});
	}

	protected function _prepareTestPostgresql()
	{
		$di = $this->_getDI();

		$di->set('db', function(){
			require 'unit-tests/config.db.php';
			return new Phalcon\Db\Adapter\Pdo\Postgresql($configPostgresql);
		});
	}

	protected function _prepareTestSqlite()
	{
		$di = $this->_getDI();

		$di->set('db', function(){
			require 'unit-tests/config.db.php';
			return new Phalcon\Db\Adapter\Pdo\Sqlite($configSqlite);
		});
	}

	public function testResultsetNormalMysql()
	{
		$this->_prepareTestMysql();

		$robots = Robots::find(array(
			'order' => 'id'
		));

		$this->_applyTests($robots);
	}

	public function testResultsetBindingMysql()
	{
		$this->_prepareTestMysql();

		$initialId = 0;
		$finalId = 4;

		$robots = Robots::find(array(
			'conditions' => 'id > :id1: and id < :id2:',
			'bind' => array('id1' => $initialId, 'id2' => $finalId),
			'order' => 'id'
		));

		$this->_applyTests($robots);
	}

	public function testResultsetNormalPostgresql()
	{
		$this->_prepareTestPostgresql();

		$robots = Robots::find(array('order' => 'id'));

		$this->_applyTests($robots);
	}

	public function testResultsetBindingPostgresql()
	{
		$this->_prepareTestPostgresql();

		$initialId = 0;
		$finalId = 4;

		$robots = Robots::find(array(
			'conditions' => 'id > :id1: and id < :id2:',
			'bind' => array('id1' => $initialId, 'id2' => $finalId),
			'order' => 'id'
		));

		$this->_applyTests($robots);
	}

	public function testResultsetNormalSqlite()
	{
		$this->_prepareTestSqlite();

		$robots = Robots::find(array('order' => 'id'));

		$this->_applyTests($robots);
	}

	public function testResultsetBindingSqlite()
	{
		$this->_prepareTestSqlite();

		$initialId = 0;
		$finalId = 4;

		$robots = Robots::find(array(
			'conditions' => 'id > :id1: and id < :id2:',
			'bind' => array('id1' => $initialId, 'id2' => $finalId),
			'order' => 'id'
		));

		$this->_applyTests($robots);
	}

	public function _applyTests($robots)
	{

		$this->assertEquals(count($robots), 3);
		$this->assertEquals($robots->count(), 3);

		//Using a foreach
		$number = 0;
		foreach ($robots as $key => $robot) {
			$this->assertEquals($key, $number);
			$this->assertEquals($robot->id, $number+1);
			$number++;
		}
		$this->assertEquals($number, 3);

		//Using a while
		$number = 0;
		$robots->rewind();
		while ($robots->valid()) {
			$robot = $robots->current();
			$this->assertEquals($robot->id, $number+1);
			$robots->next();
			$number++;
		}
		$this->assertEquals($robots->key(), 3);
		$this->assertEquals($number, 3);

		//Seeking first
		$robots->seek(1);
		$robots->valid();
		$robot = $robots->current();
		$this->assertEquals($robot->id, 2);
		$this->assertEquals($robots->key(), 1);

		//Getting First
		$robot = $robots->getFirst();
		$this->assertEquals($robot->id, 1);
		$this->assertEquals($robots->key(), 0);

		$robot = $robots->getLast();
		$this->assertEquals($robot->id, 3);
		$this->assertEquals($robots->key(), 2);

		$robot = $robots[0];
		$this->assertEquals($robot->id, 1);
		$this->assertEquals($robots->key(), 0);

		$robot = $robots[2];
		$this->assertEquals($robot->id, 3);
		$this->assertEquals($robots->key(), 2);

		$this->assertFalse(isset($robots[4]));

	}

	public function testSerializeNormalMysql()
	{

		$this->_prepareTestMysql();

		$data = serialize(Robots::find(array('order' => 'id')));

		$robots = unserialize($data);

		$this->assertEquals(get_class($robots), 'Phalcon\Mvc\Model\Resultset\Simple');

		$this->_applyTests($robots);

	}

	public function testSerializeBindingsMysql()
	{

		$this->_prepareTestMysql();

		$initialId = 0;
		$finalId = 4;

		$data = serialize(Robots::find(array(
			'conditions' => 'id > :id1: and id < :id2:',
			'bind' => array('id1' => $initialId, 'id2' => $finalId),
			'order' => 'id'
		)));

		$robots = unserialize($data);

		$this->assertEquals(get_class($robots), 'Phalcon\Mvc\Model\Resultset\Simple');

		$this->_applyTests($robots);

	}

	public function testSerializeNormalPostgresql()
	{

		$this->_prepareTestPostgresql();

		$data = serialize(Robots::find(array('order' => 'id')));

		$robots = unserialize($data);

		$this->assertEquals(get_class($robots), 'Phalcon\Mvc\Model\Resultset\Simple');

		$this->_applyTests($robots);

	}

	public function testSerializeBindingsPostgresql()
	{

		$this->_prepareTestPostgresql();

		$initialId = 0;
		$finalId = 4;

		$data = serialize(Robots::find(array(
			'conditions' => 'id > :id1: and id < :id2:',
			'bind' => array('id1' => $initialId, 'id2' => $finalId),
			'order' => 'id'
		)));

		$robots = unserialize($data);

		$this->assertEquals(get_class($robots), 'Phalcon\Mvc\Model\Resultset\Simple');

		$this->_applyTests($robots);

	}

	public function testSerializeNormalSqlite()
	{

		$this->_prepareTestPostgresql();

		$data = serialize(Robots::find(array('order' => 'id')));

		$robots = unserialize($data);

		$this->assertEquals(get_class($robots), 'Phalcon\Mvc\Model\Resultset\Simple');

		$this->_applyTests($robots);

	}

	public function testSerializeBindingsSqlite()
	{

		$this->_prepareTestPostgresql();

		$initialId = 0;
		$finalId = 4;

		$data = serialize(Robots::find(array(
			'conditions' => 'id > :id1: and id < :id2:',
			'bind' => array('id1' => $initialId, 'id2' => $finalId),
			'order' => 'id'
		)));

		$robots = unserialize($data);

		$this->assertEquals(get_class($robots), 'Phalcon\Mvc\Model\Resultset\Simple');

		$this->_applyTests($robots);

	}

}