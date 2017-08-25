<?php

namespace BehaviorTester;

abstract class TestCase extends \PHPUnit_Framework_TestCase
{
    protected $features = array();
    protected $steps = array();

    public function __construct($name = NULL, array $data = array(), $dataName = '')
    {
        parent::__construct($name, $data, $dataName);
        $this->loadFeatures();
        $this->loadSteps();
    }

    abstract protected function getFeaturesPath();

    abstract protected function getStepsPath();

    protected function loadFeatures()
    {
        $this->features = array();

        $iterator = new \RecursiveDirectoryIterator(
            $this->getFeaturesPath(),
            \FilesystemIterator::SKIP_DOTS | \FilesystemIterator::FOLLOW_SYMLINKS
        );
        $iterator = new \RecursiveIteratorIterator(
            $iterator, \RecursiveIteratorIterator::SELF_FIRST
        );
        $parser = new \Gherkin\Parser;

        foreach ($iterator as $file) {
            $this->features[] = $parser->parse(
                file_get_contents($file)
            );
        }
    }

    public function getFeatures()
    {
        return $this->features;
    }

    protected function loadSteps()
    {
        $iterator = new \RecursiveDirectoryIterator(
            $this->getStepsPath(),
            \FilesystemIterator::SKIP_DOTS | \FilesystemIterator::FOLLOW_SYMLINKS
        );
        $iterator = new \RecursiveIteratorIterator(
            $iterator, \RecursiveIteratorIterator::SELF_FIRST
        );

        $this->steps = array();
        $t = $this;
        foreach ($iterator as $file) {
            require $file;
        }
    }

    public function defineStep($type, $definition, $callback)
    {
        $this->steps[$definition] = array('type' => $type, 'callback' => $callback);

        return $this;
    }

    public function __call($command, $arguments)
    {
        return $this->defineStep($command, $arguments[0], $arguments[1]);
    }

    public function featuresProvider()
    {
        $features = array();

        array_walk($this->getFeatures(), function($feature) use(&$features) {
            $features[] = array($feature);
        });

        return $features;
    }

    /**
     * @dataProvider featuresProvider
     */
    public function testFeature(\Gherkin\Feature $feature)
    {
        foreach ($feature->getBackgrounds() as $background) {
            $this->runScenario($background);
        }

        foreach ($feature->getScenarios() as $scenario) {
            $this->runScenario($scenario);
        }
    }

    protected function runScenario(\Gherkin\Background $scenario)
    {
        if ($scenario instanceof \Gherkin\ScenarioOutline) {
            foreach ($scenario->getExamples() as $parameters) {
                foreach ($scenario->getSteps() as $step) {
                    $this->runStep($step, $parameters);
                }
            }
        } else {
            foreach ($scenario->getSteps() as $step) {
                $this->runStep($step);
            }
        }
    }

    protected function runStep(\Gherkin\Step $step, array $parameters = array())
    {
        $description = $step->getText($parameters);

        foreach ($this->steps as $regex => $params) {
            if (preg_match($regex, $description, $values)) {
                call_user_func_array(
                    $params['callback'], array_merge(array_slice($values, 1), $step->getArguments())
                );

                return;
            }
        }

        $this->notImplemented($description);
    }

    protected function notImplemented($action)
    {
        if (strstr($action, ' ')) {
            $this->markTestIncomplete("step: /$action/ not implemented.");
        }

        throw new BadMethodCallException("Method $action not defined.");
    }
}