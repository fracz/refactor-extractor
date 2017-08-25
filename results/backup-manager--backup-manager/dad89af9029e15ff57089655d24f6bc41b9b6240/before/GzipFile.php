<?php namespace BigName\DatabaseBackup\Commands\Archiving;

use BigName\DatabaseBackup\Commands\Command;
use BigName\DatabaseBackup\ShellProcessing\ShellProcessor;

class GzipFile implements Command
{
    /**
     * @var \BigName\DatabaseBackup\ShellProcessing\ShellProcessor
     */
    private $shellProcessor;
    /**
     * @var
     */
    private $sourcePath;

    public function __construct(ShellProcessor $shellProcessor, $sourcePath)
    {
        $this->shellProcessor = $shellProcessor;
        $this->sourcePath = $sourcePath;
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
        return sprintf('gzip %s', escapeshellarg($this->sourcePath));
    }
}