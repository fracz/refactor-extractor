<?php
/**
 * Humbug
 *
 * @category   Humbug
 * @package    Humbug
 * @copyright  Copyright (c) 2015 Pádraic Brady (http://blog.astrumfutura.com)
 * @license    https://github.com/padraic/humbug/blob/master/LICENSE New BSD License
 */

namespace Humbug\Adapter\Phpunit;

use Humbug\Container;
use Humbug\Exception\RuntimeException;
use Humbug\Exception\InvalidArgumentException;

class XmlConfiguration
{
    private static $dom;

    /**
     * @var \DOMElement
     */
    private static $root;

    private static $listeners;

    private static $xpath;

    private static $hasBootstrap;

    /**
     * Wrangle XML to create a PHPUnit configuration, based on the original, that
     * allows for more control over what tests are run, allows JUnit logging,
     * and ensures that Code Coverage (for Humbug use) whitelists all of the
     * relevant source code.
     *
     *
     * @return string
     */
    public static function assemble(Container $container, $firstRun = false, array $testSuites = [])
    {
        self::$hasBootstrap = false;

        /**
         * Basically a carbon copy of how PHPUnit finds its er...config file?
         */
        $configurationDir = null;
        $testDir = $container->getTestRunDirectory();

        if (!empty($testDir)) {
            $configurationDir = $testDir;
        } else {
            $configurationDir = $container->getBaseDirectory();
        }

        $xmlConfiguration = new XmlConfiguration();

        $conf = $xmlConfiguration->findConfigurationFile($configurationDir);

        if (!empty($configurationDir)) {
            $configurationDir .= '/';
        }

        /**
         * Start the DOMmobile
         */
        $oldValue = libxml_disable_entity_loader(true);
        self::$dom = new \DOMDocument;
        self::$dom->preserveWhiteSpace = false;
        self::$dom->formatOutput = true;
        self::$dom->loadXML(file_get_contents($conf));
        self::$root = self::$dom->documentElement;
        libxml_disable_entity_loader($oldValue);

        self::handleRootAttributes($conf, $container);

        self::$xpath = new \DOMXPath(self::$dom);

        /**
         * On first runs collect a test log and also generate code coverage
         */
        self::handleElementReset();
        if ($firstRun === true) {
            self::handleLogging($container);
            self::handleStartupListeners($container);
        } else {
            self::handleTestSuiteFilterListener($testSuites, $container);
        }

        /** @var \DOMNode[] $nodesToRemove */
        $nodesToRemove = array();
        $suites = self::$xpath->query('/phpunit/testsuites/testsuite');
        foreach ($suites as $suite) {
            // DOMNodeList's Traversable implementation is a bit unpredictable.
            // Iterate over the child nodes using a for loop rather than a
            // foreach so that we can append new children without these being
            // iterated over again.
            $length = $suite->childNodes->length;
            for ($i = 0; $i < $length; $i++) {
                $node = $suite->childNodes->item($i);
                if ($node instanceof \DOMElement
                && ($node->tagName == 'directory'
                || $node->tagName == 'exclude'
                || $node->tagName == 'file')) {
                    $fullPath = $configurationDir . '/' . $node->nodeValue;
                    // Check if the paths exist.
                    $paths = glob($fullPath);
                    if (0 === count($paths)) {
                        // It's no problem if an exclude path is missing.
                        if ($node->tagName !== 'exclude') {
                            throw new RuntimeException('Unable to locate file specified in testsuites: ' . $fullPath);
                        }
                    } else {
                        foreach ($paths as $path) {
                            $clone = $node->cloneNode();
                            $clone->nodeValue = self::makeAbsolutePath($path, getcwd());
                            $node->parentNode->appendChild($clone);
                        }
                    }
                    // Mark the original unprocessed node to be removed.
                    $nodesToRemove[] = $node;
                }
            }
        }

        // Remove the original unprocessed nodes. This cannot be done inside the
        // loop that processes the nodes since the removal of a node causes
        // DOMNodeList to reset its internal array keys.
        foreach ($nodesToRemove as $node) {
            $node->parentNode->removeChild($node);
        }

        self::$xpath = new \DOMXPath(self::$dom);

        /**
         * Set any remaining file & directory references to realpaths
         */
        $directories = self::$xpath->query('//directory');
        foreach ($directories as $directory) {
            $directory->nodeValue = self::makeAbsolutePath($directory->nodeValue, dirname($conf));
        }
        $files = self::$xpath->query('//file');
        foreach ($files as $file) {
            $file->nodeValue = self::makeAbsolutePath($file->nodeValue, dirname($conf));
        }

        $suite1 = self::$xpath->query('/phpunit/testsuites/testsuite')->item(0);
        if (is_a($suite1, 'DOMElement')) {
            self::handleSuite($suite1, $conf, $container);
        }

        $saveFile = $container->getCacheDirectory() . '/phpunit.humbug.xml';
        self::$dom->save($saveFile);

        return $saveFile;
    }

