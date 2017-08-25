<?php

/*
 * This file is part of Icicle, a library for writing asynchronous code in PHP using promises and coroutines.
 *
 * @copyright 2014-2015 Aaron Piotrowski. All rights reserved.
 * @license MIT See the LICENSE file that was distributed with this source code for more information.
 */

namespace Icicle\Tests\Promise;

use Exception;
use Icicle\Loop;
use Icicle\Loop\SelectLoop;
use Icicle\Promise;
use Icicle\Promise\Exception\CancelledException;
use Icicle\Promise\Exception\CircularResolutionError;
use Icicle\Promise\Exception\Error;
use Icicle\Promise\Exception\InvalidResolverError;
use Icicle\Promise\Exception\RejectedException;
use Icicle\Promise\Exception\TimeoutException;
use Icicle\Promise\Exception\UnexpectedTypeError;
use Icicle\Promise\Thenable;
use Icicle\Tests\TestCase;
use InvalidArgumentException;
use RuntimeException;

function exceptionHandler(RuntimeException $exception)
{
    return $exception;
}

class PromiseTest extends TestCase
{
    /**
     * @var \Icicle\Promise\Promise
     */
    protected $promise;

    /**
     * @var callable
     */
    protected $resolve;

    /**
     * @var callable
     */
    protected $reject;

    public function setUp()
    {
        Loop\loop(new SelectLoop());

        $this->promise = new Promise\Promise(function ($resolve, $reject) {
            $this->resolve = $resolve;
            $this->reject = $reject;
        });
    }

    /**
     * @param mixed $value
     */
    protected function resolve($value = null)
    {
        $resolve = $this->resolve;
        $resolve($value);
    }

    /**
     * @param mixed $reason
     */
    protected function reject($reason = null)
    {
        $reject = $this->reject;
        $reject($reason);
    }

    public function instanceExceptionHandler(RuntimeException $exception)
    {
        return $exception;
    }

    public static function staticExceptionHandler(RuntimeException $exception)
    {
        return $exception;
    }

    public function testResolverThrowingRejectsPromise()
    {
        $exception = new Exception();

        $promise = new Promise\Promise(function () use ($exception) {
            throw $exception;
        });

        $this->assertFalse($promise->isPending());
        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    public function testResolverReturningNonCallableRejectsPromise()
    {
        $promise = new Promise\Promise(function () {
            return true;
        });

        $this->assertFalse($promise->isPending());
        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $exception) {
            $this->assertInstanceOf(InvalidResolverError::class, $exception);
        }
    }

    public function testThenReturnsPromise()
    {
        $this->assertInstanceOf(Thenable::class, $this->promise->then());
    }

    public function testResolve()
    {
        $this->assertSame($this->promise, Promise\resolve($this->promise));

        $value = 'test';
        $fulfilled = Promise\resolve($value);

        $this->assertInstanceOf(Thenable::class, $fulfilled);

        $this->assertFalse($fulfilled->isPending());
        $this->assertTrue($fulfilled->isFulfilled());
        $this->assertFalse($fulfilled->isRejected());
        $this->assertFalse($fulfilled->isCancelled());
        $this->assertSame($value, $fulfilled->wait());
    }

    public function testReject()
    {
        $exception = new Exception();

        $rejected = Promise\reject($exception);

        $this->assertInstanceOf(Thenable::class, $rejected);

        $this->assertFalse($rejected->isPending());
        $this->assertFalse($rejected->isFulfilled());
        $this->assertTrue($rejected->isRejected());
        $this->assertFalse($rejected->isCancelled());

        try {
            $rejected->wait();
        } catch (Exception $exception) {
            $this->assertSame($exception, $exception);
        }
    }

    /**
     * @return array
     */
    public function rejectionReasons()
    {
        return [
            ['String to reject promise.'],
            [new \stdClass()],
            [[1, 2, 3]],
            [404],
            [3.14159],
            [false],
        ];
    }

    /**
     * @depends testReject
     * @dataProvider rejectionReasons
     */
    public function testRejectWithReason($reason)
    {
        $rejected = Promise\reject($reason);

        $this->assertInstanceOf(Thenable::class, $rejected);

        $this->assertFalse($rejected->isPending());
        $this->assertFalse($rejected->isFulfilled());
        $this->assertTrue($rejected->isRejected());

        try {
            $rejected->wait();
        } catch (Exception $exception) {
            $this->assertInstanceOf(RejectedException::class, $exception);
            $this->assertSame($reason, $exception->getReason());
        }
    }

    public function testResolveCallableWithValue()
    {
        $value = 'test';

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($value));

        $this->promise->done($callback);

        $this->resolve($value);

        Loop\run();

        $this->assertFalse($this->promise->isPending());
        $this->assertTrue($this->promise->isFulfilled());
        $this->assertSame($value, $this->promise->wait());
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testThenReturnsPromiseAfterFulfilled()
    {
        $value = 'test';
        $this->resolve($value);

        Loop\run();

        $this->assertInstanceOf(Thenable::class, $this->promise->then());
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testResolveMakesPromiseImmutable()
    {
        $value1 = 'test1';
        $value2 = 'test2';
        $exception = new Exception();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($value1));

        $this->promise->done($callback);

        $this->resolve($value1);
        $this->resolve($value2);
        $this->reject($exception);

        Loop\run();

        $this->assertTrue($this->promise->isFulfilled());
        $this->assertSame($value1, $this->promise->wait());
    }

    /**
     * @depends testResolve
     * @depends testResolveCallableWithValue
     */
    public function testResolveCallableWithFulfilledPromise()
    {
        $value = 'test';
        $fulfilled = Promise\resolve($value);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($value));

        $this->promise->done($callback);

        $this->resolve($fulfilled);

        Loop\run();

