<?php

namespace mageekguy\atoum\tests\units\test;

use
	mageekguy\atoum\test,
	mageekguy\atoum\test\adapter\call,
	mageekguy\atoum\test\adapter as testedClass
;

require_once __DIR__ . '/../../runner.php';

class adapter extends test
{
	public function testClass()
	{
		$this->testedClass->extends('mageekguy\atoum\adapter');
	}

	public function test__construct()
	{
		$this
			->if($adapter = new testedClass())
			->then
				->array($adapter->getInvokers())->isEmpty()
				->object($adapter->getCalls())->isEqualTo(new test\adapter\calls())
		;
	}

	public function test__set()
	{
		$this
			->if($adapter = new testedClass())
			->and($adapter->md5 = $closure = function() {})
			->then
				->object($adapter->md5->getClosure())->isIdenticalTo($closure)
			->if($adapter->md5 = $return = uniqid())
			->then
				->object($adapter->md5)->isInstanceOf('mageekguy\atoum\test\adapter\invoker')
				->object($adapter->MD5)->isInstanceOf('mageekguy\atoum\test\adapter\invoker')
				->string($adapter->invoke('md5'))->isEqualTo($return)
				->string($adapter->invoke('MD5'))->isEqualTo($return)
			->if($adapter->MD5 = $return = uniqid())
			->then
				->object($adapter->md5)->isInstanceOf('mageekguy\atoum\test\adapter\invoker')
				->object($adapter->MD5)->isInstanceOf('mageekguy\atoum\test\adapter\invoker')
				->string($adapter->invoke('md5'))->isEqualTo($return)
				->string($adapter->invoke('MD5'))->isEqualTo($return)
		;
	}

	public function test__get()
	{
		$this
			->if($adapter = new testedClass())
			->and($adapter->md5 = $closure = function() {})
			->then
				->object($adapter->md5->getClosure())->isIdenticalTo($closure)
				->object($adapter->MD5->getClosure())->isIdenticalTo($closure)
			->if($adapter->md5 = uniqid())
			->then
				->object($adapter->md5->getClosure())->isInstanceOf('closure')
				->object($adapter->MD5->getClosure())->isInstanceOf('closure')
		;
	}

	public function test__isset()
	{
		$this
			->if($adapter = new testedClass())
			->then
				->boolean(isset($adapter->md5))->isFalse()
			->if($adapter->{$function = strtolower(uniqid())} = function() {})
			->then
				->boolean(isset($adapter->{$function}))->isTrue()
				->boolean(isset($adapter->{strtoupper($function)}))->isTrue()
			->if($adapter->{$function = strtoupper(uniqid())} = function() {})
			->then
				->boolean(isset($adapter->{strtolower($function)}))->isTrue()
				->boolean(isset($adapter->{$function}))->isTrue()
			->if($adapter->{$function = strtolower(uniqid())} = uniqid())
			->then
				->boolean(isset($adapter->{$function}))->isTrue()
				->boolean(isset($adapter->{strtoupper($function)}))->isTrue()
			->if($adapter->{$function = strtoupper(uniqid())} = uniqid())
			->then
				->boolean(isset($adapter->{$function}))->isTrue()
				->boolean(isset($adapter->{strtolower($function)}))->isTrue()
			->if($adapter->{$function = uniqid()}[2] = uniqid())
			->then
				->boolean(isset($adapter->{$function}))->isFalse()
				->boolean(isset($adapter->{$function}[0]))->isFalse()
				->boolean(isset($adapter->{$function}[1]))->isFalse()
				->boolean(isset($adapter->{$function}[2]))->isTrue()
				->boolean(isset($adapter->{$function}[3]))->isFalse()
		;
	}

