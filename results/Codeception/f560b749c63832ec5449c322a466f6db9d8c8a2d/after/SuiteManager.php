<?php

namespace Codeception;

use Codeception\Event\Suite;
use Codeception\Event\SuiteEvent;
use Codeception\Lib\Di;
use Codeception\Lib\GroupManager;
use Codeception\Lib\ModuleContainer;
use Symfony\Component\EventDispatcher\EventDispatcher;

class SuiteManager
{

    public static $modules = array();
    public static $actions = array();
    public static $environment;
    public static $name;

    /**
     * @var \PHPUnit_Framework_TestSuite
     */
    protected $suite = null;

    /**
     * @var null|\Symfony\Component\EventDispatcher\EventDispatcher
     */
    protected $dispatcher = null;

    /**
     * @var GroupManager
     */
    protected $groupManager;

    /**
     * @var TestLoader
     */
    protected $testLoader;

    /**
     * @var ModuleContainer
     */
    protected $moduleContainer;

    /**
     * @var Di
     */
    protected $di;

    protected $tests = array();
    protected $debug = false;
    protected $path = '';
    protected $printer = null;
    protected $env = null;

    public function __construct(EventDispatcher $dispatcher, $name, array $settings)
    {
        $this->settings = $settings;
        $this->dispatcher = $dispatcher;
        $this->di = new Di();
        $this->path = $settings['path'];
        $this->groupManager = new GroupManager($settings['groups']);
        $this->moduleContainer = new ModuleContainer($this->di, $settings);

        $modules = Configuration::modules($this->settings);
        foreach ($modules as $moduleName) {
            $this->moduleContainer->create($moduleName);
        }
        $this->suite = $this->createSuite($name);
        if (isset($settings['current_environment'])) {
            $this->env = $settings['current_environment'];
        }
    }

    public function initialize()
    {
        foreach ($this->moduleContainer->all() as $module) {
            $module->_initialize();
        }
        $this->dispatcher->dispatch(Events::SUITE_INIT, new SuiteEvent($this->suite, null, $this->settings));
        $this->initializeActors();
        ini_set('xdebug.show_exception_trace', 0); // Issue https://github.com/symfony/symfony/issues/7646
    }

    protected function initializeActors()
    {
        if (!file_exists(Configuration::supportDir() . $this->settings['class_name'] . '.php')) {
            throw new Exception\Configuration($this->settings['class_name'] . " class doesn't exists in suite folder.\nRun the 'build' command to generate it");
        }
    }

    public function loadTests($path = null)
    {
        $testLoader = new TestLoader($this->settings['path']);
        $path
            ? $testLoader->loadTest($path)
            : $testLoader->loadTests();

        $tests = $testLoader->getTests();
        foreach ($tests as $test) {
            $this->addToSuite($test);
        }
    }

    protected function addToSuite($test)
    {
        if ($test instanceof TestCase\Interfaces\Configurable) {
            $test->configDispatcher($this->dispatcher);
            $test->configActor($this->getActor());
            $test->configEnv($this->env);
            $test->configDi($this->di);
            $test->configModules($this->moduleContainer);
        }

        if ($test instanceof \PHPUnit_Framework_TestSuite_DataProvider) {
            foreach ($test->tests() as $t) {
                if (!$t instanceof TestCase\Interfaces\Configurable) {
                    continue;
                }
                $t->configDispatcher($this->dispatcher);
                $t->configActor($this->getActor());
                $t->configEnv($this->env);
                $t->configDi($this->di);
                $t->configModules($this->moduleContainer);
            }
        }

        if ($test instanceof TestCase\Interfaces\ScenarioDriven) {
            if (!$this->isCurrentEnvironment($test->getScenario()->getEnv())) {
                return;
            }
            $test->preload();
        }

        $groups = $this->groupManager->groupsForTest($test);
        $this->suite->addTest($test, $groups);
    }

    protected function createSuite($name)
    {
        $suite = new Lib\Suite();
        $suite->setBaseName($this->env ? substr($name, 0, strpos($name, '-' . $this->env)) : $name);
        if ($this->settings['namespace']) {
            $name = $this->settings['namespace'] . ".$name";
        }
        $suite->setName($name);
        $suite->setModules($this->moduleContainer->all());
        return $suite;
    }


    public function run(PHPUnit\Runner $runner, \PHPUnit_Framework_TestResult $result, $options)
    {
        $this->dispatcher->dispatch(Events::SUITE_BEFORE, new Event\SuiteEvent($this->suite, $result, $this->settings));
        $runner->doEnhancedRun($this->suite, $result, $options);
        $this->dispatcher->dispatch(Events::SUITE_AFTER, new Event\SuiteEvent($this->suite, $result, $this->settings));
    }


    /**
     * @return null|\PHPUnit_Framework_TestSuite
     */
    public function getSuite()
    {
        return $this->suite;
    }

    protected function isCurrentEnvironment($envs)
    {
        if (empty($envs)) {
            return true;
        }
        return $this->env and in_array($this->env, $envs);
    }

    protected function getActor()
    {
        return $this->settings['namespace']
            ? $this->settings['namespace'] . '\\' . $this->settings['class_name']
            : $this->settings['class_name'];
    }
}