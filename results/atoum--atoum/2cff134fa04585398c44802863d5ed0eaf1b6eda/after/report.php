<?php

namespace mageekguy\atoum;

abstract class report implements observers\runner, observers\test
{
	protected $writers = array();
	protected $testFields = array();
	protected $runnerFields = array();

	private $lastSetFields = array();

	public function __construct()
	{
		$this->runnerFields = array(
			runner::runStart => array(),
			runner::runStop => array()
		);

		$this->testFields = array(
			test::runStart => array(),
			test::beforeSetUp => array(),
			test::afterSetUp => array(),
			test::beforeTestMethod => array(),
			test::success => array(),
			test::fail => array(),
			test::error => array(),
			test::exception => array(),
			test::afterTestMethod => array(),
			test::beforeTearDown => array(),
			test::afterTearDown => array(),
			test::runStop => array(),
		);
	}

	public function addRunnerField(report\fields\runner $field, array $events = array())
	{
		return $this->addField($field, $events, 'runnerFields');
	}

	public function addTestField(report\fields\test $field, array $events = array())
	{
		return $this->addField($field, $events, 'testFields');
	}

	public function addWriter(writer $writer)
	{
		$this->writers[] = $writer;

		return $this;
	}

	public function getRunnerFields($event = null)
	{
		$fields = array();

		if ($event === null)
		{
			$fields = $this->runnerFields;
		}
		else
		{
			if (in_array($event, runner::getObserverEvents()) === false)
			{
				throw new exceptions\logic\invalidArgument('\'' . $event . '\' is not a runner event');
			}

			$fields = $this->runnerFields[$event];
		}

		return $fields;
	}

	public function getTestFields($event = null)
	{
		$fields = array();

		if ($event === null)
		{
			$fields = $this->testFields;
		}
		else
		{
			if (in_array($event, test::getObserverEvents()) === false)
			{
				throw new exceptions\logic\invalidArgument('\'' . $event . '\' is not a test event');
			}

			$fields = $this->testFields[$event];
		}

		return $fields;
	}

	public function getWriters()
	{
		return $this->writers;
	}

	public function runnerStart(runner $runner)
	{
		return $this->setRunnerFields($runner, __FUNCTION__);
	}

	public function testRunStart(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function beforeTestSetUp(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function afterTestSetUp(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function beforeTestMethod(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function testAssertionSuccess(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function testAssertionFail(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function testError(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function testException(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function afterTestMethod(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function testRunStop(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function beforeTestTearDown(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function afterTestTearDown(test $test)
	{
		return $this->setTestFields($test, __FUNCTION__);
	}

	public function runnerStop(runner $runner)
	{
		return $this->setRunnerFields($runner, __FUNCTION__);
	}

	public function write()
	{
		foreach ($this->writers as $writer)
		{
			$writer->write((string) $this);
		}

		return $this;
	}

	public function __toString()
	{
		$string = '';

		foreach ($this->lastSetFields as $field)
		{
			$string .= $field;
		}

		return $string;
	}

	protected function addField(report\field $field, array $events, $propertyName)
	{
		if (sizeof($events) <= 0)
		{
			foreach ($this->{$propertyName} as & $fields)
			{
				$fields[] = $field;
			}
		}
		else
		{
			foreach ($events as $event)
			{
				if (isset($this->{$propertyName}[$event]) === false)
				{
					throw new exceptions\runtime('Event \'' . $event . '\' does not exist');
				}

				$this->{$propertyName}[$event][] = $field;
			}
		}

		return $this;
	}

	private function setRunnerFields(runner $runner, $event)
	{
		$this->lastSetFields = array();

		if (isset($this->runnerFields[$event]) === true)
		{
			foreach ($this->runnerFields[$event] as $field)
			{
				$field->setWithRunner($runner, $event);
			}

			$this->lastSetFields = $this->runnerFields[$event];
		}

		return $this;
	}

	private function setTestFields(test $test, $event)
	{
		$this->lastSetFields = array();

		if (isset($this->testFields[$event]) === true)
		{
			foreach ($this->testFields[$event] as $field)
			{
				$field->setWithTest($test, $event);
			}

			$this->lastSetFields = $this->testFields[$event];
		}

		return $this;
	}
}

?>