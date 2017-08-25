<?php

namespace mageekguy\atoum\tests\units\test\adapter;

require __DIR__ . '/../../../runner.php';

use
	mageekguy\atoum,
	mageekguy\atoum\test\adapter,
	mageekguy\atoum\test\adapter\calls as testedClass
;

class calls extends atoum\test
{
	public function testClass()
	{
		$this->testedClass
			->implements('countable')
			->implements('arrayAccess')
			->implements('iteratorAggregate')
		;
	}

	public function test__construct()
	{
		$this
			->if($calls = new testedClass())
			->then
				->sizeof($calls)->isZero()
		;
	}

	public function test__invoke()
	{
		$this
			->if($calls = new testedClass())
			->then
				->array($calls())->isEmpty()
			->if($calls[] = $call = new adapter\call(uniqid()))
			->then
				->array($calls())->isEqualTo(array($call->getFunction() => array(1 => $call)))
					->object[$call->getFunction()][1]->isIdenticalTo($call)
			->if($calls[] = $otherCall = new adapter\call($call->getFunction()))
			->then
				->array($calls())->isEqualTo(array($call->getFunction() => array(1 => $call, 2 => $otherCall)))
					->object[$call->getFunction()][1]->isIdenticalTo($call)
					->object[$call->getFunction()][2]->isIdenticalTo($otherCall)
					->object[$otherCall->getFunction()][2]->isIdenticalTo($otherCall)
			->if($calls[] = $anotherCall = new adapter\call(uniqid()))
			->then
				->array($calls())->isEqualTo(array(
							$call->getFunction() => array(1 => $call, 2 => $otherCall),
							$anotherCall->getFunction() => array(3 => $anotherCall)
						)
				)
					->object[$call->getFunction()][1]->isIdenticalTo($call)
					->object[$call->getFunction()][2]->isIdenticalTo($otherCall)
					->object[$otherCall->getFunction()][2]->isIdenticalTo($otherCall)
					->object[$anotherCall->getFunction()][3]->isIdenticalTo($anotherCall)
		;
	}

	public function test__toString()
	{
		$this
			->if($calls = new testedClass())
			->then
				->castToString($calls)->isEqualTo($calls->getDecorator()->decorate($calls))
		;
	}

	public function testCount()
	{
		$this
			->if($calls = new testedClass())
			->then
				->sizeof($calls)->isZero()
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->sizeof($calls)->isEqualTo(1)
			->if($otherCalls = new testedClass())
			->and($otherCalls[] = $call2 = new adapter\call(uniqid()))
			->then
				->sizeof($calls)->isEqualTo(1)
				->sizeof($otherCalls)->isEqualTo(1)
			->if($calls[] = $call2 = new adapter\call(uniqid()))
			->then
				->sizeof($calls)->isEqualTo(2)
				->sizeof($otherCalls)->isEqualTo(1)
			->if($calls[] = $call3 = new adapter\call($call1->getFunction()))
			->then
				->sizeof($calls)->isEqualTo(3)
				->sizeof($otherCalls)->isEqualTo(1)
		;
	}

	public function testSetDecorator()
	{
		$this
			->if($calls = new testedClass())
			->then
				->object($calls->setDecorator($decorator = new adapter\calls\decorator()))->isIdenticalTo($calls)
				->object($calls->getDecorator())->isIdenticalTo($decorator)
				->object($calls->setDecorator())->isIdenticalTo($calls)
				->object($calls->getDecorator())
					->isNotIdenticalTo($decorator)
					->isEqualTo(new adapter\calls\decorator())
		;
	}

	public function testReset()
	{
		$this
			->if($calls = new testedClass())
			->then
				->object($calls->reset())->isIdenticalTo($calls)
				->sizeof($calls)->isZero()
			->if($calls[] = new adapter\call(uniqid()))
			->then
				->object($calls->reset())->isIdenticalTo($calls)
				->sizeof($calls)->isZero()
			->if($calls[] = $call = new adapter\call(uniqid()))
			->then
				->array($calls[$call->getFunction()]->toArray())->isEqualTo(array(2 => $call))
		;
	}

	public function testAddCall()
	{
		$this
			->if($calls = new testedClass())
			->then
				->object($calls->addCall($call = new adapter\call(uniqid())))->isIdenticalTo($calls)
				->array($calls[$call->getFunction()]->toArray())
					->isEqualTo(array(1 => $call))
						->object[1]->isIdenticalTo($call)
		;
	}