    private static function handleSuite(\DOMElement $suite, $configFile, Container $container)
    {
        foreach ($suite->childNodes as $child) {
            // phpunit.xml may omit bootstrap location but grab it automatically - include explicitly
            if (self::$hasBootstrap === false && $child instanceof \DOMElement && $child->tagName == 'directory') {
                $bootstrapDir = self::makeAbsolutePath($child->nodeValue, dirname($configFile));
                if (file_exists($bootstrapDir . '/bootstrap.php')) {
                    self::$root->setAttribute('bootstrap', $bootstrapDir . '/bootstrap.php');

                    //@todo Get rid off this side effect
                    $container->setBootstrap($bootstrapDir . '/bootstrap.php');
                    self::$hasBootstrap = true;
                }
            }
        }
    }

    private static function handleRootAttributes($configFile, Container $container)
    {
        if (self::$root->hasAttribute('bootstrap')) {
            self::$hasBootstrap = true;
            $bootstrap = self::$root->getAttribute('bootstrap');
            $path = self::makeAbsolutePath($bootstrap, dirname($configFile));

            //@todo Get rid off this side effect...
            $container->setBootstrap($path);
        }
        self::$root->setAttribute('bootstrap', sys_get_temp_dir() . '/humbug.phpunit.bootstrap.php');
        self::$root->setAttribute('cacheTokens', 'false');
    }

    private static function handleElementReset()
    {
        $oldLogs = self::$xpath->query('//logging');
        foreach ($oldLogs as $oldLog) {
            self::$root->removeChild($oldLog);
        }
        $oldFilters = self::$xpath->query('/phpunit/filter');
        foreach ($oldFilters as $filter) {
            self::$root->removeChild($filter);
        }
        $oldListeners = self::$xpath->query('//listeners');
        foreach ($oldListeners as $listeners) {
            self::$root->removeChild($listeners);
        }

        /**
         * Add PHPUnit-Accelerator Listener
         */
        self::$listeners = self::$dom->createElement('listeners');
        self::$root->appendChild(self::$listeners);

        $listener = self::$dom->createElement('listener');
        self::$listeners->appendChild($listener);
        $listener->setAttribute('class', '\MyBuilder\PhpunitAccelerator\TestListener');
        $arguments = self::$dom->createElement('arguments');
        $listener->appendChild($arguments);
        $bool = self::$dom->createElement('boolean');
        $arguments->appendChild($bool);
        $bool->nodeValue = 'true';
    }

    private static function handleLogging(Container $container)
    {
        // add new logs as needed
        $logging = self::$dom->createElement('logging');
        self::$root->appendChild($logging);

        // php coverage
        $log = self::$dom->createElement('log');
        $log->setAttribute('type', 'coverage-php');
        $log->setAttribute(
            'target',
            $container->getCacheDirectory() . '/coverage.humbug.php'
        );
        $logging->appendChild($log);
        $log2 = self::$dom->createElement('log');
        $log2->setAttribute('type', 'coverage-text');
        $log2->setAttribute(
            'target',
            $container->getCacheDirectory() . '/coverage.humbug.txt'
        );
        $logging->appendChild($log2);

        /**
         * While we're here, reset code coverage filter to meet the known source
         * code constraints.
         */
        $filter = self::$dom->createElement('filter');
        $whitelist = self::$dom->createElement('whitelist');
        self::$root->appendChild($filter);
        $filter->appendChild($whitelist);
        $source = $container->getSourceList();
        if (isset($source->directories)) {
            foreach ($source->directories as $d) {
                $directory = self::$dom->createElement('directory', realpath($d));
                $directory->setAttribute('suffix', '.php');
                $whitelist->appendChild($directory);
            }
        }
        if (isset($source->excludes)) {
            $exclude = self::$dom->createElement('exclude');
            foreach ($source->excludes as $d) {
                $directory = self::$dom->createElement('directory', realpath($d));
                $exclude->appendChild($directory);
            }
            $whitelist->appendChild($exclude);
        }
    }

