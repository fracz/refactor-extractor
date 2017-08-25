<?php

namespace Everzet\Behat\Console\Command;

use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputArgument;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;

use Everzet\Behat\ServiceContainer\ServiceContainer;
use Everzet\Behat\Exception\Redundant;

/*
 * This file is part of the Behat package.
 * (c) 2010 Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Behat application test command.
 *
 * @package     Behat
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class TestCommand extends Command
{
    /**
     * @see Symfony\Component\Console\Command\Command
     */
    protected function configure()
    {
        $this->setName('test');

        $this->setDefinition(array(
            new InputArgument('features', InputArgument::OPTIONAL, 'Features path', 'features'),
            new InputOption('--format', '-f', InputOption::PARAMETER_REQUIRED, 'Change output formatter', 'pretty'),
            new InputOption('--tags',   '-t', InputOption::PARAMETER_REQUIRED, 'Only executes features or scenarios with specified tags')
        ));
    }

    /**
     * @see Symfony\Component\Console\Command\Command
     */
    protected function execute(InputInterface $input, OutputInterface $output)
    {
        $featuresPath = realpath($input->getArgument('features'));

        // Find features path
        if (is_dir($featuresPath . '/features')) {
            $basePath = $featuresPath . '/features';
        } elseif (is_file($featuresPath)) {
            $basePath = dirname($featuresPath);
        } else {
            $basePath = $featuresPath;
        }

        // Configure DIC
        $container = new ServiceContainer();
        $container->setParameter('i18n.path',           realpath(__DIR__ . '/../../../../../i18n'));
        $container->setParameter('filter.tags',         $input->getOption('tags'));
        $container->setParameter('features.file',       $featuresPath);
        $container->setParameter('features.path',       $basePath);
        $container->setParameter('steps.path',          $basePath . '/steps');
        $container->setParameter('environment.file',    $basePath . '/support/env.php');
        $container->setParameter('formatter.output',    $output);
        $container->setParameter('formatter.verbose',   $input->getOption('verbose'));
        $container->setParameter('formatter.name',      ucfirst($input->getOption('format')));

        // Check if we had redundant definitions
        try {
            $container->getStepsLoaderService();
        } catch (Redundant $e) {
            $output->write(sprintf("\033[31m%s\033[0m",
                strtr($e->getMessage(), array($basePath . '/' => ''))
            ), true, 1);
            return 1;
        }

        // Get features loader, run test suite & return exit code
        return $container->
            getFeaturesLoaderService()->
            getFeaturesRunner()->
            run();
    }
}