<?php

namespace Behat\Behat\Console\Processor;

use Symfony\Component\DependencyInjection\ContainerInterface,
    Symfony\Component\Console\Input\InputInterface,
    Symfony\Component\Console\Input\InputOption,
    Symfony\Component\Console\Output\OutputInterface;

class HelpProcessor implements ProcessorInterface
{
    /**
     * @see     Behat\Behat\Console\Configuration\ProcessorInterface::getInputOptions()
     */
    public function getInputOptions()
    {
        return array(
            new InputOption('--story-syntax',    null,
                InputOption::VALUE_NONE,
                ' ' .
                'Print *.feature example in specified language (<info>--lang</info>).'
            ),
            new InputOption('--definitions',    null,
                InputOption::VALUE_NONE,
                '  ' .
                'Print available step definitions in specified language (<info>--lang</info>).'."\n"
            ),
        );
    }

    /**
     * @see     Behat\Behat\Console\Configuration\ProcessorInterface::process()
     */
    public function process(ContainerInterface $container, InputInterface $input, OutputInterface $output)
    {
        if ($input->hasOption('story-syntax') && $input->getOption('story-syntax')) {
            $container->get('behat.help_printer.story_syntax')->printSyntax(
                $output, $input->getOption('lang') ?: 'en'
            );

            exit(0);
        } elseif ($input->hasOption('definitions') && $input->getOption('definitions')) {
            $container->get('behat.help_printer.definitions')->printDefinitions(
                $output, $input->getOption('lang') ?: 'en'
            );

            exit(0);
        }
    }
}