        $this->assertTrue($this->promise->isFulfilled());
        $this->assertSame($value, $this->promise->wait());
    }

    /**
     * @depends testReject
     */
    public function testResolveCallableWithRejectedPromise()
    {
        $exception = new Exception();
        $rejected = Promise\reject($exception);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $this->promise->done($this->createCallback(0), $callback);

        $this->resolve($rejected);

        Loop\run();

        $this->assertTrue($this->promise->isRejected());
    }

    /**
     * @depends testResolveCallableWithFulfilledPromise
     * @depends testResolveCallableWithRejectedPromise
     */
    public function testResolveCallableWithPendingPromise()
    {
        $promise = new Promise\Promise(function () {});

        $this->resolve($promise);

        Loop\run();

        $this->assertTrue($this->promise->isPending());
    }

    /**
     * @depends testResolveCallableWithPendingPromise
     */
    public function testResolveCallableWithPendingPromiseThenFulfillPendingPromise()
    {
        $value = 'test';

        $promise = new Promise\Promise(function ($resolve) use (&$pendingResolve) {
            $pendingResolve = $resolve;
        });

        $this->resolve($promise);

        Loop\run();

        $pendingResolve($value);

        Loop\run();

        $this->assertTrue($this->promise->isFulfilled());
        $this->assertSame($value, $this->promise->wait());
        $this->assertSame($this->promise->wait(), $promise->wait());
    }

    /**
     * @depends testResolveCallableWithPendingPromise
     */
    public function testResolveCallableWithPendingPromiseThenRejectPendingPromise()
    {
        $exception = new Exception();

        $promise = new Promise\Promise(function ($resolve, $reject) use (&$pendingReject) {
            $pendingReject = $reject;
        });

        $this->resolve($promise);

        Loop\run();

        $pendingReject($exception);

        Loop\run();

        $this->assertTrue($this->promise->isRejected());

        try {
            $this->promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testResolveCallableWithPendingPromise
     */
    public function testResolveCallableWithPendingPromiseThenResolveInvokesAllRegisteredCallbacks()
    {
        $value = 'test';

        $this->promise->done($this->createCallback(1), $this->createCallback(0));
        $this->promise->done($this->createCallback(1), $this->createCallback(0));

        $promise = new Promise\Promise(function ($resolve) use (&$pendingResolve) {
            $pendingResolve = $resolve;
        });

        $promise->done($this->createCallback(1), $this->createCallback(0));
        $promise->done($this->createCallback(1), $this->createCallback(0));

        $this->resolve($promise);

        Loop\run();

        $pendingResolve($value);

        Loop\run();

        $this->assertTrue($this->promise->isFulfilled());
        $this->assertSame($value, $this->promise->wait());
        $this->assertSame($this->promise->wait(), $promise->wait());
    }

    /**
     * @depends testResolveCallableWithPendingPromise
     */
    public function testResolveCallableWithSelfRejectsPromise()
    {
        $this->resolve($this->promise);

        $this->assertTrue($this->promise->isRejected());

        try {
            $this->promise->wait();
        } catch (Exception $exception) {
            $this->assertInstanceOf(CircularResolutionError::class, $exception);
        }
    }

    /**
     * @depends testResolveCallableWithSelfRejectsPromise
     */
    public function testResolveWithCircularReferenceRejectsPromise()
    {
        $promise = new Promise\Promise(function ($resolve) use (&$pendingResolve) {
            $pendingResolve = $resolve;
        });

        $child = $this->promise->then(function () use ($promise) {
            return $promise;
        });

        $pendingResolve($child);

        Loop\run();

        $this->resolve();

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $exception) {
            $this->assertInstanceOf(CircularResolutionError::class, $exception);
        }

        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $exception) {
            $this->assertInstanceOf(CircularResolutionError::class, $exception);
        }
    }

    public function testRejectCallable()
    {
        $exception = new Exception();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
                 ->with($this->identicalTo($exception));

        $this->promise->done($this->createCallback(0), $callback);

        $this->reject($exception);

        Loop\run();

        $this->assertFalse($this->promise->isPending());
        $this->assertTrue($this->promise->isRejected());

        try {
            $this->promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testRejectCallable
     */
    public function testRejectCallableWithValueReason()
    {
        $reason = 'String to reject promise.';

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(RejectedException::class));

        $this->promise->done($this->createCallback(0), $callback);

        $this->reject($reason);

        Loop\run();

        $this->assertFalse($this->promise->isPending());
        $this->assertTrue($this->promise->isRejected());

        try {
            $this->promise->wait();
        } catch (Exception $exception) {
            $this->assertInstanceOf(RejectedException::class, $exception);
            $this->assertSame($reason, $exception->getReason());
        }
    }

    /**
     * @depends testRejectCallable
     */
    public function testThenReturnsPromiseAfterRejected()
    {
        $exception = new Exception();
        $this->reject($exception);

        Loop\run();

        $this->assertInstanceOf(Thenable::class, $this->promise->then());
    }

    /**
     * @depends testRejectCallable
     */
    public function testRejectMakesPromiseImmutable()
    {
        $exception1 = new Exception();
        $exception2 = new Exception();
        $value = 'test';

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception1));

        $this->promise->done($this->createCallback(0), $callback);

        $this->reject($exception1);
        $this->resolve($value);
        $this->reject($exception2);

        Loop\run();

        $this->assertTrue($this->promise->isRejected());

        try {
            $this->promise->wait();
        } catch (Exception $exception) {
            $this->assertSame($exception1, $exception);
        }
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testThenInvokeNewCallbackAfterResolved()
    {
        $value = 'test';
        $this->resolve($value);

        Loop\run();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($value));

        $this->promise->then($callback, $this->createCallback(0));

        Loop\run();
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testDoneInvokeNewCallbackAfterResolved()
    {
        $value = 'test';
        $this->resolve($value);

        Loop\run();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($value));

        $this->promise->done($callback, $this->createCallback(0));

        Loop\run();
    }

    /**
     * @depends testRejectCallable
     */
    public function testThenInvokeNewCallbackAfterRejected()
    {
        $exception = new Exception();
        $this->reject($exception);

        Loop\run();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $this->promise->then($this->createCallback(0), $callback);

        Loop\run();
    }

    /**
     * @depends testRejectCallable
     */
    public function testDoneInvokeNewCallbackAfterRejected()
    {
        $exception = new Exception();
        $this->reject($exception);

        Loop\run();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $this->promise->done($this->createCallback(0), $callback);

        Loop\run();
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testChildPromiseResolutionWithNoCallbacks()
    {
        $value = 'test';

        $child = $this->promise->then();

        $this->resolve($value);

        Loop\run();

        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value, $child->wait());
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testChildPromiseFulfilledWithOnFulfilledReturnValueWithThenBeforeResolve()
    {
        $value1 = 'test';
        $value2 = 1;

        $callback = function ($value) use (&$parameter, $value2) {
            $parameter = $value;
            return $value2;
        };

        $child = $this->promise->then($callback);

        $this->resolve($value1);

        Loop\run();

        $this->assertSame($value1, $parameter);
        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value2, $child->wait());
    }

    /**
     * @depends testRejectCallable
     */
    public function testChildPromiseFulfilledWithOnRejectedReturnValueWithThenBeforeReject()
    {
        $exception = new Exception();
        $value = 1;

        $callback = function ($e) use (&$parameter, $value) {
            $parameter = $e;
            return $value;
        };

        $child = $this->promise->then($this->createCallback(0), $callback);

        $this->reject($exception);

        Loop\run();

        $this->assertSame($exception, $parameter);
        $this->assertTrue($child->isFulFilled());
        $this->assertSame($value, $child->wait());
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testChildPromiseRejectedWithOnFulfilledThrownExceptionWithThenBeforeResolve()
    {
        $value = 'test';
        $exception = new Exception();

        $callback = function ($value) use (&$parameter, $exception) {
            $parameter = $value;
            throw $exception;
        };

        $child = $this->promise->then($callback);

        $this->resolve($value);

        Loop\run();

        $this->assertSame($value, $parameter);
        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testRejectCallable
     */
    public function testChildPromiseRejectedWithOnRejectedThrownExceptionWithThenBeforeReject()
    {
        $exception1 = new Exception('Test Exception 1.');
        $exception2 = new Exception('Test Exception 2.');

        $callback = function ($e) use (&$parameter, $exception2) {
            $parameter = $e;
            throw $exception2;
        };

        $child = $this->promise->then($this->createCallback(0), $callback);

        $this->reject($exception1);

        Loop\run();

        $this->assertSame($exception1, $parameter);
        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception2, $reason);
        }
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testChildPromiseFulfilledWithOnFulfilledReturnValueWithThenAfterResolve()
    {
        $value1 = 'test';
        $value2 = 1;

        $callback = function ($value) use (&$parameter, $value2) {
            $parameter = $value;
            return $value2;
        };

        $this->resolve($value1);

        $child = $this->promise->then($callback);

        Loop\run();

        $this->assertSame($value1, $parameter);
        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value2, $child->wait());
    }

    /**
     * @depends testRejectCallable
     */
    public function testChildPromiseFulfilledWithOnRejectedReturnValueWithThenAfterReject()
    {
        $exception = new Exception();
        $value = 1;

        $callback = function ($e) use (&$parameter, $value) {
            $parameter = $e;
            return $value;
        };

        $this->reject($exception);

        $child = $this->promise->then($this->createCallback(0), $callback);

        Loop\run();

        $this->assertSame($exception, $parameter);
        $this->assertTrue($child->isFulFilled());
        $this->assertSame($value, $child->wait());
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testChildPromiseRejectedWithOnFulfilledThrownExceptionWithThenAfterResolve()
    {
        $value = 'test';
        $exception = new Exception();

        $callback = function ($value) use (&$parameter, $exception) {
            $parameter = $value;
            throw $exception;
        };

        $this->resolve($value);

        $child = $this->promise->then($callback);

        Loop\run();

        $this->assertSame($value, $parameter);
        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testRejectCallable
     */
    public function testChildPromiseRejectedWithOnRejectedThrownExceptionWithThenAfterReject()
    {
        $exception1 = new Exception('Test Exception 1.');
        $exception2 = new Exception('Test Exception 2.');

        $callback = function ($e) use (&$parameter, $exception2) {
            $parameter = $e;
            throw $exception2;
        };

        $this->reject($exception1);

        $child = $this->promise->then($this->createCallback(0), $callback);

        Loop\run();

        $this->assertSame($exception1, $parameter);
        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception2, $reason);
        }
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testFulfilledPromiseFallThroughWithNoOnFulfilled()
    {
        $value = 'test';

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($value));

        $child = $this->promise->then(null);

        $grandchild = $child->then($callback);

        $this->resolve($value);

        Loop\run();

        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value, $child->wait());
        $this->assertFalse($grandchild->isPending());
    }

    /**
     * @depends testRejectCallable
     */
    public function testRejectedPromiseFallThroughWithNoOnRejected()
    {
        $exception = new Exception();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $child = $this->promise->then($this->createCallback(0));

        $grandchild = $child->then($this->createCallback(0), $callback);

        $this->reject($exception);

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }

        $this->assertFalse($grandchild->isPending());
    }

    /**
     * @depends testResolveCallableWithValue
     * @depends testChildPromiseFulfilledWithOnFulfilledReturnValueWithThenBeforeResolve
     */
    public function testChildPromiseResolvedWithPromiseReturnedByOnFulfilled()
    {
        $value1 = 'test1';
        $value2 = 'test2';

        $callback = function ($value) use (&$parameter, $value2) {
            $parameter = $value;
            return Promise\resolve($value2);
        };

        $child = $this->promise->then($callback);

        $this->resolve($value1);

        Loop\run();

        $this->assertSame($value1, $parameter);
        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value2, $child->wait());
    }

    /**
     * @depends testResolveCallableWithValue
     * @depends testChildPromiseFulfilledWithOnFulfilledReturnValueWithThenBeforeResolve
     */
    public function testChildPromiseRejectedWithPromiseReturnedByOnFulfilled()
    {
        $value = 'test';
        $exception = new Exception();

        $callback = function ($value) use (&$parameter, $exception) {
            $parameter = $value;
            return Promise\reject($exception);
        };

        $child = $this->promise->then($callback);

        $this->resolve($value);

        Loop\run();

        $this->assertSame($value, $parameter);
        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testResolveCallableWithValue
     * @depends testChildPromiseFulfilledWithOnRejectedReturnValueWithThenBeforeReject
     */
    public function testChildPromiseResolvedWithPromiseReturnedByOnRejected()
    {
        $value = 'test';
        $exception = new Exception();

        $callback = function ($exception) use (&$parameter, $value) {
            $parameter = $exception;
            return Promise\resolve($value);
        };

        $child = $this->promise->then($this->createCallback(0), $callback);

        $this->reject($exception);

        Loop\run();

        $this->assertSame($exception, $parameter);
        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value, $child->wait());
    }

    /**
     * @depends testResolveCallableWithValue
     * @depends testChildPromiseFulfilledWithOnRejectedReturnValueWithThenBeforeReject
     */
    public function testChildPromiseRejectedWithPromiseReturnedByOnRejected()
    {
        $exception1 = new Exception();
        $exception2 = new Exception();

        $callback = function ($exception) use (&$parameter, $exception2) {
            $parameter = $exception;
            return Promise\reject($exception2);
        };

        $child = $this->promise->then($this->createCallback(0), $callback);

        $this->reject($exception1);

        Loop\run();

        $this->assertSame($exception1, $parameter);
        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception2, $reason);
        }
    }

    /**
     * @depends testRejectCallable
     * @expectedException \Icicle\Promise\Exception\Error
     */
    public function testDoneNoOnRejectedThrowsUncatchableExceptionWithRejectionAfter()
    {
        $exception = new Error();

        $this->promise->done($this->createCallback(0));

        $this->reject($exception);

        Loop\run(); // Exception will be thrown from loop.
    }

    /**
     * @depends testRejectCallable
     * @expectedException \Icicle\Promise\Exception\Error
     */
    public function testDoneNoOnRejectedThrowsUncatchableExceptionWithRejectionBefore()
    {
        $exception = new Error();

        $this->reject($exception);

        $this->promise->done($this->createCallback(0));

        Loop\run(); // Exception will be thrown from loop.
    }

    /**
     * @depends testThenInvokeNewCallbackAfterResolved
     * @depends testDoneInvokeNewCallbackAfterResolved
     */
    public function testCallToOnFulfilledIsAsynchronous()
    {
        $this->resolve();

        Loop\run();

        $this->assertTrue($this->promise->isFulfilled());

        $string = '';

        $callback = function () use (&$string) {
            $string .= '<onFulfilled>';
        };

        $string .= '<before>';

        $this->promise->then($callback);
        $this->promise->done($callback);

        $string .= '<after>';

        Loop\run();

        $this->assertSame('<before><after><onFulfilled><onFulfilled>', $string);
    }

    /**
     * @depends testThenInvokeNewCallbackAfterRejected
     * @depends testDoneInvokeNewCallbackAfterRejected
     */
    public function testCallToOnRejectedIsAsynchronous()
    {
        $this->reject(new Exception());

        Loop\run();

        $this->assertTrue($this->promise->isRejected());

        $string = '';

        $callback = function () use (&$string) {
            $string .= '<onRejected>';
        };

        $string .= '<before>';

        $this->promise->then(null, $callback);
        $this->promise->done(null, $callback);

        $string .= '<after>';

        Loop\run();

        $this->assertSame('<before><after><onRejected><onRejected>', $string);
    }

    /**
     * @depends testRejectCallable
     */
    public function testCapture()
    {
        $exception = new Exception();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $child = $this->promise->capture($callback);

        $this->reject($exception);

        Loop\run();

        $this->assertTrue($child->isFulfilled());
        $this->assertNull($child->wait());
    }

    /**
     * @return array
     */
    public function captureFunctions()
    {
        return [
            [function (RuntimeException $exception) { return $exception; }],
            [[$this, 'instanceExceptionHandler']],
            [[__CLASS__, 'staticExceptionHandler']],
            [__NAMESPACE__ . '\exceptionHandler']
        ];
    }

    /**
     * @depends testCapture
     * @dataProvider captureFunctions
     * @param callable $callback
     */
    public function testCaptureWithMatchingTypeHint(callable $callback)
    {
        $child = $this->promise->capture($callback);

        $exception = new RuntimeException();
        $this->reject($exception);

        Loop\run();

        $this->assertTrue($child->isFulfilled());
        $this->assertSame($exception, $child->wait());
    }

    /**
     * @depends testCapture
     */
    public function testCaptureWithMismatchedTypeHint()
    {
        $child = $this->promise->capture(function (InvalidArgumentException $exception) { return $exception; });

        $exception = new RuntimeException();
        $this->reject($exception);

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    public function testCancellation()
    {
        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(CancelledException::class));

        $promise = new Promise\Promise(function () use ($callback) { return $callback; });

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(CancelledException::class));

        $promise->done($this->createCallback(0), $callback);

        $this->assertFalse($promise->isCancelled());

        $promise->cancel();

        Loop\run();

        $this->assertTrue($promise->isCancelled());
        $this->assertFalse($promise->isFulfilled());
        $this->assertTrue($promise->isRejected());
    }

    /**
     * @depends testCancellation
     */
    public function testThenAfterCancel()
    {
        $exception = new Exception();

        $promise = new Promise\Promise(function () {});

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $promise->cancel($exception);

        $promise = $promise->then();

        $promise->done($this->createCallback(0), $callback);

        Loop\run();

        $this->assertTrue($promise->isCancelled());
        $this->assertFalse($promise->isFulfilled());
        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testThenAfterCancel
     */
    public function testThenAfterCancelWithCallback()
    {
        $exception = new Exception();

        $promise = new Promise\Promise(function () {});

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->will($this->throwException($exception));

        $promise->cancel();

        $child = $promise->then($this->createCallback(0), $callback);

        Loop\run();

        $this->assertFalse($child->isCancelled());
        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCancellation
     */
    public function testOnCancelledThrowsException()
    {
        $exception = new Exception();

        $callback = function () use ($exception) {
            throw $exception;
        };

        $promise = new Promise\Promise(function () use ($callback) { return $callback; });

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $promise->done($this->createCallback(0), $callback);

        $promise->cancel();

        Loop\run();

        $this->assertTrue($promise->isCancelled());
        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCancellation
     */
    public function testOnCancelledReturnsPromiseThatFulfills()
    {
        $callback = function () {
            return $this->promise;
        };

        $promise = new Promise\Promise(function () use ($callback) { return $callback; });

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(CancelledException::class));

        $promise->done($this->createCallback(0), $callback);

        $promise->cancel();

        Loop\run();

        $this->assertFalse($promise->isPending());
        $this->assertTrue($promise->isCancelled());
        $this->assertFalse($promise->isRejected());

        $this->resolve(1);

        Loop\run();

        $this->assertTrue($promise->isRejected());
    }

    /**
     * @depends testOnCancelledReturnsPromiseThatFulfills
     */
    public function testOnCancelledReturnsPromiseThatRejects()
    {
        $exception = new Exception();

        $callback = function () use ($exception) {
            return $this->promise;
        };

        $promise = new Promise\Promise(function () use ($callback) { return $callback; });

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $promise->done($this->createCallback(0), $callback);

        $promise->cancel();

        Loop\run();

        $this->assertFalse($promise->isPending());
        $this->assertTrue($promise->isCancelled());
        $this->assertFalse($promise->isRejected());

        $this->reject($exception);

        Loop\run();

        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCancellation
     */
    public function testCancellationWithSpecificException()
    {
        $exception = new Exception();

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $promise = new Promise\Promise(function () use ($callback) { return $callback; });

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $promise->done($this->createCallback(0), $callback);

        $promise->cancel($exception);

        Loop\run();
    }

    /**
     * @depends testCancellation
     */
    public function testCancellationWithValueReason()
    {
        $reason = 'String to cancel promise.';

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(CancelledException::class));

        $promise = new Promise\Promise(function () use ($callback) { return $callback; });

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(CancelledException::class));

        $promise->done($this->createCallback(0), $callback);

        $promise->cancel($reason);

        Loop\run();

        try {
            $promise->wait();
        } catch (Exception $exception) {
            $this->assertSame($reason, $exception->getReason());
        }
    }

    /**
     * @depends testCancellation
     */
    public function testCancellingParentRejectsChild()
    {
        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(CancelledException::class));

        $child = $this->promise->then();
        $child->done(null, $callback);

        $this->promise->cancel();

        Loop\run();

        $this->assertTrue($this->promise->isRejected());
        $this->assertTrue($child->isRejected());
    }

    /**
     * @depends testCancellation
     */
    public function testCancellingOnlyChildCancelsParent()
    {
        $child = $this->promise->then();

        $child->cancel();

        Loop\run();

        $this->assertFalse($this->promise->isPending());
        $this->assertTrue($this->promise->isRejected());
        $this->assertTrue($child->isRejected());
    }

    /**
     * @depends testCancellation
     */
    public function testCancellingSiblingChildDoesNotCancelParent()
    {
        $child1 = $this->promise->then();
        $child2 = $this->promise->then();

        $child1->cancel();

        Loop\run();

        $this->assertTrue($this->promise->isPending());
        $this->assertTrue($child1->isRejected());
        $this->assertTrue($child2->isPending());
    }

    /**
     * @depends testCancellingSiblingChildDoesNotCancelParent
     */
    public function testCancellingAllChildrenCancelsParent()
    {
        $child1 = $this->promise->then();
        $child2 = $this->promise->then();

        $child1->cancel();
        $child2->cancel();

        Loop\run();

        $this->assertFalse($this->promise->isPending());
        $this->assertTrue($child1->isRejected());
        $this->assertTrue($child2->isRejected());
    }

    /**
     * @depends testCancellation
     */
    public function testCancellingParentCancelsAllChildren()
    {
        $child1 = $this->promise->then();
        $child2 = $this->promise->then();

        $this->promise->cancel();

        Loop\run();

        $this->assertFalse($this->promise->isPending());
        $this->assertTrue($child1->isRejected());
        $this->assertTrue($child2->isRejected());
    }

    /**
     * @depends testCancellation
     */
    public function testCancellingSiblingsThenCreateSiblingPromise()
    {
        $child1 = $this->promise->then();
        $child2 = $this->promise->then();

        $child1->cancel();
        $child2->cancel();

        $child3 = $this->promise->then();

        Loop\run();

        $this->assertTrue($this->promise->isPending());
        $this->assertTrue($child3->isPending());
    }

    /**
     * @depends testResolveCallableWithPendingPromise
     */
    public function testCancellationAfterResolvingWithPendingPromise()
    {
        $exception = new Exception();

        $promise = new Promise\Promise(function () {});

        $this->resolve($promise);

        $this->promise->cancel($exception);

        Loop\run();

        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCancellation
     */
    public function testCancellationPreventsResolveInCancellationCallable()
    {
        $exception = new Exception();

        $promise = new Promise\Promise(function ($resolve) {
            return function () use ($resolve) {
                $resolve(1);
            };
        });

        $promise->cancel($exception);

        Loop\run();

        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCancellationPreventsResolveInCancellationCallable
     */
    public function testCancellationPreventsRejectInCancellationCallable()
    {
        $exception = new Exception();

        $promise = new Promise\Promise(function ($resolve, $reject) {
            return function () use ($reject) {
                $reject(new Exception());
            };
        });

        $promise->cancel($exception);

        Loop\run();

        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCancellation
     */
    public function testDoubleCancel()
    {
        $exception = new Exception();

        $promise = new Promise\Promise(function () {});

        $this->resolve($promise);

        $this->promise->cancel($exception);
        $this->promise->cancel(new Exception()); // Should have no effect.

        Loop\run();

        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCancellation
     */
    public function testDelayAfterCancel()
    {
        $exception = new Exception();

        $promise = new Promise\Promise(function () {});

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $promise->cancel($exception);

        $promise = $promise->delay(1);

        $promise->done($this->createCallback(0), $callback);

        Loop\run();

        $this->assertTrue($promise->isCancelled());
        $this->assertFalse($promise->isFulfilled());
        $this->assertTrue($promise->isRejected());

        try {
            $promise->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCancellation
     */
    public function testTimeout()
    {
        $time = 0.1;

        $timeout = $this->promise->timeout($time);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(TimeoutException::class));

        $timeout->done($this->createCallback(0), $callback);

        $this->assertRunTimeGreaterThan('Icicle\Loop\run', $time);

        $this->assertTrue($this->promise->isRejected());

        try {
            $this->promise->wait();
        } catch (Exception $reason) {
            $this->assertInstanceOf(TimeoutException::class, $reason);
        }
    }

    /**
     * @depends testTimeout
     */
    public function testTimeoutWithSpecificException()
    {
        $time = 0.1;
        $exception = new Exception();

        $timeout = $this->promise->timeout($time, $exception);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($exception));

        $timeout->done($this->createCallback(0), $callback);

        $this->assertRunTimeGreaterThan('Icicle\Loop\run', $time);
    }

    /**
     * @depends testTimeout
     */
    public function testTimeoutWithValueReason()
    {
        $time = 0.1;
        $reason = 'String to timeout promise.';

        $timeout = $this->promise->timeout($time, $reason);

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(TimeoutException::class));

        $timeout->done($this->createCallback(0), $callback);

        $this->assertRunTimeGreaterThan('Icicle\Loop\run', $time);

        try {
            $this->promise->wait();
        } catch (Exception $exception) {
            $this->assertSame($reason, $exception->getReason());
        }

        try {
            $timeout->wait();
        } catch (Exception $exception) {
            $this->assertSame($reason, $exception->getReason());
        }
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testTimeoutThenFulfill()
    {
        $value = 'test';
        $time = 0.1;

        $timeout = $this->promise->timeout($time);

        $this->resolve($value);

        $this->assertRunTimeLessThan('Icicle\Loop\run', $time);

        $this->assertTrue($timeout->isFulfilled());
        $this->assertSame($value, $timeout->wait());
    }

    /**
     * @depends testRejectCallable
     */
    public function testTimeoutThenReject()
    {
        $exception = new Exception();
        $time = 0.1;

        $timeout = $this->promise->timeout($time);

        $this->reject($exception);

        $this->assertRunTimeLessThan('Icicle\Loop\run', $time);

        $this->assertTrue($timeout->isRejected());

        try {
            $timeout->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testTimeoutAfterFulfilled()
    {
        $value = 'test';
        $time = 0.1;

        $this->resolve($value);

        $timeout = $this->promise->timeout($time);

        $this->assertRunTimeLessThan('Icicle\Loop\run', $time);

        $this->assertTrue($timeout->isFulfilled());
        $this->assertSame($value, $timeout->wait());
    }

    /**
     * @depends testRejectCallable
     */
    public function testTimeoutAfterRejected()
    {
        $exception = new Exception();
        $time = 0.1;

        $this->reject($exception);

        $timeout = $this->promise->timeout($time);

        $this->assertRunTimeLessThan('Icicle\Loop\run', $time);

        $this->assertTrue($timeout->isRejected());

        try {
            $timeout->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testTimeout
     * @depends testCancellation
     */
    public function testCancelTimeout()
    {
        $time = 0.1;

        $timeout = $this->promise->timeout($time);

        $timeout->cancel();

        $this->assertRunTimeLessThan('Icicle\Loop\run', $time);

        $this->assertTrue($timeout->isRejected());
        $this->assertTrue($this->promise->isRejected());
    }

    /**
     * @depends testCancelTimeout
     */
    public function testCancelTimeoutOnSibling()
    {
        $time = 0.1;

        $timeout = $this->promise->timeout($time);
        $sibling = $this->promise->then();

        $timeout->cancel();

        $this->assertRunTimeLessThan('Icicle\Loop\run', $time);

        $this->assertTrue($timeout->isRejected());
        $this->assertTrue($this->promise->isPending());
        $this->assertTrue($sibling->isPending());
    }

    /**
     * @depends testCancellation
     */
    public function testCancelTimeoutAndCancelSiblingPromise()
    {
        $time = 0.1;

        $timeout = $this->promise->timeout($time);
        $sibling = $this->promise->then();

        $timeout->cancel();
        $sibling->cancel();

        Loop\run();

        $this->assertTrue($timeout->isRejected());
        $this->assertFalse($this->promise->isPending());
        $this->assertTrue($sibling->isRejected());
    }

    /**
     * @depends testCancellation
     */
    public function testCancelTimeoutThenCreateSiblingPromise()
    {
        $time = 0.1;

        $timeout = $this->promise->delay($time);

        $timeout->cancel();

        $sibling = $this->promise->then();

        Loop\run();

        $this->assertTrue($timeout->isRejected());
        $this->assertTrue($this->promise->isPending());
        $this->assertTrue($sibling->isPending());
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testDelayThenFulfill()
    {
        $value = 'test';
        $time = 0.1;

        $delayed = $this->promise->delay($time);

        $this->resolve($value);

        $this->assertRunTimeGreaterThan('Icicle\Loop\run', $time);

        $this->assertTrue($delayed->isFulfilled());
        $this->assertSame($value, $delayed->wait());
    }

    /**
     * @depends testRejectCallable
     */
    public function testDelayThenReject()
    {
        $exception = new Exception();
        $time = 0.1;

        $delayed = $this->promise->delay($time);

        $this->reject($exception);

        $this->assertRunTimeLessThan('Icicle\Loop\run', $time);

        $this->assertTrue($delayed->isRejected());

        try {
            $delayed->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testDelayAfterFulfilled()
    {
        $value = 'test';
        $time = 0.1;

        $this->resolve($value);

        $delayed = $this->promise->delay($time);

        $this->assertRunTimeGreaterThan('Icicle\Loop\run', $time);

        $this->assertTrue($delayed->isFulfilled());
        $this->assertSame($value, $delayed->wait());
    }

    /**
     * @depends testRejectCallable
     */
    public function testDelayAfterRejected()
    {
        $exception = new Exception();
        $time = 0.1;

        $this->reject($exception);

        $delayed = $this->promise->delay($time);

        $this->assertRunTimeLessThan('Icicle\Loop\run', $time);

        $this->assertTrue($delayed->isRejected());

        try {
            $delayed->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testResolveCallableWithPendingPromise
     */
    public function testDelayAfterResolvingWithPendingPromise()
    {
        $value = 'test';
        $time = 0.1;

        $promise = new Promise\Promise(function ($resolve) use (&$pendingResolve) {
            $pendingResolve = $resolve;
        });

        $this->resolve($promise);

        $delayed = $this->promise->delay($time);

        $pendingResolve($value);

        $this->assertRunTimeGreaterThan('Icicle\Loop\run', $time);

        $this->assertTrue($delayed->isFulfilled());
        $this->assertSame($value, $delayed->wait());
    }

    /**
     * @depends testResolveCallableWithValue
     * @depends testCancellation
     */
    public function testCancelDelayBeforeFulfilled()
    {
        $value = 'test';
        $time = 0.1;

        $delayed = $this->promise->delay($time);

        $this->resolve($value);

        Loop\tick(false);

        $delayed->cancel();

        Loop\run();

        $this->assertTrue($delayed->isRejected());
        $this->assertTrue($this->promise->isFulfilled());
    }

    /**
     * @depends testResolveCallableWithValue
     * @depends testCancellation
     */
    public function testCancelDelayAfterFulfilled()
    {
        $value = 'test';
        $time = 0.1;

        $this->resolve($value);

        $delayed = $this->promise->delay($time);

        Loop\tick(false);

        $this->assertTrue($this->promise->isFulfilled());
        $this->assertTrue($delayed->isPending());

        $delayed->cancel();

        Loop\run();

        $this->assertTrue($delayed->isRejected());
        $this->assertTrue($this->promise->isFulfilled());
    }

    /**
     * @depends testCancellation
     */
    public function testCancelDelay()
    {
        $time = 0.1;

        $delayed = $this->promise->delay($time);

        $delayed->cancel();

        Loop\run();

        $this->assertTrue($delayed->isRejected());
        $this->assertTrue($this->promise->isRejected());
    }

    /**
     * @depends testCancellation
     */
    public function testCancelDelayWithSiblingPromise()
    {
        $time = 0.1;

        $delayed = $this->promise->delay($time);
        $sibling = $this->promise->then();

        $delayed->cancel();

        Loop\run();

        $this->assertTrue($delayed->isRejected());
        $this->assertTrue($this->promise->isPending());
        $this->assertTrue($sibling->isPending());
    }

    /**
     * @depends testCancellation
     */
    public function testCancelDelayAndCancelSiblingPromise()
    {
        $time = 0.1;

        $delayed = $this->promise->delay($time);
        $sibling = $this->promise->then();

        $delayed->cancel();
        $sibling->cancel();

        Loop\run();

        $this->assertTrue($delayed->isRejected());
        $this->assertFalse($this->promise->isPending());
        $this->assertTrue($sibling->isRejected());
    }

    /**
     * @depends testCancellation
     */
    public function testCancelDelayThenCreateSiblingPromise()
    {
        $time = 0.1;

        $delayed = $this->promise->delay($time);

        $delayed->cancel();

        $sibling = $this->promise->then();

        Loop\run();

        $this->assertTrue($delayed->isRejected());
        $this->assertTrue($this->promise->isPending());
        $this->assertTrue($sibling->isPending());
    }


    /**
     * @depends testResolveCallableWithValue
     */
    public function testTapAfterFulfilled()
    {
        $value = 'test';

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo($value));

        $child = $this->promise->tap($callback);

        $this->resolve($value);

        Loop\run();

        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value, $child->wait());
    }

    /**
     * @depends testRejectCallable
     */
    public function testTapAfterRejected()
    {
        $exception = new Exception();

        $child = $this->promise->tap($this->createCallback(0));

        $this->reject($exception);

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testTapAfterFulfilled
     * @depends testDelayThenFulfill
     */
    public function testTapCallbackReturnsPendingPromise()
    {
        $value = 'test';
        $time = 0.1;

        $callback = function () use ($time) {
            return Promise\resolve()->delay($time);
        };

        $child = $this->promise->tap($callback);

        $this->resolve($value);

        $this->assertRunTimeGreaterThan('Icicle\Loop\run', $time);

        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value, $child->wait());
    }

    /**
     * @depends testTapAfterFulfilled
     */
    public function testTapCallbackReturnsRejectedPromise()
    {
        $value = 'test';
        $exception = new Exception();

        $callback = function () use ($exception) {
            return Promise\reject($exception);
        };

        $child = $this->promise->tap($callback);

        $this->resolve($value);

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testTapAfterFulfilled
     */
    public function testTapCallbackThrowsException()
    {
        $value = 'test';
        $exception = new Exception();

        $callback = function () use ($exception) {
            throw $exception;
        };

        $child = $this->promise->tap($callback);

        $this->resolve($value);

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testResolveCallableWithValue
     */
    public function testCleanupAfterFulfilled()
    {
        $value = 'test';

        $child = $this->promise->cleanup($this->createCallback(1));

        $this->resolve($value);

        Loop\run();

        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value, $child->wait());
    }

    /**
     * @depends testRejectCallable
     */
    public function testCleanupAfterRejected()
    {
        $exception = new Exception();

        $child = $this->promise->cleanup($this->createCallback(1));

        $this->reject($exception);

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCleanupAfterFulfilled
     * @depends testDelayThenFulfill
     */
    public function testCleanupAfterFulfilledCallbackReturnsPendingPromise()
    {
        $value = 'test';
        $time = 0.1;

        $callback = function () use ($time) {
            return Promise\resolve()->delay($time);
        };

        $child = $this->promise->cleanup($callback);

        $this->resolve($value);

        $this->assertRunTimeGreaterThan('Icicle\Loop\run', $time);

        $this->assertTrue($child->isFulfilled());
        $this->assertSame($value, $child->wait());
    }

    /**
     * @depends testTapAfterFulfilled
     */
    public function testCleanupAfterFulfilledCallbackReturnsRejectedPromise()
    {
        $value = 'test';
        $exception = new Exception();

        $callback = function () use ($exception) {
            return Promise\reject($exception);
        };

        $child = $this->promise->cleanup($callback);

        $this->resolve($value);

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testTapAfterFulfilled
     */
    public function testCleanupAfterFulfilledCallbackThrowsException()
    {
        $value = 'test';
        $exception = new Exception();

        $callback = function () use ($exception) {
            throw $exception;
        };

        $child = $this->promise->cleanup($callback);

        $this->resolve($value);

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testCleanupAfterRejected
     * @depends testDelayThenFulfill
     */
    public function testCleanupAfterRejectedCallbackReturnsPendingPromise()
    {
        $exception = new Exception();
        $time = 0.1;

        $callback = function () use ($time) {
            return Promise\resolve()->delay($time);
        };

        $child = $this->promise->cleanup($callback);

        $this->reject($exception);

        $this->assertRunTimeGreaterThan('Icicle\Loop\run', $time);

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testTapAfterFulfilled
     */
    public function testCleanupAfterRejectedCallbackReturnsRejectedPromise()
    {
        $value = 'test';
        $exception = new Exception();

        $callback = function () use ($exception) {
            return Promise\reject($exception);
        };

        $child = $this->promise->cleanup($callback);

        $this->reject();

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    /**
     * @depends testTapAfterFulfilled
     */
    public function testCleanupAfterRejectedCallbackThrowsException()
    {
        $exception = new Exception();

        $callback = function () use ($exception) {
            throw $exception;
        };

        $child = $this->promise->cleanup($callback);

        $this->reject();

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    public function testSplat()
    {
        $values = [1, 'test', 3.14];

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo(1), $this->identicalTo('test'), $this->identicalTo(3.14));

        $child = $this->promise->splat($callback);

        $this->resolve($values);

        Loop\run();

        $this->assertTrue($child->isFulfilled());
    }

    /**
     * @return array
     */
    public function getInvalidSplatValues()
    {
        return [
            ['test'],
            [3.14],
            [0],
            [new \stdClass()],
            [null],
        ];
    }

    /**
     * @dataProvider getInvalidSplatValues
     * @depends testSplat
     *
     * @param mixed $value
     */
    public function testSplatWithNonArray($value)
    {
        $child = $this->promise->splat($this->createCallback(0));

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->isInstanceOf(UnexpectedTypeError::class));

        $child->done($this->createCallback(0), $callback);

        $this->resolve($value);

        Loop\run();

        $this->assertTrue($child->isRejected());
    }

    /**
     * @depends testSplat
     */
    public function testSplatWithRejectedPromise()
    {
        $exception = new Exception();

        $callback = $this->createCallback(0);

        $child = $this->promise->splat($callback);

        $this->reject($exception);

        Loop\run();

        $this->assertTrue($child->isRejected());

        try {
            $child->wait();
        } catch (Exception $reason) {
            $this->assertSame($exception, $reason);
        }
    }

    public function testSplatWithTraversable()
    {
        $values = [1, 'test', 3.14];

        $traversable = function () use ($values) {
            foreach ($values as $value) {
                yield $value;
            }
        };

        $callback = $this->createCallback(1);
        $callback->method('__invoke')
            ->with($this->identicalTo(1), $this->identicalTo('test'), $this->identicalTo(3.14));

        $child = $this->promise->splat($callback);

        $this->resolve($traversable());

        Loop\run();

        $this->assertTrue($child->isFulfilled());
    }

    public function testWaitOnFulfilledPromise()
    {
        $value = 'test';

        $this->resolve($value);

        $result = $this->promise->wait();

        $this->assertSame($value, $result);
    }

    public function testWaitOnRejectedPromise()
    {
        $exception = new Exception();

        $this->reject($exception);

        try {
            $result = $this->promise->wait();
            $this->fail('Rejection exception should be thrown from wait().');
        } catch (Exception $e) {
            $this->assertSame($exception, $e);
        }
    }

    /**
     * @depends testWaitOnFulfilledPromise
     */
    public function testWaitOnPendingPromise()
    {
        $value = 'test';

        $promise = $this->promise->delay(0.1);

        $this->resolve($value);

        $this->assertTrue($promise->isPending());

        $result = $promise->wait();

        $this->assertSame($value, $result);
    }

    /**
     * @expectedException \Icicle\Promise\Exception\UnresolvedError
     */
    public function testPromiseWithNoResolutionPathThrowsException()
    {
        $promise = new Promise\Promise(function () {});

        $result = $promise->wait();
    }
}