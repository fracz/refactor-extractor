<?php

/*
 * This file is part of Icicle, a library for writing asynchronous code in PHP using coroutines built with awaitables.
 *
 * @copyright 2014-2015 Aaron Piotrowski. All rights reserved.
 * @license MIT See the LICENSE file that was distributed with this source code for more information.
 */

namespace Icicle\Loop\Manager\Event;

use Event;
use EventBase;
use Icicle\Loop\EventLoop;
use Icicle\Loop\Events\SocketEvent;
use Icicle\Loop\Exception\FreedError;
use Icicle\Loop\Exception\ResourceBusyError;
use Icicle\Loop\Manager\SocketManager;

class EventSocketManager implements SocketManager
{
    const MIN_TIMEOUT = 0.001;

    /**
     * @var \Icicle\Loop\EventLoop
     */
    private $loop;

    /**
     * @var \EventBase
     */
    private $base;

    /**
     * @var \Event[]
     */
    private $events = [];

    /**
     * @var \Icicle\Loop\Events\SocketEvent[]
     */
    private $sockets = [];

    /**
     * @var \Icicle\Loop\Events\SocketEvent[]
     */
    private $unreferenced = [];

    /**
     * @var callable
     */
    private $callback;

    /**
     * @var int
     */
    private $type;

    /**
     * @param \Icicle\Loop\EventLoop $loop
     * @param int $eventType
     */
    public function __construct(EventLoop $loop, $eventType)
    {
        $this->loop = $loop;
        $this->base = $this->loop->getEventBase();
        $this->type = $eventType;

        $this->callback = function ($resource, $what, SocketEvent $socket) {
            $socket->call(0 !== (Event::TIMEOUT & $what));
        };
    }

    /**
     * @codeCoverageIgnore
     */
    public function __destruct()
    {
        foreach ($this->events as $event) {
            $event->free();
        }
    }

    /**
     * {@inheritdoc}
     */
    public function isEmpty()
    {
        foreach ($this->events as $id => $event) {
            if ($event->pending && !isset($this->unreferenced[$id])) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritdoc}
     */
    public function create($resource, callable $callback)
    {
        $id = (int) $resource;

        if (isset($this->sockets[$id])) {
            throw new ResourceBusyError('A socket event has already been created for that resource.');
        }

        return $this->sockets[$id] = new SocketEvent($this, $resource, $callback);
    }

    /**
     * {@inheritdoc}
     */
    public function listen(SocketEvent $socket, $timeout = 0)
    {
        $id = (int) $socket->getResource();

        if (!isset($this->sockets[$id]) || $socket !== $this->sockets[$id]) {
            throw new FreedError('Socket event has been freed.');
        }

        if (!isset($this->events[$id])) {
            $this->events[$id] = new Event($this->base, $socket->getResource(), $this->type, $this->callback, $socket);
        }

        if (0 === $timeout) {
            $this->events[$id]->add();
            return;
        }

        $timeout = (float) $timeout;
        if (self::MIN_TIMEOUT > $timeout) {
            $timeout = self::MIN_TIMEOUT;
        }

        $this->events[$id]->add($timeout);
    }

    /**
     * {@inheritdoc}
     */
    public function cancel(SocketEvent $socket)
    {
        $id = (int) $socket->getResource();

        if (isset($this->sockets[$id], $this->events[$id]) && $socket === $this->sockets[$id]) {
            $this->events[$id]->del();
        }
    }

    /**
     * {@inheritdoc}
     */
    public function isPending(SocketEvent $socket)
    {
        $id = (int) $socket->getResource();

        return isset($this->sockets[$id], $this->events[$id])
            && $socket === $this->sockets[$id]
            && $this->events[$id]->pending;
    }

    /**
     * {@inheritdoc}
     */
    public function free(SocketEvent $socket)
    {
        $id = (int) $socket->getResource();

        if (isset($this->sockets[$id]) && $socket === $this->sockets[$id]) {
            unset($this->sockets[$id], $this->unreferenced[$id]);

            if (isset($this->events[$id])) {
                $this->events[$id]->free();
                unset($this->events[$id]);
            }
        }
    }

    /**
     * {@inheritdoc}
     */
    public function isFreed(SocketEvent $socket)
    {
        $id = (int) $socket->getResource();

        return !isset($this->sockets[$id]) || $socket !== $this->sockets[$id];
    }


    /**
     * {@inheritdoc}
     */
    public function reference(SocketEvent $socket)
    {
        unset($this->unreferenced[(int) $socket->getResource()]);
    }

    /**
     * {@inheritdoc}
     */
    public function unreference(SocketEvent $socket)
    {
        $id = (int) $socket->getResource();

        if (isset($this->events[$id]) && $socket === $this->sockets[$id]) {
            $this->unreferenced[$id] = $socket;
        }
    }

    /**
     * {@inheritdoc}
     */
    public function clear()
    {
        foreach ($this->events as $event) {
            $event->free();
        }

        $this->events = [];
        $this->sockets = [];
    }
}