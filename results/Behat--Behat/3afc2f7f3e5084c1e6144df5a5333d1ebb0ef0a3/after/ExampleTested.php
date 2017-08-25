<?php

/*
 * This file is part of the Behat.
 * (c) Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Behat\Behat\EventDispatcher\Event;

use Behat\Gherkin\Node\ExampleNode;
use Behat\Gherkin\Node\FeatureNode;
use Behat\Testwork\Environment\Environment;
use Behat\Testwork\EventDispatcher\Event\LifecycleEvent;
use Behat\Testwork\Tester\Result\TestResult;

/**
 * Behat outline example Tested event.
 *
 * @author Konstantin Kudryashov <ever.zet@gmail.com>
 */
class ExampleTested extends LifecycleEvent implements ScenarioLikeTested
{
    const BEFORE = 'tester.example_tested.before';
    const AFTER = 'tester.example_tested.after';

    /**
     * @var FeatureNode
     */
    private $feature;
    /**
     * @var ExampleNode
     */
    private $example;
    /**
     * @var TestResult
     */
    private $testResult;

    /**
     * Initializes event.
     *
     * @param Environment     $environment
     * @param FeatureNode     $feature
     * @param ExampleNode     $example
     * @param null|TestResult $testResult
     */
    public function __construct(
        Environment $environment,
        FeatureNode $feature,
        ExampleNode $example,
        TestResult $testResult = null
    ) {
        parent::__construct($environment);

        $this->feature = $feature;
        $this->example = $example;
        $this->testResult = $testResult;
    }

    /**
     * Returns feature.
     *
     * @return FeatureNode
     */
    public function getFeature()
    {
        return $this->feature;
    }

    /**
     * Returns example node.
     *
     * @return ExampleNode
     */
    public function getExample()
    {
        return $this->example;
    }

    /**
     * Returns scenario node.
     *
     * @return ExampleNode
     */
    public function getScenario()
    {
        return $this->getExample();
    }

    /**
     * Returns scenario test result (if scenario was tested).
     *
     * @return null|TestResult
     */
    public function getTestResult()
    {
        return $this->testResult;
    }

    /**
     * Returns step tester result status.
     *
     * @return integer
     */
    public function getResultCode()
    {
        if (null === $this->testResult) {
            return null;
        }

        return $this->testResult->getResultCode();
    }

    /**
     * {@inheritdoc}
     */
    public function getNode()
    {
        return $this->getExample();
    }
}