	public function test__unset()
	{
		$this
			->if($adapter = new testedClass())
			->then
				->array($adapter->getInvokers())->isEmpty()
				->array($adapter->getCalls()->toArray())->isEmpty()
			->when(function() use ($adapter) { unset($adapter->md5); })
				->array($adapter->getInvokers())->isEmpty()
				->array($adapter->getCalls()->toArray())->isEmpty()
			->when(function() use ($adapter) { unset($adapter->MD5); })
				->array($adapter->getInvokers())->isEmpty()
				->array($adapter->getCalls()->toArray())->isEmpty()
			->when(function() use ($adapter) { $adapter->md5 = uniqid(); $adapter->md5(uniqid()); })
				->array($adapter->getInvokers())->isNotEmpty()
				->array($adapter->getCalls()->toArray())->isNotEmpty()
			->when(function() use ($adapter) { unset($adapter->{uniqid()}); })
				->array($adapter->getInvokers())->isNotEmpty()
				->array($adapter->getCalls()->toArray())->isNotEmpty()
			->when(function() use ($adapter) { unset($adapter->md5); })
				->array($adapter->getInvokers())->isEmpty()
				->array($adapter->getCalls()->toArray())->isEmpty()
			->when(function() use ($adapter) { $adapter->MD5 = uniqid(); $adapter->MD5(uniqid()); })
				->array($adapter->getInvokers())->isNotEmpty()
				->array($adapter->getCalls()->toArray())->isNotEmpty()
			->when(function() use ($adapter) { unset($adapter->{uniqid()}); })
				->array($adapter->getInvokers())->isNotEmpty()
				->array($adapter->getCalls()->toArray())->isNotEmpty()
			->when(function() use ($adapter) { unset($adapter->MD5); })
				->array($adapter->getInvokers())->isEmpty()
				->array($adapter->getCalls()->toArray())->isEmpty()
		;
	}

	public function test__call()
	{
		$this
			/*
			->if($adapter = new testedClass())
			->then
				->string($adapter->md5($hash = uniqid()))->isEqualTo(md5($hash))
				->string($adapter->MD5($hash = uniqid()))->isEqualTo(md5($hash))
			->if($adapter->md5 = $md5 = uniqid())
			->then
				->string($adapter->md5($hash))->isEqualTo($md5)
				->string($adapter->MD5($hash))->isEqualTo($md5)
			->if($adapter->md5 = $md5 = uniqid())
			->then
				->string($adapter->md5($hash))->isEqualTo($md5)
				->string($adapter->MD5($hash))->isEqualTo($md5)
				->exception(function() use ($adapter) {
							$adapter->require(uniqid());
						}
					)
					->isInstanceOf('mageekguy\atoum\exceptions\logic\invalidArgument')
					->hasMessage('Function \'require()\' is not invokable by an adapter')
				->exception(function() use ($adapter) {
							$adapter->REQUIRE(uNiqid());
						}
					)
					->isInstanceOf('mageekguy\atoum\exceptions\logic\invalidArgument')
					->hasMessage('Function \'REQUIRE()\' is not invokable by an adapter')
			->if($adapter->md5 = 0)
			->and($adapter->md5[1] = 1)
			->and($adapter->md5[2] = 2)
			->and($adapter->resetCalls())
			->then
				->integer($adapter->md5())->isEqualTo(1)
				->integer($adapter->md5())->isEqualTo(2)
				->integer($adapter->md5())->isEqualTo(0)
			->if($adapter->resetCalls())
			->then
				->integer($adapter->MD5())->isEqualTo(1)
				->integer($adapter->MD5())->isEqualTo(2)
				->integer($adapter->MD5())->isEqualTo(0)
			->if($adapter->MD5 = 0)
			->and($adapter->MD5[1] = 1)
			->and($adapter->MD5[2] = 2)
			->and($adapter->resetCalls())
			->then
				->integer($adapter->md5())->isEqualTo(1)
				->integer($adapter->md5())->isEqualTo(2)
				->integer($adapter->md5())->isEqualTo(0)
			->if($adapter->resetCalls())
			->then
				->integer($adapter->MD5())->isEqualTo(1)
				->integer($adapter->MD5())->isEqualTo(2)
				->integer($adapter->MD5())->isEqualTo(0)
			*/
			->if($adapter = new testedClass())
			->and($adapter->sha1[2] = $sha1 = uniqid())
			->then
				->string($adapter->sha1($string = uniqid()))->isEqualTo(sha1($string))
//				->string($adapter->sha1($string = uniqid()))->isEqualTo(sha1($string))
				->string($adapter->sha1(uniqid()))->isEqualTo($sha1)
//				->string($adapter->sha1($otherString = uniqid()))->isEqualTo(sha1($otherString))
		;
	}

