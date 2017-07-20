<?php
/**
 * CakePHP(tm) : Rapid Development Framework (http://cakephp.org)
 * Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 *
 * Licensed under The MIT License
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright     Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 * @link          http://cakephp.org CakePHP(tm) Project
 * @since         3.0.0
 * @license       http://www.opensource.org/licenses/mit-license.php MIT License
 */
namespace Cake\Test\TestCase\Core;

use Cake\Core\StaticConfigTrait;
use Cake\TestSuite\TestCase;
use PHPUnit_Framework_Test;

/**
 * TestConnectionManagerStaticConfig
 */
class TestConnectionManagerStaticConfig {

	use StaticConfigTrait {
		parseDsn as protected _parseDsn;
	}

	public static function parseDsn($config = null) {
		$config = static::_parseDsn($config);

		if (isset($config['path']) && empty($config['database'])) {
			$config['database'] = substr($config['path'], 1);
		}

		if (empty($config['driver'])) {
			$config['driver'] = $config['className'];
			$config['className'] = 'Cake\Database\Connection';
		}

		unset($config['path']);
		return $config;
	}

	public static function getClassMap() {
		return [
			'mysql' => 'Cake\Database\Driver\Mysql',
			'postgres' => 'Cake\Database\Driver\Postgres',
			'sqlite' => 'Cake\Database\Driver\Sqlite',
			'sqlserver' => 'Cake\Database\Driver\Sqlserver',
		];
	}

}

/**
 * TestCacheStaticConfig
 */
class TestCacheStaticConfig {

	use StaticConfigTrait;

	public static function getClassMap() {
		return [
			'apc' => 'Cake\Cache\Engine\ApcEngine',
			'file' => 'Cake\Cache\Engine\FileEngine',
			'memcached' => 'Cake\Cache\Engine\MemcachedEngine',
			'null' => 'Cake\Cache\Engine\NullEngine',
			'redis' => 'Cake\Cache\Engine\RedisEngine',
			'wincache' => 'Cake\Cache\Engine\WincacheEngine',
			'xcache' => 'Cake\Cache\Engine\XcacheEngine',
		];
	}

}

/**
 * TestEmailStaticConfig
 */
class TestEmailStaticConfig {

	use StaticConfigTrait;

	public static function getClassMap() {
		return [
			'debug' => 'Cake\Network\Email\DebugTransport',
			'mail' => 'Cake\Network\Email\MailTransport',
			'smtp' => 'Cake\Network\Email\SmtpTransport',
		];
	}

}

/**
 * TestLogStaticConfig
 */
class TestLogStaticConfig {

	use StaticConfigTrait;

	public static function getClassMap() {
		return [
			'console' => 'Cake\Log\Engine\ConsoleLog',
			'file' => 'Cake\Log\Engine\FileLog',
			'syslog' => 'Cake\Log\Engine\SyslogLog',
		];
	}

}

/**
 * StaticConfigTraitTest class
 *
 */
class StaticConfigTraitTest extends TestCase {

	public function setUp() {
		parent::setUp();
		$this->subject = $this->getObjectForTrait('Cake\Core\StaticConfigTrait');
	}

	public function tearDown() {
		unset($this->subject);
		parent::tearDown();
	}

/**
 * Tests simple usage of parseDsn
 *
 * @return void
 */
	public function testSimpleParseDsn() {
		$klassName = get_class($this->subject);

		$this->assertSame([], $klassName::parseDsn(''));
	}

/**
 * Tests that failing to pass a string to parseDsn will throw an exception
 *
 * @expectedException InvalidArgumentException
 * @return void
 */
	public function testParseBadType() {
		$klassName = get_class($this->subject);
		$klassName::parseDsn(['url' => 'http://:80']);
	}

/**
 * Tests parsing different DSNs
 *
 * @return void
 */
	public function testCustomParseDsn() {
		$dsn = 'mysql://localhost:3306/database';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'driver' => 'Cake\Database\Driver\Mysql',
			'host' => 'localhost',
			'database' => 'database',
			'port' => 3306,
			'scheme' => 'mysql',
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));

		$dsn = 'mysql://user:password@localhost:3306/database';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'driver' => 'Cake\Database\Driver\Mysql',
			'host' => 'localhost',
			'password' => 'password',
			'database' => 'database',
			'port' => 3306,
			'scheme' => 'mysql',
			'username' => 'user',
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));

		$dsn = 'sqlite:///memory:';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'driver' => 'Cake\Database\Driver\Sqlite',
			'database' => 'memory:',
			'scheme' => 'sqlite',
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));

		$dsn = 'sqlite:///?database=memory:';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'driver' => 'Cake\Database\Driver\Sqlite',
			'database' => 'memory:',
			'scheme' => 'sqlite',
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));

		$dsn = 'sqlserver://sa:Password12!@.\SQL2012SP1/cakephp?MultipleActiveResultSets=false';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'driver' => 'Cake\Database\Driver\Sqlserver',
			'host' => '.\SQL2012SP1',
			'MultipleActiveResultSets' => false,
			'password' => 'Password12!',
			'database' => 'cakephp',
			'scheme' => 'sqlserver',
			'username' => 'sa',
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));
	}

