<?php namespace BigName\DatabaseBackup\Commands\Archiving;

use BigName\DatabaseBackup\Commands\Command;
use BigName\DatabaseBackup\ShellProcessing\ShellProcessor;

class GzipFile implements Command
{
    private $sourcePath;
    private $shellProcessor;

    public function __construct($sourcePath, ShellProcessor $shellProcessor)
    {
        $this->sourcePath = $sourcePath;
        $this->shellProcessor = $shellProcessor;
    }

    public function execute()
    {
        return $this->shellProcessor->process($this->getCommand());
    }

    private function getCommand()
    {
        return sprintf('gzip %s', escapeshellarg($this->sourcePath));
    }
}