	public function test__sleep()
	{
		$this
			->if($adapter = new testedClass())
			->then
				->array($adapter->__sleep())->isEmpty()
		;
	}

	public function test__toString()
	{
		$this
			->if($adapter = new testedClass())
			->and($calls = new test\adapter\calls())
			->then
				->castToString($adapter)->isEqualTo((string) $calls)
		;
	}

	public function testSerialize()
	{
		$this
			->if($adapter = new testedClass())
			->then
				->string(serialize($adapter))->isNotEmpty()
			->if($adapter->md5 = function() {})
			->then
				->string(serialize($adapter))->isNotEmpty()
		;
	}

	public function testSetCalls()
	{
		$this
			->if($adapter = new testedClass())
			->then
				->object($adapter->setCalls($calls = new test\adapter\calls()))->isIdenticalTo($adapter)
				->object($adapter->getCalls())->isIdenticalTo($calls)
				->object($adapter->setCalls())->isIdenticalTo($adapter)
				->object($adapter->getCalls())
					->isNotIdenticalTo($calls)
					->isEqualTo(new test\adapter\calls())
			->if($calls = new test\adapter\calls())
			->and($calls[] = new test\adapter\call(uniqid()))
			->and($adapter->setCalls($calls))
			->then
				->object($adapter->getCalls())
					->isIdenticalTo($calls)
					->hasSize(0)
		;
	}

	public function testGetCalls()
	{
		$this
			->if($adapter = new testedClass())
			->and($adapter->setCalls($calls = new \mock\mageekguy\atoum\test\adapter\calls()))
			->and($this->calling($calls)->get = $innerCalls = new test\adapter\calls())
			->then
				->object($adapter->getCalls())->isIdenticalTo($calls)
				->object($adapter->getCalls($call = new test\adapter\call(uniqid())))->isIdenticalTo($innerCalls)
				->mock($calls)->call('get')->withArguments($call, false)->once()
		;
	}

	public function testGetCallsEqualTo()
	{
		$this
			->if($calls = new \mock\mageekguy\atoum\test\adapter\calls())
			->and($this->calling($calls)->getEqualTo = $equalCalls = new test\adapter\calls())
			->and($adapter = new testedClass())
			->and($adapter->setCalls($calls))
			->then
				->object($adapter->getCallsEqualTo($call = new call('md5')))->isIdenticalTo($equalCalls)
				->mock($calls)->call('getEqualTo')->withArguments($call)->once()
		;
	}

	public function testGetPreviousCalls()
	{
		$this
			->if($calls = new \mock\mageekguy\atoum\test\adapter\calls())
			->and($this->calling($calls)->getPrevious = $previousCalls = new test\adapter\calls())
			->and($adapter = new testedClass())
			->and($adapter->setCalls($calls))
			->then
				->object($adapter->getPreviousCalls($call = new call('md5'), $position = rand(1, PHP_INT_MAX)))->isIdenticalTo($previousCalls)
				->mock($calls)->call('getPrevious')->withArguments($call, $position, false)->once()
				->object($adapter->getPreviousCalls($call = new call('md5'), $position = rand(1, PHP_INT_MAX), true))->isIdenticalTo($previousCalls)
				->mock($calls)->call('getPrevious')->withArguments($call, $position, true)->once()
		;
	}

	public function testGetAfterCalls()
	{
		$this
			->if($calls = new \mock\mageekguy\atoum\test\adapter\calls())
			->and($this->calling($calls)->getAfter = $afterCalls = new test\adapter\calls())
			->and($adapter = new testedClass())
			->and($adapter->setCalls($calls))
			->then
				->object($adapter->getAfterCalls($call = new call('md5'), $position = rand(1, PHP_INT_MAX)))->isIdenticalTo($afterCalls)
				->mock($calls)->call('getAfter')->withArguments($call, $position, false)->once()
				->object($adapter->getAfterCalls($call = new call('md5'), $position = rand(1, PHP_INT_MAX), true))->isIdenticalTo($afterCalls)
				->mock($calls)->call('getAfter')->withArguments($call, $position, true)->once()
		;
	}

