<?php

namespace Codeception;

use Symfony\Component\EventDispatcher\EventDispatcher;

abstract class TestCase extends \PHPUnit_Framework_TestCase implements \PHPUnit_Framework_SelfDescribing
{

    protected $testfile = null;
    protected $output;
    protected $debug;
    protected $features = array();
    protected $scenario;
    protected $bootstrap = null;
    protected $stopped = false;
    protected $trace = array();

    protected $dispatcher;


    public function __construct(EventDispatcher $dispatcher, array $data = array(), $dataName = '')
    {
        parent::__construct('testCodecept', $data, $dataName);
        $this->dispatcher = $dispatcher;

        if (!isset($data['file'])) throw new \Exception('File with test scenario not set. Use array(file => filepath) to set a scenario');

        $this->name = $data['name'];
        $this->scenario = new \Codeception\Scenario($this);
        $this->testfile = $data['file'];
        $this->bootstrap = isset($data['bootstrap']) ? $data['bootstrap'] : null;
    }

    public function setDispatcher(EventDispatcher $dispatcher) {
        $this->dispatcher = $dispatcher;
    }


    public function getFileName()
    {
        return $this->name;
    }

    public function getSpecName() {
        return $this->name;
    }

    /**
     * @return \Codeception\Scenario
     */
    public function getScenario()
    {
        return $this->scenario;
    }

    public function getScenarioText()
    {
        $text = implode("\r\n", $this->scenario->getSteps());
        $text = str_replace(array('((', '))'), array('...', ''), $text);
        return $text = strtoupper('I want to ' . $this->scenario->getFeature()) . "\n\n" . $text;
    }

    public function __call($command, $args)
    {
        if (strrpos('Test', $command) !== 0) return;
        $this->testCodecept();
    }

    public function setUp() {
        if (file_exists($this->bootstrap)) require $this->bootstrap;
        $this->dispatcher->dispatch('test.before', new \Codeception\Event\Test($this));
        $this->loadScenario();
    }

    abstract public function loadScenario();

    /**
     * @test
     */
    public function testCodecept()
    {
        $this->output->writeln("Trying to [[{$this->scenario->getFeature()}]] (" . basename($this->testfile) . ") ");
        if ($this->debug && count($this->scenario->getSteps())) $this->output->writeln("Scenario:\n");

        try {
            $this->scenario->run();
        } catch (\PHPUnit_Framework_ExpectationFailedException $fail) {
            foreach (\Codeception\SuiteManager::$modules as $module) {
                $module->_failed($this, $fail);
            }
            $this->logger->alert("*** last command failed ***");
            $this->logger->alert($fail->getMessage());
            throw $fail;
        }
    }

    public function tearDown() {
        $this->dispatcher->dispatch('test.after', new \Codeception\Event\Test($this));
    }



    public function runStep(\Codeception\Step $step)
    {
        $this->logger->info($step);
        if ($this->debug) $this->output->put("\n* " . $step->__toString());
        if ($step->getName() == 'Comment') return;

        $this->trace[] = $step;
        $action = $step->getAction();
        $arguments = array_merge($step->getArguments());
        if (!isset(\Codeception\SuiteManager::$methods[$action])) {
            $this->logger->crit("Action $action not defined");
            $this->stopped = true;
            $this->fail("Action $action not defined");
            return;
        }

        $this->dispatcher->dispatch('step.before', new \Codeception\Event\Step($this, $step));

        $activeModule = \Codeception\SuiteManager::$modules[\Codeception\SuiteManager::$methods[$action]];

        try {
            if (is_callable(array($activeModule, $action))) {
                call_user_func_array(array($activeModule, $action), $arguments);

            } else {
                throw new \RuntimeException("Action can't be called");
            }
        } catch (\PHPUnit_Framework_ExpectationFailedException $fail) {
            $this->logger->alert($fail->getMessage());
            if ($activeModule->_getDebugOutput() && $this->debug) $this->output->debug($activeModule->_getDebugOutput());
            throw $fail;
        }

        $this->dispatcher->dispatch('step.before', new \Codeception\Event\Step($this, $step));

        $output = $activeModule->_getDebugOutput();
        if ($output) {
            if ($this->debug) $this->output->debug($output);
        }
    }

    public function toString()
    {
        return $this->scenario->getFeature() . ' (' . $this->getFileName() . ')';
    }

    public function getTrace()
    {
        return $this->trace;
    }


}