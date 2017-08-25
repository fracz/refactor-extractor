<?php

/*
 * (c) Jean-François Lépine <https://twitter.com/Halleck45>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Hal\Application\Command;
use Hal\Component\Aggregator\DirectoryAggregator;
use Hal\Component\Aggregator\DirectoryAggregatorFlat;
use Hal\Component\Aggregator\DirectoryRecursiveAggregator;
use Hal\Component\Bounds\Bounds;
use Hal\Component\Bounds\DirectoryBounds;
use Hal\Application\Command\Job\DoAnalyze;
use Hal\Application\Command\Job\Queue;
use Hal\Application\Command\Job\ReportRenderer;
use Hal\Application\Command\Job\ReportWriter;
use Hal\Application\Command\Job\SearchBounds;
use Hal\Component\File\Finder;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputArgument;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;
use Hal\Application\Formater\Summary;
use Hal\Application\Formater\Details;
use Hal\Application\Command\Job\DoAggregatedAnalyze;

/**
 * Command for run analysis
 *
 * @author Jean-François Lépine <https://twitter.com/Halleck45>
 */
class RunMetricsCommand extends Command
{
    /**
     * @inheritdoc
     */
    protected function configure()
    {
        $this
                ->setName('metrics')
                ->setDescription('Run analysis')
                ->addArgument(
                        'path', InputArgument::REQUIRED, 'Path to explore'
                )
                ->addOption(
                        'report-html',null, InputOption::VALUE_REQUIRED, 'Path to save report in HTML format. Example: /tmp/report.html'
                )
                ->addOption(
                        'summary-xml', null, InputOption::VALUE_REQUIRED, 'Path to save summary report in XML format. Example: /tmp/report.xml'
                )
                ->addOption(
                        'level', null, InputOption::VALUE_REQUIRED, 'Depth of summary report', 0
                )
                ->addOption(
                        'extensions', null, InputOption::VALUE_REQUIRED, 'Regex of extensions to include', 'php'
                )
                ->addOption(
                        'without-oop', null, InputOption::VALUE_NONE, 'If provided, tool will not extract any informations about OOP model (faster)'
                )
        ;
    }

    /**
     * @inheritdoc
     */
    protected function execute(InputInterface $input, OutputInterface $output)
    {

        $output->writeln('PHPMetrics by Jean-François Lépine <https://twitter.com/Halleck45>');
        $output->writeln('');

        $level = $input->getOption('level');

        // files
        $finder = new Finder($input->getOption('extensions'));

        // rules
        $rules = new \Hal\Application\Rule\RuleSet();
        $validator = new \Hal\Application\Rule\Validator($rules);

        // bounds
        $bounds = new Bounds;

        // jobs queue planning
        $queue = new Queue();
        $queue
            ->push(new DoAnalyze($output, $finder, $input->getArgument('path'), !$input->getOption('without-oop')))
            ->push(new SearchBounds($output, $bounds))
            ->push(new DoAggregatedAnalyze($output, new DirectoryAggregatorFlat($level)))
            ->push(new ReportRenderer($output, new Summary\Cli($validator, $bounds)))
            ->push(new ReportWriter($input->getOption('report-html'), $output, new Summary\Html($validator, $bounds)))
            ->push(new ReportWriter($input->getOption('summary-xml'), $output, new Summary\Xml($validator, $bounds)))
            ;

        // execute
        $collection = new \Hal\Component\Result\ResultCollection();
        $aggregatedResults = new \Hal\Component\Result\ResultCollection();
        $queue->execute($collection, $aggregatedResults);

        $output->writeln('<info>done</info>');

        return 0;
    }

}