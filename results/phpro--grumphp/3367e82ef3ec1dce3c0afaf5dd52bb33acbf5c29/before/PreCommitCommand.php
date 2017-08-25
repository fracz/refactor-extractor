<?php

namespace GrumPHP\Console\Command\Git;

use GrumPHP\Configuration\GrumPHP;
use GrumPHP\Locator\ChangedFiles;
use GrumPHP\TaskManager;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;

/**
 * Class InitCommand
 *
 * @package GrumPHP\Console\Command
 */
class PreCommitCommand extends Command
{

    const COMMAND_NAME = 'git:pre-commit';

    /**
     * Configure command
     */
    protected function configure()
    {
        $this
            ->setName(self::COMMAND_NAME)
            ->setDefinition(array(
                new InputOption('base-dir', 'b', InputOption::VALUE_OPTIONAL, '.', getcwd()),
            ));
    }

    /**
     * @param InputInterface  $input
     * @param OutputInterface $output
     *
     * @return int|void
     */
    public function execute(InputInterface $input, OutputInterface $output)
    {
        $baseDir = $input->getOption('base-dir');

        $config = GrumPHP::loadFromComposerFile($baseDir);
        $taskManager = new TaskManager($config);

        try {
            $files = $this->getCommitedFiles($config->getGitDir());
            $taskManager->run($files);
        } catch (\Exception $e) {
            $output->writeln('<fg=red>' . $this->getAsciiResource('failed') . '</fg=red>');
            $output->writeln('<fg=red>' . $e->getMessage() . '</fg=red>');
            return 1;
        }

        $output->write('<fg=green>' . $this->getAsciiResource('succeeded') . '</fg=green>');
        return 0;
    }

    /**
     * @param $gitDir
     *
     * @return array
     */
    protected function getCommitedFiles($gitDir)
    {
        $locator = new ChangedFiles($gitDir);
        return $locator->locate(ChangedFiles::PATTERN_PHP);
    }

    /**
     * @param $name
     *
     * @return string
     */
    protected function getAsciiResource($name)
    {
        return file_get_contents(sprintf('%s/resources/ascii/%s.txt', GRUMPHP_PATH, $name));
    }
}