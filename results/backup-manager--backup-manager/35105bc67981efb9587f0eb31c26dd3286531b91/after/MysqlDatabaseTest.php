<?php

use BigName\BackupManager\Config\Config;
use BigName\BackupManager\Databases\MysqlDatabase;
use Mockery as m;

class MysqlDatabaseTest extends PHPUnit_Framework_TestCase
{
    protected function tearDown()
    {
        m::close();
    }

    public function test_can_create()
    {
        $mysql = $this->getDatabase();
        $this->assertInstanceOf('BigName\BackupManager\Databases\MysqlDatabase', $mysql);
    }

    public function test_get_dump_command()
    {
        $config = new Config('tests/config/database.php');
        $mysql = $this->getDatabase();
        $mysql->setConfig($config->get('development'));
        $this->assertEquals("mysqldump --host='foo' --port='3306' --user='bar' --password='baz' 'test' > 'outputPath'", $mysql->getDumpCommandLine('outputPath'));
    }

    public function test_get_restore_command()
    {
        $config = new Config('tests/config/database.php');
        $mysql = $this->getDatabase();
        $mysql->setConfig($config->get('development'));
        $this->assertEquals("mysql --host='foo' --port='3306' --user='bar' --password='baz' 'test' -e \"source outputPath;\"", $mysql->getRestoreCommandLine('outputPath'));
    }

    /**
     * @return MysqlDatabase
     */
    private function getDatabase()
    {
        $database = new MysqlDatabase;
        return $database;
    }
}