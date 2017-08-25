<?php

namespace Everzet\Behat\Runners;

use Symfony\Components\DependencyInjection\Container;

use \Everzet\Gherkin\Structures\Scenario\Scenario;
use \Everzet\Gherkin\Structures\Scenario\Background;
use \Everzet\Behat\Loggers\Logger;

class ScenarioRunner extends BaseStepsRunner implements Runner
{
    protected $scenario;
    protected $container;
    protected $definitions;
    protected $tokens = array();
    protected $backgroundRunner;
    protected $skip = false;

    public function __construct(Scenario $scenario, Background $background = null,
                                Container $container, Logger $logger)
    {
        $this->scenario     = $scenario;
        $this->container    = $container;
        $this->definitions  = $this->container->getSteps_LoaderService();
        $this->setLogger(     $logger);

        if (null !== $background) {
            $this->backgroundRunner = new BackgroundRunner(
                $background
              , $this->definitions
              , $this->container
              , $this->logger
            );
        }

        $this->initStepRunners(
            $this->scenario->getSteps()
          , $this->definitions
          , $this->container
          , $this->logger
        );
    }

    public function setTokens(array $tokens)
    {
        $this->tokens = $tokens;
    }

    public function getScenario()
    {
        return $this->scenario;
    }

    public function isInOutline()
    {
        return $this->getCaller() instanceof ScenarioOutlineRunner;
    }

    public function getStatus()
    {
        $statuses = array('passed', 'pending', 'undefined', 'failed');
        $code = -1;

        foreach ($this->getStepRunners() as $stepRunner) {
            if (($current = array_search($stepRunner->getStatus(), $statuses)) > $code) {
                $code = $current;
            }
        }

        return $statuses[$code];
    }

    public function getExceptions()
    {
        $exceptions = array();

        foreach ($this->getStepRunners() as $stepRunner) {
            if (null !== $stepRunner->getException()) {
                $exceptions[] = $stepRunner->getException();
            }
        }

        return $exceptions;
    }

    public function run(Runner $caller = null)
    {
        $this->setCaller($caller);
        $this->getLogger()->beforeScenario($this);

        if (null !== $this->backgroundRunner) {
            $this->backgroundRunner->run($this);
        }

        foreach ($this as $runner) {
            if (null !== $this->tokens && count($this->tokens)) {
                $runner->setTokens($this->tokens);
            }
            if (!$this->skip) {
                if ('passed' !== $runner->run($this)) {
                    $this->skip = true;
                }
            } else {
                $runner->skip($this);
            }
        }

        $this->getLogger()->afterScenario($this);
    }
}