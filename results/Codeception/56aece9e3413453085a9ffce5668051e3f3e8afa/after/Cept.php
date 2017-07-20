<?php

namespace Codeception\TestCase;

use Codeception\Events;
use Codeception\Event\TestEvent;
use Codeception\Step;
use Codeception\TestCase;

class Cept extends TestCase implements Interfaces\ScenarioDriven, Interfaces\Descriptive
{
    use Shared\ScenarioRunner;

    public function __construct(array $data = array(), $dataName = '')
    {
        parent::__construct('testCodecept', $data, $dataName);
    }

    public function getSignature()
    {
        return $this->name;
    }

    public function getFileName()
    {
        return $this->testFile;
    }

    public function preload()
    {
        $this->parser->prepareToRun($this->getRawBody());
        $this->fire(Events::TEST_PARSED, new TestEvent($this));
    }

    public function getRawBody()
    {
        return file_get_contents($this->testFile);
    }

    public function testCodecept()
    {
        $this->fire(Events::TEST_BEFORE, new TestEvent($this));

        $scenario = $this->scenario;
        $scenario->run();
        if ($this->bootstrap) {
            /** @noinspection PhpIncludeInspection */
            require $this->bootstrap;
        }
        /** @noinspection PhpIncludeInspection */
        require $this->testFile;

        $this->fire(Events::TEST_AFTER, new TestEvent($this));
    }
}