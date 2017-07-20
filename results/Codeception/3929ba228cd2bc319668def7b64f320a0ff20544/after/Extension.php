<?php
namespace Codeception\Platform;

use Codeception\Configuration as Config;
use Codeception\Exception\ModuleRequire;
use Codeception\Subscriber\Shared\StaticEvents;
use Codeception\Lib\Console\Output;
use Symfony\Component\EventDispatcher\EventSubscriberInterface;

class Extension implements EventSubscriberInterface
{
    use StaticEvents;

    protected $config = array();
    protected $options;
    protected $output;
    protected $globalConfig;

    function __construct($config, $options)
    {
        $this->config = array_merge($this->config, $config);
        $this->options = $options;
        $this->output = new Output($options);
        $this->_reconfigure();
        $this->_initialize();
    }

    static $events = array();

    public function _reconfigure($config = array())
    {
        if (is_array($config)) {
            Config::append($config);
        }
    }

    public function _initialize()
    {
    }

    protected function write($message)
    {
        if (!$this->options['silent']) $this->output->write($message);
    }

    protected function writeln($message)
    {
        if (!$this->options['silent']) $this->output->writeln($message);
    }

    public function getModule($name)
    {
        if (!isset(\Codeception\SuiteManager::$modules[$name]))
            throw new ModuleRequire($name, "module is not enabled");
        return \Codeception\SuiteManager::$modules[$name];
    }

    public function getTestsDir()
    {
        return Config::testsDir();
    }

    public function getLogDir()
    {
        return Config::logDir();
    }

    public function getDataDir()
    {
        return Config::dataDir();
    }

    public function getRootDir()
    {
        return Config::projectDir();
    }

    public function getGlobalConfig()
    {
        return Config::config();
    }

}