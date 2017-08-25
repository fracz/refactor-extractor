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

use Humbug\Adapter\Phpunit\XmlConfiguration\AcceleratorListener;
use Humbug\Adapter\Phpunit\XmlConfiguration\FastestFirstFilter;
use Humbug\Adapter\Phpunit\XmlConfiguration\FilterListener;
use Humbug\Adapter\Phpunit\XmlConfiguration\IncludeOnlyFilter;
use Humbug\Adapter\Phpunit\XmlConfiguration\TimeCollectorListener;
use Humbug\Adapter\Phpunit\XmlConfiguration\Visitor;
use Humbug\Container;
use Humbug\Exception\RuntimeException;
use Humbug\Exception\InvalidArgumentException;

class XmlConfiguration
{
    private static $hasBootstrap;

    /**
     *
     * @var \DOMDocument
     */
    private $dom;

    /**
     * @var \DOMXPath
     */
    private $xpath;

    /**
     * @var \DOMElement
     */
    private $rootElement;

    public function __construct(\DOMDocument $dom)
    {
        if (!$dom->documentElement) {
            throw new \LogicException('No document element present. Document should not be empty!');
        }

        $this->dom = $dom;
        $this->xpath = new \DOMXPath($this->dom);
        $this->rootElement = $this->dom->documentElement;
    }

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

        $configurationDir = self::resolveConfigurationDir($container);

        $configurationFile = (new ConfigurationLocator())->locate($configurationDir);

        if (!empty($configurationDir)) {
            $configurationDir .= '/';
        }

        $dom = (new ConfigurationLoader())->load($configurationFile);

        $xmlConfiguration = new XmlConfiguration($dom);

        if ($xmlConfiguration->hasBootstrap()) {
            self::$hasBootstrap = true;
            $bootstrap = $xmlConfiguration->getBootstrap();
            $path = self::makeAbsolutePath($bootstrap, $configurationDir);

            //@todo Get rid off this side effect...
            $container->setBootstrap($path);
        }

        $xmlConfiguration->setBootstrap(self::getNewBootstrapPath());
        $xmlConfiguration->turnOffCacheTokens();

        $xmlConfiguration->cleanupLoggers();
        $xmlConfiguration->cleanupFilters();
        $xmlConfiguration->cleanupListeners();

        $xmlConfiguration->addListener(new AcceleratorListener());

        /**
         * On first runs collect a test log and also generate code coverage
         */
        if ($firstRun === true) {
            $xmlConfiguration->addLogger('coverage-php', $container->getCacheDirectory() . '/coverage.humbug.php');
            $xmlConfiguration->addLogger('coverage-text', $container->getCacheDirectory() . '/coverage.humbug.txt');

            $srcList = $container->getSourceList();

            $whiteListSrc = isset($srcList->directories) ? self::getRealPathList($srcList->directories) : [];
            $excludeDirs = isset($srcList->excludes) ? self::getRealPathList($srcList->excludes) : [];

            $xmlConfiguration->addWhiteListFilter($whiteListSrc, $excludeDirs);

            $xmlConfiguration->addListener(new TimeCollectorListener(self::getPathToTimeCollectorFile($container)));
        } else {
            $filterListener = new FilterListener([
                new IncludeOnlyFilter($testSuites),
                new FastestFirstFilter(self::getPathToTimeCollectorFile($container))
            ]);
            $xmlConfiguration->addListener($filterListener);
        }

        $xpath = new \DOMXPath($dom);

        $suitesExcludes = $xpath->query('/phpunit/testsuites/testsuite/exclude');
        foreach ($suitesExcludes as $exclude) {
            $exclude->nodeValue = self::makeAbsolutePath($exclude->nodeValue, $configurationDir);
        }
        /**
         * Set any remaining file & directory references to realpaths
         */
        $directories = $xpath->query('//directory');
        foreach ($directories as $directory) {
            $directory->nodeValue = self::makeAbsolutePath($directory->nodeValue, $configurationDir);
        }
        $files = $xpath->query('//file');
        foreach ($files as $file) {
            $file->nodeValue = self::makeAbsolutePath($file->nodeValue, $configurationDir);
        }

        $directoriesFromFirstSuite = $xpath->query('/phpunit/testsuites/testsuite[position()=1]/directory');