    private static function handleStartupListeners(Container $container)
    {
        $listener = self::$dom->createElement('listener');
        self::$listeners->appendChild($listener);
        $listener->setAttribute('class', '\Humbug\Phpunit\Listener\TimeCollectorListener');
        $arguments = self::$dom->createElement('arguments');
        $listener->appendChild($arguments);
        $jsonLogger = self::$dom->createElement('object');
        $arguments->appendChild($jsonLogger);
        $jsonLogger->setAttribute('class', '\Humbug\Phpunit\Logger\JsonLogger');
        $jsonLoggerArgs = self::$dom->createElement('arguments');
        $jsonLogger->appendChild($jsonLoggerArgs);
        $string = self::$dom->createElement('string');
        $jsonLoggerArgs->appendChild($string);
        $string->nodeValue = $container->getCacheDirectory() . '/phpunit.times.humbug.json';
    }

    private static function handleTestSuiteFilterListener(array $testSuites, Container $container)
    {
        $listener = self::$dom->createElement('listener');
        self::$listeners->appendChild($listener);
        $listener->setAttribute('class', '\Humbug\Phpunit\Listener\FilterListener');
        $arguments = self::$dom->createElement('arguments');
        $listener->appendChild($arguments);

        /**
         * Add the IncludeOnly Filter
         */
        $includeOnly = self::$dom->createElement('object');
        $arguments->appendChild($includeOnly);
        $includeOnly->setAttribute('class', '\Humbug\Phpunit\Filter\TestSuite\IncludeOnlyFilter');
        $includeOnlyArgs = self::$dom->createElement('arguments');
        $includeOnly->appendChild($includeOnlyArgs);
        foreach ($testSuites as $testSuite) {
            $string = self::$dom->createElement('string');
            $includeOnlyArgs->appendChild($string);
            $string->nodeValue = $testSuite;
        }

        /**
         * Add the FastestFirst Filter
         */
        $fastestFirst = self::$dom->createElement('object');
        $arguments->appendChild($fastestFirst);
        $fastestFirst->setAttribute('class', '\Humbug\Phpunit\Filter\TestSuite\FastestFirstFilter');
        $fastestFirstArgs = self::$dom->createElement('arguments');
        $fastestFirst->appendChild($fastestFirstArgs);
        $string = self::$dom->createElement('string');
        $fastestFirstArgs->appendChild($string);
        $string->nodeValue = $container->getCacheDirectory() . '/phpunit.times.humbug.json';
    }

    private static function makeAbsolutePath($name, $workingDir)
    {
        // @see https://github.com/symfony/Config/blob/master/FileLocator.php#L83
        if ('/' === $name[0]
            || '\\' === $name[0]
            || (strlen($name) > 3 && ctype_alpha($name[0]) && $name[1] == ':' && ($name[2] == '\\' || $name[2] == '/'))
        ) {
            if (!file_exists($name)) {
                throw new InvalidArgumentException("$name does not exist");
            }

            return realpath($name);
        }

        $relativePath = $workingDir.DIRECTORY_SEPARATOR.$name;
        $glob = glob($relativePath);
        if (file_exists($relativePath) || !empty($glob)) {
            return realpath($relativePath);
        }

        throw new InvalidArgumentException("Could not find file $name working from $workingDir");
    }

    /**
     * @param $configurationDir
     */
    public function findConfigurationFile($configurationDir)
    {
        $conf = $configurationDir . '/phpunit.xml';

        if (file_exists($conf)) {
            return realpath($conf);
        } elseif (file_exists($conf . '.dist')) {
            return realpath($conf . '.dist');
        } else {
            throw new RuntimeException('Unable to locate phpunit.xml(.dist) file. This is required by Humbug.');
        }
    }
}