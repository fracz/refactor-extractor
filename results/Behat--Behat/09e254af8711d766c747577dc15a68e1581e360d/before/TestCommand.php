<?php

namespace Everzet\Behat\Console\Command;

use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputArgument;
use Symfony\Component\Console\Output\OutputInterface;

use Everzet\Behat\ServiceContainer\ServiceContainer;
use Everzet\Behat\Exception\Redundant;

/*
 * This file is part of the behat package.
 * (c) 2010 Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Behat console test command.
 *
 * @package     behat
 * @subpackage  Behat
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class TestCommand extends Command
{
    /**
     * @see \Symfony\Component\Console\Command\Command
     */
    protected function configure()
    {
        $this->setName('test');

        $this->setDefinition(array(
            new InputArgument('features', InputArgument::OPTIONAL, 'Features folder', 'features')
        ));
    }

    /**
     * @see \Symfony\Component\Console\Command\Command
     */
    protected function execute(InputInterface $input, OutputInterface $output)
    {
        $basePath = realpath($input->getArgument('features'));
        $featureFiles = array();

        if (is_dir($basePath.'/features')) {
            $basePath .= '/features';
        } elseif (is_file($basePath)) {
            $featureFiles[] = $basePath;
            $basePath = dirname($basePath);
        }

        // Configure DIC
        $container = new ServiceContainer();
        $container->setParameter('i18n.path',           realpath(__DIR__ . '/../../../../../i18n'));
        $container->setParameter('features.path',       $basePath);
        $container->setParameter('steps.path',          $basePath . '/steps');
        $container->setParameter('environment.file',    $basePath . '/support/env.php');
        $container->setParameter('logger.output',       $output);
        $container->setParameter('logger.verbose',      $input->getOption('verbose'));

        // Check if we had redundant definitions
        try {
            $container->getStepsLoaderService();
        } catch (Redundant $e) {
            $output->writeln(sprintf("<failed>%s</failed>",
                strtr($e->getMessage(), array($basePath . '/' => ''))
            ));
            return 1;
        }

        $container->
            getFeaturesLoaderService()->
            getFeaturesRunner()->
            run();
    }
}