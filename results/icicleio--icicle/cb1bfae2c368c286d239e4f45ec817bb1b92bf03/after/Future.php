<?php

/*
 * This file is part of Icicle, a library for writing asynchronous code in PHP using promises and coroutines.
 *
 * @copyright 2014-2015 Aaron Piotrowski. All rights reserved.
 * @license MIT See the LICENSE file that was distributed with this source code for more information.
 */

namespace Icicle\Promise;

use Exception;
use Icicle\Loop;
use Icicle\Promise\Exception\CircularResolutionError;
use Icicle\Promise\Exception\TimeoutException;
use Icicle\Promise\Exception\UnresolvedError;
use Icicle\Promise\Internal\CancelledPromise;
use Icicle\Promise\Internal\FulfilledPromise;
use Icicle\Promise\Internal\RejectedPromise;
use Icicle\Promise\Internal\ThenQueue;

/**
 * Promise implementation based on the Promises/A+ specification adding support for cancellation.
 *
 * @see http://promisesaplus.com
 */
class Future implements Thenable
{
    use Internal\SharedMethods;

    /**
     * @var \Icicle\Promise\Thenable|null
     */
    private $result;

    /**
     * @var callable|\Icicle\Promise\Internal\ThenQueue|null
     */
    private $onFulfilled;

    /**
     * @var callable|\Icicle\Promise\Internal\ThenQueue|null
     */
    private $onRejected;

    /**
     * @var callable|null
     */
    private $onCancelled;

    /**
     * @var int
     */
    private $children = 0;

    /**
     * @param callable|null $onCancelled
     */
    public function __construct(callable $onCancelled = null)
    {
        $this->onCancelled = $onCancelled;
    }

    /**
     * Resolves the promise with the given promise or value. If another promise, this promise takes on the state of
     * that promise. If a value, the promise will be fulfilled with that value.
     *
     * @param mixed $value A promise can be resolved with anything other than itself.
     */
    protected function resolve($result)
    {
        if (null !== $this->result) {
            return;
        }

        if ($result instanceof self) {
            $result = $result->unwrap();
            if ($this === $result) {
                $result = new RejectedPromise(
                    new CircularResolutionError('Circular reference in promise resolution chain.')
                );
            }
        } elseif (!$result instanceof Thenable) {
            $result = new FulfilledPromise($result);
        }

        $this->result = $result;
        $this->result->done($this->onFulfilled, $this->onRejected ?: new ThenQueue());

        $this->onFulfilled = null;
        $this->onRejected = null;
        $this->onCancelled = null;
    }

    /**
     * Rejects the promise with the given exception.
     *
     * @param mixed $reason
     */
    protected function reject($reason = null)
    {
        $this->resolve(new RejectedPromise($reason));
    }

    /**
     * {@inheritdoc}
     */
    public function then(callable $onFulfilled = null, callable $onRejected = null)
    {
        if (null !== $this->result) {
            return $this->unwrap()->then($onFulfilled, $onRejected);
        }

        if (null === $onFulfilled && null === $onRejected) {
            return $this;
        }

        ++$this->children;

        $future = new self(function (Exception $exception) {
            if (0 === --$this->children) {
                $this->cancel($exception);
            }
        });

        if (null !== $onFulfilled) {
            $onFulfilled = function ($value) use ($future, $onFulfilled) {
                try {
                    $future->resolve($onFulfilled($value));
                } catch (Exception $exception) {
                    $future->reject($exception);
                }
            };
        } else {
            $onFulfilled = function () use ($future) {
                $future->resolve($this->result);
            };
        }

        if (null !== $onRejected) {
            $onRejected = function (Exception $exception) use ($future, $onRejected) {
                try {
                    $future->resolve($onRejected($exception));
                } catch (Exception $exception) {
                    $future->reject($exception);
                }
            };
        } else {
            $onRejected = function () use ($future) {
                $future->resolve($this->result);
            };
        }

        $this->done($onFulfilled, $onRejected);

        return $future;
    }

