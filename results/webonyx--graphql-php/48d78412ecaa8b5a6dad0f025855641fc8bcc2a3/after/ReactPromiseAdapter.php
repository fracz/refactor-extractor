<?php
namespace GraphQL\Executor\Promise\Adapter;

use GraphQL\Executor\Promise\Promise;
use GraphQL\Executor\Promise\PromiseAdapter;
use GraphQL\Utils;
use React\Promise\Promise as ReactPromise;
use React\Promise\PromiseInterface as ReactPromiseInterface;

class ReactPromiseAdapter implements PromiseAdapter
{
    /**
     * @inheritdoc
     */
    public function isThenable($value)
    {
        return $value instanceof ReactPromiseInterface;
    }

    /**
     * @inheritdoc
     */
    public function convert($promise)
    {
        return new Promise($promise, $this);
    }

    /**
     * @inheritdoc
     */
    public function then(Promise $promise, callable $onFulfilled = null, callable $onRejected = null)
    {
        /** @var $adoptedPromise ReactPromiseInterface */
        $adoptedPromise = $promise->adoptedPromise;
        return new Promise($adoptedPromise->then($onFulfilled, $onRejected), $this);
    }

    /**
     * @inheritdoc
     */
    public function createPromise(callable $resolver)
    {
        $promise = new ReactPromise($resolver);
        return new Promise($promise, $this);
    }

    /**
     * @inheritdoc
     */
    public function createResolvedPromise($value = null)
    {
        $promise = \React\Promise\resolve($value);
        return new Promise($promise, $this);
    }

    /**
     * @inheritdoc
     */
    public function createRejectedPromise(\Exception $reason)
    {
        $promise = \React\Promise\reject($reason);
        return new Promise($promise, $this);
    }

    /**
     * @inheritdoc
     */
    public function createPromiseAll(array $promisesOrValues)
    {
        // TODO: rework with generators when PHP minimum required version is changed to 5.5+
        $promisesOrValues = Utils::map($promisesOrValues, function ($item) {
            return $item instanceof Promise ? $item->adoptedPromise : $item;
        });
        $promise = \React\Promise\all($promisesOrValues);
        return new Promise($promise, $this);
    }
}