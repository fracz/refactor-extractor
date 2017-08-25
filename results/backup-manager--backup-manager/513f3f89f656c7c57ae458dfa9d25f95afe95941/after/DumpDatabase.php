<?php namespace McCool\DatabaseBackup\Commands\Database\Mysql;

use McCool\DatabaseBackup\Commands\Command;
use McCool\DatabaseBackup\Connections\MysqlConnection;
use McCool\DatabaseBackup\ShellProcessor;

/**
 * Class DumpDatabase
 * @package McCool\DatabaseBackup\Commands\Database\Mysql
 */
class DumpDatabase implements Command
{
    /**
     * @var string
     */
    private $outputPath;
    /**
     * @var MysqlConnection
     */
    private $connection;
    /**
     * @var \McCool\DatabaseBackup\ShellProcessor
     */
    private $shellProcessor;

    /**
     * @param $outputPath
     * @param MysqlConnection $connection
     * @param ShellProcessor $shellProcessor
     */
    public function __construct(MysqlConnection $connection, ShellProcessor $shellProcessor, $outputPath)
    {
        $this->outputPath = $outputPath;
        $this->connection = $connection;
        $this->shellProcessor = $shellProcessor;
    }

    /**
     * Execute the command.
     */
    public function execute()
    {
        $this->shellProcessor->process($this->getCommand());
    }

    /**
     * Produce the command string.
     * @return string
     */
    private function getCommand()
    {
        return sprintf('mysqldump --host=%s --port=%s --user=%s --password=%s %s > %s',
            escapeshellarg($this->connection->host),
            escapeshellarg($this->connection->port),
            escapeshellarg($this->connection->username),
            escapeshellarg($this->connection->password),
            escapeshellarg($this->connection->database),
            escapeshellarg($this->outputPath)
        );
    }
}