        foreach ($directoriesFromFirstSuite as $directory) {
            // phpunit.xml may omit bootstrap location but grab it automatically - include explicitly
            if (self::$hasBootstrap === false) {
                $bootstrapDir = self::makeAbsolutePath($directory->nodeValue, $configurationDir);
                if (file_exists($bootstrapDir . '/bootstrap.php')) {
                    $dom->documentElement->setAttribute('bootstrap', $bootstrapDir . '/bootstrap.php');

                    //@todo Get rid off this side effect
                    $container->setBootstrap($bootstrapDir . '/bootstrap.php');
                    self::$hasBootstrap = true;
                }
            }
        }

        $saveFile = $container->getCacheDirectory() . '/phpunit.humbug.xml';
        $dom->save($saveFile);

        return $saveFile;
    }

    private static function getRealPathList($directories)
    {
        return array_map('realpath', $directories);
    }

    private static function getPathToTimeCollectorFile(Container $container)
    {
        return $container->getCacheDirectory() . '/phpunit.times.humbug.json';
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
     * @param Container $container
     * @return string
     */
    private static function resolveConfigurationDir(Container $container)
    {
        $configurationDir = $container->getTestRunDirectory();

        if (empty($configurationDir)) {
            $configurationDir = $container->getBaseDirectory();
        }

        return $configurationDir;
    }

    /**
     * @return string
     */
    private static function getNewBootstrapPath()
    {
        return sys_get_temp_dir() . '/humbug.phpunit.bootstrap.php';
    }

    public function hasBootstrap()
    {
        return $this->rootElement->hasAttribute('bootstrap');
    }

    public function getBootstrap()
    {
        return $this->rootElement->getAttribute('bootstrap');
    }

    public function setBootstrap($bootstrap)
    {
        return $this->rootElement->setAttribute('bootstrap', $bootstrap);
    }

    public function turnOffCacheTokens()
    {
        return $this->rootElement->setAttribute('cacheTokens', 'false');
    }

    public function cleanupLoggers()
    {
        $this->removeDocumentChildElementsByName('logging');
    }

    public function cleanupFilters()
    {
        $this->removeDocumentChildElementsByName('filter');
    }

    public function cleanupListeners()
    {
        $this->removeDocumentChildElementsByName('listeners');
    }

    private function removeDocumentChildElementsByName($name)
    {
        $nodes = $this->xpath->query('/phpunit/' . $name);

        foreach ($nodes as $node) {
            $this->rootElement->removeChild($node);
        }
    }

    public function addListener(Visitor $visitor)
    {
        $listenersList = $this->xpath->query('/phpunit/listeners');

        if ($listenersList->length) {
            $listeners = $listenersList->item(0);
        } else {
            $listeners = $this->dom->createElement('listeners');
            $this->rootElement->appendChild($listeners);
        }

        $listener = $this->dom->createElement('listener');
        $listeners->appendChild($listener);

        $visitor->visitElement($listener);
    }

    public function addLogger($type, $target)
    {
        $loggingList = $this->xpath->query('/phpunit/logging');

        if ($loggingList->length) {
            $logging = $loggingList->item(0);
        } else {
            $logging = $this->dom->createElement('logging');
            $this->rootElement->appendChild($logging);
        }

        $log = $this->dom->createElement('log');
        $logging->appendChild($log);

        $log->setAttribute('type', $type);
        $log->setAttribute('target', $target);
    }

    public function addWhiteListFilter(array $whiteListDirectories, array $excludeDirectories = [])
    {
        if (empty($whiteListDirectories)) {
            return;
        }

        $filter = $this->dom->createElement('filter');
        $this->rootElement->appendChild($filter);

        $whiteList = $this->dom->createElement('whitelist');
        $filter->appendChild($whiteList);

        foreach ($whiteListDirectories as $dirName) {
            $directory = $this->dom->createElement('directory', $dirName);
            $whiteList->appendChild($directory);
            $directory->setAttribute('suffix', '.php');
        }

        if (empty($excludeDirectories)) {
            return;
        }

        $exclude = $this->dom->createElement('exclude');
        $whiteList->appendChild($exclude);

        foreach ($excludeDirectories as $dirName) {
            $directory = $this->dom->createElement('directory', $dirName);
            $exclude->appendChild($directory);
        }
    }
}