	public function testOffsetSet()
	{
		$this
			->if($calls = new testedClass())
			->then
				->exception(function() use ($calls) { $calls[] = new adapter\call(); })
					->isInstanceOf('mageekguy\atoum\exceptions\logic\invalidArgument')
					->hasMessage('Function is undefined')
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->array($calls[$call1]->toArray())
					->isEqualTo(array(1 => $call1))
						->object[1]->isIdenticalTo($call1)
			->if($calls[] = $call2 = new adapter\call(uniqid(), array()))
			->then
				->array($calls[$call1]->toArray())
					->isEqualTo(array(1 => $call1))
						->object[1]->isIdenticalTo($call1)
				->array($calls[$call2]->toArray())
					->isEqualTo(array(2 => $call2))
						->object[2]->isIdenticalTo($call2)
			->if($calls[] = $call3 = new adapter\call($call1->getFunction(), array()))
			->then
				->array($calls[$call1]->toArray())
					->isEqualTo(array(1 => $call1, 3 => $call3))
						->object[1]->isIdenticalTo($call1)
						->object[3]->isIdenticalTo($call3)
				->array($calls[$call2]->toArray())
					->isEqualTo(array(2 => $call2))
						->object[2]->isIdenticalTo($call2)
			->if($calls[] = $call4 = new adapter\call(uniqid()))
			->then
				->array($calls[$call1]->toArray())
					->isEqualTo(array(1 => $call1, 3 => $call3))
						->object[1]->isIdenticalTo($call1)
						->object[3]->isIdenticalTo($call3)
				->array($calls[$call2]->toArray())
					->isEqualTo(array(2 => $call2))
						->object[2]->isIdenticalTo($call2)
				->array($calls[$call4->getFunction()]->toArray())
					->isEqualTo(array(4 => $call4))
						->object[4]->isIdenticalTo($call4)
			->if($calls[$newFunction = uniqid()] = $call5 = new adapter\call(uniqid()))
			->then
				->array($calls[$newFunction]->toArray())->isEqualTo(array(5 => $call5))
					->object[5]->isIdenticalTo($call5)
				->string($call5->getFunction())->isEqualTo($newFunction)
		;
	}

	public function testOffsetGet()
	{
		$this
			->if($calls = new testedClass())
			->then
				->array($calls[uniqid()]->toArray())->isEmpty()
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->array($calls[$call1->getFunction()]->toArray())
					->isEqualTo(array(1 => $call1))
						->object[1]->isIdenticalTo($call1)
				->array($calls[$call1]->toArray())
					->isEqualTo(array(1 => $call1))
						->object[1]->isIdenticalTo($call1)
			->if($calls[] = $call2 = new adapter\call($call1->getFunction(), array()))
			->then
				->array($calls[uniqid()]->toArray())->isEmpty()
				->array($calls[$call1->getFunction()]->toArray())
					->isEqualTo(array(1 => $call1, 2 => $call2))
						->object[1]->isIdenticalTo($call1)
						->object[2]->isIdenticalTo($call2)
				->array($calls[$call1]->toArray())
					->isEqualTo(array(1 => $call1, 2 => $call2))
						->object[1]->isIdenticalTo($call1)
						->object[2]->isIdenticalTo($call2)
				->array($calls[$call2->getFunction()]->toArray())
					->isEqualTo(array(1 => $call1, 2 => $call2))
						->object[1]->isIdenticalTo($call1)
						->object[2]->isIdenticalTo($call2)
				->array($calls[$call2]->toArray())
					->isEqualTo(array(2 => $call2))
						->object[2]->isIdenticalTo($call2)
		;
	}

	public function testOffsetExists()
	{
		$this
			->if($calls = new testedClass())
			->then
				->boolean(isset($calls[uniqid()]))->isFalse()
			->if($calls[] = $call = new adapter\call(uniqid()))
			->then
				->boolean(isset($calls[uniqid()]))->isFalse()
				->boolean(isset($calls[$call->getFunction()]))->isTrue()
				->boolean(isset($calls[$call]))->isTrue()
		;
	}

	public function testOffsetUnset()
	{
		$this
			->if($calls = new testedClass())
			->when(function() use ($calls) { unset($calls[uniqid()]); })
			->then
				->sizeof($calls)->isZero()
			->if($calls[] = $call = new adapter\call(uniqid()))
			->when(function() use ($calls) { unset($calls[uniqid()]); })
			->then
				->boolean(isset($calls[$call->getFunction()]))->isTrue()
			->when(function() use ($calls, $call) { unset($calls[$call->getFunction()]); })
			->then
				->boolean(isset($calls[$call->getFunction()]))->isFalse()
		;
	}

	public function testGetIterator()
	{
		$this
			->if($calls = new testedClass())
			->then
				->object($calls->getIterator())->isEqualTo(new \arrayIterator($calls()))
		;
	}

