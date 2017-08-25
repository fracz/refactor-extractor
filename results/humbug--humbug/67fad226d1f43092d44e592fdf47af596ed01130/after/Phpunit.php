<?php
/**
 * Humbug
 *
 * @category   Humbug
 * @package    Humbug
 * @copyright  Copyright (c) 2015 Pádraic Brady (http://blog.astrumfutura.com)
 * @license    https://github.com/padraic/humbug/blob/master/LICENSE New BSD License
 */

namespace Humbug\Adapter;

use Humbug\Container;
use Humbug\Adapter\Phpunit\XmlConfiguration;
use Humbug\Utility\Job;
use Humbug\Utility\Process;
use Humbug\Utility\TestTimeAnalyser;
use Humbug\Utility\CoverageData;
use Humbug\Exception\InvalidArgumentException;
use Humbug\Exception\RuntimeException;
use Symfony\Component\Finder\Finder;
use Symfony\Component\Process\PhpProcess;

class Phpunit extends AdapterAbstract
{

    protected static $optimisedConfigFile;

    /**
     * Runs the tests suite according to Runner set options and the execution
     * order of test case (if any). It then returns an array of two elements.
     * First element is a boolean result value indicating if tests passed or not.
     * Second element is an array containing the key "stdout" which stores the
     * output from the last test run.
     *
     * @param   \Humbug\container $container
     * @param   bool              $useStdout
     * @param   bool              $firstRun
     * @param   array             $mutation
     * @param   array             $testCases
     * @return  array
     */
    public function getProcess(
        Container $container,
        $firstRun = false,
        $interceptFile = null,
        $mutantFile = null,
        array $testCases = [])
    {

        $jobopts = [
            'testdir'       => $container->getTestRunDirectory(),
            'basedir'       => $container->getBaseDirectory(),
            'timeout'       => $container->getTimeout(),
            'cachedir'      => $container->getCacheDirectory(),
            'cliopts'       => $container->getAdapterOptions(),
            'constraints'   => $container->getAdapterConstraints()
        ];

        /**
         * We like standardised easy to parse outout
         */
        array_unshift($jobopts['cliopts'], '--tap');

        /*
         * We only need a single fail!
         */
        if (!in_array('--stop-on-failure', $jobopts['cliopts'])) {
            array_unshift($jobopts['cliopts'], '--stop-on-failure');
        }

        /**
         * Setup a PHPUnit XML config file for the purposes of explicitly setting
         * test case order (this will preserve anything else from the original)
         *
         * On first runs we log to junit XML so we can sort tests by performance.
         *
         * TODO: Assemble config just once if no coverage data available!
         */
        $configFile = null;
        if (count($testCases) > 0) {
            $configFile = XmlConfiguration::assemble($container, $testCases);
        } elseif ($firstRun) {
            $configFile = XmlConfiguration::assemble($container, [], true, true);
            $coverageFile = $container->getCacheDirectory() . '/coverage.humbug.php';
            array_unshift($jobopts['cliopts'], '--coverage-php=' . $coverageFile);
        }
        if (!is_null($configFile)) {
            foreach ($jobopts['cliopts'] as $key => $value) {
                if ($value == '--configuration' || $value == '-C') {
                    unset($jobopts['cliopts'][$key]);
                    unset($jobopts['cliopts'][$key+1]);
                } elseif (preg_match('%\\-\\-configuration=%', $value)) {
                    unset($jobopts['cliopts'][$key]);
                }
            }
            array_unshift($jobopts['cliopts'], '--configuration=' . $configFile);
        }

        /**
         * Initial command is expected, of course.
         */
        array_unshift($jobopts['cliopts'], 'phpunit');

        /**
         * Log the first run so we can analyse test times to make future
         * runs more efficient in terms of deferring slow test classes to last
         */
        $timeout = 0;
        if ($firstRun) {
            $jobopts['cliopts'] = array_merge(
                $jobopts['cliopts'],
                explode(' ', $jobopts['constraints'])
            );
        } else {
            $timeout = $container->getTimeout();
        }

        $job = Job::generate(
            $mutantFile,
            $jobopts,
            $container->getBootstrap(),
            $interceptFile
        );

        $process = new PhpProcess($job, null, $_ENV);
        $process->setTimeout($timeout);

        return $process;
    }

    /**
     * Executed in a separate process spawned from the execute() method above.
     *
     * Uses an instance of PHPUnit_TextUI_Command to execute the PHPUnit
     * tests and simulate any Humbug supported command line options suitable
     * for PHPUnit. At present, we merely dissect a generic 'options' string
     * equivalant to anything typed into a console after a normal 'phpunit'
     * command. The adapter captures the TextUI output for further processing.
     *
     * @param string $arguments PHP serialised set of arguments to pass to PHPUnit
     * @return void
     */
    public static function main($arguments)
    {

        $arguments = unserialize(base64_decode($arguments));

        /**
         * Switch working directory to tests (if required) and execute the test suite
         */
        $originalWorkingDir = getcwd();
        if (isset($arguments['testdir']) && !empty($arguments['testdir'])) {
            chdir($arguments['testdir']);
        }
        $command = new \PHPUnit_TextUI_Command;
        try {
            $command->run($arguments['cliopts'], false);
            if (getcwd() !== $originalWorkingDir) chdir($originalWorkingDir);
        } catch (\Exception $e) {
            if (getcwd() !== $originalWorkingDir) chdir($originalWorkingDir);
            throw $e;
        }
    }

    /**
     * Load coverage data from and return
     *
     * @return \Humbug\Utility\CoverageData
     */
    public function getCoverageData(Container $container, TestTimeAnalyser $analyser)
    {
        $coverage = new CoverageData(
            $container->getCacheDirectory() . '/coverage.humbug.php',
            $analyser
        );
        return $coverage;
    }

    /**
     * Load coverage data from and return
     *
     * @return \Humbug\Utility\TestTimeAnalyser
     */
    public function getLogAnalyser(Container $container)
    {
        $analyser = new TestTimeAnalyser(
            $container->getCacheDirectory() . '/junitlog.humbug.xml'
        );
        return $analyser;
    }

    /**
     * Parse the PHPUnit text result output to see if there were any failures.
     * In the context of mutation testing, a test failure is good (i.e. the
     * mutation was detected by the test suite).
     *
     * This assume the output is in Test Anywhere Protocol (TAP) format.
     *
     * @param string $output
     * @return bool
     */
    public static function ok($output)
    {
        if (preg_match("%[\n\r]+not ok \\d+%", $output)) {
            return false;
        }
        return true;
    }

}