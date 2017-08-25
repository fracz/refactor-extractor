<?php

namespace mageekguy\atoum\tests\units\asserters;

use \mageekguy\atoum;
use \mageekguy\atoum\asserters;

require_once(__DIR__ . '/../../runner.php');

/**
@isolation off
*/
class adapter extends atoum\test
{
	public function test__construct()
	{
		$asserter = new asserters\adapter($score = new atoum\score(), $locale = new atoum\locale());

		$this->assert
			->object($asserter->getScore())->isIdenticalTo($score)
			->object($asserter->getLocale())->isIdenticalTo($locale)
			->variable($asserter->getAdapter())->isNull()
		;
	}

	public function testSetWith()
	{
		$currentMethod = substr(__METHOD__, strrpos(__METHOD__, ':') + 1);

		$asserter = new asserters\adapter($score = new atoum\score(), $locale = new atoum\locale());

		$this->assert
			->exception(function() use (& $line, $asserter, & $variable) { $line = __LINE__; $asserter->setWith($variable = uniqid()); })
				->isInstanceOf('\mageekguy\atoum\asserter\exception')
				->hasMessage(sprintf($locale->_('%s is not an adapter'), $asserter->toString($variable)))
			->integer($score->getFailNumber())->isEqualTo(1)
		;

		$this->assert
			->collection($score->getFailAssertions())->isEqualTo(array(
					array(
						'class' => __CLASS__,
						'method' => $currentMethod,
						'file' => __FILE__,
						'line' => $line,
						'asserter' => get_class($asserter) . '::setWith()',
						'fail' => sprintf($locale->_('%s is not an adapter'), $asserter->toString($variable))
					)
				)
			)
			->integer($score->getPassNumber())->isZero()
			->string($asserter->getAdapter())->isEqualTo($variable)
		;

		$this->assert
			->object($asserter->setWith($adapter = new atoum\adapter()))->isIdenticalTo($asserter);
		;

		$this->assert
			->integer($score->getFailNumber())->isEqualTo(1)
			->integer($score->getPassNumber())->isEqualTo(1)
			->object($asserter->getAdapter())->isIdenticalTo($adapter)
		;
	}

	public function testCall()
	{
		$currentMethod = substr(__METHOD__, strrpos(__METHOD__, ':') + 1);

		$asserter = new asserters\adapter($score = new atoum\score(), $locale = new atoum\locale());

		$this->assert
			->integer($score->getPassNumber())->isZero()
			->exception(function() use ($asserter) {
						$asserter->call(uniqid());
					}
				)
					->isInstanceOf('\logicException')
					->hasMessage('Adapter is undefined')
			->integer($score->getPassNumber())->isZero()
		;

		$adapter = new atoum\adapter();

		$asserter
			->setWith($adapter)
			->getScore()
				->reset()
		;

		$this->assert
			->integer($score->getPassNumber())->isZero()
			->integer($score->getFailNumber())->isZero()
			->exception(function() use (& $line, $asserter, & $function) { $line = __LINE__; $asserter->call($function = uniqid()); })
				->isInstanceOf('\mageekguy\atoum\asserter\exception')
				->hasMessage(sprintf($locale->_('Function %s is not called'), $function))
			->integer($score->getPassNumber())->isEqualTo(0)
			->integer($score->getFailNumber())->isEqualTo(1)
			->collection($score->getFailAssertions())->isEqualTo(array(
					array(
						'class' => __CLASS__,
						'method' => substr(__METHOD__, strrpos(__METHOD__, ':') + 1),
						'file' => __FILE__,
						'line' => $line,
						'asserter' => get_class($asserter) . '::call()',
						'fail' => sprintf($locale->_('Function %s is not called'), $function)
					)
				)
			)
		;

		$adapter->{$function = 'md5'} = function() {};

		$adapter->{$function}();

		$this->assert
			->object($asserter->call($function))->isIdenticalTo($asserter)
			->integer($score->getPassNumber())->isEqualTo(1)
			->integer($score->getFailNumber())->isEqualTo(1)
		;

		$score->reset();

		$this->assert
			->integer($score->getPassNumber())->isZero()
			->integer($score->getFailNumber())->isZero()
			->exception(function() use (& $line, $asserter, $function) { $line = __LINE__; $asserter->call($function, array(uniqid())); })
				->isInstanceOf('\mageekguy\atoum\asserter\exception')
				->hasMessage(sprintf($locale->_('Function %s is not called with this argument'), $function))
			->integer($score->getPassNumber())->isEqualTo(0)
			->integer($score->getFailNumber())->isEqualTo(1)
			->collection($score->getFailAssertions())->isEqualTo(array(
					array(
						'class' => __CLASS__,
						'method' => substr(__METHOD__, strrpos(__METHOD__, ':') + 1),
						'file' => __FILE__,
						'line' => $line,
						'asserter' => get_class($asserter) . '::call()',
						'fail' => sprintf($locale->_('Function %s is not called with this argument'), $function)
					)
				)
			)
			->exception(function() use (& $otherLine, $asserter, $function) { $otherLine = __LINE__; $asserter->call($function, array(uniqid(), uniqid())); })
				->isInstanceOf('\mageekguy\atoum\asserter\exception')
				->hasMessage(sprintf($locale->_('Function %s is not called with these arguments'), $function))
			->integer($score->getPassNumber())->isEqualTo(0)
			->integer($score->getFailNumber())->isEqualTo(2)
			->collection($score->getFailAssertions())->isEqualTo(array(
					array(
						'class' => __CLASS__,
						'method' => substr(__METHOD__, strrpos(__METHOD__, ':') + 1),
						'file' => __FILE__,
						'line' => $line,
						'asserter' => get_class($asserter) . '::call()',
						'fail' => sprintf($locale->_('Function %s is not called with this argument'), $function)
					),
					array(
						'class' => __CLASS__,
						'method' => substr(__METHOD__, strrpos(__METHOD__, ':') + 1),
						'file' => __FILE__,
						'line' => $otherLine,
						'asserter' => get_class($asserter) . '::call()',
						'fail' => sprintf($locale->_('Function %s is not called with these arguments'), $function)
					)
				)
			)
		;

		$adapter->{$function}($arg = uniqid());

		$this->assert
			->object($asserter->call($function, array($arg)))->isIdenticalTo($asserter)
			->integer($score->getPassNumber())->isEqualTo(1)
			->integer($score->getFailNumber())->isEqualTo(2)
		;
	}
}

?>