/**
 * Tests className/driver value setting
 *
 * @return void
 */
	public function testParseDsnClassnameDriver() {
		$dsn = 'mysql://localhost:3306/database';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'database' => 'database',
			'driver' => 'Cake\Database\Driver\Mysql',
			'host' => 'localhost',
			'port' => 3306,
			'scheme' => 'mysql',
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));

		$dsn = 'mysql://user:password@localhost:3306/database';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'database' => 'database',
			'driver' => 'Cake\Database\Driver\Mysql',
			'host' => 'localhost',
			'password' => 'password',
			'port' => 3306,
			'scheme' => 'mysql',
			'username' => 'user',
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));

		$dsn = 'mysql://localhost/database?className=Custom\Driver';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'database' => 'database',
			'driver' => 'Custom\Driver',
			'host' => 'localhost',
			'scheme' => 'mysql',
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));

		$dsn = 'mysql://localhost:3306/database?className=Custom\Driver';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'database' => 'database',
			'driver' => 'Custom\Driver',
			'host' => 'localhost',
			'scheme' => 'mysql',
			'port' => 3306,
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));

		$dsn = 'Cake\Database\Connection://localhost:3306/database?driver=Cake\Database\Driver\Mysql';
		$expected = [
			'className' => 'Cake\Database\Connection',
			'database' => 'database',
			'driver' => 'Cake\Database\Driver\Mysql',
			'host' => 'localhost',
			'scheme' => 'Cake\Database\Connection',
			'port' => 3306,
		];
		$this->assertEquals($expected, TestConnectionManagerStaticConfig::parseDsn($dsn));
	}

/**
 * Tests parsing querystring values
 *
 * @return void
 */
	public function testParseDsnQuerystring() {
		$dsn = 'file:///?url=test';
		$expected = [
			'className' => 'Cake\Log\Engine\FileLog',
			'path' => '/',
			'scheme' => 'file',
			'url' => 'test',
		];
		$this->assertEquals($expected, TestLogStaticConfig::parseDsn($dsn));

		$dsn = 'file:///?file=debug&key=value';
		$expected = [
			'className' => 'Cake\Log\Engine\FileLog',
			'file' => 'debug',
			'key' => 'value',
			'path' => '/',
			'scheme' => 'file',
		];
		$this->assertEquals($expected, TestLogStaticConfig::parseDsn($dsn));

		$dsn = 'file:///tmp?file=debug&types[]=notice&types[]=info&types[]=debug';
		$expected = [
			'className' => 'Cake\Log\Engine\FileLog',
			'file' => 'debug',
			'path' => '/tmp',
			'scheme' => 'file',
			'types' => ['notice', 'info', 'debug'],
		];
		$this->assertEquals($expected, TestLogStaticConfig::parseDsn($dsn));

		$dsn = 'mail:///?timeout=30&key=true&key2=false&client=null&tls=null';
		$expected = [
			'className' => 'Cake\Network\Email\MailTransport',
			'client' => null,
			'key' => true,
			'key2' => false,
			'path' => '/',
			'scheme' => 'mail',
			'timeout' => '30',
			'tls' => null,
		];
		$this->assertEquals($expected, TestEmailStaticConfig::parseDsn($dsn));

		$dsn = 'mail://true:false@null/1?timeout=30&key=true&key2=false&client=null&tls=null';
		$expected = [
			'className' => 'Cake\Network\Email\MailTransport',
			'client' => null,
			'host' => 'null',
			'key' => true,
			'key2' => false,
			'password' => 'false',
			'path' => '/1',
			'scheme' => 'mail',
			'timeout' => '30',
			'tls' => null,
			'username' => 'true',
		];
		$this->assertEquals($expected, TestEmailStaticConfig::parseDsn($dsn));

		$dsn = 'mail://user:secret@localhost:25?timeout=30&client=null&tls=null';
		$expected = [
			'className' => 'Cake\Network\Email\MailTransport',
			'client' => null,
			'host' => 'localhost',
			'password' => 'secret',
			'port' => 25,
			'scheme' => 'mail',
			'timeout' => '30',
			'tls' => null,
			'username' => 'user',
		];
		$this->assertEquals($expected, TestEmailStaticConfig::parseDsn($dsn));

		$dsn = 'file:///?prefix=myapp_cake_core_&serialize=true&duration=%2B2 minutes';
		$expected = [
			'className' => 'Cake\Log\Engine\FileLog',
			'duration' => '+2 minutes',
			'path' => '/',
			'prefix' => 'myapp_cake_core_',
			'scheme' => 'file',
			'serialize' => true,
		];
		$this->assertEquals($expected, TestLogStaticConfig::parseDsn($dsn));
	}

/**
 * Tests loading a single plugin
 *
 * @return void
 */
	public function testParseDsnPathSetting() {
		$dsn = 'file:///';
		$expected = [
			'className' => 'Cake\Log\Engine\FileLog',
			'path' => '/',
			'scheme' => 'file',
		];
		$this->assertEquals($expected, TestLogStaticConfig::parseDsn($dsn));

		$dsn = 'file:///?path=/tmp/persistent/';
		$expected = [
			'className' => 'Cake\Log\Engine\FileLog',
			'path' => '/tmp/persistent/',
			'scheme' => 'file',
		];
		$this->assertEquals($expected, TestLogStaticConfig::parseDsn($dsn));
	}

}