	public function testToArray()
	{
		$this
			->if($calls = new testedClass())
			->then
				->array($calls->toArray())->isEmpty()
				->array($calls->toArray(new adapter\call(uniqid())))->isEmpty()
			->if($calls->addCall($call1 = new adapter\call(uniqid())))
			->then
				->array($calls->toArray())->isEqualTo(array(1 => $call1))
				->array($calls->toArray(new adapter\call(uniqid())))->isEmpty()
				->array($calls->toArray($call1))->isEqualTo(array(1 => $call1))
			->if($calls->addCall($call2 = clone $call1))
			->then
				->array($calls->toArray())->isEqualTo(array(1 => $call1, 2 => $call2))
				->array($calls->toArray(new adapter\call(uniqid())))->isEmpty()
				->array($calls->toArray($call1))->isEqualTo(array(1 => $call1, 2 => $call2))
				->array($calls->toArray($call2))->isEqualTo(array(1 => $call1, 2 => $call2))
			->if($calls->addCall($call3 = new adapter\call(uniqid())))
			->then
				->array($calls->toArray())->isEqualTo(array(1 => $call1, 2 => $call2, 3 => $call3))
				->array($calls->toArray(new adapter\call(uniqid())))->isEmpty()
				->array($calls->toArray($call1))->isEqualTo(array(1 => $call1, 2 => $call2))
				->array($calls->toArray($call2))->isEqualTo(array(1 => $call1, 2 => $call2))
				->array($calls->toArray($call3))->isEqualTo(array(3 => $call3))
		;
	}