	public function testGetCallsIdenticalTo()
	{
		$this
			->if($calls = new \mock\mageekguy\atoum\test\adapter\calls())
			->and($this->calling($calls)->getIdenticalTo = $identicalCalls = new test\adapter\calls())
			->and($adapter = new testedClass())
			->and($adapter->setCalls($calls))
			->then
				->object($adapter->getCallsIdenticalTo($call = new call('md5')))->isIdenticalTo($identicalCalls)
				->mock($calls)->call('getIdenticalTo')->withArguments($call)->once()
		;
	}

	public function testGetFirstCallEqualTo()
	{
		$this
			->if($calls = new \mock\mageekguy\atoum\test\adapter\calls())
			->and($this->calling($calls)->getFirstEqualTo = null)
			->and($adapter = new testedClass())
			->and($adapter->setCalls($calls))
			->then
				->variable($adapter->getFirstCallEqualTo($call = new call(uniqid())))->isNull()
				->mock($calls)->call('getFirstEqualTo')->withArguments($call)->once()
			->if($this->calling($calls)->getFirstEqualTo = $firstCall = new call('md5'))
			->then
				->object($adapter->getFirstCallEqualTo($otherCall = new call(uniqid())))->isIdenticalTo($firstCall)
				->mock($calls)->call('getFirstEqualTo')->withArguments($otherCall)->once()
		;
	}

	public function testGetFirstCallIdenticalTo()
	{
		$this
			->if($calls = new \mock\mageekguy\atoum\test\adapter\calls())
			->and($this->calling($calls)->getFirstIdenticalTo = null)
			->and($adapter = new testedClass())
			->and($adapter->setCalls($calls))
			->then
				->variable($adapter->getFirstCallIdenticalTo($call = new call(uniqid())))->isNull()
				->mock($calls)->call('getFirstIdenticalTo')->withArguments($call)->once()
			->if($this->calling($calls)->getFirstIdenticalTo = $firstCall = new call('md5'))
			->then
				->object($adapter->getFirstCallIdenticalTo($otherCall = new call(uniqid())))->isIdenticalTo($firstCall)
				->mock($calls)->call('getFirstIdenticalTo')->withArguments($otherCall)->once()
		;
	}

	public function testGetLastCallEqualTo()
	{
		$this
			->if($calls = new \mock\mageekguy\atoum\test\adapter\calls())
			->and($this->calling($calls)->getLastEqualTo = null)
			->and($adapter = new testedClass())
			->and($adapter->setCalls($calls))
			->then
				->variable($adapter->getLastCallEqualTo($call = new call(uniqid())))->isNull()
				->mock($calls)->call('getLastEqualTo')->withArguments($call)->once()
			->if($this->calling($calls)->getLastEqualTo = $firstCall = new call('md5'))
			->then
				->object($adapter->getLastCallEqualTo($otherCall = new call(uniqid())))->isIdenticalTo($firstCall)
				->mock($calls)->call('getLastEqualTo')->withArguments($otherCall)->once()
		;
	}

	public function testGetLastCallIdenticalTo()
	{
		$this
			->if($calls = new \mock\mageekguy\atoum\test\adapter\calls())
			->and($this->calling($calls)->getLastIdenticalTo = null)
			->and($adapter = new testedClass())
			->and($adapter->setCalls($calls))
			->then
				->variable($adapter->getLastCallIdenticalTo($call = new call(uniqid())))->isNull()
				->mock($calls)->call('getLastIdenticalTo')->withArguments($call)->once()
			->if($this->calling($calls)->getLastIdenticalTo = $firstCall = new call('md5'))
			->then
				->object($adapter->getLastCallIdenticalTo($otherCall = new call(uniqid())))->isIdenticalTo($firstCall)
				->mock($calls)->call('getLastIdenticalTo')->withArguments($otherCall)->once()
		;
	}