    /**
     * {@inheritdoc}
     */
    public function done(callable $onFulfilled = null, callable $onRejected = null)
    {
        if (null !== $this->result) {
            $this->result->done($onFulfilled, $onRejected);
            return;
        }

        if (null === $onRejected) {
            $onRejected = function (Exception $exception) {
                throw $exception;
            };
        }

        if (null !== $onFulfilled) {
            if (null === $this->onFulfilled) {
                $this->onFulfilled = $onFulfilled;
            } elseif (!$this->onFulfilled instanceof ThenQueue) {
                $this->onFulfilled = new ThenQueue($this->onFulfilled);
                $this->onFulfilled->push($onFulfilled);
            } else {
                $this->onFulfilled->push($onFulfilled);
            }
        }

        if (null === $this->onRejected) {
            $this->onRejected = $onRejected;
        } elseif (!$this->onRejected instanceof ThenQueue) {
            $this->onRejected = new ThenQueue($this->onRejected);
            $this->onRejected->push($onRejected);
        } else {
            $this->onRejected->push($onRejected);
        }
    }

    /**
     * {@inheritdoc}
     */
    public function cancel($reason = null)
    {
        if (null !== $this->result) {
            $this->unwrap()->cancel($reason);
            return;
        }

        $this->resolve(new CancelledPromise($reason, $this->onCancelled));
    }

    /**
     * {@inheritdoc}
     */
    public function timeout($timeout, $reason = null)
    {
        if (null !== $this->result) {
            return $this->unwrap()->timeout($timeout, $reason);
        }

        ++$this->children;

        $timer = Loop\timer($timeout, function () use ($reason) {
            if (!$reason instanceof Exception) {
                $reason = new TimeoutException($reason);
            }
            $this->cancel($reason);
        });

        $future = new self(function (Exception $exception) use (&$timer) {
            $timer->stop();

            if (0 === --$this->children) {
                $this->cancel($exception);
            }
        });

        $onResolved = function () use ($future, $timer) {
            $future->resolve($this->result);
            $timer->stop();
        };

        $this->done($onResolved, $onResolved);

        return $future;
    }

    /**
     * {@inheritdoc}
     */
    public function delay($time)
    {
        if (null !== $this->result) {
            return $this->unwrap()->delay($time);
        }

        ++$this->children;

        $future = new Future(function (Exception $exception) use (&$timer) {
            if (null !== $timer) {
                $timer->stop();
            }

            if (0 === --$this->children) {
                $this->cancel($exception);
            }
        });

        $onFulfilled = function () use (&$timer, $time, $future) {
            $timer = Loop\timer($time, function () use ($future) {
                $future->resolve($this->result);
            });
        };

        $onRejected = function () use ($future) {
            $future->resolve($this->result);
        };

        $this->done($onFulfilled, $onRejected);

        return $future;
    }

    /**
     * {@inheritdoc}
     */
    public function wait()
    {
        while (null === $this->result) {
            if (Loop\isEmpty()) {
                throw new UnresolvedError('Loop emptied without resolving promise.');
            }

            Loop\tick(true);
        }

        return $this->unwrap()->wait();
    }

    /**
     * {@inheritdoc}
     */
    public function isPending()
    {
        return null === $this->result ?: $this->unwrap()->isPending();
    }

    /**
     * {@inheritdoc}
     */
    public function isFulfilled()
    {
        return null !== $this->result ? $this->unwrap()->isFulfilled() : false;
    }

    /**
     * {@inheritdoc}
     */
    public function isRejected()
    {
        return null !== $this->result ? $this->unwrap()->isRejected() : false;
    }

    /**
     * {@inheritdoc}
     */
    public function isCancelled()
    {
        return null !== $this->result ? $this->unwrap()->isCancelled() : false;
    }

    /**
     * {@inheritdoc}
     */
    private function unwrap()
    {
        while ($this->result instanceof self && null !== $this->result->result) {
            $this->result = $this->result->result;
        }

        return $this->result ?: $this;
    }
}