	public function testGetEqualTo()
	{
		$this
			->if($calls = new testedClass())
			->then
				->object($calls->getEqualTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->object($calls->getEqualTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getEqualTo($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getEqualTo($call1)->toArray())
						->isEqualTo(array(1 => $call1))
			->if($calls[] = $call2 = new adapter\call($call1->getFunction(), array()))
			->then
				->object($calls->getEqualTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getEqualTo($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(2)
					->array($calls->getEqualTo($call1)->toArray())
						->isEqualTo(array(1 => $call1, 2 => $call2))
				->object($calls->getEqualTo($call2))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getEqualTo($call2)->toArray())
						->isEqualTo(array(2 => $call2))
			->if($calls[] = $call3 = new adapter\call($call1->getFunction(), array($object = new \mock\object())))
			->then
				->object($calls->getEqualTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getEqualTo($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(3)
					->array($calls->getEqualTo($call1)->toArray())
						->isEqualTo(array(1 => $call1, 2 => $call2, 3 => $call3))
				->object($calls->getEqualTo($call2))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getEqualTo($call2)->toArray())
						->isEqualTo(array(2 => $call2))
				->object($calls->getEqualTo($call3))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getEqualTo($call3)->toArray())
						->isEqualTo(array(3 => $call3))
			->if($calls[] = $call4 = new adapter\call($call1->getFunction(), array($object = new \mock\object(), $arg = uniqid())))
			->then
				->object($calls->getEqualTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getEqualTo($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(4)
					->array($calls->getEqualTo($call1)->toArray())
						->isEqualTo(array(1 => $call1, 2 => $call2, 3 => $call3, 4 => $call4))
				->object($calls->getEqualTo($call2))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getEqualTo($call2)->toArray())
						->isEqualTo(array(2 => $call2))
				->object($calls->getEqualTo($call3))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(2)
					->array($calls->getEqualTo($call3)->toArray())
						->isEqualTo(array(3 => $call3, 4 => $call4))
				->object($calls->getEqualTo(new adapter\call($call1->getFunction(), array(clone $object))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(2)
					->array($calls->getEqualTo($call3)->toArray())
						->isEqualTo(array(3 => $call3, 4 => $call4))
				->object($calls->getEqualTo($call4))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getEqualTo($call4)->toArray())
						->isEqualTo(array(4 => $call4))
				->object($calls->getEqualTo(new adapter\call($call1->getFunction(), array(clone $object, $arg))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getEqualTo($call4)->toArray())
						->isEqualTo(array(4 => $call4))
			->if($calls = new testedClass())
			->and($calls[] = $call5 = new adapter\call(uniqid(), array(1, 2, 3, 4, 5)))
			->then
				->object($calls->getEqualTo(new adapter\call($call5->getFunction())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getEqualTo(new adapter\call($call5->getFunction()))->toArray())
						->isEqualTo(array(5 => $call5))
		;
	}

	public function testGetIdenticalTo()
	{
		$this
			->if($calls = new testedClass())
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call1)->toArray())
						->isEqualTo(array(1 => $call1))
			->if($calls[] = $call2 = new adapter\call($call1->getFunction(), array()))
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(2)
					->array($calls->getIdenticalTo($call1)->toArray())
						->isEqualTo(array(1 => $call1, 2 => $call2))
				->object($calls->getIdenticalTo($call2))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call2)->toArray())
						->isEqualTo(array(2 => $call2))
			->if($calls[] = $call3 = new adapter\call($call1->getFunction(), array($object = new \mock\object())))
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(3)
					->array($calls->getIdenticalTo($call1)->toArray())
						->isEqualTo(array(1 => $call1, 2 => $call2, 3 => $call3))
				->object($calls->getIdenticalTo($call2))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call2)->toArray())
						->isEqualTo(array(2 => $call2))
				->object($calls->getIdenticalTo($call3))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call3)->toArray())
						->isEqualTo(array(3 => $call3))
				->object($calls->getIdenticalTo(new adapter\call($call1->getFunction(), array(clone $object))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
			->if($calls[] = $call4 = new adapter\call($call1->getFunction(), array($object = new \mock\object(), $arg = uniqid())))
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(4)
					->array($calls->getIdenticalTo($call1)->toArray())
						->isEqualTo(array(1 => $call1, 2 => $call2, 3 => $call3, 4 => $call4))
				->object($calls->getIdenticalTo($call2))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call2)->toArray())
						->isEqualTo(array(2 => $call2))
				->object($calls->getIdenticalTo($call3))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call3)->toArray())
						->isEqualTo(array(3 => $call3))
				->object($calls->getIdenticalTo(new adapter\call($call1->getFunction(), array(clone $object))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call4))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call4)->toArray())
						->isEqualTo(array(4 => $call4))
				->object($calls->getIdenticalTo(new adapter\call($call1->getFunction(), array(clone $object, $arg))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
		;
	}

	public function testGet()
	{
		$this
			->if($calls = new testedClass())
			->then
				->object($calls->get(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->object($calls->get(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->get($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->get($call1)->toArray())
						->isEqualTo(array(1 => $call1))
			->if($calls[] = $call2 = new adapter\call($call1->getFunction(), array()))
			->then
				->object($calls->get(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->get($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(2)
					->array($calls->get($call1)->toArray())
						->isEqualTo(array(1 => $call1, 2 => $call2))
				->object($calls->get($call2))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->get($call2)->toArray())
						->isEqualTo(array(2 => $call2))
			->if($calls[] = $call3 = new adapter\call($call1->getFunction(), array($object = new \mock\object())))
			->then
				->object($calls->get(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->get($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(3)
					->array($calls->get($call1)->toArray())
						->isEqualTo(array(1 => $call1, 2 => $call2, 3 => $call3))
				->object($calls->get($call2))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->get($call2)->toArray())
						->isEqualTo(array(2 => $call2))
				->object($calls->get($call3))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->get($call3)->toArray())
						->isEqualTo(array(3 => $call3))
			->if($calls[] = $call4 = new adapter\call($call1->getFunction(), array($object = new \mock\object(), $arg = uniqid())))
			->then
				->object($calls->get(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->get($call1))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(4)
					->array($calls->get($call1)->toArray())
						->isEqualTo(array(1 => $call1, 2 => $call2, 3 => $call3, 4 => $call4))
				->object($calls->get($call2))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->get($call2)->toArray())
						->isEqualTo(array(2 => $call2))
				->object($calls->get($call3))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(2)
					->array($calls->get($call3)->toArray())
						->isEqualTo(array(3 => $call3, 4 => $call4))
				->object($calls->get(new adapter\call($call1->getFunction(), array(clone $object))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(2)
					->array($calls->get($call3)->toArray())
						->isEqualTo(array(3 => $call3, 4 => $call4))
				->object($calls->get($call4))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->get($call4)->toArray())
						->isEqualTo(array(4 => $call4))
				->object($calls->get(new adapter\call($call1->getFunction(), array(clone $object, $arg))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->get($call4)->toArray())
						->isEqualTo(array(4 => $call4))

			->if($calls = new testedClass())
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
			->if($calls[] = $call5 = new adapter\call(uniqid()))
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call5))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call5)->toArray())
						->isEqualTo(array(5 => $call5))
			->if($calls[] = $call6 = new adapter\call($call5->getFunction(), array()))
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call5))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(2)
					->array($calls->getIdenticalTo($call5)->toArray())
						->isEqualTo(array(5 => $call5, 6 => $call6))
				->object($calls->getIdenticalTo($call6))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call6)->toArray())
						->isEqualTo(array(6 => $call6))
			->if($calls[] = $call7 = new adapter\call($call5->getFunction(), array($object = new \mock\object())))
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call5))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(3)
					->array($calls->getIdenticalTo($call5)->toArray())
						->isEqualTo(array(5 => $call5, 6 => $call6, 7 => $call7))
				->object($calls->getIdenticalTo($call6))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call6)->toArray())
						->isEqualTo(array(6 => $call6))
				->object($calls->getIdenticalTo($call7))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call7)->toArray())
						->isEqualTo(array(7 => $call7))
				->object($calls->getIdenticalTo(new adapter\call($call5->getFunction(), array(clone $object))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
			->if($calls[] = $call8 = new adapter\call($call5->getFunction(), array($object = new \mock\object(), $arg = uniqid())))
			->then
				->object($calls->getIdenticalTo(new adapter\call(uniqid())))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call5))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(4)
					->array($calls->getIdenticalTo($call5)->toArray())
						->isEqualTo(array(5 => $call5, 6 => $call6, 7 => $call7, 8 => $call8))
				->object($calls->getIdenticalTo($call6))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call6)->toArray())
						->isEqualTo(array(6 => $call6))
				->object($calls->getIdenticalTo($call7))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call7)->toArray())
						->isEqualTo(array(7 => $call7))
				->object($calls->getIdenticalTo(new adapter\call($call5->getFunction(), array(clone $object))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
				->object($calls->getIdenticalTo($call8))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(1)
					->array($calls->getIdenticalTo($call8)->toArray())
						->isEqualTo(array(8 => $call8))
				->object($calls->getIdenticalTo(new adapter\call($call1->getFunction(), array(clone $object, $arg))))
					->isInstanceOf('mageekguy\atoum\test\adapter\calls')
					->hasSize(0)
		;
	}

	public function testGetFirstEqualTo()
	{
		$this
			->if($calls = new testedClass())
			->then
				->variable($calls->getFirstEqualTo(new adapter\call(uniqid())))->isNull()
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->variable($calls->getFirstEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstEqualTo($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = $call1)
			->then
				->variable($calls->getFirstEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstEqualTo($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = clone $call1)
			->then
				->variable($calls->getFirstEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstEqualTo($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = $call4 = new adapter\call($call1->getFunction(), array()))
			->then
				->variable($calls->getFirstEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstEqualTo($call1))->isIdenticalTo(array(1 => $call1))
				->array($calls->getFirstEqualTo($call4))->isIdenticalTo(array(4 => $call4))
			->if($calls[] = $call5 = new adapter\call($call1->getFunction(), array($object = new \mock\object())))
			->then
				->variable($calls->getFirstEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstEqualTo($call1))->isIdenticalTo(array(1 => $call1))
				->array($calls->getFirstEqualTo($call4))->isIdenticalTo(array(4 => $call4))
				->array($calls->getFirstEqualTo($call5))->isIdenticalTo(array(5 => $call5))
			->if($calls[] = $call6 = new adapter\call($call1->getFunction(), array(clone $object)))
			->then
				->variable($calls->getFirstEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstEqualTo($call1))->isIdenticalTo(array(1 => $call1))
				->array($calls->getFirstEqualTo($call4))->isIdenticalTo(array(4 => $call4))
				->array($calls->getFirstEqualTo($call5))->isIdenticalTo(array(5 => $call5))
		;
	}

	public function testGetFirstIdenticalTo()
	{
		$this
			->if($calls = new testedClass())
			->then
				->variable($calls->getFirstIdenticalTo(new adapter\call(uniqid())))->isNull()
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->variable($calls->getFirstIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstIdenticalTo($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = $call1)
			->then
				->variable($calls->getFirstIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstIdenticalTo($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = clone $call1)
			->then
				->variable($calls->getFirstIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstIdenticalTo($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = $call4 = new adapter\call($call1->getFunction(), array()))
			->then
				->variable($calls->getFirstIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstIdenticalTo($call1))->isIdenticalTo(array(1 => $call1))
				->array($calls->getFirstIdenticalTo($call4))->isIdenticalTo(array(4 => $call4))
			->if($calls[] = $call5 = new adapter\call($call1->getFunction(), array($object = new \mock\object())))
			->then
				->variable($calls->getFirstIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstIdenticalTo($call1))->isIdenticalTo(array(1 => $call1))
				->array($calls->getFirstIdenticalTo($call4))->isIdenticalTo(array(4 => $call4))
				->array($calls->getFirstIdenticalTo($call5))->isIdenticalTo(array(5 => $call5))
				->variable($calls->getFirstIdenticalTo(new adapter\call($call1->getFunction(), array(clone $object))))->isNull()
			->if($calls[] = $call6 = new adapter\call($call1->getFunction(), array($clone = clone $object)))
			->then
				->variable($calls->getFirstIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirstIdenticalTo($call1))->isIdenticalTo(array(1 => $call1))
				->array($calls->getFirstIdenticalTo($call4))->isIdenticalTo(array(4 => $call4))
				->array($calls->getFirstIdenticalTo($call5))->isIdenticalTo(array(5 => $call5))
				->array($calls->getFirstIdenticalTo($call6))->isIdenticalTo(array(6 => $call6))
				->variable($calls->getFirstIdenticalTo(new adapter\call($call1->getFunction(), array(clone $object))))->isNull()
		;
	}

	public function testGetFirst()
	{
		$this
			->if($calls = new testedClass())
			->then
				->variable($calls->getFirst(new adapter\call(uniqid())))->isNull()
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->variable($calls->getFirst(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirst($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = $call1)
			->then
				->variable($calls->getFirst(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirst($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = clone $call1)
			->then
				->variable($calls->getFirst(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirst($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = $call4 = new adapter\call($call1->getFunction(), array()))
			->then
				->variable($calls->getFirst(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirst($call1))->isIdenticalTo(array(1 => $call1))
				->array($calls->getFirst($call4))->isIdenticalTo(array(4 => $call4))
			->if($calls[] = $call5 = new adapter\call($call1->getFunction(), array($object = new \mock\object())))
			->then
				->variable($calls->getFirst(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirst($call1))->isIdenticalTo(array(1 => $call1))
				->array($calls->getFirst($call4))->isIdenticalTo(array(4 => $call4))
				->array($calls->getFirst($call5))->isIdenticalTo(array(5 => $call5))
			->if($calls[] = $call6 = new adapter\call($call1->getFunction(), array(clone $object)))
			->then
				->variable($calls->getFirst(new adapter\call(uniqid())))->isNull()
				->array($calls->getFirst($call1))->isIdenticalTo(array(1 => $call1))
				->array($calls->getFirst($call4))->isIdenticalTo(array(4 => $call4))
				->array($calls->getFirst($call5))->isIdenticalTo(array(5 => $call5))
			->if($calls = new testedClass())
			->then
				->variable($calls->getFirst(new adapter\call(uniqid()), true))->isNull()
			->if($calls[] = $call7 = new adapter\call(uniqid()))
			->then
				->variable($calls->getFirst(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getFirst($call7))->isIdenticalTo(array(7 => $call7))
			->if($calls[] = $call7)
			->then
				->variable($calls->getFirst(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getFirst($call7, true))->isIdenticalTo(array(7 => $call7))
			->if($calls[] = clone $call7)
			->then
				->variable($calls->getFirst(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getFirst($call7, true))->isIdenticalTo(array(7 => $call7))
			->if($calls[] = $call10 = new adapter\call($call7->getFunction(), array()))
			->then
				->variable($calls->getFirst(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getFirst($call7, true))->isIdenticalTo(array(7 => $call7))
				->array($calls->getFirst($call10, true))->isIdenticalTo(array(10 => $call10))
			->if($calls[] = $call11 = new adapter\call($call6->getFunction(), array($object = new \mock\object())))
			->then
				->variable($calls->getFirst(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getFirst($call7, true))->isIdenticalTo(array(7 => $call7))
				->array($calls->getFirst($call10, true))->isIdenticalTo(array(10 => $call10))
				->array($calls->getFirst($call11, true))->isIdenticalTo(array(11 => $call11))
				->variable($calls->getFirst(new adapter\call($call6->getFunction(), array(clone $object)), true))->isNull()
			->if($calls[] = $call12 = new adapter\call($call6->getFunction(), array($clone = clone $object)))
			->then
				->variable($calls->getFirst(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getFirst($call7, true))->isIdenticalTo(array(7 => $call7))
				->array($calls->getFirst($call10, true))->isIdenticalTo(array(10 => $call10))
				->array($calls->getFirst($call11, true))->isIdenticalTo(array(11 => $call11))
				->array($calls->getFirst($call12, true))->isIdenticalTo(array(12 => $call12))
				->variable($calls->getFirst(new adapter\call($call7->getFunction(), array(clone $object)), true))->isNull()
		;
	}

	public function testGetLastEqualTo()
	{
		$this
			->if($calls = new testedClass())
			->then
				->variable($calls->getLastEqualTo(new adapter\call(uniqid())))->isNull()
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->variable($calls->getLastEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastEqualTo($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = $call2 = clone $call1)
			->then
				->variable($calls->getLastEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastEqualTo($call1))->isIdenticalTo(array(2 => $call2))
			->if($calls[] = $call3 = new adapter\call($call1->getFunction(), array()))
			->then
				->variable($calls->getLastEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastEqualTo($call1))->isIdenticalTo(array(3 => $call3))
				->array($calls->getLastEqualTo($call3))->isIdenticalTo(array(3 => $call3))
			->if($calls[] = $call4 = clone $call3)
			->then
				->variable($calls->getLastEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastEqualTo($call1))->isIdenticalTo(array(4 => $call4))
				->array($calls->getLastEqualTo($call3))->isIdenticalTo(array(4 => $call4))
			->if($calls[] = $call5 = new adapter\call($call1->getFunction(), array($object = new \mock\object())))
			->then
				->variable($calls->getLastEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastEqualTo($call1))->isIdenticalTo(array(5 => $call5))
				->array($calls->getLastEqualTo($call3))->isIdenticalTo(array(4 => $call4))
				->array($calls->getLastEqualTo($call5))->isIdenticalTo(array(5 => $call5))
			->if($calls[] = $call6 = new adapter\call($call1->getFunction(), array(clone $object)))
			->then
				->variable($calls->getLastEqualTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastEqualTo($call1))->isIdenticalTo(array(6 => $call6))
				->array($calls->getLastEqualTo($call3))->isIdenticalTo(array(4 => $call4))
				->array($calls->getLastEqualTo($call5))->isIdenticalTo(array(6 => $call6))
		;
	}

	public function testGetLastIdenticalTo()
	{
		$this
			->if($calls = new testedClass())
			->then
				->variable($calls->getLastIdenticalTo(new adapter\call(uniqid())))->isNull()
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->variable($calls->getLastIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastIdenticalTo($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = $call2 = clone $call1)
			->then
				->variable($calls->getLastIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastIdenticalTo($call1))->isIdenticalTo(array(2 => $call2))
			->if($calls[] = $call3 = new adapter\call($call1->getFunction(), array()))
			->then
				->variable($calls->getLastIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastIdenticalTo($call1))->isIdenticalTo(array(3 => $call3))
				->array($calls->getLastIdenticalTo($call3))->isIdenticalTo(array(3 => $call3))
			->if($calls[] = $call4 = clone $call3)
			->then
				->variable($calls->getLastIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastIdenticalTo($call1))->isIdenticalTo(array(4 => $call4))
				->array($calls->getLastIdenticalTo($call3))->isIdenticalTo(array(4 => $call4))
			->if($calls[] = $call5 = new adapter\call($call1->getFunction(), array($object = new \mock\object())))
			->then
				->variable($calls->getLastIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastIdenticalTo($call1))->isIdenticalTo(array(5 => $call5))
				->array($calls->getLastIdenticalTo($call3))->isIdenticalTo(array(4 => $call4))
				->array($calls->getLastIdenticalTo($call5))->isIdenticalTo(array(5 => $call5))
				->variable($calls->getLastIdenticalTo(new adapter\call($call1->getFunction(), array(clone $object))))->isNull()
			->if($calls[] = $call6 = new adapter\call($call1->getFunction(), array(clone $object)))
			->then
				->variable($calls->getLastIdenticalTo(new adapter\call(uniqid())))->isNull()
				->array($calls->getLastIdenticalTo($call1))->isIdenticalTo(array(6 => $call6))
				->array($calls->getLastIdenticalTo($call3))->isIdenticalTo(array(4 => $call4))
				->array($calls->getLastIdenticalTo($call5))->isIdenticalTo(array(5 => $call5))
				->array($calls->getLastIdenticalTo($call6))->isIdenticalTo(array(6 => $call6))
				->variable($calls->getLastIdenticalTo(new adapter\call($call1->getFunction(), array(clone $object))))->isNull()
		;
	}

	public function testGetLast()
	{
		$this
			->if($calls = new testedClass())
			->then
				->variable($calls->getLast(new adapter\call(uniqid())))->isNull()
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->variable($calls->getLast(new adapter\call(uniqid())))->isNull()
				->array($calls->getLast($call1))->isIdenticalTo(array(1 => $call1))
			->if($calls[] = $call2 = clone $call1)
			->then
				->variable($calls->getLast(new adapter\call(uniqid())))->isNull()
				->array($calls->getLast($call1))->isIdenticalTo(array(2 => $call2))
			->if($calls[] = $call3 = new adapter\call($call1->getFunction(), array()))
			->then
				->variable($calls->getLast(new adapter\call(uniqid())))->isNull()
				->array($calls->getLast($call1))->isIdenticalTo(array(3 => $call3))
				->array($calls->getLast($call3))->isIdenticalTo(array(3 => $call3))
			->if($calls[] = $call4 = clone $call3)
			->then
				->variable($calls->getLast(new adapter\call(uniqid())))->isNull()
				->array($calls->getLast($call1))->isIdenticalTo(array(4 => $call4))
				->array($calls->getLast($call3))->isIdenticalTo(array(4 => $call4))
			->if($calls[] = $call5 = new adapter\call($call1->getFunction(), array($object = new \mock\object())))
			->then
				->variable($calls->getLast(new adapter\call(uniqid())))->isNull()
				->array($calls->getLast($call1))->isIdenticalTo(array(5 => $call5))
				->array($calls->getLast($call3))->isIdenticalTo(array(4 => $call4))
				->array($calls->getLast($call5))->isIdenticalTo(array(5 => $call5))
			->if($calls[] = $call6 = new adapter\call($call1->getFunction(), array(clone $object)))
			->then
				->variable($calls->getLast(new adapter\call(uniqid())))->isNull()
				->array($calls->getLast($call1))->isIdenticalTo(array(6 => $call6))
				->array($calls->getLast($call3))->isIdenticalTo(array(4 => $call4))
				->array($calls->getLast($call5))->isIdenticalTo(array(6 => $call6))
			->if($calls = new testedClass())
			->then
				->variable($calls->getLast(new adapter\call(uniqid()), true))->isNull()
			->if($calls[] = $call7 = new adapter\call(uniqid()))
			->then
				->variable($calls->getLast(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getLast($call7, true))->isIdenticalTo(array(7 => $call7))
			->if($calls[] = $call8 = clone $call7)
			->then
				->variable($calls->getLast(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getLast($call7, true))->isIdenticalTo(array(8 => $call8))
			->if($calls[] = $call9 = new adapter\call($call7->getFunction(), array()))
			->then
				->variable($calls->getLast(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getLast($call7, true))->isIdenticalTo(array(9 => $call9))
				->array($calls->getLast($call9, true))->isIdenticalTo(array(9 => $call9))
			->if($calls[] = $call10 = clone $call9)
			->then
				->variable($calls->getLast(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getLast($call7, true))->isIdenticalTo(array(10 => $call10))
				->array($calls->getLast($call9, true))->isIdenticalTo(array(10 => $call10))
			->if($calls[] = $call11 = new adapter\call($call7->getFunction(), array($object = new \mock\object())))
			->then
				->variable($calls->getLast(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getLast($call7, true))->isIdenticalTo(array(11 => $call11))
				->array($calls->getLast($call9, true))->isIdenticalTo(array(10 => $call10))
				->array($calls->getLast($call11, true))->isIdenticalTo(array(11 => $call11))
				->variable($calls->getLast(new adapter\call($call7->getFunction(), array(clone $object)), true))->isNull()
			->if($calls[] = $call12 = new adapter\call($call7->getFunction(), array(clone $object)))
			->then
				->variable($calls->getLast(new adapter\call(uniqid()), true))->isNull()
				->array($calls->getLast($call7, true))->isIdenticalTo(array(12 => $call12))
				->array($calls->getLast($call9, true))->isIdenticalTo(array(10 => $call10))
				->array($calls->getLast($call11, true))->isIdenticalTo(array(11 => $call11))
				->array($calls->getLast($call12, true))->isIdenticalTo(array(12 => $call12))
				->variable($calls->getLast(new adapter\call($call7->getFunction(), array(clone $object)), true))->isNull()
		;
	}

	public function testGetTimeline()
	{
		$this
			->if($calls = new testedClass())
			->then
				->array($calls->getTimeline())->isEmpty()
			->if($calls[] = $call1 = new adapter\call(uniqid()))
			->then
				->array($calls->getTimeline())->isEqualTo(array(1 => $call1))
			->if($calls[] = $call2 = new adapter\call(uniqid()))
			->then
				->array($calls->getTimeline())->isEqualTo(array(
						1 => $call1,
						2 => $call2
					)
				)
			->if($otherCalls = new testedClass())
			->and($otherCalls[] = $call3 = new adapter\call(uniqid()))
			->then
				->array($calls->getTimeline())->isEqualTo(array(
						1 => $call1,
						2 => $call2
					)
				)
			->if($calls[] = $call4 = new adapter\call(uniqid()))
			->then
				->array($calls->getTimeline())->isEqualTo(array(
						1 => $call1,
						2 => $call2,
						4 => $call4
					)
				)
		;
	}
}