	public function testGetCallNumber()
	{
		$this
			->if($calls = new \mock\mageekguy\atoum\test\adapter\calls())
			->and($this->calling($calls)->count = 0)
			->and($adapter = new testedClass())
			->and($adapter->setCalls($calls))
			->then
				->integer($adapter->getCallNumber())->isZero()
			->and($this->calling($calls)->count = $callNumber = rand(1, PHP_INT_MAX))
			->then
				->integer($adapter->getCallNumber())->isEqualTo($callNumber)
		;
	}

	public function testGetTimeline()
	{
		$this
			->if($adapter = new testedClass())
			->and($adapter->setCalls($calls = new \mock\mageekguy\atoum\test\adapter\calls()))
			->and($this->calling($calls)->getTimeline = array())
			->then
				->array($adapter->getTimeline())->isEmpty()
				->mock($calls)->call('getTimeline')->withArguments(null, false)->once()
		;
	}

	public function testAddCall()
	{
		$this
			->if($adapter = new testedClass())
			->and($adapter->setCalls($calls = new \mock\mageekguy\atoum\test\adapter\calls()))
			->and($this->calling($calls)->addCall = $calls)
			->then
				->object($adapter->addCall($method = uniqid(), $args = array(uniqid())))->isIdenticalTo($adapter)
				->mock($calls)->call('addCall')->withArguments(new test\adapter\call($method, $args))->once()
				->object($adapter->addCall($otherMethod = uniqid(), $otherArgs = array(uniqid(), uniqid())))->isIdenticalTo($adapter)
				->mock($calls)->call('addCall')->withArguments(new test\adapter\call($otherMethod, $otherArgs))->once()
				->object($adapter->addCall($method, $anotherArgs = array(uniqid())))->isIdenticalTo($adapter)
				->mock($calls)->call('addCall')->withArguments(new test\adapter\call($method, $anotherArgs))->once()
			->if($arg = 'foo')
			->and($arguments = array(& $arg))
			->then
				->object($adapter->addCall($method, $arguments))->isIdenticalTo($adapter)
				->mock($calls)->call('addCall')->withArguments(new test\adapter\call($method, $arguments))->once()
		;
	}

	public function testResetCalls()
	{
		$this
			->if($adapter = new testedClass())
			->and($adapter->md5(uniqid()))
			->then
				->sizeof($adapter->getCalls())->isGreaterThan(0)
				->object($adapter->resetCalls())->isIdenticalTo($adapter)
			->sizeof($adapter->getCalls())->isZero(0)
		;
	}

	public function testReset()
	{
		$this
			->if($adapter = new testedClass())
			->then
				->array($adapter->getInvokers())->isEmpty()
				->sizeof($adapter->getCalls())->isZero()
				->object($adapter->reset())->isIdenticalTo($adapter)
				->array($adapter->getInvokers())->isEmpty()
				->sizeof($adapter->getCalls())->isZero()
			->if($adapter->md5(uniqid()))
			->then
				->array($adapter->getInvokers())->isEmpty()
				->sizeof($adapter->getCalls())->isGreaterThan(0)
				->object($adapter->reset())->isIdenticalTo($adapter)
				->array($adapter->getInvokers())->isEmpty()
				->sizeof($adapter->getCalls())->isZero()
			->if($adapter->md5 = uniqid())
			->then
				->array($adapter->getInvokers())->isNotEmpty()
				->sizeof($adapter->getCalls())->isZero(0)
				->object($adapter->reset())->isIdenticalTo($adapter)
				->array($adapter->getInvokers())->isEmpty()
				->sizeof($adapter->getCalls())->isZero()
			->if($adapter->md5 = uniqid())
			->and($adapter->md5(uniqid()))
			->then
				->array($adapter->getInvokers())->isNotEmpty()
				->sizeof($adapter->getCalls())->isGreaterThan(0)
				->object($adapter->reset())->isIdenticalTo($adapter)
				->array($adapter->getInvokers())->isEmpty()
				->sizeof($adapter->getCalls())->isZero()
		;
	}
}