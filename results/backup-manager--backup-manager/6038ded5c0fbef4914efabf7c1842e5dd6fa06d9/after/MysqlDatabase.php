<?php namespace BigName\DatabaseBackup\Databases;

use BigName\DatabaseBackup\Filesystems\Database;

class MysqlDatabase implements Database
{
    private $config;

    public function __construct(array $config)
    {
        $this->config = $config;
    }

    public function getDumpCommandLine($outputPath)
    {
        return sprintf('mysqldump --host=%s --port=%s --user=%s --password=%s %s > %s',
            escapeshellarg($this->config['host']),
            escapeshellarg($this->config['port']),
            escapeshellarg($this->config['username']),
            escapeshellarg($this->config['password']),
            escapeshellarg($this->config['database']),
            escapeshellarg($outputPath)
        );
    }

    public function getRestoreCommandLine($inputPath)
    {
        return sprintf('mysql --host=%s --port=%s --user=%s --password=%s %s -e "source %s;"',
            escapeshellarg($this->config['host']),
            escapeshellarg($this->config['port']),
            escapeshellarg($this->config['username']),
            escapeshellarg($this->config['password']),
            escapeshellarg($this->config['database']),
            $inputPath
        );
    }
}