<?php

namespace mageekguy\atoum;

use mageekguy\atoum;
use mageekguy\atoum\asserter;

abstract class asserter
{
	protected $score = null;
	protected $locale = null;
	protected $generator = null;

	public function __construct(score $score, locale $locale, asserter\generator $generator = null)
	{
		$this->score = $score;
		$this->locale = $locale;

		if ($generator === null)
		{
			$generator = new asserter\generator($this->score, $this->locale);
		}

		$this->generator = $generator;
	}

	public function __call($asserter, $arguments)
	{
		return $this->generator->__call($asserter, $arguments);
	}

	public function getScore()
	{
		return $this->score;
	}

	public function getLocale()
	{
		return $this->locale;
	}

	public function toString($mixed)
	{
		switch (true)
		{
			case is_bool($mixed):
				return sprintf($this->locale->_('boolean(%s)'), ($mixed == false ? $this->locale->_('false') : $this->locale->_('true')));

			case is_integer($mixed):
				return sprintf($this->locale->_('integer(%s)'), $mixed);

			case is_float($mixed):
				return sprintf($this->locale->_('float(%s)'), $mixed);

			case is_null($mixed):
				return 'null';

			case is_object($mixed):
				return sprintf($this->locale->_('object(%s)'), get_class($mixed));

			case is_resource($mixed):
				return sprintf($this->locale->_('resource(%s)'), $mixed);

			case is_string($mixed):
				return sprintf($this->locale->_('string(%s) \'%s\''), strlen($mixed), $mixed);

			case is_array($mixed):
				return sprintf($this->locale->_('array(%s)'), sizeof($mixed));
		}
	}

	public abstract function setWith($variable);

	protected function pass()
	{
		$this->score->addPass();
		return $this;
	}

	protected function fail($reason)
	{
		$asserter = $this;

		$tests = atoum\registry::getInstance()->{atoum\test::getRegistryKey()};

		if (sizeof($tests) <= 0)
		{
			throw new \runtimeException('There is no test currently running');
		}

		$test = array_pop($tests);

		$class = $test->getClass();
		$method = $test->getCurrentMethod();
		$file = $test->getPath();

		$backtrace = current(array_filter(debug_backtrace(), function($value) use ($file, $asserter) {
					static $found = false;

					if ($found === false && isset($value['file']) === true && $value['file'] === $file && isset($value['object']) === true && ($value['object'] === $asserter || is_a($value['object'], __NAMESPACE__ . '\asserter\generator')))
					{
						$found = true;
						return true;
					}

					return false;
				}
			)
		);

		$line = $backtrace['line'];

		throw new asserter\exception($reason, $this->score->addFail($file, $line, $class, $method, get_class($this) . '::' . ($backtrace['function'] !== '__call' ? $backtrace['function'] : $backtrace['args'][0]) . '()', $reason));
	}
}

?>